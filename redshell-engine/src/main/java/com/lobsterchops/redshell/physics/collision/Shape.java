package com.lobsterchops.redshell.physics.collision;

/** A 2D collision shape positioned by its centre. */
public interface Shape {

    float centerX();

    float centerY();

    void setCenter(float x, float y);

    float minX();

    float minY();

    float maxX();

    float maxY();
}