package net.slipcor.banvote.api;

/**
 * 
 * ban vote command class
 * 
 * @version 0.0.4
 * 
 * @author slipcor
 *
 */

public class BanVoteCommand {
	
	private final boolean ban;
	private final boolean kick;
	private final boolean noPlayer;
	private final String command;
	private final byte action;
	private final String name;
	
	/**
	 * create a ban vote command instance
	 * @param bBan do we want to ban?
	 * @param bKick do we want to kick?
	 * @param sCommand the command string we want to perform
	 */
	public BanVoteCommand(final boolean bBan, final boolean bKick, final boolean bNoPlayer, final String sCommand, final String sName, final byte bAction) {
		ban = bBan;
		kick = bKick;
		command = sCommand;
		noPlayer = bNoPlayer;
		action = bAction;
		name = sName;
	}
	
	/**
	 * do we want to ban?
	 * @return true if we want to ban, false otherwise
	 */
	public boolean doesBan() {
		return ban;
	}
	
	/**
	 * do we ignore the player name?
	 * @return true if we need no player name, false otherwise
	 */
	public boolean doesIgnorePlayer() {
		return noPlayer;
	}

	/**
	 * do we want to kick?
	 * @return true if we want to ban, false otherwise
	 */
	public boolean doesKick() {
		return kick;
	}
	
	/**
	 * get the action byte
	 * @return the action byte
	 */
	public byte getAction() {
		return action;
	}
	
	/**
	 * get the command to commit
	 * @return the command string to commit
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * get the type name
	 * @return the type name
	 */
	public String getName() {
		return name;
	}
}
