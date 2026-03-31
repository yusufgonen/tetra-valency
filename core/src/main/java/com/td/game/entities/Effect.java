package com.td.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public class Effect {
    private Vector3 position;
    private final Texture texture;
    private final float maxLifetime;
    private float currentLifetime;
    private float scale;
    private float riseSpeed;
    private boolean alive;

    public Effect(Vector3 position, Texture texture, float lifetime, float scale) {
        this(position, texture, lifetime, scale, 0f);
    }

    public Effect(Vector3 position, Texture texture, float lifetime, float scale, float riseSpeed) {
        this.position = position.cpy();
        this.texture = texture;
        this.maxLifetime = lifetime;
        this.currentLifetime = lifetime;
        this.scale = scale;
        this.riseSpeed = riseSpeed;
        this.alive = true;
    }

    public void update(float delta) {
        currentLifetime -= delta;
        position.y += riseSpeed * delta;
        if (currentLifetime <= 0) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch, com.badlogic.gdx.graphics.Camera camera, int viewportWidth, int viewportHeight) {
        if (!alive || texture == null) return;

        Vector3 screenPos = camera.project(position.cpy(), 0, 0, viewportWidth, viewportHeight);
        if (screenPos.z < 0 || screenPos.z > 1) return;

        float alpha = currentLifetime / maxLifetime;
        batch.setColor(1, 1, 1, alpha);
        float w = texture.getWidth() * scale;
        float h = texture.getHeight() * scale;
        batch.draw(texture, screenPos.x - w / 2, screenPos.y - h / 2, w, h);
        batch.setColor(Color.WHITE);
    }

    public boolean isAlive() {
        return alive;
    }
}
