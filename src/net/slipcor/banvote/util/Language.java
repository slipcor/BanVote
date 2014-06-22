package net.slipcor.banvote.util;

import java.io.File;
import net.slipcor.banvote.BanVotePlugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Language {

	ERROR_INVALIDARGUMENT("error.invalidargument", "&cInvalid vote argument '%1%'!"),
	ERROR_JOINEXEMPT("error.joinexempt", "&cYou can not vote, you did not even see the vote message!"),
	ERROR_MUTED("error.muted", "&cYou are muted, please wait!"),
	ERROR_NOPERMISSION("error.nopermission", "&cYou don't have permission!"),
	ERROR_NOTNUMERIC("error.notnumeric", "&cInvalid argument! Not numeric: %1%"),
	ERROR_NOVOTERUNNING("error.novoterunning", "&cThere is no vote running!"),
	ERROR_PLAYERNOTFOUND("error.playernotfound", "&cPlayer not found: &r%1%"),
	ERROR_REASONREQUIRED("error.reasonrequired", "&cYou need to specify a reason!"),
    ERROR_VOTEPROTECTED("error.voteprotected", "&cYou can not vote against &6%1%&c!"),
    ERROR_VOTECOOLDOWN("error.votecooldown", "&cYou can not vote, please wait &6%1%&c seconds!"),

	BAD_VOTED("bad.voted", "&cVote successful!"),
	
	GOOD_UNBANNED("good.unbanned", "&aPlayer unbanned: &6%1%&c!"),
	GOOD_VOTED("good.voted", "&aVote successful!"),

	INFO_ALREADYVOTED("info.alreadyvoted", "&6You already voted!"),
	INFO_ARGUMENTS("info.arguments", "&6Use one of the following: '%1%'!"),
	INFO_BANNING("info.banning", "&6Banning &c%1%&6."),
	INFO_BANNINGVOTER("info.banningvoter", "&6Banning &a%1%&6."),
	INFO_NOVOTEACTIVE("info.novoteactive", "&6No vote active!"),
	
	INFO_HELP1("info.help1","&6To start a %1% vote type: "),
	INFO_HELP2("info.help2","&b/%1%vote [playername] [reason]"),
	INFO_HELP3("info.help3","&6Once started, type &a/%1%vote [+|yes|true]"
			+ " &6to vote positive"),
	INFO_HELP4("info.help4","&6or &c/%1%vote [-|no|false]"
			+ " &6to vote negative."),
	INFO_HELP5("info.help5","&6A vote against counts as &c-4 &6votes"),
	INFO_HELP6("info.help6","&6A non-vote counts as &c-0.25 &6votes"),

	INFO_GENERAL_INIT1("info.generalinit1","&a%1%&6 started a %2% vote."),
	INFO_GENERAL_INIT2("info.generalinit2","&6%1% reason: &r%2%"),
	INFO_GENERAL_INIT3("info.generalinit3","&6Say &a/%1%vote yes &6for %2%, "
			+ "&c/%3%vote no &6to vote against %4%."),
	
	INFO_GENERALSECONDS("info.generalseconds", "&6%1% seconds until vote is over."),

	INFO_MUTINGSECONDS("info.mutingseconds","&6Muting %1% for %2% seconds!"),
	INFO_NOBANS("info.nobans","&6No bans active!"),

	INFO_RESULTNO("info.resultno","&6Voters &cagainst %1%&6: %2%"),
	INFO_RESULTYES("info.resultyes","&6Voters &afor %1%&6: %2%"),

	INFO_PLAYER_INIT1("info.playerinit1","&a%1%&6 started a %2% vote against &c%3%&6."),
	INFO_PLAYER_INIT2("info.playerinit2","&6%1% reason: &r%2%"),
	INFO_PLAYER_INIT3("info.playerinit3","&6Say &a/%1%vote yes &6for %2%, &c/%3%vote no &6to vote against %4%."),
	INFO_PLAYER_INIT4("info.playerinit4","&6Muting &c%1%&6 for %2% seconds to discuss the %3% vote."),
	
	INFO_PLAYER_STATUS_MUTINGVOTER("info.playermutetarget",
			"&6Muting &a%1%&6 for %2% seconds, so &c%3%&6 can explain."),
	INFO_PLAYER_STATUS_MUTEDVOTER_SECONDS("info.playermutedvoter", "&6%1% seconds until &a%2%&6 is unmuted."),
	INFO_PLAYER_STATUS_MUTEDTARGET_SECONDS("info.playermutedtarget", "&6%1% seconds until &c%2%&6 is unmuted."),
	
	INFO_RELOADED("info.reloaded", "&areloaded!"),
	
	INFO_VERSIONEXPERIMENTAL("info.versionexperimental", "You are using %1%, an experimental version! Latest stable: %2%"),
	INFO_VERSIONOUTDATED("info.versionoutdated", "You are using %1%, an outdated version! Latest: %2%"),
	INFO_VOTEBANNED("info.votebanned","You are vote-banned!"),
	INFO_VOTEBANNEDSECONDS("info.votebannedseconds","You are vote-banned for %1% seconds!"),
	INFO_VOTECOOLINGDOWN("info.votecoolingdown", "&6Vote on &r%1%&6 cooling down!"),

	INFO_VOTERESULTCLEAR("info.voteresultclear","&6%1% vote gave a clear result."),
	INFO_PLAYERVOTERESULTCLEAR("info.playervoteresultclear","&6%1% vote on &c%2%&6 gave a clear result."),
	INFO_VOTERESULTNOTCLEAR("info.voteresultnotclear","&6%1% vote did not give a clear result."),
	INFO_PLAYERVOTERESULTNOTCLEAR("info.playervoteresultnotclear","&6%1% vote on &c%2%&6 did not give a clear result."),

	INFO_VOTERESULTNO("info.voteresultno","&6It &cfailed&6 with a score of %1%."),
	INFO_VOTERESULTYES("info.voteresultyes","&6It &asucceeded&6 with a score of %1%."),
	
	INFO_VOTESUMMARY1("info.votesummary1","%1% %2% votes = %3% :: %4%"),
	INFO_VOTESUMMARY2("info.votesummary2","%1% afk votes = %2% :: %3%"),
	INFO_VOTESUMMARY3("info.votesummary3","%1% anti votes = %2% :: %3%"),
	INFO_VOTESUMMARY4("info.votesummary4","%1% non votes = %2% :: %3%"),
	INFO_VOTESUMMARYLINE("info.votesummaryline","------------------"),
	INFO_VOTESUMMARYRESULT("info.votesummaryresult","Final vote tally = %1%"),
	
	LOG_CONSOLE("log.console", "Console only has access to /release"),
	LOG_DISABLED("log.disabled", "%2% disabled"),
	LOG_ENABLED("log.enabled","%1% enabled"),
	LOG_PLAYER_STATUS_MUTINGVOTER("log.playermutingvoter", "%1% vote: stage 2 - muting the voter"),
	LOG_STARTED("log.started","%1% vote started: [voter: %2%], reason: %3%"),
	LOG_STARTEDTARGET("log.startedtarget","%1% vote started: [voter: %2%], [target: %3%], reason: %4%");

	private final String node;
	private final String def;
	
	private static FileConfiguration cfg = null;
	
	Language(String node, String def) {
		this.node = node;
		this.def = def;
	}
	
	@Override
	public String toString() {
		return ChatColor.translateAlternateColorCodes('&', cfg.getString(node, def));
	}

	public String toString(String... var) {
		String result = toString();
		
		int pos = 1;
		
		for (String string : var) {
			result = result.replace("%"+ pos++ + "%", string);
		}
		
		return result;
	}
	
	public static void init(BanVotePlugin plugin) {
		File file = new File(plugin.getDataFolder(), "lang.yml");
		cfg = new YamlConfiguration();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			cfg.load(file);
			cfg.options().copyDefaults(true);
			for (Language lang : Language.values()) {
				cfg.addDefault(lang.node, lang.def);
			}
			
			cfg.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
