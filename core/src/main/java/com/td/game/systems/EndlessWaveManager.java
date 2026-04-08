package com.td.game.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.td.game.utils.ModelFactory;

public class EndlessWaveManager extends WaveManager {

    private static final int ENDLESS_START_WAVE = 50;
    private static final int ENDLESS_WAVE_CAP = 500;

    public EndlessWaveManager(Array<Vector3> pathWaypoints, ModelFactory modelFactory) {
        super(pathWaypoints, modelFactory);
        setCurrentWave(ENDLESS_START_WAVE);
    }

    @Override
    public int getMaxWaves() {
        return ENDLESS_WAVE_CAP;
    }

    @Override
    public int getEnemiesForWave(int wave) {
        if (wave <= ENDLESS_START_WAVE) {
            return super.getEnemiesForWave(wave);
        }
        int endlessStep = wave - ENDLESS_START_WAVE;
        int baseCount = super.getEnemiesForWave(ENDLESS_START_WAVE);
        float count = baseCount * (float) Math.pow(1.035f, endlessStep);
        return MathUtils.clamp(Math.round(count), baseCount, 500);
    }

    @Override
    protected float getSpawnIntervalForWave(int wave) {
        if (wave <= ENDLESS_START_WAVE) {
            return super.getSpawnIntervalForWave(wave);
        }
        float endlessStep = wave - ENDLESS_START_WAVE;
        float base = super.getSpawnIntervalForWave(ENDLESS_START_WAVE);
        float interval = base * (float) Math.pow(0.985f, endlessStep);
        return Math.max(0.22f, Math.min(base, interval));
    }

    @Override
    protected float getHealthMultiplierForWave(int wave) {
        if (wave <= ENDLESS_START_WAVE) {
            return super.getHealthMultiplierForWave(wave);
        }
        float endlessStep = wave - ENDLESS_START_WAVE;
        float base = super.getHealthMultiplierForWave(ENDLESS_START_WAVE);
        return base * (float) Math.pow(1.06f, endlessStep);
    }

    @Override
    protected float getSpeedMultiplierForWave(int wave) {
        if (wave <= ENDLESS_START_WAVE) {
            return super.getSpeedMultiplierForWave(wave);
        }
        float endlessStep = wave - ENDLESS_START_WAVE;
        float base = super.getSpeedMultiplierForWave(ENDLESS_START_WAVE);
        return Math.min(2.3f, base * (float) Math.pow(1.01f, endlessStep));
    }

    @Override
    protected float getRewardMultiplierForWave(int wave) {
        if (wave <= ENDLESS_START_WAVE) {
            return super.getRewardMultiplierForWave(wave);
        }
        float endlessStep = wave - ENDLESS_START_WAVE;
        float base = super.getRewardMultiplierForWave(ENDLESS_START_WAVE);
        return base * (float) Math.pow(1.05f, endlessStep);
    }

    @Override
    protected JsonValue getWaveConfig(int wave) {
        if (wave <= ENDLESS_START_WAVE) {
            return super.getWaveConfig(wave);
        }
        return null;
    }
}
