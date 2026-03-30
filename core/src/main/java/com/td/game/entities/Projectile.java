package com.td.game.entities;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.td.game.elements.Element;
import com.td.game.pillars.Pillar;

public class Projectile {
    private Vector3 position;
    private final Vector3 targetPos;
    private final Enemy targetEnemy;
    private final float speed;
    private final Element element;
    private final float damage;
    private final boolean poisonCharmActive;
    private final boolean lifeCharmActive;
    private final Pillar sourcePillar;
    private boolean alive;
    private boolean impacted;

    private ModelInstance modelInstance;

    public Projectile(Vector3 startPos, Enemy targetEnemy, Element element, float damage, float speed, ModelInstance modelInstance,
            boolean poisonCharmActive, boolean lifeCharmActive, Pillar sourcePillar) {
        this.position = startPos.cpy();
        this.targetEnemy = targetEnemy;
        this.targetPos = targetEnemy.getPosition().cpy();
        this.element = element;
        this.damage = damage;
        this.speed = speed;
        this.modelInstance = modelInstance;
        this.poisonCharmActive = poisonCharmActive;
        this.lifeCharmActive = lifeCharmActive;
        this.sourcePillar = sourcePillar;
        this.alive = true;
        this.impacted = false;
    }

    public void update(float delta) {
        if (!alive) return;

        // If target exists, update targetPos
        if (targetEnemy != null && targetEnemy.isAlive()) {
            targetPos.set(targetEnemy.getPosition());
        }

        Vector3 direction = targetPos.cpy().sub(position).nor();
        float distance = position.dst(targetPos);
        float moveAmount = speed * delta;

        if (distance <= moveAmount) {
            position.set(targetPos);
            impacted = true;
            alive = false;
        } else {
            position.add(direction.scl(moveAmount));
        }

        if (modelInstance != null) {
            modelInstance.transform.setToTranslation(position);
            // Optional: Rotate towards target
        }
    }

    public void render(ModelBatch batch, Environment env) {
        if (alive && modelInstance != null) {
            batch.render(modelInstance, env);
        }
    }

    public boolean isImpacted() {
        return impacted;
    }

    public boolean isAlive() {
        return alive;
    }

    public Enemy getTargetEnemy() {
        return targetEnemy;
    }

    public Element getElement() {
        return element;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isPoisonCharmActive() {
        return poisonCharmActive;
    }

    public boolean isLifeCharmActive() {
        return lifeCharmActive;
    }

    public Pillar getSourcePillar() {
        return sourcePillar;
    }

    public Vector3 getPosition() {
        return position;
    }
}
