package dev.phonis.schematica_extensions.util;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public class SchematicUtil
{

    public enum HitPositionWorld
    {
        REAL, SCHEMATIC, TIE, NONE;

        public boolean inSchematicWorld()
        {
            switch (this)
            {
                case SCHEMATIC:
                case TIE:
                    return true;
            }
            return false;
        }

        public boolean inRealWorld()
        {
            switch (this)
            {
                case REAL:
                case TIE:
                    return true;
            }
            return false;
        }
    }

    public static Pair<MovingObjectPosition, HitPositionWorld> rayTraceBlocks(WorldClient realWorld,
                                                                              SchematicWorld schematicWorld,
                                                                              EntityPlayerSP player,
                                                                              int rayTraceDistance)
    {
        float partialTicks = MinecraftUtil.getPartialTicks();
        Vec3 eyes = player.getPositionEyes(partialTicks);
        Vec3 look = player.getLook(partialTicks);
        Vec3 reach = eyes.addVector(
            look.xCoord * rayTraceDistance, look.yCoord * rayTraceDistance, look.zCoord * rayTraceDistance);
        Optional<MovingObjectPosition> realHitPositionOptional = MinecraftUtil.rayTraceBlocks(realWorld, eyes, reach);
        Optional<MovingObjectPosition> schematicHitPositionOptional
            = SchematicUtil.schematicRayTraceBlocks(schematicWorld, eyes, reach);
        MBlockPos schematicWorldPosition = schematicWorld.position;
        double impossiblyFarSquaredDistance = rayTraceDistance * rayTraceDistance + 1;
        boolean hitInRealWorld = realHitPositionOptional.map(MinecraftUtil::rayTraceHitBlock).orElse(false);
        boolean hitInSchematicWorld = schematicHitPositionOptional.map(MinecraftUtil::rayTraceHitBlock).orElse(false);
        double squaredDistanceToRealHitPosition = hitInRealWorld
                                                  ? eyes.squareDistanceTo(realHitPositionOptional.get().hitVec)
                                                  : impossiblyFarSquaredDistance;
        double squaredDistanceToSchematicHitPosition = hitInSchematicWorld
                                                       ? eyes.squareDistanceTo(schematicHitPositionOptional.get().hitVec.addVector(schematicWorldPosition.getX(), schematicWorldPosition.getY(), schematicWorldPosition.getZ()))
                                                       : impossiblyFarSquaredDistance;
        if (hitInRealWorld && hitInSchematicWorld && realHitPositionOptional.get().getBlockPos()
            .equals(schematicHitPositionOptional.get().getBlockPos().add(schematicWorldPosition)) &&
            MathUtil.epsilonEquals(squaredDistanceToRealHitPosition, squaredDistanceToSchematicHitPosition))
        {
            return new ImmutablePair<>(realHitPositionOptional.get(), SchematicUtil.HitPositionWorld.TIE);
        }
        if (squaredDistanceToRealHitPosition == impossiblyFarSquaredDistance &&
            squaredDistanceToSchematicHitPosition == impossiblyFarSquaredDistance)
        {
            return new ImmutablePair<>(null, SchematicUtil.HitPositionWorld.NONE);
        }
        if (squaredDistanceToRealHitPosition < squaredDistanceToSchematicHitPosition)
        {
            return new ImmutablePair<>(realHitPositionOptional.get(), SchematicUtil.HitPositionWorld.REAL);
        }
        MovingObjectPosition schematicHitPosition = schematicHitPositionOptional.get();
        schematicHitPosition.blockPos = schematicHitPosition.blockPos.add(schematicWorldPosition);
        schematicHitPosition.hitVec
            = schematicHitPosition.hitVec.addVector(schematicWorldPosition.getX(), schematicWorldPosition.getY(), schematicWorldPosition.getZ());
        return new ImmutablePair<>(schematicHitPosition, SchematicUtil.HitPositionWorld.SCHEMATIC);
    }

    public static Optional<MovingObjectPosition> schematicRayTraceBlocks(SchematicWorld schematicWorld, Vec3 start,
                                                                         Vec3 end)
    {
        return SchematicUtil.schematicRayTraceBlocks(schematicWorld, start, end, false, false, true);
    }

    public static Optional<MovingObjectPosition> schematicRayTraceBlocks(SchematicWorld schematicWorld, Vec3 start,
                                                                         Vec3 end, boolean stopOnLiquid,
                                                                         boolean ignoreBlockWithoutBoundingBox,
                                                                         boolean returnLastUncollidableBlock)
    {
        if (!schematicWorld.isRendering)
        {
            return Optional.empty();
        }
        Vec3 schematicWorldPositionVec = VecUtil.fromVec3i(schematicWorld.position);
        start = start.subtract(schematicWorldPositionVec);
        end = end.subtract(schematicWorldPositionVec);
        return Optional.ofNullable(schematicWorld.rayTraceBlocks(start, end, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock));
    }

}
