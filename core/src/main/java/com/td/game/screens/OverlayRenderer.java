package com.td.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.td.game.elements.Element;
import com.td.game.entities.Effect;
import com.td.game.entities.Enemy;
import com.td.game.pillars.Pillar;

final class OverlayRenderer {
    private OverlayRenderer() {
    }

    static void render(ShapeRenderer uiShapeRenderer,
            SpriteBatch uiBatch,
            PerspectiveCamera camera,
            Array<Pillar> pillars,
            Pillar hoveredPillar,
            boolean hoveringAlchemist,
            com.td.game.player.Player player,
            float staffAuraRadius,
            float uiScale,
            Element equippedElement,
            Array<Effect> activeEffects,
            Array<Enemy> enemies,
            Texture poisonBurstTexture,
            float time,
            int mapAreaWidth,
            int screenHeight) {
        renderEarthquakeFields(uiShapeRenderer, camera, pillars, time, mapAreaWidth, screenHeight);

        if (hoveredPillar != null) {
            RangeOverlayRenderer.drawPillarRange(uiShapeRenderer, camera, hoveredPillar, mapAreaWidth, screenHeight);
        }

        if (hoveringAlchemist) {
            RangeOverlayRenderer.drawAuraRange(uiShapeRenderer, camera, player, pillars, staffAuraRadius, uiScale,
                    mapAreaWidth, screenHeight, equippedElement);
        }

        uiBatch.begin();
        for (Effect effect : activeEffects) {
            effect.render(uiBatch, camera, mapAreaWidth, screenHeight);
        }
        uiBatch.end();

        renderPoisonBurstEffects(uiBatch, camera, enemies, poisonBurstTexture, uiScale, mapAreaWidth, screenHeight);
    }

    private static void renderEarthquakeFields(ShapeRenderer uiShapeRenderer,
            PerspectiveCamera camera,
            Array<Pillar> pillars,
            float time,
            int mapAreaWidth,
            int screenHeight) {
        if (pillars == null || pillars.size == 0) {
            return;
        }
        float pulseA = 0.65f + 0.35f * MathUtils.sin(time * 4f);
        float pulseB = 0.7f + 0.3f * MathUtils.sin(time * 6.2f + 1.3f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        for (Pillar pillar : pillars) {
            if (pillar == null || pillar.getCurrentElement() != Element.EARTH) {
                continue;
            }
            float radius = pillar.getAttackRange();
            Vector3 center = pillar.getPosition();
            int segments = 100;
            float y = 0.2f;

            uiShapeRenderer.setColor(0.45f, 0.22f, 0.1f, 0.55f + 0.20f * pulseA);
            for (int i = 0; i < segments; i++) {
                float a0 = MathUtils.PI2 * i / segments;
                float a1 = MathUtils.PI2 * (i + 1) / segments;
                Vector3 p0 = new Vector3(center.x + MathUtils.cos(a0) * radius, y, center.z + MathUtils.sin(a0) * radius);
                Vector3 p1 = new Vector3(center.x + MathUtils.cos(a1) * radius, y, center.z + MathUtils.sin(a1) * radius);
                camera.project(p0, 0, 0, mapAreaWidth, screenHeight);
                camera.project(p1, 0, 0, mapAreaWidth, screenHeight);
                uiShapeRenderer.line(p0.x, p0.y, p1.x, p1.y);
            }

            float innerA = radius * (0.82f + 0.03f * MathUtils.sin(time * 7.2f));
            uiShapeRenderer.setColor(0.65f, 0.35f, 0.15f, 0.48f + 0.18f * pulseB);
            for (int i = 0; i < segments; i++) {
                float a0 = MathUtils.PI2 * i / segments;
                float a1 = MathUtils.PI2 * (i + 1) / segments;
                Vector3 p0 = new Vector3(center.x + MathUtils.cos(a0) * innerA, y, center.z + MathUtils.sin(a0) * innerA);
                Vector3 p1 = new Vector3(center.x + MathUtils.cos(a1) * innerA, y, center.z + MathUtils.sin(a1) * innerA);
                camera.project(p0, 0, 0, mapAreaWidth, screenHeight);
                camera.project(p1, 0, 0, mapAreaWidth, screenHeight);
                uiShapeRenderer.line(p0.x, p0.y, p1.x, p1.y);
            }

            float innerB = radius * (0.62f + 0.025f * MathUtils.sin(time * 9.1f + 1.2f));
            uiShapeRenderer.setColor(0.35f, 0.16f, 0.08f, 0.42f);
            for (int i = 0; i < segments; i++) {
                float a0 = MathUtils.PI2 * i / segments;
                float a1 = MathUtils.PI2 * (i + 1) / segments;
                Vector3 p0 = new Vector3(center.x + MathUtils.cos(a0) * innerB, y, center.z + MathUtils.sin(a0) * innerB);
                Vector3 p1 = new Vector3(center.x + MathUtils.cos(a1) * innerB, y, center.z + MathUtils.sin(a1) * innerB);
                camera.project(p0, 0, 0, mapAreaWidth, screenHeight);
                camera.project(p1, 0, 0, mapAreaWidth, screenHeight);
                uiShapeRenderer.line(p0.x, p0.y, p1.x, p1.y);
            }
        }
        uiShapeRenderer.end();
        Gdx.gl.glLineWidth(1f);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private static void renderPoisonBurstEffects(SpriteBatch uiBatch,
            PerspectiveCamera camera,
            Array<Enemy> enemies,
            Texture poisonBurstTexture,
            float uiScale,
            int mapAreaWidth,
            int screenHeight) {
        if (poisonBurstTexture == null) {
            return;
        }

        uiBatch.begin();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || !enemy.isPoisonBurstActive()) {
                continue;
            }

            Vector3 enemyPos = enemy.getPosition();
            float yOffset = enemy.isFlying() ? 3.4f : 2.8f;
            Vector3 screenPos = camera.project(new Vector3(enemyPos.x, enemyPos.y + yOffset, enemyPos.z), 0, 0,
                    mapAreaWidth, screenHeight);

            if (screenPos.z < 0f || screenPos.z > 1f || screenPos.x < 0f || screenPos.x > mapAreaWidth || screenPos.y < 0f
                    || screenPos.y > screenHeight) {
                continue;
            }

            float progress = enemy.getPoisonBurstProgress();
            float eased = MathUtils.sin(progress * MathUtils.PI * 0.5f);

            Vector3 baseScreen = camera.project(new Vector3(enemyPos.x, enemyPos.y, enemyPos.z), 0, 0, mapAreaWidth,
                    screenHeight);
            Vector3 topScreen = camera.project(
                    new Vector3(enemyPos.x, enemyPos.y + (enemy.isFlying() ? 2.2f : 1.6f), enemyPos.z),
                    0, 0, mapAreaWidth, screenHeight);
            float enemyPixelSize = Math.max(52f * uiScale, Math.abs(topScreen.y - baseScreen.y));
            float drawSize = enemyPixelSize * (0.9f + 1.4f * eased);

            float alpha = 1f - progress;
            uiBatch.setColor(1f, 1f, 1f, alpha);
            uiBatch.draw(poisonBurstTexture, screenPos.x - drawSize * 0.5f, screenPos.y - drawSize * 0.5f, drawSize,
                    drawSize);
        }
        uiBatch.setColor(Color.WHITE);
        uiBatch.end();
    }
}
