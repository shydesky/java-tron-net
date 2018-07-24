package org.tron.net.common.net.udp.message.discover;

import static org.tron.net.common.net.udp.message.UdpMessageTypeEnum.DISCOVER_PING;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.tron.net.common.net.udp.message.Message;
import org.tron.net.services.detection.pojo.Node;
import org.tron.net.common.utils.ByteArray;
import org.tron.net.common.config.Args;
import org.tron.net.protos.Discover;
import org.tron.net.protos.Discover.Endpoint;

@Slf4j
public class PingMessage extends Message {

    private Discover.PingMessage pingMessage;

    public PingMessage(byte[] data) throws Exception{
        super(DISCOVER_PING, data);
        this.pingMessage = Discover.PingMessage.parseFrom(data);
    }

    public PingMessage(Node from, Node to) {
        super(DISCOVER_PING, null);
        Endpoint fromEndpoint = Endpoint.newBuilder()
                .setNodeId(ByteString.copyFrom(from.getId()))
                .setPort(from.getPort())
                .setAddress(ByteString.copyFrom(ByteArray.fromString(from.getHost())))
                .build();
        Endpoint toEndpoint = Endpoint.newBuilder()
                .setNodeId(ByteString.copyFrom(to.getId()))
                .setPort(to.getPort())
                .setAddress(ByteString.copyFrom(ByteArray.fromString(to.getHost())))
                .build();
        this.pingMessage = Discover.PingMessage.newBuilder()
                .setVersion(Args.getInstance().getNodeP2pVersion())
                .setFrom(fromEndpoint)
                .setTo(toEndpoint)
                .setTimestamp(System.currentTimeMillis())
                .build();
        this.data = this.pingMessage.toByteArray();
    }

    public int getVersion(){
        return this.pingMessage.getVersion();
    }

    public Node getTo() {
        Endpoint to = this.pingMessage.getTo();
        Node node = new Node(to.getNodeId().toByteArray(),
                ByteArray.toStr(to.getAddress().toByteArray()), to.getPort());
        return node;
    }

    @Override
    public Node getFrom() {
        return Message.getNode(pingMessage.getFrom());
    }

    @Override
    public String toString() {
        return "[pingMessage: " + pingMessage;
    }

}
