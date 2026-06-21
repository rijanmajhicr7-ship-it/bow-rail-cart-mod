package com.bowraillauncher.mixin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void onBowReleased(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (world.isClient()) return;
        if (!(user instanceof ServerPlayerEntity player)) return;
        int usedTicks = 72000 - remainingUseTicks;
        if (usedTicks < 3) return;
        ItemStack railStack = null;
        ItemStack tntCartStack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (railStack == null && s.getItem() == Items.RAIL) railStack = s;
            if (tntCartStack == null && s.getItem() == Items.TNT_MINECART) tntCartStack = s;
        }
        if (railStack == null || tntCartStack == null) return;
        Direction facing = player.getHorizontalFacing();
        BlockPos placePos = player.getBlockPos().offset(facing);
        if (!world.getBlockState(placePos).isAir() && !world.getBlockState(placePos).isReplaceable()) return;
        BlockPos below = placePos.down();
        if (!world.getBlockState(below).isSolidBlock(world, below))
            world.setBlockState(below, Blocks.STONE.getDefaultState());
        world.setBlockState(placePos, Blocks.RAIL.getDefaultState());
        railStack.decrement(1);
        TntMinecartEntity cart = new TntMinecartEntity(world, placePos.getX()+0.5, placePos.getY(), placePos.getZ()+0.5);
        world.spawnEntity(cart);
        tntCartStack.decrement(1);
    }
}
