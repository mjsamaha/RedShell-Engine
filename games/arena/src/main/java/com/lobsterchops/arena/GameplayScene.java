package com.lobsterchops.arena;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.ecs.system.AnimationSystem;
import com.lobsterchops.redshell.ecs.system.MovementSystem;
import com.lobsterchops.redshell.ecs.system.RenderSystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.AnimationComponent;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.events.EventListener;
import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.physics.CollisionSystem;
import com.lobsterchops.redshell.physics.collision.ContactEvents;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.rendering.animation.Animation2D;
import com.lobsterchops.redshell.rendering.animation.AnimationController;
import com.lobsterchops.redshell.rendering.animation.LoopMode;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;
import com.lobsterchops.redshell.rendering.sprite.SpriteSheet;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.transition.FadeTransition;
import com.lobsterchops.redshell.ui.hud.HudCounter;
import com.lobsterchops.redshell.ui.hud.HudScreen;

/** The arena: shoot chasers, lose a life when one reaches you, three lives per run. */
final class GameplayScene extends Scene {

    private static final float TILE = 32f;
    private static final InputContext CONTEXT = new InputContext("gameplay", false,
            ArenaAction.MOVE_LEFT, ArenaAction.MOVE_RIGHT, ArenaAction.MOVE_UP, ArenaAction.MOVE_DOWN,
            ArenaAction.FIRE_LEFT, ArenaAction.FIRE_RIGHT, ArenaAction.FIRE_UP, ArenaAction.FIRE_DOWN);

    private final ArenaAssets assets;
    private HudScreen hud;
    private HudCounter scoreCounter;
    private HudCounter livesCounter;
    private int score;
    private int lives = ArenaConfig.LIVES;
    private boolean over;

    GameplayScene(ArenaAssets assets) {
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
        Renderer2D renderer = context().renderer();
        if (renderer != null) {
            renderer.camera().setPosition(ArenaConfig.WIDTH / 2f, ArenaConfig.HEIGHT / 2f);
            renderer.camera().setZoom(1f);
            renderer.camera().update();
        }
        buildFloor();
        spawnPlayer();
        TextureRegion floor = region(ArenaAssets.FLOOR);
        world().addSystem(new PlayerControlSystem(context().input()));
        world().addSystem(new ShootingSystem(context().input(), floor));
        world().addSystem(new SpawnSystem(region(ArenaAssets.ENEMY)));
        world().addSystem(new ChaseSystem());
        world().addSystem(new MovementSystem());
        world().addSystem(new CollisionSystem(ArenaLayers.matrix(), context().events()));
        world().addSystem(new BoundsSystem());
        world().addSystem(new AnimationSystem());
        world().addSystem(new RenderSystem());

        hud = new HudScreen();
        scoreCounter = hud.counter("Score: ", 0);
        livesCounter = hud.counter("Lives: ", lives);
        Services.ui(context()).add(hud);
        subscribeToContacts();
    }

    private void buildFloor() {
        TextureRegion floor = region(ArenaAssets.FLOOR);
        for (int row = 0; row < 12; row++) {
            for (int column = 0; column < 20; column++) {
                Entity tile = world().createEntity();
                tile.add(new TransformComponent(column * TILE + TILE / 2f, row * TILE + TILE / 2f));
                tile.add(new SpriteComponent(floor, TILE, TILE, RenderLayer.BACKGROUND));
            }
        }
    }

    private void spawnPlayer() {
        SpriteSheet sheet = SpriteSheet.fromAsset(context().assets(), ArenaAssets.PLAYER, 32, 32);
        AnimationController controller = new AnimationController()
                .addState("idle", new Animation2D("idle", sheet.row(0, 2), 0.5f, LoopMode.LOOP))
                .addState("run", new Animation2D("run", sheet.row(1, 4), 0.1f, LoopMode.LOOP));
        controller.play("idle");
        Entity player = world().createEntity();
        player.add(new TransformComponent(ArenaConfig.WIDTH / 2f, ArenaConfig.HEIGHT / 2f));
        player.add(new VelocityComponent());
        player.add(new SpriteComponent(controller.currentFrame(), 32f, 32f, RenderLayer.ACTORS));
        player.add(new AnimationComponent(controller));
        player.add(ColliderComponent.box(18f, 18f, ArenaLayers.PLAYER));
        player.addTag(EntityTag.PLAYER);
    }

    private TextureRegion region(String assetId) {
        return new TextureRegion(context().assets().get(assetId, Texture.class));
    }

    private void subscribeToContacts() {
        subscribe(ContactEvents.TriggerEntered.class, new EventListener<ContactEvents.TriggerEntered>() {
            @Override
            public void onEvent(ContactEvents.TriggerEntered event) {
                handle(event.trigger(), event.other());
                handle(event.other(), event.trigger());
            }
        });
    }

    /** Handles one ordering of a contact pair; the caller tries both orders. */
    private void handle(Entity first, Entity second) {
        Enemy enemy = second.get(Enemy.class);
        if (enemy == null || enemy.dead || over) {
            return;
        }
        Bullet bullet = first.get(Bullet.class);
        if (bullet != null && !bullet.spent) {
            bullet.spent = true;
            enemy.dead = true;
            world().commands().destroy(first);
            world().commands().destroy(second);
            score++;
            scoreCounter.setValue(score);
            Services.play(context(), ArenaSounds.KILL);
        } else if (first.hasTag(EntityTag.PLAYER)) {
            enemy.dead = true;
            world().commands().destroy(second);
            lives--;
            livesCounter.setValue(lives);
            Services.play(context(), ArenaSounds.HIT);
            if (lives <= 0) {
                over = true;
                context().scenes().load(new GameOverScene(assets, score), new FadeTransition(0.5f));
            }
        }
    }

    @Override
    protected void onDispose() {
        Services.ui(context()).remove(hud);
    }
}