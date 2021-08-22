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
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
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
import data.campaign.econ.plugins.BoggledPheromoneSplicerNanoforgePlugin;

import javax.swing.*;

public class Boggled_Pheromone_Splicer extends BaseIndustry implements MarketImmigrationModifier
{
    public boolean canBeDisrupted() {
        return true;
    }

    protected SpecialItemData nanoforge = null;

    public void apply()
    {
        if(this.isFunctional() && this.market.hasCondition("boggled_arcology_world"))
        {
            this.market.getStability().modifyFlat(this.getModId(), (float)-3, this.getNameForModifier());
        }

        super.apply(true);
    }

    public void unapply()
    {
        this.market.getStability().unmodifyFlat(this.getModId());

        super.unapply();
    }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming)
    {
        if(this.isFunctional() && this.market.hasCondition("boggled_arcology_world"))
        {
            float bonus = getPopulationGrowthBonusMultiplier() * this.market.getSize();
            incoming.getWeight().modifyFlat(this.getModId(), bonus, this.getNameForModifier());
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
        return "Error in getUnavailableReason() in the pheromone splicer structure. Please tell Boggled about this on the forums.";
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

        if(this.market.hasCondition("boggled_arcology_world") && this.isFunctional())
        {
            tooltip.addPara("Current population growth bonus multiplier: %s\nCurrent colony size: %s\nOverall bonus population growth: +%s", opad, highlight, new String[]{getPopulationGrowthBonusMultiplier() + "", this.market.getSize() + "", (getPopulationGrowthBonusMultiplier() * this.market.getSize()) + ""});
            tooltip.addPara("Stability malus: %s", opad, highlight, new String[]{"-3"});
        }
        else if(this.market.hasCondition("boggled_arcology_world") && !this.isFunctional())
        {
            tooltip.addPara("The Pheromone Splicer is under construction and is not increasing population growth.", opad, highlight, new String[]{getPopulationGrowthBonusMultiplier() + "", this.market.getSize() + "", (getPopulationGrowthBonusMultiplier() * this.market.getSize()) + ""});
        }
        else
        {
            tooltip.addPara("The Pheromone Splicer is only useful on worlds where most inhabitants live in arcologies.", opad, highlight, new String[]{""});
        }
    }

    private int getPopulationGrowthBonusMultiplier()
    {
        int bonus = 8;
        if(this.isFunctional())
        {
            if (this.aiCoreId == null)
            {
                //Do nothing.
            }
            else if (this.aiCoreId.equals("gamma_core"))
            {
                bonus = bonus + 1;
            }
            else if (this.aiCoreId.equals("beta_core"))
            {
                bonus = bonus + 2;
            }
            else if (this.aiCoreId.equals("alpha_core"))
            {
                bonus = bonus + 3;
            }

            if(this.nanoforge != null && this.nanoforge.getId().equals("pristine_nanoforge"))
            {
                bonus = bonus + 4;
            }
            else if(this.nanoforge != null && this.nanoforge.getId().equals("corrupted_nanoforge"))
            {
                bonus = bonus + 2;
            }

            return bonus;
        }
        else
        {
            return 0;
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
            BoggledPheromoneSplicerNanoforgePlugin.NanoforgeEffect effect = (BoggledPheromoneSplicerNanoforgePlugin.NanoforgeEffect)BoggledPheromoneSplicerNanoforgePlugin.NANOFORGE_EFFECTS.get(this.nanoforge.getId());
            effect.addItemDescription(text, this.nanoforge, InstallableIndustryItemPlugin.InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
            tooltip.addImageWithText(opad);
            return true;
        }
    }

    public List<InstallableIndustryItemPlugin> getInstallableItems()
    {
        ArrayList<InstallableIndustryItemPlugin> list = new ArrayList();
        list.add(new BoggledPheromoneSplicerNanoforgePlugin(this));
        return list;
    }

    public void initWithParams(List<String> params)
    {
        super.initWithParams(params);
        Iterator var3 = params.iterator();

        while(var3.hasNext()) {
            String str = (String)var3.next();
            if (BoggledPheromoneSplicerNanoforgePlugin.NANOFORGE_EFFECTS.containsKey(str))
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
            return this.nanoforge == null && data != null && BoggledPheromoneSplicerNanoforgePlugin.NANOFORGE_EFFECTS.containsKey(data.getId());
        }
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
            text.addPara(pre + "Massively improves pheromone sophistication, increasing the population growth bonus multiplier by %s.", 0.0F, highlight, "3");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Massively improves pheromone sophistication, increasing the population growth bonus multiplier by %s.", opad, highlight, "3");
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
            text.addPara(pre + "Significantly improves pheromone sophistication, increasing the population growth bonus multiplier by %s.", opad, highlight, "2");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Significantly improves pheromone sophistication, increasing the population growth bonus multiplier by %s.", opad, highlight, "2");
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
            text.addPara(pre + "Improves pheromone sophistication, increasing the population growth bonus multiplier by %s.", opad, highlight, "1");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Improves pheromone sophistication, increasing the population growth bonus multiplier by %s.", opad, highlight, "1");
        }
    }

    public void applyAICoreToIncomeAndUpkeep() {
        //Don't reduce upkeep cost if AI core is installed
    }

    public void updateAICoreToSupplyAndDemandModifiers() {
        //Stops AI cores from impacting upkeep
    }
}

