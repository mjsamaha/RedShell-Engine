package com.lobsterchops.redshell.entity.component;

/** Linear velocity in world units per second. */
public final class VelocityComponent implements Component {

    public float vx;
    public float vy;

    public VelocityComponent() {
    }

    public VelocityComponent(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }
}