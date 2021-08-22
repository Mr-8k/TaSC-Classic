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

import javax.swing.*;

public class Spice_Harvesting extends BaseIndustry
{
    public boolean canBeDisrupted() {
        return true;
    }

    public void apply()
    {
        super.apply(true);

        if (!this.isFunctional())
        {
            this.supply.clear();
            this.unapply();
        }
    }

    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(Global.getSettings().getBoolean("boggledSpiceHarvestingEnabled") && this.market.getPlanetEntity() != null && boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("desert") && (this.market.hasCondition("hot") || this.market.hasCondition("very_hot")))
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
        if(!Global.getSettings().getBoolean("boggledSpiceHarvestingEnabled") || this.market.getPlanetEntity() == null)
        {
            return false;
        }
        else if(!boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("desert") || !(this.market.hasCondition("hot") || this.market.hasCondition("very_hot")))
        {
            return true;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String getUnavailableReason()
    {
        if(!boggledTools.getPlanetType(this.market.getPlanetEntity()).equals("desert"))
        {
            return "Spice worms can only thrive on desert worlds.";
        }
        else if(!(this.market.hasCondition("hot") || this.market.hasCondition("very_hot")))
        {
            return "Spice worms can only thrive on hot or very hot worlds.";
        }
        else
        {
            return "Error in getUnavailableReason in Spice Harvesting. Please tell boggled about this on the forums.";
        }
    }

    public float getPatherInterest() {
        return 2.0F + super.getPatherInterest();
    }
}

