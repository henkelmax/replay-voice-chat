package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FakeVoicechatConnection extends ClientVoicechatConnection {

    public FakeVoicechatConnection(ClientVoicechat client, InitializationData data) throws IOException {
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
    public DatagramSocket getSocket() {
        return null;
    }

    @Override
    public InetAddress getAddress() {
        return InetAddress.getLoopbackAddress();
    }

    @Override
    public boolean isAuthenticated() {
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
    public void sendToServer(NetworkMessage message) throws Exception {
    }
}
