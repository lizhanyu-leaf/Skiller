package com.leaf.skiller.networks;

import com.leaf.skiller.client.ClientSkillCache;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.server.PlayerPressedKeys;
import com.leaf.skiller.server.ServerSkillCache;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;

import java.util.Objects;

public class SkillComponentNetworks {
    @RPCPacket("skillerKeyPressed")
    public static void onKeyPressed(RPCSender sender, int keyIndex, boolean pressed) {
        PlayerPressedKeys.setKeyPressed(
                Objects.requireNonNull(sender.asPlayer()), keyIndex, pressed);
    }

    @RPCPacket("skillerSyncSkillComponent")
    public static void onSyncSkillComponent(RPCSender sender, SkillComponent skillComponent) {
        ServerSkillCache.onSync(sender.asPlayer(), skillComponent);
    }

    @RPCPacket("skillerSyncRequest")
    public static void OnSyncRequest(RPCSender sender, boolean enable) {
        ClientSkillCache.handleSyncRequest(enable);
    }
}
