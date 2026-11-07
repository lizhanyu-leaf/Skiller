package com.leaf.skiller.content.skill.resource;

import com.leaf.skiller.AllSkillResources;
import com.leaf.skiller.foundation.SkillResource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A resource whose unit is one point of durability on the addressed item.
 * 以被寻址物品上 1 点耐久为单位的资源。
 * <p>
 * The available amount is the item's remaining headroom: by default the item keeps
 * its last durability point, so one point is never consumable and the item can
 * never break through this resource; setting the NBT key {@code keep_last} to
 * {@code false} exposes the final point as well — consuming it destroys the item
 * through vanilla {@link ItemStack#hurtAndBreak}, with the regular break sound and
 * event.
 * 可用数量是物品的剩余余量：默认物品保留最后一点耐久，
 * 因此有一点永远不可消耗，物品不会因本资源而损坏；
 * 将NBT键 {@code keep_last} 设为 {@code false} 则最后一点也可消耗——
 * 消耗它将通过原版 {@link ItemStack#hurtAndBreak} 销毁物品，
 * 并伴随常规的破碎音效与事件。
 * </p>
 * <p>
 * {@link #canConsume} therefore checks whether the item would break: with
 * keep-last enabled a breaking consumption fails, with keep-last disabled it
 * succeeds and the break happens on apply.
 * 因此 {@link #canConsume} 检查的是物品是否会损坏：
 * 开启保留时会导致损坏的消耗失败，
 * 关闭保留时成功，并在执行时发生破碎。
 * </p>
 * <p>
 * The addressed slot comes from the owner NBT {@code slot} key (default main hand,
 * see {@link ResourceSlots}); fractional amounts are floored by the resource owner
 * per the shared rounding strategy.
 * 被寻址槽位来自拥有者NBT的 {@code slot} 键（默认主手，见 {@link ResourceSlots}）；
 * 小数数量由资源拥有者按共享取整策略向下取整。
 * </p>
 *
 * @see SkillResource
 * @see ResourceSlots
 * @see ItemStack#hurtAndBreak(int, net.minecraft.world.entity.LivingEntity, EquipmentSlot)
 * @see AllSkillResources#DURABILITY
 * @since 1.0.0
 * @author Leaf
 */
public class DurabilityResource implements SkillResource {

    /**
     * NBT key toggling the keep-last-durability guarantee (default {@code true}).
     * 控制保留最后一点耐久保证的NBT键（默认 {@code true}）。
     */
    public static final String KEEP_LAST_KEY = "keep_last";

    /**
     * Returns the registry key of this durability resource.
     * 返回此耐久资源的注册表键。
     *
     * @return The {@code skiller:durability} resource key / {@code skiller:durability} 资源键
     * @see AllSkillResources#DURABILITY
     * @since 1.0.0
     */
    @Override
    public ResourceKey<SkillResource> key() {
        return AllSkillResources.DURABILITY.key;
    }

    /**
     * Returns the consumable durability headroom of the addressed item.
     * 返回被寻址物品可消耗的耐久余量。
     * <p>
     * With keep-last enabled (the default) the final durability point is excluded;
     * empty slots and non-damageable items report zero.
     * 开启保留（默认）时排除最后一点耐久；
     * 空槽位与不可损耗物品报告零。
     * </p>
     */
    @Override
    public int getAmount(Player player, CompoundTag nbt) {
        ItemStack stack = player.getItemBySlot(ResourceSlots.of(nbt));
        if (stack.isEmpty() || !stack.isDamageableItem()) return 0;
        // Points until break, inclusive of the last one; keep-last excludes it.
        // 距损坏的可消耗点数（含最后一点）；开启保留时排除它。
        int headroom = stack.getMaxDamage() - stack.getDamageValue();
        return Math.max(0, keepLast(nbt) ? headroom - 1 : headroom);
    }

    /**
     * Consumable when the addressed item can pay the amount: a consumption that
     * would break the item fails under keep-last and succeeds without it.
     * 当被寻址物品付得起该数量时可消耗：
     * 开启保留时会导致物品损坏的消耗失败，关闭时成功。
     */
    @Override
    public boolean canConsume(Player player, int amount, CompoundTag nbt) {
        return amount >= 0 && getAmount(player, nbt) >= amount;
    }

    /**
     * Damages the addressed item by the amount; a large enough amount breaks the
     * item through vanilla {@link ItemStack#hurtAndBreak} (break sound and event
     * included). A no-op for empty slots, non-damageable items and non-positive
     * amounts.
     * 将被寻址物品损耗指定数量；数量足够大时经原版
     * {@link ItemStack#hurtAndBreak} 使物品破碎（包含破碎音效与事件）。
     * 空槽位、不可损耗物品与非正数量均为空操作。
     */
    @Override
    public void consume(Player player, int amount, CompoundTag nbt) {
        if (amount <= 0) return;
        EquipmentSlot slot = ResourceSlots.of(nbt);
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty() || !stack.isDamageableItem()) return;
        stack.hurtAndBreak(amount, player, slot);
    }

    /**
     * Reads the keep-last guarantee from the NBT; missing defaults to enabled.
     * 从NBT读取保留保证；缺失默认开启。
     */
    private static boolean keepLast(CompoundTag nbt) {
        return !nbt.contains(KEEP_LAST_KEY) || nbt.getBoolean(KEEP_LAST_KEY);
    }
}
