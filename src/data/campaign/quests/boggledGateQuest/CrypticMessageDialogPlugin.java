package data.campaign.quests.boggledGateQuest;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

import java.util.Map;

public class CrypticMessageDialogPlugin implements InteractionDialogPlugin
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
        this.optionSelected(null, CrypticMessageDialogPlugin.OptionId.INIT);
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
                    dialog.getTextPanel().addPara("Your flagship receives an unexpected transmission. It's an audio file.");

                    dialog.getOptionPanel().addOption("Listen to the message", OptionId.PLAY);
                    break;
                case PLAY:
                    dialog.getTextPanel().addPara(optionText, Misc.getBasePlayerColor());

                    dialog.getTextPanel().addPara("\"I am Odysseus, marooned on the island of the one who is captain over the winds. I seek to return home to my beloved Penelope, but I remain trapped here, in need of the help of another.\"");

                    dialog.getTextPanel().addPara("Your comms officer scratches his head. \"Whoever transmitted that message bounced it off so many relays that I can't trace the origin. It sounds like an invitation, but to where?\"");

                    dialog.getOptionPanel().addOption("Continue", OptionId.LEAVE);
                    break;
                case LEAVE:
                    dialog.dismiss();
                    Global.getSector().getIntelManager().addIntel(new BoggledGateQuestIntel());
                    Global.getSector().getPlayerFleet().addScript(new WindsEveryFrameScript());
                    break;
            }
        }
    }

    enum OptionId
    {
        INIT,
        PLAY,
        LEAVE
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