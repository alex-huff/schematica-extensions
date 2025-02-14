package dev.phonis.schematica_extensions.util;

import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

public class VecUtil
{

    public static Vec3 vec3Mul(Vec3 a, double factor)
    {
        return new Vec3(a.xCoord * factor, a.yCoord * factor, a.zCoord * factor);
    }

    public static Vec3i roundVec3(Vec3 vec)
    {
        return new Vec3i(Math.round(vec.xCoord), Math.round(vec.yCoord), Math.round(vec.zCoord));
    }

    public static boolean vec3iIsPositive(Vec3i a)
    {
        return a.getX() >= 0 && a.getY() >= 0 && a.getZ() >= 0;
    }

    public static Vec3i vec3iMul(Vec3i a, int factor)
    {
        return new Vec3i(a.getX() * factor, a.getY() * factor, a.getZ() * factor);
    }

    public static Vec3i vec3iMul(Vec3i a, Vec3i b)
    {
        return new Vec3i(a.getX() * b.getX(), a.getY() * b.getY(), a.getZ() * b.getZ());
    }

    public static Vec3 fromVec3i(Vec3i vec)
    {
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

}
