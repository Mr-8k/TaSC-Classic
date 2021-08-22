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

import javax.swing.*;
import java.lang.String;

public class Cloning extends BaseIndustry implements MarketImmigrationModifier
{
    public boolean canBeDisrupted() {
        return true;
    }

    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            this.demand("domain_artifacts", size - 2);
        }
        this.supply("organs", size - 2);

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            this.applyDeficitToProduction(1, deficit, new String[]{"organs"});
        }

        if (!this.isFunctional())
        {
            this.supply.clear();
            this.unapply();
        }
    }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming)
    {
        float bonus = (5 * this.market.getSize());
        incoming.getWeight().modifyFlat(this.getModId(), bonus, this.getNameForModifier());
    }

    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(Global.getSettings().getBoolean("boggledCloningEnabled"))
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
        return 10.0F;
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        //Nothing right now.
    }

    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        if (mode != IndustryTooltipMode.NORMAL || this.isFunctional())
        {
            float opad = 10.0F;
            float pad = 3.0F;
            Color h = Misc.getHighlightColor();

            float bonus = (5 * this.market.getSize());
            tooltip.addPara("Population growth: %s", opad, h, new String[]{"+" + (int)bonus});
        }

    }
}

