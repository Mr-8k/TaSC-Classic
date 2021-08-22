package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import data.campaign.econ.industries.*;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

public class TerraformingControlPanelDialog implements InteractionDialogPlugin
{
    protected InteractionDialogAPI dialog;

    @Override
    public void init(InteractionDialogAPI dialog)
    {
        // Save the dialog UI element so that we can write to it outside of this method
        this.dialog = dialog;

        this.optionSelected(null, TerraformingControlPanelDialog.OptionId.INIT);
    }

    private void printCurrentTerraformingStatus(InteractionDialogAPI dialog, StarSystemAPI system, SectorEntityToken targetPlanet, PlanetAPI planet)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color base = Misc.getTextColor();

        String planetType = boggledTools.getPlanetType(planet);
        if(planetType.equals("gas_giant"))
        {
            planetType = "Gas Giant";
        }
        else
        {
            planetType = planetType.substring(0, 1).toUpperCase() + planetType.substring(1);
        }

        dialog.getTextPanel().addPara("Current planet class: %s", base, highlight, new String[]{planetType});

        dialog.getTextPanel().addPara("Terraforming buildings on %s:", base, highlight, new String[]{targetPlanet.getMarket().getName()});

        boolean foundAnyBuilding = false;
        Iterator allStructuresOnMarket = targetPlanet.getMarket().getIndustries().iterator();
        while(allStructuresOnMarket.hasNext())
        {
            Industry industry = (Industry) allStructuresOnMarket.next();
            if (industry instanceof Stellar_Reflector_Array)
            {
                dialog.getTextPanel().addPara("       -Stellar Reflector Array: %s points/day", base, highlight, new String[]{((Stellar_Reflector_Array) industry).getTerraformingProgressPoints() + ""});
                foundAnyBuilding = true;
            }
            else if (industry instanceof Terraforming_Platform)
            {
                dialog.getTextPanel().addPara("       -Terraforming Platform: %s points/day", base, highlight, new String[]{((Terraforming_Platform) industry).getTerraformingProgressPoints() + ""});
                foundAnyBuilding = true;
            }
            else if (industry instanceof Atmosphere_Processor)
            {
                dialog.getTextPanel().addPara("       -Atmosphere Processor: %s points/day", base, highlight, new String[]{((Atmosphere_Processor) industry).getTerraformingProgressPoints() + ""});
                foundAnyBuilding = true;
            }
            else if (industry instanceof Eisen_Division)
            {
                dialog.getTextPanel().addPara("       -Eisen Division: %s points/day", base, highlight, new String[]{((Eisen_Division) industry).getTerraformingProgressPoints() + ""});
                foundAnyBuilding = true;
            }
        }

        if(targetPlanet.getMarket().hasCondition("US_crash"))
        {
            dialog.getTextPanel().addPara("       -Crashed terraforming drone: %s points/day", base, highlight, new String[]{4 + ""});
            foundAnyBuilding = true;
        }

        if(!foundAnyBuilding)
        {
            dialog.getTextPanel().addPara("       (none)", Misc.getGrayColor(), highlight, new String[]{});
        }

