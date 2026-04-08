package com.td.game.entities;

final class ArmorLayer {
    private final float maxArmor;
    private float armor;

    ArmorLayer(float maxArmor) {
        this.maxArmor = Math.max(0f, maxArmor);
        this.armor = this.maxArmor;
    }

    float absorb(float incomingDamage) {
        if (incomingDamage <= 0f || armor <= 0f) {
            return incomingDamage;
        }

        if (incomingDamage >= armor) {
            float remaining = incomingDamage - armor;
            armor = 0f;
            return remaining;
        }

        armor -= incomingDamage;
        return 0f;
    }

    boolean isActive() {
        return armor > 0f;
    }

    float getArmor() {
        return armor;
    }

    float getMaxArmor() {
        return maxArmor;
    }
}