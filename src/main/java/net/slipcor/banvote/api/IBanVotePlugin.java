package net.slipcor.banvote.api;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public interface IBanVotePlugin extends Plugin {
    public void brc(String message);

    public void msg(CommandSender sender, String message);

    public BanVoteCommand getBVCommand(byte action);

    public BanVoteCommand getBVCommand(String commandName);
}
