package net.slipcor.banvote;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPreLoginEvent;

/**
 * ban vote player listener class
 * 
 * @version v0.0.1
 * 
 * @author slipcor
 * 
 */

public class BanVotePlayerListener extends PlayerListener {

	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		BanVotePlugin.db.i("onPlayerCommandPreprocess: "
				+ event.getPlayer().getName());
		if (BanVotePlugin.instance.bm
				.isChatBlocked(event.getPlayer().getName())) {
			BanVotePlugin.db.i("cancelling event...");
			event.setCancelled(true);
			BanVotePlugin.msg(event.getPlayer(), ChatColor.GOLD
					+ "You are muted, please wait.");
		}
	}

	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		BanVotePlugin.db.i("onPlayerChat: " + event.getPlayer().getName());
		if (BanVotePlugin.instance.bm
				.isChatBlocked(event.getPlayer().getName())) {
			BanVotePlugin.db.i("cancelling event...");
			event.setCancelled(true);
			BanVotePlugin.msg(event.getPlayer(), ChatColor.GOLD
					+ "You are muted, please wait.");
		}
	}

	@Override
	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
		BanVotePlugin.db.i("onPlayerPreLogin: " + event.getName());
		if (BanVotePlugin.instance.bbm.isBanned(event.getName())) {
			BanVotePlugin.db.i("disallowing...");
			event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER,
					"You are vote-banned!");
		}
	}
}
