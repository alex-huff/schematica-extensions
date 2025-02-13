package dev.phonis.schematica_extensions.config;

import dev.phonis.schematica_extensions.SchematicaExtensions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class SchematicaExtensionsGuiConfig extends GuiConfig
{

    public SchematicaExtensionsGuiConfig(GuiScreen parent)
    {
        super(parent, ConfigurationManager.INSTANCE.configCategoryList, SchematicaExtensions.MODID, false, false, "Schematica Extensions Configuration");
    }

}
