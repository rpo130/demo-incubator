package pr.rpo.msg;

import pr.rpo.bencode.BencodeObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DhtMsg extends KrpcMsg {

    public DhtMsg() {}

    DhtMsg(char type, Map<String, String> arguments) {
        super(type,arguments);
    }

    DhtMsg(char type, QueryType queryType, Map<String, String> arguments) {
        super(type,queryType,arguments);
    }

    public static DhtMsg generateFrom(BencodeObject bso) {
        DhtMsg msg = new DhtMsg();
        msg.type = bso.getKv().get("y").getStr().charAt(0);
        msg.transactionId = bso.getKv().get("t").getStr();
        if(bso.getKv().get("v") != null) {
            msg.version = bso.getKv().get("v").getStr();
        }

        if(msg.type == 'q') {
            QueryType qt = QueryType.valueOf(bso.getKv().get("q").getStr());
            msg.queryType = qt;
            if(qt == QueryType.ping) {
                msg.arguments = bso.getKv().get("a").getKVInString();
            }else if(qt == QueryType.find_node) {
                //TODO
                msg.arguments = bso.getKv().get("a").getKVInString();
            }else if(qt == QueryType.get_peers) {
                //TODO
                msg.arguments = bso.getKv().get("a").getKVInString();
            }else if(qt == QueryType.announce_peer) {
                //TODO
                msg.arguments = bso.getKv().get("a").getKVInString();
            }else {

            }

        }else if(msg.type == 'r') {
            msg.responseMsgs = bso.getKv().get("r").getKVInString();
        }else if(msg.type == 'e') {
            msg.errorCode = ErrorCode.GenericError;//TODO
            msg.errorMsg = bso.getKv().get("e").getStr();
        }else {
            System.exit(0);
        }

        return msg;
    }

    public static DhtMsg pingMsg(byte[] nodeId) {
        DhtMsg msg = new DhtMsg('q',QueryType.ping,new HashMap<>() {{put("id",new String(nodeId, StandardCharsets.ISO_8859_1));}});
        msg.transactionId = KRpc.genTid.genTid();
        return msg;
    }

    public static DhtMsg responsePingMsg(byte[] localNodeId) {
        DhtMsg msg = new DhtMsg('r', new LinkedHashMap<>(1) {{put("id", new String(localNodeId, StandardCharsets.ISO_8859_1));}});
        msg.transactionId = KRpc.genTid.genTid();
        return msg;
    }

    public static DhtMsg findNodeMsg(byte[] nodeId, byte[] targetId) {
        DhtMsg msg = new DhtMsg('q', QueryType.find_node,new LinkedHashMap<>(1) {{put("id",new String(nodeId,StandardCharsets.ISO_8859_1));put("target",new String(targetId,StandardCharsets.ISO_8859_1));}});
        msg.transactionId = KRpc.genTid.genTid();
        return msg;
    }

    public DhtMsg responseFindNodeMsg() {
        //TODO
        return null;
    }

    public DhtMsg getPeersMsg(String nodeId, String infoHash) {
        DhtMsg msg = new DhtMsg('q', QueryType.find_node,new LinkedHashMap<>(1) {{put("id",nodeId);put("info_hash",infoHash);}});
        msg.transactionId = KRpc.genTid.genTid();
        return msg;
    }

    public DhtMsg responseGetPeersMsg() {
        //TODO
        return null;
    }

    public DhtMsg announcePeerMsg() {
        //TODO don't need now
        return null;
    }

}
