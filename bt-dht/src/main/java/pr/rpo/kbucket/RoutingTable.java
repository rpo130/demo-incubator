package pr.rpo.kbucket;

public interface RoutingTable extends Iterable {

    NodeInfo getRef(NodeId nodeId);

    NodeInfo getDeepCoy(NodeId nodeId);

    /**
     * including this
     * @param nodeInfo
     */
//    void updateTimeAndState(NodeInfo nodeInfo);

    /**
     * //TODO
     * make sure this is thread safe which is mean this method need to be the only entry to modify data
     * @param nodeInfo
     * @return
     */
    boolean tryPutOrUpdate(NodeInfo nodeInfo);

    NodeInfo[] findNode(NodeId nodeId);

    NodeInfo[] findNode(NodeId nodeId, int k);

    NodeInfo[] findNodeDeepCopy(NodeId nodeId);

    Bucket findBelongBucket(NodeId nodeId);

    int size();

    String prettyPrintString();
}
