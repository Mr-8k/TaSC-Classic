package data.campaign.quests.boggledGateQuest;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

public class QuestEveryFrameScript implements EveryFrameScript
{
    public QuestEveryFrameScript()
    {

    }

    private boolean questEnabledInSettings()
    {
        if(!Global.getSettings().getBoolean("boggledAstralGateContentEnabled") || !Global.getSettings().getBoolean("boggledAstralGateQuestEnabled"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean isDone()
    {
        if(!questEnabledInSettings() || Global.getSector().getPlayerFleet().hasTag("boggled_gate_quest_started") || boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4") == null || boggledTools.getPlanetTokenForQuest("Galatia", "ancyra") == null || Global.getSector().getStarSystem("Askonia") == null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean runWhilePaused()
    {
        return false;
    }

    public void advance(float var1)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if(playerFleet.hasTag("boggled_gate_quest_fire_cryptic_message") && playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && boggledTools.getDistanceBetweenTokens(playerFleet, Global.getSector().getStarSystem("Askonia").getHyperspaceAnchor()) < 18000f && Global.getSector().getCampaignUI() != null && boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4") != null && boggledTools.getPlanetTokenForQuest("Galatia", "ancyra") != null && Global.getCurrentState() == GameState.CAMPAIGN)
        {
            if(Global.getSector().getCampaignUI().showInteractionDialog(new CrypticMessageDialogPlugin(), boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4")))
            {
                playerFleet.addTag("boggled_gate_quest_started");
                playerFleet.removeTag("boggled_gate_quest_fire_cryptic_message");
                playerFleet.addTag("boggled_gate_quest_stage1");
            }
            else
            {
                playerFleet.removeTag("boggled_gate_quest_fire_cryptic_message");
            }
        }

        if(Global.getSector().getPlayerStats().getLevel() >= Global.getSettings().getInt("boggledMinimumGateQuestLevel") && questEnabledInSettings() && Global.getSector().getPlayerFleet().isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && boggledTools.getDistanceBetweenTokens(playerFleet, Global.getSector().getStarSystem("Askonia").getHyperspaceAnchor()) > 18000f)
        {
            playerFleet.addTag("boggled_gate_quest_fire_cryptic_message");
        }
    }
}