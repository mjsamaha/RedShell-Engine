package com.lobsterchops.redshell.input;

import static org.junit.jupiter.api.Assertions.*;

import com.lobsterchops.redshell.input.action.Action;
import com.lobsterchops.redshell.input.binding.Binding;
import com.lobsterchops.redshell.input.binding.BindingLoader;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.input.device.InputDevice;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InputTest {

    enum Act implements Action { JUMP, PAUSE }

    static final class FakeDevice implements InputDevice {
        final Set<Binding> down = new HashSet<>();

        @Override
        public boolean supports(Binding.Type type) {
            return type == Binding.Type.KEY;
        }

        @Override
        public float value(Binding binding) {
            return down.contains(binding) ? 1f : 0f;
        }
    }

    @Test
    void bindingTextRoundTrips() {
        BindingSet set = BindingLoader.parse("JUMP=KEY:SPACE,MOUSE:0\nPAUSE=KEY:P", Act.values());
        BindingSet again = BindingLoader.parse(set.toText(), Act.values());
        assertEquals(set.bindingsFor(Act.JUMP), again.bindingsFor(Act.JUMP));
        assertEquals(2, again.bindingsFor(Act.JUMP).size());
    }

    @Test
    void unknownActionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> BindingLoader.parse("FLY=KEY:A", Act.values()));
    }

    @Test
    void pressedHeldReleasedFollowTheDevice() {
        FakeDevice device = new FakeDevice();
        Binding space = Binding.parse("KEY:SPACE");
        InputService input = new InputService(new BindingSet().bind(Act.JUMP, space), device);
        input.poll();
        assertFalse(input.held(Act.JUMP));
        device.down.add(space);
        input.poll();
        assertTrue(input.pressed(Act.JUMP));
        input.poll();
        assertTrue(input.held(Act.JUMP));
        assertFalse(input.pressed(Act.JUMP));
        device.down.clear();
        input.poll();
        assertTrue(input.released(Act.JUMP));
    }

    @Test
    void opaqueContextMasksOtherActionsWithoutPhantomPress() {
        FakeDevice device = new FakeDevice();
        Binding space = Binding.parse("KEY:SPACE");
        InputService input = new InputService(new BindingSet().bind(Act.JUMP, space), device);
        InputContext menu = new InputContext("menu", true, Act.PAUSE);
        device.down.add(space);
        input.poll();
        input.contexts().push(menu);
        input.poll();
        assertFalse(input.held(Act.JUMP));
        input.contexts().remove(menu);
        input.poll();
        assertTrue(input.held(Act.JUMP));
        assertFalse(input.pressed(Act.JUMP));
    }

    @Test
    void rebindReplacesBindings() {
        BindingSet set = new BindingSet().bind(Act.JUMP, Binding.parse("KEY:A"));
        set.rebind(Act.JUMP, Binding.parse("KEY:B"));
        assertEquals(1, set.bindingsFor(Act.JUMP).size());
        assertEquals("KEY:B", set.bindingsFor(Act.JUMP).get(0).toText());
    }
}