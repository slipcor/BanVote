package net.slipcor.banvote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.slipcor.banvote.api.AVote;
import net.slipcor.banvote.api.BanVoteCommand;
import net.slipcor.banvote.api.IBanVotePlugin;
import net.slipcor.banvote.util.Config;
import net.slipcor.banvote.util.Debugger;
import net.slipcor.banvote.util.BanVoteListener;
import net.slipcor.banvote.util.Language;
import net.slipcor.banvote.util.Memory;
import net.slipcor.banvote.util.Update;
import net.slipcor.banvote.util.Tracker;
import net.slipcor.banvote.votes.GeneralVote;
import net.slipcor.banvote.votes.PlayerVote;
import net.slipcor.banvote.votes.UnmutedPlayerVote;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * plugin class
 * 
 * @version v0.0.4
 * 
 * @author slipcor
 * 
 */

public class BanVotePlugin extends JavaPlugin implements IBanVotePlugin {
	public static Debugger debug;
	public static BanVotePlugin instance;
	public static Set<AVote> votes = new HashSet<AVote>();
	protected static Map<Integer, BanVoteResult> results = new HashMap<Integer, BanVoteResult>();
	
	protected final BanVoteListener listen = new BanVoteListener();
	
	private static Map<String, BanVoteCommand> commands = new HashMap<String, BanVoteCommand>();

	@Override
	public void onEnable() {
		instance = this;
		debug = new Debugger(getConfig().getBoolean("debug", false));
		debug.info("registering events...");
		getServer().getPluginManager().registerEvents(listen, this);

		saveDefaultConfig();

		Language.init(this);
		
		parseConfigs();
		
		final Tracker tracker = new Tracker(this);
        tracker.start();
		Update.updateCheck(this);
		
		getLogger().info(Language.LOG_ENABLED.toString(getDescription().getVersion()));
	}

	private void parseConfigs() {
		Config.set(getConfig().getConfigurationSection("settings"));
		results.clear();

		if (getConfig().get("bans") != null) {
			
			final Set<String> bans = new HashSet<String>();

			for (Object val : getConfig().getConfigurationSection("bans")
					.getValues(true).values()) {
				bans.add(val.toString());
			}
			getConfig().set("bans", null);
			saveConfig();

			for (String s : bans) {
				BanVoteResult.add(s);
			}
			saveConfig();
		}
		commands.clear();
		int pos = 2;
		if (getConfig().get("commands") != null) {
			for (Object val : getConfig().getConfigurationSection("commands")
					.getValues(false).keySet()) {
				commands.put(val.toString(),
						new BanVoteCommand(
								getConfig().getBoolean("commands."+val.toString()+".ban"),
								getConfig().getBoolean("commands."+val.toString()+".kick"),
								getConfig().getBoolean("commands."+val.toString()+".noplayer"),
								getConfig().getString("commands."+val.toString()+".command"),
								val.toString(),
								(byte) ++pos));
			}
		}

		Memory.init(this);
	}

	@Override
	public void onDisable() {
		Tracker.stop();
		debug.info("canceling tasks...");
		Bukkit.getScheduler().cancelTasks(this);
		getLogger().info(Language.LOG_DISABLED.toString(getDescription().getVersion()));
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String sCmd,
			final String[] args) {
		if (sCmd.equals("release") || (sCmd.equals("banvote") && args.length > 0 && args[0].equals("reload"))) {
			return onAdminCommand(sender, args);
		}
		
		if (!(sender instanceof Player)) {
			debug.info("onCommand: sent from console");
			msg(sender, Language.LOG_CONSOLE.toString());
			return true;
		}
		
		byte action = 0;
		final Player player = (Player) sender;

		if (!sCmd.equals("banvote") && !sCmd.equals("mutevote")
				&& !sCmd.equals("kickvote") && !sCmd.equals("customvote")) {
			return false;
		}

		if (sCmd.startsWith("ban")) {
			action = 2;
		} else if (sCmd.startsWith("kick")) {
			action = 1;
		} else if (sCmd.startsWith("mute")) {
			action = 0;
		} else {
			AVote vote = AVote.getActiveVote();
			if (vote == null) {
				try {
					action = getBVCommand(args[1]).getAction();
				} catch (Exception e) {
					msg(player, Language.ERROR_NOVOTERUNNING.toString());
					return true;
				}
			} else {
				action = instance.getBVCommand(vote.getType()).getAction();
			}
		}

		final String type = action<3?AVote.parse(action):sCmd.substring(0, sCmd.length()-4);

		debug.info("onCommand: " + type + "vote command");

		if (!player.hasPermission(type + "vote.vote")) {
			msg(player, Language.ERROR_NOPERMISSION.toString());
			return true;
		}

		if (args == null || args.length < 1) {
			return false;
		}

		debug.info("onCommand: args: " + parseStringArray(args,(byte)0));

		if (args.length > 1) {
			debug.info("vote init: " + player.getName() + " => " + args[0]);
			debug.info("args: "
					+ instance.parseStringArray(args, action));

			if (!player.hasPermission(type + "vote.cmd")) {
				msg(player, Language.ERROR_NOPERMISSION.toString());
				return true;
			}
			
			Player pTarget = null;

			try {
				pTarget = Bukkit.matchPlayer(args[0]).get(0);
				debug.info("player found: " + pTarget.getName());
			} catch (Exception e) {
				debug.warn("player not found.");
			}
			
			BanVoteCommand bvc = getBVCommand(action);
			if (pTarget == null) {
				if (bvc == null || !bvc.doesIgnorePlayer()) {
					msg(player, Language.ERROR_PLAYERNOTFOUND.toString(args[0]));
					return true;
				}
			} else {
			
				if (pTarget.hasPermission("banvote.novote")) {
					msg(player, Language.ERROR_VOTEPROTECTED.toString(args[0]));
					return true;
				}
				
				if (!AVote.isPossible(pTarget)) {
					BanVotePlugin.instance.msg(player,Language.INFO_VOTECOOLINGDOWN.toString(args[0]));
					return true;
				}
			}
			BanVotePlugin.debug.info("possibility check positive");
			AVote vote = null;
			if (bvc != null && bvc.doesIgnorePlayer()) {
				vote = new GeneralVote(pTarget, player,
						BanVotePlugin.instance.parseStringArray(args, action), action);
			} else if (Config.mute) {
				vote = new PlayerVote(pTarget, player,
						BanVotePlugin.instance.parseStringArray(args, action), action);
			} else {
				vote = new UnmutedPlayerVote(pTarget, player,
						BanVotePlugin.instance.parseStringArray(args, action), action);
			}
			if (Config.requireReason && (vote.getReason() == null || vote.getReason().equals(""))) {
				msg(player, Language.ERROR_REASONREQUIRED.toString());
					
				return true;
			}
			BanVotePlugin.votes.add(vote);
			return true;
		}

		if (args[0].equalsIgnoreCase("help")) {
			msg(player, Language.INFO_HELP1.toString(type));
			msg(player, Language.INFO_HELP2.toString(type));
			msg(player, Language.INFO_HELP3.toString(type));
			msg(player, Language.INFO_HELP4.toString());
			msg(player, Language.INFO_HELP5.toString());
			msg(player, Language.INFO_HELP6.toString());
			return true;
		}

		AVote.commit(args[0], player);
		return true;
	}

