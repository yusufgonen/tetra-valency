package com.td.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.td.game.input.KeyBindings;
import com.td.game.map.GameMap;

public class OptionsManager {
    private static final String OPTIONS_FILE = "options.json";
    private static OptionsData cached;

    public static OptionsData get() {
        if (cached == null) {
            load();
        }
        return cached;
    }

    public static void load() {
        FileHandle local = SaveManager.getSharedSaveFile(OPTIONS_FILE);
        FileHandle internal = Gdx.files.internal("saves/" + OPTIONS_FILE);
        OptionsData data = null;
        Json json = new Json();
        if (local.exists()) {
            data = json.fromJson(OptionsData.class, local);
        } else if (internal.exists()) {
            data = json.fromJson(OptionsData.class, internal);
        }
        if (data == null) {
            data = createDefault();
            cached = data;
            save();
            return;
        }
        ensureDefaults(data);
        cached = data;
    }

    public static void save() {
        if (cached == null) {
            return;
        }
        FileHandle local = SaveManager.getSharedSaveFile(OPTIONS_FILE);
        if (!local.parent().exists()) {
            local.parent().mkdirs();
        }
        Json json = new Json();
        local.writeString(json.prettyPrint(cached), false, "UTF-8");
    }

    public static void ensureDefaults(OptionsData data) {
        if (data.bindings == null) {
            data.bindings = new ObjectMap<>();
        }
        if (data.highestWaveByMap == null) {
            data.highestWaveByMap = new ObjectMap<>();
        }
        if (data.bestTimeByMap == null) {
            data.bestTimeByMap = new ObjectMap<>();
        }
        if (data.username == null) {
            data.username = "";
        }
        for (KeyBindings.Action action : KeyBindings.Action.values()) {
            String key = action.name();
            if (!data.bindings.containsKey(key)) {
                data.bindings.put(key, KeyBindings.getDefaultKey(action));
            }
        }
        if (data.musicVolume < 0f) {
            data.musicVolume = 0.7f;
        }
        if (data.soundVolume < 0f) {
            data.soundVolume = 1f;
        }
        for (GameMap.MapType mapType : GameMap.MapType.values()) {
            String key = mapType.name();
            if (!data.highestWaveByMap.containsKey(key)) {
                data.highestWaveByMap.put(key, 0);
            }
            if (!data.bestTimeByMap.containsKey(key)) {
                data.bestTimeByMap.put(key, Float.MAX_VALUE);
            }
        }
    }

    public static void updateHighestWave(GameMap.MapType mapType, int reachedWave) {
        if (cached == null) {
            load();
        }
        if (cached == null || mapType == null) {
            return;
        }
        int normalizedWave = Math.max(1, reachedWave);
        ensureDefaults(cached);
        String key = mapType.name();
        int current = cached.highestWaveByMap.get(key, 0);
        if (normalizedWave > current) {
            cached.highestWaveByMap.put(key, normalizedWave);
            save();
        }
    }

    public static void updateBestTime(GameMap.MapType mapType, float seconds) {
        if (cached == null) {
            load();
        }
        if (cached == null || mapType == null || seconds <= 0f || seconds == Float.MAX_VALUE) {
            return;
        }
        ensureDefaults(cached);
        String key = mapType.name();
        float current = cached.bestTimeByMap.get(key, Float.MAX_VALUE);
        if (seconds < current) {
            cached.bestTimeByMap.put(key, seconds);
            save();
        }
    }

    public static int getHighestWave(GameMap.MapType mapType) {
        if (cached == null) {
            load();
        }
        if (cached == null || mapType == null) {
            return 0;
        }
        ensureDefaults(cached);
        return cached.highestWaveByMap.get(mapType.name(), 0);
    }

    public static float getBestTime(GameMap.MapType mapType) {
        if (cached == null) {
            load();
        }
        if (cached == null || mapType == null) {
            return Float.MAX_VALUE;
        }
        ensureDefaults(cached);
        return cached.bestTimeByMap.get(mapType.name(), Float.MAX_VALUE);
    }

    private static OptionsData createDefault() {
        OptionsData data = new OptionsData();
        data.musicVolume = 0.7f;
        data.soundVolume = 1f;
        data.fullscreen = Gdx.graphics.isFullscreen();
        data.username = "";
        data.hasUsername = false;
        for (KeyBindings.Action action : KeyBindings.Action.values()) {
            data.bindings.put(action.name(), KeyBindings.getDefaultKey(action));
        }
        for (GameMap.MapType mapType : GameMap.MapType.values()) {
            data.highestWaveByMap.put(mapType.name(), 0);
            data.bestTimeByMap.put(mapType.name(), Float.MAX_VALUE);
        }
        return data;
    }
}
