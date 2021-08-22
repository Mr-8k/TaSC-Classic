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
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.campaign.CampaignClock;
import com.fs.starfarer.combat.entities.terrain.Planet;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;

public class Boggled_Initiate_Recall extends BaseDurationAbility
{
    private float creditCost = Global.getSettings().getInt("boggledGateJumpCreditCost");
    private float fuelCost = Global.getSettings().getInt("boggledGateJumpFuelCost");

    private List<SectorEntityToken> allAstralGatesInSectorLowPerfStored = null;
    private int lastDayUpdated = 0;

    public Boggled_Initiate_Recall() { }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if(!Global.getSettings().getBoolean("boggledGateStationEnabled"))
        {
            CargoAPI playerCargo = playerFleet.getCargo();
            playerCargo.getCredits().subtract(creditCost);
            playerCargo.removeCommodity("fuel", fuelCost);
        }
        else if(Global.getSettings().getBoolean("boggledGateStationEnabled") && boggledTools.getClosestAstralGateInSystem(playerFleet).getFaction().getId().equals("neutral"))
        {
            CargoAPI playerCargo = playerFleet.getCargo();
            playerCargo.getCredits().subtract(creditCost);
            playerCargo.removeCommodity("fuel", fuelCost);
        }

        CampaignFleetAPI playerFleetCamp = Global.getSector().getPlayerFleet();
        SectorEntityToken destinationGate = boggledTools.getTargetGateToken(boggledTools.getTargetGateIdFromPlayer());
        JumpPointAPI.JumpDestination dest = new JumpPointAPI.JumpDestination(destinationGate, null);

        Global.getSector().doHyperspaceTransition(playerFleetCamp, boggledTools.getClosestAstralGateInSystem(playerFleet), dest);

        //Below is an alternate method preserved in the event bugs/problems arise with the doHyperspaceTransition method above
        /*
        StarSystemAPI oldSystem = playerFleet.getStarSystem();
        oldSystem.removeEntity(playerFleet);
        StarSystemAPI newSystem = playerTarget.getStarSystem();
        newSystem.addEntity(playerFleet);
        playerFleet.setLocation(playerTarget.getLocation().x, playerTarget.getLocation().y);
        Global.getSector().setCurrentLocation(playerFleet.getContainingLocation());
        playerFleet.clearAssignments();
        playerFleet.setInteractionTarget(playerTarget);
         */
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        CargoAPI playerCargo = playerFleet.getCargo();
        CampaignClockAPI clock = Global.getSector().getClock();

        //On new game, player fleet has no tags
        if(boggledTools.getTargetGateIdFromPlayer() == -1)
        {
            boggledTools.removeGateIdTagFromPlayer();
            playerFleet.addTag("boggled_astral_gate_target_id_" + 0);
        }

        List<SectorEntityToken> allAstralGatesInSector = boggledTools.getListOfActiveAstralGates();

