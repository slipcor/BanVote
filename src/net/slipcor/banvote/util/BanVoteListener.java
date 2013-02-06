package net.slipcor.banvote.util;


import net.slipcor.banvote.api.AVote;
import net.slipcor.banvote.BanVotePlugin;
import net.slipcor.banvote.BanVoteResult;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * ban vote player listener class
 * 
 * @version v0.0.4
 * 
 * @author slipcor
 * 
 */

public class BanVoteListener implements Listener {

	@EventHandler
	public void playerCommand(final PlayerCommandPreprocessEvent event) {
		BanVotePlugin.debug.info("onPlayerCommandPreprocess: "
				+ event.getPlayer().getName());
		if (isAllowed(event.getPlayer())) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void playerChat(final AsyncPlayerChatEvent event) {
		BanVotePlugin.debug.info("onPlayerChat: " + event.getPlayer().getName());
		if (isAllowed(event.getPlayer())) {
			return;
		}
		event.setCancelled(true);
	}
	
	/**
	 * is a given player allowed to chat/use commands?
	 * @param player the player to check
	 * @return true if the player may talk/use commands, false otherwise
	 */
	private synchronized boolean isAllowed(final Player player) {
		if ((AVote.isChatBlocked(player.getName()))
				|| (BanVoteResult.isMuted(player.getName()))) {
			BanVotePlugin.instance
					.msg(player, ChatColor.GOLD + "You are muted, please wait.");
			return false;
		}
		return true;
	}

	@EventHandler
	public void playerLogin(final PlayerLoginEvent event) {
		BanVotePlugin.debug.info("onPlayerPreLogin: " + event.getPlayer().getName());
		if (BanVoteResult.isBanned(event.getPlayer().getName())) {
			BanVotePlugin.debug.info("disallowing...");
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
					"You are vote-banned!");
		}
	}
	
	@EventHandler
	public void playerJoin(final PlayerJoinEvent event) {
		if (event.getPlayer().isOp()) {
			Update.message(event.getPlayer());
		}
	}
}
