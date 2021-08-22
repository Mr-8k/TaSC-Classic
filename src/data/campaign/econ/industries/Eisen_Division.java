package data.campaign.econ.industries;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
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
import com.fs.starfarer.campaign.CampaignPlanet;
import data.campaign.econ.BoggledStationConstructionIDs;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.StatModValueGetter;

import javax.lang.model.element.Element;

public class Eisen_Division extends BaseIndustry
{
    public boolean canBeDisrupted()
    {
        return false;
    }

    public float getBaseUpkeep() {
        //This fixes the erroneous upkeep calculation on the industry install page
        return this.getSpec().getUpkeep();
    }

    public int getTerraformingProgressPoints()
    {
        if(!this.isFunctional())
        {
            return 0;
        }
        else if(this.aiCoreId == null)
        {
            return 20;
        }
        else if(this.aiCoreId.equals("gamma_core"))
        {
            return 21;
        }
        else if(this.aiCoreId.equals("beta_core"))
        {
            return 23;
        }
        else if(this.aiCoreId.equals("alpha_core"))
        {
            return 25;
        }
        else
        {
            return 0;
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
            text.addPara(pre + "Adds an additional %s terraforming progress points per day.", 0.0F, highlight, "5");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Adds an additional %s terraforming progress points per day.", opad, highlight, "5");
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
            text.addPara(pre + "Adds an additional %s terraforming progress points per day.", opad, highlight, "3");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Adds an additional %s terraforming progress points per day.", opad, highlight, "3");
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
            text.addPara(pre + "Adds %s additional terraforming progress point per day.", opad, highlight, "1");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Adds %s additional terraforming progress point per day.", opad, highlight, "1");
        }
    }

    public void applyAICoreToIncomeAndUpkeep() {
        //Don't reduce upkeep cost if AI core is installed
    }

    public void updateAICoreToSupplyAndDemandModifiers() {
        //Stops AI cores from impacting upkeep
    }

    @Override
    public void apply() {
        super.apply(true);

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            this.demand(0, "domain_artifacts", 8, "Domain-era artifacts demand");
        }

        this.applyIncomeAndUpkeep(-1f);
    }

    public void applyIncomeAndUpkeep(float sizeOverride)
    {
        float sizeMult = 1.0f; //This is the only thing changed about this method. Now the upkeep cost will be constant regardless of colony size.
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
        if (upkeep != 0) {
            this.getUpkeep().modifyFlatAlways("ind_base", (float)upkeep, "Base value");
            this.getUpkeep().modifyMultAlways("ind_hazard", upkeepMult, "Market upkeep multiplier");
        } else {
            this.getUpkeep().unmodifyFlat("ind_base");
            this.getUpkeep().unmodifyMult("ind_hazard");
        }

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            float domainArtifactDeficitMult = 1.0f;
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            if(deficit.two > 0)
            {
                domainArtifactDeficitMult = domainArtifactDeficitMult + (.2f * deficit.two);
                this.getUpkeep().modifyMultAlways("ind_domain_artifact_deficit", domainArtifactDeficitMult, "Domain-era artifacts deficit");
            }
            else
            {
                this.getUpkeep().unmodifyMult("ind_domain_artifact_deficit");
            }
        }

        this.applyAICoreToIncomeAndUpkeep();
        if (!this.isFunctional())
        {
            this.getIncome().unmodifyFlat("ind_base");
            this.getIncome().unmodifyMult("ind_stability");
        }
    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public void finishBuildingOrUpgrading() {
        super.finishBuildingOrUpgrading();
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(this.market.getPlanetEntity() == null || !Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
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

        tooltip.addPara("Eisen Division contributes %s terraforming progress points per day.", opad, highlight, new String[]{"20"});
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode)
    {
        return mode != IndustryTooltipMode.NORMAL || this.isFunctional();
    }

    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        if (mode != IndustryTooltipMode.NORMAL || this.isFunctional())
        {
            float opad = 10.0F;
            Color highlight = Misc.getHighlightColor();
            Color bad = Misc.getNegativeHighlightColor();

            if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
            {
                tooltip.addPara("Eisen Division always demands %s Domain-era artifacts regardless of market size.", opad, highlight, new String[]{"8"});
            }
        }
    }
}
