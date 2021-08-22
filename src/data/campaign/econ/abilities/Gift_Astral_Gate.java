package data.campaign.econ.abilities;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.combat.entities.terrain.Planet;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;
import data.scripts.GatekeeperEveryFrameScript;
import data.scripts.GatekeeperEveryFrameScript;
import data.scripts.BoggledAstralGateStipend;

public class Gift_Astral_Gate extends BaseDurationAbility
{
    private float creditCost = Global.getSettings().getInt("boggledGateBuildCreditCost");
    private float crewCost  = Global.getSettings().getInt("boggledGateBuildCrewCost");
    private float heavyMachineryCost = Global.getSettings().getInt("boggledGateBuildHeavyMachineryCost");
    private float metalCost = Global.getSettings().getInt("boggledGateBuildMetalCost");
    private float transplutonicsCost = Global.getSettings().getInt("boggledGateBuildTransplutonicsCost");
    private float domainEraArtifactsCost = Global.getSettings().getInt("boggledGateBuildDomainEraArtifactsCost");

    private float monthlyRevenue = Global.getSettings().getInt("boggledGiftGateMonthlyPayment");

    public Gift_Astral_Gate() { }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        CargoAPI playerCargo = playerFleet.getCargo();
        StarSystemAPI system = playerFleet.getStarSystem();

        HashMap<String, Integer> factionPopHash = boggledTools.getControllingFactionForGiftGate(system);
        Map.Entry<String,Integer> entry = factionPopHash.entrySet().iterator().next();
        String faction = entry.getKey();
        Integer totalPop = entry.getValue();

        new BoggledAstralGateStipend(faction, Global.getSector().getPlayerFleet().getStarSystem().getId());

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

            astralGateToken.setFaction(faction);

            SectorEntityToken gatekeeperStation = system.addCustomEntity(system.getBaseName() + "astralGatekeeperStation", system.getBaseName() + " Gatekeeper Station", "boggled_gatekeeper_station_small", faction);
            gatekeeperStation.setCircularOrbitPointingDown(astralGateToken, boggledTools.randomOrbitalAngleFloat(), 250f, 25f);
            gatekeeperStation.setInteractionImage("illustrations", "orbital_construction");

            //Create the gatekeeper station market
            MarketAPI market = Global.getFactory().createMarket(system.getBaseName() + "astralGatekeeperStationMarket", gatekeeperStation.getName(), 3);
            market.setSize(3);

            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            market.setPrimaryEntity(gatekeeperStation);

            market.setFactionId(faction);
            market.setPlayerOwned(false);

            market.addCondition(Conditions.POPULATION_3);
            market.addCondition("sprite_controller");
            market.addCondition("cramped_quarters");

            market.addIndustry(Industries.POPULATION);
            market.addIndustry(Industries.SPACEPORT);
            market.addIndustry(BoggledStationConstructionIDs.BoggledStationConstructionIndustryIDs.ASTRAL_GATE);

            gatekeeperStation.setMarket(market);

            Global.getSector().getEconomy().addMarket(market, true);

            SectorEntityToken newGatekeeperLights = system.addCustomEntity("boggled_gatekeeperLights", market.getPrimaryEntity().getName() + " Gatekeeper Lights Overlay", "boggled_gatekeeper_station_small_lights_overlay", market.getFactionId());
            newGatekeeperLights.setOrbit(gatekeeperStation.getOrbit().makeCopy());

            market.addSubmarket("storage");
            StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
            storage.setPlayerPaidToUnlock(false);
            market.addSubmarket("open_market");
            market.addSubmarket("black_market");

            PersonAPI adminPerson = market.getFaction().createRandomPerson();
            market.setAdmin(adminPerson);
            adminPerson.setPostId(Ranks.POST_ADMINISTRATOR);
            market.getCommDirectory().addPerson(adminPerson);

            GatekeeperEveryFrameScript gatekeeperEveryFrameScript = new GatekeeperEveryFrameScript(system);
            system.addScript(gatekeeperEveryFrameScript);

            //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
            Global.getSector().getCampaignUI().showInteractionDialog(gatekeeperStation);
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

        if(boggledTools.getListOfActiveAstralGates().size() < 2)
        {
            return false;
        }

