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
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;

import javax.swing.*;

public class Domain_Archaeology extends BaseIndustry
{
    public boolean canBeDisrupted() {
        return true;
    }

    public void apply()
    {
        super.apply(true);

        MarketAPI market = this.market;
        int ruinsSize = 0;
        if(market.hasCondition(Conditions.RUINS_SCATTERED))
        {
            ruinsSize = 1;
        }
        else if(market.hasCondition(Conditions.RUINS_WIDESPREAD))
        {
            ruinsSize = 2;
        }
        else if(market.hasCondition(Conditions.RUINS_EXTENSIVE))
        {
            ruinsSize = 4;
        }
        else if(market.hasCondition(Conditions.RUINS_VAST))
        {
            ruinsSize = 8;
        }

        this.supply("domain_artifacts", ruinsSize);

        if (!this.isFunctional())
        {
            this.supply.clear();
        }
    }

    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        MarketAPI market = this.market;
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled") && (market.hasCondition(Conditions.RUINS_SCATTERED) || market.hasCondition(Conditions.RUINS_WIDESPREAD) || market.hasCondition(Conditions.RUINS_EXTENSIVE) || market.hasCondition(Conditions.RUINS_VAST)))
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
        if(!Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
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
        if(!(market.hasCondition(Conditions.RUINS_SCATTERED) || market.hasCondition(Conditions.RUINS_WIDESPREAD) || market.hasCondition(Conditions.RUINS_EXTENSIVE) || market.hasCondition(Conditions.RUINS_VAST)))
        {
            return ("Requires ruins");
        }
        else
        {
            return "Error in getUnavailableReason() in the domain archaeology structure. Please tell Boggled about this on the forums.";
        }
    }

    public float getPatherInterest()
    {
        float base = 1.0F;
        if (this.market.hasCondition("ruins_vast")) {
            base = 8.0F;
        } else if (this.market.hasCondition("ruins_extensive")) {
            base = 6.0F;
        } else if (this.market.hasCondition("ruins_widespread")) {
            base = 4.0F;
        } else if (this.market.hasCondition("ruins_scattered")) {
            base = 2.0F;
        }

        return base + super.getPatherInterest();
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        //Nothing right now.
    }

    protected void addPostSupplySection(TooltipMakerAPI tooltip, boolean hasSupply, IndustryTooltipMode mode)
    {
        if (mode != IndustryTooltipMode.NORMAL || this.isFunctional())
        {
            float opad = 10.0F;
            Color highlight = Misc.getHighlightColor();
            Color bad = Misc.getNegativeHighlightColor();

            MarketAPI market = this.market;
            if(market.hasCondition(Conditions.RUINS_SCATTERED))
            {
                tooltip.addPara("Domain-era artifact supply is determined by the size of the ruins being excavated. Market size has no impact but administrator skills and AI cores do.\n\nBase production from scattered ruins: %s", opad, highlight, new String[]{"1"});
            }
            else if(market.hasCondition(Conditions.RUINS_WIDESPREAD))
            {
                tooltip.addPara("Domain-era artifact supply is determined by the size of the ruins being excavated. Market size has no impact but administrator skills and AI cores do.\n\nBase production from widespread ruins: %s", opad, highlight, new String[]{"2"});
            }
            else if(market.hasCondition(Conditions.RUINS_EXTENSIVE))
            {
                tooltip.addPara("Domain-era artifact supply is determined by the size of the ruins being excavated. Market size has no impact but administrator skills and AI cores do.\n\nBase production from extensive ruins: %s", opad, highlight, new String[]{"4"});
            }
            else if(market.hasCondition(Conditions.RUINS_VAST))
            {
                tooltip.addPara("Domain-era artifact supply is determined by the size of the ruins being excavated. Market size has no impact but administrator skills and AI cores do.\n\nBase production from vast ruins: %s", opad, highlight, new String[]{"8"});
            }
            else
            {
                tooltip.addPara("Domain-era artifact supply is determined by the size of the ruins being excavated. Market size has no impact but administrator skills and AI cores do.", opad, highlight, new String[]{""});
            }
        }
    }
}

