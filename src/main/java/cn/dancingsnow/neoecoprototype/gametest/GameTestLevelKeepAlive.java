package cn.dancingsnow.neoecoprototype.gametest;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Keeps the game test overworld ticking. Version lock: NeoForge 21.1.251.
 *
 * <p>{@code ServerLevel#tick} runs its own body only while the level has a player or a forced chunk; with
 * neither it gives up after 300 empty ticks, and that freezes gameTime, the chunk system and every
 * {@code GameTestInstance} timer at once. A suite that needs more than those 300 ticks - a fresh world,
 * where all those far-flung test plots still have to be generated - then stops at a handful of tests,
 * records no failure and no timeout, and never ends. One forced chunk is the documented way to tell the
 * level it is attended; this fires only inside a {@link GameTestServer}.</p>
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class GameTestLevelKeepAlive {

    private static boolean forced = false;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (forced || !(event.getLevel() instanceof ServerLevel level)
                || !(level.getServer() instanceof GameTestServer)
                || level.dimension() != Level.OVERWORLD) {
            return;
        }
        forced = level.setChunkForced(0, 0, true);
    }
}
