package com.simon816.minecraft.firewall;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "mcfirewall-forge", name = "Minecraft Firewall", version = "1.0.0", serverSideOnly = true, acceptableRemoteVersions = "*")
public class MCFirewallMod {

    private static final Logger logger = LogManager.getLogger("MCFirewall");

    @Mod.EventHandler
    public void onServerStart(FMLServerAboutToStartEvent event) {
        MinecraftInternalHook.doHook(event.getServer(), logger);
    }

}
