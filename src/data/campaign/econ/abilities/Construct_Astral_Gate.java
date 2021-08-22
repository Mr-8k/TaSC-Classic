package data.campaign.econ.abilities;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import com.fs.starfarer.combat.entities.terrain.Planet;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;
import data.scripts.GatekeeperEveryFrameScript;
import data.scripts.GatekeeperEveryFrameScript;

public class Construct_Astral_Gate extends BaseDurationAbility
{
    private float creditCost = Global.getSettings().getInt("boggledGateBuildCreditCost");
    private float crewCost  = Global.getSettings().getInt("boggledGateBuildCrewCost");
    private float heavyMachineryCost = Global.getSettings().getInt("boggledGateBuildHeavyMachineryCost");
    private float metalCost = Global.getSettings().getInt("boggledGateBuildMetalCost");
    private float transplutonicsCost = Global.getSettings().getInt("boggledGateBuildTransplutonicsCost");
    private float domainEraArtifactsCost = Global.getSettings().getInt("boggledGateBuildDomainEraArtifactsCost");

    public Construct_Astral_Gate() { }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        CargoAPI playerCargo = playerFleet.getCargo();
        StarSystemAPI system = playerFleet.getStarSystem();

        playerCargo.getCredits().subtract(creditCost);
        playerCargo.removeCommodity("heavy_machinery", heavyMachineryCost);
        playerCargo.removeCommodity("metals", metalCost);
        playerCargo.removeCommodity("rare_metals", transplutonicsCost);

        boggledTools.addAstralGate(system);
        SectorEntityToken astralGateToken = boggledTools.getClosestAstralGateInSystem(playerFleet);
        astralGateToken.setName(system.getBaseName() + " Astral Gate");

