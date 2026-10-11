package com.lobsterchops.redshell.scene;

/** How a scene lets the scene below it behave while it is on top of the stack. */
public final class SceneFlags {

    /** Hides and freezes everything below. */
    public static final SceneFlags OPAQUE = new SceneFlags(false, false);
    /** Draws the scene below frozen, for example a pause menu over a level. */
    public static final SceneFlags OVERLAY = new SceneFlags(false, true);
    /** Keeps the scene below running and visible. */
    public static final SceneFlags TRANSPARENT = new SceneFlags(true, true);

    private final boolean updateBelow;
    private final boolean renderBelow;

    public SceneFlags(boolean updateBelow, boolean renderBelow) {
        this.updateBelow = updateBelow;
        this.renderBelow = renderBelow;
    }

    public boolean updateBelow() {
        return updateBelow;
    }

    public boolean renderBelow() {
        return renderBelow;
    }
}