package com.td.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.td.game.elements.Element;
import com.td.game.entities.*;
import com.td.game.utils.ModelFactory;

public class WaveManager implements Disposable {
    private static final int MAX_WAVES = 50;
    private static final int PHOTO_EMPTY_TILES_BETWEEN_ENEMIES = 0;
    private static final Element[] ENEMY_ELEMENTS = {
            Element.FIRE,
            Element.WATER,
            Element.EARTH,
            Element.AIR,
            Element.STEAM,
            Element.ICE,
            Element.POISON,
            Element.LIGHT,
            Element.LIFE
    };

    private int currentWave;
    private int enemiesSpawned;
    private int enemiesInWave;
    private float spawnTimer;
    private float spawnInterval;
    private boolean waveInProgress;
    private boolean allWavesComplete;
    private com.badlogic.gdx.utils.IntSet wavesShownAugments = new com.badlogic.gdx.utils.IntSet();

    private Array<Vector3> pathWaypoints;
    private Array<Enemy> activeEnemies;
    private ModelFactory modelFactory;
    private final Array<Integer> photoEnemyTypes;
    private JsonValue wavesData;

    private static final int TYPE_PINK_BLOB = 0;
    private static final int TYPE_GOLEM = 1;
    private static final int TYPE_BAT = 2;

    private Model pinkBlobModel;
    private Model golemModel;
    private Model batModel;
    private Model demonModel;

    public WaveManager(Array<Vector3> pathWaypoints, ModelFactory modelFactory) {
        this.pathWaypoints = pathWaypoints;
        this.modelFactory = modelFactory;
        this.currentWave = 0;
        this.waveInProgress = false;
        this.allWavesComplete = false;
        this.activeEnemies = new Array<>();
        this.photoEnemyTypes = new Array<>();

        pinkBlobModel = modelFactory.loadPinkBlobModel();
        photoEnemyTypes.add(TYPE_PINK_BLOB);

        golemModel = modelFactory.loadGolemModel();
        photoEnemyTypes.add(TYPE_GOLEM);

        batModel = modelFactory.loadBatModel();
        photoEnemyTypes.add(TYPE_BAT);

        demonModel = modelFactory.loadDemonModel();

        try {
            JsonReader parser = new JsonReader();
            JsonValue root = parser.parse(Gdx.files.internal("data/waves.json"));
            this.wavesData = root.get("waves");
            Gdx.app.log("WaveManager", "Loaded waves.json successfully");
        } catch (Exception e) {
            Gdx.app.error("WaveManager", "Failed to load waves.json: " + e.getMessage());
            this.wavesData = null;
        }
    }

    public void startNextWave() {
        if (waveInProgress)
            return;

        currentWave++;
        waveInProgress = true;

        // Calculate enemies for this wave
        enemiesInWave = getEnemiesForWave(currentWave);
        enemiesSpawned = 0;
        spawnTimer = 0f;
        spawnInterval = getSpawnIntervalForWave(currentWave);

        Gdx.app.log("WaveManager", String.format("Starting wave %d with %d enemies, spawn interval: %.1f",
                currentWave, enemiesInWave, spawnInterval));
    }

    private JsonValue getWaveConfig(int wave) {
        if (wavesData != null) {
            for (JsonValue w : wavesData) {
                if (w.getInt("wave") == wave) {
                    return w;
                }
            }
        }
        return null;
    }

    public int getEnemiesForWave(int wave) {
        JsonValue config = getWaveConfig(wave);
        if (config != null) {
            return config.getInt("enemyCount", 10 + (wave * 2));
        }
        if (wave == 50) {
            return 1;
        }
        return 10 + (wave * 2);
    }

    private float getSpawnIntervalForWave(int wave) {
        JsonValue config = getWaveConfig(wave);
        if (config != null) {
            return config.getFloat("spawnInterval", Math.max(0.8f, 2.0f - (wave * 0.05f)));
        }
        // Faster spawning as waves progress
        return Math.max(0.8f, 2.0f - (wave * 0.05f));
    }

    public void update(float deltaTime) {
        if (!waveInProgress)
            return;

        // Spawn enemies over time
        if (enemiesSpawned < enemiesInWave) {
            spawnTimer += deltaTime;
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f;
                spawnNextEnemy();
                enemiesSpawned++;
            }
        }

