package com.lobsterchops.redshell.physics;

import com.lobsterchops.redshell.physics.collision.Body;
import com.lobsterchops.redshell.physics.collision.Raycast;
import java.util.List;

/** Spatial queries against the collision world of the running scene. */
public interface PhysicsService {

    /** Nearest hit along the ray among bodies on the given layers, or null. */
    Raycast.Hit raycast(float originX, float originY, float directionX, float directionY,
                        float maxDistance, int layerMask);

    /** Appends bodies whose bounds overlap the region and whose layer bit is in {@code layerMask}. */
    void overlap(float minX, float minY, float maxX, float maxY, int layerMask, List<Body> out);
}