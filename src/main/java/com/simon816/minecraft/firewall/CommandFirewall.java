package com.simon816.minecraft.firewall;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandFirewall extends CommandBase {

    private final MCFirewall firewall;

    public CommandFirewall(MCFirewall firewall) {
        this.firewall = firewall;
    }

    @Override
    public String getName() {
        return "firewall";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/firewall reload";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!execute0(sender, args)) {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    private boolean execute0(ICommandSender sender, String[] args) {
        if (args.length == 0)
            return false;
        switch (args[0]) {
            case "reload":
                this.firewall.load();
                sender.sendMessage(new TextComponentString("Firewall reloaded. " + this.firewall.getFilter().getRules().size() + " rules loaded."));
                return true;
            default:
                return false;
        }
    }
}
