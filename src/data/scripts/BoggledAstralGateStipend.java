package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class BoggledAstralGateStipend implements EconomyTickListener, TooltipCreator
{
    public static float DURATION = 1115.0F;
    public static int STIPEND = Global.getSettings().getInt("boggledGiftGateMonthlyPayment");
    protected long startTime = 0L;
    protected String factionID = null;
    protected String systemID = null;

    public BoggledAstralGateStipend(String faction, String system)
    {
        Global.getSector().getListenerManager().addListener(this);
        this.startTime = Global.getSector().getClock().getTimestamp();
        this.factionID = faction;
        this.systemID = system;
    }

    public void reportEconomyTick(int iterIndex)
    {
        if(Global.getSector().getFaction(factionID).getRelationshipLevel(Global.getSector().getPlayerFaction().getId()) == RepLevel.HOSTILE || Global.getSector().getFaction(factionID).getRelationshipLevel(Global.getSector().getPlayerFaction().getId()) == RepLevel.VENGEFUL)
        {
            Global.getSector().getListenerManager().removeListener(this);
        }

        int lastIterInMonth = (int)Global.getSettings().getFloat("economyIterPerMonth") - 1;
        if (iterIndex == lastIterInMonth)
        {
            float daysActive = Global.getSector().getClock().getElapsedDaysSince(this.startTime);
            if (daysActive <= DURATION)
            {
                MonthlyReport report = SharedData.getData().getCurrentReport();
                int stipend = this.getStipend();
                FDNode fleetNode = report.getNode(new String[]{MonthlyReport.FLEET});
                FDNode stipendNode = report.getNode(fleetNode, new String[]{"boggledGateStipend_" + this.systemID});
                stipendNode.income = (float)stipend;
                stipendNode.name = Global.getSector().getStarSystem(this.systemID).getBaseName() + " Astral Gate shared revenue payment";
                stipendNode.icon = Global.getSettings().getSpriteName("income_report", "generic_income");
                stipendNode.tooltipCreator = this;
            }
            else
            {
                Global.getSector().getListenerManager().removeListener(this);
            }
        }
    }

    protected int getStipend()
    {
        if(Global.getSector().getFaction(factionID).getRelationshipLevel(Global.getSector().getPlayerFaction().getId()) == RepLevel.HOSTILE || Global.getSector().getFaction(factionID).getRelationshipLevel(Global.getSector().getPlayerFaction().getId()) == RepLevel.VENGEFUL)
        {
            Global.getSector().getListenerManager().removeListener(this);
        }

        return STIPEND;
    }

    public void reportEconomyMonthEnd() { }

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam)
    {
        float daysActive = Global.getSector().getClock().getElapsedDaysSince(this.startTime);
        tooltip.addPara("A monthly payment of %s credits from " + Global.getSector().getFaction(this.factionID).getDisplayNameWithArticle() + " that represents your portion of the revenue from the " + Global.getSector().getStarSystem(this.systemID).getBaseName() + " Astral Gate.", 0.0F, Misc.getHighlightColor(), new String[]{"75,000"});
        float rem = DURATION - daysActive;
        int months = (int)(rem / 30.0F);
        if (months > 0)
        {
            tooltip.addPara("You should continue receiving shared revenue payments for another %s months.", 10.0F, Misc.getHighlightColor(), new String[]{"" + months});
        }
        else if (months <= 0)
        {
            tooltip.addPara("This month's payment was the last.", 10.0F);
        }
    }

    public float getTooltipWidth(Object tooltipParam) {
        return 450.0F;
    }

    public boolean isTooltipExpandable(Object tooltipParam) {
        return false;
    }
}
