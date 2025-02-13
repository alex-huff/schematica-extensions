package dev.phonis.schematica_extensions;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(modid = SchematicaExtensions.MODID, version = SchematicaExtensions.VERSION, guiFactory = SchematicaExtensions.GUI_FACTORY)
public class SchematicaExtensions
{

    public static final String GUI_FACTORY = "dev.phonis.schematica_extensions.config.SchematicaExtensionsGuiFactory";
    public static final String MODID = "schematica_extensions";
    public static final String VERSION = "0.0.1";
    public static final Path DIRECTORY = Minecraft.getMinecraft().mcDataDir.toPath().resolve("schematica-extensions");

    @EventHandler
    public void init(FMLPreInitializationEvent event)
    {
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

}
