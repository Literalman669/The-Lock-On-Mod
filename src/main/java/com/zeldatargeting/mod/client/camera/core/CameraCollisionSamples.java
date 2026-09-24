package com.zeldatargeting.mod.client.camera.core;

public final class CameraCollisionSamples {
    private final double sample0;
    private final double sample1;
    private final double sample2;
    private final double sample3;
    private final double sample4;
    private final double sample5;
    private final double sample6;
    private final double sample7;

    public CameraCollisionSamples(
            double sample0,
            double sample1,
            double sample2,
            double sample3,
            double sample4,
            double sample5,
            double sample6,
            double sample7) {
        this.sample0 = sample0;
        this.sample1 = sample1;
        this.sample2 = sample2;
        this.sample3 = sample3;
        this.sample4 = sample4;
        this.sample5 = sample5;
        this.sample6 = sample6;
        this.sample7 = sample7;
    }

    public static CameraCollisionSamples uniform(double distance) {
        return new CameraCollisionSamples(
            distance, distance, distance, distance,
            distance, distance, distance, distance
        );
    }

    public double minimumValidDistance() {
        if (!isValid(sample0)
                || !isValid(sample1)
                || !isValid(sample2)
                || !isValid(sample3)
                || !isValid(sample4)
                || !isValid(sample5)
                || !isValid(sample6)
                || !isValid(sample7)) {
            return 0.0D;
        }
        double minimum = Math.min(sample0, sample1);
        minimum = Math.min(minimum, sample2);
        minimum = Math.min(minimum, sample3);
        minimum = Math.min(minimum, sample4);
        minimum = Math.min(minimum, sample5);
        minimum = Math.min(minimum, sample6);
        return Math.min(minimum, sample7);
    }

    private static boolean isValid(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value >= 0.0D;
    }
}
