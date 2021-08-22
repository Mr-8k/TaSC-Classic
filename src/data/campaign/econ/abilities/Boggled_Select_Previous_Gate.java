package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseAbilityPlugin;
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
import com.fs.starfarer.combat.entities.terrain.Planet;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;

public class Boggled_Select_Previous_Gate extends BaseDurationAbility
{
    public Boggled_Select_Previous_Gate() { }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if(boggledTools.getTargetGateIdFromPlayer() == 0)
        {
            boggledTools.removeGateIdTagFromPlayer();
            int targetGateId = boggledTools.getListOfActiveAstralGates().size() - 1;
            playerFleet.addTag("boggled_astral_gate_target_id_" + targetGateId);
        }
        else
        {
            int currentGateId = boggledTools.getTargetGateIdFromPlayer();
            boggledTools.removeGateIdTagFromPlayer();
            playerFleet.addTag("boggled_astral_gate_target_id_" + (currentGateId - 1));

            //Check if the newly assigned gate ID would be out of bounds in the gate list. If so, set the current gate to the last element in the list.
            if(boggledTools.getTargetGateIdFromPlayer() > boggledTools.getListOfActiveAstralGates().size() - 1)
            {
                boggledTools.removeGateIdTagFromPlayer();
                playerFleet.addTag("boggled_astral_gate_target_id_" + (boggledTools.getListOfActiveAstralGates().size() - 1));
            }
        }

        SectorEntityToken targetGate = boggledTools.getTargetGateToken(boggledTools.getTargetGateIdFromPlayer());
        if(targetGate.hasTag("boggled_astral_gate"))
        {
            playerFleet.addFloatingText(targetGate.getName(), Misc.setAlpha(Misc.getHighlightColor(), 255), 0.3F);
        }
        else
        {
            playerFleet.addFloatingText(targetGate.getStarSystem().getBaseName() + " " + targetGate.getName(), Misc.setAlpha(Misc.getHighlightColor(), 255), 0.3F);
        }
    }

    @Override
    public void activate() {
        if (this.isUsable() && !this.turnedOn) {
            this.turnedOn = true;
            this.loopFadeLeft = 0.0F;
            this.fadingOut = false;
            this.activeDaysLeft = this.getTotalDurationDays();
            if (this.entity.isInCurrentLocation() && this.entity.isVisibleToPlayerFleet() && !this.entity.isPlayerFleet()) {
                String soundId = this.getOnSoundWorld();
                if (soundId != null) {
                    Global.getSoundPlayer().playSound(soundId, 1.0F, 1.0F, this.entity.getLocation(), this.entity.getVelocity());
                }
            }

            if (this.getActivationDays() <= 0.0F) {
                this.level = 1.0F;
            }

            this.activateImpl();
            this.applyEffect(0.0F, this.level);
            this.interruptIncompatible();
            this.disableIncompatible();
            if (this.getTotalDurationDays() <= 0.0F) {
                this.deactivate();
            }

            //Reports the player used the ability instead of calling super methods
            //super.activate();
            CampaignFleetAPI fleet = this.getFleet();
            if (fleet != null && fleet.isPlayerFleet()) {
                Global.getSector().reportPlayerActivatedAbility(this, (Object)null);
            }
        }

    }

    @Override
    public boolean isUsable()
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        List<SectorEntityToken> allAstralGatesInSector = null;
        allAstralGatesInSector = boggledTools.getListOfActiveAstralGates();

        if(allAstralGatesInSector.size() < 2)
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

        LabelAPI title = tooltip.addTitle("Select Previous Gate");
        float pad = 10.0F;
        tooltip.addPara("Switch your destination gate selection to the previous gate in the sequence. The name of the selected gate will appear above your fleet.", pad, highlight, new String[]{});

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        CampaignClockAPI clock = Global.getSector().getClock();
        List<SectorEntityToken> allAstralGatesInSector = null;
        allAstralGatesInSector = boggledTools.getListOfActiveAstralGates();

        if(boggledTools.getTargetGateIdFromPlayer() > allAstralGatesInSector.size() - 1)
        {
            boggledTools.removeGateIdTagFromPlayer();
            playerFleet.addTag("boggled_astral_gate_target_id_" + 0);
        }

        if(allAstralGatesInSector.size() == 0)
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
        else
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