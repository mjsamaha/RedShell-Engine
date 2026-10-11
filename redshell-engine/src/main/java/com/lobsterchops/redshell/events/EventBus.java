package com.lobsterchops.redshell.events;

import com.lobsterchops.redshell.debug.log.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed publish/subscribe. Events are delivered to listeners of the event's exact class, in subscription
 * order. Subscribing or cancelling during delivery is safe. A throwing listener is logged and does not
 * stop delivery to the others.
 */
public final class EventBus {

    private static final String TAG = "EventBus";

    private final Map<Class<?>, List<Entry<?>>> listeners = new HashMap<>();
    private final Log log;

    public EventBus(Log log) {
        this.log = log;
    }

    public <E extends Event> Subscription subscribe(Class<E> type, EventListener<E> listener) {
        if (type == null || listener == null) {
            throw new IllegalArgumentException("type and listener must not be null");
        }
        List<Entry<?>> list = listeners.get(type);
        if (list == null) {
            list = new ArrayList<>();
            listeners.put(type, list);
        }
        Entry<E> entry = new Entry<>(this, type, listener);
        list.add(entry);
        return entry;
    }

    @SuppressWarnings("unchecked")
    public void publish(Event event) {
        List<Entry<?>> list = listeners.get(event.getClass());
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Entry<?>> snapshot = new ArrayList<>(list);
        for (int i = 0; i < snapshot.size(); i++) {
            Entry<Event> entry = (Entry<Event>) snapshot.get(i);
            if (!entry.active) {
                continue;
            }
            try {
                entry.listener.onEvent(event);
            } catch (RuntimeException e) {
                log.error(TAG, "Listener failed for " + event.getClass().getName(), e);
            }
        }
    }

    public int listenerCount(Class<? extends Event> type) {
        List<Entry<?>> list = listeners.get(type);
        return list == null ? 0 : list.size();
    }

    private void remove(Entry<?> entry) {
        List<Entry<?>> list = listeners.get(entry.type);
        if (list != null) {
            list.remove(entry);
        }
    }

    private static final class Entry<E extends Event> implements Subscription {
        private final EventBus bus;
        private final Class<E> type;
        private final EventListener<E> listener;
        private boolean active = true;

        Entry(EventBus bus, Class<E> type, EventListener<E> listener) {
            this.bus = bus;
            this.type = type;
            this.listener = listener;
        }

        @Override
        public void cancel() {
            if (active) {
                active = false;
                bus.remove(this);
            }
        }

        @Override
        public boolean isActive() {
            return active;
        }
    }
}