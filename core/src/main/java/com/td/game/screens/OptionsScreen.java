package com.td.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.td.game.TowerDefenseGame;
import com.td.game.map.GameMap;

public class OptionsScreen implements Screen {
    private final TowerDefenseGame game;
    private final GameMap.MapType returnMapType;
    private Screen returnScreen;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private GlyphLayout glyph;
    private Texture bgTexture;

    private Rectangle rootPanel;
    private Rectangle backBtn;
    private Rectangle musicLabelPill;
    private Rectangle soundLabelPill;
    private Rectangle musicPercentPill;
    private Rectangle soundPercentPill;
    private Rectangle musicTrack;
    private Rectangle soundTrack;
    private Rectangle musicKnob;
    private Rectangle soundKnob;
    private Rectangle displayModePill;

    private float musicVolume;
    private float soundVolume;
    private boolean draggingMusic;
    private boolean draggingSound;
    private boolean fullscreenMode;

    public OptionsScreen(TowerDefenseGame game) {
        this(game, (GameMap.MapType) null);
    }

    public OptionsScreen(TowerDefenseGame game, GameMap.MapType returnMapType) {
        this.game = game;
        this.returnMapType = returnMapType;
    }

    public OptionsScreen(TowerDefenseGame game, Screen returnScreen) {
        this.game = game;
        this.returnMapType = null;
        this.returnScreen = returnScreen;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(30));
        glyph = new GlyphLayout();
        bgTexture = loadTextureSafe("ui/main_menu_bg.png");
        if (bgTexture == null) {
            bgTexture = loadTextureSafe("ui/augment_screen_bg.png");
        }
        musicVolume = game.audio.getMusicVolume();
        soundVolume = game.audio.getSoundVolume();
        recalcLayout();
        updateKnobsFromVolume();
        fullscreenMode = Gdx.graphics.isFullscreen();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        rootPanel = new Rectangle(w * 0.22f, h * 0.18f, w * 0.56f, h * 0.64f);
        backBtn = new Rectangle(rootPanel.x + 20f, rootPanel.y + 20f, 140f, 48f);

        float row1 = rootPanel.y + rootPanel.height * 0.62f;
        float row2 = rootPanel.y + rootPanel.height * 0.38f;
        musicLabelPill = new Rectangle(rootPanel.x + 60f, row1, 160f, 52f);
        soundLabelPill = new Rectangle(rootPanel.x + 60f, row2, 160f, 52f);
        musicPercentPill = new Rectangle(rootPanel.x + rootPanel.width - 230f, row1, 170f, 52f);
        soundPercentPill = new Rectangle(rootPanel.x + rootPanel.width - 230f, row2, 170f, 52f);

