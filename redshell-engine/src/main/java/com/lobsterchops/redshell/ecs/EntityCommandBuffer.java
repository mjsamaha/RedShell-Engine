package com.lobsterchops.redshell.ecs;

import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.Component;
import com.lobsterchops.redshell.entity.prefab.Prefab;
import java.util.ArrayList;
import java.util.List;

/** Deferred structural changes. Systems queue them while iterating; the world applies them afterwards. */
public final class EntityCommandBuffer {

    private interface Command {
        void apply(World world);
    }

    private final List<Command> commands = new ArrayList<>();

    /** Queues creation of an entity built by the prefab. */
    public void spawn(final Prefab prefab) {
        commands.add(new Command() {
            @Override
            public void apply(World world) {
                world.spawn(prefab);
            }
        });
    }

    public void destroy(final Entity entity) {
        commands.add(new Command() {
            @Override
            public void apply(World world) {
                if (entity.isAlive()) {
                    world.destroy(entity);
                }
            }
        });
    }

    public void add(final Entity entity, final Component component) {
        commands.add(new Command() {
            @Override
            public void apply(World world) {
                if (entity.isAlive()) {
                    entity.add(component);
                }
            }
        });
    }

    public void remove(final Entity entity, final Class<? extends Component> type) {
        commands.add(new Command() {
            @Override
            public void apply(World world) {
                if (entity.isAlive()) {
                    entity.remove(type);
                }
            }
        });
    }

    public int pending() {
        return commands.size();
    }

    /** Applies and clears all queued commands. Called by the world between systems. */
    void flush(World world) {
        if (commands.isEmpty()) {
            return;
        }
        List<Command> batch = new ArrayList<>(commands);
        commands.clear();
        for (int i = 0; i < batch.size(); i++) {
            batch.get(i).apply(world);
        }
    }
}