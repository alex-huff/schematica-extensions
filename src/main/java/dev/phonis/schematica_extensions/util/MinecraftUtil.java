package dev.phonis.schematica_extensions.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

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

    public static Vec3 getPlayerDirection(EntityPlayerSP player)
    {
        float f = MathHelper.cos(-player.rotationYaw * 0.017453292f - 3.1415927f);
        float f1 = MathHelper.sin(-player.rotationYaw * 0.017453292f - 3.1415927f);
        float f2 = -MathHelper.cos(-player.rotationPitch * 0.017453292f);
        float f3 = MathHelper.sin(-player.rotationPitch * 0.017453292f);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public static float getPartialTicks()
    {
        return Minecraft.getMinecraft().timer.renderPartialTicks;
    }

}
