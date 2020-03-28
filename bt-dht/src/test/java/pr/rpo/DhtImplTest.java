package pr.rpo;

import org.junit.jupiter.api.*;
import pr.rpo.bencode.BencodeObject;
import pr.rpo.bencode.BencodeImpl;
import pr.rpo.kbucket.ContactInfo;
import pr.rpo.kbucket.NodeId;
import pr.rpo.kbucket.NodeInfo;
import pr.rpo.msg.DhtMsg;
import pr.rpo.msg.KrpcMsg;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class DhtImplTest {

    private DhtImpl dhtImpl;
    private DatagramSocket ds;
    private int serverListenPort = 9311;

    @BeforeAll
    public static void setUp() throws Exception {

    }

    @BeforeEach
    public void init() throws UnknownHostException, SocketException {
        dhtImpl = DhtImpl.getNewOne(serverListenPort);
        dhtImpl.listen();

        int port = new Random().nextInt(100)+9000;
        ds = new DatagramSocket(port,InetAddress.getLocalHost());

    }

    @AfterEach
    public void destory() {
        ds.close();
        dhtImpl.destory();
    }

    @AfterAll
    public static void tearDown() throws Exception {
    }

    @Test
    public void testPingResponse() throws IOException {
        byte[] buf = new byte[1024];
        byte[] msg = BencodeImpl.getEncoderAndDecoder().encode(BencodeObject.newFromText(DhtMsg.pingMsg(Id.generate().getBytes(StandardCharsets.ISO_8859_1)).message())).array();
        DatagramPacket dp = new DatagramPacket(msg,0,msg.length);
        dp.setAddress(InetAddress.getLocalHost());
        dp.setPort(serverListenPort);

        ds.send(dp);
        DatagramPacket dp_recv = new DatagramPacket(buf,0,buf.length);
        ds.receive(dp_recv);
        BencodeObject bso = BencodeImpl.getEncoderAndDecoder().decode(ByteBuffer.wrap(dp_recv.getData(),0,dp_recv.getLength()));
        DhtMsg recvMsg = DhtMsg.generateFrom(bso);
        String rmsg = bso.decodeToStringWithTranslate();
        assertEquals('r',recvMsg.type);
        System.out.println(recvMsg.responseMsgs.get("id"));
        assertArrayEquals(dhtImpl.localNodeId.getRaw(), recvMsg.responseMsgs.get("id").getBytes(StandardCharsets.ISO_8859_1));
    }

//    @Test
//    public void testPingSendAndResponse() throws IOException, InterruptedException {
//        NodeInfo nodeInfo = new NodeInfo();
//        ContactInfo contactInfo = new ContactInfo(InetAddress.getLocalHost(),9211);
//
//        nodeInfo.setContactInfo(contactInfo);
//        nodeInfo.setNodeId(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)));
//
//        DhtImpl.getNewOne(9211).listen();
//        Thread.sleep(500l);
//        CompletableFuture cf = DhtImpl.getNewOne(9212).pingSend(nodeInfo.getContactInfo());
//        Thread.sleep(500l);
//        assertEquals(true,cf.isDone());
//    }
//
//    @Test
//    public void testFindNodeSend() throws InterruptedException, UnknownHostException {
//        NodeInfo nodeInfo = new NodeInfo();
//        ContactInfo contactInfo = new ContactInfo(InetAddress.getLocalHost(),9211);
//        nodeInfo.setContactInfo(contactInfo);
//        nodeInfo.setNodeId(new NodeId(Id.generate().getBytes(StandardCharsets.ISO_8859_1)));
//
//        DhtImpl.getNewOne(9211).listen();
//        Thread.sleep(500l);
//        CompletableFuture cf = dhtImpl.findNodeSend(nodeInfo.getContactInfo(), dhtImpl.localNodeId.toString());
//        Thread.sleep(500l);
//
//        assertEquals(true,cf.isDone());
//    }


    @Test
    public void testErrorReceive() throws IOException {
        byte[] buf = new byte[1024];
        System.out.println(DhtMsg.getErrMsg(KrpcMsg.ErrorCode.GenericError, "error unit test").message());
        byte[] msg = BencodeImpl.getEncoderAndDecoder().encode(BencodeObject.newFromText(DhtMsg.getErrMsg(KrpcMsg.ErrorCode.GenericError, "error unit test").message())).array();
        DatagramPacket dp = new DatagramPacket(msg,0,msg.length);
        dp.setAddress(InetAddress.getLocalHost());
        dp.setPort(serverListenPort);


        ds.send(dp);
    }

    @Test
    public void testPingResponseReceive() throws IOException {
        byte[] buf = new byte[1024];
        byte[] msg = BencodeImpl.getEncoderAndDecoder().encode(BencodeObject.newFromText(DhtMsg.responsePingMsg(Id.generate().getBytes(StandardCharsets.ISO_8859_1)).message())).array();
        DatagramPacket dp = new DatagramPacket(msg,0,msg.length);
        dp.setAddress(InetAddress.getLocalHost());
        dp.setPort(serverListenPort);
        ds.send(dp);
    }

}