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
import data.campaign.econ.boggledTools;

import javax.lang.model.element.Element;

public class Ismara_Sling extends BaseIndustry
{
    public boolean canBeDisrupted()
    {
        return false;
    }

    public float getBaseUpkeep()
    {
        //This fixes the erroneous upkeep calculation on the industry install page
        return this.getSpec().getUpkeep();
    }

    @Override
    public String getCurrentName()
    {
        if(slingInstalledOnStation())
        {
            return "Asteroid Breaking";
        }
        else
        {
            return "Ismara's Sling";
        }
    }

    @Override
    public String getCurrentImage()
    {
        if(slingInstalledOnStation())
        {
            return Global.getSettings().getSpriteName("boggled", "asteroid_processing");
        }
        else
        {
            return this.getSpec().getImageName();
        }
    }

    @Override
    protected String getDescriptionOverride()
    {
        if(slingInstalledOnStation())
        {
            return "Crashing asteroids rich in water-ice into planets is an effective means of terraforming - except when the asteroid is so large that the impact would be cataclysmic. In this case, the asteroid can be towed to a space station, where the water-ice can be safely extracted and shipped to the destination planet. Can only help terraform worlds in the same system.";
        }
        else
        {
            return null;
        }
    }

    public boolean slingInstalledOnStation()
    {
        if(this.market.getPrimaryEntity().hasTag("station") || this.market.getPlanetEntity() == null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int getTerraformingProgressPoints()
    {
        if(!this.isFunctional())
        {
            return 0;
        }
        else if(this.market.getPlanetEntity() != null && !(boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("water") || boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("frozen")))
        {
            return 0;
        }
        else if(this.aiCoreId == null)
        {
            return 8;
        }
        else if(this.aiCoreId.equals("gamma_core"))
        {
            return 8;
        }
        else if(this.aiCoreId.equals("beta_core"))
        {
            return 8;
        }
        else if(this.aiCoreId.equals("alpha_core"))
        {
            return 9;
        }
        else
        {
            return 0;
        }
    }

    @Override
    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Adds %s additional terraforming progress point per day.", 0.0F, highlight, new String[]{(int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION, "1"});
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Adds %s additional terraforming progress point per day.", opad, highlight, new String[]{(int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION, "1"});
        }
    }

    protected void applyAlphaCoreSupplyAndDemandModifiers()
    {
        this.demandReduction.modifyFlat(this.getModId(0), (float)DEMAND_REDUCTION, "Alpha core");
    }

    @Override
    public void apply() {
        super.apply(true);

        this.demand("heavy_machinery", 5);

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

        float machineryDeficitMultiplier = 1.0f;
        Pair<String, Integer> deficitShips = this.getMaxDeficit(new String[]{"heavy_machinery"});
        if(deficitShips.two > 0)
        {
            machineryDeficitMultiplier = machineryDeficitMultiplier + (.20f * deficitShips.two);
            this.getUpkeep().modifyMultAlways("ind_machinery_deficit", machineryDeficitMultiplier, "Heavy machinery shortage");
        }
        else
        {
            this.getUpkeep().unmodifyMult("ind_machinery_deficit");
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
        if(!Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(this.market.getPrimaryEntity().hasTag("station"))
        {
            return true;
        }

        //Make sure we don't throw a null exception if the market entity lacks the station tag but is in fact a station
        if(this.market.getPlanetEntity() == null)
        {
            return false;
        }

        if(boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("water") || boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("frozen"))
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
        if(!Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String getUnavailableReason()
    {
        if(slingInstalledOnStation())
        {
            return "Error in getUnavailableReason() in the Ismara's Sling structure. Please tell Boggled about this on the forums. Found null planet entity when sling in unbuildable.";
        }
        else if(!boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("water") && !boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("frozen"))
        {
            return "Ismara's Sling can only be built on cryovolcanic, frozen and water-covered worlds.";
        }
        else
        {
            return "Error in getUnavailableReason() in the Ismara's Sling structure. Please tell Boggled about this on the forums.";
        }
    }

    public float getPatherInterest() { return 10.0F; }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(slingInstalledOnStation())
        {
            tooltip.addPara("Asteroid Processing contributes %s terraforming progress points per day to other worlds in this system.", opad, highlight, new String[]{"8"});
        }
        else if(boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("water") || boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("frozen"))
        {
            tooltip.addPara("Ismara's Sling contributes %s terraforming progress points per day to other worlds in this system.", opad, highlight, new String[]{"8"});
        }
        else
        {
            tooltip.addPara("Ismara's Sling can only contribute terraforming progress points when it's located on a water, frozen or cryovolcanic planet.", bad, opad);
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

            if(slingInstalledOnStation())
            {
                tooltip.addPara("Asteroid Processing always demands %s heavy machinery regardless of market size.", opad, highlight, new String[]{"5"});
            }
            else
            {
                tooltip.addPara("Ismara's Sling always demands %s heavy machinery regardless of market size.", opad, highlight, new String[]{"5"});
            }
        }
    }
}
