package com.lobsterchops.redshell.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lobsterchops.redshell.util.collection.RingBuffer;
import com.lobsterchops.redshell.util.math.MathUtil;
import com.lobsterchops.redshell.util.random.SeededRandom;
import com.lobsterchops.redshell.util.timer.Cooldown;
import com.lobsterchops.redshell.util.timer.DelayedAction;
import com.lobsterchops.redshell.util.timer.Timer;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class UtilTest {

    private static final float EPS = 1e-5f;

    @Test
    void mathBasics() {
        assertEquals(5f, MathUtil.clamp(9f, 0f, 5f), EPS);
        assertEquals(2, MathUtil.clamp(2, 0, 5));
        assertEquals(15f, MathUtil.lerp(10f, 20f, 0.5f), EPS);
        assertEquals(0.25f, MathUtil.inverseLerp(0f, 4f, 1f), EPS);
        assertEquals(50f, MathUtil.remap(5f, 0f, 10f, 0f, 100f), EPS);
        assertTrue(MathUtil.approximately(1f, 1.0001f, 0.001f));
        assertEquals(5f, MathUtil.distance(0f, 0f, 3f, 4f), EPS);
        assertEquals(0f, MathUtil.smoothStep(0f), EPS);
        assertEquals(1f, MathUtil.smoothStep(1f), EPS);
        assertEquals(1f, MathUtil.easeOutCubic(1f), EPS);
        assertEquals(0f, MathUtil.easeInOutQuad(0f), EPS);
    }

    @Test
    void angleHelpers() {
        assertEquals(350f, MathUtil.normalizeDegrees(-10f), EPS);
        assertEquals(20f, MathUtil.shortestAngleDelta(350f, 10f), EPS);
        assertEquals(-20f, MathUtil.shortestAngleDelta(10f, 350f), EPS);
    }

    @Test
    void timerFiresOnceWhenNotRepeating() {
        Timer timer = new Timer(1f, false);
        assertFalse(timer.update(0.5f));
        assertTrue(timer.update(0.6f));
        assertTrue(timer.isFinished());
        assertFalse(timer.update(1f));
        timer.reset();
        assertFalse(timer.isFinished());
    }

    @Test
    void timerRepeats() {
        Timer timer = new Timer(1f, true);
        assertTrue(timer.update(1.25f));
        assertEquals(0.25f, timer.elapsed(), EPS);
        assertTrue(timer.update(0.75f));
        assertFalse(timer.isFinished());
        assertThrows(IllegalArgumentException.class, () -> new Timer(0f, false));
    }

    @Test
    void cooldownGatesUse() {
        Cooldown cooldown = new Cooldown(2f);
        assertTrue(cooldown.tryUse());
        assertFalse(cooldown.tryUse());
        cooldown.update(1f);
        assertEquals(1f, cooldown.remaining(), EPS);
        cooldown.update(1f);
        assertTrue(cooldown.isReady());
        assertTrue(cooldown.tryUse());
    }

    @Test
    void delayedActionRunsOnceAndCanBeCancelled() {
        int[] runs = {0};
        DelayedAction action = new DelayedAction(1f, () -> runs[0]++);
        assertFalse(action.update(0.5f));
        assertTrue(action.update(0.5f));
        assertFalse(action.update(5f));
        assertEquals(1, runs[0]);
        assertTrue(action.isDone());

        DelayedAction cancelled = new DelayedAction(1f, () -> runs[0]++);
        cancelled.cancel();
        cancelled.update(5f);
        assertEquals(1, runs[0]);
    }

    @Test
    void seededRandomIsDeterministic() {
        SeededRandom a = new SeededRandom(42L);
        SeededRandom b = new SeededRandom(42L);
        for (int i = 0; i < 100; i++) {
            assertEquals(a.nextInt(1000), b.nextInt(1000));
            assertEquals(a.nextFloat(), b.nextFloat(), 0f);
        }
        assertEquals(42L, a.seed());
    }

    @Test
    void seededRandomRangesAndWeights() {
        SeededRandom random = new SeededRandom(7L);
        for (int i = 0; i < 200; i++) {
            int value = random.nextInt(3, 6);
            assertTrue(value >= 3 && value <= 6);
            float f = random.nextFloat(2f, 4f);
            assertTrue(f >= 2f && f < 4f);
        }
        float[] weights = {0f, 1f, 0f};
        for (int i = 0; i < 50; i++) {
            assertEquals(1, random.pickWeightedIndex(weights));
        }
        List<String> items = Arrays.asList("a", "b", "c");
        assertTrue(items.contains(random.pick(items)));
        assertFalse(random.chance(0f));
        assertTrue(random.chance(1f));
    }

    @Test
    void ringBufferOverwritesOldest() {
        RingBuffer<Integer> buffer = new RingBuffer<>(3);
        assertTrue(buffer.isEmpty());
        for (int i = 1; i <= 5; i++) {
            buffer.add(i);
        }
        assertTrue(buffer.isFull());
        assertEquals(3, buffer.size());
        assertEquals(3, buffer.oldest());
        assertEquals(5, buffer.newest());
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(3));
        buffer.clear();
        assertTrue(buffer.isEmpty());
    }
}
