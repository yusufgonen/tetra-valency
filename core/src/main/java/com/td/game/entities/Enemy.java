package com.td.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import com.td.game.elements.Element;
import com.td.game.utils.Constants;

public class Enemy implements Disposable {

    private static final float POISON_BURST_DURATION = 0.42f;
    private static final float WAYPOINT_REACH_EPSILON = 0.2f;
    private static Sound enemyHitSound;
    private static boolean enemyHitLoadAttempted;
    private static long enemyHitLastPlayedMs;
    private static long enemyHitBurstWindowStartMs;
    private static int enemyHitBurstCount;

    protected Vector3 position;
    protected float health;
    protected float maxHealth;
    protected float speed;
    protected float baseSpeed;
    protected int reward;
    protected boolean alive;
    protected boolean reachedEnd;
    protected boolean isFlying;
    protected float armor;
    protected float shield;
    protected boolean isAllied = false;
    protected boolean isMovingBackwards = false;

    protected Array<Vector3> waypoints;
    protected int currentWaypointIndex;
    protected float rotation;
    protected float walkTimer;

    protected Model model;
    protected ModelInstance modelInstance;
    protected AnimationController animationController;
    protected Element element;

    protected float modelScaleMultiplier = 1f;
    protected float visualScaleMultiplier = 1f;

    protected float slowTimer;
    protected float slowMultiplier;
    protected float freezeTimer;
    protected float armorMeltTimer;
    protected float armorMeltMultiplier;
    protected float burnTimer;
    protected float burnDamage;
    protected float hitTimer;

    protected float drenchTimer;
    protected float drenchMultiplier = 1.5f;

    protected float poisonTimer;
    protected float poisonDamage;
    protected int poisonStacks;
    protected float poisonFlashTimer;
    protected float poisonDamageInterval;
    protected float poisonDamageCounter;

    protected float rootTimer;
    protected float rootSlowMultiplier;
    protected float rootArmorBonus;

    protected float blindTimer;
    protected float blindRotationNoise;

    protected float confusionTimer;
    protected float confusionSpeedVariance;

    protected float greedMultiplier = 1f;

    protected float regenBlockTimer;
    protected float regenReductionTimer;
    protected float regenReductionPerStack;
    protected int regenReductionStacks;

    protected float knockbackDistance;
    protected Vector3 knockbackVelocity;
    protected float knockbackDuration;
    protected float knockbackTimer;

    public Enemy(float maxHealth, float speed, int reward) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        float scaleMultiplier = Constants.TILE_SIZE / 2.0f;
        this.baseSpeed = speed * scaleMultiplier;
        this.speed = speed * scaleMultiplier;
        this.reward = reward;
        this.alive = true;
        this.reachedEnd = false;
        this.isFlying = false;
        this.armor = 0f;
        this.shield = 0f;
        this.currentWaypointIndex = 0;
        this.position = new Vector3();

        this.slowTimer = 0;
        this.slowMultiplier = 1f;
        this.freezeTimer = 0;
        this.armorMeltTimer = 0;
        this.armorMeltMultiplier = 1f;
        this.burnTimer = 0;
        this.burnDamage = 0;
        this.hitTimer = 0;

