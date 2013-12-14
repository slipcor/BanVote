package net.slipcor.banvote.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.slipcor.banvote.BanVotePlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Memory {
	private static FileConfiguration cfg = null;
	private final static Map<String, Integer> lostVotes = new HashMap<String, Integer>();
	
	private Memory() {
	}
	
	public static void init(BanVotePlugin plugin) {
		File file = new File(plugin.getDataFolder(), "memory.yml");
		cfg = new YamlConfiguration();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			cfg.load(file);
			lostVotes.clear();
			for (String node : cfg.getKeys(false)) {
				int value = cfg.getInt(node);
				lostVotes.put(node, value);
			}
			
			cfg.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int increaseCount(String playerName) {
		if (lostVotes.containsKey(playerName)) {
			final int value = lostVotes.get(playerName)+1;
			lostVotes.put(playerName, value);
		}
		lostVotes.put(playerName, 0);
		return lostVotes.get(playerName);
	}
}
