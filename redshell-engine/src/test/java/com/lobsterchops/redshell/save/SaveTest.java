package com.lobsterchops.redshell.save;

import static org.junit.jupiter.api.Assertions.*;

import com.lobsterchops.redshell.save.data.*;
import org.junit.jupiter.api.Test;

class SaveTest {

    @Test
    void serializeParseRoundTripWithAwkwardText() {
        SaveData d = new SaveData(3);
        d.putString("name", "line1\nline2 = x\\y");
        d.putInt("n", 42);
        d.putFloat("f", 1.5f);
        d.putBoolean("b", true);
        SaveData back = SaveData.parse(d.serialize());
        assertEquals(3, back.version());
        assertEquals("line1\nline2 = x\\y", back.getString("name", ""));
        assertEquals(42, back.getInt("n", 0));
        assertEquals(1.5f, back.getFloat("f", 0), 1e-6);
        assertTrue(back.getBoolean("b", false));
    }

    @Test
    void migrationChainUpgradesOldSave() {
        MemoryKeyValueStore store = new MemoryKeyValueStore();
        new SaveService(store, 1).write("s", withCoins(1, 5));
        Migration v1to2 = new Migration() {
            public int fromVersion() {
                return 1;
            }

            public void migrate(SaveData data) {
                data.putInt("score", data.getInt("coins", 0) * 10);
                data.remove("coins");
            }
        };
        SaveService v2 = new SaveService(store, 2, v1to2);
        SaveSlot slot = v2.read("s");
        assertNotNull(slot);
        assertEquals(2, slot.data().version());
        assertEquals(50, slot.data().getInt("score", 0));
        assertFalse(slot.data().has("coins"));
        assertEquals(1, v2.list().size());
        v2.delete("s");
        assertNull(v2.read("s"));
    }

    private static SaveData withCoins(int version, int coins) {
        SaveData d = new SaveData(version);
        d.putInt("coins", coins);
        return d;
    }
}