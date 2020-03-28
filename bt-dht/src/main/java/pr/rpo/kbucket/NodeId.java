package pr.rpo.kbucket;

import java.nio.charset.StandardCharsets;

public class NodeId extends NameSpace {

    public NodeId() {}

    public NodeId(String nodeId) {
        super(nodeId.getBytes(StandardCharsets.ISO_8859_1));
    }

    public NodeId(byte[] nodeId) {
        super(nodeId);
    }

    public NodeId(NodeId nodeId) {
        super();
        setRaw(nodeId.getRaw().clone());
    }
}
