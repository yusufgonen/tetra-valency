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
import com.td.game.systems.OptionsData;
import com.td.game.systems.OptionsManager;
import com.td.game.utils.Dreamlo;

public class NameEntryScreen implements Screen {
    private static final int FETCH_LIMIT = 50;

    private final TowerDefenseGame game;
    private final boolean allowCancel;
    private final Screen returnScreen;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyph;
    private Texture bgTexture;

    private Rectangle rootPanel;
    private Rectangle inputBox;
    private Rectangle okBtn;
    private Rectangle cancelBtn;
    private String draft = "";
    private String statusMessage = "";
    private boolean checking;

    private String[][] timeRows;
    private String[][] waveRows;

    public NameEntryScreen(TowerDefenseGame game, boolean allowCancel) {
        this(game, allowCancel, null);
    }

    public NameEntryScreen(TowerDefenseGame game, boolean allowCancel, Screen returnScreen) {
        this.game = game;
        this.allowCancel = allowCancel;
        this.returnScreen = returnScreen;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", 28);
        titleFont = createFont("fonts/font_game_screen.ttf", 54);
        glyph = new GlyphLayout();
        bgTexture = loadTextureSafe("ui/main_menu_bg.png");
        if (bgTexture == null) {
            bgTexture = loadTextureSafe("ui/augment_screen_bg.png");
        }
        recalcLayout();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float panelW = Math.max(520f, w * 0.36f);
        float panelH = Math.max(320f, h * 0.38f);
        rootPanel = new Rectangle((w - panelW) * 0.5f, (h - panelH) * 0.5f, panelW, panelH);

        inputBox = new Rectangle(rootPanel.x + 40f, rootPanel.y + rootPanel.height * 0.5f - 28f,
                rootPanel.width - 80f, 56f);
        float btnW = (rootPanel.width - 100f) * 0.5f;
        float btnH = 52f;
        okBtn = new Rectangle(rootPanel.x + 40f, rootPanel.y + 40f, btnW, btnH);
        cancelBtn = new Rectangle(okBtn.x + btnW + 20f, okBtn.y, btnW, btnH);
    }

    @Override
    public void render(float delta) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.03f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        if (bgTexture != null) {
            batch.draw(bgTexture, 0, 0, w, h);
        }
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.95f, 0.72f, 0.29f, 0.94f));
        shapes.rect(rootPanel.x, rootPanel.y, rootPanel.width, rootPanel.height);
        shapes.setColor(Color.WHITE);
        shapes.rect(inputBox.x, inputBox.y, inputBox.width, inputBox.height);
        shapes.setColor(new Color(0.56f, 0.43f, 0.33f, 1f));
        shapes.rect(okBtn.x, okBtn.y, okBtn.width, okBtn.height);
        if (allowCancel) {
            shapes.rect(cancelBtn.x, cancelBtn.y, cancelBtn.width, cancelBtn.height);
        }
        shapes.end();

        batch.begin();
        titleFont.setColor(Color.BLACK);
        drawCentered(titleFont, "ENTER USERNAME", rootPanel.x, rootPanel.y + rootPanel.height - 38f,
                rootPanel.width);

        font.setColor(Color.BLACK);
        String display = draft.isEmpty() ? "Type here..." : draft;
        glyph.setText(font, display);
        font.draw(batch, display, inputBox.x + 14f, inputBox.y + 36f);

        String okLabel = checking ? "Checking..." : "OK";
        drawCentered(font, okLabel, okBtn.x, okBtn.y + 34f, okBtn.width);
        if (allowCancel) {
            drawCentered(font, "Cancel", cancelBtn.x, cancelBtn.y + 34f, cancelBtn.width);
        }

        if (!statusMessage.isEmpty()) {
            font.setColor(Color.BLACK);
            drawCentered(font, statusMessage, rootPanel.x, rootPanel.y + 18f, rootPanel.width);
        }
        batch.end();
    }

    private void submitName() {
        if (checking) {
            return;
        }
        String name = draft.trim();
        if (name.isEmpty()) {
            statusMessage = "Enter a name.";
            return;
        }
        checking = true;
        statusMessage = "Checking...";
        timeRows = null;
        waveRows = null;
        Dreamlo.fetchScores(true, null, FETCH_LIMIT, data -> {
            timeRows = data;
            maybeFinishCheck();
        });
        Dreamlo.fetchScores(false, null, FETCH_LIMIT, data -> {
            waveRows = data;
            maybeFinishCheck();
        });
    }

    private void maybeFinishCheck() {
        if (timeRows == null || waveRows == null) {
            return;
        }
        Gdx.app.postRunnable(() -> {
            boolean taken = containsName(timeRows, draft) || containsName(waveRows, draft);
            if (taken) {
                checking = false;
                statusMessage = "Name already taken. Try another.";
                return;
            }
            OptionsData options = OptionsManager.get();
            String oldName = options.username == null ? "" : options.username.trim();
            String newName = draft.trim();
            if (allowCancel && !oldName.isEmpty() && !oldName.equalsIgnoreCase(newName)) {
                Dreamlo.renamePlayerUsingLocalBest(oldName, newName, options);
            }
            options.username = newName;
            options.hasUsername = true;
            OptionsManager.save();
            Screen target = returnScreen != null ? returnScreen : new MainMenuScreen(game);
            game.setScreen(target);
            dispose();
        });
    }

    private boolean containsName(String[][] rows, String name) {
        if (rows == null || rows.length == 0) {
            return false;
        }
        String target = name.trim().toLowerCase();
        for (String[] row : rows) {
            if (row.length < 1) {
                continue;
            }
            if (row[0] != null && row[0].trim().toLowerCase().equals(target)) {
                return true;
            }
        }
        return false;
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
        if (bgTexture != null) {
            bgTexture.dispose();
        }
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

    private class InputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ENTER) {
                submitName();
                return true;
            }
            if (keycode == Input.Keys.ESCAPE) {
                if (allowCancel) {
                    Screen target = returnScreen != null ? returnScreen : new MainMenuScreen(game);
                    game.setScreen(target);
                    dispose();
                }
                return true;
            }
            if (keycode == Input.Keys.BACKSPACE && !draft.isEmpty()) {
                draft = draft.substring(0, draft.length() - 1);
                return true;
            }
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            if (checking) {
                return true;
            }
            if (character >= 32 && character != 127 && draft.length() < 18) {
                draft += character;
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
            if (okBtn.contains(screenX, y)) {
                submitName();
                return true;
            }
            if (allowCancel && cancelBtn.contains(screenX, y)) {
                Screen target = returnScreen != null ? returnScreen : new MainMenuScreen(game);
                game.setScreen(target);
                dispose();
                return true;
            }
            return true;
        }
    }
}
