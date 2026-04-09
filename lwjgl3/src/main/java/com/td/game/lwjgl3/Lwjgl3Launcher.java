package com.td.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.td.game.TowerDefenseGame;

public class Lwjgl3Launcher {

    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new TowerDefenseGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Tetra Valency");
        config.setWindowIcon("icon/tv.png");

        config.setWindowedMode(1600, 900);
        config.setMaximized(true);
        config.setDecorated(true);
        config.setResizable(true);
        config.setForegroundFPS(60);
        config.useVsync(true);

        return config;
    }
}