        this.drenchTimer = 0;
        this.poisonTimer = 0;
        this.poisonDamage = 0;
        this.poisonStacks = 0;
        this.poisonFlashTimer = 0;
        this.poisonDamageInterval = 0;
        this.poisonDamageCounter = 0;
        this.rootTimer = 0;
        this.rootSlowMultiplier = 1f;
        this.rootArmorBonus = 0;
        this.blindTimer = 0;
        this.blindRotationNoise = 0;
        this.confusionTimer = 0;
        this.confusionSpeedVariance = 0;
        this.greedMultiplier = 1f;
        this.regenBlockTimer = 0;
        this.regenReductionTimer = 0;
        this.regenReductionPerStack = 0f;
        this.regenReductionStacks = 0;
        this.knockbackDistance = 0;
        this.knockbackVelocity = new Vector3();
        this.knockbackDuration = 0;
        this.knockbackTimer = 0;
    }

    public void setModel(Model model) {
        this.model = model;
        this.modelInstance = new ModelInstance(model);
        for (int i = 0; i < modelInstance.materials.size; i++) {
            modelInstance.materials.set(i, modelInstance.materials.get(i).copy());
        }

        if (modelInstance.animations.size > 0) {
            animationController = new AnimationController(modelInstance);
            String animId = modelInstance.animations.get(0).id;
            for (com.badlogic.gdx.graphics.g3d.model.Animation anim : modelInstance.animations) {
                if (isFlying && anim.id.toLowerCase(java.util.Locale.ROOT).contains("fly")) {
                    animId = anim.id;
                    break;
                } else if (!isFlying && (anim.id.toLowerCase(java.util.Locale.ROOT).contains("walk") ||
                        anim.id.toLowerCase(java.util.Locale.ROOT).contains("move"))) {
                    animId = anim.id;
                    break;
                }
            }
            animationController.setAnimation(animId, -1);
        }

        com.badlogic.gdx.math.collision.BoundingBox bb = new com.badlogic.gdx.math.collision.BoundingBox();
        modelInstance.calculateBoundingBox(bb);
        Vector3 dim = new Vector3();
        bb.getDimensions(dim);
        float maxDim = Math.max(dim.x, Math.max(dim.y, dim.z));
        if (maxDim > 0.001f) {
            float baseToCurrentRatio = Constants.TILE_SIZE / 2.0f;
            float targetSize = 2.0f * 0.5f * baseToCurrentRatio;
            this.modelScaleMultiplier = targetSize / maxDim;
        } else {
            this.modelScaleMultiplier = Constants.TILE_SIZE / 2.0f;
        }
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (alive && modelInstance != null) {
            if (poisonFlashTimer > 0) {
                for (com.badlogic.gdx.graphics.g3d.Material mat : modelInstance.materials) {
                    mat.set(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.createEmissive(0.75f, 0.15f, 0.85f, 1f));
                }
            } else if (hitTimer > 0) {
                for (com.badlogic.gdx.graphics.g3d.Material mat : modelInstance.materials) {
                    mat.set(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.createEmissive(Color.WHITE));
                }
            } else {
                for (com.badlogic.gdx.graphics.g3d.Material mat : modelInstance.materials) {
                    mat.remove(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Emissive);
                }
            }
            modelBatch.render(modelInstance, environment);
        }
    }

    public void renderHealthBar(ShapeRenderer shapeRenderer, com.badlogic.gdx.graphics.Camera camera) {
        if (!alive)
            return;

        Vector3 screenPos = camera.project(new Vector3(position.x, position.y + (isFlying ? 3.0f : 2.5f), position.z));

        // Don't draw if behind camera
        if (screenPos.z < 0 || screenPos.z > 1)
            return;

        float barWidth = 40f;
        float barHeight = 6f;
        float barX = screenPos.x - barWidth / 2f;
        float barY = screenPos.y;

        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        Color healthColor;
        if (element != null) {
            healthColor = new Color(element.getR(), element.getG(), element.getB(), 1f);
            if (armor > 0) {
                healthColor.mul(0.6f, 0.6f, 0.6f, 1f); // Darker
            }
        } else {
            float healthPercent = health / maxHealth;
            if (healthPercent > 0.5f) {
                healthColor = Color.GREEN;
            } else if (healthPercent > 0.25f) {
                healthColor = Color.YELLOW;
            } else {
                healthColor = Color.RED;
            }
        }

        shapeRenderer.setColor(healthColor);
        float healthWidth = barWidth * (health / maxHealth);
        shapeRenderer.rect(barX, barY, healthWidth, barHeight);
    }

    public void setElement(Element element) {
        this.element = element;
        if (this.element != null && this.modelInstance != null) {
            Color eleColor = new Color(element.getR(), element.getG(), element.getB(), 1f);

            for (com.badlogic.gdx.graphics.g3d.Material mat : this.modelInstance.materials) {
                com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute ca = (com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute) mat
                        .get(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Diffuse);

                if (ca != null) {
                    if (mat.id.toLowerCase(java.util.Locale.ROOT).contains("green")
                            || (ca.color.g > ca.color.r * 1.5f && ca.color.g > ca.color.b * 1.5f)) {
                        ca.color.set(eleColor);
                    } else if (!isFlying) {
                        float brightness = (ca.color.r + ca.color.g + ca.color.b) / 3f;
                        if (brightness > 0.1f) {
                            ca.color.set(eleColor);
                        }
                    }
                }
            }
        }
    }

    public Element getElement() {
        return element;
    }

    public void setWaypoints(Array<Vector3> waypoints) {
        this.waypoints = waypoints;
        if (waypoints.size > 0) {
            this.position.set(waypoints.first());
            this.currentWaypointIndex = 0;
        }
    }

    public void update(float deltaTime) {
        if (!alive || waypoints == null || waypoints.size == 0)
            return;

        updateEffects(deltaTime);

        if (freezeTimer > 0) {
            updateModelPosition();
            return;
        }

        // Handle knockback movement
        if (knockbackTimer > 0) {
            knockbackTimer -= deltaTime;
            position.add(knockbackVelocity.cpy().scl(deltaTime));
            
            // If knockback ends, reset velocity
            if (knockbackTimer <= 0) {
                knockbackVelocity.setZero();
            }
            
            updateModelPosition();
            return; // Skip normal movement during knockback
        }

        boolean endReached = hasCompletedPath();

        if (!endReached) {
            // Calculate speed with all multipliers
            float actualSpeed = baseSpeed * slowMultiplier * rootSlowMultiplier;

            // Apply confusion speed variance
            if (confusionTimer > 0) {
                float variance = (float) Math.sin(confusionTimer * 5f) * confusionSpeedVariance;
                actualSpeed *= (1f + variance);
            }

            float remainingMove = actualSpeed * deltaTime;
            Vector3 movementDirection = new Vector3();
            int waypointSafetyCounter = 0;

            while (remainingMove > 0f && !hasCompletedPath() && waypointSafetyCounter++ <= waypoints.size) {
                Vector3 target = waypoints.get(Math.max(0, Math.min(waypoints.size - 1, currentWaypointIndex)));
                movementDirection.set(target).sub(position);
                float distanceToTarget = movementDirection.len();

                if (distanceToTarget <= WAYPOINT_REACH_EPSILON) {
                    position.set(target);
                    advanceWaypoint();
                    continue;
                }

                movementDirection.scl(1f / distanceToTarget);
                float step = Math.min(remainingMove, distanceToTarget);
                position.mulAdd(movementDirection, step);
                walkTimer += step;
                remainingMove -= step;

                if (step >= distanceToTarget - 0.0001f) {
                    position.set(target);
                    advanceWaypoint();
                }
            }

            if (movementDirection.len2() > 0.01f) {
                float targetRotation = (float) Math.toDegrees(Math.atan2(-movementDirection.x, -movementDirection.z));

                // Apply blind rotation noise
                if (blindTimer > 0) {
                    targetRotation += blindRotationNoise * (float) Math.sin(blindTimer * 10f);
                }

                float diff = targetRotation - this.rotation;
                while (diff > 180)
                    diff -= 360;
                while (diff < -180)
                    diff += 360;
                this.rotation += diff * 12f * deltaTime;
            }
        }

        if (animationController != null) {
            animationController.update(deltaTime);
        }

        updateModelPosition();
    }

    private boolean hasCompletedPath() {
        return isMovingBackwards ? currentWaypointIndex < 0 : currentWaypointIndex >= waypoints.size;
    }

    private void advanceWaypoint() {
        if (isMovingBackwards) {
            currentWaypointIndex--;
        } else {
            currentWaypointIndex++;
        }

        if (hasCompletedPath()) {
            reachedEnd = true;
            alive = false;
        }
    }

    protected void updateModelPosition() {
        if (modelInstance != null) {
            float scale = Constants.TILE_SIZE / 2.0f;
            float yOffset = isFlying ? 2f * scale : 0.25f * scale;

            modelInstance.transform.setToTranslation(position.x, position.y + yOffset, position.z);

            if (animationController == null && alive && freezeTimer <= 0) {
                if (isFlying) {
                    float bob = (float) Math.sin(walkTimer * 5f) * 0.3f * scale;
                    modelInstance.transform.translate(0, bob, 0);
                } else {
                    float bob = (float) Math.abs(Math.sin(walkTimer * 10f)) * 0.1f * scale;
                    modelInstance.transform.translate(0, bob, 0);
                }
            }

            modelInstance.transform.scl(modelScaleMultiplier * visualScaleMultiplier);
            modelInstance.transform.rotate(Vector3.Y, rotation + 180f);

            // Apply poison flash effect
            if (poisonFlashTimer > 0) {
                for (int i = 0; i < modelInstance.materials.size; i++) {
                    com.badlogic.gdx.graphics.g3d.Material mat = modelInstance.materials.get(i);
                    com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute ca = (com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute) 
                        mat.get(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Diffuse);
                    if (ca != null) {
                        ca.color.set(0.8f, 0.2f, 0.8f, 1f); // Poison purple flash
                    }
                }
            } else if (element != null) {
                // Reset to original color
                for (int i = 0; i < modelInstance.materials.size; i++) {
                    com.badlogic.gdx.graphics.g3d.Material mat = modelInstance.materials.get(i);
                    com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute ca = (com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute) 
                        mat.get(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Diffuse);
                    if (ca != null) {
                        ca.color.set(element.getR(), element.getG(), element.getB(), 1f);
                    }
                }
            }

            modelInstance.calculateTransforms();
        }
    }

    private void updateEffects(float deltaTime) {
        if (hitTimer > 0) {
            hitTimer -= deltaTime;
        }

        if (slowTimer > 0) {
            slowTimer -= deltaTime;
            if (slowTimer <= 0)
                slowMultiplier = 1f;
        }
        if (freezeTimer > 0)
            freezeTimer -= deltaTime;
        if (armorMeltTimer > 0) {
            armorMeltTimer -= deltaTime;
            if (armorMeltTimer <= 0)
                armorMeltMultiplier = 1f;
        }
        if (burnTimer > 0) {
            burnTimer -= deltaTime;
            takeDamage(burnDamage * deltaTime);
        }

        if (poisonFlashTimer > 0) {
            // Visual timers should run in real frame time, independent from sim speed (1x/2x/8x/32x)
            poisonFlashTimer -= Gdx.graphics.getDeltaTime();
        }

        if (drenchTimer > 0) {
            drenchTimer -= deltaTime;
        }

        if (poisonTimer > 0) {
            poisonTimer -= deltaTime;
            poisonDamageCounter += deltaTime;

            // Poison damage every 1.7 seconds
            if (poisonDamageCounter >= poisonDamageInterval) {
                float totalPoisonDamage = poisonDamage * poisonStacks;
                takeDamage(totalPoisonDamage);
                poisonDamageCounter = 0;
                poisonFlashTimer = POISON_BURST_DURATION;
            }

            if (poisonTimer <= 0) {
                poisonStacks = 0;
                poisonFlashTimer = 0;
                poisonDamageCounter = 0;
            }
        }

        if (rootTimer > 0) {
            rootTimer -= deltaTime;
            if (rootTimer <= 0) {
                rootSlowMultiplier = 1f;
                armor -= rootArmorBonus;
                rootArmorBonus = 0;
            }
        }

        if (blindTimer > 0) {
            blindTimer -= deltaTime;
            if (blindTimer <= 0) {
                blindRotationNoise = 0;
            }
        }

        if (confusionTimer > 0) {
            confusionTimer -= deltaTime;
            if (confusionTimer <= 0) {
                confusionSpeedVariance = 0;
            }
        }

        if (regenBlockTimer > 0) {
            regenBlockTimer -= deltaTime;
        }

        if (regenReductionTimer > 0) {
            regenReductionTimer -= deltaTime;
            if (regenReductionTimer <= 0f) {
                regenReductionTimer = 0f;
                regenReductionStacks = 0;
            }
        }
    }

    public void takeDamage(float damage) {
        takeDamage(damage, null);
    }

    public void takeDamage(float damage, Element attackerElement) {
        hitTimer = 0.1f;

        float armorReduction = armor / (armor + 100f);
        float actualDamage = damage * (1f - armorReduction);

        // Element damage multiplier - circular system with weak/strong relationships
        if (attackerElement != null && this.element != null) {
            float multiplier = com.td.game.utils.CombatUtils.getDamageMultiplier(attackerElement, this.element);
            actualDamage *= multiplier;
        }

        // Gold element special: takes 25% less damage from all elements
        if (this.element == Element.GOLD) {
            actualDamage *= 0.75f;
        }

        // Drench effect: increases lightning/light damage
        if (drenchTimer > 0 && attackerElement == Element.LIGHT) {
            actualDamage *= drenchMultiplier;
        }

        // Armor melt effect
        if (armorMeltTimer > 0) {
            actualDamage *= armorMeltMultiplier;
        }

        // Shield absorption
        if (shield > 0) {
            if (shield >= actualDamage) {
                shield -= actualDamage;
                return;
            } else {
                actualDamage -= shield;
                shield = 0;
            }
        }

        health -= actualDamage;
        if (actualDamage > 0f) {
            playEnemyHitSfxThrottled();
        }

        if (health <= 0) {
            health = 0;
            alive = false;
        }
    }

    private static void playEnemyHitSfxThrottled() {
        if (!ensureEnemyHitSoundLoaded()) {
            return;
        }

        long nowMs = TimeUtils.millis();

        // Hard gate to prevent sample stacking in the same frame burst.
        if (nowMs - enemyHitLastPlayedMs < 45L) {
            return;
        }

        // Sliding burst window so dense waves do not spam the same SFX constantly.
        if (nowMs - enemyHitBurstWindowStartMs > 320L) {
            enemyHitBurstWindowStartMs = nowMs;
            enemyHitBurstCount = 0;
        }
        enemyHitBurstCount++;

        float playChance = 1f;
        if (enemyHitBurstCount > 4) {
            playChance = 0.62f;
        }
        if (enemyHitBurstCount > 8) {
            playChance = 0.33f;
        }

        if (!MathUtils.randomBoolean(playChance)) {
            return;
        }

        enemyHitLastPlayedMs = nowMs;
        enemyHitSound.play(0.8f);
    }

    private static boolean ensureEnemyHitSoundLoaded() {
        if (enemyHitSound != null) {
            return true;
        }
        if (enemyHitLoadAttempted) {
            return false;
        }

        enemyHitLoadAttempted = true;
        FileHandle f = resolveAsset("audio/sfx/enemy_hit.ogg");
        if (!f.exists()) {
            return false;
        }

        try {
            enemyHitSound = Gdx.audio.newSound(f);
            return true;
        } catch (Exception e) {
            Gdx.app.log("Enemy", "enemy_hit load failed: " + e.getMessage());
            return false;
        }
    }

    private static FileHandle resolveAsset(String name) {
        FileHandle f = Gdx.files.internal(name);
        if (f.exists()) {
            return f;
        }
        f = Gdx.files.internal("assets/" + name);
        if (f.exists()) {
            return f;
        }
        return Gdx.files.internal(name);
    }

    public void applySlow(float duration, float multiplier) {
        this.slowTimer = duration;
        this.slowMultiplier = multiplier;
    }

    public void applyFreeze(float duration) {
        this.freezeTimer = duration;
    }

    public void applyArmorMelt(float duration, float extraDamagePercent) {
        this.armorMeltTimer = duration;
        this.armorMeltMultiplier = 1f + extraDamagePercent;
    }

    public void applyBurn(float duration, float damagePerSecond) {
        this.burnTimer = duration;
        this.burnDamage = damagePerSecond;
    }

    public void applyDrench(float duration) {
        this.drenchTimer = duration;
    }

    public void applyPoison(float duration, float damagePerSecond, int stacks) {
        boolean wasPoisoned = this.poisonTimer > 0f;
        this.poisonTimer = Math.max(this.poisonTimer, duration);
        this.poisonDamage = damagePerSecond;
        this.poisonStacks = Math.min(this.poisonStacks + stacks, 10);
        this.poisonDamageInterval = 1.7f; // Damage every 1.7 seconds
        if (!wasPoisoned) {
            this.poisonDamageCounter = 0; // Start first tick cadence only on first application
        }
    }

    public void applyRoot(float duration, float slowAmount, float armorBonus) {
        if (rootTimer <= 0) {

            this.armor += armorBonus;
            this.rootArmorBonus = armorBonus;
        }
        this.rootTimer = duration;
        this.rootSlowMultiplier = slowAmount;
    }

    public void applyBlind(float duration, float rotationNoise) {
        this.blindTimer = duration;
        this.blindRotationNoise = rotationNoise;
    }

    public void applyConfusion(float duration, float speedVariance) {
        this.confusionTimer = duration;
        this.confusionSpeedVariance = speedVariance;
    }

    public void applyGreed(float multiplier) {
        this.greedMultiplier *= multiplier;
    }

    public void applyRegenBlock(float duration) {
        this.regenBlockTimer = duration;
    }

    public void applyRegenReduction(float duration, float reductionPerStack, int stacks) {
        this.regenReductionTimer = Math.max(this.regenReductionTimer, duration);
        this.regenReductionPerStack = Math.max(this.regenReductionPerStack, reductionPerStack);
        this.regenReductionStacks = Math.min(this.regenReductionStacks + Math.max(1, stacks), 10);
    }

    public void applyKnockback(float distance) {
        this.knockbackDistance = distance;
        
        if (waypoints != null && waypoints.size > 0 && distance > 0) {
            // Calculate backward direction (towards previous waypoint)
            Vector3 backwardDirection;
            if (currentWaypointIndex > 0) {
                backwardDirection = waypoints.get(currentWaypointIndex - 1).cpy().sub(position).nor();
            } else {
                // Default backward direction if no previous waypoint
                backwardDirection = new Vector3(-1, 0, 0);
            }
            
            // Set smooth knockback parameters
            float knockbackForce = distance * 8f; // Speed multiplier for smooth animation
            knockbackVelocity.set(backwardDirection).scl(knockbackForce);
            knockbackDuration = distance / knockbackForce; // Time to reach destination
            knockbackTimer = knockbackDuration;
        }
    }

    public void stripShield(float amount) {
        shield -= amount;
        if (shield < 0)
            shield = 0;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    public void setArmor(float armor) {
        this.armor = armor;
    }

    public float getArmor() {
        return armor;
    }

    public void setShield(float shield) {
        this.shield = shield;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean hasReachedEnd() {
        return reachedEnd;
    }

    public Vector3 getPosition() {
        return position;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public int getReward() {
        return Math.round(reward * greedMultiplier);
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getHealthPercent() {
        return health / maxHealth;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public boolean isFrozen() {
        return freezeTimer > 0;
    }

    public boolean isDrenched() {
        return drenchTimer > 0;
    }

    public boolean isPoisoned() {
        return poisonTimer > 0;
    }

    public boolean isRooted() {
        return rootTimer > 0;
    }

    public boolean isBlinded() {
        return blindTimer > 0;
    }

    public boolean isConfused() {
        return confusionTimer > 0;
    }

    public boolean hasRegenBlock() {
        return regenBlockTimer > 0;
    }

    public float getRegenMultiplier() {
        if (regenReductionTimer <= 0f || regenReductionStacks <= 0) {
            return 1f;
        }
        float reduced = 1f - (regenReductionPerStack * regenReductionStacks);
        return Math.max(0f, reduced);
    }

    public int getPoisonStacks() {
        return poisonStacks;
    }

    public boolean isPoisonBurstActive() {
        return poisonFlashTimer > 0;
    }

    public float getPoisonBurstProgress() {
        if (poisonFlashTimer <= 0f) {
            return 0f;
        }
        float progress = 1f - (poisonFlashTimer / POISON_BURST_DURATION);
        return Math.max(0f, Math.min(1f, progress));
    }

    public String getName() {
        return "Enemy";
    }

    public void setVisualScaleMultiplier(float visualScaleMultiplier) {
        this.visualScaleMultiplier = Math.max(0.1f, visualScaleMultiplier);
    }

    public boolean isAllied() {
        return isAllied;
    }

    public void setAllied(boolean allied) {
        this.isAllied = allied;
    }

    public void setMovingBackwards(boolean movingBackwards) {
        this.isMovingBackwards = movingBackwards;
    }

    public void setCurrentWaypointIndex(int index) {
        this.currentWaypointIndex = index;
    }

    @Override
    public void dispose() {
    }
}
