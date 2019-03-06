package com.simon816.minecraft.firewall;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "mcfirewall-sponge", name = "Minecraft Firewall", version = "1.0.0")
public class MCFirewallSponge {

    private static final Logger logger = LogManager.getLogger("MCFirewall");

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        if (Sponge.getPluginManager().getPlugin("mcfirewall-forge").isPresent()) {
            // Remove ourselves, let the forge version run
            Sponge.getEventManager().unregisterPluginListeners(this);
        }
    }

    @Listener
    public void onServerStart(GameAboutToStartServerEvent event) {
        if (Sponge.getPlatform().getType() == Platform.Type.SERVER) {
            MinecraftInternalHook.doHook((MinecraftServer) Sponge.getServer(), logger);
        }
    }

}
