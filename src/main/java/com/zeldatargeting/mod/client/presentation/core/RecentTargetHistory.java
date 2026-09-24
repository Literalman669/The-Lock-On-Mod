package com.zeldatargeting.mod.client.presentation.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public final class RecentTargetHistory<T> {
    private final int capacity;
    private final Deque<T> entries;
    private T activeTarget;

    public RecentTargetHistory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        this.capacity = capacity;
        entries = new ArrayDeque<>(capacity);
    }

    public void track(T target) {
        if (sameTarget(activeTarget, target)) {
            return;
        }
        record(activeTarget);
        if (target != null) {
            entries.remove(target);
        }
        activeTarget = target;
    }

    public void removeIf(Predicate<T> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("predicate must not be null");
        }
        Iterator<T> iterator = entries.iterator();
        while (iterator.hasNext()) {
            if (predicate.test(iterator.next())) {
                iterator.remove();
            }
        }
    }

    public List<T> entries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public void clear() {
        entries.clear();
        activeTarget = null;
    }

    private void record(T target) {
        if (target == null) {
            return;
        }
        entries.remove(target);
        entries.addFirst(target);
        while (entries.size() > capacity) {
            entries.removeLast();
        }
    }

    private static boolean sameTarget(Object left, Object right) {
        return left == right || (left != null && left.equals(right));
    }
}
