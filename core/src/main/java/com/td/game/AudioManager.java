package com.td.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
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
    private Sound pauseToggleSound;
    private Sound speedToggleSound;
    private Sound waveStartSound;
    private Sound waveCompleteSound;
    private Sound victorySound;
    private Sound loseSound;
    private Sound towerAttackBasicSound;
    private float musicVolume;
    private float soundVolume;

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

        FileHandle pauseToggleFile = resolveAsset("audio/sfx/pause_toggle.ogg");
        if (pauseToggleFile.exists()) {
            pauseToggleSound = Gdx.audio.newSound(pauseToggleFile);
        }

        FileHandle speedToggleFile = resolveAsset("audio/sfx/speed_toggle.ogg");
        if (speedToggleFile.exists()) {
            speedToggleSound = Gdx.audio.newSound(speedToggleFile);
        }

        FileHandle waveStartFile = resolveAsset("audio/sfx/wave_start.ogg");
        if (waveStartFile.exists()) {
            waveStartSound = Gdx.audio.newSound(waveStartFile);
        }

        FileHandle waveCompleteFile = resolveAsset("audio/sfx/wave_complete.ogg");
        if (waveCompleteFile.exists()) {
            waveCompleteSound = Gdx.audio.newSound(waveCompleteFile);
        }

        FileHandle victoryFile = resolveAsset("audio/sfx/victory.ogg");
        if (victoryFile.exists()) {
            victorySound = Gdx.audio.newSound(victoryFile);
        }

        FileHandle loseFile = resolveAsset("audio/sfx/lose.ogg");
        if (loseFile.exists()) {
            loseSound = Gdx.audio.newSound(loseFile);
        }

        FileHandle towerAttackBasicFile = resolveAsset("audio/sfx/tower_attack_basic.ogg");
        if (towerAttackBasicFile.exists()) {
            towerAttackBasicSound = Gdx.audio.newSound(towerAttackBasicFile);
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
            clickSound.play(soundVolume * 3.0f);
        }
    }

    public void playError() {
        if (errorSound != null) {
            errorSound.play(soundVolume * 2.6f);
        }
    }

    public void playPauseToggle() {
        if (pauseToggleSound != null) {
            pauseToggleSound.play(soundVolume * 2.4f);
        }
    }

    public void playSpeedToggle() {
        if (speedToggleSound != null) {
            speedToggleSound.play(soundVolume * 2.4f);
        }
    }

    public void playWaveStart() {
        if (waveStartSound != null) {
            waveStartSound.play(soundVolume * 2.4f);
        }
    }

    public void playWaveComplete() {
        if (waveCompleteSound != null) {
            waveCompleteSound.play(soundVolume * 2.5f);
        }
    }

    public void playVictory() {
        if (victorySound != null) {
            victorySound.play(soundVolume * 2.8f);
        }
    }

    public void playLose() {
        if (loseSound != null) {
            loseSound.play(soundVolume * 2.8f);
        }
    }

    public void playTowerAttackBasic() {
        if (towerAttackBasicSound != null) {
            towerAttackBasicSound.play(soundVolume * 1.9f);
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
        if (pauseToggleSound != null) {
            pauseToggleSound.dispose();
            pauseToggleSound = null;
        }
        if (speedToggleSound != null) {
            speedToggleSound.dispose();
            speedToggleSound = null;
        }
        if (waveStartSound != null) {
            waveStartSound.dispose();
            waveStartSound = null;
        }
        if (waveCompleteSound != null) {
            waveCompleteSound.dispose();
            waveCompleteSound = null;
        }
        if (victorySound != null) {
            victorySound.dispose();
            victorySound = null;
        }
        if (loseSound != null) {
            loseSound.dispose();
            loseSound = null;
        }
        if (towerAttackBasicSound != null) {
            towerAttackBasicSound.dispose();
            towerAttackBasicSound = null;
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

