package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.AnimationComponent;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.entity.prefab.Prefab;
import com.lobsterchops.redshell.rendering.animation.Animation2D;
import com.lobsterchops.redshell.rendering.animation.AnimationController;
import com.lobsterchops.redshell.rendering.animation.LoopMode;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;
import com.lobsterchops.redshell.rendering.sprite.SpriteSheet;

/** The player: a sprite with idle and run animations. The sheet is read when the prefab is spawned. */
final class PlayerPrefab implements Prefab {

    static final String IDLE = "idle";
    static final String RUN = "run";

    private final AssetService assets;

    PlayerPrefab(AssetService assets) {
        this.assets = assets;
    }

    @Override
    public void build(Entity entity) {
        SpriteSheet sheet = SpriteSheet.fromAsset(assets, SandboxAssets.PLAYER, 32, 32);
        AnimationController controller = new AnimationController()
                .addState(IDLE, new Animation2D(IDLE, sheet.row(0, 2), 0.5f, LoopMode.LOOP))
                .addState(RUN, new Animation2D(RUN, sheet.row(1, 4), 0.1f, LoopMode.LOOP));
        controller.play(IDLE);
        entity.add(new TransformComponent(0f, 0f));
        entity.add(new VelocityComponent());
        entity.add(new SpriteComponent(controller.currentFrame(), 32f, 32f, RenderLayer.ACTORS));
        entity.add(new AnimationComponent(controller));
        entity.add(ColliderComponent.box(18f, 18f, SandboxLayers.PLAYER));
        entity.addTag(EntityTag.PLAYER);
    }
}