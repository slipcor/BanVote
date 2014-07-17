package net.slipcor.banvote.votes;

import net.slipcor.banvote.BanVotePlugin;
import net.slipcor.banvote.api.AVote;
import net.slipcor.banvote.util.Config;
import net.slipcor.banvote.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Set;

public class GeneralVote extends AVote {
    public GeneralVote(Player pTarget, Player player, String sReason,
                       byte bType) {
        super(pTarget, player, sReason, bType);

        BanVotePlugin.instance.brc(Language.INFO_GENERAL_INIT1.toString(player.getName(), type));
        BanVotePlugin.instance.brc(Language.INFO_GENERAL_INIT2.toString(type, sReason));
        BanVotePlugin.instance.brc(Language.INFO_GENERAL_INIT3.toString((bType > 2 ? "custom" : type)
                , type, (bType > 2 ? "custom" : type), type));
        BanVotePlugin.instance.getLogger().info(Language.LOG_STARTED.toString(type,
                player.getName(), sReason));
    }

    @Override
    public void advance() {

        if (state == voteState.MUTETARGET) {
            if (half) {
                calculateResult();
            } else {
                BanVotePlugin.instance.brc(Language.INFO_GENERALSECONDS.toString(
                        String.valueOf(Math.round(Config.stageSeconds / 2))));
            }
            half = !half;
        } else {
            // cooldown finished, remove!
            runner.cancel();
            runner = null;
            PlayerVote.remove(this);
        }
    }


    /**
     * calculate the vote result and commit bans, including timers
     */
    private void calculateResult() {
        BanVotePlugin.debug.info("calculating vote result");
        int iAfk;

        iAfk = getAfk().size();

        final Set<String> afk = getAfk();
        final Set<String> non = getNon(afk);

        int opOverride = 1;

        for (OfflinePlayer player : Bukkit.getOperators()) {
            if (player.getName().equals(this.target)) {
                opOverride = 0;
            }
        }

        if (Bukkit.getPlayer(this.target) != null) {
            opOverride = Bukkit.getPlayer(this.target).hasPermission("banvote.admin") ? 0 : 1;
        }

        final float result = ((float) opOverride) * (Config.yesValue * yes.size()) + (Config.noValue * nope.size())
                + (Config.afkValue * iAfk) + (Config.nonValue * non.size());

        BanVotePlugin.instance.brc(Language.INFO_RESULTYES.toString(type, getNames(yes)));
        BanVotePlugin.instance.brc(Language.INFO_RESULTNO.toString(type, getNames(nope)));

        if (Config.calcPublic) {
            BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARY1.toString(
                    String.valueOf(yes.size()), type, String.valueOf(yes.size() * Config.yesValue), getNames(yes)));
            BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARY2.toString(
                    String.valueOf(afk.size()), type, String.valueOf(afk.size() * Config.afkValue), getNames(afk)));
            BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARY3.toString(
                    String.valueOf(nope.size()), type, String.valueOf(nope.size() * Config.yesValue), getNames(nope)));
            BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARY4.toString(
                    String.valueOf(non.size()), type, String.valueOf(non.size() * Config.yesValue), getNames(non)));
            BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARYLINE.toString());
            BanVotePlugin.instance.brc(Language.INFO_VOTESUMMARYRESULT.toString(String.valueOf(result)));
        } else {
            BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARY1.toString(
                    String.valueOf(yes.size()), type, String.valueOf(yes.size() * Config.yesValue), getNames(yes)));
            BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARY2.toString(
                    String.valueOf(afk.size()), type, String.valueOf(afk.size() * Config.afkValue), getNames(afk)));
            BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARY3.toString(
                    String.valueOf(nope.size()), type, String.valueOf(nope.size() * Config.yesValue), getNames(nope)));
            BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARY4.toString(
                    String.valueOf(non.size()), type, String.valueOf(non.size() * Config.yesValue), getNames(non)));
            BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARYLINE.toString());
            BanVotePlugin.instance.getLogger().info(Language.INFO_VOTESUMMARYRESULT.toString(String.valueOf(result)));
        }

        if (result > Config.validMin) {
            // vote successful
            BanVotePlugin.instance.brc(Language.INFO_VOTERESULTCLEAR.toString(type));
            BanVotePlugin.instance.brc(Language.INFO_VOTERESULTYES.toString(String.valueOf(Math.round(result))));

            state = voteState.POSITIVE;

            byte action = BanVotePlugin.instance.getBVCommand(type).getAction();

            String cmd = BanVotePlugin.instance.getBVCommand(action).getCommand();

            cmd = commandReplace(cmd, "", 0);

            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            return;

        } else if (result < Config.validMax) {
            // vote failed
            BanVotePlugin.instance.brc(Language.INFO_VOTERESULTCLEAR.toString(type));
            BanVotePlugin.instance.brc(Language.INFO_VOTERESULTNO.toString(String.valueOf(Math.round(result))));

            state = voteState.NEGATIVE;
        } else {
            // community failed
            BanVotePlugin.instance.brc(Language.INFO_VOTERESULTNOTCLEAR.toString(type));

            state = voteState.NULL;
        }

        final int interval = 20 * 60 * Config.coolMinutes; // minutes
        restartRunnable(interval);
        // "the total is shown"
    }
}
