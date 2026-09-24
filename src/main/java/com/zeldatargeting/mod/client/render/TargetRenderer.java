package com.zeldatargeting.mod.client.render;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.TargetingManager;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshot;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshotFactory;
import com.zeldatargeting.mod.client.presentation.render.BossPanelRenderer;
import com.zeldatargeting.mod.client.presentation.render.DetailPanelRenderer;
import com.zeldatargeting.mod.client.presentation.render.SoftAimRenderer;
import com.zeldatargeting.mod.client.presentation.render.TargetHistoryRenderer;
import com.zeldatargeting.mod.client.presentation.render.TargetRingRenderer;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class TargetRenderer {
    private final Minecraft minecraft;
    private final TargetPresentationSnapshotFactory snapshots;
    private final TargetRingRenderer ringRenderer;
    private final DetailPanelRenderer detailPanelRenderer;
    private final BossPanelRenderer bossPanelRenderer;
    private final SoftAimRenderer softAimRenderer;
    private final TargetHistoryRenderer targetHistoryRenderer;
    private boolean warned;

    public TargetRenderer() {
        minecraft = Minecraft.getMinecraft();
        snapshots = new TargetPresentationSnapshotFactory();
        ringRenderer = new TargetRingRenderer();
        detailPanelRenderer = new DetailPanelRenderer();
        bossPanelRenderer = new BossPanelRenderer();
        softAimRenderer = new SoftAimRenderer();
        targetHistoryRenderer = new TargetHistoryRenderer();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        TargetingManager manager = TargetingManager.getInstance();
        if (manager == null || !manager.isActive()) {
            trackTargetHistory(null);
            return;
        }
        TargetPresentationSnapshot snapshot = snapshot(manager, minecraft.getRenderPartialTicks());
        if (snapshot == null) {
            trackTargetHistory(null);
            return;
        }

        manager.getPresentationFeedbackController().onPresentationStatus(
            snapshot.getTransitionId(), snapshot.getStatus()
        );

        trackTargetHistory(snapshot.getTarget());
        if (!bossPanelRenderer.render(snapshot, event.getResolution())) {
            detailPanelRenderer.render(snapshot, event.getResolution());
        }
        if (TargetingConfig.softAimIndicator) {
            softAimRenderer.render(snapshot, event.getResolution());
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        TargetingManager manager = TargetingManager.getInstance();
        if (manager == null) {
            return;
        }
        if (manager.isActive()) {
            TargetPresentationSnapshot snapshot = snapshot(manager, event.getPartialTicks());
            if (snapshot != null && TargetingConfig.showReticle) {
                ringRenderer.render(snapshot);
                ringRenderer.renderTargetMarker(snapshot);
            }
        }
        if (TargetingConfig.targetHistoryEnabled) {
            targetHistoryRenderer.render(event.getPartialTicks());
        }
    }

    private TargetPresentationSnapshot snapshot(TargetingManager manager, float partialTicks) {
        try {
            return snapshots.create(manager, partialTicks);
        } catch (RuntimeException exception) {
            if (!warned && ZeldaTargetingMod.getLogger() != null) {
                warned = true;
                ZeldaTargetingMod.getLogger().warn("Target presentation snapshot skipped a frame", exception);
            }
            return null;
        }
    }

    private void trackTargetHistory(Entity activeTarget) {
        if (TargetingConfig.targetHistoryEnabled) {
            targetHistoryRenderer.track(activeTarget);
        } else {
            targetHistoryRenderer.clear();
        }
    }
}
