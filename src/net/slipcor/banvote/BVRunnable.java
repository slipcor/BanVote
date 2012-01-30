package net.slipcor.banvote;

/**
 * ban vote runnable class
 * 
 * @version v0.0.0
 * 
 * @author slipcor
 * 
 */

public class BVRunnable implements Runnable {
	private final BanVote banVote;

	/**
	 * create a runnable instance
	 * 
	 * @param c
	 *            ban vote class to hand over
	 */
	public BVRunnable(BanVote c) {
		banVote = c;
		BanVotePlugin.db.i("BanVoteRunnable constructor - "
				+ banVote.getVoter() + " => " + banVote.getTarget());
	}

	/**
	 * advance the vote
	 */
	public void run() {
		BanVotePlugin.db.i("BanVoteRunnable run - " + banVote.getVoter()
				+ " => " + banVote.getTarget());
		banVote.advance();
	}
}
