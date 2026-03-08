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

public class LeaderboardScreen implements Screen {
    private final TowerDefenseGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private GlyphLayout glyph;
    private Texture bgTexture;

    private Rectangle rootPanel;
    private Rectangle backBtn;
    private Rectangle leftColumn;
    private Rectangle rightColumn;
    private Rectangle leftTitle;
    private Rectangle rightTitle;
    private Rectangle mapBtnA;
    private Rectangle mapBtnB;
    private Rectangle[] leftRows;
    private Rectangle[] rightRows;

    private final String[][] fastestRowsByMap = new String[2][5];
    private final String[][] highestRowsByMap = new String[2][5];
    private int selectedMap = 0;
    private int selectedLeft = -1;
    private int selectedRight = -1;
    private int pendingLoads;

    public LeaderboardScreen(TowerDefenseGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(28));
        glyph = new GlyphLayout();
        bgTexture = loadTextureSafe("ui/main_menu_bg.png");
        if (bgTexture == null) {
            bgTexture = loadTextureSafe("ui/augment_screen_bg.png");
        }
        initLoadingRows();
        recalcLayout();
        loadLeaderboardData();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void initLoadingRows() {
        for (int map = 0; map < 2; map++) {
            for (int i = 0; i < 5; i++) {
                fastestRowsByMap[map][i] = i == 0 ? "Loading..." : "";
                highestRowsByMap[map][i] = i == 0 ? "Loading..." : "";
            }
        }
    }

    private void loadLeaderboardData() {
        pendingLoads = 4;

        loadMapRows(0, GameMap.MapType.ELEMENTAL_CASTLE);
        loadMapRows(1, GameMap.MapType.DESERT_OASIS);
    }

    private void loadMapRows(int mapIndex, GameMap.MapType mapType) {
        Dreamlo.fetchScores(true, mapType, data -> Gdx.app.postRunnable(() -> {
            fillFastestRowsForMap(mapIndex, data);
            pendingLoads = Math.max(0, pendingLoads - 1);
        }));
        Dreamlo.fetchScores(false, mapType, data -> Gdx.app.postRunnable(() -> {
            fillHighestRowsForMap(mapIndex, data);
            pendingLoads = Math.max(0, pendingLoads - 1);
        }));
    }

    private void fillFastestRowsForMap(int mapIndex, String[][] data) {
        fillMapRowWithText(fastestRowsByMap, mapIndex, "No entries yet.");
        if (data == null || data.length == 0) {
            return;
        }
        for (int i = 0; i < 5 && i < data.length; i++) {
            String name = data[i].length > 0 ? data[i][0] : "Player";
            float timeSeconds = data[i].length > 1 ? parseFloatSafe(data[i][1]) : 0f;
            String line = (i + 1) + ". " + name + " - " + formatTimer(timeSeconds);
            fastestRowsByMap[mapIndex][i] = line;
        }
    }

    private void fillHighestRowsForMap(int mapIndex, String[][] data) {
        fillMapRowWithText(highestRowsByMap, mapIndex, "No entries yet.");
        if (data == null || data.length == 0) {
            return;
        }
        for (int i = 0; i < 5 && i < data.length; i++) {
            String name = data[i].length > 0 ? data[i][0] : "Player";
            int wave = data[i].length > 1 ? parseIntSafe(data[i][1]) : 0;
            String line = (i + 1) + ". " + name + " - Wave " + wave;
            highestRowsByMap[mapIndex][i] = line;
        }
    }

    private void fillMapRowWithText(String[][] target, int mapIndex, String firstRowText) {
        for (int i = 0; i < 5; i++) {
            target[mapIndex][i] = i == 0 ? firstRowText : "";
        }
    }

    private void fillEmptyRows(String[][] target, String firstRowText) {
        for (int mapIndex = 0; mapIndex < 2; mapIndex++) {
            fillMapRowWithText(target, mapIndex, firstRowText);
        }
    }

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        rootPanel = new Rectangle(w * 0.18f, h * 0.12f, w * 0.64f, h * 0.76f);
        backBtn = new Rectangle(rootPanel.x + 18f, rootPanel.y + 18f, 140f, 48f);
        float mapBtnW = rootPanel.width * 0.28f;
        float mapBtnH = 46f;
        float mapBtnGap = 18f;
        float mapTotalW = mapBtnW * 2f + mapBtnGap;
        float mapStartX = rootPanel.x + (rootPanel.width - mapTotalW) * 0.5f;
        float mapY = rootPanel.y + rootPanel.height - 126f;
        mapBtnA = new Rectangle(mapStartX, mapY, mapBtnW, mapBtnH);
        mapBtnB = new Rectangle(mapStartX + mapBtnW + mapBtnGap, mapY, mapBtnW, mapBtnH);

        float colPad = 24f;
        float colW = rootPanel.width * 0.42f;
        float colH = rootPanel.height * 0.62f;
        float colGap = rootPanel.width - colW * 2f - 64f;
        leftColumn = new Rectangle(rootPanel.x + 32f, rootPanel.y + 90f, colW, colH);
        rightColumn = new Rectangle(leftColumn.x + colW + colGap, rootPanel.y + 90f, colW, colH);