        // Check wave completion
        if (enemiesSpawned >= enemiesInWave && getAliveEnemyCount() == 0) {
            waveInProgress = false;
            allWavesComplete = currentWave == MAX_WAVES;
        }
    }

    private void spawnNextEnemy() {
        Enemy enemy = createEnemyForWave(currentWave, enemiesSpawned);
        enemy.setWaypoints(pathWaypoints);
        activeEnemies.add(enemy);
        Gdx.app.log("WaveManager", "Spawned enemy: " + enemy.getName() + " with element: " + enemy.getElement());
    }

    private Enemy createEnemyForWave(int wave, int index) {
        JsonValue config = getWaveConfig(wave);
        Array<String> types = new Array<>();
        if (config != null && config.has("types")) {
            for (JsonValue t : config.get("types")) {
                types.add(t.asString());
            }
        } else {
            if (wave == 50) {
                types.add("TYPE_DEMON");
            } else {
                types.add("TYPE_PINK_BLOB");
                if (wave > 5)
                    types.add("TYPE_BAT");
                if (wave > 15)
                    types.add("TYPE_GOLEM");
            }
        }

        String typeStr = types.get(index % types.size);
        Enemy enemy;

        float healthMult = 1f + (wave * 0.15f);
        float speedMult = Math.min(1.5f, 1f + (wave * 0.01f));

        if ("TYPE_DEMON".equals(typeStr)) {
            DemonEnemy boss = new DemonEnemy(5000f * healthMult, 0.4f * Math.min(1.2f, speedMult),
                    Math.round(1000 * (1 + wave * 0.1f)));
            boss.setModel(demonModel);
            boss.setVisualScaleMultiplier(4.0f);
            enemy = boss;
        } else if ("TYPE_GOLEM".equals(typeStr)) {
            enemy = new GolemEnemy(
                    220f * healthMult,
                    0.6f * speedMult,
                    Math.round(40 * (1 + wave * 0.1f)));
            enemy.setModel(golemModel);
            enemy.setVisualScaleMultiplier(3.0f);
        } else if ("TYPE_BAT".equals(typeStr)) {
            enemy = new BatEnemy(
                    100f * healthMult,
                    1.3f * speedMult,
                    Math.round(30 * (1 + wave * 0.1f)));
            enemy.setModel(batModel);
            enemy.setVisualScaleMultiplier(1.3f);
        } else { // TYPE_PINK_BLOB
            enemy = new PinkBlobEnemy(
                    55f * healthMult,
                    1.2f * speedMult,
                    Math.round(10 * (1 + wave * 0.1f)));
            enemy.setModel(pinkBlobModel);
            enemy.setVisualScaleMultiplier(1.2f);
        }

        Element randomElement = ENEMY_ELEMENTS[MathUtils.random(ENEMY_ELEMENTS.length - 1)];
        enemy.setElement(randomElement);

        Gdx.app.log("WaveManager", String.format("Created enemy: %s, Health: %.0f, Element: %s",
                enemy.getName(), enemy.getMaxHealth(), randomElement.name()));

        return enemy;
    }

    public int getAliveEnemyCount() {
        int count = 0;
        for (Enemy enemy : activeEnemies) {
            if (enemy.isAlive())
                count++;
        }
        return count;
    }

    public Array<Enemy> getActiveEnemies() {
        return activeEnemies;
    }

    public void removeDeadEnemies() {
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            Enemy enemy = activeEnemies.get(i);
            if (!enemy.isAlive()) {
                activeEnemies.removeIndex(i);
            }
        }
    }

    public void killAllEnemies() {
        for (Enemy enemy : activeEnemies) {
            enemy.takeDamage(enemy.getMaxHealth() * 100);
        }
        enemiesSpawned = enemiesInWave;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;
    }

    public void jumpToWave(int targetWave) {
        this.currentWave = Math.max(0, targetWave - 1);
        this.enemiesSpawned = 0;
        this.enemiesInWave = 0;
        this.spawnTimer = 0f;
        this.spawnInterval = 0f;
        this.waveInProgress = false;
        this.allWavesComplete = false;
    }

    public boolean isWaveInProgress() {
        return waveInProgress;
    }

    public boolean areAllWavesComplete() {
        return allWavesComplete;
    }

    public int getMaxWaves() {
        return MAX_WAVES;
    }

    public int getEnemiesRemaining() {
        return (enemiesInWave - enemiesSpawned) + getAliveEnemyCount();
    }

    public void save(SaveData data) {
        data.currentWave = this.currentWave;
        data.enemiesSpawned = this.enemiesSpawned;
        data.wavesShownAugments = new Array<>();
        for (com.badlogic.gdx.utils.IntSet.IntSetIterator it = wavesShownAugments.iterator(); it.hasNext; ) {
            data.wavesShownAugments.add(it.next());
        }
    }

    public void load(SaveData data) {
        this.currentWave = data.currentWave;
        this.enemiesSpawned = data.enemiesSpawned;
        this.wavesShownAugments.clear();
        if (data.wavesShownAugments != null) {
            for (int wave : data.wavesShownAugments) {
                this.wavesShownAugments.add(wave);
            }
        }
    }

    public boolean hasShownAugmentForWave(int wave) {
        return wavesShownAugments.contains(wave);
    }

    public void setShownAugmentForWave(int wave, boolean shown) {
        if (shown) {
            wavesShownAugments.add(wave);
        } else {
            wavesShownAugments.remove(wave);
        }
    }

    @Override
    public void dispose() {
        if (pinkBlobModel != null)
            pinkBlobModel.dispose();
        if (golemModel != null)
            golemModel.dispose();
        if (batModel != null)
            batModel.dispose();
        if (demonModel != null)
            demonModel.dispose();
        activeEnemies.clear();
    }

    private boolean hasModelFile(String fileName) {
        String path = "assets/3dmodels/" + fileName;
        boolean exists = Gdx.files.internal(path).exists();
        Gdx.app.log("WaveManager", "Checking for model file: " + path + " - " + (exists ? "Found" : "Not found"));
        return exists;
    }
}
