package com.td.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Timer;
import com.td.game.map.GameMap;

public class AudioManager {
    private static final String PREFS_NAME = "tetra_valency_audio";
    private static final String KEY_MUSIC_VOL = "music_volume";
    private static final String KEY_SOUND_VOL = "sound_volume";
    private static final float MAP_MUSIC_VOLUME_SCALE = 0.70f;

    private Preferences prefs;
    private Music menuMusic;
    private Music mapMusic;
    private Sound clickSound;
    private Sound errorSound;
    private float musicVolume;
    private float soundVolume;
    private static final float CLICK_DELAY_SEC = 0.2f;

    public void init() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        musicVolume = prefs.getFloat(KEY_MUSIC_VOL, 0.7f);
        soundVolume = prefs.getFloat(KEY_SOUND_VOL, 1f);

        FileHandle clickFile = resolveAsset("audio/sfx/ui_click.ogg");
        if (!clickFile.exists()) {
            clickFile = resolveAsset("audio/sfx/ui_click_primary.ogg");
        }
        if (!clickFile.exists()) {
            clickFile = resolveAsset("audio/clicked.mp3");
        }
        if (clickFile.exists()) {
            clickSound = Gdx.audio.newSound(clickFile);
        }

        FileHandle errorFile = resolveAsset("audio/sfx/ui_error.ogg");
        if (errorFile.exists()) {
            errorSound = Gdx.audio.newSound(errorFile);
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setMusicVolume(float vol) {
        musicVolume = vol;
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.setVolume(musicVolume);
        }
        if (mapMusic != null && mapMusic.isPlaying()) {
            mapMusic.setVolume(musicVolume * MAP_MUSIC_VOLUME_SCALE);
        }
        prefs.putFloat(KEY_MUSIC_VOL, musicVolume);
        prefs.flush();
    }

    public void setSoundVolume(float vol) {
        soundVolume = vol;
        prefs.putFloat(KEY_SOUND_VOL, soundVolume);
        prefs.flush();
    }

    public void playClick() {
        if (clickSound != null) {
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if (clickSound != null) {
                        clickSound.play(soundVolume * 3.0f);
                    }
                }
            }, CLICK_DELAY_SEC);
        }
    }

    public void playError() {
        if (errorSound != null) {
            errorSound.play(soundVolume * 2.6f);
        }
    }

    public void playMenuMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            return;
        }
        if (menuMusic == null) {
            FileHandle f = resolveAsset("audio/music/music_menu.ogg");
            if (!f.exists()) {
                return;
            }
            menuMusic = Gdx.audio.newMusic(f);
            menuMusic.setLooping(true);
        }
        menuMusic.setVolume(musicVolume);
        menuMusic.play();
    }

    public void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
        }
    }

    public void playMapMusic(GameMap.MapType mapType) {
        stopMapMusic();
        String path;
        if (mapType == GameMap.MapType.DESERT_OASIS) {
            path = "audio/music/music_map_2.ogg";
        } else {
            path = "audio/music/music_map_1.ogg";
        }
        FileHandle f = resolveAsset(path);
        if (!f.exists()) {
            return;
        }
        mapMusic = Gdx.audio.newMusic(f);
        mapMusic.setLooping(true);
        mapMusic.setVolume(musicVolume * MAP_MUSIC_VOLUME_SCALE);
        mapMusic.play();
    }

    public void stopMapMusic() {
        if (mapMusic != null) {
            mapMusic.stop();
            mapMusic.dispose();
            mapMusic = null;
        }
    }

    public void dispose() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
            menuMusic = null;
        }
        if (mapMusic != null) {
            mapMusic.stop();
            mapMusic.dispose();
            mapMusic = null;
        }
        if (clickSound != null) {
            clickSound.dispose();
            clickSound = null;
        }
        if (errorSound != null) {
            errorSound.dispose();
            errorSound = null;
        }
    }

    private static FileHandle resolveAsset(String name) {
        FileHandle f = Gdx.files.internal(name);
        if (f.exists())
            return f;
        f = Gdx.files.internal("assets/" + name);
        if (f.exists())
            return f;
        return Gdx.files.internal(name);
    }
}

