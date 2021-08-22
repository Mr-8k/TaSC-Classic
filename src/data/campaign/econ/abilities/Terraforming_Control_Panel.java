package data.campaign.econ.abilities;

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
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import com.fs.starfarer.combat.entities.terrain.Planet;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;
import data.campaign.quests.boggledGateQuest.WindsMessageDialogPlugin;
import data.scripts.TerraformingControlPanelDialog;

public class Terraforming_Control_Panel extends BaseDurationAbility
{
    public Terraforming_Control_Panel() { }

    @Override
    protected void activateImpl()
    {
        Global.getSector().getCampaignUI().showInteractionDialog(new TerraformingControlPanelDialog(), boggledTools.getClosestValidPlanetSectorEntityTokenInSystem(Global.getSector().getPlayerFleet().getStarSystem()));
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        SectorEntityToken planet = boggledTools.getClosestValidPlanetSectorEntityTokenInSystem(playerFleet.getStarSystem());
        if(planet == null)
        {
            return false;
        }

        if(this.isOnCooldown() || this.disableFrames > 0)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasTooltip()
    {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle("Terraforming Control Panel");
        float pad = 10.0F;
        tooltip.addPara("Click to open the terraforming control panel for the closest player-controlled world.", pad, highlight, new String[]{});

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot terraform objects located in hyperspace.", bad, pad);
            return;
        }

        StarSystemAPI system = playerFleet.getStarSystem();
        SectorEntityToken targetPlanet = boggledTools.getClosestValidPlanetSectorEntityTokenInSystem(system);

        if(targetPlanet == null)
        {
            tooltip.addPara("There are no player-controlled worlds in this system.", bad, pad);
        }
        else
        {
            tooltip.addPara("Closest world: %s", pad, highlight, new String[]{targetPlanet.getMarket().getName()});
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