        if(Global.getSettings().getBoolean("boggledGateStationEnabled"))
        {
            playerCargo.removeCommodity("crew", crewCost);
            astralGateToken.setFaction(Global.getSector().getPlayerFaction().getId());

            SectorEntityToken gatekeeperStation = system.addCustomEntity(system.getBaseName() + "astralGatekeeperStation", system.getBaseName() + " Gatekeeper Station", "boggled_gatekeeper_station_small", Global.getSector().getPlayerFaction().getId());
            gatekeeperStation.setCircularOrbitPointingDown(astralGateToken, boggledTools.randomOrbitalAngleFloat(), 250f, 25f);
            gatekeeperStation.setInteractionImage("illustrations", "orbital_construction");

            //Create the gatekeeper station market
            MarketAPI market = Global.getFactory().createMarket(system.getBaseName() + "astralGatekeeperStationMarket", gatekeeperStation.getName(), 3);
            market.setSize(3);

            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            market.setPrimaryEntity(gatekeeperStation);

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
            market.addIndustry(Industries.SPACEPORT);
            market.addIndustry(BoggledStationConstructionIDs.BoggledStationConstructionIndustryIDs.ASTRAL_GATE);

            gatekeeperStation.setMarket(market);

            Global.getSector().getEconomy().addMarket(market, true);

            SectorEntityToken newGatekeeperLights = system.addCustomEntity("boggled_gatekeeperLights", market.getPrimaryEntity().getName() + " Gatekeeper Lights Overlay", "boggled_gatekeeper_station_small_lights_overlay", market.getFactionId());
            newGatekeeperLights.setOrbit(gatekeeperStation.getOrbit().makeCopy());

            //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
            Global.getSector().getCampaignUI().showInteractionDialog(gatekeeperStation);

            market.addSubmarket("storage");
            StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
            storage.setPlayerPaidToUnlock(true);
            market.addSubmarket("local_resources");

            GatekeeperEveryFrameScript gatekeeperEveryFrameScript = new GatekeeperEveryFrameScript(system);
            system.addScript(gatekeeperEveryFrameScript);
        }

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            playerCargo.removeCommodity("domain_artifacts", domainEraArtifactsCost);
        }

        boggledTools.updateListofActiveAstralGates(true);
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        if(Global.getSettings().getBoolean("boggledAstralGateRespectBlacklist"))
        {
            if(system.hasTag("gate_blacklist"))
            {
                return false;
            }
        }

        if(boggledTools.astralGateInSystem(playerFleet))
        {
            return false;
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            return false;
        }

        if(Global.getSettings().getBoolean("boggledGateStationEnabled") && playerCargo.getCommodityQuantity("crew") < crewCost)
        {
            return false;
        }

        if(playerCargo.getCommodityQuantity("metals") < metalCost)
        {
            return false;
        }

        if(playerCargo.getCommodityQuantity("rare_metals") < transplutonicsCost)
        {
            return false;
        }

        if(playerCargo.getCommodityQuantity("heavy_machinery") < heavyMachineryCost)
        {
            return false;
        }

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled") && playerCargo.getCommodityQuantity("domain_artifacts") < domainEraArtifactsCost)
        {
            return false;
        }

        if(this.isOnCooldown() || this.disableFrames > 0)
        {
            return false;
        }

        if(Global.getSettings().getBoolean("boggledAstralGateQuestEnabled") && !playerFleet.hasTag("boggledAstralGateUnlockedFromQuest"))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle("Construct Astral Gate");
        float pad = 10.0F;

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();

        if(Global.getSettings().getBoolean("boggledGateStationEnabled"))
        {
            if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
            {
                tooltip.addPara("Construct an Astral gate as near to your current location as possible given the conditions in the system. Expends %s credits, %s crew, %s heavy machinery, %s metals, %s transplutonics and %s Domain-era artifacts for construction. A gatekeeper station will also be constructed near the gate.", pad, highlight, new String[]{(int)creditCost + "", (int)crewCost + "", (int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +"", (int)domainEraArtifactsCost +""});
            }
            else
            {
                tooltip.addPara("Construct an Astral gate as near to your current location as possible given the conditions in the system. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction. A gatekeeper station will also be constructed near the gate.", pad, highlight, new String[]{(int)creditCost + "", (int)crewCost + "", (int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});
            }
        }
        else
        {
            if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
            {
                tooltip.addPara("Construct an Astral gate in this system. Expends %s credits, %s heavy machinery, %s metals, %s transplutonics and %s Domain-era artifacts for construction.", pad, highlight, new String[]{(int)creditCost + "", (int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +"", (int)domainEraArtifactsCost + ""});
            }
            else
            {
                tooltip.addPara("Construct an Astral gate in this system. Expends %s credits, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "", (int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});
            }
        }

        if(Global.getSettings().getBoolean("boggledAstralGateQuestEnabled") && !playerFleet.hasTag("boggledAstralGateUnlockedFromQuest"))
        {
            if(boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4") == null || boggledTools.getPlanetTokenForQuest("Galatia", "ancyra") == null || Global.getSector().getStarSystem("Askonia") == null)
            {
                tooltip.addPara("This save file has modifications to the core worlds that remove content this quest depends on. Note that the quest to obtain the blueprints cannot be initiated if the player is using randomized core worlds and/or uses mods that remove systems or planets.\n\nDisabling the Astral gate quest in the settings file will immediately allow activation of this ability to construct Astral gates.", bad, pad);
            }
            else
            {
                tooltip.addPara("You don't have the blueprints necessary to construct an Astral gate. Travel far enough away from the core worlds and perhaps you will unknowingly trod the path to acquiring them...", bad, pad);
            }
            return;
        }

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            tooltip.addPara("Astral gates utilize very sophisticated technology. If your engineers are forced to maintain the gates using crude Sector-produced components, it will be exorbitantly expensive. A steady supply of %s Domain-era artifacts will dramatically reduce costs. Construct a Domain Archeology industry on a planet with vast ruins to obtain enough artifacts.", pad, highlight, new String[]{"8"});
        }

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot construct an Astral gate in hyperspace.", bad, pad);
        }
        else if(boggledTools.astralGateInSystem(playerFleet))
        {
            tooltip.addPara("There is already an active Astral gate in this system.", bad, pad);
        }
        else if(Global.getSettings().getBoolean("boggledAstralGateRespectBlacklist"))
        {
            if(system.hasTag("gate_blacklist"))
            {
                tooltip.addPara("Astral gate construction is disabled in this system.", bad, pad);
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara("Insufficient credits.", bad, pad);
        }

        if(Global.getSettings().getBoolean("boggledGateStationEnabled") && playerCargo.getCommodityQuantity("crew") < crewCost)
        {
            tooltip.addPara("Insufficient crew.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara("Insufficient heavy machinery.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("metals") < metalCost)
        {
            tooltip.addPara("Insufficient metals.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("rare_metals") < transplutonicsCost)
        {
            tooltip.addPara("Insufficient transplutonics.", bad, pad);
        }

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled") && playerCargo.getCommodityQuantity("domain_artifacts") < domainEraArtifactsCost)
        {
            tooltip.addPara("Insufficient Domain-era artifacts.", bad, pad);
        }
    }

    @Override
    public boolean isTooltipExpandable() {
        return false;
    }

    @Override
    protected void applyEffect(float v, float v1) { }

    @Override
    protected void deactivateImpl() { }

    @Override
    protected void cleanupImpl() { }
}