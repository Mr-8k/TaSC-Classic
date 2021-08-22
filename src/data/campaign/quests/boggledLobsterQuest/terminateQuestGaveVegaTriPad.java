package data.campaign.quests.boggledLobsterQuest;

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
import java.lang.String;

public class terminateQuestGaveVegaTriPad extends BaseCommandPlugin
{
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.removeTag("boggled_lobster_quest_stage1");
        playerFleet.addTag("boggled_lobster_quest_stage2");

        boggledTools.getPlanetTokenForQuest("Askonia", "cruor").removeTag("boggled_lobster_quest_fireVegaRescue");

        //Remove Horn from the comm directory
        CommDirectoryEntryAPI removalTarget = null;
        Iterator allEntries = boggledTools.getPlanetTokenForQuest("Askonia", "cruor").getMarket().getCommDirectory().getEntriesCopy().iterator();
        while(allEntries.hasNext())
        {
            CommDirectoryEntryAPI entity = (CommDirectoryEntryAPI)allEntries.next();
            if(((PersonAPI)entity.getEntryData()).getName().getLast().equals("Horn"))
            {
                removalTarget = entity;
                break;
            }
        }
        allEntries = null;
        boggledTools.getPlanetTokenForQuest("Askonia", "cruor").getMarket().getCommDirectory().removeEntry(removalTarget);

        Misc.makeUnimportant(boggledTools.getPlanetTokenForQuest("Askonia", "cruor"), "boggled_cruor_importance");

        //End the quest
        Iterator allIntel = Global.getSector().getIntelManager().getIntel().iterator();
        while(allIntel.hasNext())
        {
            BaseIntelPlugin intel = (BaseIntelPlugin)allIntel.next();
            if(intel instanceof lobsterIntel)
            {
                intel.endAfterDelay();
            }
        }

        return true;
    }
}