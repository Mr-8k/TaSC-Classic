package data.campaign.econ.industries;

import java.awt.Color;
import java.util.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.fleets.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.CoreCampaignPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoreScript;
import com.fs.starfarer.api.impl.campaign.events.CoreEventProbabilityManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CampaignPlanet;
import com.fs.starfarer.campaign.JumpPoint;
import com.fs.starfarer.combat.entities.terrain.Planet;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.campaign.econ.BoggledStationConstructionIDs;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import org.lwjgl.util.vector.Vector2f;

public class Boggled_Smart_Grid extends BaseIndustry
{
    private final int PRODUCTION_BONUS = 1;

    public boolean canBeDisrupted() {
        return false;
    }

    public void advance(float amount)
    {
        super.advance(amount);
    }

    @Override
    public void apply()
    {
        if(this.market.hasCondition("boggled_arcology_world") && this.isFunctional())
        {
            this.market.getHazard().modifyFlat(id, -.5F, "Smart grid");

            //Increased production
            for(Industry i : market.getIndustries())
            {
                for(MutableCommodityQuantity c : i.getAllSupply())
                {
                    i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, PRODUCTION_BONUS, "Smart grid");
                }
            }
        }

        super.apply(true);
    }

    @Override
    public void unapply()
    {
        this.market.getHazard().unmodifyFlat(id);

        //Increased production unmodify
        for(Industry i : market.getIndustries())
        {
            for(MutableCommodityQuantity c : i.getAllSupply())
            {
                i.getSupply(c.getCommodityId()).getQuantity().unmodifyFlat(id);
            }
        }

        super.unapply();
    }

    public void applyIncomeAndUpkeep(float sizeOverride)
    {
        float alphaCoreMult = 0.25f;
        float betaCoreMult = 0.50f;
        float gammaCoreMult = 0.75f;

        float size = (float)this.market.getSize();
        if (sizeOverride >= 0.0F) {
            size = sizeOverride;
        }

        float sizeMult = getSizeMult(size);
        sizeMult = Math.max(1.0F, sizeMult - 2.0F);
        float stabilityMult = this.market.getIncomeMult().getModifiedValue();
        float upkeepMult = this.market.getUpkeepMult().getModifiedValue();
        int income = (int)(this.getSpec().getIncome() * sizeMult);
        if (income != 0)
        {
            this.getIncome().modifyFlatAlways("ind_base", (float)income, "Base value");
            this.getIncome().modifyMultAlways("ind_stability", stabilityMult, "Market income multiplier");
        }
        else
        {
            this.getIncome().unmodifyFlat("ind_base");
            this.getIncome().unmodifyMult("ind_stability");
        }

        int upkeep = (int)(this.getSpec().getUpkeep() * sizeMult);
        if(!this.market.isPlayerOwned())
        {
            upkeep = 0;
        }
        if (upkeep != 0)
        {
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
        }
        else
        {
            this.getUpkeep().unmodifyFlat("ind_base");
            this.getUpkeep().unmodifyMult("ind_hazard");
        }

        this.applyAICoreToIncomeAndUpkeep();
        if (!this.isFunctional())
        {
            this.getIncome().unmodifyFlat("ind_base");
            this.getIncome().unmodifyMult("ind_stability");
        }
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
        return "Error in getUnavailableReason() in the smart grid structure. Please tell Boggled about this on the forums.";
    }

    public float getPatherInterest() {
        return 4.0F + super.getPatherInterest();
    }

    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
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

    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
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

    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
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

    public void applyAICoreToIncomeAndUpkeep()
    {
        //This being blank prevents installed AI cores from altering monthly upkeep
    }

    public void updateAICoreToSupplyAndDemandModifiers()
    {
        //This being blank prevents AI cores from reducing the demand
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(this.market.hasCondition("boggled_arcology_world"))
        {
            tooltip.addPara("%s hazard rating", opad, highlight, new String[]{"-50%"});
            tooltip.addPara("All industries supply %s more unit of all the commodities they produce", opad, highlight, new String[]{"1"});
        }
        else
        {
            tooltip.addPara("A smart grid is only useful on worlds where most inhabitants live in arcologies.", opad, highlight, new String[]{""});
        }
    }
}
