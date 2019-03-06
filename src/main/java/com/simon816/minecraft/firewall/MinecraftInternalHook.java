package com.simon816.minecraft.firewall;

import io.netty.channel.ChannelFuture;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class MinecraftInternalHook {

    public static void doHook(MinecraftServer server, Logger logger) {
        List<ChannelFuture> endpoints = server.getNetworkSystem().endpoints;
        if (endpoints.size() != 1) {
            logger.error("Server network system in unexpected state. Expecting 1 endpoint, got {}. Firewall not loaded!", endpoints.size());
            return;
        }
        Path configFile = server.getDataDirectory().toPath().resolve("firewall.txt");
        MCFirewall firewall = new MCFirewall(logger, configFile);
        firewall.load();
        endpoints.get(0).channel().pipeline().addFirst(firewall.connectionIntercepter());
        ServerCommandManager commandManager = (ServerCommandManager) server.getCommandManager();
        commandManager.registerCommand(new CommandFirewall(firewall));
    }
}
