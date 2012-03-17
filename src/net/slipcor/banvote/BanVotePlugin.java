package net.slipcor.banvote;

import java.util.HashMap;
import java.util.HashSet;

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

public class BanVotePlugin extends JavaPlugin {
	protected static BanVotePlugin instance;
	protected final BVListener listen = new BVListener();
	protected static HashMap<Integer, BanVoteResult> results = new HashMap<Integer, BanVoteResult>();
	protected static HashSet<BanVote> votes = new HashSet<BanVote>();

	protected static BVDebugger db;
	protected static BVLogger log = new BVLogger();
	protected static HashMap<String, BanVoteCommand> commands = new HashMap<String, BanVoteCommand>();

	@Override
	public void onEnable() {
		instance = this;
		db = new BVDebugger(getConfig().getBoolean("debug", false));
		db.i("enabling...");
		db.i("registering events...");
		getServer().getPluginManager().registerEvents(listen, this);

		getConfig().options().copyDefaults(true);
		saveConfig();

		BanVote.set(getConfig().getConfigurationSection("settings")
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
				BanVoteResult.add(s);
			}
			saveConfig();
		}
		commands.clear();
		if (getConfig().get("commands") != null) {
			for (Object val : getConfig().getConfigurationSection("commands")
					.getValues(false).keySet()) {
				commands.put(val.toString(), new BanVoteCommand(getConfig().getBoolean("commands."+val.toString()+".ban"),getConfig().getBoolean("commands."+val.toString()+".kick"),getConfig().getString("commands."+val.toString()+".command")));
			}
		}
		
		Tracker tracker = new Tracker(this);
        tracker.start();
		BVUpdate.updateCheck(this);
		
		log.i(getDescription().getVersion() + " enabled");
	}

	@Override
	public void onDisable() {
		// TODO: save ban stats/times
		db.i("disabling...");
		Tracker.stop();
		db.i("canceling tasks...");
		Bukkit.getScheduler().cancelTasks(this);
		log.i(getDescription().getVersion() + " disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String sCmd,
			String[] args) {
		if (sCmd.equals("release")) {
			return onAdminCommand(sender, args);
		}
		
		if (!(sender instanceof Player)) {
			db.i("onCommand: sent from console");
			sender.sendMessage("[BanVote] Console only has access to /release");
			return true;
		}
		
		byte b = 0;
		Player player = (Player) sender;

		if (!sCmd.equals("banvote") && !sCmd.equals("mutevote")
				&& !sCmd.equals("kickvote") && !sCmd.equals("customvote")) {
			return false;
		}

		if (sCmd.startsWith("ban")) {
			b = 2;
		} else if (sCmd.startsWith("kick")) {
			b = 1;
		} else if (sCmd.startsWith("mute")) {
			b = 0;
		} else {
			b = getCommandNumber(args[1]);
		}

		String type = b<3?BanVote.parse(b):sCmd.substring(0, sCmd.length()-4);

		db.i("onCommand: " + type + "vote command");

		if (!player.hasPermission(type + "vote.vote")) {
			BanVotePlugin.msg(player, "§cYou don't have permission!");
			return true;
		}

		if (args == null || args.length < 1) {
			return false;
		}

		db.i("onCommand: args: " + parseStringArray(args,(byte)0));

		if (args.length > 1) {
			BanVote.init(args[0], args, player, b);
			return true;
		}

		if (args[0].equalsIgnoreCase("help")) {
			msg(player, "§6To start a vote to " + type + " a player type: ");
			msg(player, "§b/" + type + "vote [playername] [reason]");
			msg(player, "§6Once started, type " + "§a/" + type
					+ "vote [+|yes|true]" + " §6to vote to ban");
			msg(player, "§6or §c/" + type
					+ "vote [-|no|false] §6to vote not to " + type + ".");
			msg(player, "§6A vote against counts as "
					+ "§c-4 §6votes towards a " + type + "");
			msg(player, "§6A non-vote counts as "
					+ "§c-0.25 §6votes towards a " + type + "");
			return true;
		}

		BanVote.commit(args[0], player);
		return true;
	}

	protected byte getCommandNumber(String sCmd) {
		byte i = 3;
		sCmd = sCmd.substring(0,sCmd.length()-4);
		System.out.print("getCommandNumber: "+sCmd);
		
		for (String name : commands.keySet()) {
			System.out.print(name + " = " + sCmd + "?");
			if (name.equals(sCmd)) {

				System.out.print("return "+String.valueOf(i));
				return i;
			}
			i++;
		}
		System.out.print("return -1");
		return -1;
	}
	
	protected String getCommand(byte b) {
		for (BanVoteCommand cmd : commands.values()) {
			if (b-- == 3) {
				return cmd.getCommand();
			}
		}
		return null;
	}

	public String getCommandName(byte bType) {
		for (String name : commands.keySet()) {
			System.out.print("-command: "+name+" = "+String.valueOf(bType)+"?");
			if (bType-- == 3) {
				System.out.print("return: "+name);
				return name;
			}
		}
		System.out.print("return: null");
		return null;
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
	private boolean onAdminCommand(CommandSender sender, String[] args) {
		if (args == null || args.length != 1) {
			return false;
		}

		if (!sender.hasPermission("banvote.admin")) {
			BanVotePlugin.msg(sender, "§cYou don't have permission!");
			return true;
		}

		if (args[0].equals("list")) {
			for (int i : results.keySet()) {
				BanVoteResult ban = results.get(i);
				BanVotePlugin.msg(sender, "§6#" + i + ": " + ban.getInfo());
			}
			if (results.size() < 1) {
				BanVotePlugin.msg(sender, "§6No bans active!");
			}
			return true;
		}

		String banPlayer = "";
		try {
			int i = Integer.parseInt(args[0]);
			banPlayer = results.get(i).getResultPlayerName();
			BanVoteResult.remove(i);
		} catch (Exception e) {
			BanVotePlugin.msg(sender, "§cInvalid argument! Not a number: "
					+ args[0]);
			return true;
		}
		BanVotePlugin.msg(sender, "§aUnbanned: " + banPlayer);
		return true;
	}

	/**
	 * send a prefixed message to a player
	 * 
	 * @param sender
	 *            player/console to send the message to
	 * @param message
	 *            string to prefix and send
	 */
	protected static void msg(CommandSender sender, String message) {
		if (message == null || message.equals("")) {
			return;
		}
		if (sender instanceof Player) {
			BanVotePlugin.db.i("@" + sender.getName() + ": " + message);
		}
		sender.sendMessage("[§bBanVote§f] " + message);
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
		Bukkit.broadcastMessage("[§bBanVote§f] " + message);
	}

	/**
	 * turn a array into a string joined by spaces
	 * 
	 * @param args
	 *            the string array to join
	 * @param b 
	 * @return a string joined by spaces
	 */
	protected String parseStringArray(String[] args, byte b) {
		String s = "";
		for (int i = (b<3?1:2); i < args.length; i++) {
			s += s.equals("") ? args[i] : (" " + args[i]);
		}
		return s;
	}
}