        HashMap<String, Integer> factionPopHash = boggledTools.getControllingFactionForGiftGate(system);
        if(factionPopHash == null || factionPopHash.size() != 1)
        {
            return false;
        }
        else
        {
            Map.Entry<String,Integer> entry = factionPopHash.entrySet().iterator().next();
            String faction = entry.getKey();
            Integer totalPop = entry.getValue();

            if(faction.equals("boggled_none"))
            {
                return false;
            }
            else if(faction.equals("boggled_tied"))
            {
                return false;
            }
            else if(totalPop < 5)
            {
                return false;
            }
            else if(faction.equals(Global.getSector().getPlayerFaction().getId()))
            {
                return false;
            }
            else if(Global.getSector().getFaction(faction).getRelationshipLevel(Global.getSector().getPlayerFaction().getId()) == RepLevel.HOSTILE)
            {
                return false;
            }
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

        LabelAPI title = tooltip.addTitle("Gift Astral Gate");
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

        HashMap<String, Integer> factionPopHash = boggledTools.getControllingFactionForGiftGate(system);
        if(factionPopHash != null && factionPopHash.size() == 1)
        {
            Map.Entry<String,Integer> entry = factionPopHash.entrySet().iterator().next();
            String faction = entry.getKey();
            Integer totalPop = entry.getValue();

            if(!faction.equals("boggled_none") && !faction.equals("boggled_tied"))
            {
                tooltip.addPara("Recipient faction: %s", pad, Global.getSector().getFaction(faction).getBaseUIColor(), new String[]{Global.getSector().getFaction(faction).getDisplayNameLong()});
            }
        }

        tooltip.addPara("Gifting an Astral gate to another faction will place it under their control. They will compensate you for constructing the gate via monthly payments of %s for three cycles. Thanks to the most powerful force in the universe (also known as compound interest), the total value of these payments will far exceed the upfront cost of constructing the gate. The payments will be terminated immediately if hostiles are initiated with the payor.", pad, highlight, new String[]{monthlyRevenue + ""});

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

        if(boggledTools.getListOfActiveAstralGates().size() < 2)
        {
            tooltip.addPara("No faction believes your claims about the capabilities of the Astral gate design right now. Once you build two gates yourself to demonstrate that the design works, other factions will be convinced.", bad, pad);
        }

        if(!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            if(factionPopHash == null || factionPopHash.size() != 1)
            {
                tooltip.addPara("There was an error determining which faction(s) control this system. Please tell Boggled about this on the forum.", bad, pad);
            }
            else
            {
                Map.Entry<String,Integer> entry = factionPopHash.entrySet().iterator().next();
                String faction = entry.getKey();
                Integer totalPop = entry.getValue();

                if(faction.equals("boggled_none"))
                {
                    tooltip.addPara("No faction has a presence in this system.", bad, pad);
                }
                else if(faction.equals("boggled_tied"))
                {
                    tooltip.addPara("Two or more factions have a roughly equal presence in this system. Neither faction is willing to pay for an Astral gate to be built here given the elevated risk that they could lose control of the system.", bad, pad);
                }
                else if(faction.equals(Global.getSector().getPlayerFaction().getId()))
                {
                    tooltip.addPara("Your faction controls this system. No other faction will pay you to build an Astral gate in your own territory.", bad, pad);
                }
                else if(Global.getSector().getFaction(faction).getRelationshipLevel(Global.getSector().getPlayerFaction().getId()) == RepLevel.HOSTILE)
                {
                    tooltip.addPara("The dominant faction in the this system is " + Global.getSector().getFaction(faction).getDisplayNameWithArticle() + " " + Global.getSector().getFaction(faction).getDisplayNameIsOrAre() + ". They are hostile towards you and refuse to cooperate to build an Astral gate here.", bad, pad);
                }
                else if(totalPop < 5)
                {
                    tooltip.addPara("The dominant faction in the this system is " + Global.getSector().getFaction(faction).getDisplayNameWithArticle() + " " + Global.getSector().getFaction(faction).getDisplayNameIsOrAre() + ". Their presence is too minimal to justify the cost of constructing an Astral gate here.", bad, pad);
                }
            }
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