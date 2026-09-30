package com.leaf.skiller;

import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.foundation.SkillData;
import com.leaf.skiller.foundation.skill.SkillBundle;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib2.plugin.LDLibPlugin;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;

@LDLibPlugin
public class SkillerLDLibPlugin implements ILDLibPlugin {
    @Override
    public void onLoad() {

    }

    public static void loadAccessor() {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(SkillComponent.class)
                .codec(SkillComponent.CODEC)
                .streamCodec(SkillComponent.STREAM_CODEC)
                .copyMark(SkillComponent::copy)
                .build());
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(SkillBundle.class)
                .codec(SkillBundle.CODEC)
                .streamCodec(SkillBundle.STREAM_CODEC)
                .copyMark(SkillBundle::new)
                .build());
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(SkillData.class)
                .codec(SkillData.CODEC)
                .streamCodec(SkillData.STREAM_CODEC)
                .build());
    }
}
