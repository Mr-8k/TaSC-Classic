package data.campaign.quests.boggledLobsterQuest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
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
import data.campaign.econ.boggledTools;

import java.util.*;

public class lobsterQuestCoordinator
{
    public static boolean shouldOfferQuest()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        //Only offer the quest if the four planets exist and the player hasn't already done the quest
        if(boggledTools.getPlanetTokenForQuest("Askonia", "cruor") == null || boggledTools.getPlanetTokenForQuest("Askonia", "umbra") == null || boggledTools.getPlanetTokenForQuest("Askonia", "volturn") == null || boggledTools.getPlanetTokenForQuest("Zagan", "ilm") == null || playerFleet.hasTag("boggled_lobster_quest_playerBeganQuest") || !Global.getSettings().getBoolean("boggledLobsterQuestEnabled"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}