package dev.phonis.schematica_extensions.keybind;

import dev.phonis.schematica_extensions.extensions.CustomSchematicLoad;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class Keybinds
{

    public static final Keybinds INSTANCE = new Keybinds();
    public static final KeyBinding customSchematicLoad
        = new KeyBinding("Custom Schematic Load", Keyboard.KEY_NONE, "Schematica Extensions");

    public static void register()
    {
        ClientRegistry.registerKeyBinding(Keybinds.customSchematicLoad);
    }

    @SubscribeEvent
    public void onKey(InputEvent event)
    {
        while (Keybinds.customSchematicLoad.isPressed())
        {
            CustomSchematicLoad.INSTANCE.customLoadSchematic();
        }
    }

}
