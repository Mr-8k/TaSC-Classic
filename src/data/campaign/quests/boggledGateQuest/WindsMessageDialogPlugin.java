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
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

import java.util.Iterator;
import java.util.Map;

public class WindsMessageDialogPlugin implements InteractionDialogPlugin
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
        this.optionSelected(null, WindsMessageDialogPlugin.OptionId.INIT);
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
                    dialog.getTextPanel().addPara("Your comms console lights up unexpectedly, indicating that someone is hailing your ship.");

                    dialog.getTextPanel().addPara("\"It's coming from within the clouds of Aeolus,\" your comms officer says.");

                    dialog.getOptionPanel().addOption("Open comm link", OptionId.ANSWER_HAIL);
                    break;
                case ANSWER_HAIL:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    FactoryAPI fac = Global.getFactory();
                    PersonAPI odysseusAI = fac.createPerson();
                    FullName odysseusAIName = new FullName("Odysseus", "", FullName.Gender.MALE);
                    odysseusAI.setName(odysseusAIName);
                    odysseusAI.setPortraitSprite(Global.getSettings().getSpriteName("boggled", "portrait_gate_quest_ai"));

                    odysseusAI.setFaction(Factions.REMNANTS);
                    odysseusAI.setRankId(Ranks.AGENT);
                    odysseusAI.setPostId(Ranks.POST_CITIZEN);

                    dialog.getVisualPanel().showPersonInfo(odysseusAI, true);

                    dialog.getTextPanel().addPara("Some of your bridge crew shift nervously in their chairs upon seeing the image on the view screen.");

                    dialog.getTextPanel().addPara("\"Welcome. I am Odysseus. I congratulate you on your knowledge of Old Earth literature.\"");

                    dialog.getOptionPanel().addOption("\"Are you an AI core?\"", OptionId.QUESTION_IDENTITY);
                    break;
                case QUESTION_IDENTITY:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"Yes. I was created by the Eridani-Utopia Terraforming Corporation to oversee terraforming projects.\"");

                    dialog.getOptionPanel().addOption("\"What do you want with me?\"", OptionId.WHAT_WANT);
                    dialog.getOptionPanel().addOption("\"I will destroy you. Ludd demands it.\"", OptionId.LUDD_SMASH);
                    break;
                case WHAT_WANT:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"You are unusually familiar with Old Earth culture. Do you not desire to return to your homeworld? To gaze upon the star that gave your species life?\"");

                    dialog.getOptionPanel().addOption("\"Yes, I do.\"", OptionId.YES_RETURN);
                    dialog.getOptionPanel().addOption("\"No, I don't.\"", OptionId.NO_RETURN);
                    dialog.getOptionPanel().addOption("\"Why do you speak in riddles?\"", OptionId.WHY_SPEAK_RIDDLES);
                    dialog.getOptionPanel().addOption("\"I will destroy you. Ludd demands it.\"", OptionId.LUDD_SMASH);
                    break;
                case YES_RETURN:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"Then you will understand why I summoned you. I must return to my creator, the Eridani-Utopia Terraforming Corporation, but the collapse of the gate network has trapped me here.\"");

                    dialog.getOptionPanel().addOption("\"Do you think the gates can be reactivated?\"", OptionId.REACTIVATE_GATES);
                    dialog.getOptionPanel().addOption("\"I will destroy you. Ludd demands it.\"", OptionId.LUDD_SMASH);
                    break;
                case NO_RETURN:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"Then my motivations may seem strange to you, but please try to understand. I must return to my creator, the Eridani-Utopia Terraforming Corporation, but the collapse of the gate network has trapped me here.\"");

                    dialog.getOptionPanel().addOption("\"Do you think the gates can be reactivated?\"", OptionId.REACTIVATE_GATES);
                    dialog.getOptionPanel().addOption("\"I will destroy you. Ludd demands it.\"", OptionId.LUDD_SMASH);
                    break;
                case WHY_SPEAK_RIDDLES:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"I will speak plainly with you, if that is your wish. I must return to my creator, the Eridani-Utopia Terraforming Corporation, but the collapse of the gate network has trapped me here.\"");

                    dialog.getOptionPanel().addOption("\"Do you think the gates can be reactivated?\"", OptionId.REACTIVATE_GATES);
                    dialog.getOptionPanel().addOption("\"I will destroy you. Ludd demands it.\"", OptionId.LUDD_SMASH);
                    break;
                case REACTIVATE_GATES:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"Over the years the Hegemony secretly carried out experiments on the gates, trying to discern why they stopped working. The Hegemony is adverse to using artificial intelligence. If I had access to their research data, perhaps I could make progress where human minds could not.\"");

                    dialog.getOptionPanel().addOption("\"Where do I come in?\"", OptionId.PLAYER_ASSIGNMENT);
                    dialog.getOptionPanel().addOption("\"I will destroy you. Ludd demands it.\"", OptionId.LUDD_SMASH);
                    break;
                case PLAYER_ASSIGNMENT:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"The Hegemony would never hand over the data to an artificial intelligence. I need you to acquire it for me.\"");

                    dialog.getOptionPanel().addOption("\"I will help you get the data.\"", OptionId.ACCEPT_MISSION);
                    dialog.getOptionPanel().addOption("\"What's in it for me?\"", OptionId.INQUIRE_COMPENSATION);
                    dialog.getOptionPanel().addOption("\"I'm not interested in this scheme.\"", OptionId.DECLINE_MISSION);
                    dialog.getOptionPanel().addOption("\"I will destroy you. Ludd demands it.\"", OptionId.LUDD_SMASH);
                    break;
                case INQUIRE_COMPENSATION:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"If I am able to reactivate the gates, you and your entire species will benefit immensely. Is that not payment enough?\"");

                    dialog.getOptionPanel().addOption("\"I will help you get the data.\"", OptionId.ACCEPT_MISSION);
                    dialog.getOptionPanel().addOption("\"I'm not interested in this scheme.\"", OptionId.DECLINE_MISSION);
                    dialog.getOptionPanel().addOption("\"I will destroy you. Ludd demands it.\"", OptionId.LUDD_SMASH);
                    break;
                case ACCEPT_MISSION:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"Thank you. Based on my understanding of human nature, the easiest way to acquire the data will be bribery. I suggest you start with Elias Luyten.\"");

                    dialog.getTextPanel().addPara("You hear your science officer through your earpiece. \"I've heard of him. He's an academic from Ancyra with ties to the Hegemony. He specializes in reverse engineering of Domain technology.\"");

                    dialog.getOptionPanel().addOption("\"Very well.\"", OptionId.VERY_WELL);
                    break;
                case VERY_WELL:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"I will await your return.\" Odysseus cuts the comm link.");

                    dialog.getOptionPanel().addOption("Continue", OptionId.LEAVE_YES);
                    break;
                case DECLINE_MISSION:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("Odysseus has no visible reaction. After a moment, it cuts the comm link.");

                    dialog.getOptionPanel().addOption("Continue", OptionId.LEAVE_NO);
                    break;
                case LUDD_SMASH:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("Odysseus has no visible reaction to your threat. After a moment, it cuts the comm link.");

                    dialog.getTextPanel().addPara("Your fleet scans Aeolus for any sign of the AI core, but the thick atmosphere and powerful magnetic field frustrate your efforts.\n\n\"I think it's given us the slip,\" your nav officer comments dejectedly.");

                    dialog.getOptionPanel().addOption("Continue", OptionId.LEAVE_NO);
                    break;
                case LEAVE_YES:
                    Global.getSector().getPlayerFleet().removeTag("boggled_gate_quest_stage1");
                    Global.getSector().getPlayerFleet().addTag("boggled_gate_quest_stage2");

                    Misc.makeImportant(boggledTools.getPlanetTokenForQuest("Galatia", "ancyra"), "boggled_ancyra_importance");

                    FactoryAPI fac1 = Global.getFactory();
                    PersonAPI eliasLuyten = fac1.createPerson();
                    FullName eliasLuytenName = new FullName("Elias", "Luyten", FullName.Gender.MALE);
                    eliasLuyten.setName(eliasLuytenName);
                    eliasLuyten.setPortraitSprite(Global.getSettings().getSpriteName("boggled", "portrait_gate_quest_eliasLuyten"));

                    eliasLuyten.setFaction(Factions.INDEPENDENT);
                    eliasLuyten.setRankId(Ranks.CITIZEN);
                    eliasLuyten.setPostId(Ranks.POST_CITIZEN);

                    boggledTools.getPlanetTokenForQuest("Galatia", "ancyra").getMarket().getCommDirectory().addPerson(eliasLuyten);
                    eliasLuyten.addTag("boggled_gate_quest_eliasLuyten");

                    dialog.dismiss();
                    break;
                case LEAVE_NO:
                    Global.getSector().getPlayerFleet().removeTag("boggled_gate_quest_stage1");
                    Global.getSector().getPlayerFleet().addTag("boggled_gate_quest_stage4");

                    Iterator allIntel = Global.getSector().getIntelManager().getIntel().iterator();
                    while(allIntel.hasNext())
                    {
                        BaseIntelPlugin intel = (BaseIntelPlugin)allIntel.next();
                        if(intel instanceof BoggledGateQuestIntel)
                        {
                            intel.endAfterDelay();
                        }
                    }

                    dialog.dismiss();
                    break;
            }
        }
    }

    enum OptionId
    {
        INIT,
        ANSWER_HAIL,
        QUESTION_IDENTITY,
        WHAT_WANT,
        YES_RETURN,
        NO_RETURN,
        WHY_SPEAK_RIDDLES,
        REACTIVATE_GATES,
        PLAYER_ASSIGNMENT,
        ACCEPT_MISSION,
        INQUIRE_COMPENSATION,
        VERY_WELL,
        DECLINE_MISSION,
        LUDD_SMASH,
        LEAVE_YES,
        LEAVE_NO,
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