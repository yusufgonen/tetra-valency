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
import com.badlogic.gdx.math.MathUtils;
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
    private Texture bgTexture;
    private Texture logoTexture;
    private Texture githubBadgeTexture;
    private Texture libgdxBadgeTexture;
    private Texture gradleBadgeTexture;
    private Texture dreamloBadgeTexture;

    private Rectangle rootPanel;
    private Rectangle namesPanel;
    private Rectangle creditsViewport;
    private Rectangle backBtn;
    private Rectangle[] nameRows;
    private String[] names;

    private Array<CreditItem> creditItems;
    private Array<LinkEntry> linkEntries;
    private float creditsScroll;
    private float creditsContentHeight;
    private float creditsAutoScroll;
    private static final float SCROLL_STEP = 34f;
    private static final float CREDITS_TOP_PADDING = 12f;
    private static final float CREDITS_BOTTOM_PADDING = 180f;
    private static final float EXTRA_SCROLL_ALLOWANCE = 120f;
    private static final float AUTO_SCROLL_SPEED = 28f;

    private enum IconKind {
        NONE, GITHUB, LIBGDX, GRADLE, DREAMLO
    }

    private static class CreditItem {
        final String category;
        final String label;
        final String url;
        final IconKind iconKind;

        CreditItem(String category, String label, String url, IconKind iconKind) {
            this.category = category;
            this.label = label;
            this.url = url;
            this.iconKind = iconKind;
        }

        boolean hasLink() {
            return url != null && !url.isEmpty();
        }
    }

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
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(24));
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(36));
        glyph = new GlyphLayout();
        bgTexture = loadTextureSafe("ui/main_menu_bg.png");
        if (bgTexture == null) {
            bgTexture = loadTextureSafe("ui/augment_screen_bg.png");
        }
        logoTexture = loadTextureSafe("ui/cosmovision.png");
        githubBadgeTexture = loadTextureSafe("credits/github.png");
        libgdxBadgeTexture = loadTextureSafe("credits/libgdx.png");
        gradleBadgeTexture = loadTextureSafe("credits/gradle.png");
        dreamloBadgeTexture = loadTextureSafe("credits/dreamlo.png");
        creditItems = createCreditItems();
        linkEntries = new Array<>();
        recalcLayout();
        Gdx.input.setInputProcessor(new InputHandler());
    }

    private Array<CreditItem> createCreditItems() {
        Array<CreditItem> items = new Array<>();

        items.add(new CreditItem("Icons", "GitHub-sourced UI/Game Icons", "https://github.com/", IconKind.NONE));

        items.add(new CreditItem("Fonts", "Paytone One", "https://fonts.google.com/specimen/Paytone+One", IconKind.NONE));

        items.add(new CreditItem("Music", "Background Music (source link pending)", "", IconKind.NONE));

        items.add(new CreditItem("SFX", "Click sound effect by Universfield", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as UI click effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "Error sound effect by Lesiakover", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as UI click error effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "Casual Click Pop UI 3 sound effect by floraphonic", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as pause toggle effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "High Speed sound effect by Universfield", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as speed toggle effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "Game Level Complete sound effect by Universfield", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as wave complete effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "Game Start sound effect by FoxBoy Tails", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as wave start effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "Gaming victory sound effect by EAGLAXLE", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as victory effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "Marimba Lose sound effect by Universfield", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as lose effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "fire magic (6) sound effect by Yodguard", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as attack effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "Crushing shells of eggs sound effect by AudioPapkin", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as core hit effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "- Damage blowhole sound effect by Prmodrai", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as enemy hit effect", "", IconKind.NONE));
        items.add(new CreditItem("SFX", "Dramatic Death Collapse sound effect by Universfield", "https://pixabay.com/sound-effects/", IconKind.NONE));
        items.add(new CreditItem("SFX", "Used as enemy death effect", "", IconKind.NONE));
        items.add(new CreditItem("3D Models", "3D Models (source links pending)", "", IconKind.NONE));
        
        
        items.add(new CreditItem("Tools/Libraries", "GitHub", "https://github.com/", IconKind.GITHUB));
        items.add(new CreditItem("Tools/Libraries", "libGDX", "https://libgdx.com/", IconKind.LIBGDX));
        items.add(new CreditItem("Tools/Libraries", "Gradle", "https://gradle.org/", IconKind.GRADLE));
        items.add(new CreditItem("Tools/Libraries", "Dreamlo", "http://dreamlo.com/", IconKind.DREAMLO));

        return items;
    }

    private void recalcLayout() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        rootPanel = new Rectangle(w * 0.18f, h * 0.06f, w * 0.64f, h * 0.88f);
        backBtn = new Rectangle(rootPanel.x + 22f, rootPanel.y + 22f, 140f, 48f);

        float namesPanelY = rootPanel.y + rootPanel.height * 0.60f;
        float namesPanelH = rootPanel.height * 0.11f;
        namesPanel = new Rectangle(rootPanel.x + 40f, namesPanelY,
                rootPanel.width - 80f, namesPanelH);

        float viewportY = backBtn.y + backBtn.height + 16f;
        float viewportTop = namesPanel.y - 24f;
        float viewportH = Math.max(140f, viewportTop - viewportY);
        creditsViewport = new Rectangle(rootPanel.x + 40f, viewportY, rootPanel.width - 80f, viewportH);

        names = new String[] { "Umit Yusuf GONEN", "Ahmet Efe CANPOLAT", "Burhan TURK", "Onur Yusuf YILMAZ",
                "Oguzhan YILMAZ" };
        nameRows = new Rectangle[names.length];
        float pad = 16f;
        float rowW = namesPanel.width - pad * 2f;
        float rowH = 24f;
        float gap = 2f;
        float startY = namesPanel.y + namesPanel.height - rowH - 6f;
        for (int i = 0; i < names.length; i++) {
            nameRows[i] = new Rectangle(namesPanel.x + pad, startY - i * (rowH + gap), rowW, rowH);
        }

        creditsContentHeight = calculateCreditsContentHeight();
        creditsScroll = MathUtils.clamp(creditsScroll, 0f, getMaxCreditsScroll());
        creditsAutoScroll = MathUtils.clamp(creditsAutoScroll, 0f, getMaxCreditsScroll());
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

        updateAutoCreditsScroll(delta);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRect(rootPanel, new Color(0.89f, 0.67f, 0.26f, 0.94f));

        drawRect(namesPanel, new Color(0.95f, 0.79f, 0.42f, 0.42f));
        drawRect(creditsViewport, new Color(0.95f, 0.79f, 0.42f, 0.28f));

        drawRect(backBtn, new Color(0.56f, 0.43f, 0.33f, 1f));

        float sepY = namesPanel.y + namesPanel.height + 12f;
        shapes.setColor(new Color(0.72f, 0.55f, 0.30f, 1f));
        shapes.rect(rootPanel.x + 60f, sepY, rootPanel.width - 120f, 2f);

        float sep2Y = namesPanel.y - 14f;
        shapes.rect(rootPanel.x + 60f, sep2Y, rootPanel.width - 120f, 2f);

        drawCreditsScrollbar();
        shapes.end();

        batch.begin();

        if (logoTexture != null) {
            float logoSize = rootPanel.height * 0.22f;
            float logoX = rootPanel.x + (rootPanel.width - logoSize) * 0.5f;
            float logoY = rootPanel.y + rootPanel.height - logoSize - 16f;
            batch.draw(logoTexture, logoX, logoY, logoSize, logoSize);
        }

        titleFont.setColor(new Color(0.23f, 0.15f, 0.08f, 1f));
        glyph.setText(titleFont, "Credits");
        titleFont.draw(batch, "Credits", rootPanel.x + (rootPanel.width - glyph.width) * 0.5f,
            rootPanel.y + rootPanel.height - 24f);

        font.setColor(new Color(0.40f, 0.28f, 0.14f, 1f));
        glyph.setText(font, "Team Members");
        font.draw(batch, "Team Members", rootPanel.x + (rootPanel.width - glyph.width) * 0.5f,
            sepY + glyph.height + 8f);

        font.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
        for (int i = 0; i < names.length; i++) {
            drawCentered(names[i], nameRows[i].x, nameRows[i].y + nameRows[i].height * 0.68f, nameRows[i].width);
        }

        font.setColor(new Color(0.40f, 0.28f, 0.14f, 1f));
        glyph.setText(font, "Used Assets");

        float usedAssetsY = sep2Y - 8f;
        font.draw(batch, "Used Assets", rootPanel.x + (rootPanel.width - glyph.width) * 0.5f,
                usedAssetsY);

        font.setColor(new Color(0.52f, 0.38f, 0.22f, 1f));
        renderAssetCreditsList();

        drawCentered("Back", backBtn.x, backBtn.y + backBtn.height * 0.67f, backBtn.width);
        batch.end();
    }

    private void drawRect(Rectangle r, Color c) {
        shapes.setColor(c);
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    private void drawCentered(String text, float x, float baselineY, float width) {
        glyph.setText(font, text);
        font.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
        font.draw(batch, text, x + (width - glyph.width) * 0.5f, baselineY);
    }

    private void renderAssetCreditsList() {
        if (creditItems == null)
            return;

        linkEntries.clear();

        float x = creditsViewport.x + 12f;
        float y = creditsViewport.y + creditsViewport.height - CREDITS_TOP_PADDING - creditsAutoScroll;
        float iconSize = 18f;
        float categoryGap = 22f;
        float labelGap = 20f;
        float linkGap = 18f;
        float itemGap = 10f;
        String currentCategory = "";

        for (CreditItem item : creditItems) {
            if (!item.category.equals(currentCategory)) {
                currentCategory = item.category;
                if (isVisibleY(y)) {
                    font.setColor(new Color(0.35f, 0.24f, 0.12f, 1f));
                    glyph.setText(font, currentCategory);
                    font.draw(batch, currentCategory, x, y);
                }
                y -= categoryGap;
            }

            float rowY = y;
            if (isVisibleY(rowY)) {
                drawProviderIcon(item.iconKind, x, rowY - iconSize + 4f, iconSize);
            }

            float textX = x + (item.iconKind == IconKind.NONE ? 0f : 26f);
            if (isVisibleY(rowY)) {
                font.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
                font.draw(batch, item.label, textX, rowY);
            }

            if (item.hasLink()) {
                float linkY = rowY - 16f;
                glyph.setText(font, item.url);
                if (isVisibleY(linkY)) {
                    font.setColor(new Color(0.10f, 0.24f, 0.55f, 1f));
                    font.draw(batch, item.url, textX, linkY);
                    linkEntries.add(new LinkEntry(new Rectangle(textX, linkY - glyph.height, glyph.width, glyph.height + 4f), item.url));
                }
                y -= linkGap;
            }

            y -= labelGap + itemGap;
        }
    }

    private float calculateCreditsContentHeight() {
        if (creditItems == null || creditItems.size == 0) {
            return 0f;
        }

        float categoryGap = 22f;
        float labelGap = 20f;
        float linkGap = 18f;
        float itemGap = 10f;
        float total = 0f;
        String currentCategory = "";

        for (CreditItem item : creditItems) {
            if (!item.category.equals(currentCategory)) {
                currentCategory = item.category;
                total += categoryGap;
            }
            total += labelGap + itemGap;
            if (item.hasLink()) {
                total += linkGap;
            }
        }

        return total + CREDITS_TOP_PADDING + CREDITS_BOTTOM_PADDING;
    }

    private float getMaxCreditsScroll() {
        return Math.max(0f, creditsContentHeight - creditsViewport.height + EXTRA_SCROLL_ALLOWANCE);
    }

    private void updateAutoCreditsScroll(float delta) {
        float maxScroll = getMaxCreditsScroll();
        if (maxScroll <= 0f) {
            creditsAutoScroll = 0f;
            return;
        }

        creditsAutoScroll += delta * AUTO_SCROLL_SPEED;
        if (creditsAutoScroll > maxScroll) {
            creditsAutoScroll = 0f;
        }
    }

    private boolean isVisibleY(float baselineY) {
        return baselineY >= creditsViewport.y + 4f && baselineY <= creditsViewport.y + creditsViewport.height - 2f;
    }

    private void drawCreditsScrollbar() {
        if (creditsViewport == null || creditsContentHeight <= creditsViewport.height + 2f) {
            return;
        }

        float trackW = 6f;
        float trackX = creditsViewport.x + creditsViewport.width - 10f;
        float trackY = creditsViewport.y + 6f;
        float trackH = creditsViewport.height - 12f;

        shapes.setColor(new Color(0.46f, 0.32f, 0.18f, 0.35f));
        shapes.rect(trackX, trackY, trackW, trackH);

        float thumbRatio = MathUtils.clamp(creditsViewport.height / creditsContentHeight, 0.15f, 1f);
        float thumbH = trackH * thumbRatio;
        float maxThumbTravel = trackH - thumbH;
        float scrollRatio = getMaxCreditsScroll() <= 0f ? 0f : creditsScroll / getMaxCreditsScroll();
        float thumbY = trackY + maxThumbTravel * (1f - scrollRatio);

        shapes.setColor(new Color(0.34f, 0.22f, 0.12f, 0.82f));
        shapes.rect(trackX, thumbY, trackW, thumbH);
    }

    private void drawProviderIcon(IconKind kind, float x, float y, float size) {
        Texture tex = null;
        switch (kind) {
            case GITHUB:
                tex = githubBadgeTexture;
                break;
            case LIBGDX:
                tex = libgdxBadgeTexture;
                break;
            case GRADLE:
                tex = gradleBadgeTexture;
                break;
            case DREAMLO:
                tex = dreamloBadgeTexture;
                break;
            default:
                break;
        }

        if (tex != null) {
            batch.setColor(Color.WHITE);
            batch.draw(tex, x, y, size, size);
            return;
        }

        if (kind == IconKind.GITHUB) {
            font.setColor(new Color(0.16f, 0.16f, 0.16f, 1f));
            font.draw(batch, "GH", x, y + size - 2f);
        }
    }

    @Override
    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        if (font != null)
            font.dispose();
        if (titleFont != null)
            titleFont.dispose();
        font = createFont("fonts/font_game_screen.ttf", scaledFontSize(22));
        titleFont = createFont("fonts/font_game_screen.ttf", scaledFontSize(46));
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
        if (githubBadgeTexture != null)
            githubBadgeTexture.dispose();
        if (libgdxBadgeTexture != null)
            libgdxBadgeTexture.dispose();
        if (gradleBadgeTexture != null)
            gradleBadgeTexture.dispose();
        if (dreamloBadgeTexture != null)
            dreamloBadgeTexture.dispose();
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

        @Override
        public boolean scrolled(float amountX, float amountY) {
            float maxScroll = getMaxCreditsScroll();
            if (maxScroll <= 0f) {
                return false;
            }

            creditsAutoScroll = MathUtils.clamp(creditsAutoScroll + amountY * SCROLL_STEP, 0f, maxScroll);
            return true;
        }
    }
}
