package pr.rpo.kbucket;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;



public class NodeInfo {

    public enum NODE_STATE {
        GOOD, //response query within 15min {send to us within 15min}
        BAD,
        QUESTIONABLE //fail multi times
    }

    private NodeId nodeId;
    private ContactInfo contactInfo;
    private NODE_STATE state;
    private byte[] token;
    private LocalDateTime lastContactTime;

    public NodeInfo(NodeInfo nodeInfo) {
        this.nodeId = new NodeId(nodeInfo.getNodeId());
        this.contactInfo = new ContactInfo(nodeInfo.getContactInfo());
        this.state = nodeInfo.state;
        this.token = nodeInfo.token.clone();
        this.lastContactTime = LocalDateTime.from(nodeInfo.lastContactTime);
    }

    public NodeInfo() {
        this.nodeId = new NodeId();
    }

    public NodeInfo(NodeId nodeId, ContactInfo contactInfo) {
        this.nodeId = nodeId;
        this.contactInfo = contactInfo;

        this.state = NODE_STATE.QUESTIONABLE;
        this.lastContactTime = LocalDateTime.now();
    }

    public NodeInfo(NodeId nodeId, ContactInfo contactInfo, NODE_STATE NODESTATE) {
        this.nodeId = nodeId;
        this.contactInfo = contactInfo;

        this.state = NODESTATE;
        this.lastContactTime = LocalDateTime.now();
    }

    public void updateTime() {
        this.lastContactTime = LocalDateTime.now();
    }



    public String compactNodeInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(new String(nodeId.getRaw(), StandardCharsets.ISO_8859_1));

        sb.append(contactInfo.compactInfo());
        return sb.toString();
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public NODE_STATE getState() {
        return state;
    }

    public void setState(NODE_STATE state) {
        this.state = state;
    }

    public LocalDateTime getLastContactTime() {
        return lastContactTime;
    }

    public void setLastContactTime(LocalDateTime lastContactTime) {
        this.lastContactTime = lastContactTime;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public static NodeInfo decodeCompactNodeInfo(byte[] bytes, int offset) {
        int LEN = 26;
        NodeInfo nodeInfo = new NodeInfo();
        NodeId nodeId = new NodeId();
        nodeId.setRaw(Arrays.copyOfRange(bytes,offset,offset+20));
        nodeInfo.setNodeId(nodeId);
        nodeInfo.setContactInfo(ContactInfo.decodeCompactPeerInfo(bytes,offset+20));

        return nodeInfo;
    }

    public static List<NodeInfo> decodeCompactNodeInfos(byte[] bytes) {
        int LEN = bytes.length;

        List<NodeInfo> nodeInfos = new ArrayList<>();
        for(int i=0; i<LEN; i=i+26) {
            nodeInfos.add(decodeCompactNodeInfo(bytes,i));
        }
        return nodeInfos;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeId=" + nodeId.base64String() +
                ", contactInfo=" + contactInfo +
                ", state=" + state +
                (token!=null ? ", token=" + Base64.getEncoder().encodeToString(token)  :  "")
                + ", lastContactTime=" + lastContactTime +
                '}';
    }
}
