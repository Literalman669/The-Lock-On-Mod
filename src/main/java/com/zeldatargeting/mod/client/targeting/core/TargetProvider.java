package com.zeldatargeting.mod.client.targeting.core;

import java.util.List;

public interface TargetProvider<T> {
    void collectCandidates(List<TargetCandidate<T>> output, boolean acquisitionCone);

    TargetObservation<T> observe(T reference, TargetAnchor preferredAnchor);
}
