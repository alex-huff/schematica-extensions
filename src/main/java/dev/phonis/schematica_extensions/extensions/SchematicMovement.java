package dev.phonis.schematica_extensions.extensions;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.util.FlipHelper;
import com.github.lunatrius.schematica.client.util.RotationHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import dev.phonis.schematica_extensions.color.Color4f;
import dev.phonis.schematica_extensions.config.ConfigurationManager;
import dev.phonis.schematica_extensions.util.MinecraftUtil;
import dev.phonis.schematica_extensions.util.RenderUtil;
import dev.phonis.schematica_extensions.util.SchematicUtil;
import dev.phonis.schematica_extensions.util.VecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.stream.Stream;

public class SchematicMovement
{

    private static class Transformation
    {

        @FunctionalInterface
        private interface DimensionConsumer
        {

            void accept(int width, int height, int length);

        }

        private static final int ROTATE_90 = 1, FLIP_NS = 2, FLIP_EW = 4;

        private static BlockPos getTransformedBlockPos(ISchematic schematic, BlockPos original, int transformation)
        {
            if (transformation == 0)
            {
                return original;
            }
            int newX = original.getX(), newY = original.getY(), newZ = original.getZ();
            int width = schematic.getWidth(), length = schematic.getLength();
            if ((transformation & Transformation.ROTATE_90) > 0)
            {
                newX = (length - 1) - original.getZ();
                newZ = original.getX();
                int temp = width;
                width = length;
                length = temp;
            }
            if ((transformation & Transformation.FLIP_NS) > 0)
            {
                newZ = (length - 1) - newZ;
            }
            if ((transformation & Transformation.FLIP_EW) > 0)
            {
                newX = (width - 1) - newX;
            }
            return new BlockPos(newX, newY, newZ);
        }

        private static void withTransformedDimensions(ISchematic schematic, int transformation,
                                                      DimensionConsumer dimensionConsumer)
        {
            if ((transformation & Transformation.ROTATE_90) > 0)
            {
                dimensionConsumer.accept(schematic.getLength(), schematic.getHeight(), schematic.getWidth());
                return;
            }
            dimensionConsumer.accept(schematic.getWidth(), schematic.getHeight(), schematic.getLength());
        }

        private static boolean applyTransformation(SchematicWorld schematicWorld, int transformation)
        {
            if (transformation == 0)
            {
                return false;
            }
            if ((transformation & Transformation.ROTATE_90) > 0)
            {
                RotationHelper.INSTANCE.rotate(schematicWorld, EnumFacing.UP, false);
            }
            if ((transformation & Transformation.FLIP_NS) > 0)
            {
                FlipHelper.INSTANCE.flip(schematicWorld, EnumFacing.NORTH, false);
            }
            if ((transformation & Transformation.FLIP_EW) > 0)
            {
                FlipHelper.INSTANCE.flip(schematicWorld, EnumFacing.EAST, false);
            }
            return true;
        }

    }

    public static final SchematicMovement INSTANCE = new SchematicMovement();

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Set<Vec3> adjustDirections;
    private final int rayTraceDistance = 500;
    private Vec3 anchorPosition = null;
    private BlockPos schematicAnchor1BlockPos = null;
    private BlockPos schematicAnchor2BlockPos = null;
    private BlockPos realAnchor1BlockPos = null;
    private BlockPos realAnchor2BlockPos = null;
    private boolean isSchematicTethered = false;
    private double anchorDistance = Double.MIN_VALUE;
    private long moveCounter = -1;

