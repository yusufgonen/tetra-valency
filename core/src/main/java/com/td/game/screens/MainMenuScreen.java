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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.td.game.TowerDefenseGame;

public class MainMenuScreen implements Screen {
    private final TowerDefenseGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont menuFont;
    private Texture bgTexture;
    private Texture logoTexture;

    private GlyphLayout glyphLayout;
    private Rectangle menuPanel;

    private Rectangle newGameBtn;
    private Rectangle leaderboardBtn;
    private Rectangle optionsBtn;

    private Rectangle creditsBtn;
    private Rectangle quitBtn;
    private String uiMessage;
    private float uiMessageTimer;

    public MainMenuScreen(TowerDefenseGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        menuFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(30));
        glyphLayout = new GlyphLayout();

        bgTexture = loadTextureSafe("ui/main_menu_bg.png");
        if (bgTexture == null) {
            bgTexture = loadTextureSafe("ui/augment_screen_bg.png");
        }
        logoTexture = loadTextureSafe("ui/game_logo.png");

        game.audio.playMenuMusic();

        uiMessage = "";
        uiMessageTimer = 0f;

        recalcLayout();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float panelW = Math.max(320f, w * 0.22f);
        float panelH = Math.max(360f, h * 0.34f);
        float panelX = (w - panelW) * 0.5f;
        float panelY = h * 0.18f;
        menuPanel = new Rectangle(panelX, panelY, panelW, panelH);

        float rowPadX = 24f;
        float rowPadY = 26f;
        float rowW = panelW - rowPadX * 2f;
        float rowH = Math.max(44f, panelH * 0.14f);
        float gap = (panelH - rowPadY * 2f - rowH * 5f) / 4f;
        float startY = panelY + panelH - rowPadY - rowH;

        newGameBtn = new Rectangle(panelX + rowPadX, startY, rowW, rowH);
        leaderboardBtn = new Rectangle(panelX + rowPadX, startY - (rowH + gap), rowW, rowH);
        optionsBtn = new Rectangle(panelX + rowPadX, startY - (rowH + gap) * 2f, rowW, rowH);
        creditsBtn = new Rectangle(panelX + rowPadX, startY - (rowH + gap) * 3f, rowW, rowH);
        quitBtn = new Rectangle(panelX + rowPadX, startY - (rowH + gap) * 4f, rowW, rowH);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.03f, 0.04f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        if (bgTexture != null) {
            batch.begin();
            batch.draw(bgTexture, 0, 0, w, h);
            batch.end();
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.95f, 0.72f, 0.29f, 0.94f));
        shapes.rect(menuPanel.x, menuPanel.y, menuPanel.width, menuPanel.height);
        shapes.setColor(1f, 0.86f, 0.54f, 0.5f);
        shapes.rect(menuPanel.x, menuPanel.y + menuPanel.height - 4f, menuPanel.width, 3f);
        shapes.end();

        batch.begin();
        if (logoTexture != null) {
            float logoW = Math.min(w * 0.62f, logoTexture.getWidth());
            float logoH = logoW * ((float) logoTexture.getHeight() / logoTexture.getWidth());
            batch.draw(logoTexture, (w - logoW) * 0.5f, h * 0.58f, logoW, logoH);
        }

        drawMenuLabel("Play", newGameBtn);
        drawMenuLabel("Leaderboard", leaderboardBtn);
        drawMenuLabel("Options", optionsBtn);
        drawMenuLabel("Credits", creditsBtn);
        drawMenuLabel("Quit", quitBtn);

        if (uiMessageTimer > 0f) {
            menuFont.setColor(new Color(0.1f, 0.06f, 0.03f, 1f));
            menuFont.draw(batch, uiMessage, w * 0.5f - 120f, h * 0.13f);
            uiMessageTimer -= delta;
        }
        batch.end();
    }

    private void drawMenuLabel(String text, Rectangle r) {
        glyphLayout.setText(menuFont, text);
        float tx = r.x + (r.width - glyphLayout.width) * 0.5f;
        float ty = r.y + (r.height + glyphLayout.height) * 0.5f;
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hover = r.contains(Gdx.input.getX(), mouseY);
        menuFont.setColor(hover ? new Color(0.10f, 0.07f, 0.04f, 1f) : new Color(0.16f, 0.11f, 0.06f, 1f));
        menuFont.draw(batch, text, tx, ty);
    }

    @Override
    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        if (menuFont != null)
            menuFont.dispose();
        menuFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(30));
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
        if (bgTexture != null)
            bgTexture.dispose();
        if (logoTexture != null)
            logoTexture.dispose();
        if (menuFont != null)
            menuFont.dispose();
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
        BitmapFont f = gen.generateFont(p);
        gen.dispose();
        return f;
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

    private Texture loadTextureSafe(String path) {
        FileHandle f = resolveAsset(path);
        if (!f.exists()) {
            return null;
        }
        try {
            return new Texture(f);
        } catch (Exception e) {
            Gdx.app.log("MainMenuScreen", "Texture load failed: " + path + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private class InputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                Gdx.app.exit();
                return true;
            }
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT)
                return false;
            float y = Gdx.graphics.getHeight() - screenY;
            if (newGameBtn.contains(screenX, y)) {
                game.audio.playClick();
                game.setScreen(new MapSelectScreen(game));
                dispose();
                return true;
            }
            if (leaderboardBtn.contains(screenX, y)) {
                game.audio.playClick();
                game.setScreen(new LeaderboardScreen(game));
                dispose();
                return true;
            }
            if (optionsBtn.contains(screenX, y)) {
                game.audio.playClick();
                game.setScreen(new OptionsScreen(game));
                dispose();
                return true;
            }
            if (creditsBtn.contains(screenX, y)) {
                game.audio.playClick();
                game.setScreen(new CreditsScreen(game));
                dispose();
                return true;
            }
            if (quitBtn.contains(screenX, y)) {
                game.audio.playClick();
                Gdx.app.exit();
                return true;
            }
            return false;
        }
    }
}
