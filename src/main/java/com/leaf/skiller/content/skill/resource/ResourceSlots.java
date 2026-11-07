package com.leaf.skiller.content.skill.resource;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Locale;

/**
 * Shared NBT parsing for equipment-slot addressing used by slot-bound resources
 * (cooldown, durability, ...).
 * 槽位绑定资源（冷却、耐久等）共用的装备槽NBT解析。
 * <p>
 * The {@value #SLOT_KEY} key holds an {@link EquipmentSlot} name
 * ({@code mainhand}, {@code offhand}, {@code head}, {@code chest}, {@code legs},
 * {@code feet}); a missing key or an unknown name falls back to the main hand.
 * {@value #SLOT_KEY} 键保存 {@link EquipmentSlot} 名称
 * （{@code mainhand}、{@code offhand}、{@code head}、{@code chest}、{@code legs}、{@code feet}）；
 * 键缺失或名称未知时回退主手。
 * </p>
 *
 * @see CooldownResource
 * @see DurabilityResource
 * @since 1.0.0
 * @author Leaf
 */
public final class ResourceSlots {

    /**
     * NBT key of the addressed equipment slot name.
     * 所寻址装备槽名称的NBT键。
     */
    public static final String SLOT_KEY = "slot";

    private ResourceSlots() {
        throw new AssertionError("This class should not be instantiated");
    }

    /**
     * Resolves the equipment slot addressed by the NBT, defaulting to the main hand.
     * 解析NBT寻址的装备槽，默认主手。
     *
     * @param nbt The owner NBT (may carry {@value #SLOT_KEY}) / 拥有者NBT（可含 {@value #SLOT_KEY}）
     * @return The resolved equipment slot / 解析出的装备槽
     * @since 1.0.0
     */
    public static EquipmentSlot of(CompoundTag nbt) {
        String name = nbt.getString(SLOT_KEY).trim().toLowerCase(Locale.ROOT);
        if (name.isEmpty()) return EquipmentSlot.MAINHAND;
        try {
            return EquipmentSlot.byName(name);
        } catch (IllegalArgumentException e) {
            // An unknown slot name falls back to the main hand rather than failing.
            // 未知的槽位名称回退主手，而不是失败。
            return EquipmentSlot.MAINHAND;
        }
    }
}
