package org.sandboxpowered.sandbox.fabric.mixin.impl.block;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.Blocks;
import net.minecraft.block.InventoryProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.state.StateFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import org.sandboxpowered.sandbox.api.block.Block;
import org.sandboxpowered.sandbox.api.block.Material;
import org.sandboxpowered.sandbox.api.block.entity.BlockEntity;
import org.sandboxpowered.sandbox.api.component.Component;
import org.sandboxpowered.sandbox.api.component.Components;
import org.sandboxpowered.sandbox.api.entity.Entity;
import org.sandboxpowered.sandbox.api.entity.player.Hand;
import org.sandboxpowered.sandbox.api.entity.player.PlayerEntity;
import org.sandboxpowered.sandbox.api.item.Item;
import org.sandboxpowered.sandbox.api.item.ItemStack;
import org.sandboxpowered.sandbox.api.item.Items;
import org.sandboxpowered.sandbox.api.state.BlockState;
import org.sandboxpowered.sandbox.api.util.Direction;
import org.sandboxpowered.sandbox.api.util.InteractionResult;
import org.sandboxpowered.sandbox.api.util.Mono;
import org.sandboxpowered.sandbox.api.util.math.Position;
import org.sandboxpowered.sandbox.api.util.math.Vec3f;
import org.sandboxpowered.sandbox.api.world.World;
import org.sandboxpowered.sandbox.api.world.WorldReader;
import org.sandboxpowered.sandbox.fabric.internal.SandboxInternal;
import org.sandboxpowered.sandbox.fabric.util.WrappingUtil;
import org.sandboxpowered.sandbox.fabric.util.wrapper.SidedRespective;
import org.sandboxpowered.sandbox.fabric.util.wrapper.StateFactoryImpl;
import org.sandboxpowered.sandbox.fabric.util.wrapper.V2SInventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(net.minecraft.block.Block.class)
@Implements(@Interface(iface = Block.class, prefix = "sbx$", remap = Interface.Remap.NONE))
@Unique
public abstract class MixinBlock implements SandboxInternal.StateFactoryHolder {
    @Shadow
    @Final
    protected StateFactory<net.minecraft.block.Block, net.minecraft.block.BlockState> stateFactory;
    private org.sandboxpowered.sandbox.api.state.StateFactory<Block, BlockState> sandboxFactory;

    @Shadow
    public abstract boolean hasBlockEntity();

    @Shadow
    public abstract void onPlaced(net.minecraft.world.World world_1, BlockPos blockPos_1, net.minecraft.block.BlockState blockState_1, @Nullable LivingEntity livingEntity_1, net.minecraft.item.ItemStack itemStack_1);

    @Shadow
    public abstract net.minecraft.item.Item asItem();

    @Shadow
    public abstract net.minecraft.block.BlockState getDefaultState();

    @Shadow
    @Deprecated
    public abstract boolean isAir(net.minecraft.block.BlockState blockState_1);

    @Shadow
    public abstract void onBroken(IWorld iWorld_1, BlockPos blockPos_1, net.minecraft.block.BlockState blockState_1);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(net.minecraft.block.Block.Settings settings, CallbackInfo info) {
        sandboxFactory = new StateFactoryImpl<>(this.stateFactory, b -> (Block) b, s -> (BlockState) s);
        ((SandboxInternal.StateFactory) this.stateFactory).setSboxFactory(sandboxFactory);
    }

    public Block.Settings sbx$getSettings() {
        return new Block.Settings(Material.AIR);
    }

    @Override
    public org.sandboxpowered.sandbox.api.state.StateFactory getSandboxStateFactory() {
        return sandboxFactory;
    }

    public boolean sbx$isAir(BlockState state) {
        return this.isAir(WrappingUtil.convert(state));
    }

    public BlockState sbx$getBaseState() {
        return (BlockState) this.getDefaultState();
    }

    public InteractionResult sbx$onBlockUsed(World world, Position pos, BlockState state, PlayerEntity player, Hand hand, Direction side, Vec3f hit) {
        return InteractionResult.IGNORE;
    }

    public InteractionResult sbx$onBlockClicked(World world, Position pos, BlockState state, PlayerEntity player) {
        return InteractionResult.IGNORE;
    }

    public Mono<Item> sbx$asItem() {
        Item item = (Item) asItem();
        if (item == Items.AIR)
            return Mono.empty();
        return Mono.of(item);
    }

    public void sbx$onBlockPlaced(World world, Position position, BlockState state, Entity entity, ItemStack itemStack) {
        this.onPlaced(
                WrappingUtil.convert(world),
                WrappingUtil.convert(position),
                WrappingUtil.convert(state),
                null,
                WrappingUtil.convert(itemStack)
        );
    }

    public void sbx$onBlockBroken(World world, Position position, BlockState state) {
        this.onBroken((net.minecraft.world.World) world, (BlockPos) position, (net.minecraft.block.BlockState) state);
    }

    public boolean sbx$hasBlockEntity() {
        return this.hasBlockEntity();
    }

    public <X> Mono<X> sbx$getComponent(WorldReader reader, Position position, BlockState state, Component<X> component, Mono<Direction> side) {
        if (component == Components.INVENTORY_COMPONENT) {
            if (this instanceof InventoryProvider) {
                SidedInventory inventory = ((InventoryProvider) this).getInventory((net.minecraft.block.BlockState) state, (IWorld) reader, (BlockPos) position);
                if (side.isPresent())
                    return Mono.of(new SidedRespective(inventory, side.get())).cast();
                return Mono.of(new V2SInventory(inventory)).cast();
            }
            net.minecraft.block.entity.BlockEntity entity = ((BlockView) reader).getBlockEntity((BlockPos) position);
            if (entity instanceof Inventory) {
                if (side.isPresent() && entity instanceof SidedInventory)
                    return Mono.of(new SidedRespective((SidedInventory) entity, side.get())).cast();
                return Mono.of(new V2SInventory((Inventory) entity)).cast();
            }
        }
        return Mono.empty();
    }

    @Nullable
    public BlockEntity sbx$createBlockEntity(WorldReader reader) {
        if (hasBlockEntity())
            return (BlockEntity) ((BlockEntityProvider) this).createBlockEntity(WrappingUtil.convert(reader));
        return null;
    }

    public boolean sbx$isNaturalStone() {
        return (Object) this == Blocks.STONE || (Object) this == Blocks.GRANITE || (Object) this == Blocks.DIORITE || (Object) this == Blocks.ANDESITE;
    }

    public boolean sbx$isNaturalDirt() {
        return (Object) this == Blocks.DIRT || (Object) this == Blocks.COARSE_DIRT || (Object) this == Blocks.PODZOL;
    }
}