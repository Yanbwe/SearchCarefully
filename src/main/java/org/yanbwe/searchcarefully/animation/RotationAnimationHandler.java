package org.yanbwe.searchcarefully.animation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RotationAnimationHandler {
    
    // 动画周期（毫秒）
    private static final long ANIMATION_PERIOD = 1000; // 1秒一圈
    
    // 纹理尺寸
    private static final int TEXTURE_SIZE = 16; // 16x16的纹理
    
    /**
     * 计算旋转纹理在指定时间的相对位置（相对于16x16槽位的左上角）
     * 返回相对于槽位左上角的偏移量
     */
    public static float[] getRotatingPosition(long startTime, long currentTime) {
        long elapsed = currentTime - startTime;
        long cycleTime = elapsed % ANIMATION_PERIOD;
        double progress = (double) cycleTime / ANIMATION_PERIOD; // 0.0 到 1.0
        
        // 将一圈分成四个阶段，每个阶段对应边长为4像素的正方形的一边
        float x, y;
        
        // 计算中心点，使边长为4的运动路径居中于16x16的槽位
        // 路径的边界是：left=6, top=6, right=10, bottom=10 (基于16x16槽位)
        float pathCenterX = 8.0f;  // 槽位中心
        float pathCenterY = 8.0f;  // 槽位中心
        
        // 从中心向外偏移2像素（4像素边长的一半），以形成边长为4的正方形路径
        float halfPathSize = 2.0f;
        
        if (progress < 0.25) { // 上边 (左上 -> 右上)
            // progress 0.0 -> 0.25 对应 x: center-2 -> center+2, y: center-2
            double segmentProgress = progress / 0.25; // 0.0 -> 1.0
            x = (float) (pathCenterX - halfPathSize + segmentProgress * (halfPathSize * 2));
            y = pathCenterY - halfPathSize;
        } else if (progress < 0.5) { // 右边 (右上 -> 右下)
            // progress 0.25 -> 0.5 对应 x: center+2, y: center-2 -> center+2
            double segmentProgress = (progress - 0.25) / 0.25; // 0.0 -> 1.0
            x = pathCenterX + halfPathSize;
            y = (float) (pathCenterY - halfPathSize + segmentProgress * (halfPathSize * 2));
        } else if (progress < 0.75) { // 下边 (右下 -> 左下)
            // progress 0.5 -> 0.75 对应 x: center+2 -> center-2, y: center+2
            double segmentProgress = (progress - 0.5) / 0.25; // 0.0 -> 1.0
            x = (float) (pathCenterX + halfPathSize - segmentProgress * (halfPathSize * 2));
            y = pathCenterY + halfPathSize;
        } else { // 左边 (左下 -> 左上)
            // progress 0.75 -> 1.0 对应 x: center-2, y: center+2 -> center-2
            double segmentProgress = (progress - 0.75) / 0.25; // 0.0 -> 1.0
            x = pathCenterX - halfPathSize;
            y = (float) (pathCenterY + halfPathSize - segmentProgress * (halfPathSize * 2));
        }
        
        // 返回纹理左上角坐标而非中心坐标的要求
        // 纹理是16x16，所以需要从中心坐标减去8来得到左上角坐标
        x -= 8; // 纹理宽度的一半
        y -= 8; // 纹理高度的一半
        
        return new float[]{x, y};
    }
}