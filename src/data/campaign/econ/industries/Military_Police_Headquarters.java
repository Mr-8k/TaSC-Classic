package data.campaign.econ.industries;

import java.awt.Color;
import java.util.Collection;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
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
import com.fs.starfarer.campaign.CampaignPlanet;
import data.campaign.econ.BoggledStationConstructionIDs;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.lang.model.element.Element;
import data.campaign.econ.boggledTools;

public class Military_Police_Headquarters extends BaseIndustry {

    private int daysWithoutShortage = 0;
    private int lastDayChecked = 0;

    public boolean canBeDisrupted() {
        return false;
    }

    public void advance(float amount)
    {
        super.advance(amount);

        if (this.isFunctional())
        {
            CampaignClockAPI clock = Global.getSector().getClock();

            if(clock.getDay() != lastDayChecked)
            {
                Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"hand_weapons", "marines"});
                if(deficit.two == 0)
                {
                    daysWithoutShortage++;
                    lastDayChecked = clock.getDay();
                }

                if(daysWithoutShortage >= 200)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Decivilized subpopulation on " + market.getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Assimilated");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                    }

                    if(this.market.hasCondition("decivilized_subpop"))
                    {
                        this.market.removeCondition("decivilized_subpop");
                    }

                    if (this.aiCoreId != null)
                    {
                        CargoAPI cargo = this.market.getSubmarket("storage").getCargo(); //Places core in planet storage
                        //CargoAPI cargo = this.getCargoForInteractionMode(MarketAPI.MarketInteractionMode.LOCAL); //Places core in player fleet storage
                        if (cargo != null)
                        {
                            cargo.addCommodity(this.aiCoreId, 1.0F);
                        }
                    }

                    boggledTools.surveyAll(market);
                    boggledTools.refreshSupplyAndDemand(market);
                    boggledTools.refreshAquacultureAndFarming(market);

                    if(this.market.hasIndustry("MILITARY_POLICE_HEADQUARTERS"))
                    {
                        this.market.removeIndustry("MILITARY_POLICE_HEADQUARTERS", null, false);
                    }
                }
            }
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
        //Installed AI cores don't alter the supply and demand of this structure
    }

    public void applyIncomeAndUpkeep(float sizeOverride) {
        float size = (float)this.market.getSize();
        if (sizeOverride >= 0.0F) {
            size = sizeOverride;
        }

        float alphaCoreMult = 0.50f;
        float betaCoreMult = 0.75f;
        float gammaCoreMult = 0.90f;

        float sizeMult = getSizeMult(size);
        sizeMult = Math.max(1.0F, sizeMult - 2.0F);
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
    public void apply() {
        super.apply(true);
        int size = this.market.getSize();

        this.applyIncomeAndUpkeep(size);

        this.demand("marines", size - 2);
        this.demand("hand_weapons", size - 2);
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

        this.market.addTag("BOGGLED_MILITARY_POLICE_HEADQUARTERS_SHOW_TOOLTIP");
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        super.notifyBeingRemoved(mode, forUpgrade);

        this.market.removeTag("BOGGLED_MILITARY_POLICE_HEADQUARTERS_SHOW_TOOLTIP");
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        //Verify planet has a decivilized subpopulation
        if (!this.market.hasCondition("decivilized_subpop"))
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

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        if(this.isBuilding() || !this.market.hasTag("BOGGLED_MILITARY_POLICE_HEADQUARTERS_SHOW_TOOLTIP"))
        {
            return;
        }

        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        //Inserts pacification status after description
        if(this.isFunctional())
        {
            //200 days to recivilize; divide daysWithoutShortage by 2 to get the percent
            int percentComplete = daysWithoutShortage / 2;

            //Shouldn't come up except for the very last day
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("Approximately %s of the decivilized subpopulation on " + this.market.getName() + " has been pacified and brought back under control of the central government.", opad, highlight, new String[]{percentComplete + "%"});
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

            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"hand_weapons", "marines"});
            if(deficit.two != 0)
            {
                tooltip.addPara("Recivilization progress is stalled due to a shortage of marines and/or heavy armaments.", bad, opad);
            }
        }
    }
}
