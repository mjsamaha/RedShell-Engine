package com.lobsterchops.redshell.physics.collision;

/** Receives each candidate pair found by the broad phase, exactly once. */
public interface PairVisitor {

    void visit(Body a, Body b);
}