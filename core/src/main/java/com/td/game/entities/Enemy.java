package com.td.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.td.game.elements.Element;
import com.td.game.utils.Constants;

public class Enemy implements Disposable {

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

    protected float rootTimer;
    protected float rootSlowMultiplier;
    protected float rootArmorBonus;

    protected float blindTimer;
    protected float blindRotationNoise;

    protected float confusionTimer;
    protected float confusionSpeedVariance;

    protected float greedMultiplier = 1f;

    protected float regenBlockTimer;

    protected float knockbackDistance;

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
        this.rootTimer = 0;
        this.rootSlowMultiplier = 1f;
        this.rootArmorBonus = 0;
        this.blindTimer = 0;
        this.blindRotationNoise = 0;
        this.confusionTimer = 0;
        this.confusionSpeedVariance = 0;
        this.greedMultiplier = 1f;
        this.regenBlockTimer = 0;
        this.knockbackDistance = 0;
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
            if (hitTimer > 0) {
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

    // not working with the enemy models
    public void renderHealthBar(ShapeRenderer shapeRenderer) {
        if (!alive)
            return;

        float barWidth = 1.2f;
        float barHeight = 0.15f;
        float barX = position.x - barWidth / 2f;

        float yOffset = isFlying ? 2.0f : 1.2f;
        float barY = position.y + yOffset;
        float barZ = position.z;

        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.9f);
        shapeRenderer.box(barX, barY, barZ, barWidth, barHeight, 0.05f);

        Color healthColor;
        if (element != null) {

            healthColor = new Color(element.getR(), element.getG(), element.getB(), 1f);
            if (armor > 0) {

                healthColor.mul(0.7f, 0.7f, 0.7f, 1f);
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
        shapeRenderer.box(barX, barY, barZ, healthWidth, barHeight, 0.05f);
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

        if (currentWaypointIndex < waypoints.size) {
            Vector3 target = waypoints.get(currentWaypointIndex);
            Vector3 direction = target.cpy().sub(position).nor();

            if (direction.len2() > 0.01f) {
                float targetRotation = (float) Math.toDegrees(Math.atan2(-direction.x, -direction.z));

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

            // Calculate speed with all multipliers
            float actualSpeed = baseSpeed * slowMultiplier * rootSlowMultiplier;

            // Apply confusion speed variance
            if (confusionTimer > 0) {
                float variance = (float) Math.sin(confusionTimer * 5f) * confusionSpeedVariance;
                actualSpeed *= (1f + variance);
            }

            position.add(direction.scl(actualSpeed * deltaTime));
            walkTimer += actualSpeed * deltaTime;

            if (position.dst(target) < 0.2f) {
                currentWaypointIndex++;

                if (currentWaypointIndex >= waypoints.size) {
                    reachedEnd = true;
                    alive = false;
                }
            }
        }

        if (animationController != null) {
            animationController.update(deltaTime);
        }

        updateModelPosition();
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

        if (drenchTimer > 0) {
            drenchTimer -= deltaTime;
        }

        if (poisonTimer > 0) {
            poisonTimer -= deltaTime;

            float totalPoisonDamage = poisonDamage * poisonStacks * deltaTime;
            takeDamage(totalPoisonDamage);
            if (poisonTimer <= 0) {
                poisonStacks = 0;
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
    }

    public void takeDamage(float damage) {
        takeDamage(damage, null);
    }

    public void takeDamage(float damage, Element attackerElement) {
        hitTimer = 0.1f;

        float armorReduction = armor / (armor + 100f);
        float actualDamage = damage * (1f - armorReduction);

        // Element damage multiplier
        if (attackerElement != null && this.element != null) {
            float multiplier = com.td.game.utils.CombatUtils.getDamageMultiplier(attackerElement, this.element);
            actualDamage *= multiplier;
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

        if (health <= 0) {
            health = 0;
            alive = false;
        }
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
        this.poisonTimer = Math.max(this.poisonTimer, duration);
        this.poisonDamage = damagePerSecond;
        this.poisonStacks = Math.min(this.poisonStacks + stacks, 10);
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

    public void applyKnockback(float distance) {
        this.knockbackDistance = distance;
        // Apply knockback immediately
        if (currentWaypointIndex > 0 && distance > 0) {
            // Move back along path
            int stepsBack = (int) (distance / 2f);
            currentWaypointIndex = Math.max(0, currentWaypointIndex - stepsBack);
            if (currentWaypointIndex < waypoints.size) {
                position.set(waypoints.get(currentWaypointIndex));
            }
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

    public int getPoisonStacks() {
        return poisonStacks;
    }

    public String getName() {
        return "Enemy";
    }

    public void setVisualScaleMultiplier(float visualScaleMultiplier) {
        this.visualScaleMultiplier = Math.max(0.1f, visualScaleMultiplier);
    }

    @Override
    public void dispose() {
    }
}
