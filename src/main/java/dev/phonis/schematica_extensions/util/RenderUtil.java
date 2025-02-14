package dev.phonis.schematica_extensions.util;

import dev.phonis.schematica_extensions.color.Color4f;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class RenderUtil
{

    @FunctionalInterface
    public interface LineConsumer
    {

        void consume(double sx, double sy, double sz, double ex, double ey, double ez, Color4f color);

    }

    public static void provideBlockOutlines(LineConsumer lineConsumer, BlockPos blockPos, Color4f color)
    {
        RenderUtil.provideBlockOutlines(lineConsumer, blockPos, .5, color);
    }

    public static void provideBlockOutlines(LineConsumer lineConsumer, BlockPos blockPos, double radius, Color4f color)
    {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        RenderUtil.provideBlockOutlines(lineConsumer, x + .5, y + .5, z + .5, radius, color);
    }

    public static void provideBlockOutlines(LineConsumer lineConsumer, Vec3 center, double radius, Color4f color)
    {
        RenderUtil.provideBlockOutlines(lineConsumer, center.xCoord, center.yCoord, center.zCoord, radius, color);
    }

    public static void provideBlockOutlines(LineConsumer lineConsumer, double centerX, double centerY, double centerZ,
                                            double radius, Color4f color)
    {
        double minX = centerX - radius;
        double minY = centerY - radius;
        double minZ = centerZ - radius;
        double maxX = centerX + radius;
        double maxY = centerY + radius;
        double maxZ = centerZ + radius;
        RenderUtil.provideCuboid(lineConsumer, minX, minY, minZ, maxX, maxY, maxZ, color);
    }

    public static void provideCuboidWithDimensions(LineConsumer lineConsumer, double minX, double minY, double minZ,
                                                   double xWidth, double height, double zWidth, Color4f color)
    {
        RenderUtil.provideCuboid(lineConsumer, minX, minY, minZ, minX + xWidth, minY + height, minZ + zWidth, color);
    }

    public static void provideCuboid(LineConsumer lineConsumer, double minX, double minY, double minZ, double maxX,
                                     double maxY, double maxZ, Color4f color)
    {
        lineConsumer.consume(minX, minY, minZ, maxX, minY, minZ, color);
        lineConsumer.consume(minX, maxY, minZ, maxX, maxY, minZ, color);
        lineConsumer.consume(minX, minY, maxZ, maxX, minY, maxZ, color);
        lineConsumer.consume(minX, maxY, maxZ, maxX, maxY, maxZ, color);
        lineConsumer.consume(minX, minY, minZ, minX, maxY, minZ, color);
        lineConsumer.consume(maxX, minY, minZ, maxX, maxY, minZ, color);
        lineConsumer.consume(minX, minY, maxZ, minX, maxY, maxZ, color);
        lineConsumer.consume(maxX, minY, maxZ, maxX, maxY, maxZ, color);
        lineConsumer.consume(minX, minY, minZ, minX, minY, maxZ, color);
        lineConsumer.consume(maxX, minY, minZ, maxX, minY, maxZ, color);
        lineConsumer.consume(minX, maxY, minZ, minX, maxY, maxZ, color);
        lineConsumer.consume(maxX, maxY, minZ, maxX, maxY, maxZ, color);
    }

    public static void provideLine(LineConsumer lineConsumer, Vec3 start, Vec3 end, Color4f color)
    {
        lineConsumer.consume(start.xCoord, start.yCoord, start.zCoord, end.xCoord, end.yCoord, end.zCoord, color);
    }

}
