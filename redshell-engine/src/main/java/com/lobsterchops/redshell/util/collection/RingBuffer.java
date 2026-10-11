package com.lobsterchops.redshell.util.collection;

/** Fixed-capacity buffer that overwrites the oldest element when full. Index 0 is the oldest element. */
public final class RingBuffer<T> {

    private final Object[] items;
    private int start;
    private int size;

    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive: " + capacity);
        }
        this.items = new Object[capacity];
    }

    public void add(T item) {
        if (size < items.length) {
            items[(start + size) % items.length] = item;
            size++;
        } else {
            items[start] = item;
            start = (start + 1) % items.length;
        }
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        return (T) items[(start + index) % items.length];
    }

    public T oldest() {
        return get(0);
    }

    public T newest() {
        return get(size - 1);
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return items.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == items.length;
    }

    public void clear() {
        for (int i = 0; i < items.length; i++) {
            items[i] = null;
        }
        start = 0;
        size = 0;
    }
}
