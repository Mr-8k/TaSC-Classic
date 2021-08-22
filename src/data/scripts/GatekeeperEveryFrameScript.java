package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

public class GatekeeperEveryFrameScript implements EveryFrameScript
{
    private StarSystemAPI system;

    public GatekeeperEveryFrameScript(StarSystemAPI system)
    {
        this.system = system;
    }

    public boolean isDone()
    {
        SectorEntityToken systemCenter = system.getCenter();

        //Remove script if there is no gate in the system
        if(!boggledTools.gatekeeperStationInSystem(systemCenter) && !boggledTools.astralGateInSystem(systemCenter))
        {
            return true;
        }

        return false;
    }

    public boolean runWhilePaused()
    {
        return false;
    }

    public void advance(float var1)
    {
        //Get station and market objects
        SectorEntityToken systemCenter = system.getCenter();
        SectorEntityToken gatekeeperStation = boggledTools.getClosestGatekeeperStationInSystem(systemCenter);
        if(gatekeeperStation == null)
        {
            return;
        }
        MarketAPI market = gatekeeperStation.getMarket();

        //Update ownership of the gate if station changes hands
        SectorEntityToken astralGate = gatekeeperStation.getOrbitFocus();

        if(!astralGate.getFaction().getId().equals(market.getFactionId()))
        {
            astralGate.setFaction(market.getFactionId());
            boggledTools.updateListofActiveAstralGates(true);
        }
    }
}