package dev.phonis.schematica_extensions;

import dev.phonis.schematica_extensions.config.ConfigurationManager;
import dev.phonis.schematica_extensions.extensions.SchematicMovement;
import dev.phonis.schematica_extensions.keybind.Keybinds;
import dev.phonis.schematica_extensions.render.WorldRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = SchematicaExtensions.MODID, version = SchematicaExtensions.VERSION, guiFactory = SchematicaExtensions.GUI_FACTORY)
public class SchematicaExtensions
{

    public static final String GUI_FACTORY = "dev.phonis.schematica_extensions.config.SchematicaExtensionsGuiFactory";
    public static final String MODID = "schematica_extensions";
    public static final String VERSION = "0.0.5";

    @EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        ConfigurationManager.INSTANCE.init(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        Keybinds.register();
        MinecraftForge.EVENT_BUS.register(Keybinds.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ConfigurationManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(WorldRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(SchematicMovement.INSTANCE);
    }

}
