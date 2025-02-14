package dev.phonis.schematica_extensions.render;

import dev.phonis.schematica_extensions.color.Color4f;
import dev.phonis.schematica_extensions.extensions.SchematicMovement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class WorldRenderer
{

    public static final WorldRenderer INSTANCE = new WorldRenderer();

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event)
    {
        this.drawLines(event.player, event.partialTicks);
    }

    public void drawLines(EntityPlayer player, float partialTicks)
    {
        double posX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double posY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double posZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(1f);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glTranslated(-posX, -posY, -posZ);
        SchematicMovement.INSTANCE.forAllLines(this::drawLine);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glLineWidth(1f);
        GL11.glTranslated(0, 0, 0);
        GL11.glPopMatrix();
    }

    private void drawLine(double sx, double sy, double sz, double ex, double ey, double ez, Color4f color)
    {
        GL11.glColor4f(color.r, color.g, color.b, color.a);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(sx, sy, sz);
        GL11.glVertex3d(ex, ey, ez);
        GL11.glEnd();
    }

}
