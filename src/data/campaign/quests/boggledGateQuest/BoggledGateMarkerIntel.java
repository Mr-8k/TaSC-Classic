package data.campaign.quests.boggledGateQuest;

import java.lang.String;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.PlanetaryShieldIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.*;
import com.fs.starfarer.combat.entities.terrain.Planet;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;

import java.awt.*;
import java.util.*;

public class BoggledGateMarkerIntel extends BaseIntelPlugin
{
    private SectorEntityToken linkedGateSectorEntityToken;

    public BoggledGateMarkerIntel(SectorEntityToken gateToken) { linkedGateSectorEntityToken = gateToken;}

    public boolean shouldRemoveIntel() {
        return super.shouldRemoveIntel();
    }

    protected void notifyEnding() {
        super.notifyEnding();
    }

    public String getSmallDescriptionTitle() {
        return this.getName();
    }

    public SectorEntityToken getMapLocation(SectorMapAPI map)
    {
        return linkedGateSectorEntityToken;
    }

    public String getName()
    {
        return linkedGateSectorEntityToken.getName();
    }

    public SectorEntityToken getLinkedGateSectorEntityToken()
    {
        return linkedGateSectorEntityToken;
    }

    @Override
    public String getIcon()
    {
        return Global.getSettings().getSpriteName("boggled", "penelope_secret_intel");
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = this.getTitleColor(mode);
        info.setParaSmallInsignia();
        info.addPara(this.getName(), c, 0.0F);
        info.setParaFontDefault();
        this.addBulletPoints(info, mode);
    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode)
    {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3.0F;
        float opad = 10.0F;
        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC)
        {
            initPad = opad;
        }

        this.getBulletColorForMode(mode);
        this.bullet(info);

        info.addPara("Controlled by: %s", initPad, linkedGateSectorEntityToken.getFaction().getBaseUIColor(), new String[]{linkedGateSectorEntityToken.getFaction().getDisplayName()});

        initPad = 0.0F;
        this.unindent(info);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height)
    {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3.0F;
        float opad = 10.0F;

        info.addPara("Gate jumps are performed using abilities. Add the abilities to your ability bar to use them.", opad);

        this.addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map)
    {
        return new HashSet<>(Arrays.asList(BoggledStationConstructionIDs.BoggledStationConstructionIndustryIDs.INTEL_ASTRAL_GATE));
    }
}