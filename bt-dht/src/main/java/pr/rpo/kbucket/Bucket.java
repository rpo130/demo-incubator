package pr.rpo.kbucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pr.rpo.util.Bit;


//TODO need to be thread safe
public interface Bucket {
    Logger logger = LoggerFactory.getLogger(Bucket.class);

    /**
     *
     * @return kv pair num
     */
    int size();

    /**
     *
     * @param i
     * @return nodeId
     */
    @Deprecated
    NodeInfo get(int i);

    NameSpace idMask();

    NameSpace startId();

    Bucket pre();

    void pre(Bucket bucket);

    Bucket next();

    void next(Bucket bucket);

    boolean isFull();

    @Deprecated
    NodeInfo[] getAll();

    @Deprecated
    NodeInfo get(NodeId nodeId);

    NodeInfo getDeepCopy(NodeId nodeId);

    /**
     * auto split
     * @param nodeInfo
     */
    boolean smartInsert(NodeInfo nodeInfo);

    /**
     * pure insert with check
     * @param nodeInfo
     * @return
     */
    boolean insert(NodeInfo nodeInfo);

    /**
     * when ping and response
     * when nodeId new add or replace
     */
    void updateTime();

    /**
     * last updatetime is 15 minutes before
     * @return
     */
    boolean needRefresh();

    boolean hasBadNode();

    void replaceBadNode(NodeInfo receiveNode);

    boolean hasQuestionableNode();

    NodeInfo[] getQuestionableNodes();

    @Deprecated
    static boolean isMatch(Bucket bucketImpl, NodeId nodeId) {
        byte[] a = new byte[20];
        byte[] b = new byte[20];
        for(int i=0; i<20; i++) {
            a[i] = (byte) (bucketImpl.idMask().getRaw()[i] & bucketImpl.startId().getRaw()[i]);
            b[i] = (byte) (bucketImpl.idMask().getRaw()[i] & nodeId.getRaw()[i]);
            if(a[i] != b[i]) return false;
        }
        return true;
    }

    static boolean isBelong(Bucket bucket, NodeId nodeId) {
        byte[] bucketNameSpaceLen = Bit.inversion(bucket.idMask().getRaw());
        byte[] bucketNameSpaceEnd = Bit.or(bucket.startId().getRaw(),bucketNameSpaceLen);

        int s = Bit.compareBigEndian(bucket.startId().getRaw(), nodeId.getRaw());
        int e = Bit.compareBigEndian(nodeId.getRaw(), bucketNameSpaceEnd);

        if(s <= 0 && e <= 0) return true;
        else return false;
    }

    static int compare(Bucket bucket, NodeId nodeId) {
        return Bit.compareBigEndian(bucket.startId().getRaw(),nodeId.getRaw());
    }

    static boolean insertBucket(Bucket bucketImpl, NodeInfo nodeInfo) {
        if(bucketImpl == null) {
            logger.error("error must fix");
            return false;
        }else {
            if(Bucket.isBelong(bucketImpl, nodeInfo.getNodeId()) == true) {
                return bucketImpl.smartInsert(nodeInfo);

            }else {
                if(Bucket.compare(bucketImpl, nodeInfo.getNodeId()) > 0) {
                    newPreInsert(bucketImpl,nodeInfo);
                    return true;
                }else {
                    if(bucketImpl.next() == null) {
                        newNexInsert(bucketImpl,nodeInfo);
                        return true;
                    }else {
                        return insertBucket(bucketImpl.next(),nodeInfo);
                    }
                }
            }
        }
    }

    static void newNexInsert(Bucket bucketImpl, NodeInfo nodeInfo) {
        BucketImpl newB = new BucketImpl(
                new NameSpace(Bit.and(nodeInfo.getNodeId().getRaw(),bucketImpl.idMask().getRaw())),
                new NameSpace(bucketImpl.idMask()));

        newB.insert(nodeInfo);
        newB.pre(bucketImpl);
        bucketImpl.next(newB);
    }

    static void newPreInsert(Bucket bucketImpl, NodeInfo nodeInfo) {
        BucketImpl newB = new BucketImpl(
                new NameSpace(Bit.and(nodeInfo.getNodeId().getRaw(),bucketImpl.idMask().getRaw())),
                new NameSpace(bucketImpl.idMask()));

        newB.insert(nodeInfo);
        newB.next(bucketImpl);
        newB.pre(bucketImpl.pre());
        bucketImpl.pre(newB);
        newB.pre().next(newB);
    }


}
