package com.td.game.screens;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.files.FileHandle;
import com.td.game.TowerDefenseGame;
import com.td.game.elements.Element;
import com.td.game.inventory.Inventory;
import com.td.game.map.GameMap;
import com.td.game.pillars.Pillar;
import com.td.game.pillars.PillarType;
import com.td.game.player.Player;
import com.td.game.systems.EconomyManager;
import com.td.game.systems.WaveManager;
import com.td.game.entities.Projectile;
import com.td.game.entities.Effect;
import com.td.game.entities.Enemy;
import com.td.game.entities.DemonEnemy;
import com.td.game.entities.GolemEnemy;
import com.td.game.entities.BatEnemy;
import com.td.game.entities.PinkBlobEnemy;
import com.td.game.ui.ContextualMenuPanel;
import com.td.game.ui.GameShop;
import com.td.game.ui.MergeBoard;
import com.td.game.ui.WizardStaffUI;
import com.td.game.utils.Constants;
import com.td.game.utils.ModelFactory;

public class GameScreen implements Screen {

    private TowerDefenseGame game;
    private final GameMap.MapType mapType;

    private ModelBatch modelBatch;
    private PerspectiveCamera camera;
    private Environment environment;

    private SpriteBatch uiBatch;
    private BitmapFont uiFont;
    private BitmapFont uiFontLarge;
    private ShapeRenderer uiShapeRenderer;

    private GameMap gameMap;
    private Player player;
    private Array<Pillar> pillars;

    private EconomyManager economyManager;
    private WaveManager waveManager;
    private Array<com.badlogic.gdx.math.Vector3> pathWaypoints;
    private ModelFactory modelFactory;
    private int currentWave = 0;
    private final int maxWaves = 50;

    private GameShop shop;
    private Inventory inventory;
    private MergeBoard mergeBoard;
    private WizardStaffUI staffUI;

    private Model playerModel;
    private Model[] orbModels;

    private ModelInstance validHighlight;

    private Texture mapAreaBackgroundTexture;
    private Texture hudLifeIconTexture;
    private Texture hudGoldIconTexture;
    private Texture hudInfoIconTexture;
    private Texture pauseMenuBackgroundTexture;
    private Model gateModel;
    private Model coreSphereModel;
    private ModelInstance gateInstance;
    private ModelInstance coreSphereInstance;
    private float coreFlashTimer;
    private float coreBobTimer;
    private Model demonModel;
    private Model golemModel;
    private Model batModel;
    private Model pinkBlobModel;
    private float coreBaseX;
    private float coreBaseY;
    private float coreBaseZ;
    private float coreScale = 1f;
    private Texture staffInfoPanelTexture;
    private Texture mergeInfoPanelTexture;
    private Texture inventoryInfoPanelTexture;
    private EnumMap<Element, Texture> orbIconTextures;
    private GlyphLayout glyphLayout;
    private boolean staffInfoOpen;
    private boolean mergeInfoOpen;
    private boolean inventoryInfoOpen;
    private float staffInfoBtnX;
    private float staffInfoBtnY;
    private float staffInfoBtnSize;
    private float mergeInfoBtnX;
    private float mergeInfoBtnY;
    private float mergeInfoBtnSize;
    private float inventoryInfoBtnX;
    private float inventoryInfoBtnY;
    private float inventoryInfoBtnSize;
    private float infoPanelX;
    private float infoPanelY;
    private float infoPanelW;
    private float infoPanelH;
    private boolean paused;
    private int speedIndex;
    private static final float[] SPEED_MULTIPLIERS = { 1f, 2f, 8f, 32f };
    private boolean autoplayEnabled;
    private boolean consoleOpen;
    private float pauseIconX;
    private float pauseIconY;
    private float pauseIconSize;
    private float speedIconX;
    private float speedIconY;
    private float speedIconSize;
    private float autoplayBtnX;
    private float autoplayBtnY;
    private float autoplayBtnW;
    private float autoplayBtnH;

    private Element hoveredTooltipElement;
    private String tooltipContext;
    private Texture elementInfoPanelTexture;
    private HashMap<String, String[]> elementDescriptions;

    private static class AcquiredAugment {
        int id;

        AcquiredAugment(int id) {
            this.id = id;
        }
    }

    private float globalTimer;
    private Array<AcquiredAugment> acquiredAugments;
    private boolean showingAugmentsPanel;
    private float playBtnX, playBtnY, playBtnW, playBtnH;
    private float seeAugmentsBtnX, seeAugmentsBtnY, seeAugmentsBtnW, seeAugmentsBtnH;

    private Array<com.td.game.entities.Projectile> activeProjectiles = new Array<>();
    private Array<com.td.game.entities.Effect> activeEffects = new Array<>();
    private HashMap<Element, Texture> effectTextures = new HashMap<>();

    private boolean gameOver;
    private boolean gameWon;
    private boolean augmentChoiceActive;
    private int augmentOptionA;
    private int augmentOptionB;

    private Pillar hoveredPillar;
    private Pillar selectedPillar;
    private com.td.game.entities.Enemy hoveredEnemy;
    private Vector3 selectedTilePos;
    private ContextualMenuPanel buildMenu;
    private boolean awaitingPillarOrbSelection;
    private float pillarPanelX;
    private float pillarPanelY;
    private float pillarPanelW;
    private float pillarPanelH;
    private static final float PILLAR_PANEL_BTN_H = 30f;

    private float moveDelay = 0.2f;
    private float moveTimer = 0;
    private int playerGridX, playerGridZ;

    private String uiMessage = "";
    private float uiMessageTimer = 0f;

    private float shopWidth;
    private float uiScale;

    private float globalDamageMult = 1f;
    private float globalRangeMult = 1f;
    private float globalAttackSpeedMult = 1f;
    private float staffAuraRadius = 8f;
    private static final int MERGE_COST = 20;
    private static final float INFO_PANEL_SHIFT_DOWN = 100f;
    private static final float GATE_MODEL_SCALE_MULTIPLIER = 2.0f;
    private static final String[] CONSOLE_BUTTON_COMMANDS = { "killall", "life", "win" };
    private static final String[] CONSOLE_BUTTON_LABELS = { "KILL ALL", "LIFE", "WIN" };

    public GameScreen(TowerDefenseGame game) {
        this(game, GameMap.MapType.ELEMENTAL_CASTLE, false);
    }

    public GameScreen(TowerDefenseGame game, GameMap.MapType mapType) {
        this(game, mapType, false);
    }

    private boolean loadFromSave;

    public GameScreen(TowerDefenseGame game, GameMap.MapType mapType, boolean loadFromSave) {
        this.game = game;
        this.mapType = mapType == null ? GameMap.MapType.ELEMENTAL_CASTLE : mapType;
        this.loadFromSave = loadFromSave;
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "Show method started");
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        uiScale = Math.min(screenWidth / 1920f, screenHeight / 1080f);
        shopWidth = calculateShopWidth(screenWidth);

        float mapAreaWidth = screenWidth - shopWidth;

        camera = new PerspectiveCamera(50, mapAreaWidth, screenHeight);
        camera.near = 0.1f;
        camera.far = 500f;

        float mapCenterX = Constants.MAP_WIDTH * Constants.TILE_SIZE / 2;
        float mapCenterZ = Constants.MAP_HEIGHT * Constants.TILE_SIZE / 2;

        float camHeight = 45f;
        float camDist = 38f;
        camera.position.set(mapCenterX, camHeight, mapCenterZ + camDist);
        camera.lookAt(mapCenterX, 0, mapCenterZ);
        camera.up.set(Vector3.Y);
        camera.update();

        modelBatch = game.modelBatch;

        uiBatch = new SpriteBatch();
        uiFont = createGameScreenFont(24);
        uiFont.getData().setScale(uiScale * 0.54f);
        uiFontLarge = createGameScreenFont(42);
        uiFontLarge.getData().setScale(uiScale * 0.72f);
        uiShapeRenderer = new ShapeRenderer();
        glyphLayout = new GlyphLayout();
        orbIconTextures = new EnumMap<>(Element.class);
        String mapBgPath = mapType == GameMap.MapType.DESERT_OASIS ? "ui/map_2_bg.png" : "ui/map_bg.png";
        mapAreaBackgroundTexture = new Texture(resolveAsset(mapBgPath));
        hudLifeIconTexture = new Texture(resolveAsset("ui/hud_life_icon.png"));
        hudGoldIconTexture = new Texture(resolveAsset("ui/hud_currency_gold.png"));
        hudInfoIconTexture = new Texture(resolveAsset("ui/hud_info_icon.png"));
        pauseMenuBackgroundTexture = new Texture(resolveAsset("ui/main_menu_bg.png"));
        elementInfoPanelTexture = loadFirstExistingTexture(
                new String[] { "ui/element_info_panel.png", "assets/ui/element_info_panel.png" });
        loadElementDescriptions();
        staffInfoPanelTexture = new Texture(resolveAsset("ui/hud_info_staff_placeholder.png"));
        mergeInfoPanelTexture = new Texture(resolveAsset("ui/hud_info_merge_placeholder.png"));
        inventoryInfoPanelTexture = new Texture(resolveAsset("ui/hud_info_inventory_placeholder.png"));
        loadOrbIconTextures();

        acquiredAugments = new Array<>();
        showingAugmentsPanel = false;
        globalTimer = 0f;

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
        environment.add(new DirectionalLight().set(0.9f, 0.9f, 0.85f, -1f, -0.8f, -0.2f));
        environment.add(new DirectionalLight().set(0.3f, 0.3f, 0.4f, 1f, -0.5f, 0.5f));

        modelFactory = new ModelFactory();
        createModels();

        try {
            Gdx.app.log("GameScreen", "Loading textures...");
            Gdx.app.log("GameScreen", "Textures loaded. Creating GameMap...");
            gameMap = new GameMap(modelFactory, mapType);
            Gdx.app.log("GameScreen", "GameMap created.");

            int[][] waypoints = gameMap.getWaypointsForMap();
            gateModel = modelFactory.createGateModel();
            gateInstance = new ModelInstance(gateModel);
            int sx = waypoints[0][0];
            int sz = waypoints[0][1];

            com.badlogic.gdx.math.collision.BoundingBox gateBounds = new com.badlogic.gdx.math.collision.BoundingBox();
            gateInstance.calculateBoundingBox(gateBounds);
            float gWidth = gateBounds.getWidth();
            float gDepth = gateBounds.getDepth();
            float gMax = Math.max(gWidth, gDepth);
            float gScale = (gMax > 0) ? (Constants.TILE_SIZE * 0.95f) / gMax : 1f;
            gScale *= GATE_MODEL_SCALE_MULTIPLIER;

            float firstTileLeftEdgeX = (sx * Constants.TILE_SIZE) - (Constants.TILE_SIZE * 0.5f);
            float gateHalfWidthX = (gWidth * gScale) * 0.5f;
            float gateCenterX = firstTileLeftEdgeX - gateHalfWidthX;
            float gateCenterZ = sz * Constants.TILE_SIZE;
            gateInstance.transform.setToTranslation(
                    gateCenterX, 0f, gateCenterZ)
                    .scl(gScale);

            coreSphereModel = modelFactory.createCoreSphereModel(new Color(0.6f, 0.2f, 0.85f, 1f));
            coreSphereInstance = new ModelInstance(coreSphereModel);
            int ex = waypoints[waypoints.length - 1][0];
            int ez = waypoints[waypoints.length - 1][1];

            com.badlogic.gdx.math.collision.BoundingBox coreBounds = new com.badlogic.gdx.math.collision.BoundingBox();
            coreSphereInstance.calculateBoundingBox(coreBounds);
            float cWidth = coreBounds.getWidth();
            float cDepth = coreBounds.getDepth();
            float cMax = Math.max(cWidth, cDepth);
            float cScale = (cMax > 0) ? (Constants.TILE_SIZE * 0.95f) / cMax : 1f;

            coreSphereInstance.transform.setToTranslation(
                    ex * Constants.TILE_SIZE, 1.0f, ez * Constants.TILE_SIZE).scl(cScale);

            coreBaseX = ex * Constants.TILE_SIZE;
            coreBaseY = 1.0f;
            coreBaseZ = ez * Constants.TILE_SIZE;
            coreScale = cScale;
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading assets or creating map", e);
            throw new RuntimeException(e);
        }

        playerGridX = 8;
        playerGridZ = 6;
        player = new Player(new Vector3(playerGridX * Constants.TILE_SIZE, 0, playerGridZ * Constants.TILE_SIZE));

        ModelInstance playerInstance = new ModelInstance(playerModel);
        com.badlogic.gdx.math.collision.BoundingBox bounds = new com.badlogic.gdx.math.collision.BoundingBox();
        playerInstance.calculateBoundingBox(bounds);
        float width = bounds.getWidth();
        float depth = bounds.getDepth();
        float height = bounds.getHeight();

        float maxDim = Math.max(width, Math.max(depth, height));
        float targetSize = Constants.TILE_SIZE * 0.95f;
        float scale = maxDim > 0 ? targetSize / maxDim : 1f;

        player.setModel(playerInstance, scale);

        pillars = new Array<>();

        economyManager = new EconomyManager();
        economyManager.setGold(Constants.STARTING_GOLD);

        pathWaypoints = new Array<>();
        for (int[] wp : gameMap.getWaypointsForMap()) {
            pathWaypoints.add(new Vector3(wp[0] * Constants.TILE_SIZE, 0, wp[1] * Constants.TILE_SIZE));
        }
        waveManager = new WaveManager(pathWaypoints, modelFactory);

        buildMenu = new ContextualMenuPanel();
        buildMenu.setFont(uiFont, uiBatch);

        float panelPadding = 10f * uiScale;
        float sectionW = shopWidth - panelPadding * 2f;

        shop = new GameShop(screenWidth - shopWidth, 0, shopWidth, screenHeight);
        inventory = new Inventory(screenWidth - shopWidth + panelPadding, 0f);
        inventory.resize(screenWidth, screenHeight);

        float invSlot = inventory.getSlotSize();
        float invPad = inventory.getPadding();
        float orbGap = 5f * uiScale;
        float orbSlot = (sectionW - orbGap * 3f) / 4f;
        float orbY = screenHeight - 145f * uiScale;

        float staffW = Math.min(sectionW, 170f * uiScale);
        float staffX = screenWidth - shopWidth + (shopWidth - staffW) * 0.5f;
        float staffY = orbY - 64f * uiScale - staffW;
        float mergeInfoY = staffY - 14f * uiScale;
        float mergeY = mergeInfoY - 52f * uiScale - invSlot;
        float invY = mergeY - 54f * uiScale - (invSlot * 2f + invPad);
        if (invY < 18f * uiScale) {
            invY = 18f * uiScale;
        }
        float invX = screenWidth - shopWidth + panelPadding;
        inventory.setPosition(invX, invY);

        float mergeX = screenWidth - shopWidth + panelPadding;
        float mergeW = shopWidth - (panelPadding * 2f);
        float mergeH = 110f * uiScale;
        mergeBoard = new MergeBoard(mergeX, mergeY, mergeW, mergeH);
        mergeBoard.setSlotSize(invSlot);

        staffUI = new WizardStaffUI((int) staffX, (int) staffY, (int) staffW, (int) staffW);

        Gdx.input.setInputProcessor(new GameInputProcessor());

        selectedTilePos = null;
        hoveredPillar = null;
        selectedPillar = null;
        awaitingPillarOrbSelection = false;
        pillarPanelX = 0f;
        pillarPanelY = 0f;
        pillarPanelW = 220f * uiScale;
        pillarPanelH = 170f * uiScale;
        gameOver = false;
        gameWon = false;
        augmentChoiceActive = false;
        augmentOptionA = -1;
        augmentOptionB = -1;
        staffInfoOpen = false;
        mergeInfoOpen = false;
        inventoryInfoOpen = false;
        paused = false;
        speedIndex = 0;
        autoplayEnabled = false;
        consoleOpen = false;


