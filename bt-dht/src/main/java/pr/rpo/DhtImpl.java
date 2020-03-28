package pr.rpo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pr.rpo.bencode.BencodeImpl;
import pr.rpo.bencode.BencodeObject;
import pr.rpo.kbucket.*;
import pr.rpo.msg.DhtMsg;
import pr.rpo.msg.KrpcMsg;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static pr.rpo.msg.KrpcMsg.QueryType.*;

public class DhtImpl {
    private final static Logger logger = LoggerFactory.getLogger(DhtImpl.class);

    private static DhtImpl dhtImpl = new DhtImpl(9300);
    public NodeId localNodeId;
    private RoutingTable rt = BucketListImpl.getNewOne();
    private DatagramSocket ds;
    public List<CompletableFuture<?>> cfList;

    public static DhtImpl getSingleton() {
        return dhtImpl;
    }

    public static DhtImpl getNewOne(int port) {
        return new DhtImpl(port);
    }

    private DhtImpl(int port) {
        init(port);
    }

    private void init(int port) {
        try {
            msgBox = new HashMap<>();
            localNodeId = new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1));
            ds = new DatagramSocket(port);
            cfList = new ArrayList<>();
        } catch (SocketException e) {
            logger.error("{}",e);
        }
    }

    private Map<String, Map<String,DhtMsg>> msgBox;

    private void addMsgBox(String tid, ContactInfo cinfo, DhtMsg msg) {
        synchronized (msgBox) {
            Map<String,DhtMsg> listMap = msgBox.get(tid);
            if(listMap == null) {
                msgBox.put(tid,
                        new LinkedHashMap<>(){{
                            put(cinfo.getAddr().getHostAddress()+":"+cinfo.getPort(),msg);}});
            }else {
                long existSize = listMap.entrySet().stream().filter(e -> e.getValue().equals(cinfo)).count();
                if(existSize >= 1) {
                    logger.error("msgBag got duplicat reply, may be wait time too long?");
                }else {
                    listMap.put(cinfo.getAddr().getHostAddress()+":"+cinfo.getPort(),msg);
                }
            }
            msgBox.notifyAll();
        }
    }

    private void getMsgMap() {}

    public void sendResponse(NodeInfo nodeInfo, KrpcMsg msg) {
        logger.debug("send Query: {},{}", nodeInfo.getContactInfo(),msg);

        try {
            ByteBuffer encodeBuff = BencodeImpl.getEncoderAndDecoder().encode(BencodeObject.newFromText(msg.message()));
            DatagramPacket dp = new DatagramPacket(encodeBuff.array(), encodeBuff.limit()-encodeBuff.position(), nodeInfo.getContactInfo().getAddr(), nodeInfo.getContactInfo().getPort());

            ds.send(dp);
        } catch (IOException e) {
            logger.error("{}",e);
        }
    }

    public CompletableFuture sendQuery(ContactInfo contactInfo, KrpcMsg msg, QueryCallBack f) {
        logger.debug("send Query: {},{}", contactInfo,msg);

        try {
            ByteBuffer encodeBuff = BencodeImpl.getEncoderAndDecoder().encode(BencodeObject.newFromText(msg.message()));
            DatagramPacket dp = new DatagramPacket(encodeBuff.array(), encodeBuff.limit()-encodeBuff.position(), contactInfo.getAddr(), contactInfo.getPort());

            ds.send(dp);
            final String tid = msg.transactionId;
            CompletableFuture<Integer> cf = CompletableFuture.<Integer>supplyAsync(() -> {
                synchronized (msgBox) {
                    while(! (msgBox.containsKey(tid)&& msgBox.get(tid).containsKey(contactInfo.getAddr().getHostAddress()+":"+contactInfo.getPort()))) {
                        try {
                            msgBox.wait();
                        } catch (InterruptedException e) {
                            logger.error("{}",e);
                        }
                    }
                }
                return 0;
            });
            cf.completeOnTimeout(1,100l, TimeUnit.SECONDS);
            cf.thenAccept(e -> {
                if(e == 1) {
                    logger.debug("{},{} timeout",tid, contactInfo);
                }else {
                    synchronized (msgBox) {
                        DhtMsg receiveMsg = msgBox.get(tid).get(contactInfo.getAddr().getHostAddress() + ":" + contactInfo.getPort());
                        f.apply(f, receiveMsg);
                        msgBox.get(tid).remove(contactInfo.getAddr().getHostAddress() + ":" + contactInfo.getPort());
                    }
                }
                synchronized (cfList) {
                    cfList.remove(cf);
                }
            });


            synchronized (cfList) {
                cfList.add(cf);
            }

            return cf;

        } catch (IOException e) {
            logger.error("{}",e);
        }
        //TODO
        return null;
    }

    public void listen() {
        new Thread(() -> {
            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            try {
                while (true) {
                    ds.receive(dp);
                    NodeInfo receiveNode = new NodeInfo();
                    ContactInfo receiveContact;
                    receiveContact = new ContactInfo(dp.getAddress(), dp.getPort());
                    receiveNode.setContactInfo(receiveContact);

                    BencodeObject bso = BencodeImpl.getEncoderAndDecoder().decode(ByteBuffer.wrap(dp.getData(), 0, dp.getLength()));
                    bso.decodeToStringWithTranslate();
                    DhtMsg recvMsg = DhtMsg.generateFrom(bso);

                    if(bso.getType() != BencodeObject.BStringType.dictionary) continue;
                    if(recvMsg.type == 'q') {
                        receiveNode.setNodeId(new NodeId(recvMsg.arguments.get("id")));
                        logger.info("remote node : {}, msg receive : {}", receiveNode, recvMsg.prettyPrint());
                        processQuery(receiveNode, bso, recvMsg);
                    }else if(recvMsg.type=='e' || recvMsg.type=='r'){
                        if(recvMsg.type == 'r') {
                            receiveNode.setNodeId(new NodeId(recvMsg.responseMsgs.get("id")));
                            logger.info("remote node : {}, msg receive : {}", receiveNode, recvMsg.prettyPrint());
                        }else {
                            logger.info("remote node : {}, msg receive : {}", receiveNode, recvMsg.prettyPrint());
                        }
                        addMsgBox(recvMsg.transactionId,receiveContact,recvMsg);
                    }else {
                        logger.info("unknown msg type");
                    }


                }
            }catch (SocketException e) {
                logger.error("{}",e);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void destory() {
        this.ds.close();
    }

    @FunctionalInterface
    interface QueryCallBack {
        int apply(QueryCallBack self,DhtMsg msg);
    }

    public CompletableFuture findNodeSend(ContactInfo contactInfo, byte[] targetId) {
        QueryCallBack f = (QueryCallBack self,DhtMsg recvMsg) -> {
            if(recvMsg.type == 'e') {
                logger.info("error {},{},{}",contactInfo, recvMsg.errorCode, recvMsg.errorMsg);
                return 0;
            }

            byte[] compactNodeInfos = recvMsg.responseMsgs.get("nodes").getBytes(StandardCharsets.ISO_8859_1);
            List<NodeInfo> findNodes = NodeInfo.decodeCompactNodeInfos(compactNodeInfos);
            findNodes.forEach(e -> {
//                NodeInfo ni = rt.getRef(e.getNodeId());
//                if(ni == null) {
//                    e.updateTime();
//                    e.setState(NodeInfo.NODE_STATE.QUESTIONABLE);
//                    rt.tryPutOrUpdate(e);
//                }else {
//                    ni.updateTime();
//                    ni.setState(NodeInfo.NODE_STATE.GOOD);
//                    rt.tryPutOrUpdate(ni);
//                }
                pingSend(e.getContactInfo());
            });

            NodeInfo nodeInfo = rt.getDeepCoy(new NodeId(recvMsg.responseMsgs.get("id")));
            nodeInfo.updateTime();
            nodeInfo.setState(NodeInfo.NODE_STATE.GOOD);
            rt.tryPutOrUpdate(nodeInfo);

            return 0;
        };

        return sendQuery(contactInfo, DhtMsg.findNodeMsg(localNodeId.getRaw(),targetId), f);
    }

    public CompletableFuture pingSend(ContactInfo contactInfo) {
        return sendQuery(contactInfo, DhtMsg.pingMsg(localNodeId.getRaw()), (self,e) -> {
            if(e.type == 'e') {
                logger.info("error {},{},{}",contactInfo, e.errorCode, e.errorMsg);
                return 0;
            }


            NodeInfo nodeInfo = rt.getDeepCoy(new NodeId(e.responseMsgs.get("id")));
            if(nodeInfo == null) {
                NodeInfo ni = new NodeInfo(new NodeId(e.responseMsgs.get("id")), new ContactInfo(contactInfo));
                ni.setState(NodeInfo.NODE_STATE.GOOD);
                ni.updateTime();
                rt.tryPutOrUpdate(ni);
            }else {
                nodeInfo.setState(NodeInfo.NODE_STATE.GOOD);
                nodeInfo.updateTime();
                if(!nodeInfo.getContactInfo().equals(contactInfo)) {
                    logger.error("same node id got different ip");
                }else {
                    rt.tryPutOrUpdate(nodeInfo);
                }
            }
            return 0;
        });
    }

    public void pingSend(NodeInfo[] nodeInfos) {
        for(NodeInfo n : nodeInfos) {
            pingSend(n.getContactInfo());
        }
    }

    public void response(String transId, NodeInfo nodeInfo, Map<String, String> msg) {
        sendResponse(nodeInfo,KrpcMsg.getResMsg(transId, msg));
    }

    public void errorResponse(NodeInfo nodeInfo, KrpcMsg.ErrorCode errorCode, String errmsg) {
        sendResponse(nodeInfo, KrpcMsg.getErrMsg(errorCode,errmsg));
    }

    public void bootstrap() {
        List<ContactInfo> bootstrapNodes = new ArrayList<>();
        try(
                BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("bootstrapNodes.txt")));
                ) {
            String s;
            while((s = br.readLine()) != null) {
                if(s.charAt(0) == '#') {
                    continue;
                }
                String[] ipAndPort = s.split(":");

                ContactInfo ia = new ContactInfo(InetAddress.getByName(ipAndPort[0]),Integer.parseInt(ipAndPort[1]));
                bootstrapNodes.add(ia);
            }
        } catch (FileNotFoundException e) {
            logger.error("{}",e);
        } catch (IOException e) {
            logger.error("{}",e);
        }
        logger.info("bootstrap node : {}", bootstrapNodes);

        for(ContactInfo ci : bootstrapNodes) {
            pingSend(ci);
        }

        try {
            logger.info("bootstrap waiting");
            Thread.sleep(20000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(rt.size() < 1) {
            logger.error("boot fail");
            System.out.println("boot fail");
            System.exit(-1);
        }
        logger.info("bootstrap finish");
    }

    public RoutingTable getRoutingTable() {
        return this.rt;
    }

    //****************************
    //query process

    private void processQuery(NodeInfo receiveNode, BencodeObject bso, DhtMsg recvMsg) {
        receiveNode.getNodeId().setRaw(recvMsg.arguments.get("id").getBytes(StandardCharsets.ISO_8859_1));

        if(recvMsg.queryType == ping) {
            processPing(receiveNode, recvMsg);
        }else if(recvMsg.queryType == find_node) {
            processFindNode(receiveNode, recvMsg);
        }else if(recvMsg.queryType == get_peers) {
            processGetPeers(receiveNode, recvMsg);
        }else if(recvMsg.queryType == announce_peer) {
            processAnnouncePeer(receiveNode, recvMsg);
        }
        else {
            logger.info("unknown query type");
        }
    }

    private void processAnnouncePeer(NodeInfo receiveNode, DhtMsg recvMsg) {
        //TODO token verify
        logger.info("peer {} (which ip is {}) is announce infohash {} in port {} with token {}",
                receiveNode.getNodeId().toString(),
                receiveNode.getContactInfo().getAddr(),
                recvMsg.arguments.get("info_hash"),
                recvMsg.arguments.get("port"),
                recvMsg.arguments.get("token"));

        response(recvMsg.transactionId, receiveNode, new HashMap<>() {{put("id", localNodeId.toString());}});
    }

    private void processGetPeers(NodeInfo receiveNode, DhtMsg recvMsg) {
        NodeId info_hash = new NodeId(recvMsg.arguments.get("info_hash").getBytes(StandardCharsets.ISO_8859_1));
        byte[] token = Id.generateToken();

        receiveNode.setNodeId(info_hash);
        receiveNode.updateTime();
        receiveNode.setState(NodeInfo.NODE_STATE.GOOD);
        receiveNode.setToken(token);


        if(rt.getRef(receiveNode.getNodeId()) == null) {
            if(rt.tryPutOrUpdate(receiveNode)) {
                final NodeInfo[] queryResultNodes = rt.findNode(info_hash);
                response(recvMsg.transactionId,receiveNode,
                        new LinkedHashMap<>() {{
                            put("id", localNodeId.toString());
                            put("token", new String(token, StandardCharsets.ISO_8859_1));
                            put("nodes", Arrays.stream(queryResultNodes).map(e -> e.compactNodeInfo()).collect(Collectors.joining())); }});
            }else {
                Bucket b = rt.findBelongBucket(receiveNode.getNodeId());
                if(b.hasBadNode()) {
                    b.replaceBadNode(receiveNode);
                }else {
                    //TODO ques node
                    pingSend(b.getQuestionableNodes());
                    errorResponse(receiveNode, KrpcMsg.ErrorCode.GenericError, "space full");
                }
            }
        }else {
            rt.tryPutOrUpdate(receiveNode);
            final NodeInfo[] queryResultNodes = rt.findNode(info_hash);
            response(recvMsg.transactionId,receiveNode,
                    new LinkedHashMap<>() {{
                        put("id", localNodeId.toString());
                        put("token", new String(receiveNode.getToken(), StandardCharsets.ISO_8859_1));
                        put("nodes", Arrays.stream(queryResultNodes).map(e -> e.compactNodeInfo()).collect(Collectors.joining())); }});
        }

    }

    private void processFindNode(NodeInfo receiveNode, DhtMsg recvMsg) {
        NodeId clientNodeId = new NodeId(recvMsg.arguments.get("id").getBytes(StandardCharsets.ISO_8859_1));
        NodeId targetNodeId = new NodeId(recvMsg.arguments.get("target").getBytes(StandardCharsets.ISO_8859_1));
        if(rt.getRef(clientNodeId) != null) {
            NodeInfo clientNodeInfo = rt.getDeepCoy(clientNodeId);
            clientNodeInfo.updateTime();
            clientNodeInfo.setState(NodeInfo.NODE_STATE.GOOD);
            rt.tryPutOrUpdate(clientNodeInfo);

            NodeInfo[] queryResultNodes = rt.findNode(targetNodeId);

            response(recvMsg.transactionId, clientNodeInfo,
                    new LinkedHashMap<>() {{
                        put("id", localNodeId.toString());
                        put("nodes", Arrays.stream(queryResultNodes).map(e -> e.compactNodeInfo()).collect(Collectors.joining())); }});
        }else {
            receiveNode.setNodeId(clientNodeId);
            receiveNode.updateTime();
            receiveNode.setState(NodeInfo.NODE_STATE.GOOD);
            rt.tryPutOrUpdate(receiveNode);

            NodeInfo[] queryResultNodes = rt.findNode(targetNodeId);

            response(recvMsg.transactionId, receiveNode,
                    new LinkedHashMap<>() {{
                        put("id", localNodeId.toString());
                        put("nodes", Arrays.stream(queryResultNodes).map(e -> e.compactNodeInfo()).collect(Collectors.joining())); }});
        }
    }

    private void processPing(NodeInfo receiveNode, DhtMsg recvMsg) {
        sendResponse(receiveNode,KrpcMsg.getResMsg(recvMsg.transactionId,new HashMap<>() {{put("id", new String(localNodeId.getRaw(), StandardCharsets.ISO_8859_1));}}));
    }

}
