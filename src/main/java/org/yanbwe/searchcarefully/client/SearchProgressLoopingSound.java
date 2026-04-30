package org.yanbwe.searchcarefully.client;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * 客户端搜索进度循环音效。
 * 在搜索过程中持续循环播放，直到收到停止指令。
 * 每 tick 更新位置以跟随玩家移动。
 */
public class SearchProgressLoopingSound extends AbstractSoundInstance
        implements TickableSoundInstance {

    private boolean stopped = false;
    private final Player player;

    public SearchProgressLoopingSound(SoundEvent soundEvent, SoundSource source, Player player) {
        super(soundEvent, source, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.5F;
        this.pitch = 1.0F;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.relative = false;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (player == null || !player.isAlive()) {
            stop();
            return;
        }
        // 跟随玩家位置
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        this.stopped = true;
        this.looping = false;
    }
}
