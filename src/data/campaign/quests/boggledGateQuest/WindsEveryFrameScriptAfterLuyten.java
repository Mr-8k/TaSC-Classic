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

public class WindsEveryFrameScriptAfterLuyten implements EveryFrameScript
{
    public WindsEveryFrameScriptAfterLuyten()
    {

    }

    public boolean isDone()
    {
        if(boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4") == null || boggledTools.getPlanetTokenForQuest("Galatia", "ancyra") == null || !(Global.getSector().getPlayerFleet().hasTag("boggled_gate_quest_stage3") || Global.getSector().getPlayerFleet().hasTag("boggled_gate_quest_stage7")))
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

        if(playerFleet.hasTag("boggled_gate_quest_stage3") && !playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && boggledTools.playerCloseToAeolus() && Global.getCurrentState() == GameState.CAMPAIGN)
        {
            Global.getSector().getCampaignUI().showInteractionDialog(new WindsMessageDialogPluginAstral(), boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4"));
        }
        else if(playerFleet.hasTag("boggled_gate_quest_stage7") && !playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && boggledTools.playerCloseToAeolus() && Global.getCurrentState() == GameState.CAMPAIGN)
        {
            Global.getSector().getCampaignUI().showInteractionDialog(new WindsMessageDialogPluginLudd(), boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4"));
        }
    }
}