	/**
	 * send a prefixed broadcast to the server
	 * 
	 * @param message
	 *            string to prefix and send
	 */
	@Override
	public void brc(final String message) {
		if (message == null || message.equals("")) {
			return;
		}
		BanVotePlugin.debug.info("@all: " + message);
		Bukkit.broadcastMessage("[�bBanVote�f] " + message);
	}

	@Override
	public BanVoteCommand getBVCommand(final String cmd) {
		
		String sCmd = cmd;
		if (sCmd.endsWith("vote")) {
			sCmd = sCmd.substring(0,sCmd.length()-4);
		}
		
		for (String name : commands.keySet()) {
			if (name.equals(sCmd)) {

				return commands.get(name);
			}
		}
		return null;
	}
	
	@Override
	public BanVoteCommand getBVCommand(final byte startPos) {
		byte pos = startPos;
		for (BanVoteCommand cmd : commands.values()) {
			if (pos-- == 3) {
				return cmd;
			}
		}
		return null;
	}

	/**
	 * send a prefixed message to a player
	 * 
	 * @param sender
	 *            player/console to send the message to
	 * @param message
	 *            string to prefix and send
	 */
	@Override
	public void msg(final CommandSender sender, final String message) {
		if (message == null || message.equals("")) {
			return;
		}
		if (sender instanceof Player) {
			BanVotePlugin.debug.info("@" + sender.getName() + ": " + message);
		}
		sender.sendMessage("[�bBanVote�f] " + message);
	}

	/**
	 * parse admin (unban) command
	 * 
	 * @param sender
	 *            player committing the command
	 * @param args
	 *            vote UID to unban
	 * @return true if args correct, false otherwise
	 */
	private boolean onAdminCommand(final CommandSender sender, final String[] args) {
		if (args == null || args.length != 1) {
			return false;
		}

		if (!sender.hasPermission("banvote.admin")) {
			msg(sender, Language.ERROR_NOPERMISSION.toString());
			return true;
		}

		if (args[0].equals("list")) {
			for (int i : results.keySet()) {
				final BanVoteResult ban = results.get(i);
				msg(sender, "�6#" + i + ": " + ban.getInfo());
			}
			if (results.size() < 1) {
				msg(sender, Language.INFO_NOBANS.toString());
			}
			return true;
		} else if (args[0].equals("reload")) {
			// /banvote reload
			reloadConfig();
			parseConfigs();
			
			msg(sender, Language.INFO_RELOADED.toString());
			
			return true;
		}

		String banPlayer = "";
		try {
			final int pos = Integer.parseInt(args[0]);
			banPlayer = results.get(pos).getResultPlayerName();
			BanVoteResult.remove(pos);
		} catch (Exception e) {
			msg(sender, Language.ERROR_NOTNUMERIC.toString(args[0]));
			return true;
		}
		msg(sender, Language.GOOD_UNBANNED.toString(banPlayer));
		return true;
	}

	/**
	 * turn a array into a string joined by spaces
	 * 
	 * @param args
	 *            the string array to join
	 * @param action 
	 * @return a string joined by spaces
	 */
	public String parseStringArray(final String[] args, final byte action) {
		String result = "";
		for (int i = (action<3?1:2); i < args.length; i++) {
			result += result.equals("") ? args[i] : (" " + args[i]);
		}
		return result;
	}
}