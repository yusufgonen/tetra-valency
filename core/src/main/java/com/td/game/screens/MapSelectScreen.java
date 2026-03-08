package com.td.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.td.game.TowerDefenseGame;
import com.td.game.map.GameMap;

public class MapSelectScreen implements Screen {
    private final TowerDefenseGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont titleFont;
    private BitmapFont textFont;
    private GlyphLayout glyph;
    private Texture backgroundTexture;
    private Texture map1Texture;
    private Texture map2Texture;

    private Rectangle mapA;
    private Rectangle mapB;
    private Rectangle backBtn;
    private Rectangle newGameABtn;
    private Rectangle continueABtn;
    private Rectangle newGameBBtn;
    private Rectangle continueBBtn;

    public MapSelectScreen(TowerDefenseGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(56));
        textFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(28));
        glyph = new GlyphLayout();
        backgroundTexture = loadTextureSafe("ui/main_menu_bg.png");
        map1Texture = loadTextureSafe("ui/map_bg.png");
        map2Texture = loadTextureSafe("ui/map_2_bg.png");
        recalcLayout();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float cardW = w * 0.37f;
        float cardH = h * 0.56f;
        float gap = w * 0.035f;
        float cardsTotalW = cardW * 2f + gap;
        float startX = (w - cardsTotalW) * 0.5f;
        float y = h * 0.19f;
        mapA = new Rectangle(startX, y, cardW, cardH);
        mapB = new Rectangle(startX + cardW + gap, y, cardW, cardH);
        float btnW = 190f;
        float btnH = 48f;
        float btnGap = 12f;
        float btnTotalW = btnW * 2f + btnGap;

        float btnStartXA = mapA.x + (mapA.width - btnTotalW) * 0.5f;
        newGameABtn = new Rectangle(btnStartXA, mapA.y + 24f, btnW, btnH);
        continueABtn = new Rectangle(btnStartXA + btnW + btnGap, mapA.y + 24f, btnW, btnH);

        float btnStartXB = mapB.x + (mapB.width - btnTotalW) * 0.5f;
        newGameBBtn = new Rectangle(btnStartXB, mapB.y + 24f, btnW, btnH);
        continueBBtn = new Rectangle(btnStartXB + btnW + btnGap, mapB.y + 24f, btnW, btnH);
        backBtn = new Rectangle(34f, 30f, 190f, 58f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.03f, 0.04f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        if (backgroundTexture != null) {
            batch.begin();
            batch.draw(backgroundTexture, 0, 0, w, h);
            batch.end();
        }

        float mouseY = h - Gdx.input.getY();
        boolean hoverA = mapA.contains(Gdx.input.getX(), mouseY);
        boolean hoverB = mapB.contains(Gdx.input.getX(), mouseY);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawCard(mapA, hoverA);
        drawCard(mapB, hoverB);

        drawButton(newGameABtn, newGameABtn.contains(Gdx.input.getX(), mouseY));
        drawDisabledButton(continueABtn);

        drawButton(newGameBBtn, newGameBBtn.contains(Gdx.input.getX(), mouseY));
        drawDisabledButton(continueBBtn);
        drawButton(backBtn, backBtn.contains(Gdx.input.getX(), mouseY));
        shapes.end();

        batch.begin();
        titleFont.setColor(new Color(0.95f, 0.77f, 0.32f, 1f));
        drawCentered(titleFont, "CHOOSE YOUR MAP", 0f, h * 0.9f, w);
        textFont.setColor(Color.WHITE);
        drawCentered(textFont, "ELEMENTAL PLATEAU", mapA.x, mapA.y + mapA.height - 18f, mapA.width);
        drawCentered(textFont, "TETRA DESERT", mapB.x, mapB.y + mapB.height - 18f, mapB.width);

        textFont.setColor(new Color(0.16f, 0.11f, 0.06f, 1f));
        drawCentered(textFont, "NEW GAME", newGameABtn.x, newGameABtn.y + 34f, newGameABtn.width);
        textFont.setColor(new Color(0.5f, 0.45f, 0.4f, 1f));
        drawCentered(textFont, "CONTINUE", continueABtn.x, continueABtn.y + 34f, continueABtn.width);

        textFont.setColor(new Color(0.16f, 0.11f, 0.06f, 1f));
        drawCentered(textFont, "NEW GAME", newGameBBtn.x, newGameBBtn.y + 34f, newGameBBtn.width);
        textFont.setColor(new Color(0.5f, 0.45f, 0.4f, 1f));
        drawCentered(textFont, "CONTINUE", continueBBtn.x, continueBBtn.y + 34f, continueBBtn.width);

        drawCentered(textFont, "BACK", backBtn.x, backBtn.y + 40f, backBtn.width);

        if (map1Texture != null) {
            batch.draw(map1Texture, mapA.x + 12f, mapA.y + 96f, mapA.width - 24f, mapA.height - 160f);
        }
        if (map2Texture != null) {
            batch.draw(map2Texture, mapB.x + 12f, mapB.y + 96f, mapB.width - 24f, mapB.height - 160f);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        if (titleFont != null)
            titleFont.dispose();
        if (textFont != null)
            textFont.dispose();
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(56));
        textFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(28));
        recalcLayout();
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
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (map1Texture != null)
            map1Texture.dispose();
        if (map2Texture != null)
            map2Texture.dispose();
        if (titleFont != null)
            titleFont.dispose();
        if (textFont != null)
            textFont.dispose();
        if (batch != null)
            batch.dispose();
        if (shapes != null)
            shapes.dispose();
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

    private void drawCentered(BitmapFont font, String text, float x, float baselineY, float width) {
        glyph.setText(font, text);
        font.draw(batch, text, x + (width - glyph.width) * 0.5f, baselineY);
    }

    private void drawCard(Rectangle r, boolean hover) {
        shapes.setColor(0.1f, 0.12f, 0.18f, hover ? 0.98f : 0.9f);
        shapes.rect(r.x, r.y, r.width, r.height);
        shapes.setColor(hover ? new Color(1f, 0.82f, 0.38f, 1f) : new Color(0.85f, 0.86f, 0.9f, 1f));
        shapes.rect(r.x, r.y + r.height - 6f, r.width, 6f);
        shapes.rect(r.x, r.y, 4f, r.height);
        shapes.rect(r.x + r.width - 4f, r.y, 4f, r.height);
    }

    private void drawButton(Rectangle r, boolean hover) {
        shapes.setColor(hover ? new Color(0.99f, 0.76f, 0.31f, 0.96f) : new Color(0.95f, 0.72f, 0.29f, 0.92f));
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    private void drawDisabledButton(Rectangle r) {
        shapes.setColor(new Color(0.6f, 0.55f, 0.48f, 0.6f));
        shapes.rect(r.x, r.y, r.width, r.height);
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
            if (newGameABtn.contains(screenX, y)) {
                game.audio.playClick();
                game.audio.stopMenuMusic();
                game.setScreen(new GameScreen(game, GameMap.MapType.ELEMENTAL_CASTLE));
                dispose();
                return true;
            }
            if (newGameBBtn.contains(screenX, y)) {
                game.audio.playClick();
                game.audio.stopMenuMusic();
                game.setScreen(new GameScreen(game, GameMap.MapType.DESERT_OASIS));
                dispose();
                return true;
            }
            return false;
        }
    }
}
