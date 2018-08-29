package org.tron.net.services.detection.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tron.net.common.net.udp.handler.EventHandler;
import org.tron.net.common.net.udp.handler.UdpEvent;
import org.tron.net.common.net.udp.message.Message;
import org.tron.net.common.net.udp.message.discover.FindNodeMessage;
import org.tron.net.common.net.udp.message.discover.NeighborsMessage;
import org.tron.net.common.net.udp.message.discover.PongMessage;
import org.tron.net.services.detection.pojo.Node;
import org.tron.net.common.utils.NetUtil;
import org.tron.net.common.config.Args;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


@Slf4j
@Component
public class NodeDetection implements EventHandler {

    private ArrayList<Node> bootNodes = new ArrayList<>();
    private Args args = Args.getInstance();
    private Consumer<UdpEvent> messageSender;
    private Node homeNode;
    private ConcurrentHashMap<String, Node> allNode = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Node> allDetectedNode = new ConcurrentHashMap<>();
    private Map<String, NodeHandler> nodeHandlerMap = new ConcurrentHashMap<>();
    private boolean forTrueNodeId = true;

    public HashMap<String, Integer> msgReceived = new HashMap();
    public HashMap<String, Integer> msgSended = new HashMap();
    public static HashMap<String, NodeHandler> tempNetNode = new HashMap<>();  // record the detect outcome current round
    public static HashMap<String, NodeHandler> currentNetNode = new HashMap<>();  // record the detect outcome last round

    public NodeDetection(){
        homeNode = new Node("c5ee1254039daa38e133642675c4c7713096cb22654be4d893d30cd14a22324c56a13cabc645599092c2fb7e234850ff910b46dcd2ce630a36e4e0e5c2939440".getBytes(), "127.0.0.1",
                18888);
        // initial the public node
        setBootNodes(args.getPublicNode().getIpList());
    }

    public void setBootNodes(ArrayList<String> ipList){
        for(String bootNode: ipList){
            bootNodes.add(Node.instanceOf(bootNode));
        }
    }

    public Consumer<UdpEvent> getMessageSender(){
        return messageSender;
    }

    public ArrayList<Node> getBootNodes(){
        return bootNodes;
    }

     public HashMap<String, NodeHandler> getCurrentNetNode(){
        return currentNetNode;
     }

    public Node getHomeNode(){
        return homeNode;
    }

    public ConcurrentHashMap<String, Node> getAllNode(){
        return allNode;
    }

    public ConcurrentHashMap<String, Node> getAllDetectedNode(){
        return allDetectedNode;
    }

    public void setMessageSender(Consumer<UdpEvent> messageSender) {
        this.messageSender = messageSender;
    }

    public void setNodeHandler(Node node){
        nodeHandlerMap.put(node.getHexId(), new NodeHandler(node, this));
    }

    public NodeHandler getNodeHandler(Node node){
        if(!nodeHandlerMap.containsKey(node.getHexId())){
            setNodeHandler(node);
        }
        return nodeHandlerMap.get(node.getHexId());
    }

    @Override
    public void channelActivated() {
        for (Node node : bootNodes) {
            getNodeHandler(node);
        }
    }

    @Override
    public void handleEvent(UdpEvent udpEvent) {
        Message m = udpEvent.getMessage();
        InetSocketAddress sender = udpEvent.getAddress();

        Node n = new Node(m.getFrom().getId(), sender.getHostString(), sender.getPort());

        // find the node handler
        NodeHandler nodeHandler = getNodeHandler(n);

        switch (m.getType()) {
            case DISCOVER_PONG:
                nodeHandler.handlePong((PongMessage) m);
                break;
            case DISCOVER_FIND_NODE:
                nodeHandler.handleFindNode((FindNodeMessage) m);
                break;
            case DISCOVER_NEIGHBORS:
                if(forTrueNodeId){
                    nodeHandler.handleNeighboursForTrueNodeId((NeighborsMessage) m);
                }else{
                    if(msgReceived.containsKey(nodeHandler.getNode().getHexId())) {
                        msgReceived.put(nodeHandler.getNode().getHexId(),msgReceived.get(nodeHandler.getNode().getHexId()) + 1);
                    }else{
                        msgReceived.put(nodeHandler.getNode().getHexId(), 1);
                    }
                    nodeHandler.handleNeighbours((NeighborsMessage) m);
                }
                break;
        }
    }

    public void sendFindNeighbourMessage(NodeHandler nodeHandler, byte[] targetId){
        FindNodeMessage msg = new FindNodeMessage(getHomeNode(), targetId);
        UdpEvent udpEvent = new UdpEvent(msg, nodeHandler.getInetSocketAddress());
        sendOutbound(udpEvent);
    }

