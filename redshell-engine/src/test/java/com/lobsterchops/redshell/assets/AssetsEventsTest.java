package com.lobsterchops.redshell.assets;

import static org.junit.jupiter.api.Assertions.*;

import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetType;
import com.lobsterchops.redshell.assets.validation.AssetValidator;
import com.lobsterchops.redshell.debug.log.Log;
import com.lobsterchops.redshell.debug.log.LogLevel;
import com.lobsterchops.redshell.events.Event;
import com.lobsterchops.redshell.events.EventBus;
import com.lobsterchops.redshell.events.Subscription;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AssetsEventsTest {

    static final class Ping implements Event { }

    @Test
    void catalogRejectsDuplicatesAndUnknownGroupMembers() {
        AssetCatalog catalog = new AssetCatalog().register("a", "a.png", AssetType.TEXTURE);
        assertThrows(IllegalArgumentException.class, () -> catalog.register("a", "b.png", AssetType.TEXTURE));
        assertThrows(IllegalArgumentException.class, () -> catalog.defineGroup("g", "missing"));
        catalog.defineGroup("g", "a");
        assertThrows(IllegalArgumentException.class, () -> catalog.defineGroup("g", "a"));
        assertEquals("a.png", catalog.entry("a").path());
    }

    @Test
    void validatorReportsMissingFiles() {
        AssetCatalog catalog = new AssetCatalog()
                .register("here", "here.png", AssetType.TEXTURE)
                .register("gone", "gone.png", AssetType.TEXTURE);
        List<String> problems = new AssetValidator(path -> path.equals("here.png")).validate(catalog);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("gone"));
    }

    @Test
    void eventBusDeliversAndCancels() {
        EventBus bus = new EventBus(new Log(LogLevel.ERROR));
        final List<Event> got = new ArrayList<>();
        Subscription sub = bus.subscribe(Ping.class, got::add);
        bus.publish(new Ping());
        sub.cancel();
        bus.publish(new Ping());
        assertEquals(1, got.size());
        assertEquals(0, bus.listenerCount(Ping.class));
    }

    @Test
    void throwingListenerDoesNotStopOthers() {
        EventBus bus = new EventBus(new Log(LogLevel.OFF));
        final int[] count = {0};
        bus.subscribe(Ping.class, e -> { throw new RuntimeException("boom"); });
        bus.subscribe(Ping.class, e -> count[0]++);
        bus.publish(new Ping());
        assertEquals(1, count[0]);
    }
}