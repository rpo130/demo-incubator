package pr.rpo.kbucket;

import pr.rpo.Id;
import pr.rpo.util.Bit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class BucketImpl implements Bucket {

    private final NameSpace startIndex;
    private final NameSpace bitMask;
    private NodeInfo[] infos;
    private int num;

    private Bucket pre;
    private Bucket next;

    public LocalDateTime lastChanged;

    public BucketImpl() {
        this.startIndex = new NameSpace();
        this.bitMask = new NameSpace();
        this.infos = new NodeInfo[BucketListImpl.K];
        this.num = 0;
    }

    public BucketImpl(NameSpace startIndex, NameSpace bitMask) {
        this.startIndex = startIndex;
        this.bitMask = bitMask;
        this.infos = new NodeInfo[BucketListImpl.K];//extra 1 is for split
        this.num = 0;
    }

    @Override
    public void updateTime() {
        this.lastChanged = LocalDateTime.now();
    }

    @Override
    public boolean needRefresh() {
        if(LocalDateTime.now().isAfter(lastChanged.plusMinutes(15l))) return true;
        return false;
    }

    @Override
    public boolean hasBadNode() {
        for(int i=0; i<num; i++) {
            if(infos[i].getState() == NodeInfo.NODE_STATE.BAD) return true;
        }
        return false;
    }

    @Override
    public void replaceBadNode(NodeInfo receiveNode) {
        for(int i=0; i<num; i++) {
            if(infos[i].getState() == NodeInfo.NODE_STATE.BAD) {
                infos[i] = receiveNode;
                break;
            }
        }
    }

    @Override
    public boolean hasQuestionableNode() {
        for(int i=0; i<num; i++) {
            if(infos[i].getState() == NodeInfo.NODE_STATE.QUESTIONABLE) return true;
        }
        return false;
    }

    @Override
    public NodeInfo[] getQuestionableNodes() {
        List<NodeInfo> nodeInfos = new ArrayList<>();
        for(int i=0; i<num; i++) {
            if(infos[i].getState() == NodeInfo.NODE_STATE.QUESTIONABLE) nodeInfos.add(infos[i]);
        }
        return nodeInfos.toArray(NodeInfo[]::new);
    }

    public NameSpace startId() {
        return this.startIndex;
    }

    public NameSpace idMask() {
        return this.bitMask;
    }

    @Override
    public int size() {
        return this.num;
    }

    @Override
    public NodeInfo get(int i) {
        return infos[i];
    }

    @Override
    public Bucket pre() {
        return this.pre;
    }

    @Override
    public void pre(Bucket bucket) {
        this.pre = bucket;
    }
    @Override
    public Bucket next() {
        return this.next;
    }
    @Override
    public void next(Bucket bucket) {
        this.next = bucket;
    }
    @Override
    public boolean isFull() {
        if(num == BucketListImpl.K) {
            return true;
        }else {
            return false;
        }
    }
    @Override
    public NodeInfo[] getAll() {
        return Arrays.stream(infos).limit(num).toArray(NodeInfo[]::new);
    }

    @Override
    public NodeInfo get(NodeId nodeId) {
        for(int i=num-1; i>=0; i--) {
            if(Arrays.equals(infos[i].getNodeId().getRaw(), nodeId.getRaw())) {
                return infos[i];
            }
        }
        return null;
    }

    @Override
    public NodeInfo getDeepCopy(NodeId nodeId) {
        NodeInfo ni = get(nodeId);
        if(ni != null) {
            return new NodeInfo(ni);
        }else {
            return null;
        }
    }

    @Override
    public boolean smartInsert(NodeInfo nodeInfo) {

        if(isFull()) {
            return trySplitWith(nodeInfo);
        }else {
            return insert(nodeInfo);
        }
    }

    public boolean insert(NodeInfo nodeInfo) {
        if(isFull()) return false;

        for(int i=num; i>=0; i--) {
            if(i == 0) {
                infos[i] = nodeInfo;
                num = num + 1;
                break;
            }else {
                int r = Id.compare(nodeInfo.getNodeId().getRaw(),infos[i-1].getNodeId().getRaw());

                if(r > 0) {
                    infos[i] = nodeInfo;
                    num = num + 1;
                    break;
                }else {
                    infos[i] = infos[i-1];
                }
            }
        }

        updateTime();
        return true;
    }

    private boolean trySplitWith(NodeInfo nodeInfo) {

        byte[] bms = NameSpace.getSplitMask(
                Stream.concat(
                        Arrays.stream(this.getAll()),
                        Stream.of(nodeInfo)
                ).map(e -> e.getNodeId()).toArray(NameSpace[]::new)
        ).getRaw();

        if(Bit.getPrefixOneNumFromLow(bms) >= 157) {
            return false;
        }

        Bucket[] newBucketImpls;

        newBucketImpls = new BucketImpl[2];

        int prefixOneLen = Bit.getPrefixOneNumFromLow(bms);

        NameSpace firstStart = new NameSpace();
        byte[] firstAddr = Bit.and(bms,nodeInfo.getNodeId().getRaw());
        Bit.clearBitInFromLow(firstAddr, prefixOneLen);
        firstStart.setRaw(firstAddr);

        byte[] secondAddr = Bit.and(bms,nodeInfo.getNodeId().getRaw());
        Bit.setBitInFromLow(secondAddr, prefixOneLen);
        NameSpace secondStart = new NameSpace();
        secondStart.setRaw(secondAddr);

        newBucketImpls[0] = new BucketImpl(firstStart,new NameSpace(bms));

        newBucketImpls[1] = new BucketImpl(secondStart,new NameSpace(bms));

        int n = num;
        while(n-- > 0) {
            int r = NodeId.compare(secondStart,infos[n].getNodeId());

            if(r > 0) {
                newBucketImpls[0].insert(infos[n]);
            }else {
                newBucketImpls[1].insert(infos[n]);
            }
        }


        newBucketImpls[0].pre(this.pre);
        newBucketImpls[1].next(this.next);

        newBucketImpls[0].next(newBucketImpls[1]);
        newBucketImpls[1].pre(newBucketImpls[0]);

        this.pre.next(newBucketImpls[0]);
        if(this.next != null) {
            this.next.pre(newBucketImpls[1]);
        }

        int r = Id.compare(newBucketImpls[1].startId().getRaw(),nodeInfo.getNodeId().getRaw());

        if(r > 0) {
            newBucketImpls[0].insert(nodeInfo);
        }else {
            newBucketImpls[1].insert(nodeInfo);
        }

        return true;
    }

    public String prettyPrintString() {
        StringBuilder sb = new StringBuilder();
        //TODO
        return "";
    }
}
