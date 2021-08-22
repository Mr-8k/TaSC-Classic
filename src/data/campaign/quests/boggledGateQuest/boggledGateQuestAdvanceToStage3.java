package data.campaign.quests.boggledGateQuest;

import java.lang.String;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.*;
import com.fs.starfarer.combat.entities.terrain.Planet;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import com.fs.starfarer.rpg.Person;
import data.campaign.econ.boggledTools;

import java.util.*;

public class boggledGateQuestAdvanceToStage3 extends BaseCommandPlugin
{
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if (dialog == null) return false;

        Global.getSector().getPlayerFleet().removeTag("boggled_gate_quest_stage2");
        Global.getSector().getPlayerFleet().addTag("boggled_gate_quest_stage3");

        Misc.makeUnimportant(boggledTools.getPlanetTokenForQuest("Galatia", "ancyra"), "boggled_ancyra_importance");
        Misc.makeImportant(boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4"), "boggled_aeolus_importance");

        //Remove Luyten from the comm directory
        CommDirectoryEntryAPI removalTarget = null;
        Iterator allEntries = boggledTools.getPlanetTokenForQuest("Galatia", "ancyra").getMarket().getCommDirectory().getEntriesCopy().iterator();
        while(allEntries.hasNext())
        {
            CommDirectoryEntryAPI entity = (CommDirectoryEntryAPI)allEntries.next();
            if(((PersonAPI)entity.getEntryData()).getName().getLast().equals("Luyten"))
            {
                removalTarget = entity;
                break;
            }
        }
        allEntries = null;
        boggledTools.getPlanetTokenForQuest("Galatia", "ancyra").getMarket().getCommDirectory().removeEntry(removalTarget);

        Global.getSector().getPlayerFleet().addScript(new WindsEveryFrameScriptAfterLuyten());

        return true;
    }
}