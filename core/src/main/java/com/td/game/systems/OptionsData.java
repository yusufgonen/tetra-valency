package com.td.game.systems;

import com.badlogic.gdx.utils.ObjectMap;

public class OptionsData {
    public float musicVolume;
    public float soundVolume;
    public boolean fullscreen;
    public ObjectMap<String, Integer> bindings;

    public OptionsData() {
        bindings = new ObjectMap<>();
    }
}
