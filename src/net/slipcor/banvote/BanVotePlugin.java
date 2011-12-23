package net.slipcor.banvote;

import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * plugin class
 * 
 * @version v0.0.1
 * 
 * @author slipcor
 * 
 */

public class BanVotePlugin extends JavaPlugin {
	protected static BanVotePlugin instance;
	protected final BanVoteManager bm = new BanVoteManager();
	protected final BanVoteBanManager bbm = new BanVoteBanManager();
	protected final BanVotePlayerListener pl = new BanVotePlayerListener();

	protected static BanVoteDebugger db;
	protected static BanVoteLogger log = new BanVoteLogger(
			Logger.getLogger("Minecraft"), "[BanVote] ");

	@Override
	public void onEnable() {
		instance = this;
		db = new BanVoteDebugger(Logger.getLogger("Minecraft"), "[BanVote] ",
				getConfig().getBoolean("debug", false));
		db.i("enabling...");
		db.i("registering events...");
		getServer().getPluginManager().registerEvent(Type.PLAYER_CHAT, pl,
				Priority.Normal, this);
		getServer().getPluginManager().registerEvent(
				Type.PLAYER_COMMAND_PREPROCESS, pl, Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Type.PLAYER_PRELOGIN, pl,
				Priority.Normal, this);

		getConfig().options().copyDefaults(true);
		saveConfig();

		BanVoteClass.set(getConfig().getConfigurationSection("settings")
				.getValues(false));

		if (getConfig().get("bans") != null) {
			HashSet<String> bans = new HashSet<String>();

			for (Object val : getConfig().getConfigurationSection("bans")
					.getValues(true).values()) {
				bans.add(val.toString());
			}
			getConfig().set("bans", null);
			saveConfig();

			for (String s : bans) {
				bbm.add(s);
			}
			saveConfig();
		}
		log.i(getDescription().getVersion() + " enabled");
	}

	@Override
	public void onDisable() {
		// TODO: save ban stats/times
		db.i("disabling...");
		db.i("canceling tasks...");
		Bukkit.getScheduler().cancelTasks(this);
		log.i(getDescription().getVersion() + " disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String sCmd,
			String[] args) {

		if (!(sender instanceof Player)) {
			db.i("onCommand: sent from console");
			sender.sendMessage("[BanVote] Commands only usable ingame!");
			return true;
		}

		Player player = (Player) sender;

		if (!sCmd.equals("banvote")) {
			if (sCmd.equals("unbanvote")) {
				return onAdminCommand(player, args);
			} else {
				return false;
			}
		}

		db.i("onCommand: banvote command");

		if (!player.hasPermission("banvote.vote")) {
			BanVotePlugin.msg(player, ChatColor.RED
					+ "You don't have permission!");
			return true;
		}

		if (args == null || args.length < 1) {
			return false;
		}

		db.i("onCommand: args: " + parseStringArray(args));

		if (args.length > 1) {
			bm.init(args[0], args, player);
			return true;
		}

		if (args[0].equalsIgnoreCase("help")) {
			msg(player, ChatColor.GOLD
					+ "To start a vote to ban a player type: ");
			msg(player, ChatColor.AQUA + "/banvote [playername] [reason]fuc");
			msg(player, ChatColor.GOLD + "Once started, type "
					+ ChatColor.GREEN + "/banvote [+|yes|true]"
					+ ChatColor.GOLD + " to vote to ban");
			msg(player, ChatColor.GOLD + "or " + ChatColor.RED
					+ "/banvote [-|no|false]" + ChatColor.GOLD
					+ " to vote not to ban.");
			msg(player, ChatColor.GOLD + "A vote against counts as "
					+ ChatColor.RED + "-4" + ChatColor.GOLD
					+ " votes towards a ban");
			msg(player, ChatColor.GOLD + "A non-vote counts as "
					+ ChatColor.RED + "-0.25" + ChatColor.GOLD
					+ " votes towards a ban");
			return true;
		}

		bm.commit(args[0], player);
		return true;
	}

	/**
	 * parse admin (unban) command
	 * 
	 * @param player
	 *            player committing the command
	 * @param args
	 *            vote UID to unban
	 * @return true if args correct, false otherwise
	 */
	private boolean onAdminCommand(Player player, String[] args) {
		if (args == null || args.length != 1) {
			return false;
		}

		if (!player.hasPermission("banvote.admin")) {
			BanVotePlugin.msg(player, ChatColor.RED
					+ "You don't have permission!");
			return true;
		}

		if (args[0].equals("list")) {
			for (int i : bbm.bans.keySet()) {
				BanVoteBan ban = bbm.bans.get(i);
				BanVotePlugin.msg(player, ChatColor.GOLD + "#" + i + ": " + ban.getInfo());
			}
			if (bbm.bans.size() < 1) {
				BanVotePlugin.msg(player, ChatColor.GOLD + "No bans active!");
			}
			return true;
		}

		String banPlayer = "";
		try {
			int i = Integer.parseInt(args[0]);
			banPlayer = bbm.bans.get(i).getBanned();
			bbm.remove(i);
		} catch (Exception e) {
			BanVotePlugin.msg(player, ChatColor.RED
					+ "Invalid argument! Not a number: " + args[0]);
			return true;
		}
		BanVotePlugin.msg(player, ChatColor.GREEN + "Unbanned: " + banPlayer);
		return true;
	}

	/**
	 * send a prefixed message to a player
	 * 
	 * @param player
	 *            player to send the message to
	 * @param message
	 *            string to prefix and send
	 */
	protected static void msg(Player player, String message) {
		if (message == null || message.equals("")) {
			return;
		}
		BanVotePlugin.db.i("@" + player.getName() + ": " + message);
		player.sendMessage("[" + ChatColor.AQUA + "BanVote" + ChatColor.WHITE
				+ "] " + message);
	}

	/**
	 * send a prefixed broadcast to the server
	 * 
	 * @param message
	 *            string to prefix and send
	 */
	protected static void brc(String message) {
		if (message == null || message.equals("")) {
			return;
		}
		BanVotePlugin.db.i("@all: " + message);
		Bukkit.broadcastMessage("[" + ChatColor.AQUA + "BanVote"
				+ ChatColor.WHITE + "] " + message);
	}

	/**
	 * turn a array into a string joined by spaces
	 * 
	 * @param args
	 *            the string array to join
	 * @return a string joined by spaces
	 */
	protected String parseStringArray(String[] args) {
		String s = "";
		for (int i = 1; i < args.length; i++) {
			s += s.equals("") ? args[i] : (" " + args[i]);
		}
		return s;
	}
}