package com.lobsterchops.redshell.ecs.system;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.rendering.RenderContext;
import com.lobsterchops.redshell.rendering.layer.LayerSorter;
import com.lobsterchops.redshell.rendering.sprite.Sprite2D;
import java.util.ArrayList;
import java.util.List;

/** Collects every visible sprite, orders them by layer then Y, and draws them. Reuses its sprite pool. */
public final class RenderSystem extends EntitySystem {

    private final Query query = Query.all(TransformComponent.class, SpriteComponent.class);
    private final List<Sprite2D> pool = new ArrayList<>();
    private final List<Sprite2D> visible = new ArrayList<>();

    public RenderSystem() {
        super(SystemPriority.RENDER);
    }

    @Override
    public void render(World world, RenderContext context) {
        List<Entity> entities = world.query(query);
        visible.clear();
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            SpriteComponent sprite = entity.get(SpriteComponent.class);
            if (!sprite.visible || sprite.region == null) {
                continue;
            }
            TransformComponent transform = entity.get(TransformComponent.class);
            Sprite2D draw = acquire(visible.size());
            draw.region = sprite.region;
            draw.x = transform.worldX();
            draw.y = transform.worldY();
            draw.width = sprite.width;
            draw.height = sprite.height;
            draw.scaleX = transform.scaleX;
            draw.scaleY = transform.scaleY;
            draw.rotationDegrees = transform.rotationDegrees;
            draw.flipX = sprite.flipX;
            draw.flipY = sprite.flipY;
            draw.red = sprite.red;
            draw.green = sprite.green;
            draw.blue = sprite.blue;
            draw.alpha = sprite.alpha;
            draw.layer = sprite.layer;
            draw.zIndex = sprite.zIndex;
            draw.sortY = draw.y - sprite.height / 2f;
            visible.add(draw);
        }
        LayerSorter.sort(visible);
        context.renderer().drawAll(visible);
    }

    private Sprite2D acquire(int index) {
        if (index == pool.size()) {
            pool.add(new Sprite2D());
        }
        return pool.get(index);
    }
}