package com.td.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
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
import com.td.game.utils.Dreamlo;

public class EndlessLeaderboardScreen implements Screen {
    private final TowerDefenseGame game;
    private final GameMap.MapType mapType;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyph;
    private Texture bgTexture;
    private Rectangle backBtn;
    private String[][] entries = new String[0][0];
    private boolean loaded;

    public EndlessLeaderboardScreen(TowerDefenseGame game, GameMap.MapType mapType) {
        this.game = game;
        this.mapType = mapType;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", 28);
        titleFont = createFont("fonts/font_game_screen.ttf", 46);
        glyph = new GlyphLayout();
        bgTexture = loadTextureSafe("ui/main_menu_bg.png");
        backBtn = new Rectangle(40f, 32f, 170f, 52f);
        loaded = false;
        Dreamlo.fetchScores(false, mapType, data -> Gdx.app.postRunnable(() -> {
            entries = data == null ? new String[0][0] : data;
            loaded = true;
        }));
        Gdx.input.setInputProcessor(new InputHandler());
    }

    @Override
    public void render(float delta) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        Gdx.gl.glClearColor(0.03f, 0.03f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        if (bgTexture != null) {
            batch.draw(bgTexture, 0, 0, w, h);
        }
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(0, 0, w, h);
        shapes.setColor(0.56f, 0.43f, 0.33f, 1f);
        shapes.rect(backBtn.x, backBtn.y, backBtn.width, backBtn.height);
        shapes.end();

        batch.begin();
        titleFont.setColor(Color.WHITE);
        drawCentered(titleFont, "ENDLESS LEADERBOARD", 0, h - 70f, w);

        float y = h - 140f;
        int idx = 1;
        for (String[] e : entries) {
            int wave = parseIntSafe(e[1]);
            String line = idx + ". " + e[0] + " - Wave " + wave;
            font.setColor(Color.WHITE);
            drawCentered(font, line, 0f, y, w);
            y -= 42f;
            idx++;
            if (idx > 10)
                break;
        }
        if (idx == 1) {
            drawCentered(font, loaded ? "No entries yet." : "Loading...", 0f, y, w);
        }
        font.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
        drawCentered(font, "Back", backBtn.x, backBtn.y + 34f, backBtn.width);
        batch.end();
    }

    private void drawCentered(BitmapFont f, String text, float x, float baselineY, float width) {
        glyph.setText(f, text);
        f.draw(batch, text, x + (width - glyph.width) * 0.5f, baselineY);
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
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

    @Override
    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
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
        if (titleFont != null)
            titleFont.dispose();
        if (batch != null)
            batch.dispose();
        if (shapes != null)
            shapes.dispose();
    }

    private class InputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
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
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return true;
            }
            return false;
        }
    }
}