        float innerW = colW - colPad * 2f;
        float titleH = 48f;
        leftTitle = new Rectangle(leftColumn.x + colPad, leftColumn.y + leftColumn.height - titleH - 16f,
                innerW, titleH);
        rightTitle = new Rectangle(rightColumn.x + colPad, rightColumn.y + rightColumn.height - titleH - 16f,
                innerW, titleH);

        leftRows = new Rectangle[5];
        rightRows = new Rectangle[5];
        float rowW = innerW;
        float rowH = 42f;
        float rowGap2 = 10f;
        float startY = leftTitle.y - 18f - rowH;
        for (int i = 0; i < 5; i++) {
            float y = startY - i * (rowH + rowGap2);
            leftRows[i] = new Rectangle(leftColumn.x + colPad, y, rowW, rowH);
            rightRows[i] = new Rectangle(rightColumn.x + colPad, y, rowW, rowH);
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
        drawPill(mapBtnA, selectedMap == 0 ? new Color(1f, 0.82f, 0.4f, 1f) : new Color(0.56f, 0.43f, 0.33f, 1f));
        drawPill(mapBtnB, selectedMap == 1 ? new Color(1f, 0.82f, 0.4f, 1f) : new Color(0.56f, 0.43f, 0.33f, 1f));
        drawRect(leftColumn, 24f, new Color(0.81f, 0.65f, 0.43f, 1f));
        drawRect(rightColumn, 24f, new Color(0.81f, 0.65f, 0.43f, 1f));
        drawPill(leftTitle, new Color(0.56f, 0.43f, 0.33f, 1f));
        drawPill(rightTitle, new Color(0.56f, 0.43f, 0.33f, 1f));
        for (int i = 0; i < 5; i++) {
            Color leftC = i == selectedLeft ? new Color(1f, 0.82f, 0.4f, 1f) : new Color(0.98f, 0.76f, 0.31f, 0.95f);
            Color rightC = i == selectedRight ? new Color(1f, 0.82f, 0.4f, 1f) : new Color(0.98f, 0.76f, 0.31f, 0.95f);
            drawPill(leftRows[i], leftC);
            drawPill(rightRows[i], rightC);
        }
        drawPill(backBtn, new Color(0.56f, 0.43f, 0.33f, 1f));
        shapes.end();

        batch.begin();
        drawCentered("Leaderboard", rootPanel.x, rootPanel.y + rootPanel.height - 40f, rootPanel.width);
        drawCentered("ELEMENTAL PLATEAU", mapBtnA.x, mapBtnA.y + mapBtnA.height * 0.67f, mapBtnA.width);
        drawCentered("TETRA DESERT", mapBtnB.x, mapBtnB.y + mapBtnB.height * 0.67f, mapBtnB.width);
        drawCentered("Fastest Completion", leftTitle.x, leftTitle.y + leftTitle.height * 0.67f, leftTitle.width);
        drawCentered("Highest Wave", rightTitle.x, rightTitle.y + rightTitle.height * 0.67f, rightTitle.width);
        for (int i = 0; i < 5; i++) {
            drawCentered(fastestRowsByMap[selectedMap][i], leftRows[i].x,
                    leftRows[i].y + leftRows[i].height * 0.68f, leftRows[i].width);
            drawCentered(highestRowsByMap[selectedMap][i], rightRows[i].x,
                    rightRows[i].y + rightRows[i].height * 0.68f, rightRows[i].width);
        }
        if (pendingLoads > 0) {
            drawCentered("Syncing leaderboard...", rootPanel.x, rootPanel.y + 26f, rootPanel.width);
        }
        drawCentered("Back", backBtn.x, backBtn.y + backBtn.height * 0.67f, backBtn.width);
        batch.end();
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

    private String formatTimer(float s) {
        int minutes = (int) (s / 60f);
        int seconds = (int) (s % 60f);
        int centis = (int) ((s * 100f) % 100f);
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private float parseFloatSafe(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return 0f;
        }
    }

    @Override
    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        if (font != null)
            font.dispose();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(28));
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
            Gdx.app.log("LeaderboardScreen", "Texture load failed: " + path + " (" + e.getMessage() + ")");
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
            if (mapBtnA.contains(screenX, y)) {
                game.audio.playClick();
                selectedMap = 0;
                selectedLeft = -1;
                selectedRight = -1;
                return true;
            }
            if (mapBtnB.contains(screenX, y)) {
                game.audio.playClick();
                selectedMap = 1;
                selectedLeft = -1;
                selectedRight = -1;
                return true;
            }
            for (int i = 0; i < 5; i++) {
                if (leftRows[i].contains(screenX, y)) {
                    game.audio.playClick();
                    selectedLeft = i;
                    return true;
                }
                if (rightRows[i].contains(screenX, y)) {
                    game.audio.playClick();
                    selectedRight = i;
                    return true;
                }
            }
            return false;
        }
    }
}
