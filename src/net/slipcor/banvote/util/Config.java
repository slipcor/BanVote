package net.slipcor.banvote.util;

import org.bukkit.configuration.ConfigurationSection;

public final class Config {
	private Config() {
	}

	public static int stageSeconds = 60;
	public static float noValue = -4;
	public static float yesValue = 1;
	public static float afkValue = -0.25f;
	public static float nonValue = 0;
	public static float validMin = 0.25f;
	public static float validMax = -0.25f;
	public static int posMinutes = 30;
	public static int negMinutes = 8;
	public static int coolMinutes = 30;
	public static boolean calcPublic = false;
	public static boolean requireReason = false;
	public static float repeatPunishmentFactor = 1.1f;
	public static boolean mute = true;
	public static boolean joincheck = false;
    public static int voteCoolDownMinutes = 30;
    public static String prefix = "&bBanVote&f";

	public static void set(final ConfigurationSection sec) {
		stageSeconds = sec.getInt("StageSeconds", 60);
		noValue = (float) sec.getDouble("NoValue", -4);
		yesValue = (float) sec.getDouble("YesValue", 1);
		afkValue = (float) sec.getDouble("AfkValue", -0.25);
		nonValue = (float) sec.getDouble("NonValue", 0);
		validMin = (float) sec.getDouble("ValidMin", 0.25);
		validMax = (float) sec.getDouble("ValidMax", -0.25);
		posMinutes = sec.getInt("PosMinutes", 30);
		negMinutes = sec.getInt("NegMinutes", 8);
		coolMinutes = sec.getInt("CoolMinutes", 30);
		calcPublic = sec.getBoolean("CalcPublic", false);
		requireReason = sec.getBoolean("RequireReason", false);
		mute = sec.getBoolean("Mute", true);
		repeatPunishmentFactor = (float)sec.getDouble("RepeatPunishmentFactor", 1.1d);
		joincheck = sec.getBoolean("JoinCheck", false);
        voteCoolDownMinutes = sec.getInt("VoteCoolDownMinutes", 30);
        prefix = sec.getString("prefix", "&bBanVote&f");
	}
}
