package dev.phonis.schematica_extensions.mixins;

import dev.phonis.schematica_extensions.extensions.SchematicMovement;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MinecraftMixin
{

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z"))
    private boolean mouseNext()
    {
        boolean inputFound = Mouse.next();
        while (inputFound && SchematicMovement.INSTANCE.onMouse())
        {
            inputFound = Mouse.next();
        }
        return inputFound;
    }

}
