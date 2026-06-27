package org.yanbwe.searchcarefully;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(value = SearchCarefully.MODID, dist = Dist.CLIENT)
public class SearchCarefullyClient {

    private static final Logger LOGGER = LogUtils.getLogger();

    public SearchCarefullyClient(ModContainer container) {
        // Register config screen
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // TODO: Phase 8 — Register key bindings
        // TODO: Phase 8 — Register client render layers / overlay events
        // TODO: Phase 8 — Register client tick handler

        LOGGER.info("SearchCarefully client initialized");
    }
}
