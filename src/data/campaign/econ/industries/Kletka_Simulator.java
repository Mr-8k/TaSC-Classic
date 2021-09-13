package data.campaign.econ.industries;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;

public class Kletka_Simulator extends BaseIndustry
{
    public boolean canBeDisrupted()
    {
        return true;
    }

    public CargoAPI generateCargoForGatheringPoint(Random random)
    {
        boolean shortage = false;
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit("domain_artifacts");
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        if (!this.isFunctional() || shortage)
        {
            return null;
        }
        else
        {
            CargoAPI result = Global.getFactory().createCargo(true);
            result.clear();
            float roll = random.nextFloat() * 100f;

            if(this.isImproved())
            {
                if (this.aiCoreId == null) {
                    if (roll > 85f) {
                        result.addCommodity("beta_core", 1f);
                    } else if (roll > 40f) {
                        result.addCommodity("gamma_core", 1f);
                    }
                } else if (this.aiCoreId.equals("alpha_core")) {
                    if (roll > 65f) {
                        result.addCommodity("alpha_core", 1f);
                    } else if (roll > 30f) {
                        result.addCommodity("beta_core", 1f);
                    } else {
                        result.addCommodity("gamma_core", 1f);
                    }
                } else if (this.aiCoreId.equals("beta_core")) {
                    if (roll > 85f) {
                        result.addCommodity("alpha_core", 1f);
                    } else if (roll > 50f) {
                        result.addCommodity("beta_core", 1f);
                    } else if (roll > 20f) {
                        result.addCommodity("gamma_core", 1f);
                    }
                } else if (this.aiCoreId.equals("gamma_core")) {
                    if (roll > 90f) {
                        result.addCommodity("alpha_core", 1f);
                    } else if (roll > 60f) {
                        result.addCommodity("beta_core", 1f);
                    } else if (roll > 35f) {
                        result.addCommodity("gamma_core", 1f);
                    }
                }
            }
            else {
                if (this.aiCoreId == null) {
                    if (roll > 95f) {
                        result.addCommodity("beta_core", 1f);
                    } else if (roll > 50f) {
                        result.addCommodity("gamma_core", 1f);
                    }
                } else if (this.aiCoreId.equals("alpha_core")) {
                    if (roll > 70f) {
                        result.addCommodity("alpha_core", 1f);
                    } else if (roll > 40f) {
                        result.addCommodity("beta_core", 1f);
                    } else if (roll > 10f) {
                        result.addCommodity("gamma_core", 1f);
                    }
                } else if (this.aiCoreId.equals("beta_core")) {
                    if (roll > 90f) {
                        result.addCommodity("alpha_core", 1f);
                    } else if (roll > 60f) {
                        result.addCommodity("beta_core", 1f);
                    } else if (roll > 30f) {
                        result.addCommodity("gamma_core", 1f);
                    }
                } else if (this.aiCoreId.equals("gamma_core")) {
                    if (roll > 95f) {
                        result.addCommodity("alpha_core", 1f);
                    } else if (roll > 70f) {
                        result.addCommodity("beta_core", 1f);
                    } else if (roll > 45f) {
                        result.addCommodity("gamma_core", 1f);
                    }
                }
            }

            return result;
        }
    }

    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Massively improves AI core training methodology.", 0.0F, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Massively improves AI core training methodology.", opad, highlight, "");
        }
    }

    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Greatly improves AI core training methodology.", opad, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Greatly improves AI core training methodology.", opad, highlight, "");
        }
    }

    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Improves AI core training methodology.", opad, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Improves AI core training methodology.", opad, highlight, "");
        }
    }

    public void applyAICoreToIncomeAndUpkeep()
    {
        //Prevent AI cores from modifying income and upkeep
    }

    public void updateAICoreToSupplyAndDemandModifiers()
    {
        //Prevent AI cores from modifying supply and demand
    }

    @Override
    public void apply()
    {
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            this.demand("domain_artifacts", 4);
        }

        super.apply(false);
        super.applyIncomeAndUpkeep(3);

        if(Global.getSettings().getBoolean("boggledKletkaSimulatorTemperatureBasedUpkeep"))
        {
            MarketAPI market = this.market;
            LinkedHashSet<String> suppCond = market.getSuppressedConditions();
            if(market.hasCondition("very_cold"))
            {
                if(!suppCond.contains(Conditions.VERY_COLD))
                {
                    getUpkeep().modifyMult("temperature", 0.25f, "Extreme cold");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Extreme cold (suppressed)");
                }
            }
            else if(market.hasCondition("cold"))
            {
                if(!suppCond.contains(Conditions.COLD))
                {
                    getUpkeep().modifyMult("temperature", 0.5f, "Cold");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Cold (suppressed)");
                }
            }
            else if(market.hasCondition("hot"))
            {
                if(!suppCond.contains(Conditions.HOT))
                {
                    getUpkeep().modifyMult("temperature", 2.0f, "Hot");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Hot (suppressed)");
                }
            }
            else if(market.hasCondition("very_hot"))
            {
                if(!suppCond.contains(Conditions.VERY_HOT))
                {
                    getUpkeep().modifyMult("temperature", 4.0f, "Extreme heat");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Extreme heat (suppressed)");
                }
            }
            else if(market.getPrimaryEntity().hasTag("station"))
            {
                getUpkeep().modifyMult("temperature", 4.0f, "Station");
            }
            else
            {
                getUpkeep().unmodifyMult("temperature");
            }
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }


    @Override
    public boolean isAvailableToBuild()
    {
        return Global.getSettings().getBoolean("boggledDomainTechContentEnabled") && Global.getSettings().getBoolean("boggledKletkaSimulatorEnabled");
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    public float getPatherInterest() { return 10.0F; }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(Global.getSettings().getBoolean("boggledKletkaSimulatorTemperatureBasedUpkeep"))
        {
            tooltip.addPara("Supercomputers will melt themselves without adequate cooling systems. Operating costs are lowest on very cold worlds and highest on very hot worlds and stations.", opad, highlight, "");
        }

        boolean shortage = false;
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit("domain_artifacts");
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED || isBuilding())
        {
            return;
        }
        else if(isDisrupted())
        {
            tooltip.addPara("Current chances to produce an AI core at the end of the month: %s", opad, bad, "           None (disrupted)");
            return;
        }
        else if(shortage)
        {
            tooltip.addPara("Current chances to produce an AI core at the end of the month: %s", opad, bad, "           None (shortage of Domain-era artifacts)");
            return;
        }

        if(isImproved())
        {
            if(this.aiCoreId == null)
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "15%","45%","40%");
            }
            else if(this.aiCoreId.equals("gamma_core"))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "10%","30%","25%","35%");
            }
            else if(this.aiCoreId.equals("beta_core"))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "15%","35%","30%","20%");
            }
            else if(this.aiCoreId.equals("alpha_core"))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "35%","35%","30%","0%");
            }
        }
        else
        {
            if(this.aiCoreId == null)
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "5%","45%","50%");
            }
            else if(this.aiCoreId.equals("gamma_core"))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "5%","25%","25%","45%");
            }
            else if(this.aiCoreId.equals("beta_core"))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "10%","30%","30%","30%");
            }
            else if(this.aiCoreId.equals("alpha_core"))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "30%","30%","30%","10%");
            }
        }
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode)
    {
        return Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled");
    }

    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            float opad = 10.0F;
            Color highlight = Misc.getHighlightColor();

            tooltip.addPara("Kletka Simulators always demand %s Domain-era artifacts regardless of market size.", opad, highlight, "4");
        }
    }

    @Override
    public boolean canImprove() {
        return true;
    }

    protected void applyImproveModifiers()
    {
        //Handled above in the cargo function
    }

    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("AI core training methodology improved.", 0f, highlight, "");
        }
        else
        {
            info.addPara("Improves AI core training methodology.", 0f, highlight, "");
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
