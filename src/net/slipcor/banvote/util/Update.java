package net.slipcor.banvote.util;

import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;

import net.slipcor.banvote.BanVotePlugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * update manager class
 * 
 * -
 * 
 * provides access to update check and methods
 * 
 * @author slipcor
 * 
 * @version v0.0.4
 * 
 */

public final class Update {
	private Update() {
	}
	
	public static boolean msg = false;
	public static boolean outdated = false;
	private static String vOnline;
	private static String vThis;
	private static Plugin instance;
	
	/**
	 * check for updates, update variables
	 */
	public static void updateCheck(final Plugin plugin) {
		
		if (!plugin.getConfig().getBoolean("update", true)) {
			return;
		}
		
		final String pluginUrlString = "http://dev.bukkit.org/server-mods/banvote/files.rss";
	    instance = plugin;
	    try {
	    	final URL url = new URL(pluginUrlString);
	    	final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
	    	doc.getDocumentElement().normalize();
	    	final NodeList nodes = doc.getElementsByTagName("item");
	    	final Node firstNode = nodes.item(0);
	    	if (firstNode.getNodeType() == 1) {
	    		final Element firstElement = (Element)firstNode;
	    		final NodeList firstElementTagName = firstElement.getElementsByTagName("title");
	    		final Element firstNameElement = (Element)firstElementTagName.item(0);
	    		final NodeList firstNodes = firstNameElement.getChildNodes();
	        
	    		String sOnlineVersion = firstNodes.item(0).getNodeValue();
	    		final String sThisVersion = instance.getDescription().getVersion();
	        
		        while(sOnlineVersion.contains(" ")) {
		        	sOnlineVersion = sOnlineVersion.substring(sOnlineVersion.indexOf(' ')+1);
		        }
	        
	        Update.vOnline = sOnlineVersion.replace("v", "");
	        Update.vThis = sThisVersion.replace("v", "");
	        
	        calculateVersions();
	        return;
	      }
	    }
	    catch (Exception localException) {
	    }
	}
	
	/**
	 * calculate the message variables based on the versions
	 */
	private static void calculateVersions() {
		final String[] aOnline = vOnline.split("\\.");
		final String[] aThis = vThis.split("\\.");
		outdated = false;
		
		for (int i=0; i<aOnline.length && i<aThis.length; i++) {
			try {
				final int onlineVersion = Integer.parseInt(aOnline[i]);
				final int localVersion = Integer.parseInt(aThis[i]);
				if (onlineVersion == localVersion) {
					msg = false;
					continue;
				}
				msg = true;
				outdated = (onlineVersion > localVersion);
				
				Update.message(null);
			} catch (Exception e) {
				calculateRadixString(aOnline[i],aThis[i]);
			}
		}
	}
	
	/**
	 * calculate a version part based on letters
	 * @param sOnline the online letter(s)
	 * @param sThis the local letter(s)
	 */
	private static void calculateRadixString(final String sOnline, final String sThis) {
		try {
			final int onlineVersion = Integer.parseInt(sOnline,46);
			final int localVersion = Integer.parseInt(sThis,46);
			if (onlineVersion == localVersion) {
				msg = false;
				return;
			}
			msg = true;
			outdated = (onlineVersion > localVersion);
			
			Update.message(null);
		} catch (Exception e) {
		}
	}
	
	/**
	 * message a player if the version is different
	 * @param player the player to message
	 */
	public static void message(final Player player) {
		if (!(player instanceof Player)) {
			if (msg) {
				if (outdated) {
					BanVotePlugin.instance.getLogger().warning(Language.INFO_VERSIONOUTDATED.toString(vThis,vOnline));
				} else {
					BanVotePlugin.instance.getLogger().warning(Language.INFO_VERSIONEXPERIMENTAL.toString(vThis,vOnline));
				}
			}
		}
		if (!msg) {
			return;
		}

		if (outdated) {
			BanVotePlugin.instance.msg(player, Language.INFO_VERSIONOUTDATED.toString(colorize("v"+vThis,'o'),colorize("v"+vOnline,'s')));
		} else {
			BanVotePlugin.instance.msg(player, Language.INFO_VERSIONEXPERIMENTAL.toString(colorize("v"+vThis,'e'),colorize("v"+vOnline,'s')));
		}
	}
	
	/**
	 * colorize a given string based on a char
	 * @param string the string to colorize
	 * @param cChar the char that decides what color
	 * @return a colorized string
	 */
	private static String colorize(final String str, final char cChar) {
		String string = str;
		if (cChar == 'o') {
			string = ChatColor.RED + string + ChatColor.WHITE;
		} else if (cChar == 'e') {
			string = ChatColor.GOLD + string + ChatColor.WHITE;
		} else if (cChar == 's') {
			string = ChatColor.GREEN + string + ChatColor.WHITE;
		}
		return string;
	}
}
