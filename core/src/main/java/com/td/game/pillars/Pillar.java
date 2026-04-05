package com.td.game.pillars;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.td.game.combat.EarthAttack;
import com.td.game.combat.LightAttack;
import com.td.game.elements.Element;
import com.td.game.entities.Enemy;
import com.td.game.utils.ModelFactory;

public class Pillar implements Disposable {
    private static final int ICE_CHARM_ATTACKS_PER_FREEZE = 3;
    private static final float ICE_CHARM_FREEZE_DURATION = 2f;
    private static final float ICE_CHARM_THAW_DAMAGE_MULTIPLIER = 3f;

    private final PillarType type;
    private final Vector3 position;
    private Element currentElement;
    private Element firstOrb;
    private boolean active;

    private ModelInstance pillarModelInstance;
    private final ModelFactory modelFactory;

    private float bonusDamageMult = 1f;
    private float bonusRangeMult = 1f;
    private float bonusAttackSpeedMult = 1f;
    private boolean goldCharmActive = false;
    private boolean iceCharmActive = false;
    private boolean poisonCharmActive = false;
    private boolean lifeCharmActive = false;
    private boolean lifeFrenzyReady = false;

    private float currentCooldown = 0f;
    private final float baseAttackCooldown = PillarData.BASE_ATTACK_COOLDOWN;
    private final float baseDamage = PillarData.BASE_DAMAGE;
    private float goldGenerationTimer = 0f;
    private int pendingGoldGenerated = 0;
    private float earthTickTimer = 0f;
    private int iceCharmAttackCounter = 0;
    private final Set<Enemy> iceCharmThawTargets = new HashSet<Enemy>();

    private final com.td.game.combat.FireAttack fireAttack;
    private float fireBeamTimer = 0f;

    public Pillar(PillarType type, Vector3 position, ModelFactory modelFactory) {
        this.type = type;
        this.position = position.cpy();
        this.active = false;
        this.currentElement = null;
        this.firstOrb = null;
        this.modelFactory = modelFactory;
        this.fireAttack = new com.td.game.combat.FireAttack();
        updateModel();
    }

    private void updateModel() {
        Color orbColor = null;
        if (currentElement != null) {
            orbColor = new Color(currentElement.getR(), currentElement.getG(), currentElement.getB(), 1f);
        }
        Model model = modelFactory.createPillarModel(type, orbColor);
        this.pillarModelInstance = new ModelInstance(model);
        this.pillarModelInstance.transform.setToTranslation(position.x, position.y, position.z);
        this.pillarModelInstance.transform.scl(com.td.game.utils.Constants.TILE_SIZE / 2.0f);
    }

    public com.badlogic.gdx.math.collision.BoundingBox getBoundingBox() {
        float scale = com.td.game.utils.Constants.TILE_SIZE / 2.0f;
        Vector3 min = position.cpy().add(-0.6f * scale, 0, -0.6f * scale);
        Vector3 max = position.cpy().add(0.6f * scale, 2.0f * scale, 0.6f * scale);
        return new com.badlogic.gdx.math.collision.BoundingBox(min, max);
    }

    public boolean placeOrb(Element element) {
        if (element == null)
            return false;

        goldGenerationTimer = 0f;
        pendingGoldGenerated = 0;

        if (!active) {
            firstOrb = element.isPrime() ? element : null;
            currentElement = element;
            active = true;
            updateModel();
            return true;
        } else if (firstOrb != null && currentElement == firstOrb && element.isPrime()) {
            Element merged = Element.merge(firstOrb, element);
            if (merged != null) {
                currentElement = merged;
                firstOrb = null;
                updateModel();
                return true;
            }
        }
        currentElement = element;
        firstOrb = element.isPrime() ? element : null;
        updateModel();
        return true;
    }

    public void update(float delta, com.badlogic.gdx.utils.Array<com.td.game.entities.Enemy> enemies,
            com.badlogic.gdx.utils.Array<com.td.game.entities.Projectile> projectiles, boolean waveInProgress) {
        if (!active || currentElement == null)
            return;

        cleanupIceCharmTargets();

        if (currentElement == Element.GOLD) {
            if (!waveInProgress) {
                goldGenerationTimer = 0f;
                return;
            }

            goldGenerationTimer += delta;
            while (goldGenerationTimer >= PillarData.GOLD_GENERATION_INTERVAL) {
                goldGenerationTimer -= PillarData.GOLD_GENERATION_INTERVAL;
                pendingGoldGenerated += PillarData.GOLD_GENERATION_AMOUNT;
            }
            return;
        }

        goldGenerationTimer = 0f;
        pendingGoldGenerated = 0;

        if (currentElement == Element.LIFE) {
            return;
        }
        if (currentElement == Element.EARTH) {
            earthTickTimer += delta;
            while (earthTickTimer >= EarthAttack.TICK_INTERVAL) {
                earthTickTimer -= EarthAttack.TICK_INTERVAL;
                applyEarthQuakeInRange(enemies);
            }
            return;
        } else {
            earthTickTimer = 0f;
        }

        if (fireBeamTimer > 0) {
            fireBeamTimer -= delta;
        } else if (currentElement == Element.FIRE) {
            fireAttack.resetRamping();
        }

        currentCooldown -= delta;

        if (currentCooldown <= 0) {
            com.td.game.entities.Enemy target = findTarget(enemies);
            if (target != null) {
                attack(target, enemies, projectiles);
                currentCooldown = getActualAttackCooldown();
            }
        }
    }

