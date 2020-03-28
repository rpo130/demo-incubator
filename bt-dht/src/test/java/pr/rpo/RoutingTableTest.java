package pr.rpo;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr.rpo.kbucket.BucketListImpl;
import pr.rpo.kbucket.NodeId;
import pr.rpo.kbucket.NodeInfo;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class RoutingTableTest {
    BucketListImpl bl;

    @BeforeEach
    public void setUp() {
        bl = new BucketListImpl();
    }

    @AfterEach
    public void destory() {

    }

    @Test
    public void smokeTest() {
        NodeId nodeId = new NodeId(Id.generate().getBytes(StandardCharsets.US_ASCII));
        NodeInfo ni = new NodeInfo(nodeId,null);
        bl.tryPutOrUpdate(ni);
        assertEquals(nodeId, bl.getRef(nodeId).getNodeId());
        assertEquals(ni, bl.getRef(nodeId));
    }

    @Test
    public void testData() {
        NodeInfo[] nodeInfos = {
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
            new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null)
        };

        for(int i=0; i<nodeInfos.length; i++) {
            bl.tryPutOrUpdate(nodeInfos[i]);
        }

        for(int i=0; i<nodeInfos.length; i++) {
            assertArrayEquals(nodeInfos[i].getNodeId().getRaw(), bl.getRef(nodeInfos[i].getNodeId()).getNodeId().getRaw());
        }
    }
//
//    @Test
//    public void testData1() {
//        NodeInfo[] nodeInfos = {
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null)
//        };
//
//        for(int i=0; i<nodeInfos.length; i++) {
//            bl.tryPutOrUpdate(nodeInfos[i]);
//        }
//
//        for(int i=0; i<nodeInfos.length; i++) {
//            assertEquals(nodeInfos[i], bl.getRef(nodeInfos[i].getNodeId()));
//        }
//    }
//
//    @Test
//    public void testFindNode() {
//        NodeInfo[] nodeInfos = {
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null),
//                new NodeInfo(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)),null)
//        };
//        for(int i=0; i<nodeInfos.length; i++) {
//            bl.tryPutOrUpdate(nodeInfos[i]);
//        }
//
//        assertEquals(8, bl.findNode(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1))).length);
//    }
//

}