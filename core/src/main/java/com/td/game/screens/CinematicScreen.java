package com.td.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.td.game.TowerDefenseGame;

import java.util.Arrays;
import java.util.Comparator;

public class CinematicScreen implements Screen {
    private static final float BYTES_PER_SECOND_ESTIMATE = 16000f;
    private static final int SPECIAL_FRAME_INDEX = 5;

    private final TowerDefenseGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private GlyphLayout glyph;
    private Array<Texture> frames;
    private float[] frameDurations;
    private float totalDuration;
    private float elapsed;
    private Music voiceover;
    private boolean finished;
    private Rectangle skipBtn;
    private float prevMusicVolume;
    private float prevSoundVolume;

    public CinematicScreen(TowerDefenseGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(28));
        glyph = new GlyphLayout();
        frames = new Array<>();
        elapsed = 0f;
        finished = false;
        recalcLayout();

        prevMusicVolume = game.audio.getMusicVolume();
        prevSoundVolume = game.audio.getSoundVolume();
        game.audio.stopMenuMusic();
        game.audio.stopMapMusic();
        game.audio.setMusicVolumeTemporary(0f);
        game.audio.setSoundVolumeTemporary(0f);

        FileHandle dir = resolveAsset("cinematic");
        if (dir.exists() && dir.isDirectory()) {
            FileHandle[] files = dir.list();
            Arrays.sort(files, new Comparator<FileHandle>() {
                @Override
                public int compare(FileHandle a, FileHandle b) {
                    int ai = extractLeadingInt(a.name());
                    int bi = extractLeadingInt(b.name());
                    if (ai != -1 && bi != -1) {
                        return Integer.compare(ai, bi);
                    }
                    return a.name().compareToIgnoreCase(b.name());
                }
            });
            for (FileHandle file : files) {
                if (!isImageFile(file.name())) {
                    continue;
                }
                try {
                    frames.add(new Texture(file));
                } catch (Exception e) {
                    Gdx.app.error("CinematicScreen", "Failed to load frame: " + file.path(), e);
                }
            }
        }

        FileHandle voiceoverFile = resolveAsset("audio/voiceover/voiceover.ogg");
        if (voiceoverFile.exists()) {
            voiceover = Gdx.audio.newMusic(voiceoverFile);
            voiceover.setVolume(prevSoundVolume);
            voiceover.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    finished = true;
                }
            });
            voiceover.play();
        }

        totalDuration = estimateDurationSeconds(voiceoverFile);
        if (frames.size == 0) {
            finished = true;
        }
        buildDurations();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float y = Gdx.graphics.getHeight() - screenY;
                if (skipBtn != null && skipBtn.contains(screenX, y)) {
                    finished = true;
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        if (finished || (totalDuration > 0f && elapsed >= totalDuration)) {
            goToMainMenu();
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (frames.size == 0) {
            return;
        }

        int index = getFrameIndex(elapsed);
        index = Math.max(0, Math.min(index, frames.size - 1));
        Texture frame = frames.get(index);
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float imgW = frame.getWidth();
        float imgH = frame.getHeight();
        float scale = Math.max(screenW / imgW, screenH / imgH);
        float drawW = imgW * scale;
        float drawH = imgH * scale;
        float drawX = (screenW - drawW) * 0.5f;
        float drawY = (screenH - drawH) * 0.5f;

        batch.begin();
        batch.draw(frame, drawX, drawY, drawW, drawH);
        batch.end();

        if (skipBtn != null) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(new Color(0.92f, 0.68f, 0.26f, 0.95f));
            shapes.rect(skipBtn.x, skipBtn.y, skipBtn.width, skipBtn.height);
            shapes.end();

            batch.begin();
            glyph.setText(font, "Skip");
            float tx = skipBtn.x + (skipBtn.width - glyph.width) * 0.5f;
            float ty = skipBtn.y + (skipBtn.height + glyph.height) * 0.5f;
            font.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
            font.draw(batch, "Skip", tx, ty);
            batch.end();
        }
    }

    private void buildDurations() {
        int count = frames.size;
        frameDurations = new float[count];
        if (count == 0) {
            return;
        }

        int extra = count > SPECIAL_FRAME_INDEX ? 1 : 0;
        float weight = count + extra;
        float base;
        if (totalDuration <= 0f) {
            base = 2.0f;
            totalDuration = base * weight;
        } else {
            base = totalDuration / weight;
        }
        for (int i = 0; i < count; i++) {
            frameDurations[i] = (i == SPECIAL_FRAME_INDEX ? base * 2f : base);
        }
    }

    private int getFrameIndex(float time) {
        if (frameDurations == null || frameDurations.length == 0) {
            return 0;
        }
        float acc = 0f;
        for (int i = 0; i < frameDurations.length; i++) {
            acc += frameDurations[i];
            if (time <= acc) {
                return i;
            }
        }
        return frameDurations.length - 1;
    }

    private float estimateDurationSeconds(FileHandle file) {
        if (file == null || !file.exists()) {
            return 0f;
        }
        float seconds = file.length() / BYTES_PER_SECOND_ESTIMATE;
        if (seconds < 10f) {
            seconds = 10f;
        } else if (seconds > 180f) {
            seconds = 180f;
        }
        return seconds;
    }

    private boolean isImageFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    private int extractLeadingInt(String name) {
        int idx = 0;
        while (idx < name.length() && Character.isDigit(name.charAt(idx))) {
            idx++;
        }
        if (idx == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(name.substring(0, idx));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void goToMainMenu() {
        if (com.td.game.systems.OptionsManager.get().hasUsername) {
            game.setScreen(new MainMenuScreen(game));
        } else {
            game.setScreen(new NameEntryScreen(game, false));
        }
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (batch != null) {
            batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        }
        if (shapes != null) {
            shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        }
        if (font != null) {
            font.dispose();
        }
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(28));
        recalcLayout();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (voiceover != null) {
            voiceover.stop();
            voiceover.dispose();
            voiceover = null;
        }
        game.audio.setMusicVolumeTemporary(prevMusicVolume);
        game.audio.setSoundVolumeTemporary(prevSoundVolume);
        disposeAll(frames);
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (shapes != null) {
            shapes.dispose();
            shapes = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
    }

    private void disposeAll(Array<? extends Disposable> list) {
        if (list == null) {
            return;
        }
        for (Disposable d : list) {
            if (d != null) {
                d.dispose();
            }
        }
        list.clear();
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

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float bw = Math.max(140f, w * 0.12f);
        float bh = Math.max(44f, h * 0.06f);
        skipBtn = new Rectangle(w - bw - 24f, 24f, bw, bh);
    }

    private int scaledFontSize(int baseSize) {
        return Math.max(12, Math.round(baseSize * Gdx.graphics.getHeight() / 1080f));
    }

    private BitmapFont createFont(String path, int size) {
        FileHandle file = resolveAsset(path);
        if (!file.exists()) {
            return new BitmapFont();
        }
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(file);
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "\u00e7\u011f\u0131\u015f\u00f6\u00fc\u00c7\u011e\u0130\u015e\u00d6\u00dc";
        p.size = size;
        p.color = Color.WHITE;
        BitmapFont f = gen.generateFont(p);
        gen.dispose();
        return f;
    }
}
