package com.td.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.td.game.input.KeyBindings;

public class OptionsManager {
    private static final String OPTIONS_PATH = "assets/saves/options.json";
    private static OptionsData cached;

    public static OptionsData get() {
        if (cached == null) {
            load();
        }
        return cached;
    }

    public static void load() {
        FileHandle local = Gdx.files.local(OPTIONS_PATH);
        FileHandle internal = Gdx.files.internal(OPTIONS_PATH);
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
        FileHandle local = Gdx.files.local(OPTIONS_PATH);
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
    }

    private static OptionsData createDefault() {
        OptionsData data = new OptionsData();
        data.musicVolume = 0.7f;
        data.soundVolume = 1f;
        data.fullscreen = Gdx.graphics.isFullscreen();
        for (KeyBindings.Action action : KeyBindings.Action.values()) {
            data.bindings.put(action.name(), KeyBindings.getDefaultKey(action));
        }
        return data;
    }
}
