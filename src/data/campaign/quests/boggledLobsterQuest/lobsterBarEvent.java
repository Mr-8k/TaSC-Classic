package data.campaign.quests.boggledLobsterQuest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.PlanetaryShieldBarEvent;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
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
import data.campaign.econ.boggledTools;

import java.lang.Object;
import java.util.*;
import java.lang.String;

public class lobsterBarEvent extends BaseBarEventWithPerson
{
    public lobsterBarEvent()
    {
    }

    protected void regen(MarketAPI market)
    {
        if (this.market == null || this.market != market)
        {
            super.regen(market);
            FullName name = new FullName("Gregory", "Simmons", FullName.Gender.MALE);
            this.person.setName(name);
            this.person.setPortraitSprite(Global.getSettings().getSpriteName("boggled", "portrait_lobster_pirate"));

            this.person.setFaction("pirates");
            this.person.setRankId(Ranks.CITIZEN);
            this.person.setPostId(Ranks.POST_SMUGGLER);
        }
    }

    /**
     * True if this event may be selected to be offered to the player,
     * or false otherwise.
     */
    public boolean shouldShowAtMarket(MarketAPI market)
    {
        if(!super.shouldShowAtMarket(market))
        {
            return false;
        }

        if(market.getId().equals("umbra"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Set up the text that appears when the player goes to the bar
     * and the option for them to start the conversation.
     */
    //@Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);

        // Calling super does nothing in this case, but is good practice because a subclass should
        // implement all functionality of the superclass (and usually more)
        this.regen(dialog.getInteractionTarget().getMarket()); // Sets field variables and creates a random person

        // Display the text that will appear when the player first enters the bar and looks around
        dialog.getTextPanel().addPara("A group of pirates sitting at a table are videoconferencing with someone via their TriPad. They appear to be trying to negotiate the release of a captured comrade.");

        // Display the option that lets the player choose to investigate our bar event
        dialog.getOptionPanel().addOption("See if the pirates need any assistance \"liberating\" their associate", this);
    }

    /**
     * Called when the player chooses this event from the list of options shown when they enter the bar.
     */

    //@Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

        // If player starts our event, then backs out of it, `done` will be set to true.
        // If they then start the event again without leaving the bar, we should reset `done` to false.
        this.done = false;

        // The boolean is for whether to show only minimal person information. True == minimal
        dialog.getVisualPanel().showPersonInfo(this.person, true);

        // Launch into our event by triggering the "INIT" option, which will call `optionSelected()`
        this.optionSelected((String)null, lobsterBarEvent.OptionId.INIT);
    }

    /**
     * This method is called when the player has selected some option for our bar event.
     *
     * @param optionText the actual text that was displayed on the selected option
     * @param optionData the value used to uniquely identify the option
     */
    @Override
    public void optionSelected(String optionText, Object optionData)
    {
        if (optionData instanceof OptionId)
        {
            SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
            // Clear shown options before we show new ones
            dialog.getOptionPanel().clearOptions();

            // Handle all possible options the player can choose
            switch ((OptionId) optionData)
            {
                case INIT:
                    // The player has chosen to walk over to the crowd, so let's tell them what happens.
                    dialog.getTextPanel().addPara("As you approach the pirates, one of them stands up and turns to face you. He picks up a gauss rifle that was leaning against the table. \"Step back,\" he commands.");
                    dialog.getTextPanel().addPara("\"Relax Artyom.\" The pirate who spoke presses a button on the TriPad, ending the videoconference while the person on the other end is in the middle of a sentence. \"Our friend here looks like they know how to handle themselves. Perhaps they can be of assistance to us.\" He tilts his head to look up at you, but his face is obscured by his helmet. \"You can call me Mr. Simmons. I'm in the import-export business.\"");

                    // And give them some options on what to do next
                    dialog.getOptionPanel().addOption("Inquire about his captured associate", OptionId.PROCEED_1);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                case PROCEED_1:
                    dialog.getTextPanel().addPara("\"One of our 'business partners' was arrested on Volturn last week. She was at the spaceport preparing to return to Umbra when the secret police nabbed her. She's been sent to a labor camp on Cruor.\" Simmons shifts to look at the now-dormant TriPad. \"I've been trying to bribe officials on Cruor to get her released, but as soon as I mention her name they refuse to cooperate.\"");
                    dialog.getOptionPanel().addOption("Ask Simmons why his associate was arrested by the secret police", OptionId.PROCEED_2);
                    dialog.getOptionPanel().addOption("Offer to liberate his associate for a fee", OptionId.PROCEED_3);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                case PROCEED_2:
                    playerFleet.addTag("boggled_lobster_quest_playerAskedAboutWhyVegaArrested");
                    dialog.getTextPanel().addPara("Simmons gestures towards one of his companions. \"You had Volturnian lobster to eat. How much did it cost?\"");
                    dialog.getTextPanel().addPara("The other pirate smirks. \"A lot.\"");
                    dialog.getTextPanel().addPara("Simmons picks up the TriPad and carefully places it in a pocket in his body armor, then turns his attention back to you. \"Volturnian lobsters are genetically engineered to thrive in the conditions on Volturn. Farming them is extremely lucrative because that is the only place in the Sector they can be grown. Volturn has maintained this monopoly since the Collapse because only they possess the Domain-era genetic modification equipment that created these lobsters. My business partner was trying to steal that equipment from the Volturnians.\"");
                    dialog.getOptionPanel().addOption("\"And the secret police stopped her?\"", OptionId.PROCEED_2_1);
                    break;
                case PROCEED_2_1:
                    dialog.getTextPanel().addPara("\"Her last report indicated she was successful in stealing the equipment, and that she had paid a smuggler to ship it off Volturn. I need to talk to her and figure out where it was sent. That equipment is immensely valuable to any government in possession of a planet with an ocean. They could create their own lobsters uniquely suited for their own planets and take market share away from Volturn.\"");
                    dialog.getTextPanel().addPara("Simmons pauses. You can't see his face, but you get the feeling he's smiling. \"So, are you in?\"");
                    dialog.getOptionPanel().addOption("Offer to liberate his associate for a fee", OptionId.PROCEED_3);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                case PROCEED_3:
                    dialog.getTextPanel().addPara("\"Excellent. In light of the... high risk nature of this assignment, I think 250,000 credits is a fair sum to pay for her rescue. Do you agree?\"");
                    dialog.getOptionPanel().addOption("Accept the offer", OptionId.ACCEPT_QUEST);
                    dialog.getOptionPanel().addOption("Demand a larger reward", OptionId.PROCEED_3_1);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                case PROCEED_3_1:
                    playerFleet.addTag("boggled_lobster_quest_playerDemandedMoreMoney");
                    dialog.getTextPanel().addPara("Simmons says nothing for a few seconds. Out of the corner of your eye, you see Artyom place his finger on the trigger of the gauss rifle. In a flat tone, Simmons says \"How about 500,000 credits? Is that enough for you?\"");
                    dialog.getOptionPanel().addOption("Accept the offer", OptionId.ACCEPT_QUEST);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                case ACCEPT_QUEST:
                    dialog.getTextPanel().addPara("\"I'm glad we could reach a deal.\" Simmons stands up and you shake hands with him. \"Her name is Elena Vega. My men will transmit the coordinates of the labor camp to your TriPad. Good luck.\"");
                    noContinue = false;
                    done = true;
                    BarEventManager.getInstance().notifyWasInteractedWith(this);

                    //Adds tag that blocks the quest from ever being offered again
                    playerFleet.addTag("boggled_lobster_quest_playerBeganQuest");

                    //Global.getSector().getEntityById("cruor").getMarket().setFactionId(playerFleet.getFaction().getId());
                    //Global.getSector().getEntityById("cruor").getMarket().setPlayerOwned(true);

                    //Give the player the quest in their intel and set them to stage 1
                    playerFleet.addTag("boggled_lobster_quest_stage1");
                    Global.getSector().getIntelManager().addIntel(new lobsterIntel());

                    //Create the labor camp commandant person and add them to the Cruor comm directory
                    PersonAPI boggledLobsterQuestCommandant = createPerson();
                    FullName commandantName = new FullName("Walter", "Horn", FullName.Gender.MALE);
                    boggledLobsterQuestCommandant.setName(commandantName);
                    boggledLobsterQuestCommandant.setPortraitSprite(Global.getSettings().getSpriteName("boggled", "portrait_cruor_commandant"));

                    boggledLobsterQuestCommandant.setFaction("sindrian_diktat");
                    boggledLobsterQuestCommandant.setRankId(Ranks.GROUND_COLONEL);
                    boggledLobsterQuestCommandant.setPostId(Ranks.POST_BASE_COMMANDER);
                    boggledTools.getPlanetTokenForQuest("Askonia", "cruor").getMarket().getCommDirectory().addPerson(boggledLobsterQuestCommandant);
                    boggledLobsterQuestCommandant.addTag("boggled_lobster_quest_commandant");

                    //Add the fireVegaRescue tag to Cruor
                    boggledTools.getPlanetTokenForQuest("Askonia", "cruor").addTag("boggled_lobster_quest_fireVegaRescue");

                    Misc.makeImportant(boggledTools.getPlanetTokenForQuest("Askonia", "cruor"), "boggled_cruor_importance");

                    break;
                case LEAVE:
                    // They've chosen to leave, so end our interaction. This will send them back to the bar.
                    // If noContinue is false, then there will be an additional "Continue" option shown
                    // before they are returned to the bar. We don't need that.
                    noContinue = true;
                    done = true;

                    // Removes this event from the bar so it isn't offered again
                    //BarEventManager.getInstance().notifyWasInteractedWith(this);
                    break;
                case LEAVE_FINAL:
                    // They've chosen to leave, so end our interaction. This will send them back to the bar.
                    // If noContinue is false, then there will be an additional "Continue" option shown
                    // before they are returned to the bar. We don't need that.
                    noContinue = true;
                    done = true;

                    // Removes this event from the bar so it isn't offered again
                    BarEventManager.getInstance().notifyWasInteractedWith(this);

                    //Adds tag that blocks the quest from ever being offered again
                    playerFleet.addTag("boggled_lobster_quest_playerBeganQuest");
                    break;
            }
        }
    }

    enum OptionId
    {
        INIT,
        PROCEED_1,
        PROCEED_2,
        PROCEED_2_1,
        PROCEED_3,
        PROCEED_3_1,
        ACCEPT_QUEST,
        LEAVE,
        LEAVE_FINAL,
    }
}