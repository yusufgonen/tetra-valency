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

public class CreditsScreen implements Screen {
    private final TowerDefenseGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyph;
    private Texture bgTexture;
    private Texture logoTexture;

    private Rectangle rootPanel;
    private Rectangle namesPanel;
    private Rectangle backBtn;
    private Rectangle[] nameRows;
    private String[] names;

    public CreditsScreen(TowerDefenseGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(24));
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(36));
        glyph = new GlyphLayout();
        bgTexture = loadTextureSafe("ui/main_menu_bg.png");
        if (bgTexture == null) {
            bgTexture = loadTextureSafe("ui/augment_screen_bg.png");
        }
        logoTexture = loadTextureSafe("ui/cosmovision.png");
        recalcLayout();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        rootPanel = new Rectangle(w * 0.22f, h * 0.08f, w * 0.56f, h * 0.84f);
        backBtn = new Rectangle(rootPanel.x + 22f, rootPanel.y + 22f, 140f, 48f);

        float namesPanelY = rootPanel.y + rootPanel.height * 0.38f;
        float namesPanelH = rootPanel.height * 0.32f;
        namesPanel = new Rectangle(rootPanel.x + 40f, namesPanelY,
                rootPanel.width - 80f, namesPanelH);

        names = new String[] { "Umit Yusuf GONEN", "Ahmet Efe CANPOLAT", "Burhan TURK", "Onur Yusuf YILMAZ",
                "Oguzhan YILMAZ" };
        nameRows = new Rectangle[names.length];
        float pad = 18f;
        float rowW = namesPanel.width - pad * 2f;
        float rowH = 38f;
        float gap = 6f;
        float startY = namesPanel.y + namesPanel.height - rowH;
        for (int i = 0; i < names.length; i++) {
            nameRows[i] = new Rectangle(namesPanel.x + pad, startY - i * (rowH + gap), rowW, rowH);
        }
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

        drawRect(backBtn, backBtn.height * 0.5f, new Color(0.56f, 0.43f, 0.33f, 1f));

        float sepY = namesPanel.y + namesPanel.height + 6f;
        shapes.setColor(new Color(0.72f, 0.55f, 0.30f, 1f));
        shapes.rect(rootPanel.x + 60f, sepY, rootPanel.width - 120f, 2f);

        float sep2Y = nameRows[nameRows.length - 1].y - 8f;
        shapes.rect(rootPanel.x + 60f, sep2Y, rootPanel.width - 120f, 2f);
        shapes.end();

        batch.begin();

        if (logoTexture != null) {
            float logoSize = rootPanel.height * 0.22f;
            float logoX = rootPanel.x + (rootPanel.width - logoSize) * 0.5f;
            float logoY = rootPanel.y + rootPanel.height - logoSize - 16f;
            batch.draw(logoTexture, logoX, logoY, logoSize, logoSize);
        }

        font.setColor(new Color(0.40f, 0.28f, 0.14f, 1f));
        glyph.setText(font, "Team Members");
        font.draw(batch, "Team Members", rootPanel.x + (rootPanel.width - glyph.width) * 0.5f,
                sepY + glyph.height + 6f);

        font.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
        for (int i = 0; i < names.length; i++) {
            drawCentered(names[i], nameRows[i].x, nameRows[i].y + nameRows[i].height * 0.68f, nameRows[i].width);
        }

        font.setColor(new Color(0.40f, 0.28f, 0.14f, 1f));
        glyph.setText(font, "Used Assets");

        float usedAssetsY = sep2Y - glyph.height - 4f;
        font.draw(batch, "Used Assets", rootPanel.x + (rootPanel.width - glyph.width) * 0.5f,
                usedAssetsY);

        font.setColor(new Color(0.52f, 0.38f, 0.22f, 1f));
        glyph.setText(font, "To be updated.");
        font.draw(batch, "To be updated.", rootPanel.x + (rootPanel.width - glyph.width) * 0.5f,
                usedAssetsY - glyph.height - 8f);

        drawCentered("Back", backBtn.x, backBtn.y + backBtn.height * 0.67f, backBtn.width);
        batch.end();
    }

    private void drawRect(Rectangle r, float radius, Color c) {
        shapes.setColor(c);
        shapes.rect(r.x, r.y, r.width, r.height);
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
        if (titleFont != null)
            titleFont.dispose();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(28));
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(52));
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
        if (font != null)
            font.dispose();
        if (titleFont != null)
            titleFont.dispose();
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
            Gdx.app.log("CreditsScreen", "Texture load failed: " + path + " (" + e.getMessage() + ")");
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
