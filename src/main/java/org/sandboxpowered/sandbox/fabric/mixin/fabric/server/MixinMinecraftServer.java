package org.sandboxpowered.sandbox.fabric.mixin.fabric.server;

import net.minecraft.resource.ResourcePackContainer;
import net.minecraft.resource.ResourcePackContainerManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelProperties;
import org.sandboxpowered.sandbox.fabric.client.AddonResourceCreator;
import org.sandboxpowered.sandbox.fabric.server.SandboxServer;
import org.sandboxpowered.sandbox.fabric.util.ArrayUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Shadow
    @Final
    private ResourcePackContainerManager<ResourcePackContainer> dataPackContainerManager;

    @ModifyVariable(method = "main", at = @At("HEAD"), ordinal = 0)
    private static String[] main(String[] args) {
        SandboxServer.ARGS = args;
        ArrayUtil.removeAll(args, "-noaddons");
        return args;
    }

    @Inject(method = "shutdown",
            at = @At(value = "TAIL")
    )
    public void shutdown(CallbackInfo info) {
        SandboxServer.INSTANCE.shutdown();
    }

    @Inject(method = "loadWorldDataPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackContainerManager;callCreators()V", shift = At.Shift.BEFORE))
    public void loadDatapacks(File file_1, LevelProperties levelProperties_1, CallbackInfo info) {
        this.dataPackContainerManager.addCreator(new AddonResourceCreator());
    }

    /**
     * @author B0undarybreaker
     */
    @ModifyConstant(method = "getServerModName")
    public String getServerModName(String original) {
        return "Sandbox";
    }
}