
package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CircularFleetOrbit;
import com.fs.starfarer.campaign.CircularOrbit;
import com.fs.starfarer.campaign.CircularOrbitPointDown;
import com.fs.starfarer.campaign.CircularOrbitWithSpin;
import com.fs.starfarer.combat.entities.terrain.Planet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import data.campaign.econ.boggledTools;

public class Boggled_Arcology_World extends BaseHazardCondition
{
    public Boggled_Arcology_World() { }

    public void advance(float amount)
    {
        super.advance(amount);
    }

    /*
    public void apply(String id)
    {
        super.apply(id);

        //eliminate food demand
        Industry industry = market.getIndustry(Industries.POPULATION);
        if(industry!=null)
        {
            industry.getDemand(Commodities.FOOD).getQuantity().modifyMult(id, 0);
        }

        //this.market.getHazard().modifyFlat(id, -.5F, "Arcology");
    }

    public void unapply(String id)
    {
        super.unapply(id);

        market.getIndustry(Industries.POPULATION).getDemand(Commodities.FOOD).getQuantity().unmodifyMult(id);

        //this.market.getHazard().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded)
    {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("Arcologies .", 10f, Misc.getHighlightColor(), "");
    }

     */
}