        if(boggledTools.getTargetGateIdFromPlayer() > allAstralGatesInSector.size() - 1)
        {
            boggledTools.removeGateIdTagFromPlayer();
            playerFleet.addTag("boggled_astral_gate_target_id_" + 0);
        }

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }
        else if(allAstralGatesInSector.size() == 0)
        {
            return false;
        }
        else if(allAstralGatesInSector.size() == 1 && boggledTools.astralGateInSystem(playerFleet))
        {
            return false;
        }
        else if(!boggledTools.astralGateInSystem(playerFleet))
        {
            return false;
        }
        else if(boggledTools.getClosestAstralGateInSystem(playerFleet).equals(allAstralGatesInSector.get(boggledTools.getTargetGateIdFromPlayer())))
        {
            return false;
        }
        else if(!boggledTools.gateJumpPermittedByControllingFaction(boggledTools.getClosestAstralGateInSystem(playerFleet)))
        {
            return false;
        }
        else if(boggledTools.getDistanceBetweenTokens(boggledTools.getClosestAstralGateInSystem(playerFleet), playerFleet) > 400f)
        {
            return false;
        }
        else if(playerCargo.getCredits().get() < creditCost && !Global.getSettings().getBoolean("boggledGateStationEnabled"))
        {
            return false;
        }
        else if(playerCargo.getCommodityQuantity("fuel") < fuelCost && !Global.getSettings().getBoolean("boggledGateStationEnabled"))
        {
            return false;
        }
        else if(playerCargo.getCredits().get() < creditCost && Global.getSettings().getBoolean("boggledGateStationEnabled") && boggledTools.astralGateInSystem(playerFleet) && boggledTools.getClosestAstralGateInSystem(playerFleet).getFaction().getId().equals("neutral"))
        {
            return false;
        }
        else if(playerCargo.getCommodityQuantity("fuel") < fuelCost && Global.getSettings().getBoolean("boggledGateStationEnabled") && boggledTools.astralGateInSystem(playerFleet) && boggledTools.getClosestAstralGateInSystem(playerFleet).getFaction().getId().equals("neutral"))
        {
            return false;
        }
        else if(this.isOnCooldown() || this.disableFrames > 0)
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

        LabelAPI title = tooltip.addTitle("Initiate Recall Jump");
        float pad = 10.0F;

        if(Global.getSettings().getBoolean("boggledGateStationEnabled"))
        {
            tooltip.addPara("Direct your fleet to traverse the warped space-time bubble at the center of the Astral gate. When your fleet enters the bubble, it will be recalled by your chosen destination gate. Other factions will only permit you to use their gates if you are on good terms with them. Costs %s credits and %s fuel if no faction is managing the gate.", pad, highlight, new String[]{(int)creditCost + "", (int)fuelCost + ""});
        }
        else
        {
            tooltip.addPara("Direct your fleet to traverse the warped space-time bubble at the center of the Astral gate. When your fleet enters the bubble, it will be recalled by your chosen destination gate. Costs %s credits and %s fuel.", pad, highlight, new String[]{(int)creditCost + "", (int)fuelCost + ""});
        }

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        List<SectorEntityToken> allAstralGatesInSector = boggledTools.getListOfActiveAstralGates();

        if(this.isUsable())
        {
            SectorEntityToken targetGate = boggledTools.getTargetGateToken(boggledTools.getTargetGateIdFromPlayer());
            if(targetGate.hasTag("boggled_astral_gate"))
            {
                tooltip.addPara("Recall jump destination: %s", pad, highlight, new String[]{targetGate.getName()});
            }
            else
            {
                tooltip.addPara("Recall jump destination: %s", pad, highlight, new String[]{targetGate.getStarSystem().getBaseName() + " " + targetGate.getName()});
            }
        }
        else if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot initiate a recall jump in hyperspace.", bad, pad);
        }
        else if(allAstralGatesInSector.size() == 0)
        {
            tooltip.addPara("There are no traversable astral gates in the Sector.", bad, pad);
        }
        else if(allAstralGatesInSector.size() == 1)
        {
            SectorEntityToken targetGate = boggledTools.getTargetGateToken(boggledTools.getTargetGateIdFromPlayer());
            if(targetGate.hasTag("boggled_astral_gate"))
            {
                tooltip.addPara("Recall jump destination: %s", pad, highlight, new String[]{targetGate.getName()});
            }
            else
            {
                tooltip.addPara("Recall jump destination: %s", pad, highlight, new String[]{targetGate.getStarSystem().getBaseName() + " " + targetGate.getName()});
            }
            tooltip.addPara("There is only one traversable astral gate in the Sector. There are no other gates to be recalled to.", bad, pad);
        }
        else if(!boggledTools.astralGateInSystem(playerFleet))
        {
            SectorEntityToken targetGate = boggledTools.getTargetGateToken(boggledTools.getTargetGateIdFromPlayer());
            if(targetGate.hasTag("boggled_astral_gate"))
            {
                tooltip.addPara("Recall jump destination: %s", pad, highlight, new String[]{targetGate.getName()});
            }
            else
            {
                tooltip.addPara("Recall jump destination: %s", pad, highlight, new String[]{targetGate.getStarSystem().getBaseName() + " " + targetGate.getName()});
            }
            tooltip.addPara("There is no traversable astral gate in this system. You must be near an Astral gate to initiate a recall jump.", bad, pad);
        }
        else if(boggledTools.getClosestAstralGateInSystem(playerFleet).equals(boggledTools.getTargetGateToken(boggledTools.getTargetGateIdFromPlayer())))
        {
            SectorEntityToken targetGate = boggledTools.getTargetGateToken(boggledTools.getTargetGateIdFromPlayer());
            if(targetGate.hasTag("boggled_astral_gate"))
            {
                tooltip.addPara("Recall jump destination: %s", pad, highlight, new String[]{targetGate.getName()});
            }
            else
            {
                tooltip.addPara("Recall jump destination: %s", pad, highlight, new String[]{targetGate.getStarSystem().getBaseName() + " " + targetGate.getName()});
            }
            tooltip.addPara("You are already nearby to your selected destination gate.", bad, pad);
        }
        else if(!boggledTools.gateJumpPermittedByControllingFaction(boggledTools.getClosestAstralGateInSystem(playerFleet)))
        {
            tooltip.addPara("You cannot traverse the " + boggledTools.getClosestAstralGateInSystem(playerFleet).getName() + " because the " + boggledTools.getClosestAstralGateInSystem(playerFleet).getFaction().getDisplayNameLong() + " control the gatekeeper station. To use the gate, you must either improve your relationship with the "  + boggledTools.getClosestAstralGateInSystem(playerFleet).getFaction().getDisplayNameLong() + " or evict them from gatekeeper station.", bad, pad);
        }
        else if(boggledTools.getDistanceBetweenTokens(boggledTools.getClosestAstralGateInSystem(playerFleet), playerFleet) > 400f)
        {
            SectorEntityToken closestAstralGate = boggledTools.getClosestAstralGateInSystem(playerFleet);
            float distanceInSu = boggledTools.getDistanceBetweenTokens(playerFleet, closestAstralGate) / 2000f;
            String distanceInSuString = String.format("%.1f", distanceInSu);
            float requiredDistanceInSu = (closestAstralGate.getRadius() + 200f) / 2000f;
            String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
            tooltip.addPara("Your fleet is " + distanceInSuString + " stellar units away from the closest astral gate. You must be within " + requiredDistanceInSuString + " stellar units to initiate a recall jump.", bad, pad);
        }

        if(!Global.getSettings().getBoolean("boggledGateStationEnabled"))
        {
            CargoAPI playerCargo = playerFleet.getCargo();
            if(playerCargo.getCredits().get() < creditCost)
            {
                tooltip.addPara("Insufficient credits.", bad, pad);
            }

            if(playerCargo.getCommodityQuantity("fuel") < fuelCost)
            {
                tooltip.addPara("Insufficient fuel.", bad, pad);
            }
        }

        if(Global.getSettings().getBoolean("boggledGateStationEnabled") && !playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && boggledTools.astralGateInSystem(playerFleet))
        {
            SectorEntityToken entryGate = boggledTools.getClosestAstralGateInSystem(playerFleet);
            if(entryGate.getFaction().getId().equals("neutral"))
            {
                CargoAPI playerCargo = playerFleet.getCargo();
                if(playerCargo.getCredits().get() < creditCost)
                {
                    tooltip.addPara("Insufficient credits. You must pay %s credits to use the " + entryGate.getName() + " because the gatekeeper station is abandoned and no faction is funding the recall jump costs.", pad, highlight, new String[]{(int)creditCost + ""});
                }
                else
                {
                    tooltip.addPara("You must pay %s credits to use the " + entryGate.getName() + " because the gatekeeper station is abandoned and no faction is funding the recall jump costs.", pad, highlight, new String[]{(int)creditCost + ""});
                }

                if(playerCargo.getCommodityQuantity("fuel") < fuelCost)
                {
                    tooltip.addPara("Insufficient fuel. You must pay %s fuel to use the " + entryGate.getName() + " because the gatekeeper station is abandoned and no faction is funding the recall jump costs.", pad, highlight, new String[]{(int)fuelCost + ""});
                }
                else
                {
                    tooltip.addPara("You must pay %s fuel to use the " + entryGate.getName() + " because the gatekeeper station is abandoned and no faction is funding the recall jump costs.", pad, highlight, new String[]{(int)fuelCost + ""});
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