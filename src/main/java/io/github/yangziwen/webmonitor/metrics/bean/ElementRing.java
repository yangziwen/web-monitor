package io.github.yangziwen.webmonitor.metrics.bean;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.ListUtils;

public class ElementRing<E> {

    private AtomicLong posHolder = new AtomicLong(-1);

    private final int capacity;

    private E[] elements;

    @SuppressWarnings("unchecked")
    public ElementRing(int capacity, Class<E> clazz) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.elements = (E[]) Array.newInstance(clazz, capacity);
    }

    private int getIndex(long pos) {
        return (int) (pos % capacity);
    }

    public int currentPos() {
        return getIndex(posHolder.get());
    }

    public int nextPos() {
        long pos = posHolder.incrementAndGet();
        long value;
        for (int i = 0; i < 3 && (value = posHolder.get()) >= capacity; i++) {
            posHolder.compareAndSet(value, value - capacity);
        }
        return getIndex(pos);
    }

    public E set(int index, E element) {
       E previous = elements[index];
       elements[index] = element;
       return previous;
    }

    public E add(E element) {
        return set(nextPos(), element);
    }

    public List<E> latest(int n) {
        if (n <= 0) {
            return Collections.emptyList();
        }
        long pos = posHolder.get();
        if (pos < 0) {
            return Collections.emptyList();
        }
        if (n > pos + 1) {
            if (pos + 1 >= capacity) {
                return Arrays.asList(Arrays.copyOf(elements, capacity));
            } else {
                return Arrays.asList(Arrays.copyOf(elements, (int) pos + 1));
            }
        } else {
            if (n >= capacity) {
                return Arrays.asList(Arrays.copyOf(elements, capacity));
            } else {
                int head = (int) ((pos - n + 1) % capacity);
                int tail = (int) ((pos + 1) % capacity);
                if (tail >= head) {
                    return Arrays.asList(Arrays.copyOfRange(elements, head, tail));
                } else {
                    List<E> list1 = Arrays.asList(Arrays.copyOfRange(elements, head, capacity));
                    List<E> list2 = Arrays.asList(Arrays.copyOfRange(elements, 0, tail));
                    return ListUtils.union(list1, list2);
                }
            }
        }
    }

    public boolean isEmpty() {
        return posHolder.get() < 0;
    }

}
