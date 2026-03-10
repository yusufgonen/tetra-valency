package com.td.game.systems;

import com.badlogic.gdx.utils.Array;

public class SaveData {
    public String mapType;
    public float globalTimer;

    // WaveManager state
    public int currentWave;
    public int enemiesSpawned;

    // EconomyManager state
    public int gold;
    public int lives;

    // Player state
    public float playerX;
    public float playerY;
    public float playerZ;

    // Inventory state
    public Array<String> inventoryOrbs;
    public int unlockedInventorySlots;

    // Staff state
    public String staffOrb;

    // MergeBoard state
    public String mergeSlot1;
    public String mergeSlot2;
    public String mergeResult;

    // Global Multipliers & Augments
    public float globalDamageMult;
    public float globalRangeMult;
    public float globalAttackSpeedMult;
    public float staffAuraRadius;
    public Array<Integer> acquiredAugments;

    // Pillars state
    public Array<PillarSaveData> pillars;

    public static class PillarSaveData {
        public String type;
        public float x;
        public float y;
        public float z;
        public String currentElement;
        public String firstOrb;
        public boolean active;
        public float bonusDamageMult;
        public float bonusRangeMult;
        public float bonusAttackSpeedMult;

        public PillarSaveData() {
        }
    }

    public SaveData() {
        inventoryOrbs = new Array<>();
        acquiredAugments = new Array<>();
        pillars = new Array<>();
    }
}
