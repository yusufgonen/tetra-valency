package com.td.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.td.game.systems.EconomyManager;
import com.td.game.systems.WaveManager;

public class ConsoleMenu {
    public interface Context {
        EconomyManager getEconomyManager();
        WaveManager getWaveManager();
        float getElapsedTime();
        void showMessage(String msg);
        void clearGameOver();
        void killAllEnemies();
        void removeDeadEnemies();
        boolean openEndgameForKillAllIfNeeded(float elapsedTime);
        void winNormal(float elapsedTime);
        void loseEndless(float elapsedTime);
        void lose(float elapsedTime);
        boolean isAugmentAcquired(int id);
        void applyAugmentById(int id);
        void addAcquiredAugment(int id);
    }

    public static class Layout {
        public final Rectangle panel = new Rectangle();
        public final Rectangle livesInputBox = new Rectangle();
        public final Rectangle goldInputBox = new Rectangle();
        public final Rectangle waveBox = new Rectangle();
        public final Rectangle augmentInputBox = new Rectangle();
        public final Rectangle[] buttons;
        public float titleY;
        public float livesLabelY;
        public float goldLabelY;
        public float waveLabelY;

        public Layout(int buttonCount) {
            buttons = new Rectangle[buttonCount];
            for (int i = 0; i < buttonCount; i++) {
                buttons[i] = new Rectangle();
            }
        }
    }

    private static final int INPUT_NONE = 0;
    private static final int INPUT_LIVES = 1;
    private static final int INPUT_WAVE = 2;
    private static final int INPUT_GOLD = 3;
    private static final int INPUT_AUGMENT = 4;
    private static final int MAX_AUGMENT_ID = 8;

    private boolean open = false;
    private int activeInput = INPUT_NONE;
    private String livesInput = "";
    private String waveInput = "";
    private String goldInput = "";
    private String augmentInput = "";
    private boolean coreShieldEnabled = false;

    public boolean isOpen() {
        return open;
    }

    public boolean isCoreShieldEnabled() {
        return coreShieldEnabled;
    }

    public void toggle() {
        open = !open;
        activeInput = INPUT_NONE;
        if (open) {
            livesInput = "";
            waveInput = "";
            goldInput = "";
            augmentInput = "";
        }
    }

