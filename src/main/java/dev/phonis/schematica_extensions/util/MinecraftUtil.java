package dev.phonis.schematica_extensions.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class MinecraftUtil
{

    public static void sendLocalMessage(String message)
    {
        MinecraftUtil.sendLocalMessage(new ChatComponentText(message));
    }

    public static void sendLocalMessage(ChatComponentText message)
    {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message);
    }

}
