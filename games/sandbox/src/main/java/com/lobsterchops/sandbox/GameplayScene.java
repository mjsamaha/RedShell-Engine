package com.lobsterchops.sandbox;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.audio.AudioService;
import com.lobsterchops.redshell.ecs.system.AnimationSystem;
import com.lobsterchops.redshell.ecs.system.CameraFollowSystem;
import com.lobsterchops.redshell.ecs.system.MovementSystem;
import com.lobsterchops.redshell.ecs.system.RenderSystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.physics.CollisionSystem;
import com.lobsterchops.redshell.physics.collision.ContactEvents;
import com.lobsterchops.redshell.events.EventListener;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.rendering.camera.CameraController;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.ui.hud.HudCounter;
import com.lobsterchops.redshell.ui.hud.HudPrompt;
import com.lobsterchops.redshell.ui.hud.HudScreen;

/**
 * A walled tile map with coins to collect and patrolling hazards. Coins are trigger colliders: touching one
 * publishes a {@link SandboxEvents.CoinCollected}, which the HUD and the audio react to.
 */
final class GameplayScene extends Scene {

    private static final int COLUMNS = 30;
    private static final int ROWS = 20;
    private static final float TILE = 32f;
    private static final float START_X = 5.5f * TILE;
    private static final float START_Y = 10.5f * TILE;
    private static final int[][] COINS = {
        {3, 3}, {7, 16}, {13, 6}, {13, 10}, {17, 3}, {22, 10}, {26, 4}, {26, 16}, {18, 17}, {5, 8}, {12, 16}
    };
    private static final InputContext CONTEXT = new InputContext("gameplay", false,
            SandboxAction.MOVE_LEFT, SandboxAction.MOVE_RIGHT, SandboxAction.MOVE_UP, SandboxAction.MOVE_DOWN,
            SandboxAction.PAUSE);

    private final SandboxAssets assets;
    private Entity player;
    private HudScreen hud;
    private int collected;

    GameplayScene(SandboxAssets assets) {
        this.assets = assets;
    }

    @Override
    public AssetGroup assets() {
        return assets.gameplay();
    }

    @Override
    public InputContext inputContext() {
        return CONTEXT;
    }

    @Override
    protected void onEnter() {
        buildLevel();
        player = world().spawn(context().prefabs().get("player"));
        player.get(TransformComponent.class).x = START_X;
        player.get(TransformComponent.class).y = START_Y;

        CollisionSystem collision = new CollisionSystem(SandboxLayers.matrix(), context().events());
        world().addSystem(new PlayerControlSystem(context().input()));
        world().addSystem(new PatrolSystem());
        world().addSystem(new MovementSystem());
        world().addSystem(collision);
        world().addSystem(new AnimationSystem());
        world().addSystem(new RenderSystem());

        Renderer2D renderer = context().renderer();
        if (renderer != null) {
            CameraController controller = new CameraController(renderer.camera(), renderer.worldWidth(),
                    renderer.worldHeight());
            controller.setBounds(0f, 0f, COLUMNS * TILE, ROWS * TILE);
            controller.setSmoothing(8f);
            controller.snapTo(START_X, START_Y);
            world().addSystem(new CameraFollowSystem(controller, EntityTag.PLAYER));
        }

        buildHud();
        subscribeToContacts();
    }

