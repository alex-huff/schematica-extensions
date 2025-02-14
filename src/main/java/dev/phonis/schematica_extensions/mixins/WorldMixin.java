package dev.phonis.schematica_extensions.mixins;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(World.class)
public class WorldMixin
{

    @ModifyConstant(method = "rayTraceBlocks(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;ZZZ)Lnet/minecraft/util/MovingObjectPosition;", constant = @Constant(intValue = 200))
    private int rayTraceMaxDistance(int ignored)
    {
        return 500;
    }

}
