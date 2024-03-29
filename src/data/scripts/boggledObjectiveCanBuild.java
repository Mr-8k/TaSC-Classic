package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CampaignObjective;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.loading.Description;
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
import com.fs.starfarer.rpg.Person;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledObjectiveCanBuild extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledObjectiveCanBuild() {}

    public boggledObjectiveCanBuild(SectorEntityToken entity) {
        this.init(entity);
    }

    protected void init(SectorEntityToken entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;

        String type = ((Misc.Token)params.get(0)).getString(memoryMap);
        this.entity = dialog.getInteractionTarget();

        TextPanelAPI text = dialog.getTextPanel();

        if(canBuild(type))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int[] getQuantities()
    {
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            return new int[]{50, 200, 20, 20};
        }
        else
        {
            return new int[]{50, 200, 20};
        }
    }

    public String[] getResources()
    {
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            return new String[]{"heavy_machinery", "metals", "rare_metals", "domain_artifacts"};
        }
        else
        {
            return new String[]{"heavy_machinery", "metals", "rare_metals"};
        }
    }

    public boolean canBuild(String type)
    {
        if(DebugFlags.OBJECTIVES_DEBUG)
        {
            return true;
        }
        else
        {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            String[] res = getResources();
            int[] quantities = getQuantities();

            for(int i = 0; i < res.length; ++i)
            {
                String commodityId = res[i];
                int quantity = quantities[i];
                if ((float)quantity > cargo.getQuantity(CargoAPI.CargoItemType.RESOURCES, commodityId))
                {
                    return false;
                }
            }

            return true;
        }
    }

    public void printDescription(String type)
    {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        TextPanelAPI text = dialog.getTextPanel();
        SectorEntityToken entity = Global.getSector().getPlayerFleet().getInteractionTarget();

        Description desc = Global.getSettings().getDescription(type, Description.Type.CUSTOM);
        if (desc != null)
        {
            text.addParagraph(desc.getText1());
        }

        CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(type);
        CustomCampaignEntityPlugin plugin = spec.getPlugin();
        SectorEntityToken temp = this.entity.getContainingLocation().createToken(0.0F, 0.0F);
        Iterator var7 = spec.getTags().iterator();

        while(var7.hasNext())
        {
            String tag = (String)var7.next();
            temp.addTag(tag);
        }

        plugin.init(temp,null);
        boolean objective = this.entity.hasTag("objective");
        if (objective)
        {
            plugin = this.entity.getCustomPlugin();
        }

        Class c = null;
        if (plugin instanceof CampaignObjective)
        {
            CampaignObjective o = (CampaignObjective)plugin;
            c = o.getClass();
            TooltipMakerAPI info = text.beginTooltip();
            o.printEffect(info, 0.0F);
            text.addTooltip();
            o.printNonFunctionalAndHackDescription(text);
        }

        Iterator var15 = this.entity.getContainingLocation().getEntitiesWithTag("objective").iterator();

        while(var15.hasNext())
        {
            SectorEntityToken curr = (SectorEntityToken)var15.next();
            if (curr.hasTag("objective") && curr.getFaction() != null && curr.getFaction().isPlayerFaction() && curr.getCustomEntitySpec() != null)
            {
                CustomCampaignEntityPlugin ccep = curr.getCustomPlugin();
                if (ccep instanceof CampaignObjective)
                {
                    CampaignObjective o = (CampaignObjective)ccep;
                    if (c == o.getClass())
                    {
                        if (this.entity == curr)
                        {
                            text.addPara("Another one in this star system would have no effect beyond providing redundancy in case this one is lost.");
                        }
                        else
                        {
                            text.addPara("There's already " + curr.getCustomEntitySpec().getAOrAn() + " " + curr.getCustomEntitySpec().getNameInText() + " under your control " + "in this star system. Another one would have no effect " + "beyond providing redundancy if one is lost.");
                        }
                        break;
                    }
                }
            }
        }
    }
}