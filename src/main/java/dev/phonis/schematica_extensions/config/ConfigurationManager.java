package dev.phonis.schematica_extensions.config;

import dev.phonis.schematica_extensions.SchematicaExtensions;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationManager
{

    public static final ConfigurationManager INSTANCE = new ConfigurationManager();
    public static final String VERSION = "1";

    private final static String[] SCHEMATIC_CUSTOM_LOAD_COMMAND_WINDOWS_DEFAULT = new String[]{
        "alacritty.exe", "--option", "window.startup_mode='Fullscreen'", "--command", "PowerShell.exe", "-Command",
        "& {$dir=$args[0]; cd $dir; echo $dir\\$(fzf) > $args[1]}", "{schematics_directory}", "{choice_file}"
    };
    private final static String[] SCHEMATIC_CUSTOM_LOAD_COMMAND_OTHER_DEFAULT = new String[]{
        "sh", "-c",
        "chosen_schematic_name=$(find \"$1\" -type f -name \"*.schematic\" | xargs -d \"\\n\" realpath --relative-to \"$1\" | sed \"s/\\.schematic$//\" | rofi -dmenu -i -theme-str \"#window { width: 500; }\") && echo \"${1}/${chosen_schematic_name}.schematic\"",
        "_", "{schematics_directory}"
    };
    private final static int SCHEMATIC_MOVE_LARGE_INCREMENT_DEFAULT = 25;
    private final static int SCHEMATIC_MOVE_REFRESH_DELAY_DEFAULT = 1000;

    public Configuration configuration;
    public List<IConfigElement> configCategoryList = new ArrayList<>();

    public String[] schematicCustomLoadCommand = ConfigurationManager.getSchematicCustomLoadCommandDefault();
    public int schematicMoveLargeIncrement = ConfigurationManager.SCHEMATIC_MOVE_LARGE_INCREMENT_DEFAULT;
    public int schematicMoveRefreshDelay = ConfigurationManager.SCHEMATIC_MOVE_REFRESH_DELAY_DEFAULT;

    private Property schematicCustomLoadCommandProperty;
    private Property schematicMoveLargeIncrementProperty;
    private Property schematicMoveRefreshDelayProperty;

    private static String[] getSchematicCustomLoadCommandDefault()
    {
        return SystemUtils.IS_OS_WINDOWS ? ConfigurationManager.SCHEMATIC_CUSTOM_LOAD_COMMAND_WINDOWS_DEFAULT
                                         : ConfigurationManager.SCHEMATIC_CUSTOM_LOAD_COMMAND_OTHER_DEFAULT;
    }

    public void init(File configFile)
    {
        if (this.configuration != null)
        {
            return;
        }
        this.configuration = new Configuration(configFile, ConfigurationManager.VERSION);

        this.schematicCustomLoadCommandProperty
            = this.configuration.get("Loading", "Custom Schematic Load Command", ConfigurationManager.getSchematicCustomLoadCommandDefault(), "Command (and arguments) to use for selecting a schematic to load. The command should write the selected schematic path to the choice file or STDOUT.\nThe following placeholders are defined:\n'{schematics_directory}': The path to the schematics directory.\n'{choice_file}': The path to the choice file. If nothing is written to this file, the STDOUT of the spawned process will be used instead for acquiring the selected schematic path.");
        ConfigCategory loadingCategory = new ConfigCategory("Loading");
        loadingCategory.put("Custom Schematic Load Command", this.schematicCustomLoadCommandProperty);
        this.configCategoryList.add(new ConfigElement(loadingCategory));

        this.schematicMoveLargeIncrementProperty
            = this.configuration.get("Moving", "Move Large Increment", ConfigurationManager.SCHEMATIC_MOVE_LARGE_INCREMENT_DEFAULT, "Increment to use when moving schematic with sprint held");
        this.schematicMoveRefreshDelayProperty
            = this.configuration.get("Moving", "Move Refresh Delay", ConfigurationManager.SCHEMATIC_MOVE_REFRESH_DELAY_DEFAULT, "How long in milliseconds to wait after last move before refreshing schematic");
        ConfigCategory movingCategory = new ConfigCategory("Moving");
        movingCategory.put("Move Large Increment", this.schematicMoveLargeIncrementProperty);
        movingCategory.put("Move Refresh Delay", this.schematicMoveRefreshDelayProperty);
        this.configCategoryList.add(new ConfigElement(movingCategory));

        this.loadConfiguration();
    }

    public void loadConfiguration()
    {
        this.reloadConfigurationLoading();
        this.reloadConfigurationMoving();
        if (this.configuration.hasChanged())
        {
            this.configuration.save();
        }
    }

    private void reloadConfigurationLoading()
    {
        this.schematicCustomLoadCommand = this.schematicCustomLoadCommandProperty.getStringList();
    }

    private void reloadConfigurationMoving()
    {
        this.schematicMoveLargeIncrement = this.schematicMoveLargeIncrementProperty.getInt();
        this.schematicMoveRefreshDelay = this.schematicMoveRefreshDelayProperty.getInt();
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.modID.equalsIgnoreCase(SchematicaExtensions.MODID))
        {
            this.loadConfiguration();
        }
    }

}
