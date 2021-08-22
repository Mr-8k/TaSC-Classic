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
import com.fs.starfarer.campaign.econ.Market;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;
import data.campaign.econ.plugins.BoggledNanoforgeInstallableItemPlugin;
import java.text.DecimalFormat;

public class Atmosphere_Processor extends BaseIndustry
{
    protected SpecialItemData nanoforge = null;

    public int getTerraformingProgressPoints()
    {
        if(!this.isFunctional())
        {
            return 0;
        }
        else
        {
            int terraforming_points = 1;
            if(this.aiCoreId == null)
            {
                //Do nothing
            }
            else if(this.aiCoreId.equals("gamma_core"))
            {
                terraforming_points = terraforming_points + 1;
            }
            else if(this.aiCoreId.equals("beta_core"))
            {
                terraforming_points = terraforming_points + 2;
            }
            else if(this.aiCoreId.equals("alpha_core"))
            {
                terraforming_points = terraforming_points + 3;
            }

            if(this.nanoforge != null && this.nanoforge.getId().equals("pristine_nanoforge"))
            {
                terraforming_points = terraforming_points + 6;
            }
            else if(this.nanoforge != null && this.nanoforge.getId().equals("corrupted_nanoforge"))
            {
                terraforming_points = terraforming_points + 3;
            }

            return terraforming_points;
        }
    }

    public void addInstalledItemsSection(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded)
    {
        float opad = 10.0F;
        FactionAPI faction = this.market.getFaction();
        Color color = faction.getBaseUIColor();
        Color dark = faction.getDarkUIColor();
        LabelAPI heading = tooltip.addSectionHeading("Items", color, dark, Alignment.MID, opad);
        boolean addedSomething = false;
        if (this.aiCoreId != null)
        {
            AICoreDescriptionMode aiCoreDescMode = AICoreDescriptionMode.INDUSTRY_TOOLTIP;
            this.addAICoreSection(tooltip, this.aiCoreId, aiCoreDescMode);
            addedSomething = true;
        }

        addedSomething |= this.addNonAICoreInstalledItems(mode, tooltip, expanded);
        if (!addedSomething)
        {
            heading.setText("No items installed");
        }
    }

    protected boolean addNonAICoreInstalledItems(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded)
    {
        if (this.nanoforge == null)
        {
            return false;
        }
        else
        {
            float opad = 10.0F;
            FactionAPI faction = this.market.getFaction();
            Color color = faction.getBaseUIColor();
            Color dark = faction.getDarkUIColor();
            SpecialItemSpecAPI nanoforgeSpec = Global.getSettings().getSpecialItemSpec(this.nanoforge.getId());
            TooltipMakerAPI text = tooltip.beginImageWithText(nanoforgeSpec.getIconName(), 48.0F);
            BoggledNanoforgeInstallableItemPlugin.NanoforgeEffect effect = (BoggledNanoforgeInstallableItemPlugin.NanoforgeEffect)BoggledNanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.get(this.nanoforge.getId());
            effect.addItemDescription(text, this.nanoforge, InstallableIndustryItemPlugin.InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
            tooltip.addImageWithText(opad);
            return true;
        }
    }

    public List<InstallableIndustryItemPlugin> getInstallableItems()
    {
        ArrayList<InstallableIndustryItemPlugin> list = new ArrayList();
        list.add(new BoggledNanoforgeInstallableItemPlugin(this));
        return list;
    }

    public void initWithParams(List<String> params)
    {
        super.initWithParams(params);
        Iterator var3 = params.iterator();

        while(var3.hasNext()) {
            String str = (String)var3.next();
            if (BoggledNanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.containsKey(str))
            {
                this.setNanoforge(new SpecialItemData(str, (String)null));
                break;
            }
        }
    }

    public List<SpecialItemData> getVisibleInstalledItems()
    {
        List<SpecialItemData> result = super.getVisibleInstalledItems();
        if (this.nanoforge != null)
        {
            result.add(this.nanoforge);
        }

        return result;
    }

    public void setNanoforge(SpecialItemData nanoforge)
    {
        this.nanoforge = nanoforge;
    }

    public SpecialItemData getNanoforge() {
        return this.nanoforge;
    }

    public SpecialItemData getSpecialItem() {
        return this.nanoforge;
    }

    public void setSpecialItem(SpecialItemData special) {
        this.nanoforge = special;
    }

    public boolean wantsToUseSpecialItem(SpecialItemData data)
    {
        if (this.nanoforge != null && "corrupted_nanoforge".equals(this.nanoforge.getId()) && data != null && "pristine_nanoforge".equals(data.getId()))
        {
            return true;
        }
        else
        {
            return this.nanoforge == null && data != null && BoggledNanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.containsKey(data.getId());
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
            text.addPara(pre + "Adds an additional %s terraforming progress points per day.", 0.0F, highlight, "3");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Adds an additional %s terraforming progress points per day.", opad, highlight, "3");
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
            text.addPara(pre + "Adds an additional %s terraforming progress points per day.", opad, highlight, "2");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Adds an additional %s terraforming progress points per day.", opad, highlight, "2");
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

    public void applyAICoreToIncomeAndUpkeep()
    {
        //This code is overwritten below because all three AI cores reduce the upkeep by a different amount
    }

    public void updateAICoreToSupplyAndDemandModifiers()
    {
        // Prevents AI cores from modifying supply and demand
    }

    public void applyIncomeAndUpkeep(float sizeOverride)
    {
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
        } else {
            this.getUpkeep().unmodifyFlat("ind_base");
            this.getUpkeep().unmodifyMult("ind_hazard");
        }

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
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        //mode is null if removed by the industry itself
        if(mode == null || mode.equals(MarketAPI.MarketInteractionMode.REMOTE))
        {
            if (this.nanoforge != null)
            {
                CargoAPI cargo = this.market.getSubmarket("storage").getCargo();
                if (cargo != null)
                {
                    cargo.addSpecial(this.nanoforge, 1.0F);
                }
            }

            if (this.aiCoreId != null)
            {
                CargoAPI cargo = this.market.getSubmarket("storage").getCargo();
                if (cargo != null)
                {
                    cargo.addCommodity(this.aiCoreId, 1.0F);
                }
            }
        }
        else if(mode.equals(MarketAPI.MarketInteractionMode.LOCAL))
        {
            if (this.nanoforge != null)
            {
                CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
                if (cargo != null)
                {
                    cargo.addSpecial(this.nanoforge, 1.0F);
                }
            }

            if (this.aiCoreId != null)
            {
                CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
                if (cargo != null)
                {
                    cargo.addCommodity(this.aiCoreId, 1.0F);
                }
            }
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
        PlanetAPI planet = market.getPlanetEntity();

        //Can't be built by station markets
        if(planet == null || !Global.getSettings().getBoolean("boggledTerraformingContentEnabled")) { return false; }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    public float getPatherInterest() {
        if (this.aiCoreId == null) {
            return 4.0F;
        } else if ("alpha_core".equals(this.aiCoreId)) {
            return 8.0F;
        } else if ("beta_core".equals(this.aiCoreId)) {
            return 6.0F;
        } else {
            return 5f;
        }
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        tooltip.addPara("Atmosphere Processors contribute %s terraforming progress point per day. This can be increased by installing a nanoforge.", opad, highlight, new String[]{"1"});
    }
}

