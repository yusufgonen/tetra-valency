package com.td.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.td.game.TowerDefenseGame;

public class HowToPlayScreen implements Screen {
    private final TowerDefenseGame game;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyph;

    private Rectangle backBtn;

    private static final float LINE_GAP = 30f;
    private static final float SECTION_BREAK = 20f;
    private static final Color HEADING_COLOR = new Color(0.95f, 0.72f, 0.29f, 1f);
    private static final Color BODY_COLOR = Color.WHITE;
    private static final Color EMPHASIS_COLOR = new Color(0.45f, 0.82f, 1f, 1f);

    public HowToPlayScreen(TowerDefenseGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(22));
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(36));
        glyph = new GlyphLayout();

        recalcLayout();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void recalcLayout() {
        backBtn = new Rectangle(18f, 18f, 180f, 56f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.04f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRect(backBtn, new Color(1f, 1f, 1f, 0.12f));
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        drawRect(backBtn, Color.WHITE);
        shapes.end();

        batch.begin();
        renderHowToPlayText();
        drawCentered(titleFont, "Back", backBtn.x, backBtn.y + backBtn.height * 0.82f, backBtn.width, Color.BLACK);
        batch.end();
    }

    private void renderHowToPlayText() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        drawCentered(titleFont, "HOW TO PLAY", 0f, h - 30f, w, HEADING_COLOR);

        float leftX = w * 0.15f;
        float rightX = w * 0.55f;
        float topY = h - 100f;

        float leftY = topY;
        leftY = drawHeading("1. Defend the Core", leftX, leftY);
        leftY = drawLine("- Start with 200 Gold & 20 Lives", leftX, leftY, BODY_COLOR);
        leftY = drawLine("- Don't let enemies reach the Core!", leftX, leftY, BODY_COLOR);

        leftY -= SECTION_BREAK;
        leftY = drawHeading("2. Build Pillars", leftX, leftY);
        leftY = drawLine("- Rapid Spire: High attack speed", leftX, leftY, BODY_COLOR);
        leftY = drawLine("- Power Monolith: Heavy damage", leftX, leftY, BODY_COLOR);
        leftY = drawLine("- Sniper Pedestal: Long range", leftX, leftY, BODY_COLOR);

        leftY -= SECTION_BREAK;
        leftY = drawHeading("3. Use the Elements", leftX, leftY);
        leftY = drawLine("- Buy Orbs in Shop: Fire, Water, Earth, Air", leftX, leftY, BODY_COLOR);
        leftY = drawLine("- Equip to Staff & Socket into Pillars", leftX, leftY, BODY_COLOR);

        float rightY = topY;
        rightY = drawHeading("4. Merge & Master", rightX, rightY);
        rightY = drawLine("- Place 2 Orbs on Merge Board for hybrids:", rightX, rightY, BODY_COLOR);
        rightY = drawLine("   Fire + Water = Steam (AoE)", rightX, rightY, EMPHASIS_COLOR);
        rightY = drawLine("   Water + Air  = Ice (Freeze)", rightX, rightY, EMPHASIS_COLOR);
        rightY = drawLine("   Water + Earth= Poison (DoT)", rightX, rightY, EMPHASIS_COLOR);
        rightY = drawLine("   Fire + Air   = Light (Piercing)", rightX, rightY, EMPHASIS_COLOR);
        rightY = drawLine("   Fire + Earth = Gold (Earn Money)", rightX, rightY, EMPHASIS_COLOR);
        rightY = drawLine("   Earth + Air  = Life (Nature)", rightX, rightY, EMPHASIS_COLOR);
    }

    private float drawHeading(String text, float x, float y) {
        drawLeft(font, text.toUpperCase(), x, y, HEADING_COLOR);
        return y - (LINE_GAP * 1.5f);
    }

    private float drawLine(String text, float x, float y, Color textColor) {
        drawLeft(font, text, x, y, textColor);
        return y - LINE_GAP;
    }

    private void drawRect(Rectangle r, Color c) {
        shapes.setColor(c);
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    private void drawCentered(BitmapFont drawFont, String text, float x, float baselineY, float width, Color color) {
        glyph.setText(drawFont, text);
        drawFont.setColor(color);
        drawFont.draw(batch, text, x + (width - glyph.width) * 0.5f, baselineY);
    }

    private void drawLeft(BitmapFont drawFont, String text, float x, float baselineY, Color color) {
        drawFont.setColor(color);
        drawFont.draw(batch, text, x, baselineY);
    }

    @Override
    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(22));
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(36));
        recalcLayout();
    }

    private int scaledFontSize(int baseSize) {
        return Math.max(12, Math.round(baseSize * Gdx.graphics.getHeight() / 1080f));
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
    }

    private BitmapFont createFont(String path, int size) {
        FileHandle f = resolveAsset(path);
        if (!f.exists()) {
            return new BitmapFont();
        }

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

    private static FileHandle resolveAsset(String name) {
        FileHandle f = Gdx.files.internal(name);
        if (f.exists()) return f;
        f = Gdx.files.internal("assets/" + name);
        if (f.exists()) return f;
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
            if (button != Input.Buttons.LEFT) return false;

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
