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

public class Kletka_Simulator extends BaseIndustry
{
    public boolean canBeDisrupted()
    {
        return false;
    }

    public CargoAPI generateCargoForGatheringPoint(Random random)
    {
        if (!this.isFunctional())
        {
            return null;
        }
        else
        {
            CargoAPI result = Global.getFactory().createCargo(true);
            result.clear();
            float roll = random.nextFloat() * 100f;

            if(this.aiCoreId == null)
            {
                if(roll > 95f)
                {
                    result.addCommodity("beta_core", 1f);
                }
                else if(roll > 50f)
                {
                    result.addCommodity("gamma_core", 1f);
                }
            }
            else if(this.aiCoreId.equals("alpha_core"))
            {
                if(roll > 70f)
                {
                    result.addCommodity("alpha_core", 1f);
                }
                else if(roll > 40f)
                {
                    result.addCommodity("beta_core", 1f);
                }
                else if(roll > 10f)
                {
                    result.addCommodity("gamma_core", 1f);
                }
            }
            else if(this.aiCoreId.equals("beta_core"))
            {
                if(roll > 90f)
                {
                    result.addCommodity("alpha_core", 1f);
                }
                else if(roll > 60f)
                {
                    result.addCommodity("beta_core", 1f);
                }
                else if(roll > 30f)
                {
                    result.addCommodity("gamma_core", 1f);
                }
            }
            else if(this.aiCoreId.equals("gamma_core"))
            {
                if(roll > 95f)
                {
                    result.addCommodity("alpha_core", 1f);
                }
                else if(roll > 70f)
                {
                    result.addCommodity("beta_core", 1f);
                }
                else if(roll > 45f)
                {
                    result.addCommodity("gamma_core", 1f);
                }
            }

            return result;
        }
    }

    public float getBaseUpkeep() {
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
            text.addPara(pre + "Massively improves AI core training methodology.", 0.0F, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Massively improves AI core training methodology.", opad, highlight, "");
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
            text.addPara(pre + "Greatly improves AI core training methodology.", opad, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Greatly improves AI core training methodology.", opad, highlight, "");
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
            text.addPara(pre + "Improves AI core training methodology.", opad, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Improves AI core training methodology.", opad, highlight, "");
        }
    }

    public void applyAICoreToIncomeAndUpkeep() {
        //Don't reduce upkeep cost if AI core is installed
    }

    public void updateAICoreToSupplyAndDemandModifiers() {
        //There is no supply or demand for this industry, but put this here as a placeholder for if that changes
    }

    @Override
    public void apply() {
        super.apply(true);

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            this.demand(0, "domain_artifacts", 4, "Domain-era artifacts demand");
        }

        this.applyIncomeAndUpkeep(-1f);
    }

    public void applyIncomeAndUpkeep(float sizeOverride) {
        float size = (float)this.market.getSize();
        if (sizeOverride >= 0.0F) {
            size = sizeOverride;
        }

        float sizeMult = getSizeMult(size);
        sizeMult = 1.0f; //This is the only thing changed about this method. Now the upkeep cost will be constant regardless of colony size.
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
                domainArtifactDeficitMult = domainArtifactDeficitMult + (.25f * deficit.two);
                this.getUpkeep().modifyMultAlways("ind_domain_artifact_deficit", domainArtifactDeficitMult, "Domain-era artifacts deficit");
            }
            else
            {
                this.getUpkeep().unmodifyMult("ind_domain_artifact_deficit");
            }
        }

        if(Global.getSettings().getBoolean("boggledKletkaSimulatorTemperateBasedUpkeep"))
        {
            if(this.market.hasCondition("very_cold"))
            {
                this.getUpkeep().modifyMultAlways("ind_boggled_temperature", 0.05f, "Temperature modifier");
            }
            else if(this.market.hasCondition("cold"))
            {
                this.getUpkeep().modifyMultAlways("ind_boggled_temperature", 0.15f, "Temperature modifier");
            }
            else if(this.market.hasCondition("hot"))
            {
                this.getUpkeep().modifyMultAlways("ind_boggled_temperature", 1.5f, "Temperature modifier");
            }
            else if(this.market.hasCondition("very_hot"))
            {
                this.getUpkeep().modifyMultAlways("ind_boggled_temperature", 3.0f, "Temperature modifier");
            }
            else if(this.market.getPrimaryEntity() != null && this.market.getPrimaryEntity().hasTag("station"))
            {
                this.getUpkeep().modifyMultAlways("ind_boggled_temperature", 4.0f, "Station modifier");
            }
            else
            {
                this.getUpkeep().modifyMultAlways("ind_boggled_temperature", 1.0f, "Temperature modifier");
            }
        }
        else
        {
            //Reduces upkeep to compensate for the extremely high value in industries.csv when
            //temperature modification is disabled
            this.getUpkeep().modifyMultAlways("ind_boggled_temperature", 0.2f, "Standard reduction");
        }

        this.applyAICoreToIncomeAndUpkeep();
        if (!this.isFunctional()) {
            this.getIncome().unmodifyFlat("ind_base");
            this.getIncome().unmodifyMult("ind_stability");
            this.getIncome().unmodifyMult("ind_boggled_temperature");
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

        this.market.addTag("BOGGLED_KLETKA_SIMULATOR_SHOW_TOOLTIP");
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        //Check to ensure non-player factions cannot build this
        if(this.market.isPlayerOwned() && Global.getSettings().getBoolean("boggledKletkaSimulatorEnabled"))
        {
            return true;
        }

        return false;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    public float getPatherInterest() { return 10.0F; }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(Global.getSettings().getBoolean("boggledKletkaSimulatorTemperateBasedUpkeep"))
        {
            tooltip.addPara("Supercomputers will melt themselves without adequate cooling systems. Operating costs are lowest on very cold worlds and highest on stations.", opad, highlight, new String[]{""});
        }

        if(this.isBuilding() || !this.market.hasTag("BOGGLED_KLETKA_SIMULATOR_SHOW_TOOLTIP"))
        {
            return;
        }

        if((this.aiCoreId == null) && this.isFunctional())
        {
            tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, new String[]{"5%","45%","50%"});
        }
        else if(this.aiCoreId.equals("gamma_core") && this.isFunctional())
        {
            tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, new String[]{"5%","25%","25%","45%"});
        }
        else if(this.aiCoreId.equals("beta_core") && this.isFunctional())
        {
            tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, new String[]{"10%","30%","30%","30%"});
        }
        else if(this.aiCoreId.equals("alpha_core") && this.isFunctional())
        {
            tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, new String[]{"30%","30%","30%","10%"});
        }
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);

        this.market.removeTag("BOGGLED_KLETKA_SIMULATOR_SHOW_TOOLTIP");
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
                tooltip.addPara("Kletka Simulators always demand %s Domain-era artifacts regardless of market size.", opad, highlight, new String[]{"4"});
            }
        }
    }
}
