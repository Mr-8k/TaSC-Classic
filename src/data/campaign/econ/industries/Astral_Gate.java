package data.campaign.econ.industries;

import java.awt.Color;
import java.util.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
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

public class Astral_Gate extends BaseIndustry
{
    public boolean canBeDisrupted() {
        return false;
    }

    public boolean showShutDown() {
        return true;
    }

    public boolean canShutDown() {
        return false;
    }

    public String getCanNotShutDownReason()
    {
        return "The astral gate is a core component of this station and cannot be disabled. Consider abandoning the station if the monthly upkeep is unaffordable.";
    }

    public void advance(float amount)
    {
        super.advance(amount);
    }

    @Override
    public void apply()
    {
        super.apply(true);

        MarketAPI market = this.market;
        StarSystemAPI system = market.getStarSystem();

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            this.demand(0, "domain_artifacts", 8, "Domain-era artifacts demand");
        }

        this.applyIncomeAndUpkeep(-1f);

        //Increases accessibility on the Astral gate station
        if(Global.getSettings().getInt("boggledGatekeeperStationAccessBoost") != 0)
        {
            float access = (float)Global.getSettings().getInt("boggledGatekeeperStationAccessBoost") / 100.0F;
            market.getAccessibilityMod().modifyFlat(this.getModId(5), access, "Gatekeeper station");
        }

        //Increases accessibility for allied markets in system
        Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(system).iterator();
        while(allMarketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)allMarketsInSystem.next();
            if ((marketElement.getFactionId().equals(market.getFactionId()) || marketElement.getFaction().getRelationshipLevel(market.getFaction()).equals(RepLevel.COOPERATIVE)) && !marketElement.getPrimaryEntity().hasTag("boggled_gatekeeper_station"))
            {
                if(Global.getSettings().getInt("boggledFriendlyMarketsInSystemAstralGateAccessBoost") != 0)
                {
                    float access = (float)Global.getSettings().getInt("boggledFriendlyMarketsInSystemAstralGateAccessBoost") / 100.0F;
                    marketElement.getAccessibilityMod().modifyFlat(this.getModId(6), access, "Ally-controlled Astral gate in system");
                }
            }
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();

        this.market.getAccessibilityMod().unmodifyFlat(this.getModId(5));

        //Removes accessibility boost for allied markets in system
        StarSystemAPI system = market.getStarSystem();
        Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(system).iterator();
        while(allMarketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)allMarketsInSystem.next();
            if (!marketElement.getPrimaryEntity().hasTag("boggled_gatekeeper_station"))
            {
                marketElement.getAccessibilityMod().unmodifyFlat(this.getModId(6));
            }
        }
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        super.notifyBeingRemoved(mode, forUpgrade);

        //Removes accessibility boost for allied markets in system
        StarSystemAPI system = market.getStarSystem();
        Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(system).iterator();
        while(allMarketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)allMarketsInSystem.next();
            if (!marketElement.getPrimaryEntity().hasTag("boggled_gatekeeper_station"))
            {
                marketElement.getAccessibilityMod().unmodifyFlat(this.getModId(6));
            }
        }
    }

    public void applyIncomeAndUpkeep(float sizeOverride)
    {
        float alphaCoreMult = 0.50f;
        float betaCoreMult = 0.75f;
        float gammaCoreMult = 0.9f;

        float sizeMult = 1.0f;
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
    public float getBaseUpkeep()
    {
        //This fixes the erroneous upkeep calculation on the industry install page
        return this.getSpec().getUpkeep();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    public float getPatherInterest() { return 10.0F; }

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
        //Do nothing right now.
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
                tooltip.addPara("Astral Gates always demand %s Domain-era artifacts regardless of market size.", opad, highlight, new String[]{"8"});
            }
        }
    }
}
