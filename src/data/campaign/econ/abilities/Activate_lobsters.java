package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import data.campaign.econ.boggledTools;

public class Activate_lobsters extends BaseDurationAbility
{
    public Activate_lobsters() { }

    @Override
    protected void activateImpl()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        MarketAPI market = boggledTools.getClosestPlanetToken(playerFleet).getMarket();
        market.addCondition("volturnian_lobster_pens");

        boggledTools.surveyAll(market);
        boggledTools.refreshAquacultureAndFarming(market);
        boggledTools.refreshSupplyAndDemand(market);

        MessageIntel intel = new MessageIntel("Lobsters seeded on " + market.getName(), Misc.getBasePlayerColor());
        intel.addLine("    - Complete");
        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
    }

    @Override
    public boolean isUsable()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        if(!playerFleet.hasTag("boggled_lobster_quest_hasGeneEditor") && Global.getSettings().getBoolean("boggledLobsterQuestEnabled"))
        {
            return false;
        }

        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()) && !boggledTools.planetInSystem(playerFleet))
        {
            return false;
        }

        SectorEntityToken closestPlanetToken = boggledTools.getClosestPlanetToken(playerFleet);
        PlanetAPI closestPlanetAPI = ((PlanetAPI)closestPlanetToken);

        if(boggledTools.getPlanetType(closestPlanetAPI).equals("unknown") && !Global.getSettings().getBoolean("boggledIgnorePlanetTypeAndAllowSeedingLobstersAnywhere"))
        {
            return false;
        }
        if(!boggledTools.getPlanetType(closestPlanetAPI).equals("water") && !Global.getSettings().getBoolean("boggledIgnorePlanetTypeAndAllowSeedingLobstersAnywhere"))
        {
            return false;
        }
        else if(closestPlanetToken.getMarket() == null || !closestPlanetToken.getMarket().isPlayerOwned())
        {
            return false;
        }
        else if(closestPlanetToken.getMarket().hasCondition("volturnian_lobster_pens"))
        {
            return false;
        }
        else if(boggledTools.getDistanceBetweenTokens(playerFleet, closestPlanetToken) > closestPlanetToken.getRadius() + 400f)
        {
            return false;
        }

        return !this.isOnCooldown() && this.disableFrames <= 0;
    }

    @Override
    public boolean hasTooltip() { return true; }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle("Seed Lobsters");
        float pad = 10.0F;
        if(playerFleet.hasTag("boggled_lobster_quest_hasGeneEditor") || !Global.getSettings().getBoolean("boggledLobsterQuestEnabled"))
        {
            tooltip.addPara("Use your Domain-era gene editing equipment to create a new variation of the Volturnian lobster. The traits of the new lobsters will be customized to ensure they can thrive on any planet you choose to seed.", pad, highlight, new String[]{""});
        }
        else
        {
            tooltip.addPara("Volturnian lobsters were designed to thrive in the unique conditions on Volturn. They can't survive anywhere else.\n\nRumor has it that specialized Domain-era gene editing equipment may still exist on Volturn, and that it could be used to create new species of lobsters that can survive on other planets.\n\nYou may be able to acquire the fabled equipment for yourself with the help of some miscreants in the Askonia system...", pad, highlight, new String[]{""});
        }

        boolean planetCheckOK = true;

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot seed lobsters in hyperspace.", bad, pad);
            planetCheckOK = false;
        }

        if(!playerFleet.hasTag("boggled_lobster_quest_hasGeneEditor") && Global.getSettings().getBoolean("boggledLobsterQuestEnabled"))
        {
            tooltip.addPara("You lack the technology necessary to create new variations of the Volturnian lobster that can survive on other planets.", bad, pad);
            return;
        }

        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()) && !boggledTools.planetInSystem(playerFleet))
        {
            tooltip.addPara("There are no planets in this system.", bad, pad);
            planetCheckOK = false;
        }

        if(planetCheckOK)
        {
            SectorEntityToken closestPlanetToken = boggledTools.getClosestPlanetToken(playerFleet);
            PlanetAPI closestPlanetAPI = ((PlanetAPI)closestPlanetToken);

            if(this.isUsable())
            {
                tooltip.addPara("Lobster seeding target: %s\n\nNote: Volturnian lobsters can only be harvested using the aquaculture industry. You cannot build and/or maintain this industry on any planet type except water worlds.", pad, highlight, new String[]{closestPlanetToken.getName()});
            }

            if(boggledTools.getPlanetType(closestPlanetAPI).equals("unknown"))
            {
                tooltip.addPara("The planet closest to your location is " + closestPlanetToken.getName() + ". ERROR: This planet type appears to be unsupported in Boggled's Terraforming Mod. Please let Boggled know about this in the forum thread for his terraforming mod so he can add support. Unsupported planet type: " + closestPlanetAPI.getTypeId(), bad, pad);
            }
            else if(!boggledTools.getPlanetType(closestPlanetAPI).equals("water") && !Global.getSettings().getBoolean("boggledIgnorePlanetTypeAndAllowSeedingLobstersAnywhere"))
            {
                //tooltip.addPara("The planet closest to your location is " + closestPlanetToken.getName() + ". Volturnian lobsters can only be seeded on water worlds.\n\nTEMPORARY DEBUG TEXT: Planet type found: " + boggledTools.getPlanetType(closestPlanetAPI) + ". Planet radius: " + closestPlanetToken.getRadius() + ".", bad, pad);
                tooltip.addPara("The planet closest to your location is " + closestPlanetToken.getName() + ". Volturnian lobsters can only be seeded on water worlds.", bad, pad);
            }
            else if(!boggledTools.getPlanetType(closestPlanetAPI).equals("water") && Global.getSettings().getBoolean("boggledIgnorePlanetTypeAndAllowSeedingLobstersAnywhere"))
            {
                //tooltip.addPara("The planet closest to your location is " + closestPlanetToken.getName() + ". Volturnian lobsters can only be seeded on water worlds.\n\nTEMPORARY DEBUG TEXT: Planet type found: " + boggledTools.getPlanetType(closestPlanetAPI) + ". Planet radius: " + closestPlanetToken.getRadius() + ".", bad, pad);
                tooltip.addPara("The planet closest to your location is " + closestPlanetToken.getName() + ". Volturnian lobsters can only be seeded on water worlds. OVERRIDDEN USING SETTINGS FILE. LOBSTERS CAN BE SEEDED ON ANY WORLD THE PLAYER CONTROLS.", bad, pad);
            }
            else if(closestPlanetToken.getMarket().hasCondition("volturnian_lobster_pens"))
            {
                tooltip.addPara("The planet closest to your location is " + closestPlanetToken.getName() + ", which already has lobsters present.", bad, pad);
            }
            else if(closestPlanetToken.getMarket() == null || !closestPlanetToken.getMarket().isPlayerOwned())
            {
                tooltip.addPara("The planet closest to your location is " + closestPlanetToken.getName() + ". You can only seed lobsters on planets you control.", bad, pad);
            }
            else if(boggledTools.getDistanceBetweenTokens(playerFleet, closestPlanetToken) > closestPlanetToken.getRadius() + 400f)
            {
                float distanceInSu = boggledTools.getDistanceBetweenTokens(playerFleet, closestPlanetToken) / 2000f;
                String distanceInSuString = String.format("%.1f", distanceInSu);
                float requiredDistanceInSu = (closestPlanetToken.getRadius() + 400f) / 2000f;
                String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
                tooltip.addPara("The planet closest to your location is " + closestPlanetToken.getName() + ". Your fleet is " + distanceInSuString + " stellar units away. You must be within " + requiredDistanceInSuString + " stellar units to seed lobsters.", bad, pad);
            }
        }
    }

    @Override
    public boolean isTooltipExpandable() { return false; }

    @Override
    protected void applyEffect(float v, float v1) { }

    @Override
    protected void deactivateImpl() { }

    @Override
    protected void cleanupImpl() { }
}