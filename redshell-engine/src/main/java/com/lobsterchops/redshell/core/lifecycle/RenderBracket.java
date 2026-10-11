package com.lobsterchops.redshell.core.lifecycle;

/**
 * Brackets the render pass of every frame. The renderer implements this so the engine can begin
 * and end drawing around all systems' {@code render} calls without depending on the rendering package.
 */
public interface RenderBracket {

    /** Called before any system renders. */
    void beginRender();

    /** Called after all systems have rendered, even if one of them threw. */
    void endRender();
}