    private void getTrueNodeId(){
        byte[] targetId = new byte[64];
        for (Node node : bootNodes) {
            sendFindNeighbourMessage(getNodeHandler(node), targetId);
        }
    }

    private void clear(){
        allNode.clear();
        logger.info("clear allNode:" + allNode.size());
        NodeDetection.tempNetNode = new HashMap<>();
        logger.info("clear tempNetNode:" + NodeDetection.tempNetNode.size());
        allDetectedNode.clear();
        logger.info("allDetectedNode:" + allDetectedNode.size());
        nodeHandlerMap.clear();
        logger.info("nodeHandlerMap:" + nodeHandlerMap.size());
    }

    public boolean beforeDetect(){
        logger.info("before detect start!");
        clear();
        getTrueNodeId();
        int count = 0;
        while(count < 5){
            try{
                Thread.sleep(10000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            nodeHandlerMap.entrySet().stream().filter(
                    e-> !e.getValue().getNode().getIsFakeNodeId()
            ).forEach(e->allNode.put(e.getValue().getNode().getHexId(), e.getValue().getNode()));
            if(allNode.size() == 0){
                count ++;
            }else{
                forTrueNodeId = false;  // not handle the NeighboursForTrueNodeId msg any more
                logger.info("before detect success!");
                return true;
            }
        }
        logger.error("before detect failure after retry " + count + " times!");
        return false;
    }

    public int doDetect(){
        logger.info("启动！启动节点的数量：" + allNode.size());
        for(Map.Entry<String, Node> entry:allNode.entrySet()){
          logger.info("ss" + entry.getValue().getHost());
        }
        long start = System.currentTimeMillis() / 1000;
        while(allNode.size() > 0){
            allNode.values().forEach(node->{
                if(!allDetectedNode.containsKey(node.getHexId())){
                    detectNeighbour(getNodeHandler(node));
                    allDetectedNode.put(node.getHexId(), node);
                }
                logger.info("allDetectedNode:" + allDetectedNode.size());
            });
            long end = System.currentTimeMillis() / 1000;
            //TODO: optimise the force quit time algorithm
            if(end - start > 100){
                break;
            }
        }

        /*msgReceived.clear();
        logger.info(String.valueOf(msgReceived.size()));
        allNode.values().forEach(node-> {
            try {
                Thread.sleep(10);
            }catch (Exception e){

            }
            NodeHandler handler = nodeHandlerMap.get(node.getHexId());
            handler.sendFindNode(new byte[64]);
        });
        try {
            Thread.sleep(20000);
        }catch (Exception e){

        }*/
        // generate detect outcome this round
        allNode.entrySet().forEach(e->{
            String key = e.getValue().getHost() + ":" +  e.getValue().getPort();
            NodeDetection.tempNetNode.put(key, getNodeHandler(e.getValue()));
        });
        // one round ends, assign the tempNetNode to the currentNetNode.
        NodeDetection.currentNetNode = NodeDetection.tempNetNode;
        return NodeDetection.currentNetNode.size();
    }

    public void statisticsAllNode(){
        Iterator<String> iter = nodeHandlerMap.keySet().iterator();
        String keyp;
        while(iter.hasNext()){
            keyp = iter.next();
            logger.info("neighbours:" + nodeHandlerMap.get(keyp).getAllNeighbourCount());
        }
        logger.info("Detect neighbours of the nodes ends!");
        allNode.entrySet().forEach(e->{
            String key = e.getValue().getHost() + ":" +  e.getValue().getPort();
            NodeDetection.tempNetNode.put(key, getNodeHandler(e.getValue()));
        });
        NodeDetection.currentNetNode = NodeDetection.tempNetNode;
    }

    private void detectNeighbour(NodeHandler nodeHandler){
        int distanceBegin = 240;
        int distanceEnd = 256;

        Node node = nodeHandler.getNode();
        byte[] targetId;
        for (int i = distanceBegin + 1; i <= distanceEnd; i++) {
            try {
                Thread.sleep(3);
            }catch (Exception e){

            }
            targetId = NetUtil.mockTargetIdWithDistance(node.getId(), i);
            sendOutbound(new UdpEvent(new FindNodeMessage(getHomeNode(), targetId),nodeHandler.getInetSocketAddress()));
        }
        msgSended.put(node.getHexId(), distanceEnd - distanceBegin);  // record the node and msg we have send the find node msg
        nodeHandler.setFindNeighbourMsgCount(distanceEnd - distanceBegin);
    }

    public void sendOutbound(UdpEvent udpEvent) {
        if (messageSender != null) {
            messageSender.accept(udpEvent);
        }
    }
}























































































































































