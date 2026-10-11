package com.lobsterchops.redshell.input.action;

/** A named logical input such as MOVE_LEFT or PAUSE. Games usually implement it with an enum. */
public interface Action {

    String name();
}