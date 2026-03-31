package com.td.game.combat;

import com.td.game.entities.Enemy;
import com.td.game.pillars.Pillar;
import com.td.game.pillars.PillarData;

public class EarthAttack implements AttackAction {
    public static final float TICK_INTERVAL = 1.0f;
    public static final float SLOW_DURATION = 1.25f;
    public static final float SLOW_MULTIPLIER = 0.55f;
    public static final float DAMAGE_MULTIPLIER = 0.45f;

    public static void applyQuakeInRange(AttackContext context, float baseDamage) {
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
                enemy.takeDamage(baseDamage * DAMAGE_MULTIPLIER, source.getCurrentElement());
                enemy.applySlow(SLOW_DURATION, SLOW_MULTIPLIER);
            }
        }
    }

    @Override
    public void attack(AttackContext context) {
        float baseDamage = 0f;
        if (context != null && context.getSource() != null) {
            baseDamage = PillarData.BASE_DAMAGE * context.getSource().getType().getDamageMult();
        }
        applyQuakeInRange(context, baseDamage);
    }
}
