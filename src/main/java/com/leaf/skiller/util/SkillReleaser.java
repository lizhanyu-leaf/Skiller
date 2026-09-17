package com.leaf.skiller.util;

import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.foundation.skill.SkillBundle;
import com.leaf.skiller.foundation.skill.SkillType;
import com.leaf.skiller.foundation.skill.config.SkillContextEnvironment;
import com.leaf.skiller.server.PlayerPressedKeys;
import com.leaf.skiller.server.ServerSkillCache;
import net.minecraft.server.level.ServerPlayer;

public class SkillReleaser {
    private final ServerPlayer player;
    private final SkillComponent skills;

    public SkillReleaser(ServerPlayer player) {
        this.player = player;
        skills = ServerSkillCache.getComponent(player);
    }

    public boolean release(SkillType type, SkillContextEnvironment environment) {
        for (var entry : skills.bindings().entrySet()) {
            int key = entry.getKey();
            SkillBundle bundle = entry.getValue();

            if (!PlayerPressedKeys.isPressed(player, key)) continue;
            if (!bundle.releaseSkills(type, environment)) return false;
        }
        return true;
    }
}
