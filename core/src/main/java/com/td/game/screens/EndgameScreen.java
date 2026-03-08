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

public class EndgameScreen implements Screen {
    public enum EndState {
        WIN,
        LOSE,
        ENDLESS_FINISH
    }

    private final TowerDefenseGame game;
    private final EndState endState;
    private final GameMap.MapType mapType;
    private final int lastWave;
    private final float timerSeconds;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyph;
    private Texture bgTexture;
    private Rectangle rootPanel;
    private Rectangle enterLeaderboardBtn;
    private Rectangle endlessModeBtn;
    private Rectangle mainMenuBtn;
    private Rectangle nicknameModal;
    private Rectangle nicknameInputBox;
    private Rectangle nicknameOkBtn;
    private Rectangle nicknameCancelBtn;
    private String statusMessage = "";
    private boolean nicknamePromptOpen;
    private String nicknameDraft = "";
    private boolean leaderboardSubmitted;
    private static final Color MENU_BASE = new Color(0.95f, 0.72f, 0.29f, 0.94f);
    private static final Color BUTTON_BG = new Color(0.56f, 0.43f, 0.33f, 1f);

    public EndgameScreen(TowerDefenseGame game, EndState endState, GameMap.MapType mapType, int lastWave,
            float timerSeconds) {
        this.game = game;
        this.endState = endState;
        this.mapType = mapType == null ? GameMap.MapType.ELEMENTAL_CASTLE : mapType;
        this.lastWave = lastWave;
        this.timerSeconds = timerSeconds;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont("fonts/font_game_screen.ttf", 28);
        titleFont = createFont("fonts/font_game_screen.ttf", 54);
        glyph = new GlyphLayout();
        bgTexture = loadTextureSafe("ui/main_menu_bg.png");
        recalcLayout();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float panelW = Math.max(560f, w * 0.34f);
        float panelH = Math.max(430f, h * 0.58f);
        rootPanel = new Rectangle((w - panelW) * 0.5f, (h - panelH) * 0.5f, panelW, panelH);

        float btnW = rootPanel.width - 80f;
        float btnH = 58f;
        float x = rootPanel.x + (rootPanel.width - btnW) * 0.5f;
        float y = rootPanel.y + rootPanel.height * 0.40f;

        enterLeaderboardBtn = new Rectangle(x, y, btnW, btnH);
        endlessModeBtn = new Rectangle(x, y - 70f, btnW, btnH);
        mainMenuBtn = new Rectangle(x, y - 140f, btnW, btnH);

        float modalW = Math.max(460f, w * 0.32f);
        float modalH = 220f;
        nicknameModal = new Rectangle((w - modalW) * 0.5f, (h - modalH) * 0.5f, modalW, modalH);
        nicknameInputBox = new Rectangle(nicknameModal.x + 24f, nicknameModal.y + modalH - 120f, modalW - 48f, 54f);
        float modalBtnW = (modalW - 64f) * 0.5f;
        nicknameOkBtn = new Rectangle(nicknameModal.x + 24f, nicknameModal.y + 24f, modalBtnW, 52f);
        nicknameCancelBtn = new Rectangle(nicknameOkBtn.x + modalBtnW + 16f, nicknameModal.y + 24f, modalBtnW, 52f);
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
        drawRect(rootPanel, 30f, MENU_BASE);

        if (endState == EndState.WIN || endState == EndState.ENDLESS_FINISH) {
            drawRect(enterLeaderboardBtn, enterLeaderboardBtn.height * 0.5f, BUTTON_BG);
        }
        if (endState == EndState.WIN) {
            drawRect(endlessModeBtn, endlessModeBtn.height * 0.5f, BUTTON_BG);
        }
        drawRect(mainMenuBtn, mainMenuBtn.height * 0.5f, BUTTON_BG);
        shapes.end();

        batch.begin();
        titleFont.setColor(Color.BLACK);
        float titleBaseline = rootPanel.y + rootPanel.height - 52f;
        float detailBaseline = rootPanel.y + rootPanel.height - 100f;
        if (endState == EndState.WIN) {
            titleFont.getData().setScale(1.2f);
            drawCentered(titleFont, "YOU WIN", rootPanel.x, titleBaseline, rootPanel.width);
            titleFont.getData().setScale(1f);
            font.setColor(Color.BLACK);
            drawCentered(font, "Finish Time: " + formatTimer(timerSeconds), rootPanel.x, detailBaseline - 25f,
                    rootPanel.width);
        } else if (endState == EndState.LOSE) {
            titleFont.setColor(Color.BLACK);
            drawCentered(titleFont, "YOU LOSE", rootPanel.x, titleBaseline, rootPanel.width);
            font.setColor(Color.BLACK);
            drawCentered(font, "Last Wave: " + lastWave, rootPanel.x, detailBaseline, rootPanel.width);
        } else {
            titleFont.setColor(Color.BLACK);
            drawCentered(titleFont, "ENDLESS FINISH", rootPanel.x, titleBaseline, rootPanel.width);
            font.setColor(Color.BLACK);
            drawCentered(font, "Last Wave: " + lastWave, rootPanel.x, detailBaseline, rootPanel.width);
        }

        font.setColor(Color.BLACK);
        if (endState == EndState.WIN || endState == EndState.ENDLESS_FINISH) {
            drawCentered(font, "Enter Leaderboard", enterLeaderboardBtn.x, enterLeaderboardBtn.y + 38f,
                    enterLeaderboardBtn.width);
        }
        if (endState == EndState.WIN) {
            drawCentered(font, "Endless Mode", endlessModeBtn.x, endlessModeBtn.y + 38f, endlessModeBtn.width);
        }
        drawCentered(font, "Main Menu", mainMenuBtn.x, mainMenuBtn.y + 38f, mainMenuBtn.width);

        if (!statusMessage.isEmpty()) {
            font.setColor(Color.BLACK);
            drawCentered(font, statusMessage, rootPanel.x, rootPanel.y + 24f, rootPanel.width);
        }
        batch.end();

        if (nicknamePromptOpen) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            drawRect(nicknameModal, 24f, new Color(0f, 0f, 0f, 0.92f));
            drawRect(nicknameInputBox, 14f, Color.WHITE);
            drawRect(nicknameOkBtn, nicknameOkBtn.height * 0.5f, BUTTON_BG);
            drawRect(nicknameCancelBtn, nicknameCancelBtn.height * 0.5f, BUTTON_BG);
            shapes.end();

            batch.begin();
            titleFont.setColor(Color.WHITE);
            titleFont.getData().setScale(0.72f);
            drawCentered(titleFont, "Enter Nickname", nicknameModal.x, nicknameModal.y + nicknameModal.height - 20f,
                    nicknameModal.width);
            titleFont.getData().setScale(1f);

            font.setColor(Color.BLACK);
            String display = nicknameDraft.isEmpty() ? "Type here..." : nicknameDraft;
            glyph.setText(font, display);
            font.draw(batch, display, nicknameInputBox.x + 14f, nicknameInputBox.y + 34f);

            drawCentered(font, "OK", nicknameOkBtn.x, nicknameOkBtn.y + 34f, nicknameOkBtn.width);
            drawCentered(font, "Cancel", nicknameCancelBtn.x, nicknameCancelBtn.y + 34f, nicknameCancelBtn.width);
            batch.end();
        }

    }

    private void drawRect(Rectangle r, float radius, Color c) {
        shapes.setColor(c);
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    private void handleEnterLeaderboard() {
        nicknamePromptOpen = true;
        nicknameDraft = "";
    }

    private void submitLeaderboardEntry(String name) {
        if (leaderboardSubmitted) {
            return;
        }
        leaderboardSubmitted = true;
        if (endState == EndState.WIN) {
            Dreamlo.uploadTimeScore(name, timerSeconds, mapType);
            game.setScreen(new WinLeaderboardScreen(game, mapType));
            dispose();
            return;
        }
        if (endState == EndState.ENDLESS_FINISH) {
            Dreamlo.uploadWaveScore(name, lastWave, mapType);
            game.setScreen(new EndlessLeaderboardScreen(game, mapType));
            dispose();
        }
    }

    private String formatTimer(float secondsTotal) {
        int minutes = (int) (secondsTotal / 60f);
        int seconds = (int) (secondsTotal % 60f);
        int centis = (int) ((secondsTotal * 100f) % 100f);
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
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
            if (nicknamePromptOpen) {
                if (keycode == Input.Keys.ENTER) {
                    String name = nicknameDraft.trim().isEmpty() ? "Player" : nicknameDraft.trim();
                    statusMessage = "Score submitted: " + name;
                    nicknamePromptOpen = false;
                    submitLeaderboardEntry(name);
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    nicknamePromptOpen = false;
                    return true;
                }
                if (keycode == Input.Keys.BACKSPACE && !nicknameDraft.isEmpty()) {
                    nicknameDraft = nicknameDraft.substring(0, nicknameDraft.length() - 1);
                    return true;
                }
            }
            if (keycode == Input.Keys.ESCAPE) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return true;
            }
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            if (!nicknamePromptOpen) {
                return false;
            }
            if (character >= 32 && character != 127 && nicknameDraft.length() < 18) {
                nicknameDraft += character;
                return true;
            }
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT)
                return false;
            float y = Gdx.graphics.getHeight() - screenY;

            if (nicknamePromptOpen) {
                if (nicknameOkBtn.contains(screenX, y)) {
                    String name = nicknameDraft.trim().isEmpty() ? "Player" : nicknameDraft.trim();
                    statusMessage = "Score submitted: " + name;
                    nicknamePromptOpen = false;
                    submitLeaderboardEntry(name);
                    return true;
                }
                if (nicknameCancelBtn.contains(screenX, y)) {
                    nicknamePromptOpen = false;
                    return true;
                }

                return true;
            }

            if ((endState == EndState.WIN || endState == EndState.ENDLESS_FINISH)
                    && enterLeaderboardBtn.contains(screenX, y)) {
                game.audio.playClick();
                handleEnterLeaderboard();
                return true;
            }
            if (endState == EndState.WIN && endlessModeBtn.contains(screenX, y)) {
                game.audio.playClick();
                game.setScreen(new GameScreen(game, mapType));
                dispose();
                return true;
            }
            if (mainMenuBtn.contains(screenX, y)) {
                game.audio.playClick();
                game.audio.playMenuMusic();
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return true;
            }
            return false;
        }
    }
}
