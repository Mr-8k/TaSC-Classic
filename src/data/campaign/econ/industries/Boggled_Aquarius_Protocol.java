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

import javax.swing.*;

public class Boggled_Aquarius_Protocol extends BaseIndustry
{
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);
    }

    public void apply()
    {
        super.apply(true);

        if(this.isFunctional() && this.market.hasCondition("boggled_arcology_world"))
        {
            if (this.aiCoreId == null)
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)1, this.getNameForModifier());
            }
            else if (this.aiCoreId.equals("gamma_core"))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)2, this.getNameForModifier());
            }
            else if (this.aiCoreId.equals("beta_core"))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)4, this.getNameForModifier());
            }
            else if (this.aiCoreId.equals("alpha_core"))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)6, this.getNameForModifier());
            }
        }

    }

    public void unapply()
    {
        this.market.getStability().unmodifyFlat(this.getModId());
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        MarketAPI market = this.market;
        if(market.hasCondition("boggled_arcology_world") || Global.getSettings().getBoolean("boggledArcologySpecificStructuresCanBeBuiltOnAnyPlanetType"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    @Override
    public String getUnavailableReason()
    {
        return "Error in getUnavailableReason() in the aquarius protocol structure. Please tell Boggled about this on the forums.";
    }

    public float getPatherInterest() {
        return 10.0F;
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(this.market.hasCondition("boggled_arcology_world"))
        {
            tooltip.addPara("The Aquarius Protocol increases stability by %s.", opad, highlight, new String[]{"1"});
        }
        else
        {
            tooltip.addPara("The Aquarius Protocol is only useful on worlds where most inhabitants live in arcologies.", opad, highlight, new String[]{"1"});
        }

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
            text.addPara(pre + "Further increases stability by %s.", 0.0F, highlight, "5");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Further increases stability by %s.", opad, highlight, "5");
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
            text.addPara(pre + "Further increases stability by %s.", opad, highlight, "3");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Further increases stability by %s.", opad, highlight, "3");
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
            text.addPara(pre + "Further increases stability by %s.", opad, highlight, "1");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Further increases stability by %s.", opad, highlight, "1");
        }
    }

    public void applyAICoreToIncomeAndUpkeep() {
        //Don't reduce upkeep cost if AI core is installed
    }

    public void updateAICoreToSupplyAndDemandModifiers() {
        //Stops AI cores from impacting upkeep
    }
}

