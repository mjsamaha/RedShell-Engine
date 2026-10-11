package com.lobsterchops.arena;

import com.lobsterchops.redshell.input.action.Action;

/** Twin-stick controls: WASD moves, the arrow keys fire in that direction. */
public enum ArenaAction implements Action {
    MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN, FIRE_LEFT, FIRE_RIGHT, FIRE_UP, FIRE_DOWN, CONFIRM
}