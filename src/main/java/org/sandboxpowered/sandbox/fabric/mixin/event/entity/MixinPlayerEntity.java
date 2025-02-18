package org.sandboxpowered.sandbox.fabric.mixin.event.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.sandboxpowered.sandbox.api.entity.LivingEntity;
import org.sandboxpowered.sandbox.api.event.ItemEvent;
import org.sandboxpowered.sandbox.api.event.entity.LivingEvent;
import org.sandboxpowered.sandbox.fabric.event.EventDispatcher;
import org.sandboxpowered.sandbox.fabric.util.WrappingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends net.minecraft.entity.LivingEntity {

    public MixinPlayerEntity(EntityType<? extends net.minecraft.entity.LivingEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void onDeath(DamageSource source, CallbackInfo info) {
        LivingEvent.Death event = EventDispatcher.publish(new LivingEvent.Death((LivingEntity) this));
        if (event.isCancelled())
            info.cancel();
    }

    /**
     * @author B0undarybreaker
     */
    @Inject(method = "getArrowType", at = @At("RETURN"))
    public void getModifiedArrowType(ItemStack weapon, CallbackInfoReturnable<ItemStack> info) {
        ItemEvent.GetArrowType event = EventDispatcher.publish(new ItemEvent.GetArrowType(
                WrappingUtil.cast(weapon, org.sandboxpowered.sandbox.api.item.ItemStack.class),
                WrappingUtil.cast(info.getReturnValue(), org.sandboxpowered.sandbox.api.item.ItemStack.class)
        ));
        info.setReturnValue(WrappingUtil.convert(event.getArrow()));
    }
}