    private SchematicMovement()
    {
        this.adjustDirections = new HashSet<>();
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    if (x == 0 && y == 0 && z == 0)
                    {
                        continue;
                    }
                    Vec3 direction = new Vec3(x, y, z).normalize();
                    this.adjustDirections.add(direction);
                }
            }
        }
    }

    private void resetMoveCounter()
    {
        this.moveCounter = Math.round(ConfigurationManager.INSTANCE.schematicMoveRefreshDelay / 50d);
    }

    private boolean currentlyPerformingMove()
    {
        return this.anchorPosition != null;
    }

    private void resetMoveState()
    {
        this.anchorPosition = null;
        this.schematicAnchor1BlockPos = null;
        this.schematicAnchor2BlockPos = null;
        this.realAnchor1BlockPos = null;
        this.realAnchor2BlockPos = null;
        this.isSchematicTethered = false;
        this.anchorDistance = Double.MIN_VALUE;
    }

    private boolean validSchematicAnchors(BlockPos a, BlockPos b)
    {
        int xDiff = Math.abs(b.getX() - a.getX());
        int zDiff = Math.abs(b.getZ() - a.getZ());
        return xDiff != zDiff && xDiff > 0 && zDiff > 0;
    }

    private boolean validAnchors(BlockPos _schematicAnchor1BlockPos, BlockPos _schematicAnchor2BlockPos,
                                 BlockPos _realAnchor1BlockPos, BlockPos _realAnchor2BlockPos)
    {
        Vec3i realDiff = _realAnchor2BlockPos.subtract(_realAnchor1BlockPos);
        Vec3i schematicDiff = _schematicAnchor2BlockPos.subtract(_schematicAnchor1BlockPos);
        if (realDiff.getY() != schematicDiff.getY())
        {
            return false;
        }
        int realAbsXDiff = Math.abs(realDiff.getX());
        int realAbsZDiff = Math.abs(realDiff.getZ());
        int realMinDiff = Math.min(realAbsXDiff, realAbsZDiff);
        int realMaxDiff = Math.max(realAbsXDiff, realAbsZDiff);
        int schematicAbsXDiff = Math.abs(schematicDiff.getX());
        int schematicAbsZDiff = Math.abs(schematicDiff.getZ());
        int schematicMinDiff = Math.min(schematicAbsXDiff, schematicAbsZDiff);
        int schematicMaxDiff = Math.max(schematicAbsXDiff, schematicAbsZDiff);
        return realMinDiff == schematicMinDiff && realMaxDiff == schematicMaxDiff;
    }

    private int getTransformationFromAnchors(BlockPos _schematicAnchor1BlockPos, BlockPos _schematicAnchor2BlockPos,
                                             BlockPos _realAnchor1BlockPos, BlockPos _realAnchor2BlockPos)
    {
        int transformation = 0;
        if (_schematicAnchor2BlockPos == null)
        {
            return transformation;
        }
        Vec3i realDiff = _realAnchor2BlockPos.subtract(_realAnchor1BlockPos);
        Vec3i schematicDiff = _schematicAnchor2BlockPos.subtract(_schematicAnchor1BlockPos);
        int realXDiff = realDiff.getX();
        int realZDiff = realDiff.getZ();
        int schematicXDiff = schematicDiff.getX();
        int schematicZDiff = schematicDiff.getZ();
        if (Math.abs(realXDiff) == Math.abs(schematicZDiff))
        {
            int temp = schematicXDiff;
            schematicXDiff = -schematicZDiff;
            schematicZDiff = temp;
            transformation |= Transformation.ROTATE_90;
        }
        if (realXDiff != schematicXDiff)
        {
            transformation |= Transformation.FLIP_EW;
        }
        if (realZDiff != schematicZDiff)
        {
            transformation |= Transformation.FLIP_NS;
        }
        return transformation;
    }

    private boolean onScroll(EntityPlayerSP player, int dWheel, boolean altHeld, boolean sprintHeld)
    {
        SchematicWorld schematicWorld = ClientProxy.schematic;
        if (schematicWorld == null)
        {
            return false;
        }
        MBlockPos schematicWorldPosition = schematicWorld.position;
        boolean forward = dWheel > 0;
        int modifier = (sprintHeld ? ConfigurationManager.INSTANCE.schematicMoveLargeIncrement : 1) *
                       (forward ? 1 : -1);
        if (this.currentlyPerformingMove())
        {
            if (altHeld)
            {
                this.anchorDistance += modifier;
                this.anchorDistance = Math.max(this.anchorDistance, 0);
            }
            return altHeld;
        }
        if (!altHeld)
        {
            return false;
        }
        Vec3 playerDirection = MinecraftUtil.getPlayerDirection(player).normalize();
        Vec3 direction = this.adjustDirections.stream()
            .min(Comparator.comparingDouble(adjustDirection -> adjustDirection.squareDistanceTo(playerDirection)))
            .get();
        Vec3 offset = VecUtil.vec3Mul(direction, modifier);
        Vec3i roundedOffset = VecUtil.roundVec3(offset);
        schematicWorldPosition.set(schematicWorldPosition.add(roundedOffset));
        this.resetMoveCounter();
        return true;
    }

    private boolean onClick(EntityPlayerSP player, boolean altHeld, boolean sprintHeld)
    {
        WorldClient realWorld = this.minecraft.theWorld;
        SchematicWorld schematicWorld = ClientProxy.schematic;
        if (realWorld == null || schematicWorld == null)
        {
            return false;
        }
        MBlockPos schematicWorldPosition = schematicWorld.position;
        float partialTicks = MinecraftUtil.getPartialTicks();
        Vec3 eyes = player.getPositionEyes(partialTicks);
        if (!this.currentlyPerformingMove())
        {
            if (!altHeld)
            {
                return false;
            }
            Pair<MovingObjectPosition, SchematicUtil.HitPositionWorld> hitPositionPair
                = SchematicUtil.rayTraceBlocks(realWorld, schematicWorld, player, this.rayTraceDistance);
            MovingObjectPosition hitPosition = hitPositionPair.getLeft();
            SchematicUtil.HitPositionWorld hitPositionWorld = hitPositionPair.getRight();
            if (!hitPositionWorld.inSchematicWorld())
            {
                return false;
            }
            this.anchorPosition
                = hitPosition.hitVec.subtract(schematicWorldPosition.getX(), schematicWorldPosition.getY(), schematicWorldPosition.getZ());
            this.schematicAnchor1BlockPos = hitPosition.getBlockPos().subtract(schematicWorldPosition);
            this.anchorDistance = hitPosition.hitVec.distanceTo(eyes);
            this.isSchematicTethered = !sprintHeld;
            return true;
        }
        boolean shouldPlaceSchematic = true;
        notTethered:
        if (!this.isSchematicTethered)
        {
            Pair<MovingObjectPosition, SchematicUtil.HitPositionWorld> hitPositionPair
                = SchematicUtil.rayTraceBlocks(realWorld, schematicWorld, player, this.rayTraceDistance);
            MovingObjectPosition hitPosition = hitPositionPair.getLeft();
            SchematicUtil.HitPositionWorld hitPositionWorld = hitPositionPair.getRight();
            if (this.schematicAnchor2BlockPos == null && hitPositionWorld.inSchematicWorld())
            {
                BlockPos targettedBlockPos = hitPosition.getBlockPos();
                BlockPos _schematicAnchor2BlockPos = targettedBlockPos.subtract(schematicWorldPosition);
                if (!this.validSchematicAnchors(this.schematicAnchor1BlockPos, _schematicAnchor2BlockPos))
                {
                    shouldPlaceSchematic = false;
                    break notTethered;
                }
                this.schematicAnchor2BlockPos = _schematicAnchor2BlockPos;
                return true;
            }
            if (!hitPositionWorld.inRealWorld())
            {
                shouldPlaceSchematic = false;
                break notTethered;
            }
            BlockPos targettedBlockPos = hitPosition.getBlockPos();
            if (this.realAnchor1BlockPos == null)
            {
                this.realAnchor1BlockPos = targettedBlockPos;
            }
            else if (this.validAnchors(this.schematicAnchor1BlockPos, this.schematicAnchor2BlockPos, this.realAnchor1BlockPos, targettedBlockPos))
            {
                this.realAnchor2BlockPos = targettedBlockPos;
            }
            else
            {
                shouldPlaceSchematic = false;
                break notTethered;
            }
            boolean shouldOrientSchematic = this.schematicAnchor2BlockPos == null || this.realAnchor2BlockPos != null;
            if (!shouldOrientSchematic)
            {
                return true;
            }
            int transformation
                = this.getTransformationFromAnchors(this.schematicAnchor1BlockPos, this.schematicAnchor2BlockPos, this.realAnchor1BlockPos, this.realAnchor2BlockPos);
            Vec3i newOrigin
                = this.realAnchor1BlockPos.subtract(Transformation.getTransformedBlockPos(schematicWorld.getSchematic(), this.schematicAnchor1BlockPos, transformation));
            if (transformation == 0 && newOrigin.equals(schematicWorldPosition))
            {
                shouldPlaceSchematic = false;
                break notTethered;
            }
            if (Transformation.applyTransformation(schematicWorld, transformation))
            {
                SchematicPrinter.INSTANCE.refresh();
            }
            schematicWorldPosition.set(newOrigin);
        }
        this.resetMoveState();
        if (shouldPlaceSchematic)
        {
            RenderSchematic.INSTANCE.refresh();
            this.moveCounter = -1;
        }
        return true;
    }

    public boolean onMouse()
    {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null)
        {
            return false;
        }
        boolean altHeld = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
        int dWheel = Mouse.getEventDWheel();
        boolean sprintHeld = Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown();
        if (dWheel != 0)
        {
            return this.onScroll(player, dWheel, altHeld, sprintHeld);
        }
        else if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState())
        {
            return this.onClick(player, altHeld, sprintHeld);
        }
        return false;
    }

    public void forAllLines(RenderUtil.LineConsumer lineConsumer)
    {
        EntityPlayerSP player = this.minecraft.thePlayer;
        WorldClient realWorld = this.minecraft.theWorld;
        SchematicWorld schematicWorld = ClientProxy.schematic;
        if (player == null || realWorld == null || schematicWorld == null)
        {
            return;
        }
        MBlockPos schematicWorldPosition = schematicWorld.position;
        if (!this.currentlyPerformingMove())
        {
            boolean altHeld = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
            if (!altHeld)
            {
                return;
            }
            Pair<MovingObjectPosition, SchematicUtil.HitPositionWorld> hitPositionPair
                = SchematicUtil.rayTraceBlocks(realWorld, schematicWorld, player, this.rayTraceDistance);
            MovingObjectPosition hitPosition = hitPositionPair.getLeft();
            SchematicUtil.HitPositionWorld hitPositionWorld = hitPositionPair.getRight();
            if (!hitPositionWorld.inSchematicWorld())
            {
                return;
            }
            BlockPos targettedBlockPos = hitPosition.getBlockPos();
            RenderUtil.provideBlockOutlines(lineConsumer, targettedBlockPos, .6, Color4f.WHITE);
            return;
        }
        Stream.of(this.schematicAnchor1BlockPos, this.schematicAnchor2BlockPos).filter(Objects::nonNull)
            .forEach(blockPos ->
            {
                BlockPos absoluteAnchorBlockPos = blockPos.add(schematicWorldPosition);
                RenderUtil.provideBlockOutlines(lineConsumer, absoluteAnchorBlockPos, .6, Color4f.WHITE);
            });
        if (this.isSchematicTethered)
        {
            return;
        }
        Stream.of(this.realAnchor1BlockPos).filter(Objects::nonNull)
            .forEach(blockPos -> RenderUtil.provideBlockOutlines(lineConsumer, blockPos, .6, Color4f.WHITE));
        Pair<MovingObjectPosition, SchematicUtil.HitPositionWorld> hitPositionPair
            = SchematicUtil.rayTraceBlocks(realWorld, schematicWorld, player, this.rayTraceDistance);
        MovingObjectPosition hitPosition = hitPositionPair.getLeft();
        SchematicUtil.HitPositionWorld hitPositionWorld = hitPositionPair.getRight();
        if (hitPositionWorld.equals(SchematicUtil.HitPositionWorld.NONE) ||
            hitPositionWorld.equals(SchematicUtil.HitPositionWorld.SCHEMATIC) && this.schematicAnchor2BlockPos != null)
        {
            return;
        }
        BlockPos targettedBlockPos = hitPosition.getBlockPos();
        boolean invalidSchematicAnchor2Target = this.schematicAnchor2BlockPos == null &&
                                                hitPositionWorld.inSchematicWorld() &&
                                                !this.validSchematicAnchors(this.schematicAnchor1BlockPos, targettedBlockPos.subtract(schematicWorldPosition));
        boolean invalidRealAnchor2Target = (this.realAnchor1BlockPos != null && hitPositionWorld.inRealWorld() &&
                                            !this.validAnchors(this.schematicAnchor1BlockPos, this.schematicAnchor2BlockPos, this.realAnchor1BlockPos, targettedBlockPos));
        boolean invalidTarget = invalidSchematicAnchor2Target || invalidRealAnchor2Target;
        RenderUtil.provideBlockOutlines(lineConsumer, targettedBlockPos, .6,
            invalidTarget ? Color4f.RED : Color4f.WHITE);
        if (invalidTarget)
        {
            return;
        }
        // @formatter:off
        boolean shouldPreviewSchematic = hitPositionWorld.inRealWorld() &&
                                         (this.schematicAnchor2BlockPos != null ||
                                          !hitPositionWorld.inSchematicWorld()) &&
                                         (this.schematicAnchor2BlockPos == null ||
                                          this.realAnchor1BlockPos != null);
        // @formatter:on
        if (!shouldPreviewSchematic)
        {
            return;
        }
        ISchematic schematic = schematicWorld.getSchematic();
        BlockPos _realAnchor1BlockPos = this.realAnchor1BlockPos;
        BlockPos _realAnchor2BlockPos = this.realAnchor2BlockPos;
        if (this.realAnchor1BlockPos == null)
        {
            _realAnchor1BlockPos = targettedBlockPos;
        }
        else
        {
            _realAnchor2BlockPos = targettedBlockPos;
        }
        int transformation
            = this.getTransformationFromAnchors(this.schematicAnchor1BlockPos, this.schematicAnchor2BlockPos, _realAnchor1BlockPos, _realAnchor2BlockPos);
        Vec3i newOrigin
            = _realAnchor1BlockPos.subtract(Transformation.getTransformedBlockPos(schematic, this.schematicAnchor1BlockPos, transformation));
        Transformation.withTransformedDimensions(schematic, transformation, (width, height, length) -> RenderUtil.provideCuboidWithDimensions(lineConsumer, newOrigin.getX(), newOrigin.getY(), newOrigin.getZ(), width, height, length, Color4f.WHITE));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (!event.phase.equals(TickEvent.Phase.START) || !event.side.equals(Side.CLIENT))
        {
            return;
        }
        if (this.moveCounter == 0)
        {
            RenderSchematic.INSTANCE.refresh();
        }
        if (this.moveCounter >= 0)
        {
            this.moveCounter--;
        }
    }

    @SubscribeEvent
    public void onFrame(TickEvent.RenderTickEvent event)
    {
        EntityPlayerSP player = this.minecraft.thePlayer;
        if (!event.phase.equals(TickEvent.Phase.END) || !this.currentlyPerformingMove() || player == null)
        {
            return;
        }
        WorldClient realWorld = this.minecraft.theWorld;
        SchematicWorld schematicWorld = ClientProxy.schematic;
        if (realWorld == null || schematicWorld == null)
        {
            this.resetMoveState();
            return;
        }
        if (!this.isSchematicTethered)
        {
            return;
        }
        MBlockPos schematicWorldPosition = schematicWorld.position;
        float partialTicks = MinecraftUtil.getPartialTicks();
        Vec3 eyes = player.getPositionEyes(partialTicks);
        Vec3 look = player.getLook(partialTicks);
        Vec3i newOrigin;
        boolean altHeld = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
        calcNewOrigin:
        {
            notAltHeld:
            if (!altHeld)
            {
                Vec3 reach = eyes.addVector(
                    look.xCoord * this.rayTraceDistance,
                    look.yCoord * this.rayTraceDistance, look.zCoord * this.rayTraceDistance);
                Optional<MovingObjectPosition> hitPositionOptional
                    = MinecraftUtil.rayTraceBlocks(realWorld, eyes, reach);
                if (!hitPositionOptional.isPresent())
                {
                    break notAltHeld;
                }
                MovingObjectPosition hitPosition = hitPositionOptional.get();
                if (!hitPosition.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK))
                {
                    break notAltHeld;
                }
                ISchematic schematic = schematicWorld.getSchematic();
                BlockPos prePushOrigin = hitPosition.getBlockPos().subtract(this.schematicAnchor1BlockPos);
                Vec3i sideDirection = hitPosition.sideHit.getDirectionVec();
                // @formatter:off
                Vec3i offset = VecUtil.vec3iIsPositive(sideDirection) ?
                               this.schematicAnchor1BlockPos.add(1, 1, 1) :
                               new BlockPos(
                                   schematic.getWidth() - this.schematicAnchor1BlockPos.getX(),
                                   schematic.getHeight() - this.schematicAnchor1BlockPos.getY(),
                                   schematic.getLength() - this.schematicAnchor1BlockPos.getZ());
                // @formatter:on
                Vec3i relevantOffset = VecUtil.vec3iMul(offset, sideDirection);
                newOrigin = prePushOrigin.add(relevantOffset);
                break calcNewOrigin;
            }
            Vec3 end = eyes.addVector(
                look.xCoord * this.anchorDistance,
                look.yCoord * this.anchorDistance, look.zCoord * this.anchorDistance);
            newOrigin = VecUtil.roundVec3(end.subtract(this.anchorPosition));
        }
        if (newOrigin.equals(schematicWorldPosition))
        {
            return;
        }
        schematicWorldPosition.set(newOrigin);
        this.resetMoveCounter();
    }

}
