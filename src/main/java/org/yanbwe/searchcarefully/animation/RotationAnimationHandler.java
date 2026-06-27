package org.yanbwe.searchcarefully.animation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RotationAnimationHandler {

    private static final long ANIMATION_PERIOD = 1000;
    private static final int TEXTURE_SIZE = 16;

    public static float[] getRotatingPosition(long startTime, long currentTime) {
        long elapsed = currentTime - startTime;
        long cycleTime = elapsed % ANIMATION_PERIOD;
        double progress = (double) cycleTime / ANIMATION_PERIOD;

        float x, y;
        float pathCenterX = 8.0f;
        float pathCenterY = 8.0f;
        float halfPathSize = 2.0f;

        if (progress < 0.25) {
            double segmentProgress = progress / 0.25;
            x = (float) (pathCenterX - halfPathSize + segmentProgress * (halfPathSize * 2));
            y = pathCenterY - halfPathSize;
        } else if (progress < 0.5) {
            double segmentProgress = (progress - 0.25) / 0.25;
            x = pathCenterX + halfPathSize;
            y = (float) (pathCenterY - halfPathSize + segmentProgress * (halfPathSize * 2));
        } else if (progress < 0.75) {
            double segmentProgress = (progress - 0.5) / 0.25;
            x = (float) (pathCenterX + halfPathSize - segmentProgress * (halfPathSize * 2));
            y = pathCenterY + halfPathSize;
        } else {
            double segmentProgress = (progress - 0.75) / 0.25;
            x = pathCenterX - halfPathSize;
            y = (float) (pathCenterY + halfPathSize - segmentProgress * (halfPathSize * 2));
        }

        x -= 8;
        y -= 8;
        return new float[]{x, y};
    }
}
