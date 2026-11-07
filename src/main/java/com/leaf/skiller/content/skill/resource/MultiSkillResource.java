package com.leaf.skiller.content.skill.resource;

import com.leaf.skiller.AllSkillResources;
import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.Consumable;
import com.leaf.skiller.foundation.SkillResource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A resource that consumes several {@link SkillResource}s at once.
 * 同时消耗多个 {@link SkillResource} 的资源。
 * <p>
 * The members are not fixed at construction time: the instance NBT's
 * {@value #RESOURCES_KEY} key holds a string list of resource registry ids
 * (e.g. {@code ["skiller:mana", "skiller:stamina"]}), resolved on every access.
 * Unknown ids and self-references are skipped, so a hand-edited list can never
 * crash or cycle.
 * 成员不在构造时固定：实例NBT的 {@value #RESOURCES_KEY} 键保存资源注册表ID的
 * 字符串列表（例如 {@code ["skiller:mana", "skiller:stamina"]}），每次访问时解析。
 * 未知ID与自引用会被跳过，因此手工编辑的列表不会导致崩溃或成环。
 * </p>
 * <p>
 * Consumption is atomic, following the same accumulate → check-all → apply-all
 * pattern as {@code SkillBundle#releaseSkills}: the delayed consumable fans each
 * cost out to every member and only applies when <em>all</em> members can pay,
 * so a partial deduction (paying some members but not others) can never happen.
 * 消耗是原子的，遵循与 {@code SkillBundle#releaseSkills} 相同的
 * 累积 → 全部检查 → 全部执行 模式：延迟可消耗项把成本分发给所有成员，
 * 只有当<em>所有</em>成员都付得起时才执行，
 * 因此不会出现部分扣除（付了部分成员而另一些没付）的情况。
 * </p>
 * <p>
 * Like any other resource, the multi resource can be wrapped by a rate expression
 * (the instance NBT {@code rate} key handled by
 * {@link com.leaf.skiller.foundation.SkillResourceOwner}); the rated amount is then
 * consumed from every member, i.e. the rate applies to the whole bundle.
 * 与其他资源一样，多重资源也可以被变化率表达式包装
 * （实例NBT的 {@code rate} 键，由
 * {@link com.leaf.skiller.foundation.SkillResourceOwner} 处理）；
 * 变化后的数量会从每个成员消耗，即变化率作用于整体。
 * </p>
 *
 * @see SkillResource
 * @see com.leaf.skiller.foundation.SkillResourceOwner
 * @see AllSkillResources#MULTI
 * @since 1.0.0
 * @author Leaf
 */
public class MultiSkillResource implements SkillResource {

    /**
     * NBT key of the member resource id string list.
     * 成员资源ID字符串列表的NBT键。
     */
    public static final String RESOURCES_KEY = "resources";

    /**
     * Returns the registry key of this multi resource.
     * 返回此多重资源的注册表键。
     *
     * @return The {@code skiller:multi} resource key / {@code skiller:multi} 资源键
     * @see AllSkillResources#MULTI
     * @since 1.0.0
     */
    @Override
    public ResourceKey<SkillResource> key() {
        return AllSkillResources.MULTI.key;
    }

    /**
     * Resolves the member resources from the NBT string list.
     * 从NBT字符串列表解析成员资源。
     * <p>
     * Unparsable ids, unknown registry entries and a reference to the multi key
     * itself are skipped; the self-reference guard prevents infinite recursion when
     * a list accidentally contains {@code skiller:multi}.
     * 无法解析的ID、未知的注册表条目以及指向多重键自身的引用都会被跳过；
     * 自引用守卫防止列表意外包含 {@code skiller:multi} 时无限递归。
     * </p>
     *
     * @param nbt The owner NBT holding {@value #RESOURCES_KEY} / 持有 {@value #RESOURCES_KEY} 的拥有者NBT
     * @return The resolved member resources, possibly empty / 解析出的成员资源，可能为空
     * @since 1.0.0
     */
    private List<SkillResource> members(CompoundTag nbt) {
        List<SkillResource> result = new ArrayList<>();
        ListTag list = nbt.getList(RESOURCES_KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id == null) continue;
            if (id.equals(AllSkillResources.MULTI.key.location())) continue;
            SkillResource member = SkillerBuiltInRegistries.SKILL_RESOURCES.get(id);
            if (member != null && member != this) result.add(member);
        }
        return result;
    }

    /**
     * Returns the smallest member amount — the binding constraint of the bundle.
     * 返回最小的成员数量——即整组的瓶颈约束。
     */
    @Override
    public int getAmount(Player player, CompoundTag nbt) {
        return members(nbt).stream()
                .mapToInt(member -> member.getAmount(player, nbt))
                .min()
                .orElse(0);
    }

    /**
     * Returns true only when every member can pay the amount.
     * 仅当所有成员都付得起该数量时返回 true。
     */
    @Override
    public boolean canConsume(Player player, int amount, CompoundTag nbt) {
        for (SkillResource member : members(nbt)) {
            if (!member.canConsume(player, amount, nbt)) return false;
        }
        return true;
    }

    /**
     * Direct consumption of every member. Prefer the delayed path, which checks
     * all members first.
     * 直接消耗所有成员。优先使用会先检查所有成员的延迟路径。
     */
    @Override
    public void consume(Player player, int amount, CompoundTag nbt) {
        for (SkillResource member : members(nbt)) {
            member.consume(player, amount, nbt);
        }
    }

    /**
     * Atomic immediate consumption: every member is checked before any is consumed.
     * 原子的立即消耗：先检查所有成员，再消耗任何成员。
     */
    @Override
    public Consumable getConsumable(Player player, CompoundTag nbt) {
        return amount -> {
            List<SkillResource> list = members(nbt);
            for (SkillResource member : list) {
                if (!member.canConsume(player, amount, nbt)) return;
            }
            for (SkillResource member : list) {
                member.consume(player, amount, nbt);
            }
        };
    }

    /**
     * Builds a delayed consumable that fans costs out to every member and applies
     * them all-or-nothing.
     * 构建一个把成本分发到所有成员并按全有或全无执行的延迟可消耗项。
     */
    @Override
    public DelayConsumable getDelayConsumable(Player player, CompoundTag nbt) {
        List<DelayConsumable> parts = new ArrayList<>();
        for (SkillResource member : members(nbt)) {
            parts.add(member.getDelayConsumable(player, nbt));
        }
        return new MultiDelayConsumable(player, parts);
    }

    /**
     * A {@link DelayConsumable} that is atomic across all members: each cost is
     * fanned out to every part, the check passes only when every part passes, and
     * the apply consumes from every part.
     * 跨所有成员保持原子的 {@link DelayConsumable}：
     * 每笔成本分发到所有部分，仅当所有部分都通过时检查才通过，
     * 执行时会从每个部分消耗。
     *
     * @see SkillResource.DelayConsumable
     * @since 1.0.0
     */
    private static final class MultiDelayConsumable extends DelayConsumable {

        /**
         * One delayed consumable per member resource.
         * 每个成员资源一个延迟可消耗项。
         */
        private final List<DelayConsumable> parts;

        private MultiDelayConsumable(Player player, List<DelayConsumable> parts) {
            super(player);
            this.parts = parts;
        }

        /**
         * Fans the amount out to every member part.
         * 将数量分发到每个成员部分。
         */
        @Override
        public void consume(int amount) {
            for (DelayConsumable part : parts) {
                part.consume(amount);
            }
        }

        /**
         * Passes only when every member part can pay its accumulated amount.
         * 仅当每个成员部分都付得起其累积数量时通过。
         */
        @Override
        public boolean canConsume() {
            for (DelayConsumable part : parts) {
                if (!part.canConsume()) return false;
            }
            return true;
        }

        /**
         * Consumes the accumulated amount from every member part. Only call after
         * {@link #canConsume()} passed, mirroring the releaseSkills flow.
         * 从每个成员部分消耗累积数量。仅在 {@link #canConsume()} 通过后调用，
         * 与 releaseSkills 流程保持一致。
         */
        @Override
        public void apply() {
            for (DelayConsumable part : parts) {
                part.apply();
            }
        }
    }
}