    public int consumeGeneratedGold() {
        int generatedGold = pendingGoldGenerated;
        pendingGoldGenerated = 0;
        return generatedGold;
    }

    private com.td.game.entities.Enemy findTarget(com.badlogic.gdx.utils.Array<com.td.game.entities.Enemy> enemies) {
        if (currentElement == Element.FIRE && fireAttack != null) {
            com.td.game.entities.Enemy last = fireAttack.getLastTarget();
            if (last != null && last.isAlive() && !last.isAllied() && position.dst(last.getPosition()) <= getAttackRange()) {
                return last;
            }
        }

        com.td.game.entities.Enemy closest = null;
        float minDist = getAttackRange();

        for (com.td.game.entities.Enemy enemy : enemies) {
            if (!enemy.isAlive() || enemy.isAllied()) continue;
            float dist = position.dst(enemy.getPosition());
            if (dist < minDist) {
                minDist = dist;
                closest = enemy;
            }
        }
        return closest;
    }

    private void attack(com.td.game.entities.Enemy target, com.badlogic.gdx.utils.Array<com.td.game.entities.Enemy> enemies,
            com.badlogic.gdx.utils.Array<com.td.game.entities.Projectile> projectiles) {
        float damage = getActualDamage();
        boolean iceCharmActive = shouldTriggerIceCharmFreeze();

        
        if (lifeFrenzyReady) {
            damage *= 2.5f;
            lifeFrenzyReady = false;
        }
        
        
        if (currentElement == Element.FIRE) {
            com.td.game.combat.AttackContext ctx = new com.td.game.combat.AttackContext(this, target, enemies, 0f);
            fireAttack.attack(ctx);
            fireBeamTimer = 0.2f;
            return;
        }

        
        if (currentElement == Element.LIGHT) {
            damage = LightAttack.scaleByCurrentHp(damage, target);
        }

        damage = applyIceCharmThawBonus(target, damage);

        
        Model projectileModel = modelFactory.getProjectileModel(currentElement);
        ModelInstance mi = new ModelInstance(projectileModel);
        mi.transform.scl(0.5f);
        projectiles.add(new com.td.game.entities.Projectile(position.cpy().add(0, 2f, 0), target, currentElement, damage, 25f, mi,
            iceCharmActive, poisonCharmActive, lifeCharmActive, this));
    }

    private void applyEarthQuakeInRange(com.badlogic.gdx.utils.Array<com.td.game.entities.Enemy> enemies) {
        if (enemies == null) {
            return;
        }

        float attackRange = getAttackRange();
        boolean iceCharmFreezeAttack = shouldTriggerIceCharmFreeze();
        float baseDamage = getActualDamage() * EarthAttack.DAMAGE_MULTIPLIER;
        for (com.td.game.entities.Enemy enemy : enemies) {
            if (enemy == null || !enemy.isAlive() || enemy.isAllied()) {
                continue;
            }
            if (position.dst(enemy.getPosition()) <= attackRange) {
                float damage = applyIceCharmThawBonus(enemy, baseDamage);
                enemy.takeDamage(damage, currentElement, this);
                enemy.applySlow(EarthAttack.SLOW_DURATION, EarthAttack.SLOW_MULTIPLIER);
                if (iceCharmFreezeAttack) {
                    applyIceCharmFreeze(enemy);
                }
            }
        }
    }

    private boolean shouldTriggerIceCharmFreeze() {
        if (!iceCharmActive) {
            return false;
        }

        iceCharmAttackCounter++;
        if (iceCharmAttackCounter >= ICE_CHARM_ATTACKS_PER_FREEZE) {
            iceCharmAttackCounter = 0;
            return true;
        }
        return false;
    }

    private float applyIceCharmThawBonus(Enemy target, float damage) {
        if (!iceCharmActive || target == null || !iceCharmThawTargets.contains(target)) {
            return damage;
        }
        if (target.isFrozen()) {
            return damage;
        }

        iceCharmThawTargets.remove(target);
        return damage * ICE_CHARM_THAW_DAMAGE_MULTIPLIER;
    }

