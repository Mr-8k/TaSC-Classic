package data.campaign.econ.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
//import com.fs.starfarer.api.impl.campaign.econ.impl.NanoforgeInstallableItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.HashMap;
import java.util.Map;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;
import data.campaign.econ.industries.Boggled_Pheromone_Splicer;

public class BoggledPheromoneSplicerNanoforgePlugin extends BaseInstallableIndustryItemPlugin
{
    private Boggled_Pheromone_Splicer industry;

    public BoggledPheromoneSplicerNanoforgePlugin(Boggled_Pheromone_Splicer industry) {
        this.industry = industry;
    }

    public static Map<String, BoggledPheromoneSplicerNanoforgePlugin.NanoforgeEffect> NANOFORGE_EFFECTS = new HashMap<String, BoggledPheromoneSplicerNanoforgePlugin.NanoforgeEffect>()
    {
        {
            this.put("corrupted_nanoforge", new BoggledPheromoneSplicerNanoforgePlugin.NanoforgeEffect()
            {
                public void apply(Industry industry) { }

                public void unapply(Industry industry) { }

                public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode)
                {
                    SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec("corrupted_nanoforge");
                    String name = Misc.ucFirst(spec.getName().toLowerCase());
                    String pre = "";
                    float pad = 0.0F;
                    if (mode != InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST && mode != InstallableItemDescriptionMode.INDUSTRY_TOOLTIP) {
                        if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_INSTALLED || mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP) {
                            pre = name + " currently installed. ";
                        }
                    } else {
                        pre = name + ". ";
                    }

                    if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP || mode == InstallableItemDescriptionMode.CARGO_TOOLTIP) {
                        pad = 10.0F;
                    }

                    text.addPara(pre + "Improves the amount of pheromone that can be produced, increasing the population growth bonus multiplier by %s.", pad, Misc.getHighlightColor(), new String[]{"2"});
                }
            });
            this.put("pristine_nanoforge", new BoggledPheromoneSplicerNanoforgePlugin.NanoforgeEffect()
            {
                public void apply(Industry industry) { }

                public void unapply(Industry industry) { }

                public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode)
                {
                    SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec("pristine_nanoforge");
                    String name = Misc.ucFirst(spec.getName().toLowerCase());
                    String pre = "";
                    float pad = 0.0F;
                    if (mode != InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST && mode != InstallableItemDescriptionMode.INDUSTRY_TOOLTIP) {
                        if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_INSTALLED || mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP) {
                            pre = name + " currently installed. ";
                        }
                    } else {
                        pre = name + ". ";
                    }

                    if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP || mode == InstallableItemDescriptionMode.CARGO_TOOLTIP) {
                        pad = 10.0F;
                    }

                    text.addPara(pre + "Massively improves the amount of pheromone that can be produced, increasing the population growth bonus multiplier by %s.", pad, Misc.getHighlightColor(), new String[]{"4"});
                }
            });
        }
    };

    public interface NanoforgeEffect
    {
        void apply(Industry var1);

        void unapply(Industry var1);

        void addItemDescription(TooltipMakerAPI var1, SpecialItemData var2, InstallableItemDescriptionMode var3);
    }

    public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode)
    {
        if(data.getId().equals("pristine_nanoforge"))
        {
            String name = "Pristine nanoforge";
            String pre = "";
            float pad = 0.0F;
            if (mode != InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST && mode != InstallableItemDescriptionMode.INDUSTRY_TOOLTIP)
            {
                if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_INSTALLED || mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP)
                {
                    pre = name + " currently installed. ";
                }
            }
            else
            {
                pre = name + ". ";
            }

            if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP || mode == InstallableItemDescriptionMode.CARGO_TOOLTIP)
            {
                pad = 10.0F;
            }

            text.addPara(pre + "Massively improves the amount of pheromone that can be produced, increasing the population growth bonus multiplier by %s.", pad, Misc.getHighlightColor(), new String[]{"4"});
        }
        else if(data.getId().equals("corrupted_nanoforge"))
        {
            String name = "Corrupted nanoforge";
            String pre = "";
            float pad = 0.0F;
            if (mode != InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST && mode != InstallableItemDescriptionMode.INDUSTRY_TOOLTIP)
            {
                if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_INSTALLED || mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP)
                {
                    pre = name + " currently installed. ";
                }
            }
            else
            {
                pre = name + ". ";
            }

            if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP || mode == InstallableItemDescriptionMode.CARGO_TOOLTIP)
            {
                pad = 10.0F;
            }

            text.addPara(pre + "Improves the amount of pheromone that can be produced, increasing the population growth bonus multiplier by %s.", pad, Misc.getHighlightColor(), new String[]{"2"});
        }
    }

    public String getMenuItemTitle()
    {
        return this.getCurrentlyInstalledItemData() == null ? "Install nanoforge..." : "Manage nanoforge...";
    }

    public String getUninstallButtonText()
    {
        return "Uninstall nanoforge";
    }

    public boolean isInstallableItem(CargoStackAPI stack)
    {
        if (stack.isSpecialStack() && (stack.getSpecialItemSpecIfSpecial().getId().equals("pristine_nanoforge") || stack.getSpecialItemSpecIfSpecial().getId().equals("corrupted_nanoforge")))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public SpecialItemData getCurrentlyInstalledItemData()
    {
        return this.industry.getNanoforge();
    }

    public void setCurrentlyInstalledItemData(SpecialItemData data)
    {
        this.industry.setNanoforge(data);
    }

    public String getNoItemCurrentlyInstalledText()
    {
        return "No nanoforge currently installed";
    }

    public String getNoItemsAvailableText()
    {
        return "No nanoforges available";
    }

    public String getNoItemsAvailableTextRemote()
    {
        return "No nanoforges available in storage";
    }

    public String getSelectItemToAssignToIndustryText()
    {
        return "Select nanoforge to install for " + this.industry.getCurrentName();
    }

    public boolean isMenuItemTooltipExpandable() {
        return false;
    }

    public float getMenuItemTooltipWidth() {
        return super.getMenuItemTooltipWidth();
    }

    public boolean hasMenuItemTooltip() {
        return super.hasMenuItemTooltip();
    }

    public void createMenuItemTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        float pad = 3.0F;
        float opad = 10.0F;
        tooltip.addPara("An irreplaceable piece of Domain technology, a nanoforge improves the quality of ship production by reducing the number of manufacturing defects. It also makes the construction of higher-tier weapons easier.", 0.0F);
    }
}
