package com.lobsterchops.arena;

import com.lobsterchops.redshell.entity.component.Component;

/** A player shot. {@code spent} stops one bullet hitting two enemies in one frame. */
final class Bullet implements Component {
    boolean spent;
}