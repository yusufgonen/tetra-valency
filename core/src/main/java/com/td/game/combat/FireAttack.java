package com.td.game.combat;

import com.td.game.elements.Element;
import com.td.game.entities.Enemy;
import com.td.game.pillars.Pillar;

public class FireAttack implements AttackAction {
    private final float rampingUp;
    private final float maxRamping;
    private Enemy lastTarget;
    private float currentRamping = 1f;
    private long lastSeenArmorBreakSerial = -1L;

    public FireAttack() {
        this(0.035f, 4.5f);
    }

    public FireAttack(float rampingUp, float maxRamping) {
        this.rampingUp = rampingUp;
        this.maxRamping = maxRamping;
    }

    @Override
    public void attack(AttackContext context) {
        if (context == null || context.getTarget() == null || context.getSource() == null) {
            return;
        }

        Enemy target = context.getTarget();
        Pillar pillar = context.getSource();

        long currentArmorBreakSerial = target.getArmorBreakSerial();
        boolean armorJustBrokenForThisTarget = target == lastTarget
                && currentArmorBreakSerial != lastSeenArmorBreakSerial;

        if (target == lastTarget && target.isAlive() && !armorJustBrokenForThisTarget) {
            currentRamping = Math.min(maxRamping, currentRamping + rampingUp);
        } else {
            currentRamping = 1f;
        }
        lastTarget = target;
        lastSeenArmorBreakSerial = currentArmorBreakSerial;

        Element attackerElement = pillar.getCurrentElement();
        float damage = pillar.getActualDamage() * 0.1f * currentRamping;
        
        target.takeDamage(damage, attackerElement, pillar);
    }

    public Enemy getLastTarget() {
        return lastTarget;
    }

    public float getCurrentRamping() {
        return currentRamping;
    }

    public void resetRamping() {
        currentRamping = 1f;
        lastTarget = null;
        lastSeenArmorBreakSerial = -1L;
    }
}
