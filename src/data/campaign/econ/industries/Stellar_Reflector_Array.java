package data.campaign.econ.industries;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.String;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.CoreCampaignPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoreScript;
import com.fs.starfarer.api.impl.campaign.events.CoreEventProbabilityManager;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableLuddicPathFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.DisposablePirateFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.MercFleetManagerV2;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.*;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;

public class Stellar_Reflector_Array extends BaseIndustry
{
    public int getTerraformingProgressPoints()
    {
        if(this.isFunctional())
        {
            return 5;
        }
        else
        {
            return 0;
        }
    }

    public boolean canBeDisrupted() {
        return false;
    }

    public float getBaseUpkeep()
    {
        //This fixes the erroneous upkeep calculation on the industry install page
        return this.getSpec().getUpkeep();
    }

    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Massively reduces upkeep cost.", 0.0F, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Massively reduces upkeep cost.", opad, highlight, "");
        }
    }

    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Greatly reduces upkeep cost.", opad, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Greatly reduces upkeep cost.", opad, highlight, "");
        }
    }

    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Reduces upkeep cost.", opad, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Reduces upkeep cost.", opad, highlight, "");
        }
    }

    public void applyAICoreToIncomeAndUpkeep() {
        //This code is overwritten below because all three AI cores reduce the upkeep by a different amount
    }

    public void updateAICoreToSupplyAndDemandModifiers() {
        //There is no supply or demand for this industry, but put this here as a placeholder for if that changes
    }

    public void applyIncomeAndUpkeep(float sizeOverride)
    {
        float alphaCoreMult = 0.50f;
        float betaCoreMult = 0.75f;
        float gammaCoreMult = 0.90f;

        float sizeMult = 1.0f; //Prevents colony size from altering the upkeep amount
        float stabilityMult = this.market.getIncomeMult().getModifiedValue();
        float upkeepMult = this.market.getUpkeepMult().getModifiedValue();
        int income = (int)(this.getSpec().getIncome() * sizeMult);
        if (income != 0) {
            this.getIncome().modifyFlatAlways("ind_base", (float)income, "Base value");
            this.getIncome().modifyMultAlways("ind_stability", stabilityMult, "Market income multiplier");
        } else {
            this.getIncome().unmodifyFlat("ind_base");
            this.getIncome().unmodifyMult("ind_stability");
        }

        int upkeep = (int)(this.getSpec().getUpkeep() * sizeMult);
        if(!this.market.isPlayerOwned())
        {
            upkeep = 0;
        }
        if (upkeep != 0) {
            this.getUpkeep().modifyFlatAlways("ind_base", (float)upkeep, "Base value");
            this.getUpkeep().modifyMultAlways("ind_hazard", upkeepMult, "Market upkeep multiplier");
            if(this.aiCoreId == null)
            {
                //Do not reduce upkeep, and remove any previous upkeep-reducing effects from AI cores that were previously installed
                this.getUpkeep().unmodifyMult("ind_ai_core_alpha");
                this.getUpkeep().unmodifyMult("ind_ai_core_beta");
                this.getUpkeep().unmodifyMult("ind_ai_core_gamma");
            }
            else if(this.aiCoreId.equals("alpha_core"))
            {
                this.getUpkeep().modifyMultAlways("ind_ai_core_alpha", alphaCoreMult, "Alpha Core assigned");
                this.getUpkeep().unmodifyMult("ind_ai_core_beta");
                this.getUpkeep().unmodifyMult("ind_ai_core_gamma");
            }
            else if(this.aiCoreId.equals("beta_core"))
            {
                this.getUpkeep().modifyMultAlways("ind_ai_core_beta", betaCoreMult, "Beta Core assigned");
                this.getUpkeep().unmodifyMult("ind_ai_core_alpha");
                this.getUpkeep().unmodifyMult("ind_ai_core_gamma");
            }
            else if(this.aiCoreId.equals("gamma_core"))
            {
                this.getUpkeep().modifyMultAlways("ind_ai_core_gamma", gammaCoreMult, "Gamma Core assigned");
                this.getUpkeep().unmodifyMult("ind_ai_core_alpha");
                this.getUpkeep().unmodifyMult("ind_ai_core_beta");
            }
        } else {
            this.getUpkeep().unmodifyFlat("ind_base");
            this.getUpkeep().unmodifyMult("ind_hazard");
        }

        this.applyAICoreToIncomeAndUpkeep();
        if (!this.isFunctional()) {
            this.getIncome().unmodifyFlat("ind_base");
            this.getIncome().unmodifyMult("ind_stability");
        }
    }

    @Override
    public void finishBuildingOrUpgrading() {
        super.finishBuildingOrUpgrading();
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();

        MarketAPI market = this.market;
        boggledTools.removeCondition(market, "poor_light");
        boggledTools.addCondition(market, "solar_array");

        if(boggledTools.numReflectorsInOrbit(market) >= 3)
        {
            return;
        }

        boolean mirrorsOrShades = boggledTools.getCreateMirrorsOrShades(market);
        boggledTools.clearReflectorsInOrbit(market);

        // True is mirrors, false is shades
        // Don't remove reflectors even if industry is shut down
        if(mirrorsOrShades)
        {
            market.getPrimaryEntity().addTag("boggled_has_mirrors");

            SectorEntityToken orbitFocus = market.getPrimaryEntity().getOrbitFocus();
            float orbitRadius = market.getPrimaryEntity().getRadius() + 80.0F;
            if (orbitFocus != null && orbitFocus.isStar())
            {
                StarSystemAPI system = market.getStarSystem();

                //First mirror
                SectorEntityToken mirrorAlpha = system.addCustomEntity("stellar_mirror_alpha", ("Stellar Mirror Alpha"), "stellar_mirror", market.getFactionId());
                mirrorAlpha.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() - 30, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                mirrorAlpha.setCustomDescriptionId("stellar_mirror");

                //Second mirror
                SectorEntityToken mirrorBeta = system.addCustomEntity("stellar_mirror_beta", ("Stellar Mirror Beta"), "stellar_mirror", market.getFactionId());
                mirrorBeta.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle(), orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                mirrorBeta.setCustomDescriptionId("stellar_mirror");

                //Third mirror
                SectorEntityToken mirrorGamma = system.addCustomEntity("stellar_mirror_gamma", ("Stellar Mirror Gamma"), "stellar_mirror", market.getFactionId());
                mirrorGamma.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() + 30, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                mirrorGamma.setCustomDescriptionId("stellar_mirror");
            }
            else
            {
                StarSystemAPI system = market.getStarSystem();

                //First mirror
                SectorEntityToken mirrorAlpha = system.addCustomEntity("stellar_mirror_alpha", ("Stellar Mirror Alpha"), "stellar_mirror", market.getFactionId());
                mirrorAlpha.setCircularOrbitPointingDown(market.getPrimaryEntity(), 0, orbitRadius, orbitRadius / 10.0F);
                mirrorAlpha.setCustomDescriptionId("stellar_mirror");

                //Second mirror
                SectorEntityToken mirrorBeta = system.addCustomEntity("stellar_mirror_beta", ("Stellar Mirror Beta"), "stellar_mirror", market.getFactionId());
                mirrorBeta.setCircularOrbitPointingDown(market.getPrimaryEntity(), 120, orbitRadius, orbitRadius / 10.0F);
                mirrorBeta.setCustomDescriptionId("stellar_mirror");

                //Third mirror
                SectorEntityToken mirrorGamma = system.addCustomEntity("stellar_mirror_gamma", ("Stellar Mirror Gamma"), "stellar_mirror", this.market.getFactionId());
                mirrorGamma.setCircularOrbitPointingDown(market.getPrimaryEntity(), 240, orbitRadius, orbitRadius / 10.0F);
                mirrorGamma.setCustomDescriptionId("stellar_mirror");
            }
        }
        else
        {
            market.getPrimaryEntity().addTag("boggled_has_shades");

            SectorEntityToken orbitFocus = market.getPrimaryEntity().getOrbitFocus();
            float orbitRadius = market.getPrimaryEntity().getRadius() + 80.0F;
            if (orbitFocus != null && orbitFocus.isStar())
            {
                StarSystemAPI system = market.getStarSystem();

                //First shade
                SectorEntityToken shadeAlpha = system.addCustomEntity("stellar_shade_alpha", ("Stellar Shade Alpha"), "stellar_shade", market.getFactionId());
                shadeAlpha.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() + 154, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                shadeAlpha.setCustomDescriptionId("stellar_shade");

                //Second shade
                SectorEntityToken shadeBeta = system.addCustomEntity("stellar_shade_beta", ("Stellar Shade Beta"), "stellar_shade", market.getFactionId());
                shadeBeta.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() + 180, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                shadeBeta.setCustomDescriptionId("stellar_shade");

                //Third shade
                SectorEntityToken shadeGamma = system.addCustomEntity("stellar_shade_gamma", ("Stellar Shade Gamma"), "stellar_shade", market.getFactionId());
                shadeGamma.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() + 206, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                shadeGamma.setCustomDescriptionId("stellar_shade");
            }
            else
            {
                StarSystemAPI system = market.getStarSystem();

                //First shade
                SectorEntityToken shadeAlpha = system.addCustomEntity("stellar_shade_alpha", ("Stellar Shade Alpha"), "stellar_shade", market.getFactionId());
                shadeAlpha.setCircularOrbitPointingDown(market.getPrimaryEntity(), 0, orbitRadius, orbitRadius / 10.0F);
                shadeAlpha.setCustomDescriptionId("stellar_shade");

                //Second shade
                SectorEntityToken shadeBeta = system.addCustomEntity("stellar_shade_beta", ("Stellar Shade Beta"), "stellar_shade", market.getFactionId());
                shadeBeta.setCircularOrbitPointingDown(market.getPrimaryEntity(), 120, orbitRadius, orbitRadius / 10.0F);
                shadeBeta.setCustomDescriptionId("stellar_shade");

                //Third shade
                SectorEntityToken shadeGamma = system.addCustomEntity("stellar_shade_gamma", ("Stellar Shade Gamma"), "stellar_shade", market.getFactionId());
                shadeGamma.setCircularOrbitPointingDown(market.getPrimaryEntity(), 240, orbitRadius, orbitRadius / 10.0F);
                shadeGamma.setCustomDescriptionId("stellar_shade");
            }
        }
    }

    @Override
    public String getCurrentImage()
    {
        if(this.market.getPrimaryEntity().hasTag("boggled_has_mirrors"))
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_mirror");
        }
        else if(this.market.getPrimaryEntity().hasTag("boggled_has_shades"))
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_shade");
        }
        else if(boggledTools.getCreateMirrorsOrShades(this.market))
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_mirror");
        }
        else if(!boggledTools.getCreateMirrorsOrShades(this.market))
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_shade");
        }
        else
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_mirror");
        }
    }

    @Override
    public void apply() {
        super.apply(true);

        this.applyIncomeAndUpkeep(-1f);
    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        MarketAPI market = this.market;

        if(!Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        //Can't be built by station markets
        if(this.market.getPlanetEntity() == null) { return false; }

        //Can't be built on planets with poor light. All planets in nebulas and orbiting black holes have dark condition.
        if(market.hasCondition("dark")) { return false; }

        //Check if spaceport is built
        if(!market.hasSpaceport())
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(this.market.getPlanetEntity() == null || !Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        return true;
    }

    @Override
    public String getUnavailableReason()
    {
        MarketAPI market = this.market;

        //Can't be built on planets with poor light. All planets in nebulas and orbiting black holes have dark condition.
        if(market.hasCondition("dark")) { return "Stellar reflectors won't have any effect on a world that receives no light."; }

        //Can't be built on planets that already have an orbital shade array (only blocks Eochu Bres and Eventide in vanilla sector)
        if(boggledTools.numReflectorsInOrbit(market) >= 3) { return "There is already a stellar reflector constellation in orbit around " + this.market.getName() + "."; }

        //Check if spaceport is built
        if(!market.hasSpaceport())
        {
            return (this.market.getName() + " lacks a functioning spaceport. It will be impossible to build a stellar reflector array in orbit around " + this.market.getName() + " due to logistical problems until a spaceport becomes operational.");
        }

        return "Error in getUnavailableReason() in the Stellar Reflector Array. Please tell Boggled about this on the forums.";
    }

    public float getPatherInterest() { return 10.0F; }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        tooltip.addPara("Stellar Reflector Arrays contribute %s terraforming progress points per day.", opad, highlight, new String[]{"5"});
    }
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        super.notifyBeingRemoved(mode, forUpgrade);
        boggledTools.removeCondition(market, "solar_array");
    }
}

