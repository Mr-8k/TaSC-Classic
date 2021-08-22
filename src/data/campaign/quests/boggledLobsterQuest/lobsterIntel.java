package data.campaign.quests.boggledLobsterQuest;

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

public class lobsterIntel extends BaseIntelPlugin
{
    public lobsterIntel() { }

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
            return boggledTools.getPlanetTokenForQuest("Askonia", "cruor");
        }
        else if (getQuestStage() == 2)
        {
            return null;
        }
        else if (getQuestStage() == 3)
        {
            return boggledTools.getPlanetTokenForQuest("Askonia", "umbra");
        }
        else if (getQuestStage() == 4)
        {
            return null;
        }
        else if (getQuestStage() == 5)
        {
            return boggledTools.getPlanetTokenForQuest("Zagan", "ilm");
        }
        else if (getQuestStage() == 6)
        {
            return null;
        }
        else if (getQuestStage() == 7)
        {
            return null;
        }
        else if (getQuestStage() == 8)
        {
            return null;
        }
        else
        {
            return null;
        }
    }

    public String getName()
    {
        return !this.isEnded() && !this.isEnding() ? "The Crustacean Job" : "The Crustacean Job - Completed";
    }

    @Override
    public String getIcon()
    {
        return Global.getSettings().getSpriteName("boggled", "the_crustacean_job_intel");
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
            info.addPara("Rescue Elena Vega from %s", initPad, boggledTools.getPlanetTokenForQuest("Askonia", "cruor").getMarket().getFaction().getBaseUIColor(), new String[]{boggledTools.getPlanetTokenForQuest("Askonia", "cruor").getMarket().getName()});
        }
        else if (getQuestStage() == 2)
        {
            //Do nothing
        }
        else if (getQuestStage() == 3)
        {
            info.addPara("Meet Simmons on %s", initPad, boggledTools.getPlanetTokenForQuest("Askonia", "umbra").getMarket().getFaction().getBaseUIColor(), new String[]{boggledTools.getPlanetTokenForQuest("Askonia", "umbra").getMarket().getName()});
        }
        else if (getQuestStage() == 4)
        {
            //Do nothing
        }
        else if (getQuestStage() == 5)
        {
            info.addPara("Retrieve the equipment from %s", initPad, boggledTools.getPlanetTokenForQuest("Zagan", "ilm").getMarket().getFaction().getBaseUIColor(), new String[]{boggledTools.getPlanetTokenForQuest("Zagan", "ilm").getMarket().getName()});
        }
        else if (getQuestStage() == 6)
        {
            //Do nothing
        }
        else if (getQuestStage() == 7)
        {
            //Do nothing
        }
        else if (getQuestStage() == 8)
        {
            //Do nothing
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
            info.addPara("A pirate calling himself \"Gregory Simmons\" has agreed to pay you " + getAgreedUponCreditAmount() + " credits if you can rescue his associate, Elena Vega, from a labor camp on Cruor.", opad);
        }
        else if (getQuestStage() == 2)
        {
            info.addPara("You successfully rescued Vega from the labor camp on Cruor.\n\nSimmons paid you " + getAgreedUponCreditAmount() + " credits for her safe return.", opad);
        }
        else if (getQuestStage() == 3)
        {
            info.addPara("You successfully rescued Vega from the labor camp on Cruor.\n\nSimmons reluctantly agreed to your demand for an increased reward, but he wants to meet in-person on Umbra to confirm Vega has the information he needs before he sends you the credits.", opad);
        }
        else if (getQuestStage() == 4)
        {
            info.addPara("You tried to demand a larger reward from Simmons for rescuing Vega. He pretended to agree to your new terms, but then ambushed you on Umbra using a custom-built shuttle with advanced stealth capabilities. The men clad in Tri-Tachyon combat armor may be a clue as to the shuttle's origins.\n\nSimmons paid you nothing, and you have no way to track him down or uncover his real identity.", opad);
        }
        else if (getQuestStage() == 5)
        {
            info.addPara("Rather than turning Vega over to Simmons in exchange for " + getAgreedUponCreditAmount() + " credits, you forced her to reveal the location of the Domain-era gene editing equipment.\n\nVega paid a smuggler to ship the equipment to an orbital warehouse at Ilm. Retrieving it should be simple, assuming Simmons doesn't interfere.", opad);
        }
        else if (getQuestStage() == 6)
        {
            info.addPara("You were able to secure the Domain-era gene editing equipment without any trouble. Perhaps Simmons really was tracking you via your compromised TriPad.\n\nTo make use of the gene editing equipment, add the \"Seed Lobsters\" ability to your ability bar.", opad);
        }
        else if (getQuestStage() == 7)
        {
            info.addPara("Simmons, who is actually a Tri-Tachyon executive named James Zhang, discovered the location of the gene editing equipment and ambushed you in orbit above Ilm. Despite his formidable fleet, you prevailed and were able to obtain the equipment for yourself.\n\nVega managed to escape during the chaos of the battle. You decide not to pursue her as you already have the gene editing equipment.\n\nTo make use of the gene editing equipment, add the \"Seed Lobsters\" ability to your ability bar.", opad);
        }
        else if (getQuestStage() == 8)
        {
            info.addPara("Simmons, who is actually a Tri-Tachyon executive named James Zhang, discovered the location of the gene editing equipment and ambushed you in orbit above Ilm. Your fleet was no match for his, and he seized control of the warehouse where the lobster gene editing equipment is located.\n\nVega managed to steal an escape pod during the chaos of the encounter. You decide not to pursue her because Zhang already has the gene editing equipment.", opad);
        }

        this.addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    private String getAgreedUponCreditAmount()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        if(playerFleet.hasTag("boggled_lobster_quest_playerDemandedMoreMoney"))
        {
            return "500,000";
        }
        else
        {
            return "250,000";
        }
    }

    private int getQuestStage()
    {
        //Stage 1: You just picked up quest from Simmons on Umbra and are traveling to Cruor
        //Stage 2: You let Vega call Simmons and she left in the shuttle

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet.hasTag("boggled_lobster_quest_stage1"))
        {
            return 1;
        }
        else if (playerFleet.hasTag("boggled_lobster_quest_stage2"))
        {
            return 2;
        }
        else if (playerFleet.hasTag("boggled_lobster_quest_stage3"))
        {
            return 3;
        }
        else if (playerFleet.hasTag("boggled_lobster_quest_stage4"))
        {
            return 4;
        }
        else if (playerFleet.hasTag("boggled_lobster_quest_stage5"))
        {
            return 5;
        }
        else if (playerFleet.hasTag("boggled_lobster_quest_stage6"))
        {
            return 6;
        }
        else if (playerFleet.hasTag("boggled_lobster_quest_stage7"))
        {
            return 7;
        }
        else if (playerFleet.hasTag("boggled_lobster_quest_stage8"))
        {
            return 8;
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