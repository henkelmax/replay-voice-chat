package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.voicechat.api.ClientVoicechatSocket;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;

import java.net.InetAddress;

public class FakeVoicechatConnection extends ClientVoicechatConnection {

    public FakeVoicechatConnection(ClientVoicechat client, InitializationData data) throws Exception {
        super(client, data);
        super.close();
        ClientCompatibilityManager.INSTANCE.emitVoiceChatConnectedEvent(this);
    }

    @Override
    public void run() {

    }

    @Override
    public void close() {

    }

    @Override
    public ClientVoicechatSocket getSocket() {
        return null;
    }

    @Override
    public InetAddress getAddress() {
        return InetAddress.getLoopbackAddress();
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void checkTimeout() {
    }

    @Override
    public boolean sendToServer(NetworkMessage message) {
        return true;
    }
}
