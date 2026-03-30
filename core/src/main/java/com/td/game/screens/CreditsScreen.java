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
import com.badlogic.gdx.utils.Array;
import com.td.game.TowerDefenseGame;

public class CreditsScreen implements Screen {
    private final TowerDefenseGame game;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyph;

    private Rectangle backBtn;
    private Array<LinkEntry> linkEntries;

    private static final float LINE_GAP = 28f;
    private static final float SECTION_BREAK = 26f;
    private static final float SYSTEMS_BLOCK_DROP = 34f;
    private static final Color HEADING_COLOR = new Color(0.86f, 0.15f, 0.15f, 1f);
    private static final Color BODY_COLOR = Color.WHITE;
    private static final Color CONTRIBUTOR_COLOR = new Color(0.45f, 0.82f, 1f, 1f);

    private static class LinkEntry {
        Rectangle rect;
        String url;

        LinkEntry(Rectangle rect, String url) {
            this.rect = rect;
            this.url = url;
        }
    }

    public CreditsScreen(TowerDefenseGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(23));
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(36));
        glyph = new GlyphLayout();
        linkEntries = new Array<>();

        recalcLayout();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void recalcLayout() {
        backBtn = new Rectangle(18f, 18f, 180f, 56f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRect(backBtn, new Color(1f, 1f, 1f, 0.12f));
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        drawRect(backBtn, Color.WHITE);
        shapes.end();

        batch.begin();
        linkEntries.clear();
        renderStaticCredits();
        drawCentered(titleFont, "Back", backBtn.x, backBtn.y + backBtn.height * 0.82f, backBtn.width, Color.BLACK);
        batch.end();
    }

    private void renderStaticCredits() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        drawCentered(titleFont, "CREDITS", 0f, h - 30f, w, HEADING_COLOR);

        float leftX = 40f;
        float rightX = w * 0.48f;
        float topY = h - 85f;

        float leftY = topY;
        leftY = drawHeading("Group Members", leftX, leftY);
        leftY = drawLine("- Umit Yusuf GONEN", leftX, leftY, null, CONTRIBUTOR_COLOR);
        leftY = drawLine("- Oguzhan YILMAZ", leftX, leftY, null, CONTRIBUTOR_COLOR);
        leftY = drawLine("- Burhan TURK", leftX, leftY, null, CONTRIBUTOR_COLOR);
        leftY = drawLine("- Ahmet Efe CANPOLAT", leftX, leftY, null, CONTRIBUTOR_COLOR);       
        leftY = drawLine("- Onur Yusuf YILMAZ", leftX, leftY, null, CONTRIBUTOR_COLOR);
        

        leftY -= SECTION_BREAK;
        leftY = drawHeading("Icons", leftX, leftY);
        leftY = drawLine("- All icons found on Flaticon", leftX, leftY, "https://www.flaticon.com/", BODY_COLOR);

        leftY -= SECTION_BREAK;
        leftY = drawHeading("Fonts", leftX, leftY);
        leftY = drawLine("- Paytone One", leftX, leftY, "https://fonts.google.com/specimen/Paytone+One", BODY_COLOR);

        leftY -= (SECTION_BREAK * 2f) + SYSTEMS_BLOCK_DROP;
        leftY = drawHeading("Systems Used", leftX, leftY);
        float systemsStartY = leftY;
        float leftSystemsY = systemsStartY;
        leftSystemsY = drawSystemItem("Java", "https://www.java.com/", leftX, leftSystemsY);
        leftSystemsY = drawSystemItem("GitHub", "https://github.com/", leftX, leftSystemsY);
        leftSystemsY = drawSystemItem("libGDX", "https://libgdx.com/", leftX, leftSystemsY);
        leftSystemsY = drawSystemItem("LWJGL", "https://www.lwjgl.org/", leftX, leftSystemsY);
        leftSystemsY = drawSystemItem("Gradle", "https://gradle.org/", leftX, leftSystemsY);
        leftSystemsY = drawSystemItem("Dreamlo", "http://dreamlo.com/", leftX, leftSystemsY);

        float rightY = topY;
        rightY = drawHeading("MusIc", rightX, rightY);
        rightY = drawLine("- Source Website", rightX, rightY, "https://pixabay.com/music/", BODY_COLOR);
        rightY = drawMusicItem("Main Menu Music", "Roman_Sol", rightX, rightY);
        rightY = drawMusicItem("Elemental Plateau Music", "DanielHren", rightX, rightY);
        rightY = drawMusicItem("Desert Oasis Music", "STAROSTIN", rightX, rightY);

        rightY -= SECTION_BREAK;
        rightY = drawHeading("SFX", rightX, rightY);
        rightY = drawLine("- Source Website:", rightX, rightY,
            "https://pixabay.com/sound-effects/", BODY_COLOR);
        rightY = drawAuthorLine("- Authors: ", "Universfield, Lesiakover, floraphonic", rightX, rightY);
        rightY = drawLine("  FoxBoy Tails, EAGLAXLE, Yodguard, AudioPapkin", rightX, rightY, null, CONTRIBUTOR_COLOR);
        rightY = drawLine("  Prmodrai, freesound_community", rightX, rightY, null, CONTRIBUTOR_COLOR);

        rightY -= SECTION_BREAK;
        rightY = drawHeading("3D Models", rightX, rightY);
        rightY = drawLine("- Quaternius", rightX, rightY, "https://poly.pizza/u/Quaternius", CONTRIBUTOR_COLOR);

        float rightSystemsY = systemsStartY;
        rightSystemsY = drawSystemItem("TTSMaker", "https://ttsmaker.com/", w * 0.2f, rightSystemsY);
        rightSystemsY = drawSystemItem("Pixilart", "https://www.pixilart.com/", w * 0.2f, rightSystemsY);
        rightSystemsY = drawSystemItem("Canva", "https://www.canva.com/", w * 0.2f, rightSystemsY);
        rightSystemsY = drawSystemItem("Microsoft Paint", "https://apps.microsoft.com/detail/9pcfs5b6t72h", w * 0.2f, rightSystemsY);
    }

    private float drawHeading(String text, float x, float y) {
        drawLeft(font, text.toUpperCase(), x, y, HEADING_COLOR);
        return y - LINE_GAP;
    }

    private float drawLine(String text, float x, float y, String url, Color textColor) {
        drawLeft(font, text, x, y, textColor);
        float nextY = y - (LINE_GAP * 1.1f);

        if (url != null && !url.isEmpty()) {
            drawLeft(font, url, x + 14f, nextY, BODY_COLOR);
            glyph.setText(font, url);
            linkEntries.add(new LinkEntry(new Rectangle(x + 14f, nextY - glyph.height, glyph.width, glyph.height + 4f), url));
            nextY -= (LINE_GAP * 1.0f);
        }

        return nextY;
    }

    private float drawMusicItem(String musicName, String author, float x, float y) {
        String prefix = "- " + musicName + " by ";
        drawLeft(font, prefix, x, y, BODY_COLOR);
        glyph.setText(font, prefix);
        drawLeft(font, author, x + glyph.width, y, CONTRIBUTOR_COLOR);
        return y - (LINE_GAP * 1.1f);
    }

    private float drawAuthorLine(String prefix, String names, float x, float y) {
        drawLeft(font, prefix, x, y, BODY_COLOR);
        glyph.setText(font, prefix);
        drawLeft(font, names, x + glyph.width, y, CONTRIBUTOR_COLOR);
        return y - (LINE_GAP * 1.1f);
    }

    private float drawSystemItem(String systemName, String url, float x, float y) {
        drawLeft(font, "- " + systemName, x, y, CONTRIBUTOR_COLOR);
        float nextY = y - (LINE_GAP * 1.1f);

        drawLeft(font, url, x + 14f, nextY, BODY_COLOR);
        glyph.setText(font, url);
        linkEntries.add(new LinkEntry(new Rectangle(x + 14f, nextY - glyph.height, glyph.width, glyph.height + 4f), url));
        nextY -= (LINE_GAP * 1.0f);

        return nextY;
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
        if (font != null) {
            font.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(23));
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(36));
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
        if (font != null) {
            font.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
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
        if (f.exists()) {
            return f;
        }
        f = Gdx.files.internal("assets/" + name);
        if (f.exists()) {
            return f;
        }
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
            if (button != Input.Buttons.LEFT) {
                return false;
            }

            float y = Gdx.graphics.getHeight() - screenY;

            if (linkEntries != null) {
                for (LinkEntry entry : linkEntries) {
                    if (entry != null && entry.url != null && entry.rect.contains(screenX, y)) {
                        game.audio.playClick();
                        Gdx.net.openURI(entry.url);
                        return true;
                    }
                }
            }

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
