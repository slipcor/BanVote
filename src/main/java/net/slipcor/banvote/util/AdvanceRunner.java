package net.slipcor.banvote.util;

import net.slipcor.banvote.BanVotePlugin;
import net.slipcor.banvote.api.AVote;

/**
 * ban vote runnable class
 *
 * @author slipcor
 * @version v0.0.0
 */

public class AdvanceRunner implements Runnable {
    private final AVote banVote;

    /**
     * create a runnable instance
     *
     * @param vote ban vote class to hand over
     */
    public AdvanceRunner(final AVote vote) {
        banVote = vote;
        BanVotePlugin.debug.info("BanVoteRunnable constructor - "
                + banVote.getVoter() + " => " + banVote.getTarget());
    }

    /**
     * advance the vote
     */
    public void run() {
        BanVotePlugin.debug.info("BanVoteRunnable run - " + banVote.getVoter()
                + " => " + banVote.getTarget());
        banVote.advance();
    }
}
