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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WinLeaderboardScreen implements Screen {
    private static final int TOP_COUNT = 5;
    private final TowerDefenseGame game;
    private final GameMap.MapType mapType;
    private final String submittedName;
    private final float submittedTimeSeconds;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyph;
    private Texture bgTexture;
    private Rectangle backBtn;
    private String[][] entries = new String[0][0];
    private boolean loaded;

    public WinLeaderboardScreen(TowerDefenseGame game, GameMap.MapType mapType) {
        this(game, mapType, null, -1f);
    }

    public WinLeaderboardScreen(TowerDefenseGame game, GameMap.MapType mapType, String submittedName, float submittedTimeSeconds) {
        this.game = game;
        this.mapType = mapType;
        this.submittedName = submittedName == null ? "" : submittedName.trim();
        this.submittedTimeSeconds = submittedTimeSeconds;
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
        Dreamlo.fetchScores(true, mapType, 50, data -> Gdx.app.postRunnable(() -> {
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
        drawCentered(titleFont, "WIN LEADERBOARD", 0, h - 70f, w);

        float y = h - 140f;
        List<DisplayRow> rows = buildRowsToRender();
        boolean separatorAdded = false;
        for (DisplayRow row : rows) {
            if (!row.inTopSection && !separatorAdded) {
                y -= 22f;
                separatorAdded = true;
            }
            String line = row.rank + ". " + row.name + " - " + formatTimer(row.timeSeconds);
            font.setColor(row.isSubmitted ? new Color(0.95f, 0.84f, 0.35f, 1f) : Color.WHITE);
            drawCentered(font, line, 0f, y, w);
            y -= 42f;
        }
        if (rows.isEmpty()) {
            drawCentered(font, loaded ? "No entries yet." : "Loading...", 0f, y, w);
        }
        font.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
        drawCentered(font, "Back", backBtn.x, backBtn.y + 34f, backBtn.width);
        batch.end();
    }

    private List<DisplayRow> buildRowsToRender() {
        List<RankedEntry> all = new ArrayList<>();
        for (String[] e : entries) {
            if (e.length < 2) {
                continue;
            }
            all.add(new RankedEntry(e[0], parseFloatSafe(e[1]), false));
        }

        int submittedIndex = -1;
        if (!submittedName.isEmpty() && submittedTimeSeconds >= 0f) {
            float targetTime = (float) ((int) submittedTimeSeconds);
            for (int i = 0; i < all.size(); i++) {
                RankedEntry entry = all.get(i);
                if (entry.name.equals(submittedName) && Math.abs(entry.timeSeconds - targetTime) < 0.001f) {
                    submittedIndex = i;
                    entry.isSubmitted = true;
                    break;
                }
            }
            if (submittedIndex == -1) {
                all.add(new RankedEntry(submittedName, targetTime, true));
                all.sort((a, b) -> Float.compare(a.timeSeconds, b.timeSeconds));
                for (int i = 0; i < all.size(); i++) {
                    if (all.get(i).isSubmitted) {
                        submittedIndex = i;
                        break;
                    }
                }
            }
        }

        List<DisplayRow> rows = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();

        int top = Math.min(TOP_COUNT, all.size());
        for (int i = 0; i < top; i++) {
            addDisplayRow(rows, all, usedIndices, i);
        }

        if (submittedIndex >= TOP_COUNT) {
            addDisplayRow(rows, all, usedIndices, submittedIndex - 1);
            addDisplayRow(rows, all, usedIndices, submittedIndex);
            addDisplayRow(rows, all, usedIndices, submittedIndex + 1);
        }

        if (rows.isEmpty() && !all.isEmpty()) {
            int maxRows = Math.min(10, all.size());
            for (int i = 0; i < maxRows; i++) {
                addDisplayRow(rows, all, usedIndices, i);
            }
        }

        return rows;
    }

    private void addDisplayRow(List<DisplayRow> out, List<RankedEntry> all, Set<Integer> usedIndices, int index) {
        if (index < 0 || index >= all.size() || usedIndices.contains(index)) {
            return;
        }
        usedIndices.add(index);
        RankedEntry entry = all.get(index);
        out.add(new DisplayRow(index + 1, entry.name, entry.timeSeconds, entry.isSubmitted, index < TOP_COUNT));
    }

    private String formatTimer(float s) {
        int minutes = (int) (s / 60f);
        int seconds = (int) (s % 60f);
        int centis = (int) ((s * 100f) % 100f);
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }

    private float parseFloatSafe(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return Float.MAX_VALUE;
        }
    }

    private void drawCentered(BitmapFont f, String text, float x, float baselineY, float width) {
        glyph.setText(f, text);
        f.draw(batch, text, x + (width - glyph.width) * 0.5f, baselineY);
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

    private static final class RankedEntry {
        final String name;
        final float timeSeconds;
        boolean isSubmitted;

        RankedEntry(String name, float timeSeconds, boolean isSubmitted) {
            this.name = name;
            this.timeSeconds = timeSeconds;
            this.isSubmitted = isSubmitted;
        }
    }

    private static final class DisplayRow {
        final int rank;
        final String name;
        final float timeSeconds;
        final boolean isSubmitted;
        final boolean inTopSection;

        DisplayRow(int rank, String name, float timeSeconds, boolean isSubmitted, boolean inTopSection) {
            this.rank = rank;
            this.name = name;
            this.timeSeconds = timeSeconds;
            this.isSubmitted = isSubmitted;
            this.inTopSection = inTopSection;
        }
    }
}
