package data.campaign.econ.industries;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import java.lang.String;

public class Boggled_Krill_Harvesting extends BaseIndustry
{
    @Override
    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();
        this.demand(0, "heavy_machinery", size - 3, "Base value for colony size");
        this.supply("food", size + 2);

        Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"heavy_machinery"});
        this.applyDeficitToProduction(1, deficit, new String[]{"food"});
        if (!this.isFunctional() || !this.market.hasCondition("boggled_arcology_world"))
        {
            this.supply.clear();
        }

        if(this.market.hasCondition("boggled_arcology_world"))
        {
            this.market.getStability().modifyFlat(this.getModId(), (float)-3, this.getNameForModifier());
        }
    }

    @Override
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

    public float getPatherInterest() {
        return 4.0F + super.getPatherInterest();
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(this.market.hasCondition("boggled_arcology_world"))
        {
            tooltip.addPara("Stability malus: %s", opad, highlight, new String[]{"-3"});
        }
        else
        {
            tooltip.addPara("Krill Harvesting can only function on worlds where most inhabitants live in arcologies.", opad, highlight, new String[]{""});
        }
    }
}

