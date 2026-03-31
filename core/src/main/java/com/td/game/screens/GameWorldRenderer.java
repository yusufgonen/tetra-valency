package com.td.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.td.game.entities.Enemy;
import com.td.game.entities.Projectile;
import com.td.game.map.GameMap;
import com.td.game.pillars.Pillar;
import com.td.game.player.Player;
import com.td.game.ui.ContextualMenuPanel;

final class GameWorldRenderer {
    private GameWorldRenderer() {
    }

    static float renderWorld(ModelBatch modelBatch,
            PerspectiveCamera camera,
            GameMap gameMap,
            Environment environment,
            ContextualMenuPanel buildMenu,
            Vector3 selectedTilePos,
            ModelInstance validHighlight,
            Array<Pillar> pillars,
            Array<Projectile> projectiles,
            Array<Enemy> enemies,
            Player player,
            ModelInstance gateInstance,
            ModelInstance coreSphereInstance,
            float coreFlashTimer) {
        modelBatch.begin(camera);
        gameMap.render(modelBatch, environment);

        if (buildMenu.isActive() && selectedTilePos != null) {
            ModelInstance highlight = validHighlight;
            highlight.transform.setToTranslation(selectedTilePos.x, 0.05f, selectedTilePos.z);
            modelBatch.render(highlight, environment);
        }

        for (Pillar pillar : pillars) {
            pillar.render(modelBatch, environment);
        }

        for (Projectile proj : projectiles) {
            proj.render(modelBatch, environment);
        }

        for (Enemy enemy : enemies) {
            enemy.render(modelBatch, environment);
        }

        player.render(modelBatch, environment);

        if (gateInstance != null) {
            modelBatch.render(gateInstance, environment);
        }
        if (coreSphereInstance != null) {
            if (coreFlashTimer > 0) {
                float t = Math.min(coreFlashTimer / 0.4f, 1f);
                Color flash = new Color(0.6f + 0.4f * t, 0.2f * (1f - t), 0.85f * (1f - t), 1f);
                coreSphereInstance.materials.get(0).set(ColorAttribute.createDiffuse(flash));
                coreFlashTimer -= Gdx.graphics.getDeltaTime();
                if (coreFlashTimer <= 0) {
                    coreSphereInstance.materials.get(0).set(
                            ColorAttribute.createDiffuse(new Color(0.6f, 0.2f, 0.85f, 1f)));
                }
            }
            modelBatch.render(coreSphereInstance, environment);
        }

        modelBatch.end();
        return coreFlashTimer;
    }
}
