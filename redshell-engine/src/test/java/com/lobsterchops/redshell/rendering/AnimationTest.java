package com.lobsterchops.redshell.rendering;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.rendering.animation.Animation2D;
import com.lobsterchops.redshell.rendering.animation.AnimationController;
import com.lobsterchops.redshell.rendering.animation.LoopMode;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnimationTest {

    private static TextureRegion[] frames(int n) {
        TextureRegion[] r = new TextureRegion[n];
        for (int i = 0; i < n; i++) {
            r[i] = new TextureRegion();
        }
        return r;
    }

    @Test
    void loopWrapsAround() {
        Animation2D a = new Animation2D("a", frames(3), 0.1f, LoopMode.LOOP);
        assertEquals(0, a.frameIndexAt(0.05f));
        assertEquals(2, a.frameIndexAt(0.25f));
        assertEquals(0, a.frameIndexAt(0.31f));
    }

    @Test
    void onceHoldsLastFrameAndFinishes() {
        Animation2D a = new Animation2D("a", frames(3), 0.1f, LoopMode.ONCE);
        assertEquals(2, a.frameIndexAt(5f));
        assertTrue(a.isFinished(5f));
        assertFalse(a.isFinished(0.1f));
    }

    @Test
    void pingPongReversesDirection() {
        Animation2D a = new Animation2D("a", frames(3), 0.1f, LoopMode.PING_PONG);
        assertEquals(2, a.frameIndexForStep(2));
        assertEquals(1, a.frameIndexForStep(3));
        assertEquals(0, a.frameIndexForStep(4));
    }

    @Test
    void frameEventsFireOncePerPassEvenOnLargeSteps() {
        Animation2D a = new Animation2D("a", frames(4), 0.1f, LoopMode.LOOP).addEvent(1, "step");
        AnimationController c = new AnimationController().addState("a", a);
        final List<String> fired = new ArrayList<>();
        c.setListener(e -> fired.add(e.name()));
        c.play("a");
        c.update(0.15f);
        assertEquals(1, fired.size());
        c.update(0.9f);
        assertEquals(3, fired.size());
    }

    @Test
    void emptyAnimationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Animation2D("a", frames(0), 0.1f, LoopMode.LOOP));
    }
}