        game.audio.playMapMusic(mapType);
        loadEffectTextures();

        if (this.loadFromSave) {
            loadGame();
        }
    }

    private void createModels() {
        playerModel = modelFactory.createPlayerModel();

        orbModels = new Model[10];
        Element[] elements = Element.values();
        for (int i = 0; i < elements.length && i < 10; i++) {
            Element e = elements[i];
            orbModels[i] = modelFactory.createOrbModel(new Color(e.getR(), e.getG(), e.getB(), 1f));
        }

        Model valid = modelFactory.createHighlightModel(Color.YELLOW);
        validHighlight = new ModelInstance(valid);

        demonModel = modelFactory.loadDemonModel();
        golemModel = modelFactory.loadGolemModel();
        batModel = modelFactory.loadBatModel();
        pinkBlobModel = modelFactory.loadPinkBlobModel();
    }

    public void showMessage(String msg) {
        this.uiMessage = msg;
        this.uiMessageTimer = 2.0f;
    }

    public void toggleConsole() {
        consoleOpen = !consoleOpen;
    }


    public void killAllEnemies() {
        if (waveManager != null) {
            waveManager.killAllEnemies();
        }
    }

    public void skipToNextWave() {
        if (waveManager != null) {
            waveManager.killAllEnemies();
            if (!waveManager.isWaveInProgress() && !waveManager.areAllWavesComplete()) {
                waveManager.startNextWave();
            }
        }
    }

    @Override
    public void render(float delta) {
        if (mapType == GameMap.MapType.DESERT_OASIS) {
            Gdx.gl.glClearColor(0.74f, 0.49f, 0.2f, 1f);
        } else {
            Gdx.gl.glClearColor(0.15f, 0.35f, 0.12f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (!gameOver && !gameWon) {
            globalTimer += delta;
        }

        if (!gameOver && !gameWon && !paused) {
            float simDelta = delta * SPEED_MULTIPLIERS[speedIndex];
            update(simDelta);
        }

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        int mapAreaWidth = (int) (screenWidth - shopWidth);

        com.badlogic.gdx.graphics.glutils.HdpiUtils.glViewport(0, 0, mapAreaWidth, screenHeight);
        uiBatch.getProjectionMatrix().setToOrtho2D(0, 0, mapAreaWidth, screenHeight);
        uiBatch.begin();
        uiBatch.draw(mapAreaBackgroundTexture, 0, 0, mapAreaWidth, screenHeight);
        uiBatch.end();

        com.badlogic.gdx.graphics.glutils.HdpiUtils.glViewport(0, 0, mapAreaWidth, screenHeight);
        modelBatch.begin(camera);
        gameMap.render(modelBatch, environment);

        if (buildMenu.isActive() && selectedTilePos != null) {
            ModelInstance highlight = validHighlight;
            highlight.transform.setToTranslation(selectedTilePos.x, 0.05f, selectedTilePos.z);
            modelBatch.render(highlight, environment);
        }

        for (Pillar pillar : pillars)
            pillar.render(modelBatch, environment);

        for (com.td.game.entities.Projectile proj : activeProjectiles) {
            proj.render(modelBatch, environment);
        }

        // Render enemies
        for (com.td.game.entities.Enemy enemy : waveManager.getActiveEnemies()) {
            enemy.render(modelBatch, environment);
        }

        player.render(modelBatch, environment);

        if (gateInstance != null)
            modelBatch.render(gateInstance, environment);
        if (coreSphereInstance != null) {

            if (coreFlashTimer > 0) {
                float t = Math.min(coreFlashTimer / 0.4f, 1f);
                Color flash = new Color(0.6f + 0.4f * t, 0.2f * (1f - t), 0.85f * (1f - t), 1f);
                coreSphereInstance.materials.get(0).set(ColorAttribute.createDiffuse(flash));
                coreFlashTimer -= Gdx.graphics.getDeltaTime();
                if (coreFlashTimer <= 0) {
                    coreSphereInstance.materials.get(0).set(
                            ColorAttribute.createDiffuse(new Color(0.6f, 0.2f, 0.85f, 1f)));
                }
            }
            modelBatch.render(coreSphereInstance, environment);
        }

        modelBatch.end();

        uiShapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);

        com.badlogic.gdx.graphics.glutils.HdpiUtils.glViewport(0, 0, screenWidth, screenHeight);
        uiBatch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);

        if (hoveredPillar != null) {
            RangeOverlayRenderer.drawPillarRange(uiShapeRenderer, camera, hoveredPillar, mapAreaWidth, screenHeight);
        }

        if (hoveringAlchemist) {
            RangeOverlayRenderer.drawAuraRange(uiShapeRenderer, camera, player, pillars, staffAuraRadius, uiScale,
                    mapAreaWidth, screenHeight, staffUI.getEquippedElement());
        }

        uiBatch.begin();
        for (com.td.game.entities.Effect effect : activeEffects) {
            effect.render(uiBatch, camera);
        }
        uiBatch.end();

        renderMinimalHud(screenWidth, screenHeight, mapAreaWidth);
        renderMessages(screenWidth, screenHeight);

        if (selectedPillar != null) {
            updatePillarPanelPosition(screenWidth, screenHeight, mapAreaWidth);
            renderPillarActionPanel(screenWidth, screenHeight);
        } else if (hoveredPillar != null) {
            renderPillarStats(screenWidth, screenHeight);
        }

        if (augmentChoiceActive) {
            renderAugmentSelection(screenWidth, screenHeight);
        } else if (showingAugmentsPanel) {
            renderAcquiredAugments(screenWidth, screenHeight);
        }

        buildMenu.render(uiBatch);

        updateHoveredElement(screenWidth, screenHeight);
        if (hoveredTooltipElement != null) {
            renderElementTooltip(screenWidth, screenHeight);
        }

        updateHoveredEnemy(screenWidth, screenHeight, mapAreaWidth);
        if (hoveredEnemy != null) {
            renderEnemyHoverUI(screenWidth, screenHeight);
        }

        if (gameOver)
            renderGameOver(screenWidth, screenHeight);
        else if (gameWon)
            renderGameWon(screenWidth, screenHeight);
        else if (paused)
            renderPauseMenu(screenWidth, screenHeight);

        renderMessages(screenWidth, screenHeight);
        renderConsoleOverlay(screenWidth, screenHeight);

    }

    private void renderConsoleOverlay(int screenWidth, int screenHeight) {
        if (!consoleOpen) {
            return;
        }

        float consoleW = screenWidth * 0.25f;
        float consoleH = screenHeight * 0.25f;
        float consoleX = 0f;
        float consoleY = screenHeight - consoleH;
        float padding = 8f * uiScale;
        float buttonW = consoleW * 0.40f;
        float buttonH = Math.max(30f * uiScale, consoleH * 0.16f);
        float buttonGap = 8f * uiScale;
        float firstButtonY = consoleY + consoleH - padding - buttonH;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0f, 0f, 0f, 0.92f);
        uiShapeRenderer.rect(consoleX, consoleY, consoleW, consoleH);
        uiShapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.95f);
        for (int i = 0; i < CONSOLE_BUTTON_LABELS.length; i++) {
            float buttonX = consoleX + padding;
            float buttonY = firstButtonY - i * (buttonH + buttonGap);
            uiShapeRenderer.rect(buttonX, buttonY, buttonW, buttonH);
        }
        uiShapeRenderer.end();

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        uiShapeRenderer.setColor(Color.WHITE);
        uiShapeRenderer.rect(consoleX, consoleY, consoleW, consoleH);
        for (int i = 0; i < CONSOLE_BUTTON_LABELS.length; i++) {
            float buttonX = consoleX + padding;
            float buttonY = firstButtonY - i * (buttonH + buttonGap);
            uiShapeRenderer.rect(buttonX, buttonY, buttonW, buttonH);
        }
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFont.setColor(Color.WHITE);
        uiFont.getData().setScale(uiScale * 0.54f);
        for (int i = 0; i < CONSOLE_BUTTON_LABELS.length; i++) {
            float buttonX = consoleX + padding;
            float buttonY = firstButtonY - i * (buttonH + buttonGap);
            glyphLayout.setText(uiFont, CONSOLE_BUTTON_LABELS[i], Color.WHITE, buttonW,
                    com.badlogic.gdx.utils.Align.center, false);
            float textX = buttonX;
            float textY = buttonY + buttonH * 0.5f + glyphLayout.height * 0.5f;
            uiFont.draw(uiBatch, glyphLayout, textX, textY);
        }
        uiBatch.end();
    }

    private void handleConsoleCommand(String rawCommand) {
        if (rawCommand == null) {
            return;
        }
        String command = rawCommand.trim();
        if (command.isEmpty()) {
            return;
        }
        command = command.toLowerCase(Locale.ROOT);

        switch (command) {
            case "help":
                break;
            case "killall":
                killAllEnemies();
                break;
            case "life":
                if (economyManager != null) {
                    economyManager.setLives(20);
                }
                gameOver = false;
                break;
            case "win":
                int winWave = waveManager != null ? waveManager.getCurrentWave() : 0;
                float winTime = globalTimer;
                game.setScreen(new EndgameScreen(game, EndgameScreen.EndState.WIN, mapType, winWave, winTime));
                dispose();
                break;
            default:
                break;
        }
    }

    private int getConsoleButtonIndexAt(int screenX, int flippedY, int screenWidth, int screenHeight) {
        if (!consoleOpen) {
            return -1;
        }
        float consoleW = screenWidth * 0.25f;
        float consoleH = screenHeight * 0.25f;
        float consoleX = 0f;
        float consoleY = screenHeight - consoleH;
        float padding = 8f * uiScale;
        float buttonW = consoleW * 0.40f;
        float buttonH = Math.max(30f * uiScale, consoleH * 0.16f);
        float buttonGap = 8f * uiScale;
        float firstButtonY = consoleY + consoleH - padding - buttonH;

        for (int i = 0; i < CONSOLE_BUTTON_LABELS.length; i++) {
            float buttonX = consoleX + padding;
            float buttonY = firstButtonY - i * (buttonH + buttonGap);
            if (isInRect(screenX, flippedY, buttonX, buttonY, buttonW, buttonH)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isInConsoleArea(int screenX, int flippedY, int screenWidth, int screenHeight) {
        if (!consoleOpen) {
            return false;
        }
        float consoleW = screenWidth * 0.25f;
        float consoleH = screenHeight * 0.25f;
        float consoleX = 0f;
        float consoleY = screenHeight - consoleH;
        return isInRect(screenX, flippedY, consoleX, consoleY, consoleW, consoleH);
    }


    private void loadElementDescriptions() {
        elementDescriptions = new HashMap<>();
        try {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal("data/elements.json");
            if (!file.exists())
                file = Gdx.files.internal("assets/data/elements.json");
            if (file.exists()) {
                JsonValue root = new com.badlogic.gdx.utils.JsonReader().parse(file);
                for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                    String key = entry.name;
                    String pillar = entry.getString("pillar", "");
                    String staff = entry.getString("staff", "");
                    String tier = entry.getString("tier", "");
                    String recipe = entry.getString("recipe", "");
                    elementDescriptions.put(key, new String[] { pillar, staff, tier, recipe });
                }
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Could not load element descriptions", e);
        }
    }

    private void updateHoveredElement(int screenWidth, int screenHeight) {
        hoveredTooltipElement = null;
        tooltipContext = null;

        int mx = Gdx.input.getX();
        int my = screenHeight - Gdx.input.getY();

        int shopIdx = shop.getOrbIndexAt(mx, my);
        if (shopIdx >= 0) {
            Element e = shop.getOrbElement(shopIdx);
            if (e != null) {
                hoveredTooltipElement = e;
                tooltipContext = "pillar";
                return;
            }
        }

        int invSlot = inventory.getSlotAt(mx, my);
        if (invSlot >= 0) {
            Element e = inventory.getOrbAt(invSlot);
            if (e != null) {
                hoveredTooltipElement = e;
                tooltipContext = "pillar";
                return;
            }
        }

        float ss = mergeBoard.getSlotSize();
        float half = ss / 2f;
        if (mergeBoard.hasSlot1() && mx >= mergeBoard.getSlot1X() - half && mx <= mergeBoard.getSlot1X() + half
                && my >= mergeBoard.getSlot1Y() - half && my <= mergeBoard.getSlot1Y() + half) {
            hoveredTooltipElement = mergeBoard.getSlot1Element();
            tooltipContext = "pillar";
            return;
        }
        if (mergeBoard.hasSlot2() && mx >= mergeBoard.getSlot2X() - half && mx <= mergeBoard.getSlot2X() + half
                && my >= mergeBoard.getSlot2Y() - half && my <= mergeBoard.getSlot2Y() + half) {
            hoveredTooltipElement = mergeBoard.getSlot2Element();
            tooltipContext = "pillar";
            return;
        }
        if (mergeBoard.hasResult() && mx >= mergeBoard.getResultX() - half && mx <= mergeBoard.getResultX() + half
                && my >= mergeBoard.getResultY() - half && my <= mergeBoard.getResultY() + half) {
            hoveredTooltipElement = mergeBoard.getResultElement();
            tooltipContext = "pillar";
            return;
        }

        if (staffUI.hasOrb() && staffUI.contains(Gdx.input.getX(), Gdx.input.getY())) {
            hoveredTooltipElement = staffUI.getEquippedElement();
            tooltipContext = "staff";
        }
    }

    private void renderElementTooltip(int screenWidth, int screenHeight) {
        if (elementDescriptions == null)
            return;
        String key = hoveredTooltipElement.name();
        String[] desc = elementDescriptions.get(key);
        if (desc == null)
            return;

        String pillarDesc = desc[0];
        String staffDesc = desc[1];
        String tier = desc[2];
        String recipe = desc[3];

        float panelW = 340f * uiScale;
        float panelH = 280f * uiScale;
        float mx = Gdx.input.getX();
        float my = screenHeight - Gdx.input.getY();

        float px = mx + 16f * uiScale;
        float py = my + 16f * uiScale;
        if (px + panelW > screenWidth)
            px = mx - panelW - 8f * uiScale;
        if (py + panelH > screenHeight)
            py = my - panelH - 8f * uiScale;
        if (px < 0)
            px = 4f;
        if (py < 0)
            py = 4f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (elementInfoPanelTexture != null) {
            uiBatch.begin();
            uiBatch.draw(elementInfoPanelTexture, px, py, panelW, panelH);
            uiBatch.end();
        } else {
            uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            uiShapeRenderer.setColor(0.08f, 0.06f, 0.12f, 0.92f);
            uiShapeRenderer.rect(px, py, panelW, panelH);
            uiShapeRenderer.end();
        }

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Color elemColor = new Color(hoveredTooltipElement.getR(), hoveredTooltipElement.getG(),
                hoveredTooltipElement.getB(), 1f);
        uiShapeRenderer.setColor(elemColor);
        uiShapeRenderer.rect(px, py, panelW, panelH);
        uiShapeRenderer.end();

        float textX = px + 16f * uiScale;
        float textY = py + panelH - 18f * uiScale;
        float lineH = 22f * uiScale;

        uiBatch.begin();
        uiFont.getData().setScale(uiScale * 1.1f);
        uiFont.setColor(elemColor);
        uiFont.draw(uiBatch, hoveredTooltipElement.getDisplayName().toUpperCase(java.util.Locale.ENGLISH), textX,
                textY);

        textY -= lineH * 1.5f;
        uiFont.getData().setScale(uiScale * 0.7f);
        uiFont.setColor(Color.LIGHT_GRAY);
        uiFont.draw(uiBatch, "Tier: " + tier + (recipe.isEmpty() ? "" : "  |  " + recipe), textX, textY);

        textY -= lineH * 1.6f;
        uiFont.setColor(Color.GOLD);
        uiFont.getData().setScale(uiScale * 0.75f);
        String contextLabel = "staff".equals(tooltipContext) ? "Charm Effect:" : "Pillar Effect:";
        uiFont.draw(uiBatch, contextLabel, textX, textY);

        textY -= lineH * 1.3f;
        uiFont.setColor(Color.WHITE);
        uiFont.getData().setScale(uiScale * 0.65f);
        String description = "staff".equals(tooltipContext) ? staffDesc : pillarDesc;

        float maxTextW = panelW - 32f * uiScale;
        glyphLayout.setText(uiFont, description, Color.WHITE, maxTextW, com.badlogic.gdx.utils.Align.left, true);
        uiFont.draw(uiBatch, glyphLayout, textX, textY);

        uiFont.getData().setScale(uiScale * 0.72f);
        uiBatch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderMessages(int width, int height) {
        if (uiMessageTimer > 0) {
            uiBatch.begin();
            uiFontLarge.setColor(Color.RED);
            uiFontLarge.getData().setScale(uiScale * 0.95f);
            glyphLayout.setText(uiFontLarge, uiMessage);
            uiFontLarge.draw(uiBatch, uiMessage, width * 0.5f - glyphLayout.width * 0.5f, height - 55f * uiScale);
            uiFontLarge.getData().setScale(uiScale * 0.72f);
            uiBatch.end();
            uiMessageTimer -= Gdx.graphics.getDeltaTime();
        }
    }
    

    private void loadOrbIconTextures() {
        orbIconTextures.clear();
        for (Element element : Element.values()) {
            Texture t = loadFirstExistingTexture(getOrbTextureCandidates(element));
            if (t != null) {
                orbIconTextures.put(element, t);
            }
        }
    }

    private String[] getOrbTextureCandidates(Element e) {
        switch (e) {
            case FIRE:
                return new String[] { "orbs/fire64.png", "orbs/firesembollu64.png", "orbs/fire.png",
                        "orbs/firesembollu.png",
                        "orb_fire.png" };
            case WATER:
                return new String[] { "orbs/water64.png", "orbs/watersembollu64.png", "orbs/water.png",
                        "orbs/watersembollu.png", "orb_water.png" };
            case EARTH:
                return new String[] { "orbs/earth64.png", "orbs/earthsembollu64.png", "orbs/earth.png",
                        "orbs/earthsembollu.png", "orb_earth.png" };
            case AIR:
                return new String[] { "orbs/air64.png", "orbs/windsembollu64.png", "orbs/air.png",
                        "orbs/windsembollu.png",
                        "orb_air.png" };
            case STEAM:
                return new String[] { "orbs/steam64.png", "orbs/steamsembollu64.png", "orbs/steam.png",
                        "orbs/steamsembollu.png", "orb_steam.png" };
            case GOLD:
                return new String[] { "orbs/gold64.png", "orbs/goldsembollu64.png", "orbs/gold.png",
                        "orbs/goldsembollu.png",
                        "orb_gold.png" };
            case LIGHT:
                return new String[] { "orbs/light64.png", "orbs/lightsembollu64.png", "orbs/light.png",
                        "orbs/lightsembollu.png", "orb_light.png" };
            case POISON:
                return new String[] { "orbs/poison64.png", "orbs/poisonsembollu64.png", "orbs/poison.png",
                        "orbs/poisonsembollu.png", "orb_poison.png" };
            case ICE:
                return new String[] { "orbs/ice64.png", "orbs/icesembollu64.png", "orbs/ice.png",
                        "orbs/icesembollu.png",
                        "orb_ice.png" };
            case LIFE:
                return new String[] { "orbs/life64.png", "orbs/lifesembollu64.png", "orbs/life.png",
                        "orbs/lifesembollu.png",
                        "orb_life.png" };
            default:
                return new String[0];
        }
    }

    private Texture loadFirstExistingTexture(String[] candidates) {
        for (String path : candidates) {
            com.badlogic.gdx.files.FileHandle file = resolveAsset(path);
            if (file.exists()) {
                return new Texture(file);
            }
        }
        return null;
    }

    private Texture getOrbTexture(Element e) {
        return orbIconTextures == null ? null : orbIconTextures.get(e);
    }

    private void drawOrbTextureCentered(Element e, float slotX, float slotY, float slotW, float slotH) {
        Texture texture = getOrbTexture(e);
        if (texture == null) {
            return;
        }
        float iconW = 64f * uiScale;
        float iconH = 64f * uiScale;
        float drawX = slotX + (slotW - iconW) * 0.5f;
        float drawY = slotY + (slotH - iconH) * 0.5f;
        uiBatch.draw(texture, drawX, drawY, iconW, iconH);
    }

    private void renderMinimalHud(int screenWidth, int screenHeight, int mapAreaWidth) {
        float panelX = screenWidth - shopWidth;
        float panelW = shopWidth;
        float panelPad = 10f * uiScale;
        float sectionX = panelX + panelPad;
        float sectionW = panelW - panelPad * 2f;
        float headerH = 30f * uiScale;
        float gap = 8f * uiScale;
        float slotGap = 6f * uiScale;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.13f, 0.13f, 0.2f, 1f);
        uiShapeRenderer.rect(panelX, 0, panelW, screenHeight);
        uiShapeRenderer.end();

        float lifeIconSize = 48f * uiScale;
        float topY = screenHeight - 22f * uiScale;

        uiBatch.begin();
        Color hudTopColor = mapType == GameMap.MapType.ELEMENTAL_CASTLE ? Color.WHITE : Color.BLACK;
        uiFontLarge.setColor(hudTopColor);
        uiBatch.draw(hudLifeIconTexture, 16f * uiScale, topY - lifeIconSize + 4f * uiScale, lifeIconSize, lifeIconSize);
        String livesStr = economyManager.getLives() + "/" + Constants.STARTING_LIVES;
        uiFontLarge.draw(uiBatch, livesStr, 70f * uiScale, topY);
        glyphLayout.setText(uiFontLarge, livesStr);
        float livesEndX = 70f * uiScale + glyphLayout.width;

        String waveText = "WAVE " + (waveManager != null ? waveManager.getCurrentWave() : 0) + "/" + maxWaves;
        glyphLayout.setText(uiFontLarge, waveText);
        float waveStartX = mapAreaWidth - glyphLayout.width - 28f * uiScale;
        uiFontLarge.setColor(hudTopColor);
        uiFontLarge.draw(uiBatch, waveText, waveStartX, topY);

        int minutes = (int) (globalTimer / 60);
        int seconds = (int) (globalTimer % 60);
        int centis = (int) ((globalTimer * 100) % 100);
        String timerStr = String.format("%02d:%02d.%02d", minutes, seconds, centis);
        glyphLayout.setText(uiFontLarge, timerStr);
        float timerCenterX = livesEndX + (waveStartX - livesEndX) * 0.5f;
        uiFontLarge.setColor(hudTopColor);
        uiFontLarge.draw(uiBatch, timerStr, timerCenterX - glyphLayout.width * 0.5f, topY);

        float goldIconSize = 38f * uiScale;
        float goldY = screenHeight - 20f * uiScale;
        uiBatch.draw(hudGoldIconTexture, sectionX, goldY - goldIconSize, goldIconSize, goldIconSize);
        uiFontLarge.setColor(Color.WHITE);
        uiFontLarge.draw(uiBatch, String.valueOf(economyManager.getGold()), sectionX + 46f * uiScale,
                goldY - 2f * uiScale);
        uiBatch.end();

        float y = screenHeight - 56f * uiScale;
        float shopBarY = y - headerH;
        float shopSlotSize = (sectionW - slotGap * 3f) / 4f;
        float shopSlotsY = shopBarY - gap - shopSlotSize;
        float pricesY = shopSlotsY - 8f * uiScale;
        float orbStartX = sectionX;
        shop.setOrbLayout(orbStartX, shopSlotsY, shopSlotSize, slotGap);

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.92f, 0.92f, 0.92f, 1f);
        uiShapeRenderer.rect(sectionX, shopBarY, sectionW, headerH);
        for (int i = 0; i < 4; i++) {
            float sx = orbStartX + i * (shopSlotSize + slotGap);
            uiShapeRenderer.setColor(0.93f, 0.93f, 0.93f, 1f);
            uiShapeRenderer.rect(sx, shopSlotsY, shopSlotSize, shopSlotSize);
        }
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(Color.BLACK);
        glyphLayout.setText(uiFontLarge, "SHOP");
        uiFontLarge.draw(uiBatch, "SHOP", sectionX + (sectionW - glyphLayout.width) * 0.5f, shopBarY + headerH * 0.78f);
        uiFont.setColor(Color.WHITE);
        for (int i = 0; i < 4; i++) {
            float sx = orbStartX + i * (shopSlotSize + slotGap);
            drawOrbTextureCentered(shop.getOrbElement(i), sx, shopSlotsY, shopSlotSize, shopSlotSize);
            uiFont.draw(uiBatch, shop.getOrbPrice(i) + "G", sx + 8f * uiScale, pricesY);
        }
        uiBatch.end();

        float staffBarY = pricesY - 20f * uiScale - headerH;
        float staffSize = Math.min(sectionW - 16f * uiScale, 170f * uiScale);
        float staffX = sectionX + (sectionW - staffSize) * 0.5f;
        float staffY = staffBarY - gap - staffSize;
        staffUI.setBounds((int) staffX, (int) staffY, (int) staffSize, (int) staffSize);
        staffInfoBtnSize = 20f * uiScale;
        staffInfoBtnX = sectionX + sectionW - staffInfoBtnSize;
        staffInfoBtnY = staffBarY + (headerH - staffInfoBtnSize) * 0.5f;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.92f, 0.92f, 0.92f, 1f);
        uiShapeRenderer.rect(sectionX, staffBarY, sectionW, headerH);
        uiShapeRenderer.setColor(0.93f, 0.93f, 0.93f, 1f);
        uiShapeRenderer.rect(staffX, staffY, staffSize, staffSize);
        Element equipped = staffUI.getEquippedElement();
        if (equipped != null) {
            uiShapeRenderer.setColor(equipped.getR(), equipped.getG(), equipped.getB(), 1f);
            uiShapeRenderer.circle(staffX + staffSize * 0.5f, staffY + staffSize * 0.5f, staffSize * 0.22f);
        }
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(Color.BLACK);
        glyphLayout.setText(uiFontLarge, "CHARM");
        uiFontLarge.draw(uiBatch, "CHARM", sectionX + (sectionW - glyphLayout.width) * 0.5f,
                staffBarY + headerH * 0.78f);
        if (equipped != null) {
            drawOrbTextureCentered(equipped, staffX, staffY, staffSize, staffSize);
        }
        uiBatch.draw(hudInfoIconTexture, staffInfoBtnX, staffInfoBtnY, staffInfoBtnSize, staffInfoBtnSize);
        uiBatch.end();

        float mergeBarY = staffY - 16f * uiScale - headerH;
        float mergeSlotSize = Math.min(inventory.getSlotSize(), 60f * uiScale);
        float mergeRowWidth = mergeSlotSize + 170f;
        float mergeBoardX = sectionX + (sectionW - mergeRowWidth) * 0.5f - 15f;
        float mergeRowY = mergeBarY - 12f * uiScale - mergeSlotSize;
        float mergeBoardY = mergeRowY - mergeBoard.getHeight() * 0.5f + mergeSlotSize * 0.5f - 5f;
        mergeBoard.setSlotSize(mergeSlotSize);
        mergeBoard.setPosition(mergeBoardX, mergeBoardY);
        float mergeSlot1X = mergeBoard.getSlot1X();
        float mergeSlot2X = mergeBoard.getSlot2X();
        float mergeResultX = mergeBoard.getResultX();
        float mergePlusX = mergeSlot1X + mergeSlotSize + (mergeSlot2X - (mergeSlot1X + mergeSlotSize)) * 0.5f;
        float mergeEqualX = mergeSlot2X + mergeSlotSize + (mergeResultX - (mergeSlot2X + mergeSlotSize)) * 0.5f;
        mergeInfoBtnSize = 20f * uiScale;
        mergeInfoBtnX = sectionX + sectionW - mergeInfoBtnSize;
        mergeInfoBtnY = mergeBarY + (headerH - mergeInfoBtnSize) * 0.5f;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.92f, 0.92f, 0.92f, 1f);
        uiShapeRenderer.rect(sectionX, mergeBarY, sectionW, headerH);
        uiShapeRenderer.setColor(0.93f, 0.93f, 0.93f, 1f);
        uiShapeRenderer.rect(mergeSlot1X, mergeRowY, mergeSlotSize, mergeSlotSize);
        uiShapeRenderer.rect(mergeSlot2X, mergeRowY, mergeSlotSize, mergeSlotSize);
        uiShapeRenderer.rect(mergeResultX, mergeRowY, mergeSlotSize, mergeSlotSize);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(Color.BLACK);
        glyphLayout.setText(uiFontLarge, "MERGE");
        uiFontLarge.draw(uiBatch, "MERGE", sectionX + (sectionW - glyphLayout.width) * 0.5f,
                mergeBarY + headerH * 0.78f);
        if (mergeBoard.getSlot1Element() != null) {
            drawOrbTextureCentered(mergeBoard.getSlot1Element(), mergeSlot1X, mergeRowY, mergeSlotSize, mergeSlotSize);
        }
        if (mergeBoard.getSlot2Element() != null) {
            drawOrbTextureCentered(mergeBoard.getSlot2Element(), mergeSlot2X, mergeRowY, mergeSlotSize, mergeSlotSize);
        }
        if (mergeBoard.getResultElement() != null) {
            drawOrbTextureCentered(mergeBoard.getResultElement(), mergeResultX, mergeRowY, mergeSlotSize,
                    mergeSlotSize);
        }
        uiBatch.draw(hudInfoIconTexture, mergeInfoBtnX, mergeInfoBtnY, mergeInfoBtnSize, mergeInfoBtnSize);
        uiFontLarge.setColor(Color.WHITE);
        glyphLayout.setText(uiFontLarge, "+");
        uiFontLarge.draw(uiBatch, "+", mergePlusX - glyphLayout.width * 0.5f,
                mergeRowY + (mergeSlotSize + glyphLayout.height) * 0.5f);
        glyphLayout.setText(uiFontLarge, "=");
        uiFontLarge.draw(uiBatch, "=", mergeEqualX - glyphLayout.width * 0.5f,
                mergeRowY + (mergeSlotSize + glyphLayout.height) * 0.5f);
        uiBatch.end();

        float invHeaderY = mergeRowY - 18f * uiScale - headerH;
        float invSlotSize = inventory.getSlotSize();
        float invPad = inventory.getPadding();
        float invRowWidth = invSlotSize * 3f + invPad * 2f;
        float invX = sectionX + (sectionW - invRowWidth) * 0.5f;
        float invTopY = invHeaderY - gap - invSlotSize;
        float invBottomY = invTopY - invPad - invSlotSize;
        inventory.setPosition(invX, invBottomY);

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.92f, 0.92f, 0.92f, 1f);
        uiShapeRenderer.rect(sectionX, invHeaderY, sectionW, headerH);
        int unlocked = Math.min(6, inventory.getUnlockedSlots());
        for (int i = 0; i < unlocked; i++) {
            int col = i % 3;
            int row = i / 3;
            float sx = invX + col * (invSlotSize + invPad);
            float sy = invBottomY + (1 - row) * (invSlotSize + invPad);
            uiShapeRenderer.setColor(0.93f, 0.93f, 0.93f, 1f);
            uiShapeRenderer.rect(sx, sy, invSlotSize, invSlotSize);
            Element e = inventory.getOrbAt(i);
            if (e != null) {
                uiShapeRenderer.setColor(e.getR(), e.getG(), e.getB(), 1f);
                uiShapeRenderer.circle(sx + invSlotSize * 0.5f, sy + invSlotSize * 0.5f, invSlotSize * 0.3f);
            }
        }
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(Color.BLACK);
        glyphLayout.setText(uiFontLarge, "INVENTORY");
        uiFontLarge.draw(uiBatch, "INVENTORY", sectionX + (sectionW - glyphLayout.width) * 0.5f,
                invHeaderY + headerH * 0.78f);
        for (int i = 0; i < unlocked; i++) {
            Element e = inventory.getOrbAt(i);
            if (e == null) {
                continue;
            }
            int col = i % 3;
            int row = i / 3;
            float sx = invX + col * (invSlotSize + invPad);
            float sy = invBottomY + (1 - row) * (invSlotSize + invPad);
            drawOrbTextureCentered(e, sx, sy, invSlotSize, invSlotSize);
        }
        inventoryInfoBtnSize = 20f * uiScale;
        inventoryInfoBtnX = sectionX + sectionW - inventoryInfoBtnSize;
        inventoryInfoBtnY = invHeaderY + (headerH - inventoryInfoBtnSize) * 0.5f;
        uiBatch.draw(hudInfoIconTexture, inventoryInfoBtnX, inventoryInfoBtnY, inventoryInfoBtnSize,
                inventoryInfoBtnSize);
        uiBatch.end();

        int selected = inventory.getSelectedIndex();
        if (selected >= 0 && selected < 6) {
            int col = selected % 3;
            int row = selected / 3;
            float sx = invX + col * (invSlotSize + invPad);
            float sy = invBottomY + (1 - row) * (invSlotSize + invPad);
            uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            uiShapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f);
            uiShapeRenderer.rect(sx - 2f, sy - 2f, invSlotSize + 4f, invSlotSize + 4f);
            uiShapeRenderer.end();
        }

        pauseIconSize = (sectionW - slotGap) * 0.5f;
        speedIconSize = pauseIconSize;
        pauseIconX = sectionX;
        speedIconX = sectionX + pauseIconSize + slotGap;
        pauseIconY = 10f * uiScale;
        speedIconY = 10f * uiScale;
        autoplayBtnW = sectionW;
        autoplayBtnH = 30f * uiScale;
        autoplayBtnX = sectionX;
        autoplayBtnY = pauseIconY + pauseIconSize + 35f * uiScale;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.93f, 0.93f, 0.93f, 1f);
        uiShapeRenderer.rect(pauseIconX, pauseIconY, pauseIconSize, pauseIconSize);
        uiShapeRenderer.rect(speedIconX, speedIconY, speedIconSize, speedIconSize);
        uiShapeRenderer.setColor(0.92f, 0.92f, 0.92f, 1f);
        uiShapeRenderer.rect(autoplayBtnX, autoplayBtnY, autoplayBtnW, autoplayBtnH);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFont.setColor(Color.WHITE);
        glyphLayout.setText(uiFont, "PAUSE");
        uiFont.draw(uiBatch, "PAUSE", pauseIconX + (pauseIconSize - glyphLayout.width) * 0.5f,
                pauseIconY + pauseIconSize + glyphLayout.height + 6f * uiScale);

        glyphLayout.setText(uiFont, "SPEED");
        uiFont.draw(uiBatch, "SPEED", speedIconX + (speedIconSize - glyphLayout.width) * 0.5f,
                speedIconY + speedIconSize + glyphLayout.height + 6f * uiScale);

        uiFontLarge.setColor(Color.BLACK);
        String pauseSymbol = paused ? ">" : "||";
        glyphLayout.setText(uiFontLarge, pauseSymbol);
        uiFontLarge.draw(uiBatch, pauseSymbol,
                pauseIconX + (pauseIconSize - glyphLayout.width) * 0.5f,
                pauseIconY + (pauseIconSize + glyphLayout.height) * 0.5f);
        uiFontLarge.setColor(Color.BLACK);
        String speedText = String.format("%dX", (int) SPEED_MULTIPLIERS[speedIndex]);
        glyphLayout.setText(uiFontLarge, speedText);
        uiFontLarge.draw(uiBatch, speedText, speedIconX + (speedIconSize - glyphLayout.width) * 0.5f,
                speedIconY + (speedIconSize + glyphLayout.height) * 0.5f);

        uiFontLarge.setColor(Color.BLACK);
        String autoText = autoplayEnabled ? "AUTOPLAY: ON" : "AUTOPLAY: OFF";
        glyphLayout.setText(uiFontLarge, autoText);
        if (glyphLayout.width > autoplayBtnW - 4f) {
            uiFont.setColor(Color.BLACK);
            glyphLayout.setText(uiFont, autoText);
            uiFont.draw(uiBatch, autoText, autoplayBtnX + (autoplayBtnW - glyphLayout.width) * 0.5f,
                    autoplayBtnY + (autoplayBtnH + glyphLayout.height) * 0.5f);
        } else {
            uiFontLarge.draw(uiBatch, autoText, autoplayBtnX + (autoplayBtnW - glyphLayout.width) * 0.5f,
                    autoplayBtnY + autoplayBtnH * 0.78f);
        }

        playBtnW = sectionW;
        playBtnH = 30f * uiScale;
        playBtnX = sectionX;
        playBtnY = autoplayBtnY + autoplayBtnH + 10f * uiScale;

        seeAugmentsBtnW = sectionW;
        seeAugmentsBtnH = 30f * uiScale;
        seeAugmentsBtnX = sectionX;
        seeAugmentsBtnY = playBtnY + playBtnH + 10f * uiScale;

        uiBatch.end();

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        uiShapeRenderer.setColor(0.92f, 0.92f, 0.92f, 1f);
        uiShapeRenderer.rect(seeAugmentsBtnX, seeAugmentsBtnY, seeAugmentsBtnW, seeAugmentsBtnH);

        boolean canStartWave = !waveManager.isWaveInProgress() && !waveManager.areAllWavesComplete();
        uiShapeRenderer.setColor(canStartWave ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
        uiShapeRenderer.rect(playBtnX, playBtnY, playBtnW, playBtnH);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(Color.BLACK);

        String seeAugText = "SEE AUGMENTS";
        glyphLayout.setText(uiFontLarge, seeAugText);
        if (glyphLayout.width > seeAugmentsBtnW - 4f) {
            uiFont.setColor(Color.BLACK);
            glyphLayout.setText(uiFont, seeAugText);
            uiFont.draw(uiBatch, seeAugText, seeAugmentsBtnX + (seeAugmentsBtnW - glyphLayout.width) * 0.5f,
                    seeAugmentsBtnY + (seeAugmentsBtnH + glyphLayout.height) * 0.5f);
        } else {
            uiFontLarge.draw(uiBatch, seeAugText, seeAugmentsBtnX + (seeAugmentsBtnW - glyphLayout.width) * 0.5f,
                    seeAugmentsBtnY + seeAugmentsBtnH * 0.78f);
        }

        uiFontLarge.setColor(canStartWave ? Color.BLACK : Color.DARK_GRAY);
        String playText = waveManager.areAllWavesComplete() ? "DONE" : "PLAY";
        glyphLayout.setText(uiFontLarge, playText);
        if (glyphLayout.width > playBtnW - 4f) {
            uiFont.setColor(canStartWave ? Color.BLACK : Color.DARK_GRAY);
            glyphLayout.setText(uiFont, playText);
            uiFont.draw(uiBatch, playText, playBtnX + (playBtnW - glyphLayout.width) * 0.5f,
                    playBtnY + (playBtnH + glyphLayout.height) * 0.5f);
        } else {
            uiFontLarge.draw(uiBatch, playText, playBtnX + (playBtnW - glyphLayout.width) * 0.5f,
                    playBtnY + playBtnH * 0.78f);
        }
        uiBatch.end();

        if (staffInfoOpen || mergeInfoOpen || inventoryInfoOpen) {
            infoPanelW = Math.min(420f * uiScale, mapAreaWidth * 0.44f);
            infoPanelH = 250f * uiScale;
            infoPanelX = panelX - infoPanelW - 12f * uiScale;
            infoPanelY = screenHeight - infoPanelH - 26f * uiScale - INFO_PANEL_SHIFT_DOWN * uiScale;
            if (infoPanelY < 8f * uiScale) {
                infoPanelY = 8f * uiScale;
            }

            Texture activeInfoTexture = mergeInfoOpen ? mergeInfoPanelTexture
                    : (inventoryInfoOpen ? inventoryInfoPanelTexture : staffInfoPanelTexture);

            uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            uiShapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.94f);
            uiShapeRenderer.rect(infoPanelX, infoPanelY, infoPanelW, infoPanelH);
            uiShapeRenderer.end();

            uiBatch.begin();
            uiBatch.draw(activeInfoTexture, infoPanelX, infoPanelY, infoPanelW, infoPanelH);
            uiBatch.end();
        }
    }

    private void renderPillarStats(int screenWidth, int screenHeight) {

        float boxW = 220 * uiScale;
        float boxH = 120 * uiScale;
        float boxX = Gdx.input.getX() + 20;
        float boxY = screenHeight - Gdx.input.getY() + 20;
        if (boxX + boxW > screenWidth - shopWidth)
            boxX = screenWidth - shopWidth - boxW - 10;
        if (boxY + boxH > screenHeight)
            boxY = screenHeight - boxH - 10;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        uiShapeRenderer.rect(boxX, boxY, boxW, boxH);
        uiShapeRenderer.end();
        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        uiShapeRenderer.setColor(0.5f, 0.5f, 0.6f, 1f);
        uiShapeRenderer.rect(boxX, boxY, boxW, boxH);
        uiShapeRenderer.end();

        uiBatch.begin();
        float lineH = 18 * uiScale;
        float tx = boxX + 10;
        float ty = boxY + boxH - 15;
        PillarType type = hoveredPillar.getType();
        uiFont.setColor(Color.GOLD);
        uiFont.draw(uiBatch, type.getDisplayName(), tx, ty);
        ty -= lineH;
        uiFont.setColor(Color.WHITE);
        uiFont.draw(uiBatch, "Damage: x" + String.format("%.1f", type.getDamageMult()), tx, ty);
        ty -= lineH;
        uiFont.draw(uiBatch, "Range: x" + String.format("%.1f", type.getRangeMult()), tx, ty);
        ty -= lineH;
        uiFont.draw(uiBatch, "Speed: x" + String.format("%.1f", type.getAttackSpeedMult()), tx, ty);
        ty -= lineH;
        if (hoveredPillar.isActive()) {
            Element e = hoveredPillar.getCurrentElement();
            uiFont.setColor(e.getR(), e.getG(), e.getB(), 1f);
            uiFont.draw(uiBatch, "Element: " + e.name(), tx, ty);
        } else {
            uiFont.setColor(Color.GRAY);
            uiFont.draw(uiBatch, "Elemental Orb Needed!", tx, ty);
        }
        uiBatch.end();
    }

    private void renderGameOver(int w, int h) {
        uiBatch.begin();
        uiBatch.setColor(1f, 1f, 1f, 1f);
        uiBatch.draw(pauseMenuBackgroundTexture, 0, 0, w, h);
        uiFont.setColor(Color.RED);
        uiFont.getData().setScale(uiScale * 1.8f);
        uiFont.draw(uiBatch, "GAME OVER", w / 2 - 150 * uiScale, h / 2 + 150 * uiScale);
        uiFont.getData().setScale(uiScale * 0.54f);

        int minutes = (int) (globalTimer / 60);
        int seconds = (int) (globalTimer % 60);
        String timerString = String.format("Time: %02d:%02d", minutes, seconds);
        uiFont.setColor(Color.WHITE);
        glyphLayout.setText(uiFont, timerString);
        uiFont.draw(uiBatch, timerString, (w - glyphLayout.width) / 2, h / 2 - 180 * uiScale);
        uiBatch.end();

        float btnW = 300 * uiScale;
        float btnH = 60 * uiScale;
        float btnX = (w - btnW) / 2;
        float menuY = h / 2 - btnH / 2;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.56f, 0.43f, 0.33f, 1f);
        uiShapeRenderer.rect(btnX, menuY, btnW, btnH);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
        glyphLayout.setText(uiFontLarge, "Main Menu");
        uiFontLarge.draw(uiBatch, "Main Menu", btnX + (btnW - glyphLayout.width) / 2,
                menuY + (btnH + glyphLayout.height) / 2);
        uiBatch.end();
    }

    private void renderGameWon(int w, int h) {
        uiBatch.begin();
        uiBatch.setColor(1f, 1f, 1f, 1f);
        uiBatch.draw(pauseMenuBackgroundTexture, 0, 0, w, h);
        uiFont.setColor(Color.GREEN);
        uiFont.getData().setScale(uiScale * 1.8f);
        uiFont.draw(uiBatch, "YOU WIN!", w / 2 - 120 * uiScale, h / 2 + 150 * uiScale);
        uiFont.getData().setScale(uiScale * 0.54f);

        int minutes = (int) (globalTimer / 60);
        int seconds = (int) (globalTimer % 60);
        String timerString = String.format("Time: %02d:%02d", minutes, seconds);
        uiFont.setColor(Color.WHITE);
        glyphLayout.setText(uiFont, timerString);
        uiFont.draw(uiBatch, timerString, (w - glyphLayout.width) / 2, h / 2 - 180 * uiScale);
        uiBatch.end();

        float btnW = 300 * uiScale;
        float btnH = 60 * uiScale;
        float btnX = (w - btnW) / 2;
        float freeY = h / 2 + 10 * uiScale;
        float menuY = h / 2 - 70 * uiScale;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.56f, 0.43f, 0.33f, 1f);
        uiShapeRenderer.rect(btnX, freeY, btnW, btnH);
        uiShapeRenderer.rect(btnX, menuY, btnW, btnH);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));
        glyphLayout.setText(uiFontLarge, "Continue Freeplay");
        uiFontLarge.draw(uiBatch, "Continue Freeplay", btnX + (btnW - glyphLayout.width) / 2,
                freeY + (btnH + glyphLayout.height) / 2);
        glyphLayout.setText(uiFontLarge, "Main Menu");
        uiFontLarge.draw(uiBatch, "Main Menu", btnX + (btnW - glyphLayout.width) / 2,
                menuY + (btnH + glyphLayout.height) / 2);
        uiBatch.end();
    }

    private void renderPauseMenu(int w, int h) {
        uiBatch.begin();
        uiBatch.setColor(1f, 1f, 1f, 1f);
        uiBatch.draw(pauseMenuBackgroundTexture, 0, 0, w, h);
        uiBatch.end();

        uiBatch.begin();
        int minutes = (int) (globalTimer / 60);
        int seconds = (int) (globalTimer % 60);
        int centis = (int) ((globalTimer * 100) % 100);
        String timerStr = String.format("%02d:%02d.%02d", minutes, seconds, centis);
        uiFont.setColor(Color.WHITE);
        uiFont.getData().setScale(uiScale * 1.2f);
        glyphLayout.setText(uiFont, timerStr);
        uiFont.draw(uiBatch, timerStr, (w - glyphLayout.width) / 2, h / 2 + 270 * uiScale);
        uiFont.getData().setScale(uiScale * 0.54f);
        uiBatch.end();

        uiBatch.begin();
        uiFont.setColor(Color.BLACK);
        uiFont.getData().setScale(uiScale * 1.8f);
        glyphLayout.setText(uiFont, "PAUSED");
        uiFont.draw(uiBatch, "PAUSED", (w - glyphLayout.width) / 2, h / 2 + 230 * uiScale);
        uiFont.getData().setScale(uiScale * 0.54f);
        uiBatch.end();

        float btnW = 300 * uiScale;
        float btnH = 50 * uiScale;
        float btnX = (w - btnW) / 2;
        float gap = 12 * uiScale;
        float resumeY = h / 2 + 110 * uiScale;
        float optionsY = resumeY - btnH - gap;
        float menuY = optionsY - btnH - gap;
        float quitY = menuY - btnH - gap;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.56f, 0.43f, 0.33f, 1f);
        uiShapeRenderer.rect(btnX, resumeY, btnW, btnH);
        uiShapeRenderer.rect(btnX, optionsY, btnW, btnH);
        uiShapeRenderer.rect(btnX, menuY, btnW, btnH);
        uiShapeRenderer.setColor(0.6f, 0.2f, 0.2f, 1f);
        uiShapeRenderer.rect(btnX, quitY, btnW, btnH);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(new Color(0.14f, 0.1f, 0.06f, 1f));

        glyphLayout.setText(uiFontLarge, "Resume");
        uiFontLarge.draw(uiBatch, "Resume", btnX + (btnW - glyphLayout.width) / 2,
                resumeY + (btnH + glyphLayout.height) / 2);

        glyphLayout.setText(uiFontLarge, "Options");
        uiFontLarge.draw(uiBatch, "Options", btnX + (btnW - glyphLayout.width) / 2,
                optionsY + (btnH + glyphLayout.height) / 2);

        glyphLayout.setText(uiFontLarge, "Main Menu");
        uiFontLarge.draw(uiBatch, "Main Menu", btnX + (btnW - glyphLayout.width) / 2,
                menuY + (btnH + glyphLayout.height) / 2);

        uiFontLarge.setColor(Color.WHITE);
        glyphLayout.setText(uiFontLarge, "Quit");
        uiFontLarge.draw(uiBatch, "Quit", btnX + (btnW - glyphLayout.width) / 2,
                quitY + (btnH + glyphLayout.height) / 2);
        uiBatch.end();
    }

    private void updatePillarPanelPosition(int screenWidth, int screenHeight, int mapAreaWidth) {
        if (selectedPillar == null)
            return;
        Vector3 projected = selectedPillar.getPosition().cpy();
        projected.y = 1.4f;
        camera.project(projected, 0, 0, mapAreaWidth, screenHeight);

        pillarPanelW = 230f * uiScale;
        pillarPanelH = 170f * uiScale;
        pillarPanelX = projected.x + 16f * uiScale;
        pillarPanelY = projected.y + 16f * uiScale;

        if (pillarPanelX + pillarPanelW > mapAreaWidth - 8f)
            pillarPanelX = mapAreaWidth - pillarPanelW - 8f;
        if (pillarPanelY + pillarPanelH > screenHeight - 8f)
            pillarPanelY = screenHeight - pillarPanelH - 8f;
        if (pillarPanelX < 8f)
            pillarPanelX = 8f;
        if (pillarPanelY < 8f)
            pillarPanelY = 8f;
    }

    private void renderPillarActionPanel(int screenWidth, int screenHeight) {
        if (selectedPillar == null)
            return;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.07f, 0.09f, 0.14f, 0.95f);
        uiShapeRenderer.rect(pillarPanelX, pillarPanelY, pillarPanelW, pillarPanelH);
        uiShapeRenderer.end();

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        uiShapeRenderer.setColor(0.55f, 0.7f, 0.9f, 1f);
        uiShapeRenderer.rect(pillarPanelX, pillarPanelY, pillarPanelW, pillarPanelH);
        uiShapeRenderer.end();

        float btnW = pillarPanelW - 20f * uiScale;
        float btnX = pillarPanelX + 10f * uiScale;
        float sellY = pillarPanelY + pillarPanelH - (PILLAR_PANEL_BTN_H + 18f) * uiScale;
        float addY = sellY - (PILLAR_PANEL_BTN_H + 10f) * uiScale;
        float removeY = addY - (PILLAR_PANEL_BTN_H + 10f) * uiScale;

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.35f, 0.18f, 0.18f, 1f);
        uiShapeRenderer.rect(btnX, sellY, btnW, PILLAR_PANEL_BTN_H * uiScale);
        uiShapeRenderer.setColor(awaitingPillarOrbSelection ? 0.12f : 0.18f, 0.32f, 0.18f, 1f);
        uiShapeRenderer.rect(btnX, addY, btnW, PILLAR_PANEL_BTN_H * uiScale);
        uiShapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        uiShapeRenderer.rect(btnX, removeY, btnW, PILLAR_PANEL_BTN_H * uiScale);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFont.setColor(Color.GOLD);
        uiFont.draw(uiBatch, selectedPillar.getType().getDisplayName(), pillarPanelX + 10f * uiScale,
                pillarPanelY + pillarPanelH - 8f * uiScale);

        uiFont.setColor(Color.WHITE);
        uiFont.draw(uiBatch, "Sell", btnX + 8f * uiScale, sellY + 20f * uiScale);
        uiFont.draw(uiBatch, "Add Orb", btnX + 8f * uiScale, addY + 20f * uiScale);
        uiFont.draw(uiBatch, "Remove Orb", btnX + 8f * uiScale, removeY + 20f * uiScale);

        Element e = selectedPillar.getCurrentElement();
        if (e != null) {
            uiFont.setColor(e.getR(), e.getG(), e.getB(), 1f);
            uiFont.draw(uiBatch, "Filled", pillarPanelX + 10f * uiScale, pillarPanelY + 16f * uiScale);
        } else {
            uiFont.setColor(Color.LIGHT_GRAY);
            uiFont.draw(uiBatch, "Empty", pillarPanelX + 10f * uiScale, pillarPanelY + 16f * uiScale);
        }
        if (awaitingPillarOrbSelection) {
            uiFont.setColor(Color.CYAN);
            uiFont.draw(uiBatch, "Pick an orb from inventory", pillarPanelX + 80f * uiScale,
                    pillarPanelY + 16f * uiScale);
        }
        uiBatch.end();

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (e != null) {
            uiShapeRenderer.setColor(e.getR(), e.getG(), e.getB(), 1f);
            uiShapeRenderer.circle(pillarPanelX + 55f * uiScale, pillarPanelY + 10f * uiScale, 8f * uiScale);
        } else {
            uiShapeRenderer.setColor(0.3f, 0.3f, 0.35f, 1f);
            uiShapeRenderer.circle(pillarPanelX + 55f * uiScale, pillarPanelY + 10f * uiScale, 8f * uiScale);
        }
        uiShapeRenderer.end();
    }

    private void renderAugmentSelection(int screenWidth, int screenHeight) {
        float boxW = 800 * uiScale;
        float boxH = 340 * uiScale;
        float boxX = (screenWidth - boxW) * 0.5f;
        float boxY = (screenHeight - boxH) * 0.5f;
        float cardW = (boxW - 80 * uiScale) * 0.5f;
        float cardH = 220 * uiScale;
        float cardY = boxY + 40 * uiScale;

        uiBatch.begin();
        uiBatch.setColor(1f, 1f, 1f, 1f);
        uiBatch.draw(pauseMenuBackgroundTexture, 0, 0, screenWidth, screenHeight);
        uiBatch.end();

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.1f, 0.08f, 0.14f, 0.98f);
        uiShapeRenderer.rect(boxX, boxY, boxW, boxH);
        uiShapeRenderer.setColor(0.16f, 0.12f, 0.2f, 1f);
        uiShapeRenderer.rect(boxX + 25 * uiScale, cardY, cardW, cardH);
        uiShapeRenderer.rect(boxX + 55 * uiScale + cardW, cardY, cardW, cardH);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(Color.GOLD);
        glyphLayout.setText(uiFontLarge, "AUGMENT CHOICE");
        uiFontLarge.draw(uiBatch, "AUGMENT CHOICE", boxX + (boxW - glyphLayout.width) * 0.5f,
                boxY + boxH - 25 * uiScale);

        float leftX = boxX + 40 * uiScale;
        Texture iconA = getAugmentIconTexture(augmentOptionA);
        if (iconA != null) {
            uiBatch.draw(iconA, leftX + (cardW - 80 * uiScale) * 0.5f, cardY + cardH - 100 * uiScale, 80 * uiScale,
                    80 * uiScale);
        }
        uiFontLarge.setColor(Color.CYAN);
        String nameA = "[1] " + getAugmentName(augmentOptionA);
        glyphLayout.setText(uiFontLarge, nameA);
        uiFontLarge.draw(uiBatch, nameA, leftX + (cardW - glyphLayout.width) * 0.5f, cardY + cardH - 120 * uiScale);
        uiFont.setColor(Color.WHITE);
        String descA = getAugmentDesc(augmentOptionA);
        glyphLayout.setText(uiFont, descA);
        uiFont.draw(uiBatch, descA, leftX + (cardW - glyphLayout.width) * 0.5f, cardY + 50 * uiScale);

        float rightX = boxX + 55 * uiScale + cardW + 15 * uiScale;
        Texture iconB = getAugmentIconTexture(augmentOptionB);
        if (iconB != null) {
            uiBatch.draw(iconB, rightX + (cardW - 80 * uiScale) * 0.5f, cardY + cardH - 100 * uiScale, 80 * uiScale,
                    80 * uiScale);
        }
        uiFontLarge.setColor(Color.CYAN);
        String nameB = "[2] " + getAugmentName(augmentOptionB);
        glyphLayout.setText(uiFontLarge, nameB);
        uiFontLarge.draw(uiBatch, nameB, rightX + (cardW - glyphLayout.width) * 0.5f, cardY + cardH - 120 * uiScale);
        uiFont.setColor(Color.WHITE);
        String descB = getAugmentDesc(augmentOptionB);
        glyphLayout.setText(uiFont, descB);
        uiFont.draw(uiBatch, descB, rightX + (cardW - glyphLayout.width) * 0.5f, cardY + 50 * uiScale);

        uiBatch.end();
    }

    private void renderAcquiredAugments(int screenWidth, int screenHeight) {
        float boxW = 800 * uiScale;
        float boxH = 400 * uiScale;
        float boxX = (screenWidth - boxW) * 0.5f;
        float boxY = (screenHeight - boxH) * 0.5f;

        uiBatch.begin();
        uiBatch.setColor(1f, 1f, 1f, 1f);
        uiBatch.draw(pauseMenuBackgroundTexture, 0, 0, screenWidth, screenHeight);
        uiBatch.end();

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.1f, 0.08f, 0.14f, 0.98f);
        uiShapeRenderer.rect(boxX, boxY, boxW, boxH);
        uiShapeRenderer.end();

        uiBatch.begin();
        uiFontLarge.setColor(Color.GOLD);
        glyphLayout.setText(uiFontLarge, "ACQUIRED AUGMENTS");
        uiFontLarge.draw(uiBatch, "ACQUIRED AUGMENTS", boxX + (boxW - glyphLayout.width) * 0.5f,
                boxY + boxH - 25 * uiScale);

        int hoverAugId = -1;
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();

        if (acquiredAugments.isEmpty()) {
            uiFontLarge.setColor(Color.LIGHT_GRAY);
            glyphLayout.setText(uiFontLarge, "No augments acquired yet.");
            uiFontLarge.draw(uiBatch, "No augments acquired yet.", boxX + (boxW - glyphLayout.width) * 0.5f,
                    boxY + boxH * 0.5f);
        } else {
            float gapX = 220 * uiScale;
            float totalW = (acquiredAugments.size - 1) * gapX;
            float startX = boxX + boxW * 0.5f - totalW * 0.5f;
            float startY = boxY + boxH - 140 * uiScale;

            for (int i = 0; i < acquiredAugments.size; i++) {
                AcquiredAugment aug = acquiredAugments.get(i);
                float cx = startX + i * gapX;

                String augName = getAugmentName(aug.id);
                Texture iconTex = getAugmentIconTexture(aug.id);

                float iconSize = 80 * uiScale;
                float iconX = cx - iconSize * 0.5f;
                float iconY = startY;

                if (iconTex != null) {
                    uiBatch.draw(iconTex, iconX, iconY, iconSize, iconSize);
                }

                uiFont.setColor(Color.WHITE);
                glyphLayout.setText(uiFont, augName);
                uiFont.draw(uiBatch, augName, cx - glyphLayout.width * 0.5f, startY - 20 * uiScale);

                if (mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= iconY && mouseY <= iconY + iconSize) {
                    hoverAugId = aug.id;
                }
            }
        }

        uiFontLarge.setColor(Color.WHITE);
        glyphLayout.setText(uiFontLarge, "Close");
        uiFontLarge.draw(uiBatch, "Close", boxX + (boxW - glyphLayout.width) * 0.5f, boxY + 50 * uiScale);
        uiBatch.end();

        if (hoverAugId != -1) {
            String name = getAugmentName(hoverAugId);
            String desc = getAugmentDesc(hoverAugId);

            uiFont.setColor(Color.GOLD);
            glyphLayout.setText(uiFont, name);
            float nameW = glyphLayout.width;
            uiFont.setColor(Color.WHITE);
            glyphLayout.setText(uiFont, desc);
            float descW = glyphLayout.width;

            float tooltipW = Math.max(nameW, descW) + 20 * uiScale;
            float tooltipH = 60 * uiScale;
            float tooltipX = mouseX + 15 * uiScale;
            float tooltipY = mouseY - tooltipH - 15 * uiScale;

            if (tooltipX + tooltipW > screenWidth)
                tooltipX = screenWidth - tooltipW - 10 * uiScale;
            if (tooltipY < 0)
                tooltipY = 10 * uiScale;

            uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            uiShapeRenderer.setColor(0f, 0f, 0f, 0.85f);
            uiShapeRenderer.rect(tooltipX, tooltipY, tooltipW, tooltipH);
            uiShapeRenderer.end();

            uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            uiShapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
            uiShapeRenderer.rect(tooltipX, tooltipY, tooltipW, tooltipH);
            uiShapeRenderer.end();

            uiBatch.begin();
            uiFont.setColor(Color.GOLD);
            uiFont.draw(uiBatch, name, tooltipX + 10 * uiScale, tooltipY + tooltipH - 10 * uiScale);
            uiFont.setColor(Color.WHITE);
            uiFont.draw(uiBatch, desc, tooltipX + 10 * uiScale, tooltipY + tooltipH - 35 * uiScale);
            uiBatch.end();
        }
    }

    private Texture getAugmentIconTexture(int augmentId) {
        String path = "";
        switch (augmentId) {
            case 0:
                path = "ui/augment_icon_range.png";
                break;
            case 1:
                path = "ui/augment_icon_speed.png";
                break;
            case 2:
                path = "ui/augment_icon_damage.png";
                break;
            case 3:
                path = "ui/augment_icon_gold.png";
                break;
            case 4:
                path = "ui/augment_icon_slots.png";
                break;
            case 5:
                path = "ui/augment_icon_funding.png";
                break;
            case 6:
                path = "ui/augment_icon_wave_bonus.png";
                break;
            case 7:
                path = "ui/augment_icon_range.png";
                break;
        }
        com.badlogic.gdx.files.FileHandle file = resolveAsset(path);
        if (file.exists()) {
            return new Texture(file);
        }
        return null;
    }

    private void rollAugments() {
        Array<Integer> available = new Array<>();
        for (int i = 0; i <= 7; i++) {
            boolean has = false;
            for (int j = 0; j < acquiredAugments.size; j++) {
                if (acquiredAugments.get(j).id == i) {
                    has = true;
                    break;
                }
            }
            if (!has)
                available.add(i);
        }

        if (available.size >= 2) {
            available.shuffle();
            augmentOptionA = available.get(0);
            augmentOptionB = available.get(1);
        } else if (available.size == 1) {
            augmentOptionA = available.get(0);
            augmentOptionB = available.get(0);
        } else {
            augmentOptionA = MathUtils.random(0, 7);
            augmentOptionB = MathUtils.random(0, 7);
        }
    }

    private void showAugmentSelection() {
        rollAugments();
        augmentChoiceActive = true;
    }

    private void giveRandomAugment() {
        int r = MathUtils.random(1, 4);
        applyAugment(r);
    }

    private void saveGameState() {
        com.td.game.systems.SaveData data = new com.td.game.systems.SaveData();
        data.mapType = this.mapType.name();
        data.globalTimer = this.globalTimer;

        waveManager.save(data);
        economyManager.save(data);
        player.save(data);
        inventory.save(data);
        staffUI.save(data);

        if (mergeBoard.hasSlot1())
            data.mergeSlot1 = mergeBoard.getSlot1Element().name();
        if (mergeBoard.hasSlot2())
            data.mergeSlot2 = mergeBoard.getSlot2Element().name();
        if (mergeBoard.hasResult())
            data.mergeResult = mergeBoard.getResultElement().name();

        this.save(data);

        data.pillars.clear();
        for (Pillar p : pillars) {
            com.td.game.systems.SaveData.PillarSaveData pData = new com.td.game.systems.SaveData.PillarSaveData();
            p.save(pData);
            data.pillars.add(pData);
        }

        com.td.game.systems.SaveManager.save(data, mapType);
    }

    private void loadGame() {
        com.td.game.systems.SaveData data = com.td.game.systems.SaveManager.load(mapType);
        if (data != null) {
            this.globalTimer = data.globalTimer;
            waveManager.load(data);
            economyManager.load(data);
            player.load(data);
            inventory.load(data);
            staffUI.load(data);
            if (staffUI.hasOrb()) {
                int idx = staffUI.getEquippedElement().ordinal();
                player.setStaffOrbModel(new com.badlogic.gdx.graphics.g3d.ModelInstance(orbModels[idx]),
                        staffUI.getEquippedElement());
            }

            if (data.mergeSlot1 != null)
                mergeBoard.setSlot1Element(Element.valueOf(data.mergeSlot1));
            if (data.mergeSlot2 != null)
                mergeBoard.setSlot2Element(Element.valueOf(data.mergeSlot2));
            if (data.mergeResult != null)
                mergeBoard.setResultElement(Element.valueOf(data.mergeResult));

            this.load(data);

            pillars.clear();
            for (com.td.game.systems.SaveData.PillarSaveData pData : data.pillars) {
                PillarType pType = PillarType.valueOf(pData.type);
                Pillar pillar = new Pillar(pType, new Vector3(pData.x, pData.y, pData.z), modelFactory);
                pillar.load(pData);
                pillars.add(pillar);
            }
            com.badlogic.gdx.Gdx.app.log("GameScreen", "Save file loaded successfully.");
        } else {
            com.badlogic.gdx.Gdx.app.error("GameScreen", "No save file found to load.");
        }
    }

    public void save(com.td.game.systems.SaveData data) {
        data.globalDamageMult = this.globalDamageMult;
        data.globalRangeMult = this.globalRangeMult;
        data.globalAttackSpeedMult = this.globalAttackSpeedMult;
        data.staffAuraRadius = this.staffAuraRadius;

        data.acquiredAugments.clear();
        for (AcquiredAugment aug : this.acquiredAugments) {
            data.acquiredAugments.add(aug.id);
        }
    }

    public void load(com.td.game.systems.SaveData data) {
        this.globalDamageMult = data.globalDamageMult;
        this.globalRangeMult = data.globalRangeMult;
        this.globalAttackSpeedMult = data.globalAttackSpeedMult;
        this.staffAuraRadius = data.staffAuraRadius;

        this.acquiredAugments.clear();
        for (int augId : data.acquiredAugments) {
            this.acquiredAugments.add(new AcquiredAugment(augId));
        }
    }

    private void applyAugmentChoice(int option) {
        if (!augmentChoiceActive)
            return;

        int picked = option == 1 ? augmentOptionA : augmentOptionB;
        applyAugment(picked);
        acquiredAugments.add(new AcquiredAugment(picked));
        augmentChoiceActive = false;
        augmentOptionA = -1;
        augmentOptionB = -1;
    }

    private void applyAugment(int augmentId) {
        switch (augmentId) {
            case 0:
                globalRangeMult *= 1.15f;
                showMessage("Augment: Arcane Reach");
                break;
            case 1:
                globalAttackSpeedMult *= 1.15f;
                showMessage("Augment: Overcharge");
                break;
            case 2:
                globalDamageMult *= 1.2f;
                showMessage("Augment: Ember Core");
                break;
            case 3:
                economyManager.earn(80);
                showMessage("Augment: Golden Pact");
                break;
            case 4:
                inventory.unlockSlots(3);
                showMessage("Augment: Inventory Slots +3");
                break;
            case 5:
                economyManager.earn(120);
                showMessage("Augment: Emergency Funding");
                break;
            case 6:
                economyManager.earn(100);
                showMessage("Augment: Stipend");
                break;
            case 7:
                staffAuraRadius += 2.5f;
                showMessage("Augment: Expanding Aura");
                break;
            default:
                break;
        }
    }

    private String getAugmentName(int augmentId) {
        switch (augmentId) {
            case 0:
                return "Arcane Reach";
            case 1:
                return "Overcharge";
            case 2:
                return "Ember Core";
            case 3:
                return "Golden Pact";
            case 4:
                return "Tetra Slots";
            case 5:
                return "Emergency Funding";
            case 6:
                return "Stipend";
            case 7:
                return "Expanding Aura";
            default:
                return "Unknown";
        }
    }

    private String getAugmentDesc(int augmentId) {
        switch (augmentId) {
            case 0:
                return "All pillar range +15%";
            case 1:
                return "All pillar speed +15%";
            case 2:
                return "All pillar damage +20%";
            case 3:
                return "Instant +80 gold";
            case 4:
                return "Inventory capacity +3";
            case 5:
                return "Instant +120 gold";
            case 6:
                return "Instant +100 gold";
            case 7:
                return "Alchemist aura radius +30%";
            default:
                return "-";
        }
    }

    private float[] getAuraMultipliers(Element element) {
        float damage = 1f;
        float range = 1f;
        float speed = 1f;
        switch (element) {
            case FIRE:
                damage = 1.25f;
                break;
            case WATER:
                range = 1.2f;
                break;
            case EARTH:
                damage = 1.1f;
                range = 1.1f;
                break;
            case AIR:
                speed = 1.25f;
                break;
            case STEAM:
                speed = 1.1f;
                range = 1.1f;
                break;
            case GOLD:
                damage = 1.15f;
                break;
            case LIGHT:
                range = 1.2f;
                speed = 1.1f;
                break;
            case POISON:
                damage = 1.12f;
                break;
            case ICE:
                range = 1.15f;
                break;
            case LIFE:
                speed = 1.15f;
                damage = 1.08f;
                break;
            default:
                break;
        }
        return new float[] { damage, range, speed };
    }

    private void updatePillarMultipliers() {
        Element staffElement = staffUI.getEquippedElement();
        float[] aura = null;
        if (staffElement != null) {
            aura = getAuraMultipliers(staffElement);
        }

        for (Pillar pillar : pillars) {
            float damage = globalDamageMult;
            float range = globalRangeMult;
            float speed = globalAttackSpeedMult;

            if (aura != null && pillar.getPosition().dst(player.getPosition()) <= staffAuraRadius) {
                damage *= aura[0];
                range *= aura[1];
                speed *= aura[2];
            }
            pillar.setExternalMultipliers(damage, range, speed);
        }
    }

    private boolean isPlayerInsideGrid() {
        return playerGridX >= 0 && playerGridX < Constants.MAP_WIDTH &&
                playerGridZ >= 0 && playerGridZ < Constants.MAP_HEIGHT;
    }

    private boolean isPlayerOnBuildableTile() {
        if (!isPlayerInsideGrid()) {
            return false;
        }
        float worldX = playerGridX * Constants.TILE_SIZE;
        float worldZ = playerGridZ * Constants.TILE_SIZE;
        return !gameMap.isPathAt(worldX, worldZ);
    }

    private boolean isPillarAt(float worldX, float worldZ) {
        for (Pillar p : pillars) {
            if (p.getPosition().dst(worldX, 0f, worldZ) < 0.5f) {
                return true;
            }
        }
        return false;
    }

    private boolean isPillarPanelVisible() {
        return selectedPillar != null;
    }

    private boolean isPointInPillarPanel(float x, float y) {
        return isPillarPanelVisible() &&
                x >= pillarPanelX && x <= pillarPanelX + pillarPanelW &&
                y >= pillarPanelY && y <= pillarPanelY + pillarPanelH;
    }

    private int getPillarPanelButton(float x, float y) {
        if (!isPillarPanelVisible())
            return -1;
        float btnW = pillarPanelW - 20f * uiScale;
        float btnX = pillarPanelX + 10f * uiScale;
        float btnH = PILLAR_PANEL_BTN_H * uiScale;
        float sellY = pillarPanelY + pillarPanelH - (PILLAR_PANEL_BTN_H + 18f) * uiScale;
        float addY = sellY - (PILLAR_PANEL_BTN_H + 10f) * uiScale;
        float removeY = addY - (PILLAR_PANEL_BTN_H + 10f) * uiScale;

        if (x >= btnX && x <= btnX + btnW) {
            if (y >= sellY && y <= sellY + btnH)
                return 0;
            if (y >= addY && y <= addY + btnH)
                return 1;
            if (y >= removeY && y <= removeY + btnH)
                return 2;
        }
        return -1;
    }

    private boolean isInStaffInfoButton(float x, float y) {
        return isInRect(x, y, staffInfoBtnX, staffInfoBtnY, staffInfoBtnSize, staffInfoBtnSize);
    }

    private boolean isInMergeInfoButton(float x, float y) {
        return isInRect(x, y, mergeInfoBtnX, mergeInfoBtnY, mergeInfoBtnSize, mergeInfoBtnSize);
    }

    private boolean isInInventoryInfoButton(float x, float y) {
        return isInRect(x, y, inventoryInfoBtnX, inventoryInfoBtnY, inventoryInfoBtnSize, inventoryInfoBtnSize);
    }

    private boolean isInAnyInfoPanel(float x, float y) {
        return (staffInfoOpen || mergeInfoOpen || inventoryInfoOpen) &&
                x >= infoPanelX && x <= infoPanelX + infoPanelW &&
                y >= infoPanelY && y <= infoPanelY + infoPanelH;
    }

    private boolean isInRect(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private void update(float delta) {
        if (augmentChoiceActive)
            return;

        moveTimer -= delta;

        if (moveTimer <= 0) {
            int dx = 0, dz = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.W))
                dz = -1;
            else if (Gdx.input.isKeyPressed(Input.Keys.S))
                dz = 1;
            else if (Gdx.input.isKeyPressed(Input.Keys.A))
                dx = -1;
            else if (Gdx.input.isKeyPressed(Input.Keys.D))
                dx = 1;

            if (dx != 0 || dz != 0) {
                int newX = playerGridX + dx;
                int newZ = playerGridZ + dz;
                if (newX >= 0 && newX < Constants.MAP_WIDTH &&
                        newZ >= 0 && newZ < Constants.MAP_HEIGHT) {
                    float worldX = newX * Constants.TILE_SIZE;
                    float worldZ = newZ * Constants.TILE_SIZE;

                    if (!isPillarAt(worldX, worldZ)) {
                        playerGridX = newX;
                        playerGridZ = newZ;
                        player.moveTo(new Vector3(worldX, 0, worldZ), moveDelay);
                        moveTimer = moveDelay;
                    }
                }
            }
        }

        player.update(delta);
        staffUI.update(delta);

        coreBobTimer += delta;
        coreSphereInstance.transform.setToTranslation(
                coreBaseX,
                coreBaseY + com.badlogic.gdx.math.MathUtils.sin(coreBobTimer * 2f) * 0.5f,
                coreBaseZ).scl(coreScale);

        buildMenu.updateHover(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

        // Update WaveManager
        waveManager.update(delta);

        if (!waveManager.isWaveInProgress() && !waveManager.areAllWavesComplete()) {
            if (waveManager.getCurrentWave() > 0 && waveManager.getCurrentWave() % 10 == 0
                    && !waveManager.hasShownAugmentForWave(waveManager.getCurrentWave())) {
                showAugmentSelection();
                waveManager.setShownAugmentForWave(waveManager.getCurrentWave(), true);
                saveGameState();
            } else if (autoplayEnabled) {
                waveManager.startNextWave();
            }
        }

        // Update enemies
        for (com.td.game.entities.Enemy enemy : waveManager.getActiveEnemies()) {
            enemy.update(delta);
            if (enemy.hasReachedEnd()) {
                if (!enemy.isAllied()) {
                    economyManager.loseLife();
                    coreFlashTimer = 0.4f;
                }
            }
            if (!enemy.isAlive() && !enemy.hasReachedEnd()) {
                if (!enemy.isAllied()) {
                    economyManager.earn(enemy.getReward());
                    
                    // Check for LIFE pillar revive
                    for (Pillar p : pillars) {
                        if (p.isActive() && p.getCurrentElement() == Element.LIFE) {
                            if (p.getPosition().dst(enemy.getPosition()) < p.getAttackRange()) {
                                reviveAsAlly(enemy);
                                break;
                            }
                        }
                    }
                }
            }
        }
        waveManager.removeDeadEnemies();

        // Update Staff Aura Buffs
        updateStaffAuraBuffs();

        // Update pillars and Gold passive
        for (Pillar pillar : pillars) {
            pillar.update(delta, waveManager.getActiveEnemies(), activeProjectiles);
            if (pillar.isActive() && pillar.getCurrentElement() == Element.GOLD) {
                // Gold passive: 1 gold per second per level? Let's say 2 gold/sec base.
                economyManager.earn(2.0f * delta);
            }
        }

        // Update Projectiles
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            com.td.game.entities.Projectile proj = activeProjectiles.get(i);
            proj.update(delta);
            if (proj.isImpacted()) {
                handleProjectileImpact(proj);
                activeProjectiles.removeIndex(i);
            } else if (!proj.isAlive()) {
                activeProjectiles.removeIndex(i);
            }
        }

        // Update Effects
        for (int i = activeEffects.size - 1; i >= 0; i--) {
            activeEffects.get(i).update(delta);
            if (!activeEffects.get(i).isAlive()) {
                activeEffects.removeIndex(i);
            }
        }

        // Check game over conditions
        if (economyManager.isGameOver()) {
            gameOver = true;
            com.td.game.systems.SaveManager.deleteSave(mapType);
            return;
        }
        if (waveManager.areAllWavesComplete() && waveManager.getAliveEnemyCount() == 0) {
            gameWon = true;
            com.td.game.systems.SaveManager.deleteSave(mapType);
            return;
        }

        HoverDetector.HoverState hoverState = HoverDetector.detect(camera, Gdx.input.getX(), Gdx.input.getY(),
                shopWidth,
                player, pillars, buildMenu);
        hoveredPillar = hoverState.hoveredPillar;
        hoveringAlchemist = hoverState.hoveringAlchemist;
        updatePillarMultipliers();
    }

    private boolean hoveringAlchemist = false;

    private void buyOrb(int index) {
        int price = shop.getOrbPrice(index);
        Element element = shop.getOrbElement(index);
        if (economyManager.canAfford(price)) {
            if (inventory.addOrb(element)) {
                economyManager.spend(price);
            } else {
                showMessage("Inventory Full!");
            }
        } else {
            showMessage("Not enough gold!");
        }
    }

    private class GameInputProcessor extends com.badlogic.gdx.InputAdapter {

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.GRAVE) {
                toggleConsole();
                return true;
            }
            if (com.td.game.input.KeyBindings.handleShortcutKeys(keycode, game, mapType, GameScreen.this)) {
                return true;
            }
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) {
                paused = !paused;
                return true;
            }
            if (augmentChoiceActive) {
                if (keycode == Input.Keys.NUM_1) {
                    applyAugmentChoice(1);
                    return true;
                }
                if (keycode == Input.Keys.NUM_2) {
                    applyAugmentChoice(2);
                    return true;
                }
                return true;
            }

            if (keycode == Input.Keys.SPACE && !waveManager.isWaveInProgress()) {
                waveManager.startNextWave();
                return true;
            }

            if (keycode == Input.Keys.ESCAPE) {
                if (paused) {
                    paused = false;
                    return true;
                }
                if (buildMenu.isActive()) {
                    buildMenu.hide();
                    selectedTilePos = null;
                } else if (selectedPillar != null) {
                    selectedPillar = null;
                    awaitingPillarOrbSelection = false;
                } else if (inventory.hasSelection()) {
                    inventory.cancelSelection();
                } else {
                    saveGameState();
                    Gdx.app.exit();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            int flippedY = Gdx.graphics.getHeight() - screenY;

            if (consoleOpen && button == Input.Buttons.LEFT) {
                int consoleButtonIndex = getConsoleButtonIndexAt(screenX, flippedY, Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight());
                if (consoleButtonIndex >= 0) {
                    handleConsoleCommand(CONSOLE_BUTTON_COMMANDS[consoleButtonIndex]);
                    return true;
                }
                if (isInConsoleArea(screenX, flippedY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())) {
                    return true;
                }
            }

            if (gameOver && button == Input.Buttons.LEFT) {
                float sw = Gdx.graphics.getWidth();
                float sh = Gdx.graphics.getHeight();
                float btnW = 300 * uiScale;
                float btnH = 60 * uiScale;
                float btnX = (sw - btnW) / 2;
                float menuY = sh / 2 - btnH / 2;
                if (isInRect(screenX, flippedY, btnX, menuY, btnW, btnH)) {
                    game.audio.playClick();
                    game.audio.playMenuMusic();
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                }
                return true;
            }

            if (gameWon && button == Input.Buttons.LEFT) {
                float sw = Gdx.graphics.getWidth();
                float sh = Gdx.graphics.getHeight();
                float btnW = 300 * uiScale;
                float btnH = 60 * uiScale;
                float btnX = (sw - btnW) / 2;
                float freeY = sh / 2 + 10 * uiScale;
                float menuY = sh / 2 - 70 * uiScale;

                if (isInRect(screenX, flippedY, btnX, freeY, btnW, btnH)) {
                    gameWon = false;
                } else if (isInRect(screenX, flippedY, btnX, menuY, btnW, btnH)) {
                    game.audio.playClick();
                    game.audio.playMenuMusic();
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                }
                return true;
            }

            if (paused && button == Input.Buttons.LEFT) {
                float sw = Gdx.graphics.getWidth();
                float sh = Gdx.graphics.getHeight();
                float btnW = 300 * uiScale;
                float btnH = 50 * uiScale;
                float btnX = (sw - btnW) / 2;
                float gap = 12 * uiScale;
                float resumeY = sh / 2 + 110 * uiScale;
                float optionsY = resumeY - btnH - gap;
                float menuY = optionsY - btnH - gap;
                float quitY = menuY - btnH - gap;

                if (isInRect(screenX, flippedY, btnX, resumeY, btnW, btnH)) {
                    game.audio.playClick();
                    paused = false;
                } else if (isInRect(screenX, flippedY, btnX, optionsY, btnW, btnH)) {
                    game.audio.playClick();
                    game.setScreen(new OptionsScreen(game, (com.badlogic.gdx.Screen) GameScreen.this));
                    return true;
                } else if (isInRect(screenX, flippedY, btnX, menuY, btnW, btnH)) {
                    game.audio.playClick();
                    game.audio.playMenuMusic();
                    saveGameState();
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                } else if (isInRect(screenX, flippedY, btnX, quitY, btnW, btnH)) {
                    game.audio.playClick();
                    saveGameState();
                    Gdx.app.exit();
                }
                return true;
            }

            if (augmentChoiceActive && button == Input.Buttons.LEFT) {
                int sw = Gdx.graphics.getWidth();
                int sh = Gdx.graphics.getHeight();
                float boxW = 700 * uiScale;
                float boxH = 230 * uiScale;
                float boxX = (sw - boxW) * 0.5f;
                float boxY = (sh - boxH) * 0.5f;
                float cardW = (boxW - 60 * uiScale) * 0.5f;
                float cardH = 130 * uiScale;
                float cardY = boxY + 35 * uiScale;

                boolean inLeft = screenX >= boxX + 15 * uiScale && screenX <= boxX + 15 * uiScale + cardW &&
                        flippedY >= cardY && flippedY <= cardY + cardH;
                boolean inRight = screenX >= boxX + 30 * uiScale + cardW &&
                        screenX <= boxX + 30 * uiScale + cardW + cardW &&
                        flippedY >= cardY && flippedY <= cardY + cardH;

                if (inLeft) {
                    applyAugmentChoice(1);
                } else if (inRight) {
                    applyAugmentChoice(2);
                }
                return true;
            }
            if (augmentChoiceActive) {
                return true;
            }

            if (button == Input.Buttons.RIGHT) {
                if (buildMenu.isActive()) {
                    buildMenu.hide();
                    selectedTilePos = null;
                }
                selectedPillar = null;
                awaitingPillarOrbSelection = false;
                inventory.cancelSelection();
                return true;
            }

            if (button == Input.Buttons.LEFT) {
                if (showingAugmentsPanel) {
                    float sw = Gdx.graphics.getWidth();
                    float sh = Gdx.graphics.getHeight();
                    float boxW = 800 * uiScale;
                    float boxH = 400 * uiScale;
                    float boxX = (sw - boxW) * 0.5f;
                    float boxY = (sh - boxH) * 0.5f;

                    float closeY = boxY + 10 * uiScale;
                    if (flippedY >= closeY && flippedY <= closeY + 60 * uiScale && screenX >= boxX
                            && screenX <= boxX + boxW) {
                        showingAugmentsPanel = false;
                    }
                    return true;
                }

                if (isInRect(screenX, flippedY, seeAugmentsBtnX, seeAugmentsBtnY, seeAugmentsBtnW, seeAugmentsBtnH)) {
                    showingAugmentsPanel = !showingAugmentsPanel;
                    return true;
                }
                if (isInRect(screenX, flippedY, playBtnX, playBtnY, playBtnW, playBtnH)) {
                    if (!waveManager.isWaveInProgress()) {
                        if (waveManager.areAllWavesComplete()) {
                            gameWon = true;
                        } else if (!augmentChoiceActive) {
                            saveGameState();
                            waveManager.startNextWave();
                        }
                    }
                    return true;
                }
                if (isInRect(screenX, flippedY, autoplayBtnX, autoplayBtnY, autoplayBtnW, autoplayBtnH)) {
                    autoplayEnabled = !autoplayEnabled;
                    return true;
                }
                if (isInRect(screenX, flippedY, pauseIconX, pauseIconY, pauseIconSize, pauseIconSize)) {
                    paused = !paused;
                    return true;
                }
                if (isInRect(screenX, flippedY, speedIconX, speedIconY, speedIconSize, speedIconSize)) {
                    speedIndex = (speedIndex + 1) % SPEED_MULTIPLIERS.length;
                    showMessage(String.format("Speed: %dX", (int) SPEED_MULTIPLIERS[speedIndex]));
                    return true;
                }

                if (isInStaffInfoButton(screenX, flippedY)) {
                    staffInfoOpen = !staffInfoOpen;
                    mergeInfoOpen = false;
                    inventoryInfoOpen = false;
                    return true;
                }
                if (isInMergeInfoButton(screenX, flippedY)) {
                    mergeInfoOpen = !mergeInfoOpen;
                    staffInfoOpen = false;
                    inventoryInfoOpen = false;
                    return true;
                }
                if (isInInventoryInfoButton(screenX, flippedY)) {
                    inventoryInfoOpen = !inventoryInfoOpen;
                    staffInfoOpen = false;
                    mergeInfoOpen = false;
                    return true;
                }
                if (isInAnyInfoPanel(screenX, flippedY)) {
                    return true;
                }
                if (staffInfoOpen || mergeInfoOpen || inventoryInfoOpen) {
                    staffInfoOpen = false;
                    mergeInfoOpen = false;
                    inventoryInfoOpen = false;
                }

                if (buildMenu.isActive()) {
                    int selection = buildMenu.getSelection();
                    if (selection != -1) {
                        PillarType type = buildMenu.getSelectedType();
                        if (type != null && selectedTilePos != null) {
                            if (economyManager.canAfford(type.getPrice())) {
                                economyManager.spend(type.getPrice());
                                Pillar pillar = new Pillar(type, selectedTilePos.cpy(), modelFactory);
                                pillars.add(pillar);
                                buildMenu.hide();
                                selectedTilePos = null;
                            } else {
                                showMessage("Not enough gold!");
                            }
                        }
                    } else {
                        buildMenu.hide();
                        selectedTilePos = null;
                    }
                    return true;
                }

                if (isPillarPanelVisible()) {
                    int panelButton = getPillarPanelButton(screenX, flippedY);
                    if (panelButton == 0) {
                        Element removed = selectedPillar.removeOrb();
                        if (removed != null) {
                            // If inventory is full, orb is lost on sell.
                            inventory.addOrb(removed);
                        }
                        economyManager.earn(selectedPillar.getType().getPrice() / 2);
                        pillars.removeValue(selectedPillar, true);
                        selectedPillar = null;
                        awaitingPillarOrbSelection = false;
                        return true;
                    }
                    if (panelButton == 1) {
                        awaitingPillarOrbSelection = true;
                        showMessage("Pick an orb from inventory!");
                        return true;
                    }
                    if (panelButton == 2) {
                        Element removed = selectedPillar.removeOrb();
                        if (removed != null) {
                            if (!inventory.addOrb(removed)) {
                                selectedPillar.placeOrb(removed);
                                showMessage("Inventory Full!");
                            }
                        }
                        awaitingPillarOrbSelection = false;
                        return true;
                    }

                    if (!awaitingPillarOrbSelection && !isPointInPillarPanel(screenX, flippedY)) {
                        selectedPillar = null;
                        awaitingPillarOrbSelection = false;
                        if (screenX < Gdx.graphics.getWidth() - shopWidth) {
                            return true;
                        }
                    }
                }

                int slot = inventory.getSlotAt(screenX, flippedY);
                if (slot != -1) {
                    inventory.handleClick(screenX, flippedY);
                    if (awaitingPillarOrbSelection && selectedPillar != null && inventory.hasSelection()) {
                        Element selected = inventory.takeSelected();
                        selectedPillar.placeOrb(selected);
                        awaitingPillarOrbSelection = false;
                    }
                    return true;
                }

                int orbIndex = shop.getOrbIndexAt(screenX, flippedY);
                if (orbIndex != -1) {
                    buyOrb(orbIndex);
                    return true;
                }

                if (staffUI.contains(screenX, screenY)) {
                    if (inventory.hasSelection()) {
                        Element selected = inventory.takeSelected();
                        Element old = staffUI.equipOrb(selected);
                        if (old != null && !inventory.addOrb(old)) {
                            showMessage("Inventory Full!");
                            inventory.addOrb(selected);
                            staffUI.equipOrb(old);
                        } else {
                            int idx = staffUI.getEquippedElement().ordinal();
                            player.setStaffOrbModel(new ModelInstance(orbModels[idx]), staffUI.getEquippedElement());
                        }
                    } else {
                        Element orb = staffUI.removeOrb();
                        if (orb != null && !inventory.addOrb(orb)) {
                            showMessage("Inventory Full!");
                            staffUI.equipOrb(orb);
                        } else {
                            player.clearStaffOrb();
                        }
                    }
                    return true;
                }

                if (mergeBoard.isInBounds(screenX, flippedY)) {
                    if (inventory.hasSelection()) {
                        boolean willCompleteMerge = mergeBoard.hasSlot1() ^ mergeBoard.hasSlot2();
                        if (willCompleteMerge && !economyManager.canAfford(MERGE_COST)) {
                            showMessage("Need " + MERGE_COST + "G to merge!");
                            return true;
                        }

                        boolean hadResult = mergeBoard.hasResult();
                        if (mergeBoard.placeOrb(screenX, flippedY, inventory.getSelectedOrb())) {
                            inventory.takeSelected();
                            if (!hadResult && mergeBoard.hasResult()) {
                                economyManager.spend(MERGE_COST);
                            }
                        }
                    } else {

                        Element result = mergeBoard.tryTakeResult(screenX, flippedY);
                        if (result != null) {
                            if (!inventory.addOrb(result))
                                showMessage("Inventory Full!");
                        } else {
                            Element inputOrb = mergeBoard.tryTakeInputOrb(screenX, flippedY);
                            if (inputOrb != null && !inventory.addOrb(inputOrb)) {
                                showMessage("Inventory Full!");
                                mergeBoard.placeOrb(screenX, flippedY, inputOrb);
                            }
                        }
                    }
                    return true;
                }

                int mapWidth = (int) (Gdx.graphics.getWidth() - shopWidth);
                Ray ray = camera.getPickRay(screenX, screenY, 0, 0, mapWidth, Gdx.graphics.getHeight());

                float t = (0.1f - ray.origin.y) / ray.direction.y;
                if (t > 0) {
                    float worldX = ray.origin.x + ray.direction.x * t;
                    float worldZ = ray.origin.z + ray.direction.z * t;

                    int gx = Math.round(worldX / Constants.TILE_SIZE);
                    int gz = Math.round(worldZ / Constants.TILE_SIZE);
                    float snapX = gx * Constants.TILE_SIZE;
                    float snapZ = gz * Constants.TILE_SIZE;

                    Pillar clickedPillar = null;

                    for (Pillar pillar : pillars) {
                        Vector3 pos = pillar.getPosition();

                        if (Math.abs(pos.x - snapX) < 0.1f && Math.abs(pos.z - snapZ) < 0.1f) {
                            clickedPillar = pillar;
                            break;
                        }
                    }

                    if (clickedPillar != null) {
                        selectedPillar = clickedPillar;
                        awaitingPillarOrbSelection = false;
                        buildMenu.hide();
                        selectedTilePos = null;
                        return true;
                    }

                    if (!inventory.hasSelection()) {
                        int tileGx = Math.round(worldX / Constants.TILE_SIZE);
                        int tileGz = Math.round(worldZ / Constants.TILE_SIZE);
                        if (tileGx < 0 || tileGx >= Constants.MAP_WIDTH || tileGz < 0 || tileGz >= Constants.MAP_HEIGHT)
                            return true;
                        float sx = tileGx * Constants.TILE_SIZE;
                        float sz = tileGz * Constants.TILE_SIZE;

                        if (gameMap.isPathAt(sx, sz))
                            return true;

                        boolean occupied = false;
                        for (Pillar p : pillars)
                            if (p.getPosition().dst(sx, 0, sz) < 1f)
                                occupied = true;

                        float playerX = playerGridX * Constants.TILE_SIZE;
                        float playerZ = playerGridZ * Constants.TILE_SIZE;
                        if (Math.abs(playerX - sx) < 0.1f && Math.abs(playerZ - sz) < 0.1f) {
                            showMessage("Cannot build on Alchemist tile!");
                            return true;
                        }

                        if (!occupied) {
                            if (!waveManager.isWaveInProgress()) {
                                selectedTilePos = new Vector3(sx, 0, sz);
                                buildMenu.show(screenX, flippedY, true);
                            } else {
                                showMessage("Cannot build during a wave!");
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        uiScale = Math.min(width / 1920f, height / 1080f);
        shopWidth = calculateShopWidth(width);

        int mapAreaWidth = (int) (width - shopWidth);
        camera.viewportWidth = mapAreaWidth;
        camera.viewportHeight = height;
        camera.update();

        uiBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        uiShapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        uiFont.getData().setScale(uiScale * 0.54f);
        uiFontLarge.getData().setScale(uiScale * 0.72f);

        shop = new GameShop(width - shopWidth, 0, shopWidth, height);
        inventory.resize(width, height);
        float panelPadding = 10f * uiScale;
        float sectionW = shopWidth - panelPadding * 2f;
        float invSlot = inventory.getSlotSize();
        float invPad = inventory.getPadding();
        float orbGap = 5f * uiScale;
        float orbSlot = (sectionW - orbGap * 3f) / 4f;
        float orbY = height - 145f * uiScale;
        float staffSize = Math.min(sectionW, 170f * uiScale);
        float staffY = orbY - 64f * uiScale - staffSize;
        float mergeInfoY = staffY - 14f * uiScale;
        float mergeY = mergeInfoY - 52f * uiScale - invSlot;
        float invY = mergeY - 34f * uiScale - (invSlot * 2f + invPad);
        if (invY < 18f * uiScale) {
            invY = 18f * uiScale;
            mergeY = invY + (invSlot * 2f + invPad) + 34f * uiScale;
        }
        float invX = width - shopWidth + panelPadding;
        inventory.setPosition(invX, invY);

        float mergeX = width - shopWidth + panelPadding;
        float mergeW = shopWidth - (panelPadding * 2f);
        mergeBoard.setPosition(mergeX, mergeY);
        mergeBoard.setSlotSize(invSlot);

        float staffX = width - shopWidth + (shopWidth - staffSize) * 0.5f;
        if (staffUI == null) {
            staffUI = new WizardStaffUI((int) staffX, (int) staffY, (int) staffSize, (int) staffSize);
        } else {
            staffUI.setBounds((int) staffX, (int) staffY, (int) staffSize, (int) staffSize);
        }
    }

    private float calculateShopWidth(int screenWidth) {
        float target = screenWidth * 0.205f;
        return MathUtils.clamp(target, 285f, 340f);
    }

    private static com.badlogic.gdx.files.FileHandle resolveAsset(String name) {
        com.badlogic.gdx.files.FileHandle direct = Gdx.files.internal(name);
        if (direct.exists()) {
            return direct;
        }
        return Gdx.files.internal("assets/" + name);
    }

    private BitmapFont createGameScreenFont(int size) {
        com.badlogic.gdx.files.FileHandle f = resolveAsset("fonts/font_game_screen.ttf");
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(f);
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "\u00e7\u011f\u0131\u015f\u00f6\u00fc\u00c7\u011e\u0130\u015e\u00d6\u00dc";
        p.size = size;
        p.color = Color.WHITE;
        BitmapFont font = gen.generateFont(p);
        gen.dispose();
        return font;
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
        game.audio.stopMapMusic();
        gameMap.dispose();
        shop.dispose();
        inventory.dispose();
        staffUI.dispose();
        modelFactory.dispose();
        uiBatch.dispose();
        uiFont.dispose();
        if (uiFontLarge != null)
            uiFontLarge.dispose();
        uiShapeRenderer.dispose();
        buildMenu.dispose();
        if (playerModel != null)
            playerModel.dispose();
        for (Model m : orbModels)
            if (m != null)
                m.dispose();
        for (Pillar p : pillars)
            p.dispose();
        waveManager.dispose();
        economyManager.dispose();
        if (mapAreaBackgroundTexture != null)
            mapAreaBackgroundTexture.dispose();
        if (hudInfoIconTexture != null)
            hudInfoIconTexture.dispose();
        if (pauseMenuBackgroundTexture != null)
            pauseMenuBackgroundTexture.dispose();
        if (gateModel != null)
            gateModel.dispose();
        if (coreSphereModel != null)
            coreSphereModel.dispose();
        if (staffInfoPanelTexture != null)
            staffInfoPanelTexture.dispose();
        if (mergeInfoPanelTexture != null)
            mergeInfoPanelTexture.dispose();
        if (inventoryInfoPanelTexture != null)
            inventoryInfoPanelTexture.dispose();
        if (orbIconTextures != null) {
            HashSet<Texture> unique = new HashSet<>(orbIconTextures.values());
            for (Texture texture : unique) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            orbIconTextures.clear();
        }
        if (hudLifeIconTexture != null)
            hudLifeIconTexture.dispose();
        if (hudGoldIconTexture != null)
            hudGoldIconTexture.dispose();
    }

    private void updateHoveredEnemy(int screenWidth, int screenHeight, int mapAreaWidth) {
        hoveredEnemy = null;
        if (paused || gameOver || gameWon)
            return;

        float mx = Gdx.input.getX();
        float my = screenHeight - Gdx.input.getY();
        if (mx > mapAreaWidth)
            return;

        float closestDistSq = Float.MAX_VALUE;
        float hoverRadiusSq = (50f * uiScale) * (50f * uiScale);

        for (com.td.game.entities.Enemy enemy : waveManager.getActiveEnemies()) {
            if (!enemy.isAlive())
                continue;
            Vector3 pos = new Vector3(enemy.getPosition());
            pos.y += 1.0f;
            Vector3 screenPos = camera.project(pos.cpy(), 0, 0, mapAreaWidth, screenHeight);

            float dx = screenPos.x - mx;
            float dy = screenPos.y - my;
            float distSq = dx * dx + dy * dy;
            if (distSq < hoverRadiusSq && distSq < closestDistSq) {
                closestDistSq = distSq;
                hoveredEnemy = enemy;
            }
        }
    }

    private void renderEnemyHoverUI(int screenWidth, int screenHeight) {
        Vector3 pos = new Vector3(hoveredEnemy.getPosition());
        pos.y += 2.8f;
        Vector3 screenPos = camera.project(pos.cpy(), 0, 0, screenWidth - shopWidth, screenHeight);

        float px = screenPos.x;
        float py = screenPos.y;

        String enemyName = hoveredEnemy.getName();
        if ("Enemy".equals(enemyName)) {
            if (hoveredEnemy instanceof com.td.game.entities.DemonEnemy)
                enemyName = "Demon Boss";
            else if (hoveredEnemy instanceof com.td.game.entities.BatEnemy)
                enemyName = "Bat";
            else if (hoveredEnemy instanceof com.td.game.entities.GolemEnemy)
                enemyName = "Golem";
            else if (hoveredEnemy instanceof com.td.game.entities.PinkBlobEnemy)
                enemyName = "Pink Blob";
        }

        String elemName = (hoveredEnemy.getElement() != null) ? hoveredEnemy.getElement().getDisplayName()
                : "No Element";
        String hpStr = String.format("HP: %.0f / %.0f", Math.max(0, hoveredEnemy.getHealth()),
                hoveredEnemy.getMaxHealth());

        float panelW = 220f * uiScale;
        float panelH = 95f * uiScale;

        px -= panelW / 2;
        if (px + panelW > screenWidth)
            px = screenWidth - panelW - 4f;
        if (px < 0)
            px = 4f;
        if (py + panelH > screenHeight)
            py = screenHeight - panelH - 4f;
        if (py < 0)
            py = 4f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        uiShapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.85f);
        uiShapeRenderer.rect(px, py, panelW, panelH);

        // draw health bar inside panel
        float hpBarWidth = panelW - 20f * uiScale;
        float hpBarHeight = 8f * uiScale;
        float hpBarX = px + 10f * uiScale;
        float hpBarY = py + 12f * uiScale;

        uiShapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.9f);
        uiShapeRenderer.rect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);

        Color healthColor;
        if (hoveredEnemy.getElement() != null) {
            healthColor = new Color(hoveredEnemy.getElement().getR(), hoveredEnemy.getElement().getG(),
                    hoveredEnemy.getElement().getB(), 1f);
            if (hoveredEnemy.getArmor() > 0) {
                healthColor.mul(0.6f, 0.6f, 0.6f, 1f);
            }
        } else {
            float healthPercent = hoveredEnemy.getHealth() / hoveredEnemy.getMaxHealth();
            if (healthPercent > 0.5f) {
                healthColor = Color.GREEN;
            } else if (healthPercent > 0.25f) {
                healthColor = Color.YELLOW;
            } else {
                healthColor = Color.RED;
            }
        }

        uiShapeRenderer.setColor(healthColor);
        float hpWidth = hpBarWidth * (Math.max(0, hoveredEnemy.getHealth()) / hoveredEnemy.getMaxHealth());
        uiShapeRenderer.rect(hpBarX, hpBarY, hpWidth, hpBarHeight);

        uiShapeRenderer.end();

        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Color elemColor = (hoveredEnemy.getElement() != null) ? new Color(hoveredEnemy.getElement().getR(),
                hoveredEnemy.getElement().getG(), hoveredEnemy.getElement().getB(), 1f) : Color.WHITE;
        uiShapeRenderer.setColor(elemColor);
        uiShapeRenderer.rect(px, py, panelW, panelH);
        uiShapeRenderer.end();

        uiBatch.begin();
        float textX = px + 10f * uiScale;
        float textY = py + panelH - 10f * uiScale;
        float lineH = 24f * uiScale;

        uiFont.getData().setScale(uiScale * 0.8f);
        uiFont.setColor(Color.WHITE);
        uiFont.draw(uiBatch, enemyName, textX, textY);
        textY -= lineH;

        uiFont.setColor(elemColor);
        uiFont.draw(uiBatch, elemName, textX, textY);
        textY -= lineH;

        uiFont.setColor(Color.WHITE);
        // Moved the HP text slightly up to avoid overlapping with the health bar
        uiFont.draw(uiBatch, hpStr, textX, textY + 5f * uiScale);

        uiFont.getData().setScale(uiScale);
        uiBatch.end();
    }

    private void loadEffectTextures() {
        String[] types = { "fire", "water", "air", "ice", "poison", "light", "steam" };
        for (String type : types) {
            String path = "textures/attacks/" + type + "_effect.png";
            com.badlogic.gdx.files.FileHandle file = resolveAsset(path);
            if (file.exists()) {
                effectTextures.put(Element.valueOf(type.toUpperCase(java.util.Locale.ROOT)), new Texture(file));
            }
        }
    }

    private void handleProjectileImpact(com.td.game.entities.Projectile proj) {
        com.td.game.entities.Enemy target = proj.getTargetEnemy();
        if (target == null || !target.isAlive())
            return;

        Element element = proj.getElement();
        float damage = proj.getDamage();

        target.takeDamage(damage, element);

        // Spawn impact effect
        spawnEffect(target.getPosition(), element, 0.5f, 1.0f);

        // Element-specific effects
        switch (element) {
            case FIRE:
                // AoE Damage (radius 3.0)
                for (com.td.game.entities.Enemy e : waveManager.getActiveEnemies()) {
                    if (e != target && e.isAlive() && e.getPosition().dst(target.getPosition()) < 3.0f) {
                        e.takeDamage(damage * 0.5f, element);
                    }
                }
                target.applyBurn(3f, damage * 0.2f);
                break;
            case WATER:
                target.applySlow(3f, 0.5f);
                break;
            case AIR:
                target.applyKnockback(2.0f);
                break;
            case EARTH:
                target.applyRoot(2f, 0.5f, 0f); // Stun/Root
                break;
            case ICE:
                target.applyFreeze(2f);
                break;
            case POISON:
                target.applyPoison(5f, damage * 0.1f, 1);
                target.applyRegenBlock(5f);
                break;
            case STEAM:
                float hpPercent = target.getHealth() / target.getMaxHealth();
                float kbDist = 1.0f + (1.0f - hpPercent) * 3.0f;
                target.applyKnockback(kbDist);
                break;
            default:
                break;
        }
    }

    private void spawnEffect(Vector3 pos, Element element, float lifetime, float scale) {
        Texture tex = effectTextures.get(element);
        if (tex != null) {
            activeEffects.add(new com.td.game.entities.Effect(pos, tex, lifetime, scale * uiScale));
        }
    }

    private void reviveAsAlly(com.td.game.entities.Enemy deadEnemy) {
        com.td.game.entities.Enemy ally;
        if (deadEnemy instanceof com.td.game.entities.DemonEnemy) {
            ally = new com.td.game.entities.DemonEnemy(deadEnemy.getMaxHealth() * 0.5f, 1.0f, 0);
            ally.setModel(demonModel);
        } else if (deadEnemy instanceof com.td.game.entities.GolemEnemy) {
            ally = new com.td.game.entities.GolemEnemy(deadEnemy.getMaxHealth() * 0.5f, 1.0f, 0);
            ally.setModel(golemModel);
        } else if (deadEnemy instanceof com.td.game.entities.BatEnemy) {
            ally = new com.td.game.entities.BatEnemy(deadEnemy.getMaxHealth() * 0.5f, 1.0f, 0);
            ally.setModel(batModel);
        } else {
            ally = new com.td.game.entities.PinkBlobEnemy(deadEnemy.getMaxHealth() * 0.5f, 1.0f, 0);
            ally.setModel(pinkBlobModel);
        }

        // Set waypoints WITHOUT resetting position (setWaypoints resets to first)
        // Manually wire up for backwards travel from end of path
        ally.setWaypoints(pathWaypoints); // sets waypoints + positions at start
        ally.setAllied(true);
        ally.setMovingBackwards(true);
        ally.setCurrentWaypointIndex(pathWaypoints.size - 1);
        // Override position to end of path
        if (pathWaypoints.size > 0) {
            ally.getPosition().set(pathWaypoints.get(pathWaypoints.size - 1));
        }

        waveManager.getActiveEnemies().add(ally);
        spawnEffect(ally.getPosition(), Element.LIFE, 1.0f, 1.5f);
    }

    private void updateStaffAuraBuffs() {
        Vector3 alchemistPos = new Vector3(playerGridX * Constants.TILE_SIZE, 0, playerGridZ * Constants.TILE_SIZE);
        float radius = staffAuraRadius * Constants.TILE_SIZE;
        Element equipped = staffUI.getEquippedElement();

        for (Pillar p : pillars) {
            if (p.getPosition().dst(alchemistPos) < radius) {
                float dmgBuff = 1f;
                float spdBuff = 1f;
                float rngBuff = 1f;

                if (equipped != null) {
                    switch (equipped) {
                        case LIGHT: spdBuff = 1.25f; break; // +25% Speed
                        case FIRE: dmgBuff = 1.25f; break; // +25% Damage
                        case WATER: rngBuff = 1.25f; break; // +25% Range
                        case LIFE: dmgBuff = 1.5f; break; // Massive Damage Bonus
                        default: break;
                    }
                }
                p.setExternalMultipliers(dmgBuff, rngBuff, spdBuff);
            } else {
                p.setExternalMultipliers(1f, 1f, 1f);
            }
        }
    }

}
