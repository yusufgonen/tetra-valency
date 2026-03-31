package com.td.game.combat;

import com.td.game.entities.Enemy;
import com.td.game.pillars.Pillar;

public class EarthAttack implements AttackAction {
    public static final float STUN_DURATION = 0.5f;
    private static final float ATTACK_COOLDOWN_MULTIPLIER = 2.5f;

    public static float getAttackCooldown(float defaultCooldown) {
        return defaultCooldown * ATTACK_COOLDOWN_MULTIPLIER;
    }

    public static void applyStunInRange(AttackContext context) {
        if (context == null) {
            return;
        }

        Pillar source = context.getSource();
        if (source == null || context.getEnemiesInRange() == null) {
            return;
        }

        float attackRange = source.getAttackRange();
        for (Enemy enemy : context.getEnemiesInRange()) {
            if (enemy == null || !enemy.isAlive() || enemy.isAllied()) {
                continue;
            }
            if (source.getPosition().dst(enemy.getPosition()) <= attackRange) {
                enemy.applyFreeze(STUN_DURATION);
            }
        }
    }

    @Override
    public void attack(AttackContext context) {
        applyStunInRange(context);
    }
}
