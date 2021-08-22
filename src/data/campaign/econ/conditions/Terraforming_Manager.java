
package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
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

public class Terraforming_Manager extends BaseHazardCondition
{
    public Terraforming_Manager() { }

    public void advance(float amount)
    {
        super.advance(amount);

        MarketAPI market = this.market;
        CampaignClockAPI clock = Global.getSector().getClock();

        // Remove condition if the market is no longer a valid terraforming target
        if(!market.isPlayerOwned())
        {
            market.removeCondition("terraforming_manager");
            return;
        }

        // Reload day check
        int lastDayChecked = boggledTools.getLastDayChecked(market);

        // Exit if a day has not passed
        if(clock.getDay() == lastDayChecked)
        {
            return;
        }
        else
        {
            SectorEntityToken entity = market.getPrimaryEntity();
            PlanetAPI planet = market.getPlanetEntity();

            boggledTools.refreshAquacultureAndFarming(market);

            // Reload variables from persistent market tags and remove the tags
            String project = boggledTools.getTerraformingProjectTypeOngoing(market);
            if(project == null)
            {
                return;
            }
            int progress = boggledTools.getTerraformingProjectProgressPoints(market);

            int additionalPoints = boggledTools.getTotalTerraformingPoints(market);
            boggledTools.incrementTerraformingProjectProgressPoints(market, additionalPoints);

            int pointsRequirement = boggledTools.getTerraformingProjectPointRequirement(market, project);
            if(progress + additionalPoints >= pointsRequirement)
            {
                if(project.contains("transformation"))
                {
                    boggledTools.terraformVariantToVariant(market, project.replaceAll("_transformation", ""));
                    boggledTools.clearBothTerraformingTags(market);
                }
                else
                {
                    if(project.equals("modify_climate"))
                    {
                        boggledTools.addCondition(market, "mild_climate");

                        if (market.isPlayerOwned())
                        {
                            MessageIntel intel = new MessageIntel("Climate modification on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                    else if(project.equals("remediate_pollution"))
                    {
                        boggledTools.removeCondition(market, "pollution");

                        if (market.isPlayerOwned())
                        {
                            MessageIntel intel = new MessageIntel("Pollution remediation on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                    else if(project.equals("establish_meteor_defense"))
                    {
                        boggledTools.removeCondition(market, "meteor_impacts");

                        if (market.isPlayerOwned())
                        {
                            MessageIntel intel = new MessageIntel("Meteor defense infrastructure on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                    else if(project.equals("planet_cracking"))
                    {
                        boggledTools.addCondition(market, "tectonic_activity");

                        boggledTools.incrementOreForPlanetCracking(market);

                        if (market.isPlayerOwned())
                        {
                            MessageIntel intel = new MessageIntel("Planet cracking on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                    else if(project.equals("ouyang_optimization"))
                    {
                        boggledTools.addCondition(market, "extreme_weather");

                        boggledTools.incrementVolatilesForOuyangOptimization(market);

                        if (market.isPlayerOwned())
                        {
                            MessageIntel intel = new MessageIntel("Ouyang optimization on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                    else if(project.equals("stabilize_tectonics"))
                    {
                        boggledTools.removeCondition(market, "tectonic_activity");
                        boggledTools.removeCondition(market, "extreme_tectonic_activity");

                        if (market.isPlayerOwned())
                        {
                            MessageIntel intel = new MessageIntel("Tectonic stabilization on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                    else if(project.equals("gravitational_manipulator"))
                    {
                        boggledTools.removeCondition(market, "low_gravity");
                        boggledTools.removeCondition(market, "high_gravity");

                        if (market.isPlayerOwned())
                        {
                            MessageIntel intel = new MessageIntel("Gravity manipulation on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                    boggledTools.clearBothTerraformingTags(market);
                }

                boggledTools.surveyAll(market);
                boggledTools.refreshSupplyAndDemand(market);
                boggledTools.refreshAquacultureAndFarming(market);
            }

            boggledTools.clearClockCheckTags(market);
            market.addTag("boggled_terraforming_progress_lastDayChecked_" + clock.getDay());
        }
    }

    public void apply(String id) { super.apply(id); }

    public void unapply(String id) { super.unapply(id); }

    public Map<String, String> getTokenReplacements() { return super.getTokenReplacements(); }

    public boolean showIcon() { return false; }
}
