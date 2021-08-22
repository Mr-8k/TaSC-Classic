package data.campaign.econ.industries;

import java.awt.Color;
import java.util.Iterator;
import java.util.Random;
import java.lang.String;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
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
import com.fs.starfarer.combat.entities.terrain.Planet;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;

public class Skyhook_Anchor extends BaseIndustry
{
    @Override
    public void apply()
    {
        super.apply(true);

        MarketAPI market = this.market;

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            this.demand(0, "domain_artifacts", 1, "Domain-era artifacts demand");
        }

        float aiCoreAccessBoost = 0.0f;
        if("alpha_core".equals(this.aiCoreId))
        {
            aiCoreAccessBoost = 0.15f;
        }
        else if("beta_core".equals(this.aiCoreId))
        {
            aiCoreAccessBoost = 0.10f;
        }
        else if("gamma_core".equals(this.aiCoreId))
        {
            aiCoreAccessBoost = 0.05f;
        }

        float gravityAccessBoost = 0.0f;
        if(market.hasCondition("high_gravity"))
        {
            gravityAccessBoost = 0.25f;
        }
        else if(market.hasCondition("low_gravity"))
        {
            gravityAccessBoost = 0.05f;
        }
        else
        {
            gravityAccessBoost = 0.15f;
        }

        market.getAccessibilityMod().modifyFlat(this.getModId(5), aiCoreAccessBoost + gravityAccessBoost, "Skyhook Anchor");

        if (!this.isFunctional())
        {
            this.unapply();
        }

        this.applyIncomeAndUpkeep(market.getSize());
    }

    @Override
    public void unapply()
    {
        super.unapply();

        this.market.getAccessibilityMod().unmodifyFlat(this.getModId(5));
    }

    @Override
    public void applyIncomeAndUpkeep(float sizeOverride)
    {
        float size = (float)this.market.getSize();
        if (sizeOverride >= 0.0F)
        {
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
        if (upkeep != 0)
        {
            this.getUpkeep().modifyFlatAlways("ind_base", (float)upkeep, "Base value");
            this.getUpkeep().modifyMultAlways("ind_hazard", upkeepMult, "Market upkeep multiplier");
        }
        else
        {
            this.getUpkeep().unmodifyFlat("ind_base");
            this.getUpkeep().unmodifyMult("ind_hazard");
        }

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            float domainArtifactDeficitMult = 1.0f;
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            if(deficit.two > 0)
            {
                domainArtifactDeficitMult = domainArtifactDeficitMult + (.5f * deficit.two);
                this.getUpkeep().modifyMultAlways("ind_domain_artifact_deficit", domainArtifactDeficitMult, "Domain-era artifacts deficit");
            }
            else
            {
                this.getUpkeep().unmodifyMult("ind_domain_artifact_deficit");
            }
        }

        if (!this.isFunctional())
        {
            this.getIncome().unmodifyFlat("ind_base");
            this.getIncome().unmodifyMult("ind_stability");
        }
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
            text.addPara(pre + "Further increases accessibility by %s.", 0.0F, highlight, "15%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Further increases accessibility by %s.", opad, highlight, "15%");
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
            text.addPara(pre + "Further increases accessibility by %s.", opad, highlight, "10%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Further increases accessibility by %s.", opad, highlight, "10%");
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
            text.addPara(pre + "Further increases accessibility by %s.", opad, highlight, "5%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Further increases accessibility by %s.", opad, highlight, "5%");
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
    public boolean isAvailableToBuild()
    {
        if(Global.getSettings().getBoolean("boggledSkyhookAnchorEnabled") && this.market.getPlanetEntity() != null && this.market.hasSpaceport() && boggledTools.marketHasOrbitalStation(this.market))
        {
            return true;
        }

        return false;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!Global.getSettings().getBoolean("boggledSkyhookAnchorEnabled") || this.market.getPlanetEntity() == null)
        {
            return false;
        }

        if(!this.market.hasSpaceport())
        {
            return true;
        }
        else if(!boggledTools.marketHasOrbitalStation(this.market))
        {
            return true;
        }

        return false;
    }

    @Override
    public String getUnavailableReason()
    {
        if(!this.market.hasSpaceport())
        {
            return (this.market.getName() + " lacks a functioning spaceport. It will be impossible to build a skyhook anchor in orbit around " + this.market.getName() + " due to logistical problems until a spaceport becomes operational.");
        }
        else if(!boggledTools.marketHasOrbitalStation(this.market))
        {
            return (this.market.getName() + " lacks an orbital station. The skyhook anchor must be attached to an orbital station that can act as a counterweight for lifting payloads to orbit.");
        }
        else
        {
            return "Error in getUnavailableReason() in the Skyhook Anchor structure. Please tell Boggled about this on the forums.";
        }
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(market.hasCondition("high_gravity"))
        {
            tooltip.addPara("Base accessibility boost: %s", opad, highlight, new String[]{"15%"});
            tooltip.addPara("Additional boost due to high gravity: %s", opad, highlight, new String[]{"10%"});
        }
        else if(market.hasCondition("low_gravity"))
        {
            tooltip.addPara("Base accessibility boost: %s", opad, highlight, new String[]{"15%"});
            tooltip.addPara("Reduction to boost due to low gravity: %s", opad, bad, new String[]{"-10%"});
        }
        else
        {
            tooltip.addPara("Base accessibility boost: %s", opad, highlight, new String[]{"15%"});
        }
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
                tooltip.addPara("Skyhook Anchors always demand %s Domain-era artifact regardless of market size.", opad, highlight, new String[]{"1"});
            }
        }
    }

    public float getPatherInterest() {
        return 3.0F + super.getPatherInterest();
    }
}
