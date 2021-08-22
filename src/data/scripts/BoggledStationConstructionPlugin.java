package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.econ.Market;
import data.campaign.econ.boggledTools;
import data.campaign.quests.boggledGateQuest.QuestEveryFrameScript;
import data.campaign.quests.boggledGateQuest.WindsMessageDialogPluginAstral;
import data.campaign.quests.boggledLobsterQuest.lobsterBarEventCreator;
import data.campaign.quests.boggledLobsterQuest.lobsterQuestCoordinator;

//Additionally required for Example 2:

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class BoggledStationConstructionPlugin extends BaseModPlugin
{
    public void applyStationSettingsToAllStationsInSector()
    {
        if(Global.getSettings().getBoolean("boggledApplyStationSettingsToAllStationsInSector"))
        {
            Iterator allSystems = Global.getSector().getStarSystems().iterator();
            while(allSystems.hasNext())
            {
                StarSystemAPI system = (StarSystemAPI) allSystems.next();
                Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(system).iterator();
                while(allMarketsInSystem.hasNext())
                {
                    MarketAPI market = (MarketAPI) allMarketsInSystem.next();
                    SectorEntityToken primaryEntity = market.getPrimaryEntity();
                    if(primaryEntity != null && primaryEntity.hasTag("station"))
                    {
                        //Cramped Quarters also controls global hazard and accessibility modifications
                        //even if Cramped Quarters itself is disabled
                        if(!market.hasCondition("cramped_quarters"))
                        {
                            market.addCondition("cramped_quarters");
                        }

                        //Some special items require "no_atmosphere" condition on market to be installed
                        //Stations by default don't meet this condition because they don't have the "no_atmosphere" condition
                        //Combined with market_conditions.csv overwrite, this will give stations no_atmosphere while
                        //hiding all effects from the player and having no impact on the economy or hazard rating
                        if(!market.hasCondition("no_atmosphere"))
                        {
                            market.addCondition("no_atmosphere");
                            market.suppressCondition("no_atmosphere");
                        }
                    }
                }
            }
        }
    }

    public void applyStationConstructionAbilitiesPerSettingsFile()
    {
        if(Global.getSettings().getBoolean("boggledStationConstructionContentEnabled"))
        {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_astropolis_station"))
            {
                if(Global.getSettings().getBoolean("boggledAstropolisEnabled"))
                {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_astropolis_station");
                }
            }
            else
            {
                if(!Global.getSettings().getBoolean("boggledAstropolisEnabled"))
                {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_astropolis_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_mining_station"))
            {
                if(Global.getSettings().getBoolean("boggledMiningStationEnabled"))
                {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_mining_station");
                }
            }
            else
            {
                if(!Global.getSettings().getBoolean("boggledMiningStationEnabled"))
                {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_siphon_station"))
            {
                if(Global.getSettings().getBoolean("boggledSiphonStationEnabled"))
                {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_siphon_station");
                }
            }
            else
            {
                if(!Global.getSettings().getBoolean("boggledSiphonStationEnabled"))
                {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_colonize_abandoned_station"))
            {
                if(Global.getSettings().getBoolean("boggledStationColonizationEnabled"))
                {
                    Global.getSector().getCharacterData().addAbility("boggled_colonize_abandoned_station");
                }
            }
            else
            {
                if(!Global.getSettings().getBoolean("boggledStationColonizationEnabled"))
                {
                    Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");
                }
            }
        }
        else
        {
            Global.getSector().getCharacterData().removeAbility("boggled_construct_astropolis_station");
            Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
            Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
            Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");
        }
    }

    @Override
    public void onNewGame()
    {
        applyStationSettingsToAllStationsInSector();
    }

    public void afterGameSave()
    {
        applyStationSettingsToAllStationsInSector();

        //Confirm terraforming ability is enabled/disabled per the settings file
        if(Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_terraforming_control_panel"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_terraforming_control_panel");
            }

            if(!Global.getSector().getPlayerFleet().hasAbility("boggled_seed_lobsters"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_seed_lobsters");
            }
        }
        else
        {
            Global.getSector().getCharacterData().removeAbility("boggled_terraforming_control_panel");
            Global.getSector().getCharacterData().removeAbility("boggled_seed_lobsters");
        }

        applyStationConstructionAbilitiesPerSettingsFile();

        //Confirm gate abilities are enabled/disabled per the settings file
        if(Global.getSettings().getBoolean("boggledAstralGateContentEnabled"))
        {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_astral_gate"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_construct_astral_gate");
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_initiate_warp"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_initiate_warp");
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_select_previous_gate"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_select_previous_gate");
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_select_next_gate"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_select_next_gate");
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_gift_astral_gate") && Global.getSettings().getBoolean("boggledGiftAstralGateEnabled"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_gift_astral_gate");
            }

            if(!boggledTools.playerHasGateIdTag())
            {
                Global.getSector().getPlayerFleet().addTag("boggled_astral_gate_target_id_0");
            }

            Global.getSector().getPlayerFleet().removeScriptsOfClass(QuestEveryFrameScript.class);
            Global.getSector().getPlayerFleet().addScript(new QuestEveryFrameScript());
        }
        else
        {
            Global.getSector().getCharacterData().removeAbility("boggled_construct_astral_gate");
            Global.getSector().getCharacterData().removeAbility("boggled_initiate_warp");
            Global.getSector().getCharacterData().removeAbility("boggled_select_previous_gate");
            Global.getSector().getCharacterData().removeAbility("boggled_select_next_gate");
            Global.getSector().getCharacterData().removeAbility("boggled_gift_astral_gate");

            if(!boggledTools.playerHasGateIdTag())
            {
                Global.getSector().getPlayerFleet().addTag("boggled_astral_gate_target_id_0");
            }

            Global.getSector().getPlayerFleet().removeScriptsOfClass(QuestEveryFrameScript.class);
        }

        //Enable/disable the Domain Archaeology industry
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            if(Global.getSector().getFaction("luddic_church") != null && !Global.getSector().getFaction("luddic_church").isIllegal("domain_artifacts"))
            {
                Global.getSector().getFaction("luddic_church").makeCommodityIllegal("domain_artifacts");
            }

            if(Global.getSector().getFaction("luddic_path") != null && !Global.getSector().getFaction("luddic_path").isIllegal("domain_artifacts"))
            {
                Global.getSector().getFaction("luddic_path").makeCommodityIllegal("domain_artifacts");
            }

            Global.getSettings().getCommoditySpec("domain_artifacts").getTags().clear();

            if(Global.getSettings().getBoolean("boggledReplaceAgreusTechMiningWithDomainArchaeology"))
            {
                SectorEntityToken agreusPlanet = boggledTools.getPlanetTokenForQuest("Arcadia", "agreus");
                if(agreusPlanet != null)
                {
                    MarketAPI agreusMarket = agreusPlanet.getMarket();
                    if(agreusMarket != null && agreusMarket.hasIndustry(Industries.TECHMINING) && !agreusMarket.hasIndustry("DOMAIN_ARCHAEOLOGY") && !agreusMarket.isPlayerOwned())
                    {
                        agreusMarket.removeIndustry(Industries.TECHMINING, null, false);
                        agreusMarket.addIndustry("DOMAIN_ARCHAEOLOGY");
                    }
                }
            }
        }
        else
        {
            Global.getSettings().getCommoditySpec("domain_artifacts").getTags().add("nonecon");
        }
    }

    public void beforeGameSave()
    {
        Global.getSector().getCharacterData().removeAbility("boggled_terraforming_control_panel");
        Global.getSector().getCharacterData().removeAbility("boggled_seed_lobsters");

        Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
        Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
        Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");

        Global.getSector().getCharacterData().removeAbility("boggled_construct_astral_gate");
        Global.getSector().getCharacterData().removeAbility("boggled_initiate_warp");
        Global.getSector().getCharacterData().removeAbility("boggled_select_previous_gate");
        Global.getSector().getCharacterData().removeAbility("boggled_select_next_gate");
        if(!boggledTools.playerHasGateIdTag())
        {
                Global.getSector().getPlayerFleet().addTag("boggled_astral_gate_target_id_0");
        }
        Global.getSector().getPlayerFleet().removeScriptsOfClass(QuestEveryFrameScript.class);
        Global.getSettings().getCommoditySpec("domain_artifacts").getTags().clear();
    }

    public void onGameLoad(boolean newGame)
    {
        if(Global.getSettings().getBoolean("boggledCheckForUpdates"))
        {
            Global.getSector().addTransientScript(new BoggledUpdateNotificationScript());
        }

        applyStationSettingsToAllStationsInSector();

        //Confirm terraforming ability is enabled/disabled per the settings file
        if(Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_terraforming_control_panel"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_terraforming_control_panel");
            }

            if(!Global.getSector().getPlayerFleet().hasAbility("boggled_seed_lobsters"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_seed_lobsters");
            }
        }
        else
        {
            Global.getSector().getCharacterData().removeAbility("boggled_terraforming_control_panel");
            Global.getSector().getCharacterData().removeAbility("boggled_seed_lobsters");
        }

        applyStationConstructionAbilitiesPerSettingsFile();

        //Confirm gate abilities are enabled/disabled per the settings file
        if(Global.getSettings().getBoolean("boggledAstralGateContentEnabled"))
        {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_astral_gate"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_construct_astral_gate");
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_initiate_warp"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_initiate_warp");
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_select_previous_gate"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_select_previous_gate");
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_select_next_gate"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_select_next_gate");
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_gift_astral_gate") && Global.getSettings().getBoolean("boggledGiftAstralGateEnabled"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_gift_astral_gate");
            }

            if(!boggledTools.playerHasGateIdTag())
            {
                Global.getSector().getPlayerFleet().addTag("boggled_astral_gate_target_id_0");
            }

            Global.getSector().getPlayerFleet().removeScriptsOfClass(QuestEveryFrameScript.class);
            Global.getSector().getPlayerFleet().addScript(new QuestEveryFrameScript());
        }
        else
        {
            Global.getSector().getCharacterData().removeAbility("boggled_construct_astral_gate");
            Global.getSector().getCharacterData().removeAbility("boggled_initiate_warp");
            Global.getSector().getCharacterData().removeAbility("boggled_select_previous_gate");
            Global.getSector().getCharacterData().removeAbility("boggled_select_next_gate");
            Global.getSector().getCharacterData().removeAbility("boggled_gift_astral_gate");

            if(!boggledTools.playerHasGateIdTag())
            {
                Global.getSector().getPlayerFleet().addTag("boggled_astral_gate_target_id_0");
            }

            Global.getSector().getPlayerFleet().removeScriptsOfClass(QuestEveryFrameScript.class);
        }

        //Enable/disable the Domain Archaeology industry
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            if(Global.getSector().getFaction("luddic_church") != null && !Global.getSector().getFaction("luddic_church").isIllegal("domain_artifacts"))
            {
                Global.getSector().getFaction("luddic_church").makeCommodityIllegal("domain_artifacts");
            }

            if(Global.getSector().getFaction("luddic_path") != null && !Global.getSector().getFaction("luddic_path").isIllegal("domain_artifacts"))
            {
                Global.getSector().getFaction("luddic_path").makeCommodityIllegal("domain_artifacts");
            }

            Global.getSettings().getCommoditySpec("domain_artifacts").getTags().clear();

            if(Global.getSettings().getBoolean("boggledReplaceAgreusTechMiningWithDomainArchaeology"))
            {
                SectorEntityToken agreusPlanet = boggledTools.getPlanetTokenForQuest("Arcadia", "agreus");
                if(agreusPlanet != null)
                {
                    MarketAPI agreusMarket = agreusPlanet.getMarket();
                    if(agreusMarket != null && agreusMarket.hasIndustry(Industries.TECHMINING) && !agreusMarket.hasIndustry("DOMAIN_ARCHAEOLOGY") && !agreusMarket.isPlayerOwned())
                    {
                        agreusMarket.removeIndustry(Industries.TECHMINING, null, false);
                        agreusMarket.addIndustry("DOMAIN_ARCHAEOLOGY");
                    }
                }
            }
        }
        else
        {
            Global.getSettings().getCommoditySpec("domain_artifacts").getTags().add("nonecon");
        }

        if(Global.getSettings().getBoolean("boggledLobsterQuestEnabled"))
        {
            BarEventManager barEventManager = BarEventManager.getInstance();

            // If the prerequisites for the quest have been met (optional) and the game isn't already aware of the bar event,
            // add it to the BarEventManager so that it shows up in bars
            if (lobsterQuestCoordinator.shouldOfferQuest() && !barEventManager.hasEventCreator(lobsterBarEventCreator.class))
            {
                barEventManager.addEventCreator(new lobsterBarEventCreator());
            }
        }
    }
}