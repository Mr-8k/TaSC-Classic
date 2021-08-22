package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import java.awt.*;

public class Boggled_Remnant_Station extends OrbitalStation
{
    @Override
    public void advance(float amount)
    {
        super.advance(amount);


    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(Global.getSettings().getBoolean("boggledRemnantStationEnabled") && super.isAvailableToBuild())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public String getUnavailableReason() {
        return "Requires a functional spaceport";
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    @Override
    public void apply() {
        int size = 7;

        //this.modifyStabilityWithBaseMod();
        this.market.getStability().modifyFlat(this.getModId(), 3.0f, "Autonomous AI battlestation");
        this.applyIncomeAndUpkeep((float)size);
        this.demand("supplies", size);

        //this.market.getStats().getDynamic().getMod("ground_defenses_mod").modifyMult(this.getModId(), 3.0f, this.getNameForModifier());
        this.market.getStats().getDynamic().getMod("ground_defenses_mod").modifyMult(this.getModId(), 3.0f, "Autonomous AI battlestation");
        this.matchCommanderToAICore(this.aiCoreId);
        if (!this.isFunctional()) {
            this.supply.clear();
            this.unapply();
        } else {
            this.applyCRToStation();
        }

    }

    @Override
    public void unapply() {
        this.unmodifyStabilityWithBaseMod();
        this.matchCommanderToAICore((String)null);
        this.market.getStats().getDynamic().getMod("ground_defenses_mod").unmodifyMult(this.getModId());
    }

    @Override
    protected int getBaseStabilityMod()
    {
        int stabilityMod = 3;

        return stabilityMod;
    }

    @Override
    protected float getCR() {
        float deficit = (float)(Integer)this.getMaxDeficit(new String[]{"supplies"}).two;
        float demand = (float)Math.max(0, this.getDemand("supplies").getQuantity().getModifiedInt());
        if (deficit < 0.0F) {
            deficit = 0.0F;
        }

        if (demand < 1.0F) {
            demand = 1.0F;
            deficit = 0.0F;
        }

        float q = Misc.getShipQuality(this.market);
        if (q < 0.0F) {
            q = 0.0F;
        }

        if (q > 1.0F) {
            q = 1.0F;
        }

        float d = (demand - deficit) / demand;
        if (d < 0.0F) {
            d = 0.0F;
        }

        if (d > 1.0F) {
            d = 1.0F;
        }

        float cr = 0.5F + 0.5F * Math.min(d, q);
        if (cr > 1.0F) {
            cr = 1.0F;
        }

        return cr;
    }

    @Override
    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return this.getMaxDeficit(new String[]{"supplies"});
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        if (mode != IndustryTooltipMode.NORMAL || this.isFunctional()) {
            Color h = Misc.getHighlightColor();
            float opad = 10.0F;
            float cr = this.getCR();
            tooltip.addPara("Station combat readiness: %s", opad, h, new String[]{Math.round(cr * 100.0F) + "%"});
            this.addStabilityPostDemandSectionBoggledRemnantStation(tooltip, hasDemand, mode);
            float bonus = 2.0f;

            this.addGroundDefensesImpactSectionBoggledRemnantStation(tooltip, bonus, new String[]{"supplies"});
        }

    }

    protected void addStabilityPostDemandSectionBoggledRemnantStation(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        Color h = Misc.getHighlightColor();
        float opad = 10.0F;
        MutableStat fake = new MutableStat(0.0F);
        int stabilityMod = this.getBaseStabilityMod();
        int stabilityPenalty = this.getStabilityPenalty();
        if (stabilityPenalty > stabilityMod) {
            stabilityPenalty = stabilityMod;
        }

        String str = getDeficitText((String)this.getStabilityAffectingDeficit().one);
        //fake.modifyFlat("1", (float)stabilityMod, this.getNameForModifier());
        fake.modifyFlat("1", (float)stabilityMod, "Autonomous AI Battlestation");
        if (stabilityPenalty != 0) {
            fake.modifyFlat("2", (float)(-stabilityPenalty), str);
        }

        int total = stabilityMod - stabilityPenalty;
        String totalStr = "+" + total;
        if (total < 0) {
            totalStr = "" + total;
            h = Misc.getNegativeHighlightColor();
        }

        float pad = 3.0F;
        if (total >= 0) {
            tooltip.addPara("Stability bonus: %s", opad, h, new String[]{totalStr});
        } else {
            tooltip.addPara("Stability penalty: %s", opad, h, new String[]{totalStr});
        }

        tooltip.addStatModGrid(400.0F, 35.0F, opad, pad, fake, new TooltipMakerAPI.StatModValueGetter()
        {
            public String getPercentValue(MutableStat.StatMod mod) {
                return null;
            }

            public String getMultValue(MutableStat.StatMod mod) {
                return null;
            }

            public Color getModColor(MutableStat.StatMod mod) {
                return mod.value < 0.0F ? Misc.getNegativeHighlightColor() : null;
            }

            public String getFlatValue(MutableStat.StatMod mod) {
                return null;
            }
        });
    }

    protected void addGroundDefensesImpactSectionBoggledRemnantStation(TooltipMakerAPI tooltip, float bonus, String... commodities) {
        Color h = Misc.getHighlightColor();
        float opad = 10.0F;
        MutableStat fake = new MutableStat(1.0F);
        //fake.modifyFlat("1", bonus, this.getNameForModifier());
        fake.modifyFlat("1", bonus, "Autonomous AI Battlestation");
        float mult;
        String totalStr;
        if (commodities != null) {
            mult = this.getDeficitMult(commodities);
            if (mult != 1.0F) {
                totalStr = (String)this.getMaxDeficit(commodities).one;
                fake.modifyFlat("2", -(1.0F - mult) * bonus, getDeficitTextBoggledRemnantStation(totalStr));
            }
        }

        mult = Misc.getRoundedValueFloat(fake.getModifiedValue());
        totalStr = "×" + mult;
        if (mult < 1.0F) {
            h = Misc.getNegativeHighlightColor();
        }

        float pad = 3.0F;
        tooltip.addPara("Ground defense strength: %s", opad, h, new String[]{totalStr});
        tooltip.addStatModGrid(400.0F, 35.0F, opad, pad, fake, new TooltipMakerAPI.StatModValueGetter()
        {
            public String getPercentValue(MutableStat.StatMod mod) {
                return null;
            }

            public String getMultValue(MutableStat.StatMod mod) {
                return null;
            }

            public Color getModColor(MutableStat.StatMod mod) {
                return mod.value < 0.0F ? Misc.getNegativeHighlightColor() : null;
            }

            public String getFlatValue(MutableStat.StatMod mod) {
                String r = Misc.getRoundedValue(mod.value);
                return mod.value >= 0.0F ? "+" + r : r;
            }
        });
    }

    public static String getDeficitTextBoggledRemnantStation(String commodityId) {
        if (commodityId == null) {
            return "Various shortages";
        } else {
            CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
            return Misc.ucFirst(spec.getName().toLowerCase() + " shortage");
        }
    }

    @Override
    protected int getHumanCommanderLevel()
    {
        return Global.getSettings().getInt("tier3StationOfficerLevel");
    }

    @Override
    public float getPatherInterest() {
        return 10.0F;
    }
}