        musicTrack = new Rectangle(rootPanel.x + 90f, row1 - 56f, rootPanel.width - 190f, 8f);
        soundTrack = new Rectangle(rootPanel.x + 90f, row2 - 56f, rootPanel.width - 190f, 8f);
        musicKnob = new Rectangle(musicTrack.x, musicTrack.y - 10f, 26f, 26f);
        soundKnob = new Rectangle(soundTrack.x, soundTrack.y - 10f, 26f, 26f);
        displayModePill = new Rectangle(rootPanel.x + rootPanel.width * 0.5f - 180f, rootPanel.y + 92f, 360f, 52f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        if (bgTexture != null) {
            batch.begin();
            batch.draw(bgTexture, 0, 0, w, h);
            batch.end();
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRect(rootPanel, 34f, new Color(0.89f, 0.67f, 0.26f, 0.96f));
        drawPill(musicLabelPill, new Color(0.56f, 0.43f, 0.33f, 1f));
        drawPill(soundLabelPill, new Color(0.56f, 0.43f, 0.33f, 1f));
        drawPill(musicPercentPill, new Color(0.56f, 0.43f, 0.33f, 1f));
        drawPill(soundPercentPill, new Color(0.56f, 0.43f, 0.33f, 1f));
        drawPill(displayModePill, new Color(0.56f, 0.43f, 0.33f, 1f));
        drawRect(backBtn, backBtn.height * 0.5f, new Color(0.56f, 0.43f, 0.33f, 1f));

        shapes.setColor(new Color(0.82f, 0.82f, 0.82f, 1f));
        shapes.rect(musicTrack.x, musicTrack.y, musicTrack.width, musicTrack.height);
        shapes.rect(soundTrack.x, soundTrack.y, soundTrack.width, soundTrack.height);
        shapes.setColor(new Color(0.15f, 0.18f, 0.22f, 1f));
        shapes.rect(musicTrack.x, musicTrack.y, musicTrack.width * musicVolume, musicTrack.height);
        shapes.rect(soundTrack.x, soundTrack.y, soundTrack.width * soundVolume, soundTrack.height);
        shapes.setColor(new Color(0.24f, 0.27f, 0.33f, 1f));
        shapes.rect(musicKnob.x, musicKnob.y, musicKnob.width, musicKnob.height);
        shapes.rect(soundKnob.x, soundKnob.y, soundKnob.width, soundKnob.height);
        shapes.end();

        batch.begin();
        drawCentered("Options", rootPanel.x, rootPanel.y + rootPanel.height - 34f, rootPanel.width);
        drawCentered("Music:", musicLabelPill.x, musicLabelPill.y + musicLabelPill.height * 0.67f,
                musicLabelPill.width);
        drawCentered("Sound:", soundLabelPill.x, soundLabelPill.y + soundLabelPill.height * 0.67f,
                soundLabelPill.width);
        drawCentered(Math.round(musicVolume * 100f) + "%", musicPercentPill.x,
                musicPercentPill.y + musicPercentPill.height * 0.67f, musicPercentPill.width);
        drawCentered(Math.round(soundVolume * 100f) + "%", soundPercentPill.x,
                soundPercentPill.y + soundPercentPill.height * 0.67f, soundPercentPill.width);
        drawCentered("Display: " + (fullscreenMode ? "Fullscreen" : "Windowed"), displayModePill.x,
                displayModePill.y + displayModePill.height * 0.67f, displayModePill.width);
        drawCentered("Back", backBtn.x, backBtn.y + backBtn.height * 0.67f, backBtn.width);
        batch.end();
    }

    private void toggleDisplayMode() {
        if (fullscreenMode) {
            Gdx.graphics.setWindowedMode(1920, 1080);
            fullscreenMode = false;
        } else {
            DisplayMode dm = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(dm);
            fullscreenMode = true;
        }

        OptionsScreen fresh;
        if (returnScreen != null) {
            fresh = new OptionsScreen(game, returnScreen);
        } else {
            fresh = new OptionsScreen(game, returnMapType);
        }
        fresh.musicVolume = this.musicVolume;
        fresh.soundVolume = this.soundVolume;
        game.setScreen(fresh);
        dispose();
    }

    private void updateKnobsFromVolume() {
        musicKnob.x = musicTrack.x + musicTrack.width * musicVolume - musicKnob.width * 0.5f;
        soundKnob.x = soundTrack.x + soundTrack.width * soundVolume - soundKnob.width * 0.5f;
    }

    private void setMusicVolumeFromX(float x) {
        musicVolume = com.badlogic.gdx.math.MathUtils.clamp((x - musicTrack.x) / musicTrack.width, 0f, 1f);
        game.audio.setMusicVolume(musicVolume);
        updateKnobsFromVolume();
    }

    private void setSoundVolumeFromX(float x) {
        soundVolume = com.badlogic.gdx.math.MathUtils.clamp((x - soundTrack.x) / soundTrack.width, 0f, 1f);
        game.audio.setSoundVolume(soundVolume);
        updateKnobsFromVolume();
    }

    private void drawRect(Rectangle r, float radius, Color c) {
        shapes.setColor(c);
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    private void drawPill(Rectangle r, Color c) {
        drawRect(r, r.height * 0.5f, c);
    }

    private void drawCentered(String text, float x, float baselineY, float width) {
        glyph.setText(font, text);
        font.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
        font.draw(batch, text, x + (width - glyph.width) * 0.5f, baselineY);
    }

    @Override
    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        if (font != null)
            font.dispose();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(30));
        recalcLayout();
        updateKnobsFromVolume();
    }

    private int scaledFontSize(int baseSize) {
        return Math.max(12, Math.round(baseSize * Gdx.graphics.getHeight() / 1080f));
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
        if (bgTexture != null)
            bgTexture.dispose();
        if (font != null)
            font.dispose();
        if (batch != null)
            batch.dispose();
        if (shapes != null)
            shapes.dispose();
    }

    private BitmapFont createFont(String path, int size) {
        FileHandle f = resolveAsset(path);
        if (!f.exists())
            return new BitmapFont();
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(f);
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "\u00e7\u011f\u0131\u015f\u00f6\u00fc\u00c7\u011e\u0130\u015e\u00d6\u00dc";
        p.size = size;
        p.color = Color.WHITE;
        BitmapFont out = gen.generateFont(p);
        gen.dispose();
        return out;
    }

    private Texture loadTextureSafe(String path) {
        FileHandle f = resolveAsset(path);
        if (!f.exists())
            return null;
        try {
            return new Texture(f);
        } catch (Exception e) {
            Gdx.app.log("OptionsScreen", "Texture load failed: " + path + " (" + e.getMessage() + ")");
            return null;
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

    private class InputHandler extends InputAdapter {
        private void goBack() {
            if (returnScreen != null) {
                game.setScreen(returnScreen);
            } else if (returnMapType != null) {
                game.setScreen(new GameScreen(game, returnMapType));
            } else {
                game.setScreen(new MainMenuScreen(game));
            }
            dispose();
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                goBack();
                return true;
            }
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT)
                return false;
            float y = Gdx.graphics.getHeight() - screenY;
            if (backBtn.contains(screenX, y)) {
                game.audio.playClick();
                goBack();
                return true;
            }

            if (musicTrack.contains(screenX, y) || musicKnob.contains(screenX, y)) {
                game.audio.playClick();
                draggingMusic = true;
                setMusicVolumeFromX(screenX);
                return true;
            }
            if (soundTrack.contains(screenX, y) || soundKnob.contains(screenX, y)) {
                game.audio.playClick();
                draggingSound = true;
                setSoundVolumeFromX(screenX);
                return true;
            }
            if (displayModePill.contains(screenX, y)) {
                game.audio.playClick();
                toggleDisplayMode();
                return true;
            }
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (draggingMusic) {
                setMusicVolumeFromX(screenX);
                return true;
            }
            if (draggingSound) {
                setSoundVolumeFromX(screenX);
                return true;
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            draggingMusic = false;
            draggingSound = false;
            return false;
        }
    }
}
