package data.campaign.quests.boggledLobsterQuest;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.*;
import com.fs.starfarer.combat.entities.terrain.Planet;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import com.fs.starfarer.rpg.Person;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;

import java.util.*;
import java.lang.String;

public class boggledInitiateZhangBattle extends BaseCommandPlugin
{
    public boggledInitiateZhangBattle()
    {
    }

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap)
    {
        if (dialog == null)
        {
            return false;
        }
        else
        {
            final SectorEntityToken entity = dialog.getInteractionTarget();
            final MemoryAPI memory = getEntityMemory(memoryMap);

            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 5);
            market.getStability().modifyFlat("fake", 10000.0F);
            market.setFactionId("tritachyon");
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0.0F, 0.0F);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod("fleet_quality_mod").modifyFlat("fake", 1.0f);
            market.getStats().getDynamic().getMod("combat_fleet_size_mult").modifyFlat("fake", 1.0F);

            FleetParamsV3 zhangFleetParams = new FleetParamsV3(market, "taskForce", 550f, 0f, 0f, 0f, 0f, 0f, 5f);
            final CampaignFleetAPI defenders = FleetFactoryV3.createFleet(zhangFleetParams);
            if (defenders == null)
            {
                return false;
            }
            else
            {
                dialog.setInteractionTarget(defenders);
                final FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();
                config.leaveAlwaysAvailable = false;
                config.showCommLinkOption = false;
                config.showEngageText = false;
                config.showFleetAttitude = false;
                config.showTransponderStatus = false;
                config.showWarningDialogWhenNotHostile = false;
                config.alwaysAttackVsAttack = true;
                config.impactsAllyReputation = false;
                config.impactsEnemyReputation = false;
                config.pullInAllies = false;
                config.pullInEnemies = false;
                config.pullInStations = false;
                config.lootCredits = false;
                config.firstTimeEngageOptionText = "Engage Zhang's Fleet";
                config.afterFirstTimeEngageOptionText = "Re-engage Zhang's Fleet";
                config.noSalvageLeaveOptionText = "Continue";
                config.dismissOnLeave = false;
                config.printXPToDialog = true;
                long seed = memory.getLong("$salvageSeed");
                config.salvageRandom = Misc.getRandom(seed, 75);
                final FleetInteractionDialogPluginImpl plugin = new FleetInteractionDialogPluginImpl(config);
                final InteractionDialogPlugin originalPlugin = dialog.getPlugin();
                config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate()
                {
                    public void notifyLeave(InteractionDialogAPI dialog)
                    {
                        defenders.clearAssignments();
                        defenders.deflate();
                        dialog.setPlugin(originalPlugin);
                        dialog.setInteractionTarget(entity);
                        if (plugin.getContext() instanceof FleetEncounterContext)
                        {
                            FleetEncounterContext context = (FleetEncounterContext)plugin.getContext();
                            if (context.didPlayerWinEncounterOutright())
                            {
                                SalvageGenFromSeed.SDMParams p = new SalvageGenFromSeed.SDMParams();
                                p.entity = entity;
                                p.factionId = defenders.getFaction().getId();
                                SalvageGenFromSeed.SalvageDefenderModificationPlugin pluginx = (SalvageGenFromSeed.SalvageDefenderModificationPlugin)Global.getSector().getGenericPlugins().pickPlugin(SalvageGenFromSeed.SalvageDefenderModificationPlugin.class, p);
                                if (pluginx != null)
                                {
                                    pluginx.reportDefeated(p, entity, defenders);
                                }

                                FireBest.fire((String)null, dialog, memoryMap, "boggledBeatZhangFleet");
                            }
                            else
                            {
                                FireBest.fire((String)null, dialog, memoryMap, "boggledLostToZhangFleet");
                            }
                        }
                        else
                        {
                            dialog.dismiss();
                        }
                    }

                    public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc)
                    {
                        bcc.aiRetreatAllowed = false;
                        bcc.objectivesAllowed = false;
                        bcc.enemyDeployAll = true;
                    }

                    public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage)
                    {
                        FleetEncounterContextPlugin.DataForEncounterSide winner = context.getWinnerData();
                        FleetEncounterContextPlugin.DataForEncounterSide loser = context.getLoserData();
                        if (winner != null && loser != null)
                        {
                            float playerContribMult = context.computePlayerContribFraction();
                            List<SalvageEntityGenDataSpec.DropData> dropRandom = new ArrayList();
                            List<SalvageEntityGenDataSpec.DropData> dropValue = new ArrayList();
                            float valueMultFleet = Global.getSector().getPlayerFleet().getStats().getDynamic().getValue("battle_salvage_value_bonus_fleet");
                            float valueModShips = context.getSalvageValueModPlayerShips();
                            Iterator var12 = winner.getEnemyCasualties().iterator();

                            while(var12.hasNext())
                            {
                                FleetEncounterContextPlugin.FleetMemberData data = (FleetEncounterContextPlugin.FleetMemberData)var12.next();
                                if (config.salvageRandom.nextFloat() < playerContribMult) {
                                    SalvageEntityGenDataSpec.DropData drop = new SalvageEntityGenDataSpec.DropData();
                                    drop.chances = 1;
                                    drop.value = -1;

                                    if (drop.group != null)
                                    {
                                        dropRandom.add(drop);
                                    }
                                }
                            }

                            float fuelMult = Global.getSector().getPlayerFleet().getStats().getDynamic().getValue("fuel_salvage_value_mult_fleet");
                            CargoAPI extra = SalvageEntity.generateSalvage(config.salvageRandom, valueMultFleet + valueModShips, 1.0F, 1.0F, fuelMult, dropValue, dropRandom);

                            CargoStackAPI stack;
                            for(Iterator var14 = extra.getStacksCopy().iterator(); var14.hasNext(); salvage.addFromStack(stack))
                            {
                                stack = (CargoStackAPI)var14.next();
                                if (stack.isFuelStack())
                                {
                                    stack.setSize((float)((int)(stack.getSize() * fuelMult)));
                                }
                            }

                        }
                    }
                };
                dialog.setPlugin(plugin);
                plugin.init(dialog);
                return true;
            }
        }
    }
}