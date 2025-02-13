package dev.phonis.schematica_extensions.config;

import dev.phonis.schematica_extensions.SchematicaExtensions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
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

    static
    {
        ConfigurationManager.schematicaExtensionsDirectory.mkdirs();
    }

    public Configuration configuration;
    public List<IConfigElement> configCategoryList = new ArrayList<>();

    public void init(File configFile)
    {
        if (this.configuration != null)
        {
            return;
        }
        this.configuration = new Configuration(configFile, ConfigurationManager.VERSION);
        this.loadConfiguration();
    }

    public void loadConfiguration()
    {
        if (this.configuration.hasChanged())
        {
            this.configuration.save();
        }
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