    private void cleanupIceCharmTargets() {
        if (iceCharmThawTargets.isEmpty()) {
            return;
        }

        Iterator<Enemy> iterator = iceCharmThawTargets.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (enemy == null || !enemy.isAlive()) {
                iterator.remove();
            }
        }
    }

    public void applyIceCharmFreeze(Enemy target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        target.applyFreeze(ICE_CHARM_FREEZE_DURATION);
        iceCharmThawTargets.add(target);
    }

    public float getActualDamage() {
        float tierMult = (currentElement != null && !currentElement.isPrime()) ? PillarData.TIER_HYBRID_DAMAGE_MULT : PillarData.TIER_PRIME_DAMAGE_MULT;
        return baseDamage * type.getDamageMult() * bonusDamageMult * tierMult;
    }

    private float getActualAttackCooldown() {
        float defaultCooldown = baseAttackCooldown / (type.getAttackSpeedMult() * bonusAttackSpeedMult);
        if (currentElement == Element.FIRE) {
            return defaultCooldown * 0.1f;
        }
        return defaultCooldown;
    }

    public void update(float delta) {
        
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (pillarModelInstance != null) {
            modelBatch.render(pillarModelInstance, environment);
        }
    }

    public boolean isActive() {
        return active;
    }

    public Element getCurrentElement() {
        return currentElement;
    }

    public PillarType getType() {
        return type;
    }

    public Vector3 getPosition() {
        return position;
    }

    public com.td.game.combat.FireAttack getFireAttack() {
        return fireAttack;
    }

    public float getFireBeamTimer() {
        return fireBeamTimer;
    }

    public float getAttackRange() {
        return PillarData.BASE_RANGE * type.getRangeMult() * bonusRangeMult;
    }

    public boolean canAcceptOrb() {
        return !active || (firstOrb != null && currentElement == firstOrb);
    }

    public Element removeOrb() {
        if (currentElement == null) {
            return null;
        }
        Element removed = currentElement;
        goldGenerationTimer = 0f;
        pendingGoldGenerated = 0;
        currentElement = null;
        firstOrb = null;
        active = false;
        updateModel();
        return removed;
    }

    public void setExternalMultipliers(float damageMult, float rangeMult, float attackSpeedMult) {
        float d = Math.max(0.1f, damageMult);
        float r = Math.max(0.1f, rangeMult);
        float s = Math.max(0.1f, attackSpeedMult);

        if (Math.abs(bonusDamageMult - d) < 0.0001f &&
                Math.abs(bonusRangeMult - r) < 0.0001f &&
                Math.abs(bonusAttackSpeedMult - s) < 0.0001f) {
            return;
        }

        bonusDamageMult = d;
        bonusRangeMult = r;
        bonusAttackSpeedMult = s;
    }

    public void setPoisonCharmActive(boolean poisonCharmActive) {
        this.poisonCharmActive = poisonCharmActive;
    }

    public void setGoldCharmActive(boolean goldCharmActive) {
        this.goldCharmActive = goldCharmActive;
    }

    public void setIceCharmActive(boolean iceCharmActive) {
        this.iceCharmActive = iceCharmActive;
        if (!iceCharmActive) {
            iceCharmAttackCounter = 0;
            iceCharmThawTargets.clear();
        }
    }

    public boolean isGoldCharmActive() {
        return goldCharmActive;
    }

    public int getGoldCharmBonus(int goldEarned) {
        if (!goldCharmActive || goldEarned <= 0) {
            return 0;
        }
        return Math.max(1, Math.round(goldEarned * 0.1f));
    }

    public void setLifeCharmActive(boolean lifeCharmActive) {
        this.lifeCharmActive = lifeCharmActive;
        if (!lifeCharmActive) {
            lifeFrenzyReady = false;
        }
    }

    public void activateLifeFrenzy() {
        if (lifeCharmActive) {
            lifeFrenzyReady = true;
        }
    }

    public void save(com.td.game.systems.SaveData.PillarSaveData pData) {
        pData.type = this.type.name();
        pData.x = this.position.x;
        pData.y = this.position.y;
        pData.z = this.position.z;
        pData.currentElement = this.currentElement != null ? this.currentElement.name() : null;
        pData.firstOrb = this.firstOrb != null ? this.firstOrb.name() : null;
        pData.active = this.active;
        pData.bonusDamageMult = this.bonusDamageMult;
        pData.bonusRangeMult = this.bonusRangeMult;
        pData.bonusAttackSpeedMult = this.bonusAttackSpeedMult;
    }

    public void load(com.td.game.systems.SaveData.PillarSaveData pData) {
        this.position.set(pData.x, pData.y, pData.z);
        this.currentElement = pData.currentElement != null ? Element.valueOf(pData.currentElement) : null;
        this.firstOrb = pData.firstOrb != null ? Element.valueOf(pData.firstOrb) : null;
        this.active = pData.active;
        this.bonusDamageMult = pData.bonusDamageMult;
        this.bonusRangeMult = pData.bonusRangeMult;
        this.bonusAttackSpeedMult = pData.bonusAttackSpeedMult;
        this.goldGenerationTimer = 0f;
        this.pendingGoldGenerated = 0;
        updateModel();
    }

    @Override
    public void dispose() {
    }
}

