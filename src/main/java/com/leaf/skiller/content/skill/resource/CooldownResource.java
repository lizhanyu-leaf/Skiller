package com.leaf.skiller.content.skill.resource;

import com.leaf.skiller.AllSkillResources;
import com.leaf.skiller.foundation.SkillResource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A resource whose unit is one tick of vanilla item cooldown.
 * 以原版物品冷却 1 tick 为单位的资源。
 * <p>
 * Availability is binary: while the addressed item is not on cooldown the resource
 * is effectively infinite; while it is on cooldown (or the slot is empty) there is
 * nothing to consume. Consuming applies the requested amount as vanilla cooldown
 * ticks to the addressed item — keyed by item type, so all items of the same type
 * share the cooldown, exactly like thrown ender pearls.
 * 可用性是二值的：被寻址物品不在冷却时资源实际上是无限的；
 * 在冷却中（或槽位为空）则没有可消耗的东西。
 * 消耗会把请求的数量作为原版冷却tick施加到被寻址物品上——
 * 以物品类型为键，因此同类物品共享冷却，与投掷出的末影珍珠完全一致。
 * </p>
 * <p>
 * The addressed slot comes from the owner NBT {@code slot} key (an
 * {@link EquipmentSlot} name, defaulting to the main hand — see {@link ResourceSlots}).
 * 被寻址槽位来自拥有者NBT的 {@code slot} 键（{@link EquipmentSlot} 名称，
 * 默认主手——见 {@link ResourceSlots}）。
 * </p>
 * <p>
 * Fractional amounts (which can only come from a rate expression) are floored by
 * {@link com.leaf.skiller.foundation.SkillResourceOwner} before reaching this
 * resource, per the shared rounding strategy.
 * 小数数量（只可能来自变化率表达式）会在到达本资源前由
 * {@link com.leaf.skiller.foundation.SkillResourceOwner} 向下取整，遵循共享取整策略。
 * </p>
 *
 * @see SkillResource
 * @see ResourceSlots
 * @see AllSkillResources#COOLDOWN
 * @since 1.0.0
 * @author Leaf
 */
public class CooldownResource implements SkillResource {

    /**
     * Amount reported while the addressed item is not on cooldown — effectively
     * unlimited availability.
     * 被寻址物品不在冷却时报告的数量——实际上是无限可用。
     */
    public static final int AVAILABLE = Integer.MAX_VALUE;

    /**
     * Returns the registry key of this cooldown resource.
     * 返回此冷却资源的注册表键。
     *
     * @return The {@code skiller:cooldown} resource key / {@code skiller:cooldown} 资源键
     * @see AllSkillResources#COOLDOWN
     * @since 1.0.0
     */
    @Override
    public ResourceKey<SkillResource> key() {
        return AllSkillResources.COOLDOWN.key;
    }

    /**
     * Returns {@link #AVAILABLE} while the addressed item is off cooldown, and zero
     * while it is cooling down (or the slot is empty).
     * 被寻址物品不在冷却时返回 {@link #AVAILABLE}；
     * 在冷却中（或槽位为空）时返回零。
     */
    @Override
    public int getAmount(Player player, CompoundTag nbt) {
        ItemStack stack = player.getItemBySlot(slot(nbt));
        if (stack.isEmpty()) return 0;
        return player.getCooldowns().isOnCooldown(stack.getItem()) ? 0 : AVAILABLE;
    }

    /**
     * Consumable exactly when the addressed item exists and is not cooling down;
     * the requested amount is irrelevant to this check.
     * 当且仅当被寻址物品存在且不在冷却时可消耗；
     * 请求的数量与本检查无关。
     */
    @Override
    public boolean canConsume(Player player, int amount, CompoundTag nbt) {
        ItemStack stack = player.getItemBySlot(slot(nbt));
        return !stack.isEmpty() && !player.getCooldowns().isOnCooldown(stack.getItem());
    }

    /**
     * Applies the amount as vanilla cooldown ticks to the addressed item. A
     * no-op when the slot is empty or the amount is not positive.
     * 将数量作为原版冷却tick施加到被寻址物品上。
     * 槽位为空或数量非正时为空操作。
     */
    @Override
    public void consume(Player player, int amount, CompoundTag nbt) {
        if (amount <= 0) return;
        ItemStack stack = player.getItemBySlot(slot(nbt));
        if (stack.isEmpty()) return;
        Item item = stack.getItem();
        // Cooldowns are keyed by item type: every item of this type is locked,
        // matching vanilla ender-pearl behavior.
        // 冷却以物品类型为键：该类型的所有物品都会被锁定，与原版末影珍珠行为一致。
        player.getCooldowns().addCooldown(item, amount);
    }

    /**
     * Resolves the addressed equipment slot from the NBT.
     * 从NBT解析被寻址的装备槽。
     */
    private static EquipmentSlot slot(CompoundTag nbt) {
        return ResourceSlots.of(nbt);
    }
}