        int contributionFromSlings = 0;
        Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(system).iterator();
        while(allMarketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)allMarketsInSystem.next();
            if(marketElement.isPlayerOwned() && !marketElement.equals(targetPlanet.getMarket()) && marketElement.hasIndustry("ISMARA_SLING") && marketElement.getIndustry("ISMARA_SLING").isFunctional())
            {
                contributionFromSlings = contributionFromSlings + ((Ismara_Sling)marketElement.getIndustry("ISMARA_SLING")).getTerraformingProgressPoints();
            }
        }

        dialog.getTextPanel().addPara("Contribution from Ismara's Slings and Asteroid Breaking in-system: %s", base, highlight, new String[]{contributionFromSlings + ""});

        dialog.getTextPanel().addPara("Total points/day: %s", base, highlight, new String[]{boggledTools.getTotalTerraformingPoints(targetPlanet.getMarket()) + ""});

        printTerraformingProject(dialog, system, targetPlanet, planet);
    }

    private void printTerraformingProject(InteractionDialogAPI dialog, StarSystemAPI system, SectorEntityToken targetPlanet, PlanetAPI planet)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color base = Misc.getTextColor();

        if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
        {
            String projectType = boggledTools.getTerraformingProjectTypeOngoing(targetPlanet.getMarket());
            if(projectType != null)
            {
                String projectTypeHumanReadable = projectType.replaceAll("_", " ");
                projectTypeHumanReadable = projectTypeHumanReadable.substring(0, 1).toUpperCase() + projectTypeHumanReadable.substring(1);
                dialog.getTextPanel().addPara("Current project: %s", base, highlight, new String[]{projectTypeHumanReadable});

                dialog.getTextPanel().addPara("Project progress: %s/%s", base, base, new String[]{boggledTools.getTerraformingProjectProgressPoints(targetPlanet.getMarket()) + "", boggledTools.getTerraformingProjectPointRequirement(targetPlanet.getMarket(), projectType) + ""});
            }
            else
            {
                dialog.getTextPanel().addPara("Error with null project type. Tell Boggled about this on the forums.");
            }
        }
        else
        {
            dialog.getTextPanel().addPara("Current project: %s", base, Misc.getGrayColor(), new String[]{"(none)"});
        }
    }

    private void printProjectPointRequirements(String project, InteractionDialogAPI dialog, StarSystemAPI system, SectorEntityToken targetPlanet, PlanetAPI planet)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color base = Misc.getTextColor();
        MarketAPI market = targetPlanet.getMarket();

        String projectTypeHumanReadable = project.replaceAll("_", " ");
        projectTypeHumanReadable = projectTypeHumanReadable.substring(0, 1).toUpperCase() + projectTypeHumanReadable.substring(1);

        dialog.getTextPanel().addPara("Prospective project: %s", base, highlight, new String[]{projectTypeHumanReadable});

        dialog.getTextPanel().addPara("Project point cost: %s", base, highlight, new String[]{boggledTools.getTerraformingProjectPointRequirement(targetPlanet.getMarket(), project) + ""});

        if(project.contains("transformation"))
        {
            dialog.getTextPanel().addPara("       -Base point cost: %s", base, bad, new String[]{Global.getSettings().getInt("boggledBaseTerraformingPoints") + ""});

            // Global points offsets
            if(market.hasCondition("dense_atmosphere"))
            {
                dialog.getTextPanel().addPara("       -Dense Atmosphere: %s", base, bad, new String[]{Global.getSettings().getInt("boggledExtraPointsForDenseAtmo") + ""});
            }
            else if(market.hasCondition("thin_atmosphere"))
            {
                dialog.getTextPanel().addPara("       -Thin Atmosphere: %s", base, bad, new String[]{Global.getSettings().getInt("boggledExtraPointsForThinAtmo") + ""});
            }
            else if(market.hasCondition("no_atmosphere"))
            {
                dialog.getTextPanel().addPara("       -No Atmosphere: %s", base, bad, new String[]{Global.getSettings().getInt("boggledExtraPointsForNoAtmo") + ""});
            }

            if(market.hasCondition("toxic_atmosphere"))
            {
                dialog.getTextPanel().addPara("       -Toxic Atmosphere: %s", base, bad, new String[]{Global.getSettings().getInt("boggledExtraPointsForToxicAtmo") + ""});
            }

            if(market.hasCondition("irradiated"))
            {
                dialog.getTextPanel().addPara("       -Irradiated: %s", base, bad, new String[]{Global.getSettings().getInt("boggledExtraPointsForRadiation") + ""});
            }

            if(market.hasCondition("habitable"))
            {
                dialog.getTextPanel().addPara("       -Habitable: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledPointReductionForHabitable") + ""});
            }

            if(project.equals("arcology_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }

                dialog.getTextPanel().addPara("       -Arcology difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForArcology") + "%"});

                if(boggledTools.getPlanetType(planet).equals("arcology"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("terran_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }

                dialog.getTextPanel().addPara("       -Terran difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForTerran") + "%"});

                if(boggledTools.getPlanetType(planet).equals("terran"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("terran_eccentric_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }

                dialog.getTextPanel().addPara("       -Terran Eccentric difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForTerran") + "%"});

                if(boggledTools.getPlanetType(planet).equals("terran"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("auric_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }

                dialog.getTextPanel().addPara("       -Auric difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForTerran") + "%"});

                if(boggledTools.getPlanetType(planet).equals("terran"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("archipelago_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }

                dialog.getTextPanel().addPara("       -Archipelago difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForTerran") + "%"});

                if(boggledTools.getPlanetType(planet).equals("terran"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("continental_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }

                dialog.getTextPanel().addPara("       -Continental difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForTerran") + "%"});

                if(boggledTools.getPlanetType(planet).equals("terran"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("water_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }

                dialog.getTextPanel().addPara("       -Water difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForWater") + "%"});

                if(boggledTools.getPlanetType(planet).equals("water"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("jungle_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(1 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot"))
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else if(market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(1 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }

                dialog.getTextPanel().addPara("       -Jungle difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForJungle") + "%"});

                if(boggledTools.getPlanetType(planet).equals("jungle"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("arid_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(1 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot"))
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else if(market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(1 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }

                dialog.getTextPanel().addPara("       -Arid difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForArid") + "%"});

                if(boggledTools.getPlanetType(planet).equals("desert"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("tundra_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else if(market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(1 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(1 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }

                dialog.getTextPanel().addPara("       -Tundra difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForTundra") + "%"});

                if(boggledTools.getPlanetType(planet).equals("tundra"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("frozen_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(4 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(1 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }

                dialog.getTextPanel().addPara("       -Frozen difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForFrozen") + "%"});

                if(boggledTools.getPlanetType(planet).equals("frozen"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("toxic_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }

                dialog.getTextPanel().addPara("       -Toxic difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForToxic") + "%"});

                if(boggledTools.getPlanetType(planet).equals("toxic"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }
            else if(project.equals("volcanic_transformation"))
            {
                if(market.hasCondition("very_cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(4 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("cold"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("hot"))
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(1 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }
                else if(market.hasCondition("very_hot"))
                {
                    dialog.getTextPanel().addPara("       -Ideal Temperature: %s", base, Misc.getPositiveHighlightColor(), new String[]{Global.getSettings().getInt("boggledTemperaturePointMagnitude") + ""});
                }
                else
                {
                    dialog.getTextPanel().addPara("       -Temperature Differential: %s", base, bad, new String[]{(2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude")) + ""});
                }

                dialog.getTextPanel().addPara("       -Volcanic difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForVolcanic") + "%"});

                if(boggledTools.getPlanetType(planet).equals("volcanic"))
                {
                    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
                }
            }

            //Doesn't work for all planet types, especially since the additional non-default types were added
            //if(boggledTools.getPlanetType(planet).equals(project.replaceAll("_transformation", "")))
            //{
            //    dialog.getTextPanel().addPara("       -Same-type difficulty multiplier: %s", base, highlight, new String[]{Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation") + "%"});
            //}
        }
        else
        {
            dialog.getTextPanel().addPara("       -Base point cost: %s", base, bad, new String[]{Global.getSettings().getInt("boggledBaseConditionModificationPoints") + ""});
        }
    }

    private void printTransformationResourcesChange(String project, InteractionDialogAPI dialog, StarSystemAPI system, SectorEntityToken targetPlanet, PlanetAPI planet)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color base = Misc.getTextColor();
        MarketAPI market = targetPlanet.getMarket();

        String projectTypeHumanReadable = project.replaceAll("_", " ");
        projectTypeHumanReadable = projectTypeHumanReadable.substring(0, 1).toUpperCase() + projectTypeHumanReadable.substring(1);

        if(projectTypeHumanReadable.contains("transformation"))
        {
            dialog.getTextPanel().addPara("Projected resources on planet after project completion:", base, bad, new String[]{""});

            if(projectTypeHumanReadable.contains("Arcology") || projectTypeHumanReadable.contains("Volcanic"))
            {
                dialog.getTextPanel().addPara("       -No farmland", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -No organics", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -No volatiles", base, bad, new String[]{""});
            }
            else if(projectTypeHumanReadable.contains("Terran") || projectTypeHumanReadable.contains("Auric") || projectTypeHumanReadable.contains("Archipelago") || projectTypeHumanReadable.contains("Continental"))
            {
                dialog.getTextPanel().addPara("       -Bountiful farmland", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -Plentiful organics", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -Trace volatiles", base, bad, new String[]{""});
            }
            else if(projectTypeHumanReadable.contains("Water"))
            {
                dialog.getTextPanel().addPara("       -Abundant organics", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -No volatiles", base, bad, new String[]{""});
            }
            else if(projectTypeHumanReadable.contains("Jungle"))
            {
                dialog.getTextPanel().addPara("       -Rich farmland", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -Abundant organics", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -No volatiles", base, bad, new String[]{""});
            }
            else if(projectTypeHumanReadable.contains("Arid"))
            {
                dialog.getTextPanel().addPara("       -Poor farmland", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -No organics", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -No volatiles", base, bad, new String[]{""});
            }
            else if(projectTypeHumanReadable.contains("Tundra"))
            {
                dialog.getTextPanel().addPara("       -Adequate farmland", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -Common organics", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -Abundant volatiles", base, bad, new String[]{""});
            }
            else if(projectTypeHumanReadable.contains("Frozen"))
            {
                dialog.getTextPanel().addPara("       -No farmland", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -No organics", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -Plentiful volatiles", base, bad, new String[]{""});
            }
            else if(projectTypeHumanReadable.contains("Toxic"))
            {
                dialog.getTextPanel().addPara("       -No farmland", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -Common organics", base, bad, new String[]{""});
                dialog.getTextPanel().addPara("       -Diffuse volatiles", base, bad, new String[]{""});
            }

            dialog.getTextPanel().addPara("Ore deposits are never modified by terraforming.", base, bad, new String[]{""});
        }
        else
        {
            dialog.getTextPanel().addPara("Error in printTransformationResourcesChange. Please tell Boggled about this on the forums.", base, bad, new String[]{""});
        }
    }

    @Override
    public void optionSelected(String optionText, Object optionData)
    {
        if(optionData instanceof OptionId)
        {
            // Clear shown options before we show new ones
            dialog.getOptionPanel().clearOptions();

            SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
            StarSystemAPI system = playerFleet.getStarSystem();
            SectorEntityToken targetPlanet = boggledTools.getClosestValidPlanetSectorEntityTokenInSystem(system);
            PlanetAPI planet = (PlanetAPI) targetPlanet;

            boggledTools.addCondition(targetPlanet.getMarket(), "terraforming_manager");

            switch ((OptionId) optionData)
            {
                case INIT:
                    dialog.getVisualPanel().showPlanetInfo(targetPlanet);
                    printCurrentTerraformingStatus(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case ENUM_TYPE_CHANGE_OPTIONS:
                    if(Global.getSettings().getBoolean("boggledArcologyWorldTransformationEnabled"))
                    {
                        dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into an arcology planet", OptionId.TERRAFORM_ARCOLOGY);
                        dialog.getOptionPanel().setTooltip(OptionId.TERRAFORM_ARCOLOGY, "Arcologies have no farmlands, organics or volatiles, but can host powerful infrastructure buildings that can only be constructed on this world type.");
                    }
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a Terran planet", OptionId.TERRAFORM_TERRAN);
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a water planet", OptionId.TERRAFORM_WATER);
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a jungle planet", OptionId.TERRAFORM_JUNGLE);
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into an arid planet", OptionId.TERRAFORM_ARID);
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a tundra planet", OptionId.TERRAFORM_TUNDRA);
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a frozen planet", OptionId.TERRAFORM_FROZEN);
                    if(Global.getSettings().getBoolean("boggledExtraPlanetTypeTransformationsEnabled"))
                    {
                        dialog.getOptionPanel().addOption("More options", OptionId.ENUM_TYPE_CHANGE_OPTIONS_2);
                    }
                    if(targetPlanet.getMarket().hasCondition("dark"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.TERRAFORM_TERRAN, false);
                        dialog.getOptionPanel().setEnabled(OptionId.TERRAFORM_WATER, false);
                        dialog.getOptionPanel().setEnabled(OptionId.TERRAFORM_JUNGLE, false);
                        dialog.getOptionPanel().setEnabled(OptionId.TERRAFORM_ARID, false);
                        dialog.getOptionPanel().setEnabled(OptionId.TERRAFORM_TUNDRA, false);
                    }
                    dialog.getOptionPanel().addOption("Back", OptionId.BACK_TO_MAIN_MENU);
                    break;
                case ENUM_TYPE_CHANGE_OPTIONS_2:
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a Terran Eccentric planet", OptionId.TERRAFORM_TERRAN_ECCENTRIC);
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a toxic planet", OptionId.TERRAFORM_TOXIC);
                    dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a volcanic planet", OptionId.TERRAFORM_VOLCANIC);
                    if(Global.getSettings().getModManager().isModEnabled("US"))
                    {
                        dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into an auric planet", OptionId.TERRAFORM_AURIC);
                        dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into an archipelago planet", OptionId.TERRAFORM_ARCHIPELAGO);
                        dialog.getOptionPanel().addOption("Terraform " + targetPlanet.getMarket().getName() + " into a continental planet", OptionId.TERRAFORM_CONTINENTAL);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case BACK_TO_MAIN_MENU:
                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_ARCOLOGY:
                    printProjectPointRequirements("arcology_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("arcology_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard arcology world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard arcology world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_ARCOLOGY_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_ARCOLOGY_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "arcology_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_TERRAN:
                    printProjectPointRequirements("terran_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("terran_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard Terran world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard Terran world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_TERRAN_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_TERRAN_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "terran_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_TERRAN_ECCENTRIC:
                    printProjectPointRequirements("terran_eccentric_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("terran_eccentric_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard Terran Eccentric world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard Terran Eccentric world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_TERRAN_ECCENTRIC_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_TERRAN_ECCENTRIC_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "terran_eccentric_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_TOXIC:
                    printProjectPointRequirements("toxic_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("toxic_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard toxic world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard toxic world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_TOXIC_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_TOXIC_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "toxic_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_VOLCANIC:
                    printProjectPointRequirements("volcanic_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("volcanic_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard volcanic world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard volcanic world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_VOLCANIC_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_VOLCANIC_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "volcanic_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_AURIC:
                    printProjectPointRequirements("auric_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("auric_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard auric world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard auric world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_AURIC_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_AURIC_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "auric_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_ARCHIPELAGO:
                    printProjectPointRequirements("archipelago_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("archipelago_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard archipelago world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard archipelago world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_ARCHIPELAGO_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_ARCHIPELAGO_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "archipelago_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_CONTINENTAL:
                    printProjectPointRequirements("continental_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("continental_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard continental world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard continental world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_CONTINENTAL_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_CONTINENTAL_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "continental_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_WATER:
                    printProjectPointRequirements("water_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("water_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard water world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard water world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_WATER_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_WATER_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "water_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_JUNGLE:
                    printProjectPointRequirements("jungle_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("jungle_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard jungle world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard jungle world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_JUNGLE_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_JUNGLE_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "jungle_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_ARID:
                    printProjectPointRequirements("arid_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("arid_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard arid world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard arid world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_ARID_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_ARID_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "arid_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_TUNDRA:
                    printProjectPointRequirements("tundra_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("tundra_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard tundra world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard tundra world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_TUNDRA_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_TUNDRA_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "tundra_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case TERRAFORM_FROZEN:
                    printProjectPointRequirements("frozen_transformation", dialog, system, targetPlanet, planet);
                    printTransformationResourcesChange("frozen_transformation", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard frozen world? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin transforming " + targetPlanet.getMarket().getName() + " into a standard frozen world?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.TERRAFORM_FROZEN_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    break;
                case TERRAFORM_FROZEN_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "frozen_transformation");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case ENUM_CONDITIONS_OPTIONS:
                    dialog.getOptionPanel().addOption("Make climate mild", OptionId.CONDITIONS_MILD_CLIMATE);
                    if(targetPlanet.getMarket().hasCondition("mild_climate") || targetPlanet.getMarket().hasCondition("dark") || targetPlanet.getMarket().hasCondition("extreme_weather") || !(boggledTools.getPlanetType(planet).equals("terran") || boggledTools.getPlanetType(planet).equals("water") || boggledTools.getPlanetType(planet).equals("desert") || boggledTools.getPlanetType(planet).equals("jungle") || boggledTools.getPlanetType(planet).equals("tundra")))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.CONDITIONS_MILD_CLIMATE, false);
                    }
                    dialog.getOptionPanel().addOption("Clean up pollution", OptionId.CONDITIONS_POLLUTION);
                    if(!targetPlanet.getMarket().hasCondition("pollution"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.CONDITIONS_POLLUTION, false);
                    }
                    dialog.getOptionPanel().addOption("Build meteor deflection infrastructure", OptionId.CONDITIONS_METEOR_IMPACTS);
                    if(!targetPlanet.getMarket().hasCondition("meteor_impacts"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.CONDITIONS_METEOR_IMPACTS, false);
                    }
                    dialog.getOptionPanel().addOption("Organize a planet cracking operation", OptionId.CONDITIONS_PLANET_CRACKING);
                    dialog.getOptionPanel().setTooltip(OptionId.CONDITIONS_PLANET_CRACKING, "Planet cracking will improve ore and rare ore resources by one level. If ore or rare ore resources do not exist, they will be added at the sparse level.\n\nGives market the tectonic activity condition permanently.\n\nCannot be initiated if ore resources are already at the highest level, the market already has tectonic activity or the market is a gas giant.");
                    if(boggledTools.getPlanetType(targetPlanet.getMarket().getPlanetEntity()).equals("gas_giant") || targetPlanet.getMarket().hasCondition("tectonic_activity") || targetPlanet.getMarket().hasCondition("extreme_tectonic_activity") || (targetPlanet.getMarket().hasCondition("ore_ultrarich") && targetPlanet.getMarket().hasCondition("rare_ore_ultrarich")))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.CONDITIONS_PLANET_CRACKING, false);
                    }
                    dialog.getOptionPanel().addOption("Ouyang optimization", OptionId.CONDITIONS_OUYANG_OPTIMIZATION);
                    dialog.getOptionPanel().setTooltip(OptionId.CONDITIONS_OUYANG_OPTIMIZATION, "Ouyang optimization will improve the volatiles resource on a gas giant by two levels. If the volatile resource does not exist, it will be added at the diffuse level.\n\nGives market the extreme weather condition permanently.\n\nCannot be initiated if the volatiles resource is already at the highest level, the market already has extreme weather or the market is not a gas giant.");
                    if(!boggledTools.getPlanetType(targetPlanet.getMarket().getPlanetEntity()).equals("gas_giant") || targetPlanet.getMarket().hasCondition("extreme_weather") || targetPlanet.getMarket().hasCondition("volatiles_plentiful"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.CONDITIONS_OUYANG_OPTIMIZATION, false);
                    }

                    if(Global.getSettings().getBoolean("boggledStabilizeTectonicsEnabled"))
                    {
                        dialog.getOptionPanel().addOption("Stabilize tectonic activity", OptionId.CONDITIONS_TECTONICS);
                        if(!(targetPlanet.getMarket().hasCondition("tectonic_activity") || targetPlanet.getMarket().hasCondition("extreme_tectonic_activity")))
                        {
                            dialog.getOptionPanel().setEnabled(OptionId.CONDITIONS_TECTONICS, false);
                        }
                    }

                    if(Global.getSettings().getBoolean("boggledGravitationalManipulatorEnabled"))
                    {
                        dialog.getOptionPanel().addOption("Set up gravitational manipulation field", OptionId.CONDITIONS_GRAVITY);
                        if(!(targetPlanet.getMarket().hasCondition("low_gravity") || targetPlanet.getMarket().hasCondition("high_gravity")))
                        {
                            dialog.getOptionPanel().setEnabled(OptionId.CONDITIONS_GRAVITY, false);
                        }
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.BACK_TO_MAIN_MENU);
                    break;
                case CONDITIONS_MILD_CLIMATE:
                    printProjectPointRequirements("modify_climate", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin modifying the climate on " + targetPlanet.getMarket().getName() + "? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin modifying the climate on " + targetPlanet.getMarket().getName() + "?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.CONDITIONS_MILD_CLIMATE_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_CONDITIONS_OPTIONS);
                    break;
                case CONDITIONS_MILD_CLIMATE_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "modify_climate");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case CONDITIONS_POLLUTION:
                    printProjectPointRequirements("remediate_pollution", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin remediating the pollution on " + targetPlanet.getMarket().getName() + "? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin remediating the pollution on " + targetPlanet.getMarket().getName() + "?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.CONDITIONS_POLLUTION_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_CONDITIONS_OPTIONS);
                    break;
                case CONDITIONS_POLLUTION_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "remediate_pollution");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case CONDITIONS_METEOR_IMPACTS:
                    printProjectPointRequirements("establish_meteor_defense", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin setting up a meteor deflection system on " + targetPlanet.getMarket().getName() + "? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin setting up a meteor deflection system on " + targetPlanet.getMarket().getName() + "?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.CONDITIONS_METEOR_IMPACTS_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_CONDITIONS_OPTIONS);
                    break;
                case CONDITIONS_METEOR_IMPACTS_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "establish_meteor_defense");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case CONDITIONS_PLANET_CRACKING:
                    printProjectPointRequirements("planet_cracking", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin a planet cracking operation on " + targetPlanet.getMarket().getName() + "? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin a planet cracking operation on " + targetPlanet.getMarket().getName() + "?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.CONDITIONS_PLANET_CRACKING_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_CONDITIONS_OPTIONS);
                    break;
                case CONDITIONS_PLANET_CRACKING_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "planet_cracking");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case CONDITIONS_OUYANG_OPTIMIZATION:
                    printProjectPointRequirements("ouyang_optimization", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin Ouyang optimization on " + targetPlanet.getMarket().getName() + "? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin Ouyang optimization on " + targetPlanet.getMarket().getName() + "?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.CONDITIONS_OUYANG_OPTIMIZATION_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_CONDITIONS_OPTIONS);
                    break;
                case CONDITIONS_OUYANG_OPTIMIZATION_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "ouyang_optimization");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case CONDITIONS_TECTONICS:
                    printProjectPointRequirements("stabilize_tectonics", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin stabilizing the tectonic activity on " + targetPlanet.getMarket().getName() + "? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin stabilizing the tectonic activity on " + targetPlanet.getMarket().getName() + "?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.CONDITIONS_TECTONICS_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_CONDITIONS_OPTIONS);
                    break;
                case CONDITIONS_TECTONICS_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "stabilize_tectonics");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case CONDITIONS_GRAVITY:
                    printProjectPointRequirements("gravitational_manipulator", dialog, system, targetPlanet, planet);

                    if(boggledTools.terraformingProjectAlreadyOngoing(targetPlanet.getMarket()))
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin setting up a gravitational manipulation field on " + targetPlanet.getMarket().getName() + "? You will lose all progress on your current project.");
                    }
                    else
                    {
                        dialog.getTextPanel().addPara("Are you sure you want to begin setting up a gravitational manipulation field on " + targetPlanet.getMarket().getName() + "?");
                    }
                    dialog.getOptionPanel().addOption("Initiate project", OptionId.CONDITIONS_GRAVITY_INITIATE);
                    dialog.getOptionPanel().addOption("Back", OptionId.ENUM_CONDITIONS_OPTIONS);
                    break;
                case CONDITIONS_GRAVITY_INITIATE:
                    boggledTools.initiateTerraformingProject(targetPlanet.getMarket(), "gravitational_manipulator");
                    printTerraformingProject(dialog, system, targetPlanet, planet);

                    dialog.getOptionPanel().addOption("Change planet type", OptionId.ENUM_TYPE_CHANGE_OPTIONS);
                    if(boggledTools.getPlanetType(planet).equals("gas_giant") && !Global.getSettings().getBoolean("boggledAllowGasGiantTerraforming"))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ENUM_TYPE_CHANGE_OPTIONS, false);
                    }
                    dialog.getOptionPanel().addOption("Improve conditions", OptionId.ENUM_CONDITIONS_OPTIONS);
                    dialog.getOptionPanel().addOption("Exit", OptionId.DISMISS_DIALOG);
                    break;
                case DISMISS_DIALOG:
                    dialog.dismiss();
                    break;
            }
        }
    }

    enum OptionId
    {
        INIT,
        ENUM_TYPE_CHANGE_OPTIONS,
        ENUM_TYPE_CHANGE_OPTIONS_2,
        ENUM_CONDITIONS_OPTIONS,
        BACK_TO_MAIN_MENU,
        TERRAFORM_ARCOLOGY,
        TERRAFORM_ARCOLOGY_INITIATE,
        TERRAFORM_TERRAN,
        TERRAFORM_TERRAN_INITIATE,
        TERRAFORM_TERRAN_ECCENTRIC,
        TERRAFORM_TERRAN_ECCENTRIC_INITIATE,
        TERRAFORM_TOXIC,
        TERRAFORM_TOXIC_INITIATE,
        TERRAFORM_VOLCANIC,
        TERRAFORM_VOLCANIC_INITIATE,
        TERRAFORM_AURIC,
        TERRAFORM_AURIC_INITIATE,
        TERRAFORM_ARCHIPELAGO,
        TERRAFORM_ARCHIPELAGO_INITIATE,
        TERRAFORM_CONTINENTAL,
        TERRAFORM_CONTINENTAL_INITIATE,
        TERRAFORM_WATER,
        TERRAFORM_WATER_INITIATE,
        TERRAFORM_JUNGLE,
        TERRAFORM_JUNGLE_INITIATE,
        TERRAFORM_ARID,
        TERRAFORM_ARID_INITIATE,
        TERRAFORM_TUNDRA,
        TERRAFORM_TUNDRA_INITIATE,
        TERRAFORM_FROZEN,
        TERRAFORM_FROZEN_INITIATE,
        CONDITIONS_MILD_CLIMATE,
        CONDITIONS_MILD_CLIMATE_INITIATE,
        CONDITIONS_POLLUTION,
        CONDITIONS_POLLUTION_INITIATE,
        CONDITIONS_METEOR_IMPACTS,
        CONDITIONS_METEOR_IMPACTS_INITIATE,
        CONDITIONS_PLANET_CRACKING,
        CONDITIONS_PLANET_CRACKING_INITIATE,
        CONDITIONS_OUYANG_OPTIMIZATION,
        CONDITIONS_OUYANG_OPTIMIZATION_INITIATE,
        CONDITIONS_TECTONICS,
        CONDITIONS_TECTONICS_INITIATE,
        CONDITIONS_GRAVITY,
        CONDITIONS_GRAVITY_INITIATE,
        DISMISS_DIALOG,
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData)
    {
        // Can add things like hints for what the option does here
    }

    @Override
    public void advance(float amount)
    {

    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult)
    {

    }

    @Override
    public Object getContext()
    {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap()
    {
        return null;
    }
}