package com.zeldatargeting.mod.client.targeting.core;

import java.util.Arrays;

public final class TargetHistory {
    private final int[] entityIds;
    private int size;

    public TargetHistory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        entityIds = new int[capacity];
        Arrays.fill(entityIds, -1);
    }

    public void record(int entityId) {
        int existingIndex = indexOf(entityId);
        int shiftCount;
        if (existingIndex >= 0) {
            shiftCount = existingIndex;
        } else {
            shiftCount = Math.min(size, entityIds.length - 1);
            if (size < entityIds.length) {
                size++;
            }
        }

        if (shiftCount > 0) {
            System.arraycopy(entityIds, 0, entityIds, 1, shiftCount);
        }
        entityIds[0] = entityId;
    }

    public int size() {
        return size;
    }

    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("history index: " + index);
        }
        return entityIds[index];
    }

    public int mostRecent() {
        return size == 0 ? -1 : entityIds[0];
    }

    public boolean contains(int entityId) {
        return indexOf(entityId) >= 0;
    }

    public void clear() {
        Arrays.fill(entityIds, -1);
        size = 0;
    }

    private int indexOf(int entityId) {
        for (int i = 0; i < size; i++) {
            if (entityIds[i] == entityId) {
                return i;
            }
        }
        return -1;
    }
}
