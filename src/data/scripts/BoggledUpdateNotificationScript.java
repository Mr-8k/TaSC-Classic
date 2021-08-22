package data.scripts;

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
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BoggledUpdateNotificationScript implements EveryFrameScript
{
    private boolean hasWarned = false;
    private float timeUntilWarn = .75f;

    public BoggledUpdateNotificationScript() { }

    private void checkForUpdates()
    {
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        try
        {
            URL url = new URL("https://pastebin.com/raw/WTd8sqnW");

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String latestVersion = in.readLine();
            in.close();

            String clientVersion = Global.getSettings().getModManager().getModSpec("Terraforming and Station Construction").getVersion();

            if(latestVersion.equals(clientVersion))
            {
                ui.addMessage("You have the latest version of TASC.", Color.GREEN);
            }
            else
            {
                ui.addMessage("There is an update available for TASC!", Color.RED);
            }
        }
        catch (MalformedURLException e)
        {
            ui.addMessage("MalformedURLException thrown trying to check for updates.", Color.RED);
        }
        catch (IOException e)
        {
            ui.addMessage("IOException thrown trying to check for updates.", Color.RED);
        }
    }

    public boolean isDone()
    {
        if(hasWarned)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean runWhilePaused()
    {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        // Don't do anything while in a menu/dialog
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (Global.getSector().isInNewGameAdvance() || ui.isShowingDialog() || ui.isShowingMenu())
        {
            return;
        }

        // On first game load, warn about any updates available
        if (!hasWarned && timeUntilWarn <= 0f)
        {
            checkForUpdates();
            hasWarned = true;
        }
        else
        {
            timeUntilWarn -= amount;
        }
    }
}