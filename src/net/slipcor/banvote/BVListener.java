package net.slipcor.banvote;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

/**
 * ban vote player listener class
 * 
 * @version v0.0.4
 * 
 * @author slipcor
 * 
 */

public class BVListener implements Listener {

	@EventHandler
	public void playerCommand(PlayerCommandPreprocessEvent event) {
		BanVotePlugin.db.i("onPlayerCommandPreprocess: "
				+ event.getPlayer().getName());
		if (isAllowed(event.getPlayer())) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void playerChat(PlayerChatEvent event) {
		BanVotePlugin.db.i("onPlayerChat: " + event.getPlayer().getName());
		if (isAllowed(event.getPlayer())) {
			return;
		}
		event.setCancelled(true);
	}
	
	/**
	 * is a given player allowed to chat/use commands?
	 * @param p the player to check
	 * @return true if the player may talk/use commands, false otherwise
	 */
	private boolean isAllowed(Player p) {
		if ((BanVote.isChatBlocked(p.getName()))
				|| (BanVoteResult.isMuted(p.getName()))) {
			BanVotePlugin
					.msg(p, ChatColor.GOLD + "You are muted, please wait.");
			return false;
		}
		return true;
	}

	@EventHandler
	public void playerPreLogin(PlayerPreLoginEvent event) {
		BanVotePlugin.db.i("onPlayerPreLogin: " + event.getName());
		if (BanVoteResult.isBanned(event.getName())) {
			BanVotePlugin.db.i("disallowing...");
			event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER,
					"You are vote-banned!");
		}
	}
}
