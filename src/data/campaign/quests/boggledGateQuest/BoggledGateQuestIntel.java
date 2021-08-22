package data.campaign.quests.boggledGateQuest;

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
import data.campaign.econ.boggledTools;

import java.awt.*;
import java.util.*;
import java.lang.String;

public class BoggledGateQuestIntel extends BaseIntelPlugin
{
    public BoggledGateQuestIntel() { }

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
        if (getQuestStage() == 1)
        {
            return null;
        }
        else if (getQuestStage() == 2)
        {
            return boggledTools.getPlanetTokenForQuest("Galatia", "ancyra");
        }
        else if (getQuestStage() == 3)
        {
            return boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4");
        }
        else if (getQuestStage() == 4)
        {
            return null;
        }
        else if (getQuestStage() == 5)
        {
            return null;
        }
        else if (getQuestStage() == 6)
        {
            return null;
        }
        else if (getQuestStage() == 7)
        {
            return boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4");
        }
        else
        {
            return null;
        }
    }

    public String getName()
    {
        return !this.isEnded() && !this.isEnding() ? "Penelope's Secret" : "Penelope's Secret - Completed";
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

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        if (getQuestStage() == 1)
        {
            info.addPara("Find the source of the transmission", initPad, playerFleet.getFaction().getBaseUIColor(), new String[]{""});
        }
        else if (getQuestStage() == 2)
        {
            info.addPara("Contact Elias Luyten on %s", initPad, playerFleet.getFaction().getBaseUIColor(), new String[]{boggledTools.getPlanetTokenForQuest("Galatia", "ancyra").getMarket().getName()});
        }
        else if (getQuestStage() == 3)
        {
            info.addPara("Return to Odysseus on %s", initPad, Global.getSector().getEntityById("penelope4").getMarket().getFaction().getBaseUIColor(), new String[]{boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4").getMarket().getName()});
        }
        else if (getQuestStage() == 4)
        {
            //Do nothing
        }
        else if (getQuestStage() == 5)
        {
            //Do nothing
        }
        else if (getQuestStage() == 6)
        {
            //Do nothing
        }
        else if (getQuestStage() == 7)
        {
            info.addPara("Return to Odysseus on %s", initPad, Global.getSector().getEntityById("penelope4").getMarket().getFaction().getBaseUIColor(), new String[]{boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4").getMarket().getName()});
        }

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
        if (getQuestStage() == 0)
        {
            info.addPara("Quest stage bugged. Please tell Boggled about this on the forums.", opad);
        }
        else if (getQuestStage() == 1)
        {
            info.addPara("Someone calling themselves \"Odysseus\" transmitted a cryptic message to your flagship from an unknown location. Perhaps \"Penelope\" or \"the one who is captain over the winds\" refer to a real person or place in the Sector.", opad);
        }
        else if (getQuestStage() == 2)
        {
            info.addPara("Odysseus turned out to be an artificial intelligence created by the Eridani-Utopia Terraforming Corporation. It seeks to reactivate the gate network so it can return to the world where it was created.\n\nYou've been tasked with obtaining classified Hegemony research data on the gates, in the hopes that the AI can find a solution to whatever problem caused the gates to shut down. It identified Elias Luyten of Ancyra as someone likely to have access to the data, and suggested trying to bribe him to get it.", opad);
        }
        else if (getQuestStage() == 3)
        {
            info.addPara("Chancellor Luyten proved to be eccentric, but you managed to reach a deal to obtain the Hegemony gate research data.\n\nYou let him perform experiments using the recall device aboard your Astral-class carrier, and Luyten promised to transfer the Hegemony data once Odysseus completes the blueprints for an enormous station hosting a scaled-up version of the recall device.", opad);
        }
        else if (getQuestStage() == 4)
        {
            info.addPara("Odysseus turned out to be an artificial intelligence created by the Eridani-Utopia Terraforming Corporation.\n\nYou refused to do the AI core's bidding.", opad);
        }
        else if (getQuestStage() == 5)
        {
            info.addPara("Odysseus completed the blueprints for Luyten in exchange for the Hegemony gate research data, but was unable to ascertain why the gates stopped working. Perhaps Luyten's station design, if constructed, could provide similar functionality, albeit only within the confines of the Sector.", opad);
        }
        else if (getQuestStage() == 6)
        {
            info.addPara("You provided the data to Odysseus, but the AI core was unable to ascertain why the gates stopped working.", opad);
        }
        else if (getQuestStage() == 7)
        {
            info.addPara("You convinced Luyten you weren't working for an AI core, and he gave you the Hegemony gate research data in exchange for a bribe of 100,000 credits. Hopefully Odysseus can solve the mystery of why the gate network collapsed.", opad);
        }

        this.addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    private int getQuestStage()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet.hasTag("boggled_gate_quest_stage1"))
        {
            return 1;
        }
        else if (playerFleet.hasTag("boggled_gate_quest_stage2"))
        {
            return 2;
        }
        else if (playerFleet.hasTag("boggled_gate_quest_stage3"))
        {
            return 3;
        }
        else if (playerFleet.hasTag("boggled_gate_quest_stage4"))
        {
            return 4;
        }
        else if (playerFleet.hasTag("boggled_gate_quest_stage5"))
        {
            return 5;
        }
        else if (playerFleet.hasTag("boggled_gate_quest_stage6"))
        {
            return 6;
        }
        else if (playerFleet.hasTag("boggled_gate_quest_stage7"))
        {
            return 7;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map)
    {
        return new HashSet<>(Arrays.asList(Tags.INTEL_EXPLORATION, Tags.INTEL_STORY));
    }
}