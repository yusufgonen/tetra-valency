package com.td.game.systems;

import com.badlogic.gdx.utils.ObjectMap;

public class OptionsData {
    public float musicVolume;
    public float soundVolume;
    public boolean fullscreen;
    public ObjectMap<String, Integer> bindings;
    public String username;
    public boolean hasUsername;
    public ObjectMap<String, Integer> highestWaveByMap;
    public ObjectMap<String, Float> bestTimeByMap;

    public OptionsData() {
        bindings = new ObjectMap<>();
        highestWaveByMap = new ObjectMap<>();
        bestTimeByMap = new ObjectMap<>();
        username = "";
        hasUsername = false;
    }
}
