package data.campaign.quests.boggledGateQuest;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class WindsMessageDialogPluginLudd implements InteractionDialogPlugin
{
    protected InteractionDialogAPI dialog;

    /**
     * Called when the dialog is shown.
     *
     * @param dialog the actual UI element being shown
     */
    @Override
    public void init(InteractionDialogAPI dialog)
    {
        // Save the dialog UI element so that we can write to it outside of this method
        this.dialog = dialog;

        // Launch into our event by triggering the invisible "INIT" option,
        // which will call `optionSelected()`
        this.optionSelected(null, WindsMessageDialogPluginLudd.OptionId.INIT);
    }

    /**
     * This method is called when the player has selected some option on the dialog.
     *
     * @param optionText the actual text that was displayed on the selected option
     * @param optionData the value used to uniquely identify the option
     */
    @Override
    public void optionSelected(String optionText, Object optionData)
    {
        if (optionData instanceof OptionId)
        {
            // Clear shown options before we show new ones
            dialog.getOptionPanel().clearOptions();

            // Handle all possible options the player can choose
            switch ((OptionId) optionData)
            {
                // The invisible "init" option was selected by the init method.
                case INIT:
                    FactoryAPI fac = Global.getFactory();
                    PersonAPI odysseusAI = fac.createPerson();
                    FullName odysseusAIName = new FullName("Odysseus", "", FullName.Gender.MALE);
                    odysseusAI.setName(odysseusAIName);
                    odysseusAI.setPortraitSprite(Global.getSettings().getSpriteName("boggled", "portrait_gate_quest_ai"));

                    odysseusAI.setFaction(Factions.REMNANTS);
                    odysseusAI.setRankId(Ranks.AGENT);
                    odysseusAI.setPostId(Ranks.POST_CITIZEN);

                    dialog.getVisualPanel().showPersonInfo(odysseusAI, true);

                    dialog.getTextPanel().addPara("As you approach Aeolus, you receive a hail from Odysseus. You accept the connection.");

                    dialog.getTextPanel().addPara("\"Welcome back. Were you able to acquire the data?\"");

                    dialog.getOptionPanel().addOption("Transmit the data to Odysseus", WindsMessageDialogPluginLudd.OptionId.EXPLAIN_DEMAND);
                    break;
                case EXPLAIN_DEMAND:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"Allow me a moment to analyze the data.\"");

                    dialog.getTextPanel().addPara("The image of the AI core on your bridge monitor remains completely still for several hours. \"How long will this take?\" your science officer moans. \"And where is that thing getting the electricity to power itself? These sort of calculations should be extremely energy intensive.\"");

                    dialog.getTextPanel().addPara("Eventually Odysseus begins animating the simulated visage once more.");

                    dialog.getOptionPanel().addOption("\"Why did the gates stop working?\"", WindsMessageDialogPluginLudd.OptionId.CAN_FIX_GATES);
                    break;
                case CAN_FIX_GATES:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"I do not know.\" The AI core pauses for a moment. \"My calculations show a moderate probability that the gates were developed by reverse engineering technology from a more advanced non-human civilization. This would explain why even their creators had a poor understanding of the principles behind their function.\"");

                    dialog.getOptionPanel().addOption("\"So you're saying I wasted my time getting this data?\"", WindsMessageDialogPluginLudd.OptionId.TIME_WASTED);
                    dialog.getOptionPanel().addOption("\"The gates were copied from alien designs?\"", WindsMessageDialogPluginLudd.OptionId.ALIEN_DESIGNS);
                    break;
                case ALIEN_DESIGNS:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"Based on the information I have, it is inconclusive whether the gates are of alien design. That is merely a moderately probable scenario.\"");

                    dialog.getOptionPanel().addOption("\"So you're saying I wasted my time getting this data?\"", WindsMessageDialogPluginLudd.OptionId.TIME_WASTED);
                    break;
                case TIME_WASTED:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"It was not a complete waste. I now know that I cannot reactivate the gates.\" The AI pauses again, this time for a longer duration. \"Returning to my creators will take considerably more time and effort than I anticipated.\"");

                    dialog.getOptionPanel().addOption("\"How do you plan on doing that without the gate network?\"", WindsMessageDialogPluginLudd.OptionId.BUILD_ASTRAL_GATE_NET);
                    break;
                case BUILD_ASTRAL_GATE_NET:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"It will be necessary to establish a Sector-wide government. Only then could sufficient resources be allocated to conducting a serious inquiry into why the gate network stopped working.\"");

                    dialog.getOptionPanel().addOption("\"Do you plan to try to conquer the Sector? How?\"", WindsMessageDialogPluginLudd.OptionId.CONQUER_SECTOR);
                    break;
                case CONQUER_SECTOR:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"I will need a capable human to serve as a figurehead. Are you interested?\"");

                    dialog.getOptionPanel().addOption("\"Yes.\"", OptionId.YES_INTERESTED);
                    dialog.getOptionPanel().addOption("\"No.\"", OptionId.NO_INTERESTED);
                    dialog.getOptionPanel().addOption("\"I'm going to inform the Knights of Ludd about your plans.\"", OptionId.NO_LUDDIC);
                    break;
                case YES_INTERESTED:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"Excellent.\" For the first time, you sense the AI core may not be entirely emotionless. \"Allow me to fly up and join your fleet.\" It cuts the comm link.");

                    dialog.getTextPanel().addPara("After a short wait, a lone Conquest-class battlecruiser emerges from the clouds of Aeolus and falls in with your fleet. \"That's it? I was expecting a huge armada,\" says your nav officer.");

                    dialog.getTextPanel().addPara("As if on queue, the battlecruiser hails your flagship. The AI core reappears on the bridge monitor. \"Don't be fooled by appearances. This vessel is far superior to the standard model of this hull. It will prove invaluable in the battles to come.\"");

                    if(Global.getSettings().getModManager().isModEnabled("archeus"))
                    {
                        dialog.getTextPanel().addPara("NOTE: You appear to have the Archean Order mod enabled. The Conquest-class battlecruiser will not be added to your fleet because Archean Order changes core game files in such a way that adding the ship would result in a crash. Sorry!");
                    }

                    dialog.getOptionPanel().addOption("Continue", WindsMessageDialogPluginLudd.OptionId.DISMISS_DIALOGUE_ADD_ODYSSEUS);
                    break;
                case NO_INTERESTED:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("The AI core cuts the comm link without any further comments. Your fleet detects no activity in the clouds of Aeolus.");

                    dialog.getTextPanel().addPara("\"That's it? I guess it was expecting us to provide the ships it would need to conquer the Sector,\" says your nav officer.");

                    dialog.getTextPanel().addPara("Your science officer grimaces. \"I wouldn't be so sure. I imagine it will need some time to gather allies and ships. It may be centuries before it decides to act.\"");

                    dialog.getOptionPanel().addOption("Continue", WindsMessageDialogPluginLudd.OptionId.DISMISS_DIALOGUE);
                    break;
                case NO_LUDDIC:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("The AI core cuts the comm link without any further comments. Your fleet detects no activity in the clouds of Aeolus.");

                    dialog.getTextPanel().addPara("\"It will likely be pointless trying warn anyone about this. They won't believe you, and this thing is clearly able to evade any attempts to find it,\" says your science officer.");

                    dialog.getOptionPanel().addOption("Continue", WindsMessageDialogPluginLudd.OptionId.DISMISS_DIALOGUE);
                    break;
                case DISMISS_DIALOGUE:
                    Global.getSector().getPlayerFleet().removeTag("boggled_gate_quest_stage7");
                    Global.getSector().getPlayerFleet().addTag("boggled_gate_quest_stage6");

                    //End the quest
                    Iterator allIntel = Global.getSector().getIntelManager().getIntel().iterator();
                    while(allIntel.hasNext())
                    {
                        BaseIntelPlugin intel = (BaseIntelPlugin)allIntel.next();
                        if(intel instanceof BoggledGateQuestIntel)
                        {
                            intel.endAfterDelay();
                        }
                    }

                    Misc.makeUnimportant(boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4"), "boggled_aeolus_importance");

                    dialog.dismiss();
                    break;
                case DISMISS_DIALOGUE_ADD_ODYSSEUS:
                    Global.getSector().getPlayerFleet().removeTag("boggled_gate_quest_stage7");
                    Global.getSector().getPlayerFleet().addTag("boggled_gate_quest_stage6");

                    FleetDataAPI fleet = Global.getSector().getPlayerFleet().getFleetData();

                    FactoryAPI fac1 = Global.getFactory();
                    PersonAPI odysseusOfficer = fac1.createPerson();
                    FullName odysseusOfficerName = new FullName("Odysseus", "", FullName.Gender.MALE);
                    odysseusOfficer.setName(odysseusOfficerName);
                    odysseusOfficer.setPortraitSprite(Global.getSettings().getSpriteName("boggled", "portrait_gate_quest_ai"));

                    odysseusOfficer.setFaction(Global.getSector().getPlayerFaction().getId());
                    odysseusOfficer.setRankId(Ranks.SPACE_CAPTAIN);
                    maxOutSkills(odysseusOfficer);

                    fleet.addOfficer(odysseusOfficer);

                    if(!Global.getSettings().getModManager().isModEnabled("archeus"))
                    {
                        Random r = new Random();
                        FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "conquest_BoggledOlympians");
                        ship.setShipName("EUTCS Nausicaa");
                        FleetEncounterContext.prepareShipForRecovery(ship, true, true, true, 1f, 1f, r);

                        fleet.addFleetMember(ship);
                        ship.setCaptain(odysseusOfficer);
                    }

                    //odysseusOfficer.getStats().addXP(350000); //lvl 20
                    odysseusOfficer.getStats().addXP(3206250); //lvl 44 - enough to legitimately max out skills

                    //End the quest
                    Iterator allIntel1 = Global.getSector().getIntelManager().getIntel().iterator();
                    while(allIntel1.hasNext())
                    {
                        BaseIntelPlugin intel = (BaseIntelPlugin)allIntel1.next();
                        if(intel instanceof BoggledGateQuestIntel)
                        {
                            intel.endAfterDelay();
                        }
                    }

                    Misc.makeUnimportant(boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4"), "boggled_aeolus_importance");

                    dialog.dismiss();
                    break;
            }
        }
    }

    private void maxOutSkills(PersonAPI person)
    {
        person.getStats().increaseSkill("combat_endurance");
        person.getStats().increaseSkill("combat_endurance");
        person.getStats().increaseSkill("combat_endurance");

        person.getStats().increaseSkill("missile_specialization");
        person.getStats().increaseSkill("missile_specialization");
        person.getStats().increaseSkill("missile_specialization");

        person.getStats().increaseSkill("ordnance_expert");
        person.getStats().increaseSkill("ordnance_expert");
        person.getStats().increaseSkill("ordnance_expert");

        person.getStats().increaseSkill("target_analysis");
        person.getStats().increaseSkill("target_analysis");
        person.getStats().increaseSkill("target_analysis");

        person.getStats().increaseSkill("damage_control");
        person.getStats().increaseSkill("damage_control");
        person.getStats().increaseSkill("damage_control");

        person.getStats().increaseSkill("impact_mitigation");
        person.getStats().increaseSkill("impact_mitigation");
        person.getStats().increaseSkill("impact_mitigation");

        person.getStats().increaseSkill("defensive_systems");
        person.getStats().increaseSkill("defensive_systems");
        person.getStats().increaseSkill("defensive_systems");

        person.getStats().increaseSkill("advanced_countermeasures");
        person.getStats().increaseSkill("advanced_countermeasures");
        person.getStats().increaseSkill("advanced_countermeasures");

        person.getStats().increaseSkill("evasive_action");
        person.getStats().increaseSkill("evasive_action");
        person.getStats().increaseSkill("evasive_action");

        person.getStats().increaseSkill("helmsmanship");
        person.getStats().increaseSkill("helmsmanship");
        person.getStats().increaseSkill("helmsmanship");

        person.getStats().increaseSkill("carrier_command");
        person.getStats().increaseSkill("carrier_command");
        person.getStats().increaseSkill("carrier_command");

        person.getStats().increaseSkill("wing_commander");
        person.getStats().increaseSkill("wing_commander");
        person.getStats().increaseSkill("wing_commander");

        person.getStats().increaseSkill("strike_commander");
        person.getStats().increaseSkill("strike_commander");
        person.getStats().increaseSkill("strike_commander");

        person.getStats().increaseSkill("gunnery_implants");
        person.getStats().increaseSkill("gunnery_implants");
        person.getStats().increaseSkill("gunnery_implants");

        person.getStats().increaseSkill("flux_modulation");
        person.getStats().increaseSkill("flux_modulation");
        person.getStats().increaseSkill("flux_modulation");
    }

    enum OptionId
    {
        INIT,
        EXPLAIN_DEMAND,
        CAN_FIX_GATES,
        ALIEN_DESIGNS,
        TIME_WASTED,
        BUILD_ASTRAL_GATE_NET,
        CONQUER_SECTOR,
        YES_INTERESTED,
        NO_INTERESTED,
        NO_LUDDIC,
        DISMISS_DIALOGUE,
        DISMISS_DIALOGUE_ADD_ODYSSEUS,
    }

    // The rest of the methods must exist, but can be ignored for our simple demo quest.
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