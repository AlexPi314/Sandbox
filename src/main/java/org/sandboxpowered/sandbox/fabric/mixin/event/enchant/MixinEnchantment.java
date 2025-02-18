package org.sandboxpowered.sandbox.fabric.mixin.event.enchant;

import net.minecraft.item.ItemStack;
import org.sandboxpowered.sandbox.api.enchant.Enchantment;
import org.sandboxpowered.sandbox.api.event.EnchantmentEvent;
import org.sandboxpowered.sandbox.api.event.EventResult;
import org.sandboxpowered.sandbox.fabric.event.EventDispatcher;
import org.sandboxpowered.sandbox.fabric.util.WrappingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.enchantment.Enchantment.class)
public abstract class MixinEnchantment {

    @Inject(method = "isAcceptableItem", at = @At(value = "HEAD"), cancellable = true)
    public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        EnchantmentEvent.AcceptableItem event = EventDispatcher.publish(new EnchantmentEvent.AcceptableItem((Enchantment) this, WrappingUtil.cast(stack, org.sandboxpowered.sandbox.api.item.ItemStack.class)));
        if (event.getResult() != EventResult.IGNORE) {
            info.setReturnValue(event.getResult() == EventResult.SUCCESS);
        }
    }

    @Inject(method = "isDifferent", at = @At(value = "HEAD"), cancellable = true)
    public void isDifferent(net.minecraft.enchantment.Enchantment other, CallbackInfoReturnable<Boolean> info) {
        EnchantmentEvent.Compatible event = EventDispatcher.publish(new EnchantmentEvent.Compatible((Enchantment) this, (Enchantment) other));
        if (event.getResult() != EventResult.IGNORE) {
            info.setReturnValue(event.getResult() == EventResult.SUCCESS);
        }
    }
}