    public boolean handleKeyDown(int keycode, Context ctx) {
        if (!open) {
            return false;
        }
        if (activeInput != INPUT_NONE) {
            if (keycode == Input.Keys.BACKSPACE) {
                removeLastInputChar();
                return true;
            }
            if (keycode == Input.Keys.FORWARD_DEL) {
                setActiveInputValue("");
                return true;
            }
            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                applyActiveInput(ctx);
                return true;
            }
            if (keycode == Input.Keys.ESCAPE) {
                activeInput = INPUT_NONE;
                return true;
            }
        } else {
            if (keycode == Input.Keys.NUM_1) {
                handleCommand("lose", ctx);
                return true;
            }
            if (keycode == Input.Keys.NUM_2) {
                handleCommand("loseendless", ctx);
                return true;
            }
            if (keycode == Input.Keys.NUM_3) {
                handleCommand("winnormal", ctx);
                return true;
            }
        }
        return false;
    }

    public boolean handleKeyTyped(char character) {
        if (!open || activeInput == INPUT_NONE) {
            return false;
        }
        if (character >= '0' && character <= '9') {
            String value = getActiveInputValue();
            if (value.length() < 6) {
                setActiveInputValue(value + character);
            }
            return true;
        }
        return false;
    }

    public boolean handleTouchDown(int screenX, int flippedY, int screenWidth, int screenHeight, int button, float uiScale, Context ctx) {
        if (!open || button != Input.Buttons.LEFT) {
            return false;
        }
        Layout layout = getLayout(screenWidth, screenHeight, uiScale, getButtonLabels().length);
        if (isInRect(screenX, flippedY, layout.livesInputBox.x, layout.livesInputBox.y,
                layout.livesInputBox.width, layout.livesInputBox.height)) {
            activeInput = INPUT_LIVES;
            return true;
        }
        if (isInRect(screenX, flippedY, layout.waveBox.x, layout.waveBox.y, layout.waveBox.width, layout.waveBox.height)) {
            activeInput = INPUT_WAVE;
            return true;
        }
        if (isInRect(screenX, flippedY, layout.goldInputBox.x, layout.goldInputBox.y,
                layout.goldInputBox.width, layout.goldInputBox.height)) {
            activeInput = INPUT_GOLD;
            return true;
        }
        if (isInRect(screenX, flippedY, layout.augmentInputBox.x, layout.augmentInputBox.y,
                layout.augmentInputBox.width, layout.augmentInputBox.height)) {
            activeInput = INPUT_AUGMENT;
            return true;
        }
        activeInput = INPUT_NONE;
        for (int i = 0; i < layout.buttons.length; i++) {
            Rectangle buttonRect = layout.buttons[i];
            if (isInRect(screenX, flippedY, buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height)) {
                handleCommand(getButtonCommands()[i], ctx);
                return true;
            }
        }
        return isInRect(screenX, flippedY, layout.panel.x, layout.panel.y, layout.panel.width, layout.panel.height);
    }

    public void render(int screenWidth,
                       int screenHeight,
                       float uiScale,
                       Context ctx,
                       SpriteBatch uiBatch,
                       ShapeRenderer uiShapeRenderer,
                       BitmapFont uiFont,
                       BitmapFont uiFontLarge,
                       GlyphLayout glyphLayout) {
        if (!open) {
            return;
        }

        String[] buttonLabels = getButtonLabels();
        Layout layout = getLayout(screenWidth, screenHeight, uiScale, buttonLabels.length);
        float defaultFontScale = uiScale * 0.54f;
        float defaultLargeFontScale = uiScale * 0.72f;

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.92f);
        uiShapeRenderer.rect(layout.panel.x, layout.panel.y, layout.panel.width, layout.panel.height);
        drawInputBox(uiShapeRenderer, layout.livesInputBox, activeInput == INPUT_LIVES);
        drawInputBox(uiShapeRenderer, layout.waveBox, activeInput == INPUT_WAVE);
        drawInputBox(uiShapeRenderer, layout.goldInputBox, activeInput == INPUT_GOLD);
        drawInputBox(uiShapeRenderer, layout.augmentInputBox, activeInput == INPUT_AUGMENT);
        uiShapeRenderer.setColor(0.16f, 0.16f, 0.16f, 0.98f);
        for (Rectangle button : layout.buttons) {
            uiShapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        uiShapeRenderer.end();

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        uiShapeRenderer.setColor(0.95f, 0.95f, 0.95f, 1f);
        uiShapeRenderer.rect(layout.panel.x, layout.panel.y, layout.panel.width, layout.panel.height);
        uiShapeRenderer.rect(layout.livesInputBox.x, layout.livesInputBox.y, layout.livesInputBox.width, layout.livesInputBox.height);
        uiShapeRenderer.rect(layout.goldInputBox.x, layout.goldInputBox.y, layout.goldInputBox.width, layout.goldInputBox.height);
        uiShapeRenderer.rect(layout.waveBox.x, layout.waveBox.y, layout.waveBox.width, layout.waveBox.height);
        uiShapeRenderer.rect(layout.augmentInputBox.x, layout.augmentInputBox.y, layout.augmentInputBox.width, layout.augmentInputBox.height);
        for (Rectangle button : layout.buttons) {
            uiShapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.getData().setScale(uiScale * 0.54f);
        uiFontLarge.setColor(Color.WHITE);
        glyphLayout.setText(uiFontLarge, "CONSOLE MENU");
        uiFontLarge.draw(uiBatch, glyphLayout,
                layout.panel.x + (layout.panel.width - glyphLayout.width) * 0.5f,
                layout.titleY);

        uiFont.getData().setScale(uiScale * 0.50f);
        uiFont.setColor(Color.WHITE);

        EconomyManager economy = ctx.getEconomyManager();
        WaveManager waves = ctx.getWaveManager();

        String livesLabel = "HEALTH";
        String livesPlaceholder = "";
        if (economy != null) {
            livesLabel = "HEALTH (" + economy.getLives() + ")";
            livesPlaceholder = String.valueOf(economy.getLives());
        }
        drawFieldLabel(uiBatch, uiFont, glyphLayout, livesLabel, layout.livesLabelY, layout.livesInputBox);
        drawInputField(uiBatch, uiFontLarge, glyphLayout, uiScale, layout.livesInputBox, livesInput, livesPlaceholder);

        String waveLabel = "WAVE";
        String wavePlaceholder = "";
        if (waves != null) {
            waveLabel = "WAVE (" + waves.getCurrentWave() + ")";
            wavePlaceholder = String.valueOf(waves.getCurrentWave());
        }
        drawFieldLabel(uiBatch, uiFont, glyphLayout, waveLabel, layout.waveLabelY, layout.waveBox);
        drawInputField(uiBatch, uiFontLarge, glyphLayout, uiScale, layout.waveBox, waveInput, wavePlaceholder);

        String goldLabel = "GOLD";
        String goldPlaceholder = "";
        if (economy != null) {
            goldLabel = "GOLD (" + economy.getGold() + ")";
            goldPlaceholder = String.valueOf(economy.getGold());
        }
        drawFieldLabel(uiBatch, uiFont, glyphLayout, goldLabel, layout.goldLabelY, layout.goldInputBox);
        drawInputField(uiBatch, uiFontLarge, glyphLayout, uiScale, layout.goldInputBox, goldInput, goldPlaceholder);

        drawInputField(uiBatch, uiFontLarge, glyphLayout, uiScale, layout.augmentInputBox, augmentInput, "");

        uiFontLarge.getData().setScale(uiScale * 0.44f);
        for (int i = 0; i < buttonLabels.length; i++) {
            Rectangle button = layout.buttons[i];
            glyphLayout.setText(uiFontLarge, buttonLabels[i], Color.WHITE, button.width,
                    com.badlogic.gdx.utils.Align.center, false);
            float textY = button.y + button.height * 0.5f + glyphLayout.height * 0.5f;
            uiFontLarge.draw(uiBatch, glyphLayout, button.x, textY);
        }
        uiBatch.end();

        uiFont.getData().setScale(defaultFontScale);
        uiFontLarge.getData().setScale(defaultLargeFontScale);
    }

    private void handleCommand(String command, Context ctx) {
        if (command == null) {
            return;
        }
        String cmd = command.trim().toLowerCase(java.util.Locale.ROOT);
        float elapsedTime = ctx.getElapsedTime();
        switch (cmd) {
            case "killall":
                ctx.killAllEnemies();
                ctx.removeDeadEnemies();
                ctx.openEndgameForKillAllIfNeeded(elapsedTime);
                break;
            case "winnormal":
                ctx.winNormal(-1f);
                break;
            case "loseendless":
                ctx.loseEndless(-1f);
                break;
            case "lose":
                ctx.lose(-1f);
                break;
            case "coreinvuln":
                coreShieldEnabled = !coreShieldEnabled;
                ctx.showMessage(coreShieldEnabled ? "Core shield enabled." : "Core shield disabled.");
                break;
            case "addaugment":
                applyAugmentFromConsole(ctx);
                break;
            default:
                break;
        }
    }

    private void applyActiveInput(Context ctx) {
        switch (activeInput) {
            case INPUT_LIVES:
                setLivesFromConsole(ctx);
                break;
            case INPUT_WAVE:
                jumpToWaveFromConsole(ctx);
                break;
            case INPUT_GOLD:
                setGoldFromConsole(ctx);
                break;
            case INPUT_AUGMENT:
                applyAugmentFromConsole(ctx);
                break;
            default:
                break;
        }
    }

    private void setLivesFromConsole(Context ctx) {
        EconomyManager economy = ctx.getEconomyManager();
        if (economy == null) {
            return;
        }
        String rawValue = livesInput == null ? "" : livesInput.trim();
        if (rawValue.isEmpty()) {
            ctx.showMessage("Enter a life value first.");
            return;
        }
        try {
            int value = Integer.parseInt(rawValue);
            if (value < 0) {
                ctx.showMessage("Life cannot be negative.");
                return;
            }
            economy.setLives(value);
            ctx.clearGameOver();
            activeInput = INPUT_NONE;
            ctx.showMessage("Lives set to " + value);
        } catch (NumberFormatException ex) {
            ctx.showMessage("Life must be a number.");
        }
    }

    private void setGoldFromConsole(Context ctx) {
        EconomyManager economy = ctx.getEconomyManager();
        if (economy == null) {
            return;
        }
        String rawValue = goldInput == null ? "" : goldInput.trim();
        if (rawValue.isEmpty()) {
            ctx.showMessage("Enter a gold value first.");
            return;
        }
        try {
            int value = Integer.parseInt(rawValue);
            if (value < 0) {
                ctx.showMessage("Gold cannot be negative.");
                return;
            }
            economy.setGold(value);
            activeInput = INPUT_NONE;
            ctx.showMessage("Gold set to " + value);
        } catch (NumberFormatException ex) {
            ctx.showMessage("Gold must be a number.");
        }
    }

    private void jumpToWaveFromConsole(Context ctx) {
        WaveManager waveManager = ctx.getWaveManager();
        if (waveManager == null) {
            return;
        }

        String rawValue = waveInput == null ? "" : waveInput.trim();
        if (rawValue.isEmpty()) {
            ctx.showMessage("Enter a wave number first.");
            return;
        }

        int targetWave;
        try {
            targetWave = Integer.parseInt(rawValue);
        } catch (NumberFormatException ex) {
            ctx.showMessage("Wave must be a number.");
            return;
        }

        if (targetWave < 1) {
            ctx.showMessage("Wave must be at least 1.");
            return;
        }

        int waveCap = waveManager.getMaxWaves();
        boolean firstFiftyCleared = waveManager.getCurrentWave() > waveCap
                || (waveManager.getCurrentWave() >= waveCap && waveManager.areAllWavesComplete());
        int effectiveWave = targetWave;

        if (!firstFiftyCleared && targetWave > waveCap) {
            effectiveWave = waveCap;
        } else if (firstFiftyCleared && targetWave < waveCap) {
            effectiveWave = waveCap;
        }

        ctx.killAllEnemies();
        waveManager.removeDeadEnemies();
        waveManager.jumpToWave(effectiveWave);
        waveManager.startNextWave();
        waveInput = String.valueOf(effectiveWave);
        activeInput = INPUT_NONE;
        ctx.showMessage("Jumped to wave " + effectiveWave);
    }

    private void applyAugmentFromConsole(Context ctx) {
        String rawValue = augmentInput == null ? "" : augmentInput.trim();
        if (rawValue.isEmpty()) {
            ctx.showMessage("Enter an augment id first.");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(rawValue);
        } catch (NumberFormatException ex) {
            ctx.showMessage("Augment id must be a number.");
            return;
        }
        if (id < 0 || id > MAX_AUGMENT_ID) {
            ctx.showMessage("Augment id must be between 0 and " + MAX_AUGMENT_ID + ".");
            return;
        }
        if (ctx.isAugmentAcquired(id)) {
            ctx.showMessage("Augment already acquired.");
            return;
        }
        ctx.applyAugmentById(id);
        ctx.addAcquiredAugment(id);
        activeInput = INPUT_NONE;
    }

    private void removeLastInputChar() {
        String value = getActiveInputValue();
        if (!value.isEmpty()) {
            setActiveInputValue(value.substring(0, value.length() - 1));
        }
    }

    private String getActiveInputValue() {
        switch (activeInput) {
            case INPUT_LIVES:
                return livesInput;
            case INPUT_WAVE:
                return waveInput;
            case INPUT_GOLD:
                return goldInput;
            case INPUT_AUGMENT:
                return augmentInput;
            default:
                return "";
        }
    }

    private void setActiveInputValue(String value) {
        switch (activeInput) {
            case INPUT_LIVES:
                livesInput = value;
                break;
            case INPUT_WAVE:
                waveInput = value;
                break;
            case INPUT_GOLD:
                goldInput = value;
                break;
            case INPUT_AUGMENT:
                augmentInput = value;
                break;
            default:
                break;
        }
    }

    private String[] getButtonLabels() {
        return new String[] {
                "KILL ALL",
                "WIN NORMAL",
                "LOSE ENDLESS",
                "LOSE",
                coreShieldEnabled ? "CORE SHIELD: ON" : "CORE SHIELD: OFF",
                "ADD AUGMENT"
        };
    }

    private String[] getButtonCommands() {
        return new String[] { "killall", "winnormal", "loseendless", "lose", "coreinvuln", "addaugment" };
    }

    private Layout getLayout(int screenWidth, int screenHeight, float uiScale, int buttonCount) {
        Layout layout = new Layout(buttonCount);
        float sideMargin = 24f * uiScale;
        float topMargin = 64f * uiScale;
        float bottomMargin = 0f;
        float panelW = MathUtils.clamp(screenWidth * 0.185f, 210f * uiScale, 300f * uiScale);
        float titleInset = 16f * uiScale;
        float titleBlockH = 20f * uiScale;
        float titleToFirstLabel = 28f * uiScale;
        float inputBoxH = 22f * uiScale;
        float labelToInputGap = 9f * uiScale;
        float sectionGap = 10f * uiScale;
        float buttonGap = 8f * uiScale;
        float buttonH = 30f * uiScale;
        float extraButtonTopGap = 14f * uiScale;
        float panelH = titleInset + titleBlockH + titleToFirstLabel
                + (labelToInputGap + inputBoxH)
                + (sectionGap + labelToInputGap + inputBoxH)
                + (sectionGap + labelToInputGap + inputBoxH)
                + (buttonCount * buttonH)
                + ((buttonCount - 1) * buttonGap)
                + (labelToInputGap + titleBlockH);
        panelH = Math.min(panelH, screenHeight - topMargin - bottomMargin);
        float panelX = sideMargin;
        float panelY = screenHeight - panelH - topMargin;
        layout.panel.set(panelX, panelY, panelW, panelH);

        float innerPadding = 10f * uiScale;
        float contentX = panelX + innerPadding;
        float contentW = panelW - innerPadding * 2f;

        layout.titleY = panelY + panelH - titleInset;
        layout.livesLabelY = layout.titleY - titleToFirstLabel;
        float livesInputY = layout.livesLabelY - labelToInputGap - inputBoxH;
        layout.livesInputBox.set(contentX, livesInputY, contentW, inputBoxH);

        layout.waveLabelY = layout.livesInputBox.y - sectionGap;
        float waveBoxY = layout.waveLabelY - labelToInputGap - inputBoxH;
        layout.waveBox.set(contentX, waveBoxY, contentW, inputBoxH);

        layout.goldLabelY = layout.waveBox.y - sectionGap;
        float goldInputY = layout.goldLabelY - labelToInputGap - inputBoxH;
        layout.goldInputBox.set(contentX, goldInputY, contentW, inputBoxH);

        float buttonY = layout.goldInputBox.y - extraButtonTopGap - buttonH;
        for (Rectangle button : layout.buttons) {
            button.set(contentX, buttonY, contentW, buttonH);
            buttonY -= buttonH + buttonGap;
        }

        if (layout.buttons.length > 0) {
            Rectangle addAugmentButton = layout.buttons[layout.buttons.length - 1];
            float gap = 6f * uiScale;
            float augmentInputW = Math.max(70f * uiScale, contentW * 0.36f);
            float augmentButtonW = Math.max(90f * uiScale, contentW - augmentInputW - gap);
            addAugmentButton.width = augmentButtonW;
            layout.augmentInputBox.set(addAugmentButton.x + addAugmentButton.width + gap, addAugmentButton.y,
                    augmentInputW, addAugmentButton.height);
        }

        return layout;
    }

    private void drawInputBox(ShapeRenderer uiShapeRenderer, Rectangle box, boolean active) {
        if (active) {
            uiShapeRenderer.setColor(0.18f, 0.18f, 0.18f, 0.98f);
        } else {
            uiShapeRenderer.setColor(0.12f, 0.12f, 0.12f, 0.96f);
        }
        uiShapeRenderer.rect(box.x, box.y, box.width, box.height);
    }

    private void drawFieldLabel(SpriteBatch batch, BitmapFont uiFont, GlyphLayout glyphLayout, String label, float labelY, Rectangle inputBox) {
        glyphLayout.setText(uiFont, label, Color.WHITE, inputBox.width, com.badlogic.gdx.utils.Align.center, false);
        uiFont.draw(batch, glyphLayout, inputBox.x, labelY);
    }

    private void drawInputField(SpriteBatch batch, BitmapFont uiFontLarge, GlyphLayout glyphLayout, float uiScale,
                                Rectangle box, String value, String placeholder) {
        String displayValue = value == null ? "" : value;
        if (displayValue.isEmpty()) {
            displayValue = placeholder == null ? "" : placeholder;
        }
        uiFontLarge.getData().setScale(uiScale * 0.40f);
        glyphLayout.setText(uiFontLarge, displayValue, Color.WHITE, box.width,
                com.badlogic.gdx.utils.Align.center, false);
        float valueY = box.y + box.height * 0.5f + glyphLayout.height * 0.5f;
        uiFontLarge.draw(batch, glyphLayout, box.x, valueY);
    }

    private boolean isInRect(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }
}