    private void buildLevel() {
        TextureRegion floor = region(SandboxAssets.TILE);
        TextureRegion wall = region(SandboxAssets.WALL);
        TextureRegion coin = region(SandboxAssets.COIN);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                Entity tile = world().createEntity();
                tile.add(new TransformComponent(centre(column), centre(row)));
                tile.add(new SpriteComponent(floor, TILE, TILE, RenderLayer.BACKGROUND));
                if (isWall(column, row)) {
                    tile.add(new SpriteComponent(wall, TILE, TILE, RenderLayer.BACKGROUND));
                    tile.add(solid());
                }
            }
        }
        for (int i = 0; i < COINS.length; i++) {
            Entity entity = world().createEntity();
            entity.add(new TransformComponent(centre(COINS[i][0]), centre(COINS[i][1])));
            entity.add(new SpriteComponent(coin, 24f, 24f, RenderLayer.ACTORS));
            ColliderComponent trigger = ColliderComponent.circle(10f, SandboxLayers.COIN);
            trigger.trigger = true;
            trigger.immovable = true;
            entity.add(trigger);
            entity.add(new CoinComponent());
        }
        addHazard(16, 8, 12, 19);
        addHazard(8, 17, 3, 11);
    }

    private void addHazard(int column, int row, int fromColumn, int toColumn) {
        Entity hazard = world().createEntity();
        hazard.add(new TransformComponent(centre(column), centre(row)));
        hazard.add(new VelocityComponent());
        hazard.add(new SpriteComponent(region(SandboxAssets.ENEMY), TILE, TILE, RenderLayer.ACTORS));
        ColliderComponent trigger = ColliderComponent.box(22f, 18f, SandboxLayers.HAZARD);
        trigger.trigger = true;
        hazard.add(trigger);
        hazard.add(new HazardComponent(centre(fromColumn), centre(toColumn), 50f));
        hazard.addTag(EntityTag.ENEMY);
    }

    private static ColliderComponent solid() {
        ColliderComponent collider = ColliderComponent.box(TILE, TILE, SandboxLayers.WALL);
        collider.immovable = true;
        return collider;
    }

    private static boolean isWall(int column, int row) {
        boolean border = column == 0 || row == 0 || column == COLUMNS - 1 || row == ROWS - 1;
        boolean vertical = column == 10 && row >= 4 && row <= 12;
        boolean horizontal = row == 14 && column >= 15 && column <= 24;
        boolean block = column >= 20 && column <= 21 && row >= 6 && row <= 7;
        return border || vertical || horizontal || block;
    }

    private static float centre(int index) {
        return index * TILE + TILE / 2f;
    }

    private TextureRegion region(String assetId) {
        return new TextureRegion(context().assets().get(assetId, Texture.class));
    }

    private HudCounter counter;
    private HudPrompt prompt;

    private void buildHud() {
        hud = new HudScreen();
        counter = hud.counter("Coins: ", 0);
        prompt = hud.prompt("All coins collected!");
        prompt.setVisible(false);
        Services.ui(context()).add(hud);
    }

    private void subscribeToContacts() {
        subscribe(ContactEvents.TriggerEntered.class, new EventListener<ContactEvents.TriggerEntered>() {
            @Override
            public void onEvent(ContactEvents.TriggerEntered event) {
                Entity other = event.other();
                Entity trigger = event.trigger();
                if (!other.hasTag(EntityTag.PLAYER)) {
                    return;
                }
                if (trigger.has(CoinComponent.class)) {
                    world().commands().destroy(trigger);
                    collected++;
                    context().events().publish(new SandboxEvents.CoinCollected(collected, COINS.length));
                } else if (trigger.has(HazardComponent.class)) {
                    hurt();
                }
            }
        });
        subscribe(SandboxEvents.CoinCollected.class, new EventListener<SandboxEvents.CoinCollected>() {
            @Override
            public void onEvent(SandboxEvents.CoinCollected event) {
                counter.setValue(event.collected());
                prompt.setVisible(event.collected() == event.total());
                play(SandboxSounds.COIN);
            }
        });
        subscribe(SandboxEvents.PlayerHurt.class, new EventListener<SandboxEvents.PlayerHurt>() {
            @Override
            public void onEvent(SandboxEvents.PlayerHurt event) {
                play(SandboxSounds.HIT);
            }
        });
    }

    private void hurt() {
        TransformComponent transform = player.get(TransformComponent.class);
        transform.x = START_X;
        transform.y = START_Y;
        context().events().publish(new SandboxEvents.PlayerHurt());
    }

    private void play(String soundId) {
        AudioService audio = Services.audio(context());
        if (audio != null) {
            audio.playSound(soundId);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (context().input().pressed(SandboxAction.PAUSE)) {
            context().scenes().push(new PauseScene(assets));
        }
    }

    @Override
    protected void onDispose() {
        Services.ui(context()).remove(hud);
    }
}