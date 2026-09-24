package com.zeldatargeting.mod.client.camera;

import com.zeldatargeting.mod.client.camera.core.CameraProfile;

public interface CameraProfileResolver {
    CameraProfile resolve(String profileName, int perspective);
}
