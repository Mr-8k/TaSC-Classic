package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.CampaignObjective;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.*;
import com.fs.starfarer.campaign.econ.Market;
import com.fs.starfarer.combat.entities.terrain.Planet;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.campaign.econ.industries.*;
import data.campaign.quests.boggledGateQuest.BoggledGateMarkerIntel;
import data.campaign.quests.boggledLobsterQuest.lobsterIntel;
import org.json.JSONException;
import java.lang.String;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class boggledTools
{
    public static float getDistanceBetweenPoints(float x1, float y1, float x2, float y2)
    {
        return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public static float getDistanceBetweenTokens(SectorEntityToken tokenA, SectorEntityToken tokenB)
    {
        return getDistanceBetweenPoints(tokenA.getLocation().x, tokenA.getLocation().y, tokenB.getLocation().x, tokenB.getLocation().y);
    }

    public static float getAngle(float focusX, float focusY, float playerX, float playerY)
    {
        float angle = (float) Math.toDegrees(Math.atan2(focusY - playerY, focusX - playerX));

        //Not entirely sure what math is going on behind the scenes but this works to get the station to spawn next to the player
        angle = angle + 180f;

        return angle;
    }

    public static float getAngleFromPlayerFleet(SectorEntityToken target)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        return getAngle(target.getLocation().x, target.getLocation().y, playerFleet.getLocation().x, playerFleet.getLocation().y);
    }

    public static float getAngleFromEntity(SectorEntityToken entity, SectorEntityToken target)
    {
        return getAngle(target.getLocation().x, target.getLocation().y, entity.getLocation().x, entity.getLocation().y);
    }

    public static void surveyAll(MarketAPI market)
    {
        for (MarketConditionAPI condition : market.getConditions())
        {
            condition.setSurveyed(true);
        }
    }

    public static void refreshSupplyAndDemand(MarketAPI market)
    {
        //Refreshes supply and demand for each industry on the market
        List<Industry> industries = market.getIndustries();
        for (int i = 0; i < industries.size(); i++)
        {
            industries.get(i).doPreSaveCleanup();
            industries.get(i).doPostSaveRestore();
        }
    }

    public static float getRandomOrbitalAngleFloat(float min, float max)
    {
        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    public static boolean playerMarketInSystem(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getMarket() != null && entity.getMarket().isPlayerOwned())
            {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestPlayerMarketToken(SectorEntityToken playerFleet)
    {
        if(!playerMarketInSystem(playerFleet))
        {
            return null;
        }
        else
        {
            ArrayList<SectorEntityToken> allPlayerMarketsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if (entity.getMarket() != null && entity.getMarket().isPlayerOwned())
                {
                    allPlayerMarketsInSystem.add(entity);
                }
            }

            SectorEntityToken closestMarket = null;
            Iterator checkDistancesOfPlayerMarkets = allPlayerMarketsInSystem.iterator();
            while(checkDistancesOfPlayerMarkets.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)checkDistancesOfPlayerMarkets.next();
                if (closestMarket == null)
                {
                    closestMarket = entity;
                }
                else if(getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestMarket, playerFleet))
                {
                    closestMarket = entity;
                }
            }

            return closestMarket;
        }
    }

    public static boolean gasGiantInSystem(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken planet = (SectorEntityToken) allEntitiesInSystem.next();
            if (planet instanceof PlanetAPI && ((PlanetAPI) planet).isGasGiant())
            {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestGasGiantToken(SectorEntityToken playerFleet)
    {
        if(!gasGiantInSystem(playerFleet))
        {
            return null;
        }
        else
        {
            ArrayList<SectorEntityToken> allGasGiantsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken planet = (SectorEntityToken) allEntitiesInSystem.next();
                if (planet instanceof PlanetAPI && ((PlanetAPI) planet).isGasGiant())
                {
                    allGasGiantsInSystem.add(planet);
                }
            }

            SectorEntityToken closestGasGiant = null;
            Iterator checkDistancesOfGasGiants = allGasGiantsInSystem.iterator();
            while(checkDistancesOfGasGiants.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)checkDistancesOfGasGiants.next();
                if (closestGasGiant == null)
                {
                    closestGasGiant = entity;
                }
                else if(getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestGasGiant, playerFleet))
                {
                    closestGasGiant = entity;
                }
            }

            return closestGasGiant;
        }
    }

    public static boolean colonizableStationInSystem(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if (entity.hasTag("station") && entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION))
            {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestColonizableStationInSystem(SectorEntityToken playerFleet)
    {
        if(!colonizableStationInSystem(playerFleet))
        {
            return null;
        }
        else
        {
            ArrayList<SectorEntityToken> allColonizableStationsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity.hasTag("station") && entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION))
                {
                    allColonizableStationsInSystem.add(entity);
                }
            }

            SectorEntityToken closestStation = null;
            Iterator checkDistancesOfStations = allColonizableStationsInSystem.iterator();
            while(checkDistancesOfStations.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)checkDistancesOfStations.next();
                if (closestStation == null)
                {
                    closestStation = entity;
                }
                else if(getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestStation, playerFleet))
                {
                    closestStation = entity;
                }
            }

            return closestStation;
        }
    }

    public static boolean stationInSystem(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if (entity.hasTag("station"))
            {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestStationInSystem(SectorEntityToken playerFleet)
    {
        if(!stationInSystem(playerFleet))
        {
            return null;
        }
        else
        {
            ArrayList<SectorEntityToken> allStationsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity.hasTag("station") && !entity.hasTag("boggled_astral_gate"))
                {
                    allStationsInSystem.add(entity);
                }
            }

            SectorEntityToken closestStation = null;
            Iterator checkDistancesOfStations = allStationsInSystem.iterator();
            while(checkDistancesOfStations.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)checkDistancesOfStations.next();
                if (closestStation == null)
                {
                    closestStation = entity;
                }
                else if(getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestStation, playerFleet))
                {
                    closestStation = entity;
                }
            }

            return closestStation;
        }
    }

    public static void addAstralGate(StarSystemAPI system)
    {
        Random random = new Random();
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        LinkedHashMap<BaseThemeGenerator.LocationType, Float> weights = new LinkedHashMap();
        weights.put(BaseThemeGenerator.LocationType.STAR_ORBIT, 10.0F);
        weights.put(BaseThemeGenerator.LocationType.OUTER_SYSTEM, 10.0F);
        weights.put(BaseThemeGenerator.LocationType.L_POINT, 10.0F);
        weights.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 2.0F);

        BaseThemeGenerator.EntityLocation loc = null;
        boolean createdCloseGate = false;
        for(int i = 0; i < 10000; i++)
        {
            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> locs = BaseThemeGenerator.getLocations(random, system, (Set)null, 100.0F, weights);
            loc = (BaseThemeGenerator.EntityLocation)locs.pick();
            BaseThemeGenerator.AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, "boggled_astral_gate", "neutral");
            if(getDistanceBetweenPoints(playerFleet.getLocation().x, playerFleet.getLocation().y, added.entity.getLocation().x, added.entity.getLocation().y) < 500f)
            {
                createdCloseGate = true;
                system.removeEntity(added.entity);
                break;
            }

            system.removeEntity(added.entity);
        }

        if(!createdCloseGate)
        {
            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> locs = BaseThemeGenerator.getLocations(random, system, (Set)null, 100.0F, weights);
            loc = (BaseThemeGenerator.EntityLocation)locs.pick();
            BaseThemeGenerator.AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, "boggled_astral_gate", "neutral");
            system.removeEntity(added.entity);
        }

        BaseThemeGenerator.AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, "boggled_astral_gate", "neutral");
        if (added != null)
        {
            BaseThemeGenerator.convertOrbitPointingDown(added.entity);
        }
    }

    public static boolean astralGateInSystem(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if (entity.hasTag("boggled_astral_gate") || entity.hasTag("GatesAwakened__gate_activated"))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean gatekeeperStationInSystem(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if (entity.hasTag("boggled_gatekeeper_station"))
            {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestAstralGateInSystem(SectorEntityToken playerFleet)
    {
        if(!astralGateInSystem(playerFleet))
        {
            return null;
        }
        else
        {
            ArrayList<SectorEntityToken> allAstralGatesInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity.hasTag("boggled_astral_gate") || entity.hasTag("GatesAwakened__gate_activated"))
                {
                    allAstralGatesInSystem.add(entity);
                }
            }

            SectorEntityToken closestAstralGate = null;
            Iterator checkDistancesOfAstralGates = allAstralGatesInSystem.iterator();
            while(checkDistancesOfAstralGates.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)checkDistancesOfAstralGates.next();
                if (closestAstralGate == null)
                {
                    closestAstralGate = entity;
                }
                else if(getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestAstralGate, playerFleet))
                {
                    closestAstralGate = entity;
                }
            }

            return closestAstralGate;
        }
    }

    public static SectorEntityToken getClosestGatekeeperStationInSystem(SectorEntityToken playerFleet)
    {
        if(!gatekeeperStationInSystem(playerFleet))
        {
            return null;
        }
        else
        {
            ArrayList<SectorEntityToken> allGatekeeperStationsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity.hasTag("boggled_gatekeeper_station"))
                {
                    allGatekeeperStationsInSystem.add(entity);
                }
            }

            SectorEntityToken closestGatekeeperStation = null;
            Iterator checkDistancesOfGatekeeperStations = allGatekeeperStationsInSystem.iterator();
            while(checkDistancesOfGatekeeperStations.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)checkDistancesOfGatekeeperStations.next();
                if (closestGatekeeperStation == null)
                {
                    closestGatekeeperStation = entity;
                }
                else if(getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestGatekeeperStation, playerFleet))
                {
                    closestGatekeeperStation = entity;
                }
            }

            return closestGatekeeperStation;
        }
    }

    public static boolean gateJumpPermittedByControllingFaction(SectorEntityToken gate)
    {
        String gateFaction = gate.getFaction().getId();
        if(gateFaction.equals("player") || gateFaction.equals("neutral") || gate.getFaction().getRelationshipLevel(Global.getSector().getPlayerFaction()) == RepLevel.FRIENDLY || gate.getFaction().getRelationshipLevel(Global.getSector().getPlayerFaction()) == RepLevel.FAVORABLE || gate.getFaction().getRelationshipLevel(Global.getSector().getPlayerFaction()) == RepLevel.COOPERATIVE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static HashMap<String, Integer> getControllingFactionForGiftGate(StarSystemAPI system)
    {
        //Create return variable
        HashMap<String, Integer> retHash = new HashMap<>();

        //Load ArrayList with list of all factions with a market in the system
        ArrayList<String> factionsWithMarketInSystem = getListOfFactionsWithMarketInSystem(system);

        //Create companion array
        ArrayList<Integer> totalPopulationSizeCompanionArrayList = getCompanionListOfTotalMarketPopulation(system, factionsWithMarketInSystem);

        if(totalPopulationSizeCompanionArrayList.size() == 0)
        {
            retHash.put("boggled_none", 0);
            return retHash;
        }
        else
        {
            int maxPop = Collections.max(totalPopulationSizeCompanionArrayList);
            int numElementsWithMaxPopNumber = 0;

            Iterator allElements = totalPopulationSizeCompanionArrayList.iterator();
            while(allElements.hasNext())
            {
                Integer ie = (Integer) allElements.next();
                if(ie == maxPop)
                {
                    numElementsWithMaxPopNumber++;
                }
            }

            if(numElementsWithMaxPopNumber != 1)
            {
                retHash.put("boggled_tied", 0);
                return retHash;
            }
            else
            {
                for(int i = 0; i < totalPopulationSizeCompanionArrayList.size(); i++)
                {
                    if(totalPopulationSizeCompanionArrayList.get(i) == maxPop)
                    {
                        retHash.put(factionsWithMarketInSystem.get(i), totalPopulationSizeCompanionArrayList.get(i));
                        return retHash;
                    }
                }
            }

            return null;
        }
    }

    public static ArrayList<String> getListOfFactionsWithMarketInSystem(StarSystemAPI system)
    {
        ArrayList<String> factionsWithMarketInSystem = new ArrayList<String>();

        Iterator allMarkets = Global.getSector().getEconomy().getMarkets(system).iterator();
        while(allMarkets.hasNext())
        {
            MarketAPI market = (MarketAPI) allMarkets.next();
            if(!factionsWithMarketInSystem.contains(market.getFactionId()))
            {
                factionsWithMarketInSystem.add(market.getFactionId());
            }
        }

        return factionsWithMarketInSystem;
    }

    public static ArrayList<Integer> getCompanionListOfTotalMarketPopulation(StarSystemAPI system, ArrayList<String> factions)
    {
        ArrayList<Integer> totalFactionMarketSize = new ArrayList<Integer>();
        int buffer = 0;
        Iterator allMarkets = null;

        Iterator allFactionsWithMarketInSystem = factions.iterator();
        while(allFactionsWithMarketInSystem.hasNext())
        {
            String faction = (String) allFactionsWithMarketInSystem.next();
            allMarkets = Global.getSector().getEconomy().getMarkets(system).iterator();
            while(allMarkets.hasNext())
            {
                MarketAPI market = (MarketAPI) allMarkets.next();
                if(market.getFactionId().equals(faction))
                {
                    buffer = buffer + market.getSize();
                }
            }

            totalFactionMarketSize.add(buffer);
            buffer = 0;
        }

        return totalFactionMarketSize;
    }

    public static List<SectorEntityToken> listOfActiveAstralGates = null;
    public static int lastDayChecked = 0;

    public static void updateListofActiveAstralGates(boolean forceUpdate)
    {
        if(listOfActiveAstralGates == null || forceUpdate)
        {
            listOfActiveAstralGates = new ArrayList<SectorEntityToken>();
            Iterator allStarSystems = Global.getSector().getStarSystems().iterator();
            while(allStarSystems.hasNext())
            {
                StarSystemAPI system = (StarSystemAPI) allStarSystems.next();
                if(!system.isEnteredByPlayer())
                {
                    continue;
                }
                Iterator allEntitiesInSystem = system.getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                    if((entity.hasTag("boggled_astral_gate") || entity.hasTag("GatesAwakened__gate_activated")) && gateJumpPermittedByControllingFaction(entity))
                    {
                        listOfActiveAstralGates.add(entity);
                        confirmGateMarkerInPlace(entity);
                    }
                }
            }
        }
        else
        {
            CampaignClockAPI clock = Global.getSector().getClock();
            if(lastDayChecked != clock.getDay() || !Global.getSettings().getBoolean("boggledAstralGateConstructionLowPerformanceModeEnabled"))
            {
                listOfActiveAstralGates = new ArrayList<SectorEntityToken>();
                Iterator allStarSystems = Global.getSector().getStarSystems().iterator();
                while(allStarSystems.hasNext())
                {
                    StarSystemAPI system = (StarSystemAPI) allStarSystems.next();
                    if(!system.isEnteredByPlayer())
                    {
                        continue;
                    }
                    Iterator allEntitiesInSystem = system.getAllEntities().iterator();
                    while(allEntitiesInSystem.hasNext())
                    {
                        SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                        if((entity.hasTag("boggled_astral_gate") || entity.hasTag("GatesAwakened__gate_activated")) && gateJumpPermittedByControllingFaction(entity))
                        {
                            listOfActiveAstralGates.add(entity);
                            confirmGateMarkerInPlace(entity);
                        }
                    }
                }

                lastDayChecked = clock.getDay();
                //sendDebugIntelMessage("Updated the gate list");
            }
        }
    }

    public static void confirmGateMarkerInPlace(SectorEntityToken gateToken)
    {
        Iterator allPlayerQuests = Global.getSector().getIntelManager().getIntel().iterator();
        while(allPlayerQuests.hasNext())
        {
            IntelInfoPlugin intel = (IntelInfoPlugin) allPlayerQuests.next();
            if(intel instanceof BoggledGateMarkerIntel && ((BoggledGateMarkerIntel) intel).getLinkedGateSectorEntityToken().equals(gateToken))
            {
                return;
            }
        }

        //If we loop through all quests and don't find the marker quest for this gate, add the marker quest
        Global.getSector().getIntelManager().addIntel(new BoggledGateMarkerIntel(gateToken));
    }

    public static List<SectorEntityToken> getListOfActiveAstralGates()
    {
        /*
        List<SectorEntityToken> listOfActiveAstralGates = new ArrayList<SectorEntityToken>();
        Iterator allStarSystems = Global.getSector().getStarSystems().iterator();
        while(allStarSystems.hasNext())
        {
            StarSystemAPI system = (StarSystemAPI) allStarSystems.next();
            if(!system.isEnteredByPlayer())
            {
                continue;
            }
            Iterator allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if((entity.hasTag("boggled_astral_gate") || entity.hasTag("GatesAwakened__gate_activated")) && gateJumpPermittedByControllingFaction(entity))
                {
                    listOfActiveAstralGates.add(entity);
                }
            }
        }

         */

        updateListofActiveAstralGates(false);
        return  listOfActiveAstralGates;

        //Below code is actually significantly slower than my custom code above
        //Iterator allGatesWithTargetTag = Global.getSector().getEntitiesWithTag("boggled_astral_gate").iterator();
    }

    public static void removeGateIdTag(SectorEntityToken astralGateToken)
    {
        Iterator allTagsOnGate = astralGateToken.getTags().iterator();
        while(allTagsOnGate.hasNext())
        {
            String tag = (String)allTagsOnGate.next();
            if(tag.contains("boggled_astral_gate_id_"))
            {
                astralGateToken.removeTag(tag);
            }
        }
    }

    public static void removeGateIdTagFromPlayer()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        String tagToRemove = null;
        Iterator allTagsOnPlayer = playerFleet.getTags().iterator();
        while(allTagsOnPlayer.hasNext())
        {
            String tag = (String)allTagsOnPlayer.next();
            if(tag.contains("boggled_astral_gate_target_id_"))
            {
                tagToRemove = tag;
                break;
            }
        }
        allTagsOnPlayer = null;

        if(tagToRemove != null)
        {
            playerFleet.removeTag(tagToRemove);
            removeGateIdTagFromPlayer();
        }

    }

    public static int getTargetGateIdFromPlayer()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        Iterator allTagsOnPlayer = playerFleet.getTags().iterator();
        while(allTagsOnPlayer.hasNext())
        {
            String tag = (String)allTagsOnPlayer.next();
            if(tag.contains("boggled_astral_gate_target_id_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_astral_gate_target_id_", ""));
            }
        }

        return -1;
    }

    public static boolean playerHasGateIdTag()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        Iterator allTagsOnPlayer = playerFleet.getTags().iterator();
        while(allTagsOnPlayer.hasNext())
        {
            String tag = (String)allTagsOnPlayer.next();
            if(tag.contains("boggled_astral_gate_target_id_"))
            {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getTargetGateToken(int targetGateId)
    {
        List<SectorEntityToken> listOfActiveAstralGates = getListOfActiveAstralGates();
        return listOfActiveAstralGates.get(targetGateId);
    }

    public static boolean planetInSystem(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken planet = (SectorEntityToken) allEntitiesInSystem.next();
            if (planet instanceof PlanetAPI && !getPlanetType(((PlanetAPI)planet)).equals("star"))
            {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestPlanetToken(SectorEntityToken playerFleet)
    {
        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return null;
        }

        if(!planetInSystem(playerFleet))
        {
            return null;
        }
        else
        {
            ArrayList<SectorEntityToken> allPlanetsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity instanceof PlanetAPI && !getPlanetType(((PlanetAPI)entity)).equals("star"))
                {
                    allPlanetsInSystem.add(entity);
                }
            }

            SectorEntityToken closestPlanet = null;
            Iterator checkDistancesOfPlanets = allPlanetsInSystem.iterator();
            while(checkDistancesOfPlanets.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)checkDistancesOfPlanets.next();
                if (closestPlanet == null)
                {
                    closestPlanet = entity;
                }
                else if(getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestPlanet, playerFleet))
                {
                    closestPlanet = entity;
                }
            }

            return closestPlanet;
        }
    }

    public static String getPlanetType(PlanetAPI planet)
    {
        //Sets the spec planet type, but not the actual planet type. Need the API fix from Alex to correct this.
        //All code should rely on this function to get the planet type so it should work without bugs.
        //String planetType = planet.getTypeId();
        String planetType = planet.getSpec().getPlanetType();

        if(planetType.equals("nebula_center_old") || planetType.equals("nebula_center_average") || planetType.equals("nebula_center_young") || planetType.equals("star_neutron") || planetType.equals("black_hole") || planetType.equals("star_yellow") || planetType.equals("star_white") || planetType.equals("star_blue_giant") || planetType.equals("star_blue_supergiant") || planetType.equals("star_orange") || planetType.equals("star_orange_giant") || planetType.equals("star_red_supergiant") || planetType.equals("star_red_giant") || planetType.equals("star_red_dwarf") || planetType.equals("star_browndwarf") || planetType.equals("US_star_blue_giant") || planetType.equals("US_star_yellow") || planetType.equals("US_star_orange") || planetType.equals("US_star_red_giant") || planetType.equals("US_star_white") || planetType.equals("US_star_browndwarf") || planetType.equals("SCY_star") || planetType.equals("SCY_companionStar") || planetType.equals("SCY_wormholeUnder") || planetType.equals("SCY_wormholeA") || planetType.equals("SCY_wormholeB") || planetType.equals("SCY_wormholeC") || planetType.equals("istl_sigmaworld") || planetType.equals("istl_dysonshell") || planetType.equals("vayra_star_blue") || planetType.equals("vayra_star_brown") || planetType.equals("vayra_star_yellow_white"))
        {
            return "star";
        }
        else if(planetType.equals("gas_giant") || planetType.equals("ice_giant") || planetType.equals("US_gas_giant") || planetType.equals("US_gas_giantB") || planetType.equals("fds_gas_giant") || planetType.equals("SCY_tartarus") || planetType.equals("galaxytigers_gas_giant"))
        {
            return "gas_giant";
        }
        else if(planetType.equals("barren") || planetType.equals("barren_castiron") || planetType.equals("barren2") || planetType.equals("barren3") || planetType.equals("barren_venuslike") || planetType.equals("rocky_metallic") || planetType.equals("rocky_unstable") || planetType.equals("rocky_ice") || planetType.equals("irradiated") || planetType.equals("barren-bombarded") || planetType.equals("US_acid") || planetType.equals("US_acidRain") || planetType.equals("US_acidWind") || planetType.equals("US_barrenA") || planetType.equals("US_barrenB") || planetType.equals("US_barrenC") || planetType.equals("US_barrenD") || planetType.equals("US_barrenE") || planetType.equals("US_barrenF") || planetType.equals("US_azure") || planetType.equals("US_burnt") || planetType.equals("US_artificial") || planetType.equals("haunted") || planetType.equals("hmi_crystalline") || planetType.equals("SCY_miningColony") || planetType.equals("SCY_burntPlanet") || planetType.equals("SCY_moon") || planetType.equals("SCY_redRock"))
        {
            return "barren";
        }
        else if(planetType.equals("toxic") || planetType.equals("toxic_cold") || planetType.equals("US_green") || planetType.equals("SCY_acid"))
        {
            return "toxic";
        }
        else if(planetType.equals("desert") || planetType.equals("desert1") || planetType.equals("arid") || planetType.equals("barren-desert") || planetType.equals("US_dust") || planetType.equals("US_desertA") || planetType.equals("US_desertB") || planetType.equals("US_desertC") || planetType.equals("US_red") || planetType.equals("US_redWind") || planetType.equals("US_lifelessArid") || planetType.equals("US_arid") || planetType.equals("US_crimson") || planetType.equals("US_storm") || planetType.equals("fds_desert") || planetType.equals("SCY_homePlanet") || planetType.equals("istl_aridbread") || planetType.equals("vayra_bread") || planetType.equals("US_auric") || planetType.equals("US_auricCloudy"))
        {
            return "desert";
        }
        else if(planetType.equals("terran") || planetType.equals("terran-eccentric") || planetType.equals("US_lifeless") || planetType.equals("US_alkali") || planetType.equals("US_continent") || planetType.equals("US_magnetic") || planetType.equals("US_water") || planetType.equals("US_waterB"))
        {
            return "terran";
        }
        else if(planetType.equals("water"))
        {
            return "water";
        }
        else if(planetType.equals("tundra") || planetType.equals("US_purple") || planetType.equals("fds_tundra") || planetType.equals("galaxytigers_tundra"))
        {
            return "tundra";
        }
        else if(planetType.equals("jungle") || planetType.equals("US_jungle"))
        {
            return "jungle";
        }
        else if(planetType.equals("frozen") || planetType.equals("frozen1") || planetType.equals("frozen2") || planetType.equals("frozen3") || planetType.equals("cryovolcanic") || planetType.equals("US_iceA") || planetType.equals("US_iceB") || planetType.equals("US_blue") || planetType.equals("fds_cryovolcanic") || planetType.equals("fds_frozen"))
        {
            return "frozen";
        }
        else if(planetType.equals("lava") || planetType.equals("lava_minor") || planetType.equals("US_lava") || planetType.equals("US_volcanic") || planetType.equals("fds_lava"))
        {
            return "volcanic";
        }
        else if(planetType.equals("boggled_arcology"))
        {
            return "arcology";
        }
        else
        {
            return "unknown";
        }
    }

    public static boolean getCreateMirrorsOrShades(MarketAPI market)
    {
        //Return true for mirrors, false for shades

        if(market.hasCondition("poor_light"))
        {
            return true;
        }

        if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("desert") || boggledTools.getPlanetType(market.getPlanetEntity()).equals("jungle") || boggledTools.getPlanetType(market.getPlanetEntity()).equals("volcano"))
        {
            return false;
        }

        if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("tundra") || boggledTools.getPlanetType(market.getPlanetEntity()).equals("frozen"))
        {
            return true;
        }

        if(market.hasCondition("cold") || market.hasCondition("very_cold"))
        {
            return true;
        }

        if(market.hasCondition("hot") || market.hasCondition("very_hot"))
        {
            return false;
        }

        return true;
    }

    public static boolean hasEuteckImprovableConditions(PlanetAPI planet)
    {
        if(planet.getMarket().hasCondition("US_storm") || planet.getMarket().hasCondition("no_atmosphere") || planet.getMarket().hasCondition("thin_atmosphere") || planet.getMarket().hasCondition("toxic_atmosphere") || planet.getMarket().hasCondition("dense_atmosphere") || planet.getMarket().hasCondition("extreme_weather") || planet.getMarket().hasCondition("irradiated") || planet.getMarket().hasCondition("inimical_biosphere") || planet.getMarket().hasCondition("water_surface") || planet.getMarket().hasCondition("meteor_impacts") || planet.getMarket().hasCondition("pollution") || planet.getMarket().hasCondition("very_hot") || planet.getMarket().hasCondition("very_cold"))
        {
            return true;
        }
        else if(!planet.getMarket().hasCondition("habitable") || !planet.getMarket().hasCondition("farmland_bountiful") || !(planet.getMarket().hasCondition("organics_common") || planet.getMarket().hasCondition("organics_abundant") || planet.getMarket().hasCondition("organics_plentiful")))
        {
            return true;
        }

        return false;
    }

    public static SectorEntityToken getFocusOfAsteroidBelt(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_belt"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        return entity.getOrbitFocus();
                    }
                }
            }
        }

        return null;
    }

    public static OrbitAPI getAsteroidFieldOrbit(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_field"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        AsteroidFieldTerrainPlugin asteroidPlugin = (AsteroidFieldTerrainPlugin)terrain.getPlugin();
                        return asteroidPlugin.getEntity().getOrbit();
                    }
                }
            }
        }

        return null;
    }

    public static SectorEntityToken getAsteroidFieldEntity(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_field"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        AsteroidFieldTerrainPlugin asteroidPlugin = (AsteroidFieldTerrainPlugin)terrain.getPlugin();
                        return asteroidPlugin.getEntity();
                    }
                }
            }
        }

        return null;
    }

    public static boolean playerFleetInAsteroidBelt(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_belt"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean playerFleetInAsteroidField(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_field"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static Integer getNumAsteroidTerrainsInSystem(SectorEntityToken playerFleet)
    {
        Integer numRoids = 0;
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_field") || terrainID.equals("asteroid_belt"))
                {
                    numRoids++;
                }
            }
        }

        return numRoids;
    }

    public static Integer getNumAsteroidBeltsInSystem(SectorEntityToken playerFleet)
    {
        Integer numBelts = 0;
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_belt"))
                {
                    numBelts++;
                }
            }
        }

        return numBelts;
    }

    public static String getMiningStationResourceString(Integer numAsteroidTerrains)
    {
        if(numAsteroidTerrains <= 1)
        {
            return "sparse";
        }
        else if(numAsteroidTerrains > 1 && numAsteroidTerrains < 5)
        {
            return "moderate";
        }
        else if(numAsteroidTerrains >= 5)
        {
            return "abundant";
        }
        else
        {
            return "moderate";
        }
    }

    public static int getNumberOfStationExpansions(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if (tag.contains("boggled_station_construction_numExpansions_"))
            {
                return Integer.parseInt(tag.substring(tag.length() - 1));
            }
        }

        return 0;
    }

    public static void incrementNumberOfStationExpansions(MarketAPI market)
    {
        if(getNumberOfStationExpansions(market) == 0)
        {
            market.addTag("boggled_station_construction_numExpansions_1");
        }
        else
        {
            int numExpansionsOld = getNumberOfStationExpansions(market);
            market.removeTag("boggled_station_construction_numExpansions_" + numExpansionsOld);
            market.addTag("boggled_station_construction_numExpansions_" + (numExpansionsOld + 1));
        }
    }

    public static float randomOrbitalAngleFloat()
    {
        Random rand = new Random();
        return rand.nextFloat() * (360f);
    }

    public static void refreshAquacultureAndFarming(MarketAPI market)
    {
        if(market.hasTag("station") || market.getPrimaryEntity().hasTag("station") || market.getPlanetEntity() == null)
        {
            return;
        }
        else
        {
            if(getPlanetType(market.getPlanetEntity()).equals("water") && market.hasIndustry("farming"))
            {
                market.getIndustry("farming").init("aquaculture", market);
            }
            else if(market.hasIndustry("aquaculture") && !getPlanetType(market.getPlanetEntity()).equals("water"))
            {
                market.getIndustry("aquaculture").init("farming", market);
            }
        }
    }

    public static boolean playerTooClose(StarSystemAPI system)
    {
        return Global.getSector().getPlayerFleet().isInOrNearSystem(system);
    }

    public static void clearConnectedPlanets(MarketAPI market)
    {
        Iterator removePlanets = market.getConnectedEntities().iterator();
        SectorEntityToken targetEntityToRemove = null;

        while(removePlanets.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)removePlanets.next();
            if (entity instanceof PlanetAPI && !entity.hasTag("station"))
            {
                targetEntityToRemove = entity;
            }
        }

        removePlanets = null;
        if(targetEntityToRemove != null)
        {
            market.getConnectedEntities().remove(targetEntityToRemove);
            clearConnectedPlanets(market);
        }
    }

    public static void clearConnectedStations(MarketAPI market)
    {
        Iterator removeStations = market.getConnectedEntities().iterator();
        SectorEntityToken targetEntityToRemove = null;

        while(removeStations.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)removeStations.next();
            if (entity.hasTag("station"))
            {
                targetEntityToRemove = entity;
            }
        }

        removeStations = null;
        if(targetEntityToRemove != null)
        {
            market.getConnectedEntities().remove(targetEntityToRemove);
            clearConnectedStations(market);
        }
    }

    public static int numReflectorsInOrbit(MarketAPI market)
    {
        int numReflectors = 0;
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.getId().contains("stellar_shade") || entity.hasTag("stellar_mirror") || entity.hasTag("stellar_shade")))
            {
                numReflectors++;
            }
        }

        return numReflectors;
    }

    public static int numMirrorsInOrbit(MarketAPI market)
    {
        int numMirrors = 0;
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.hasTag("stellar_mirror")))
            {
                numMirrors++;
            }
        }

        return numMirrors;
    }

    public static int numShadesInOrbit(MarketAPI market)
    {
        int numShades = 0;
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_shade") || entity.hasTag("stellar_shade")))
            {
                numShades++;
            }
        }

        return numShades;
    }

    public static void clearReflectorsInOrbit(MarketAPI market)
    {
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.getId().contains("stellar_shade") || entity.hasTag("stellar_mirror") || entity.hasTag("stellar_shade")))
            {
                allEntitiesInSystem.remove();
                market.getStarSystem().removeEntity(entity);
            }
        }
    }

    public static boolean hasIsmaraSling(MarketAPI market)
    {
        Iterator marketsInSystem = Global.getSector().getEconomy().getMarkets(market.getStarSystem()).iterator();
        while(marketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)marketsInSystem.next();
            if(marketElement.getFactionId().equals(market.getFactionId()) && marketElement.hasIndustry("ISMARA_SLING") && marketElement.getIndustry("ISMARA_SLING").isFunctional())
            {
                return true;
            }
        }

        return false;
    }

    public static void terraformVariantToTerran(MarketAPI market)
    {
        String oldPlanetType = getPlanetType(market.getPlanetEntity());

        String newPlanetType = "terran";
        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext()) {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType)) {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());
                myspec.setGlowTexture(spec.getGlowTexture());
                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();

        if(market.hasCondition("water_surface"))
        {
            market.removeCondition("water_surface");
        }

        if(oldPlanetType.equals("toxic") && market.hasCondition("toxic_atmosphere"))
        {
            market.removeCondition("toxic_atmosphere");
        }

        if(market.hasCondition("US_storm"))
        {
            market.removeCondition("US_storm");
        }

        if(!market.hasCondition("habitable"))
        {
            market.addCondition("habitable");
        }

        if(market.hasCondition("very_cold"))
        {
            market.removeCondition("very_cold");
            market.addCondition("cold");
        }

        if(market.hasCondition("very_hot"))
        {
            market.removeCondition("very_hot");
            market.addCondition("hot");
        }

        if(market.hasCondition("no_atmosphere"))
        {
            market.removeCondition("no_atmosphere");
        }

        if(market.hasCondition("thin_atmosphere"))
        {
            market.removeCondition("thin_atmosphere");
        }

        if(market.hasCondition("dense_atmosphere"))
        {
            market.removeCondition("dense_atmosphere");
        }

        if(market.hasCondition("extreme_weather"))
        {
            market.removeCondition("extreme_weather");
        }

        if(market.hasCondition("inimical_biosphere"))
        {
            market.removeCondition("inimical_biosphere");
        }

        if(market.hasCondition("meteor_impacts"))
        {
            market.removeCondition("meteor_impacts");
        }

        if(market.hasCondition("organics_trace"))
        {
            market.removeCondition("organics_trace");
        }

        if(!market.hasCondition("organics_common") && !market.hasCondition("organics_abundant") && !market.hasCondition("organics_plentiful"))
        {
            market.addCondition("organics_common");
        }

        if(market.hasCondition("farmland_poor"))
        {
            market.removeCondition("farmland_poor");
        }

        if(!market.hasCondition("farmland_adequate") && !market.hasCondition("farmland_rich") && !market.hasCondition("farmland_bountiful"))
        {
            market.addCondition("farmland_adequate");
        }

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        market.addTag("boggled_terraformed");
        market.addTag("boggled_original_planet_type_" + oldPlanetType);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void terraformFrozenToWater(MarketAPI market)
    {
        String newPlanetType = "water";
        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext()) {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType)) {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());
                myspec.setGlowTexture(spec.getGlowTexture());
                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();

        if(!market.hasCondition("water_surface"))
        {
            market.addCondition("water_surface");
        }

        if(market.hasCondition("US_storm"))
        {
            market.removeCondition("US_storm");
        }

        if(!market.hasCondition("habitable"))
        {
            market.addCondition("habitable");
        }

        if(market.hasCondition("very_cold"))
        {
            market.removeCondition("very_cold");
            market.addCondition("cold");
        }

        if(market.hasCondition("very_hot"))
        {
            market.removeCondition("very_hot");
            market.addCondition("hot");
        }

        if(market.hasCondition("no_atmosphere"))
        {
            market.removeCondition("no_atmosphere");
        }

        if(market.hasCondition("thin_atmosphere"))
        {
            market.removeCondition("thin_atmosphere");
        }

        if(market.hasCondition("dense_atmosphere"))
        {
            market.removeCondition("dense_atmosphere");
        }

        if(market.hasCondition("extreme_weather"))
        {
            market.removeCondition("extreme_weather");
        }

        if(market.hasCondition("inimical_biosphere"))
        {
            market.removeCondition("inimical_biosphere");
        }

        if(market.hasCondition("meteor_impacts"))
        {
            market.removeCondition("meteor_impacts");
        }

        if(market.hasCondition("organics_trace"))
        {
            market.removeCondition("organics_trace");
        }

        if(!market.hasCondition("organics_common") && !market.hasCondition("organics_abundant") && !market.hasCondition("organics_plentiful"))
        {
            market.addCondition("organics_common");
        }

        if(market.hasCondition("farmland_poor"))
        {
            market.removeCondition("farmland_poor");
        }

        if(market.hasCondition("farmland_adequate"))
        {
            market.removeCondition("farmland_adequate");
        }

        if(market.hasCondition("farmland_rich"))
        {
            market.removeCondition("farmland_rich");
        }

        if(market.hasCondition("farmland_bountiful"))
        {
            market.removeCondition("farmland_bountiful");
        }

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        market.addTag("boggled_terraformed");
        market.addTag("boggled_original_planet_type_frozen");

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void deterraform(MarketAPI market)
    {
        String deterraformTypeID = "null";
        Iterator tags = market.getTags().iterator();

        while(tags.hasNext())
        {
            String tag = (String)tags.next();
            if (tag.length() > 30 && tag.substring(0,29).equals("boggled_original_planet_type_"))
            {
                deterraformTypeID = tag.substring(29);
            }
        }

        String newPlanetType = deterraformTypeID;
        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext()) {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType)) {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());
                myspec.setGlowTexture(spec.getGlowTexture());
                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();

        if(market.hasCondition("water_surface"))
        {
            market.removeCondition("water_surface");
        }

        if(!market.hasCondition("extreme_weather") && !market.hasCondition("mild_climate"))
        {
            market.addCondition("extreme_weather");
        }

        if(market.hasCondition("habitable"))
        {
            market.removeCondition("habitable");
        }

        if(market.hasCondition("farmland_poor"))
        {
            market.removeCondition("farmland_poor");
        }

        if(market.hasCondition("farmland_adequate"))
        {
            market.removeCondition("farmland_adequate");
        }

        if(market.hasCondition("farmland_rich"))
        {
            market.removeCondition("farmland_rich");
        }

        if(market.hasCondition("farmland_bountiful"))
        {
            market.removeCondition("farmland_bountiful");
        }

        surveyAll(market);
        refreshAquacultureAndFarming(market);
        refreshSupplyAndDemand(market);

        market.removeTag("boggled_terraformed");
        market.removeTag("boggled_original_planet_type_" + deterraformTypeID);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Regressed to a " + deterraformTypeID + " planet");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }

        market.removeCondition("deterraforming_tracker");
    }

    public static void swapStationSprite(SectorEntityToken station, String stationType, String stationGreekLetter, int targetSize)
    {
        MarketAPI market = station.getMarket();
        StarSystemAPI system = market.getStarSystem();
        OrbitAPI orbit = null;
        if(station.getOrbit() != null)
        {
            orbit = station.getOrbit();
        }
        CampaignClockAPI clock = Global.getSector().getClock();
        SectorEntityToken newStation = null;
        SectorEntityToken newStationLights = null;

        String size = "null";
        if(targetSize == 1)
        {
            size = "small";
        }
        else if(targetSize == 2)
        {
            size = "medium";
        }
        else if(targetSize == 3)
        {
            size = "large";
        }

        if(size.equals("null"))
        {
            //Do nothing if an erroneous size value was passed.
            return;
        }

        if(stationType.equals("astropolis"))
        {
            newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + stationGreekLetter + "_" + size, market.getFactionId());
            newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + stationGreekLetter + "_" + size + "_lights_overlay", market.getFactionId());
        }
        else if(stationType.equals("mining"))
        {
            newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
            //We can't tell which lights overlay to delete earlier because there could be multiple mining stations in a single system.
            //Therefore we delete them all earlier, then recreate them all later.
        }
        else if(stationType.equals("siphon"))
        {
            newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
            newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + size + "_lights_overlay", market.getFactionId());
        }
        else if(stationType.equals("gatekeeper"))
        {
            newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
            newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + size + "_lights_overlay", market.getFactionId());
        }
        else
        {
            //Do nothing because the station type is unrecognized
            return;
        }

        if(newStation == null)
        {
            //Failed to create a new station likely because of erroneous passed values. Do nothing.
            return;
        }

        newStation.setContainingLocation(station.getContainingLocation());
        if(newStationLights != null)
        {
            newStationLights.setContainingLocation(station.getContainingLocation());
        }

        if(orbit != null)
        {
            newStation.setOrbit(orbit);
            if(newStationLights != null)
            {
                newStationLights.setOrbit(newStation.getOrbit().makeCopy());
            }
        }
        newStation.setMemory(station.getMemory());
        newStation.setFaction(market.getFactionId());
        station.setCircularOrbit(newStation, 0, 0, 1);

        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station))
            {
                if (entity.getOrbit().getClass().equals(CircularFleetOrbit.class))
                {
                    ((CircularFleetOrbit)entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbit.class))
                {
                    ((CircularOrbit)entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbitPointDown.class))
                {
                    ((CircularOrbitPointDown)entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbitWithSpin.class))
                {
                    ((CircularOrbitWithSpin)entity.getOrbit()).setFocus(newStation);
                }
            }
        }

        //Deletes the old station. May cause limited issues related to ships orbiting the old location
        clearConnectedStations(market);
        system.removeEntity(station);

        newStation.setMarket(market);
        market.setPrimaryEntity(newStation);

        surveyAll(market);
        refreshSupplyAndDemand(market);
    }

    public static void deleteOldLightsOverlay(SectorEntityToken station, String stationType, String stationGreekLetter)
    {
        StarSystemAPI system = station.getStarSystem();
        OrbitAPI orbit = null;
        if(station.getOrbit() != null)
        {
            orbit = station.getOrbit();
        }

        if(stationType.equals("astropolis"))
        {
            SectorEntityToken targetTokenToDelete = null;

            if(stationGreekLetter.equals("alpha"))
            {
                Iterator allEntitiesInSystem = system.getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                    if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_astropolis_alpha_small") || entity.hasTag("boggled_lights_overlay_astropolis_alpha_medium") || entity.hasTag("boggled_lights_overlay_astropolis_alpha_large")))
                    {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                allEntitiesInSystem = null;

                if(targetTokenToDelete != null)
                {
                    system.removeEntity(targetTokenToDelete);
                    deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                }
            }
            else if(stationGreekLetter.equals("beta"))
            {
                Iterator allEntitiesInSystem = system.getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                    if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_astropolis_beta_small") || entity.hasTag("boggled_lights_overlay_astropolis_beta_medium") || entity.hasTag("boggled_lights_overlay_astropolis_beta_large")))
                    {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                allEntitiesInSystem = null;

                if(targetTokenToDelete != null)
                {
                    system.removeEntity(targetTokenToDelete);
                    deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                }
            }
            else if(stationGreekLetter.equals("gamma"))
            {
                Iterator allEntitiesInSystem = system.getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                    if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_astropolis_gamma_small") || entity.hasTag("boggled_lights_overlay_astropolis_gamma_medium") || entity.hasTag("boggled_lights_overlay_astropolis_gamma_large")))
                    {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                allEntitiesInSystem = null;

                if(targetTokenToDelete != null)
                {
                    system.removeEntity(targetTokenToDelete);
                    deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                }
            }
        }
        else if(stationType.equals("mining"))
        {
            SectorEntityToken targetTokenToDelete = null;

            Iterator allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if(entity.hasTag("boggled_lights_overlay_mining_small") || entity.hasTag("boggled_lights_overlay_mining_medium"))
                {
                    targetTokenToDelete = entity;
                    break;
                }
            }
            allEntitiesInSystem = null;

            if(targetTokenToDelete != null)
            {
                system.removeEntity(targetTokenToDelete);
                deleteOldLightsOverlay(station, stationType, stationGreekLetter);
            }
        }
        else if(stationType.equals("siphon"))
        {
            SectorEntityToken targetTokenToDelete = null;

            Iterator allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_siphon_small") || entity.hasTag("boggled_lights_overlay_siphon_medium")))
                {
                    targetTokenToDelete = entity;
                    break;
                }
            }
            allEntitiesInSystem = null;

            if(targetTokenToDelete != null)
            {
                system.removeEntity(targetTokenToDelete);
                deleteOldLightsOverlay(station, stationType, stationGreekLetter);
            }
        }
        else if(stationType.equals("gatekeeper"))
        {
            SectorEntityToken targetTokenToDelete = null;

            Iterator allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if(entity.getOrbitFocus() != null && entity.getOrbitFocus().hasTag("boggled_astral_gate") && (entity.hasTag("boggled_lights_overlay_gatekeeper_small") || entity.hasTag("boggled_lights_overlay_gatekeeper_medium")))
                {
                    targetTokenToDelete = entity;
                    break;
                }
            }
            allEntitiesInSystem = null;

            if(targetTokenToDelete != null)
            {
                system.removeEntity(targetTokenToDelete);
                deleteOldLightsOverlay(station, stationType, stationGreekLetter);
            }
        }
        else
        {
            //Do nothing because the station type is unrecognized
            return;
        }
    }

    public static void reapplyMiningStationLights(StarSystemAPI system)
    {
        SectorEntityToken stationToApplyOverlayTo = null;
        int stationsize = 0;

        Iterator allEntitiesInSystem = system.getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if(entity.hasTag("boggled_mining_station_small") && !entity.hasTag("boggled_already_reapplied_lights_overlay"))
            {
                stationToApplyOverlayTo = entity;
                stationsize = 1;
                entity.addTag("boggled_already_reapplied_lights_overlay");
                break;
            }
            else if(entity.hasTag("boggled_mining_station_medium") && !entity.hasTag("boggled_already_reapplied_lights_overlay"))
            {
                stationToApplyOverlayTo = entity;
                stationsize = 2;
                entity.addTag("boggled_already_reapplied_lights_overlay");
                break;
            }
        }
        allEntitiesInSystem = null;

        if(stationToApplyOverlayTo != null)
        {
            if(stationsize == 1)
            {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals("neutral"))
                {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_small_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            }
            else if(stationsize == 2)
            {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals("neutral"))
                {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_medium_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            }
        }
        else
        {
            allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if(entity.hasTag("boggled_already_reapplied_lights_overlay"))
                {
                    entity.removeTag("boggled_already_reapplied_lights_overlay");
                }
            }
        }
    }

    public static boolean marketHasOrbitalStation(MarketAPI market)
    {
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && entity.hasTag("station"))
            {
                return true;
            }
        }

        return false;
    }

    public static void incrementOreForPlanetCracking(MarketAPI market)
    {
        if(market.hasCondition("ore_sparse"))
        {
            boggledTools.removeCondition(market, "ore_sparse");
            boggledTools.addCondition(market, "ore_moderate");
        }
        else if(market.hasCondition("ore_moderate"))
        {
            boggledTools.removeCondition(market, "ore_moderate");
            boggledTools.addCondition(market, "ore_abundant");
        }
        else if(market.hasCondition("ore_abundant"))
        {
            boggledTools.removeCondition(market, "ore_abundant");
            boggledTools.addCondition(market, "ore_rich");
        }
        else if(market.hasCondition("ore_rich"))
        {
            boggledTools.removeCondition(market, "ore_rich");
            boggledTools.addCondition(market, "ore_ultrarich");
        }
        else if(market.hasCondition("ore_ultrarich"))
        {
            //Do Nothing
        }
        else
        {
            boggledTools.addCondition(market, "ore_sparse");
        }

        if(market.hasCondition("rare_ore_sparse"))
        {
            boggledTools.removeCondition(market, "rare_ore_sparse");
            boggledTools.addCondition(market, "rare_ore_moderate");
        }
        else if(market.hasCondition("rare_ore_moderate"))
        {
            boggledTools.removeCondition(market, "rare_ore_moderate");
            boggledTools.addCondition(market, "rare_ore_abundant");
        }
        else if(market.hasCondition("rare_ore_abundant"))
        {
            boggledTools.removeCondition(market, "rare_ore_abundant");
            boggledTools.addCondition(market, "rare_ore_rich");
        }
        else if(market.hasCondition("rare_ore_rich"))
        {
            boggledTools.removeCondition(market, "rare_ore_rich");
            boggledTools.addCondition(market, "rare_ore_ultrarich");
        }
        else if(market.hasCondition("rare_ore_ultrarich"))
        {
            //Do Nothing
        }
        else
        {
            boggledTools.addCondition(market, "rare_ore_sparse");
        }
    }

    public static void incrementVolatilesForOuyangOptimization(MarketAPI market)
    {
        if(market.hasCondition("volatiles_trace"))
        {
            boggledTools.removeCondition(market, "volatiles_trace");
            boggledTools.addCondition(market, "volatiles_abundant");
        }
        else if(market.hasCondition("volatiles_diffuse"))
        {
            boggledTools.removeCondition(market, "volatiles_diffuse");
            boggledTools.addCondition(market, "volatiles_plentiful");
        }
        else if(market.hasCondition("volatiles_abundant"))
        {
            boggledTools.removeCondition(market, "volatiles_abundant");
            boggledTools.addCondition(market, "volatiles_plentiful");
        }
        else if(market.hasCondition("volatiles_plentiful"))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, "volatiles_diffuse");
        }

        SectorEntityToken closestGasGiantToken = market.getPrimaryEntity();
        if(closestGasGiantToken != null)
        {
            Iterator allEntitiesInSystem = closestGasGiantToken.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if(entity.hasTag("station") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(closestGasGiantToken) && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station") || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station")) && !entity.getId().equals("beholder_station"))
                {
                    if(entity.getMarket() != null)
                    {
                        market = entity.getMarket();
                        if(market.hasCondition("volatiles_trace"))
                        {
                            boggledTools.removeCondition(market, "volatiles_trace");
                            boggledTools.addCondition(market, "volatiles_abundant");
                        }
                        else if(market.hasCondition("volatiles_diffuse"))
                        {
                            boggledTools.removeCondition(market, "volatiles_diffuse");
                            boggledTools.addCondition(market, "volatiles_plentiful");
                        }
                        else if(market.hasCondition("volatiles_abundant"))
                        {
                            boggledTools.removeCondition(market, "volatiles_abundant");
                            boggledTools.addCondition(market, "volatiles_plentiful");
                        }

                        boggledTools.surveyAll(market);
                        boggledTools.refreshSupplyAndDemand(market);
                        boggledTools.refreshAquacultureAndFarming(market);
                    }
                }
            }
        }
    }

    public static SectorEntityToken getPlanetTokenForQuest(String systemID, String entityID)
    {
        StarSystemAPI system = Global.getSector().getStarSystem(systemID);
        if(system != null)
        {
            SectorEntityToken possibleTarget = system.getEntityById(entityID);
            if(possibleTarget != null)
            {
                if(possibleTarget instanceof PlanetAPI)
                {
                    return possibleTarget;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public static boolean playerCloseToAeolus()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        float distanceFromObject = Math.abs(boggledTools.getDistanceBetweenTokens(playerFleet, boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4")));
        if(distanceFromObject < 1000 && distanceFromObject > 0 && playerFleet.getStarSystem().equals(getPlanetTokenForQuest("Penelope's Star", "penelope4").getStarSystem()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static SectorEntityToken getClosestValidPlanetSectorEntityTokenInSystem(StarSystemAPI system)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        SectorEntityToken targetToken = null;

        Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(system).iterator();
        while(allMarketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)allMarketsInSystem.next();
            if (marketElement.isPlayerOwned() && marketElement.getPrimaryEntity() != null && marketElement.getPrimaryEntity() instanceof PlanetAPI)
            {
                if(targetToken == null)
                {
                    targetToken = marketElement.getPrimaryEntity();
                }
                else if(boggledTools.getDistanceBetweenTokens(playerFleet, targetToken) > boggledTools.getDistanceBetweenTokens(playerFleet, marketElement.getPrimaryEntity()))
                {
                    targetToken = marketElement.getPrimaryEntity();
                }
            }
        }

        return targetToken;
    }

    public static int getTotalTerraformingPoints(MarketAPI market)
    {
        int points = 0;
        Iterator allStructuresOnMarket = market.getIndustries().iterator();
        while(allStructuresOnMarket.hasNext())
        {
            Industry industry = (Industry) allStructuresOnMarket.next();
            if (industry instanceof Stellar_Reflector_Array)
            {
                points = points + ((Stellar_Reflector_Array) industry).getTerraformingProgressPoints();
            }
            else if (industry instanceof Terraforming_Platform)
            {
                points = points + ((Terraforming_Platform) industry).getTerraformingProgressPoints();
            }
            if (industry instanceof Atmosphere_Processor)
            {
                points = points + ((Atmosphere_Processor) industry).getTerraformingProgressPoints();
            }
            if (industry instanceof Eisen_Division)
            {
                points = points + ((Eisen_Division) industry).getTerraformingProgressPoints();
            }
        }

        Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(market.getStarSystem()).iterator();
        while(allMarketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)allMarketsInSystem.next();
            if(marketElement.isPlayerOwned() && !marketElement.equals(market) && marketElement.hasIndustry("ISMARA_SLING") && marketElement.getIndustry("ISMARA_SLING").isFunctional())
            {
                points = points + ((Ismara_Sling)marketElement.getIndustry("ISMARA_SLING")).getTerraformingProgressPoints();
            }
        }

        if(market.hasCondition("US_crash"))
        {
            points = points + 4;
        }

        return points;
    }

    public static void clearOldTerraformingProjectTags(MarketAPI market)
    {
        String oldTag = null;
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_project_"))
            {
                oldTag = tag;
                break;
            }
        }
        allTagsOnMarket = null;

        if(oldTag != null)
        {
            market.getPrimaryEntity().removeTag(oldTag);
            boggledTools.clearOldTerraformingProjectTags(market);
        }
    }

    public static void clearOldTerraformingProjectProgressTags(MarketAPI market)
    {
        String oldTag = null;
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_points_"))
            {
                oldTag = tag;
                break;
            }
        }
        allTagsOnMarket = null;

        if(oldTag != null)
        {
            market.getPrimaryEntity().removeTag(oldTag);
            boggledTools.clearOldTerraformingProjectProgressTags(market);
        }
    }

    public static void clearBothTerraformingTags(MarketAPI market)
    {
        clearOldTerraformingProjectTags(market);
        clearOldTerraformingProjectProgressTags(market);
    }

    public static void initiateTerraformingProject(MarketAPI market, String projectType)
    {
        boggledTools.clearBothTerraformingTags(market);

        market.getPrimaryEntity().addTag("boggled_terraforming_project_" + projectType);
        market.getPrimaryEntity().addTag("boggled_terraforming_progress_points_000000");
    }

    public static boolean terraformingProjectAlreadyOngoing(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_project_"))
            {
                return true;
            }
        }

        return false;
    }

    public static String getTerraformingProjectTypeOngoing(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_project_"))
            {
                return tag.replaceAll("boggled_terraforming_project_", "");
            }
        }

        return null;
    }

    public static int getTerraformingProjectProgressPoints(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_points_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_terraforming_progress_points_", ""));
            }
        }

        return 0;
    }

    public static void incrementTerraformingProjectProgressPoints(MarketAPI market, int amount)
    {
        int currentPoints = getTerraformingProjectProgressPoints(market);

        clearOldTerraformingProjectProgressTags(market);

        currentPoints = currentPoints + amount;

        String strPoints = currentPoints + "";

        while(strPoints.length() < 6)
        {
            strPoints = "0" + strPoints;
        }

        market.getPrimaryEntity().addTag("boggled_terraforming_progress_points_" + strPoints);
    }

    public static int getTerraformingProjectPointRequirement(MarketAPI market, String project)
    {
        if(project.contains("transformation"))
        {
            int points = Global.getSettings().getInt("boggledBaseTerraformingPoints");

            // Global points offsets
            if(market.hasCondition("dense_atmosphere"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForDenseAtmo");
            }
            else if(market.hasCondition("thin_atmosphere"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForThinAtmo");
            }
            else if(market.hasCondition("no_atmosphere"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForNoAtmo");
            }

            if(market.hasCondition("toxic_atmosphere"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForToxicAtmo");
            }

            if(market.hasCondition("irradiated"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForRadiation");
            }

            if(market.hasCondition("habitable"))
            {
                points = points - Global.getSettings().getInt("boggledPointReductionForHabitable");
            }

            if(project.equals("arcology_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(project.replaceAll("_transformation", "")))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForArcology")) / 100;
            }
            else if(project.equals("terran_transformation") || project.equals("terran_eccentric_transformation") || project.equals("auric_transformation") || project.equals("archipelago_transformation") || project.equals("continental_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("terran"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForTerran")) / 100;
            }
            else if(project.equals("water_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(project.replaceAll("_transformation", "")))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForWater")) / 100;
            }
            else if(project.equals("jungle_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("hot"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(project.replaceAll("_transformation", "")))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForJungle")) / 100;
            }
            else if(project.equals("arid_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("hot"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("desert"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForArid")) / 100;
            }
            else if(project.equals("tundra_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("cold"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("tundra"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForTundra")) / 100;
            }
            else if(project.equals("frozen_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points + (4 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("frozen"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForFrozen")) / 100;
            }
            else if(project.equals("volcanic_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("hot"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("cold"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points + (4 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("volcanic"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForVolcanic")) / 100;
            }
            else if(project.equals("toxic_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(project.replaceAll("_transformation", "")))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForToxic")) / 100;
            }
        }
        else
        {
            return Global.getSettings().getInt("boggledBaseConditionModificationPoints");
        }

        return 0;
    }

    public static int getLastDayChecked(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_lastDayChecked_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_terraforming_progress_lastDayChecked_", ""));
            }
        }

        return 0;
    }

    public static int getLastMonthChecked(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_lastMonthChecked_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_terraforming_progress_lastMonthChecked_", ""));
            }
        }

        return 0;
    }

    public static int getLastCycleChecked(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_lastCycleChecked_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_terraforming_progress_lastCycleChecked_", ""));
            }
        }

        return 0;
    }

    public static void clearClockCheckTags(MarketAPI market)
    {
        String tagToDelete = null;
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_last"))
            {
                tagToDelete = tag;
                break;
            }
        }
        allTagsOnMarket = null;

        if(tagToDelete != null)
        {
            market.removeTag(tagToDelete);
            clearClockCheckTags(market);
        }
    }

    public static void addCondition(MarketAPI market, String condition)
    {
        if(!market.hasCondition(condition))
        {
            market.addCondition(condition);
        }
    }

    public static void removeCondition(MarketAPI market, String condition)
    {
        if(market.hasCondition(condition))
        {
            market.removeCondition(condition);
        }
    }

    public static void terraformVariantToVariant(MarketAPI market, String newPlanetType)
    {
        //String oldPlanetType = getPlanetType(market.getPlanetEntity());

        if(newPlanetType.equals("arcology"))
        {
            newPlanetType = "boggled_arcology";
        }
        else if(newPlanetType.equals("terran_eccentric"))
        {
            newPlanetType = "terran-eccentric";
        }
        else if(newPlanetType.equals("volcanic"))
        {
            newPlanetType = "lava";
        }
        else if(newPlanetType.equals("auric"))
        {
            newPlanetType = "US_auric";
        }
        else if(newPlanetType.equals("archipelago"))
        {
            newPlanetType = "US_water";
        }
        else if(newPlanetType.equals("continental"))
        {
            newPlanetType = "US_continent";
        }

        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext())
        {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType))
            {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());

                if(newPlanetType.equals("boggled_arcology"))
                {
                    myspec.setGlowTexture("graphics/planets/arcology_glow.png");
                }
                else
                {
                    myspec.setGlowTexture(spec.getGlowTexture());
                }

                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();

        if(!newPlanetType.equals("boggled_arcology") && market.hasCondition("boggled_arcology_world"))
        {
            removeCondition(market, "boggled_arcology_world");
        }

        if(newPlanetType.equals("boggled_arcology"))
        {
            // Modded conditions
            addCondition(market, "boggled_arcology_world");
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "habitable");
            removeCondition(market, "mild_climate");
            removeCondition(market, "pollution");

            removeCondition(market, "cold");
            removeCondition(market, "very_cold");
            removeCondition(market, "hot");
            removeCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("terran") || newPlanetType.equals("terran-eccentric") || newPlanetType.equals("US_auric") || newPlanetType.equals("US_water") || newPlanetType.equals("US_continent"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            removeCondition(market, "volturnian_lobster_pens");

            addCondition(market, "habitable");

            removeCondition(market, "cold");
            removeCondition(market, "very_cold");
            removeCondition(market, "hot");
            removeCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            addCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            addCondition(market, "organics_plentiful");

            addCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("water"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            addCondition(market, "habitable");

            removeCondition(market, "cold");
            removeCondition(market, "very_cold");
            removeCondition(market, "hot");
            removeCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            addCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            addCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("jungle"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            removeCondition(market, "volturnian_lobster_pens");

            addCondition(market, "habitable");

            removeCondition(market, "cold");
            removeCondition(market, "very_cold");
            addCondition(market, "hot");
            removeCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");
            removeCondition(market, "mild_climate");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            addCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            addCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("arid"))
        {
            // Modded conditions

            // Vanilla Conditions
            removeCondition(market, "volturnian_lobster_pens");

            addCondition(market, "habitable");

            removeCondition(market, "cold");
            removeCondition(market, "very_cold");
            addCondition(market, "hot");
            removeCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");
            removeCondition(market, "mild_climate");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "water_surface");

            addCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("tundra"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            removeCondition(market, "volturnian_lobster_pens");

            addCondition(market, "habitable");

            addCondition(market, "cold");
            removeCondition(market, "very_cold");
            removeCondition(market, "hot");
            removeCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");
            removeCondition(market, "mild_climate");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            addCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            addCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            addCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("frozen"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "habitable");

            removeCondition(market, "cold");
            addCondition(market, "very_cold");
            removeCondition(market, "hot");
            removeCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");
            removeCondition(market, "mild_climate");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            addCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("lava"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "habitable");

            removeCondition(market, "cold");
            removeCondition(market, "very_cold");
            removeCondition(market, "hot");
            addCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");
            removeCondition(market, "mild_climate");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("toxic"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "habitable");

            removeCondition(market, "cold");
            removeCondition(market, "very_cold");
            removeCondition(market, "hot");
            removeCondition(market, "very_hot");
            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            addCondition(market, "toxic_atmosphere");
            removeCondition(market, "mild_climate");
            removeCondition(market, "extreme_weather");
            removeCondition(market, "irradiated");
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            addCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            addCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static MarketAPI createMiningStationMarket(SectorEntityToken stationEntity)
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        StarSystemAPI system = stationEntity.getStarSystem();
        String systemName = system.getName();

        //Create the mining station market
        MarketAPI market = Global.getFactory().createMarket(systemName + clock.getCycle() + clock.getMonth() + clock.getDay() + "MiningStationMarket", stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        if(Global.getSettings().getBoolean("boggledMiningStationLinkToResourceBelts"))
        {
            int numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(stationEntity);
            String resourceLevel = boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem);
            market.addCondition("ore_" + resourceLevel);
            market.addCondition("rare_ore_" + resourceLevel);
        }
        else
        {
            String resourceLevel = "moderate";
            int staticAmountPerSettings = Global.getSettings().getInt("boggledMiningStationStaticAmount");
            switch(staticAmountPerSettings)
            {
                case 1:
                    resourceLevel = "sparse";
                    break;
                case 2:
                    resourceLevel = "moderate";
                    break;
                case 3:
                    resourceLevel = "abundant";
                    break;
                case 4:
                    resourceLevel = "rich";
                    break;
                case 5:
                    resourceLevel = "ultrarich";
                    break;
            }
            market.addCondition("ore_" + resourceLevel);
            market.addCondition("rare_ore_" + resourceLevel);
        }

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
        market.getConstructionQueue().addToEnd(Industries.MINING, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);
        //Global.getSector().getCampaignUI().getCurrentInteractionDialog().dismiss();

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);

        return market;
    }

    public static MarketAPI createSiphonStationMarket(SectorEntityToken stationEntity, SectorEntityToken hostGasGiant)
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        StarSystemAPI system = stationEntity.getStarSystem();
        String systemName = system.getName();

        //Create the siphon station market
        MarketAPI market = Global.getFactory().createMarket(systemName + ":" + hostGasGiant.getName() + "SiphonStationMarket", stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        if(Global.getSettings().getBoolean("boggledSiphonStationLinkToGasGiant"))
        {
            if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_TRACE))
            {
                market.addCondition(Conditions.VOLATILES_TRACE);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_DIFFUSE))
            {
                market.addCondition(Conditions.VOLATILES_DIFFUSE);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_ABUNDANT))
            {
                market.addCondition(Conditions.VOLATILES_ABUNDANT);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_PLENTIFUL))
            {
                market.addCondition(Conditions.VOLATILES_PLENTIFUL);
            }
            else //Can a gas giant not have any volatiles at all?
            {
                market.addCondition(Conditions.VOLATILES_TRACE);
            }
        }
        else
        {
            String resourceLevel = "diffuse";
            int staticAmountPerSettings = Global.getSettings().getInt("boggledSiphonStationStaticAmount");
            switch(staticAmountPerSettings)
            {
                case 1:
                    resourceLevel = "trace";
                    break;
                case 2:
                    resourceLevel = "diffuse";
                    break;
                case 3:
                    resourceLevel = "abundant";
                    break;
                case 4:
                    resourceLevel = "plentiful";
                    break;
            }
            market.addCondition("volatiles_" + resourceLevel);
        }

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
        market.getConstructionQueue().addToEnd(Industries.MINING, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);
        //Global.getSector().getCampaignUI().getCurrentInteractionDialog().dismiss();

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
        return market;
    }

    public static MarketAPI createAstropolisStationMarket(SectorEntityToken stationEntity, SectorEntityToken hostPlanet)
    {
        CampaignClockAPI clock = Global.getSector().getClock();

        //Create the astropolis market
        MarketAPI market = Global.getFactory().createMarket(hostPlanet.getName() + "astropolisMarket" + clock.getCycle() + clock.getMonth() + clock.getDay(), stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
        return market;
    }

    public static int getLastDayCheckedForConstruction(SectorEntityToken stationEntity)
    {
        Iterator allTagsOnStation = stationEntity.getTags().iterator();
        while(allTagsOnStation.hasNext())
        {
            String tag = (String)allTagsOnStation.next();
            if(tag.contains("boggled_construction_progress_lastDayChecked_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_construction_progress_lastDayChecked_", ""));
            }
        }

        return 0;
    }

    public static void clearClockCheckTagsForConstruction(SectorEntityToken stationEntity)
    {
        String tagToDelete = null;
        Iterator allTagsOnStation = stationEntity.getTags().iterator();
        while(allTagsOnStation.hasNext())
        {
            String tag = (String)allTagsOnStation.next();
            if(tag.contains("boggled_construction_progress_lastDayChecked_"))
            {
                tagToDelete = tag;
                break;
            }
        }
        allTagsOnStation = null;

        if(tagToDelete != null)
        {
            stationEntity.removeTag(tagToDelete);
            clearClockCheckTagsForConstruction(stationEntity);
        }
    }

    public static int getConstructionProgressDays(SectorEntityToken stationEntity)
    {
        Iterator allTagsOnStation = stationEntity.getTags().iterator();
        while(allTagsOnStation.hasNext())
        {
            String tag = (String)allTagsOnStation.next();
            if(tag.contains("boggled_construction_progress_days_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_construction_progress_days_", ""));
            }
        }

        return 0;
    }

    public static void clearProgressCheckTagsForConstruction(SectorEntityToken stationEntity)
    {
        String tagToDelete = null;
        Iterator allTagsOnStation = stationEntity.getTags().iterator();
        while(allTagsOnStation.hasNext())
        {
            String tag = (String)allTagsOnStation.next();
            if(tag.contains("boggled_construction_progress_days_"))
            {
                tagToDelete = tag;
                break;
            }
        }
        allTagsOnStation = null;

        if(tagToDelete != null)
        {
            stationEntity.removeTag(tagToDelete);
            clearProgressCheckTagsForConstruction(stationEntity);
        }
    }

    public static void incrementConstructionProgressDays(SectorEntityToken stationEntity, int amount)
    {
        int currentDays = getConstructionProgressDays(stationEntity);

        clearProgressCheckTagsForConstruction(stationEntity);

        currentDays = currentDays + amount;

        String strDays = currentDays + "";

        while(strDays.length() < 6)
        {
            strDays = "0" + strDays;
        }

        stationEntity.addTag("boggled_construction_progress_days_" + strDays);
    }

    /*
    public static void sendDebugIntelMessage(String message) throws IOException, JSONException {
        MessageIntel intel = new MessageIntel(message, Misc.getBasePlayerColor());
        //intel.addLine("");
        String test = Global.getSettings().loadText("data/config/test.txt");
        intel.addLine(test);
        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, null);

        Iterator allEntitiesInSystem = Global.getSector().getPlayerFleet().getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if(entity.hasTag("boggled_astral_gate"))
            {
                entity.setName("WRITE NEW NAME HERE");
            }
        }
    }

     */
}