package net.slipcor.banvote.util;


import net.slipcor.banvote.BanVotePlugin;
import net.slipcor.banvote.BanVoteResult;
import net.slipcor.banvote.api.AVote;
import net.slipcor.banvote.util.Updater.UpdateResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ban vote player listener class
 *
 * @author slipcor
 * @version v0.0.4
 */

public class BanVoteListener implements Listener {

    private final List<String> exemptExempt = new ArrayList<String>();
    public final List<String> exempt = new ArrayList<String>();

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
     *
     * @param player the player to check
     * @return true if the player may talk/use commands, false otherwise
     */
    private synchronized boolean isAllowed(final Player player) {
        if ((AVote.isChatBlocked(player.getName()))
                || (BanVoteResult.isMuted(player.getName()))) {
            BanVotePlugin.instance
                    .msg(player, Language.ERROR_MUTED.toString());
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
                    Language.INFO_VOTEBANNEDSECONDS.toString(String.valueOf(
                            BanVoteResult.getBannedSeconds(event.getPlayer().getName()))));
        }
    }

    @EventHandler
    public void playerJoin(final PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            if (BanVotePlugin.instance.getUpdater() != null) {
                UpdateResult test = BanVotePlugin.instance.getUpdater().getResult();
                if (test == UpdateResult.UPDATE_AVAILABLE) {
                    BanVotePlugin.instance.msg(event.getPlayer(), "There is an update available, please update!");
                } else if (test == UpdateResult.SUCCESS) {
                    BanVotePlugin.instance.msg(event.getPlayer(), "The plugin has been updated, please restart the server!");
                }
            }
        } else if (BanVotePlugin.votes.size() > 0 && Config.joincheck) {
            if (exemptExempt.contains(event.getPlayer().getName())) {
                exemptExempt.remove(event.getPlayer().getName());
            } else {
                exemptExempt.add(event.getPlayer().getName());
            }
        }
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        if (BanVotePlugin.votes.size() > 0 && Config.joincheck) {
            exemptExempt.add(event.getPlayer().getName());
        }
    }
}
