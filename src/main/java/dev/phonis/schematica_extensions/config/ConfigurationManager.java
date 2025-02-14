package dev.phonis.schematica_extensions.config;

import dev.phonis.schematica_extensions.SchematicaExtensions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationManager
{

    public static final ConfigurationManager INSTANCE = new ConfigurationManager();
    public static final String VERSION = "1";
    public static final String SCHEMATICA_EXTENSIONS_DIRECTORY_STR = "schematica-extensions";
    public static final File schematicaExtensionsDirectory
        = new File(Minecraft.getMinecraft().mcDataDir, ConfigurationManager.SCHEMATICA_EXTENSIONS_DIRECTORY_STR);

    private final static String[] SCHEMATIC_CUSTOM_LOAD_COMMAND_DEFAULT = new String[]{
        "sh", "-c",
        "schematic_directory_path=$(realpath \"$1\") && chosen_schematic_name=$(find \"$schematic_directory_path\" -type f -name \"*.schematic\" | xargs -d \"\\n\" realpath --relative-to \"$schematic_directory_path\" | sed \"s/\\(.*\\)\\.schematic$/\\1/\" | rofi -dmenu -i -theme-str \"#window { width: 500; }\") && echo \"${schematic_directory_path}/${chosen_schematic_name}.schematic\"",
        "_", "{directory}"
    };
    private final static int SCHEMATIC_MOVE_LARGE_INCREMENT_DEFAULT = 25;
    private final static int SCHEMATIC_MOVE_REFRESH_DELAY_DEFAULT = 1000;

    static
    {
        ConfigurationManager.schematicaExtensionsDirectory.mkdirs();
    }

    public Configuration configuration;
    public List<IConfigElement> configCategoryList = new ArrayList<>();

    public String[] schematicCustomLoadCommand = ConfigurationManager.SCHEMATIC_CUSTOM_LOAD_COMMAND_DEFAULT;
    public int schematicMoveLargeIncrement = ConfigurationManager.SCHEMATIC_MOVE_LARGE_INCREMENT_DEFAULT;
    public int schematicMoveRefreshDelay = ConfigurationManager.SCHEMATIC_MOVE_REFRESH_DELAY_DEFAULT;

    private Property schematicCustomLoadCommandProperty;
    private Property schematicMoveLargeIncrementProperty;
    private Property schematicMoveRefreshDelayProperty;

    public void init(File configFile)
    {
        if (this.configuration != null)
        {
            return;
        }
        this.configuration = new Configuration(configFile, ConfigurationManager.VERSION);

        this.schematicCustomLoadCommandProperty
            = this.configuration.get("Loading", "Custom Schematic Load Command", ConfigurationManager.SCHEMATIC_CUSTOM_LOAD_COMMAND_DEFAULT, "A custom command/arguments for choosing a schematic to load. '{directory}' is replaced with the full path to the current schematic directory for each argument. The STDOUT of the process is expected to be the full path to the chosen file.");
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
