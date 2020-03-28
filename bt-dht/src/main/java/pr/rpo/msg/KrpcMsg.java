package pr.rpo.msg;

import pr.rpo.Id;
import pr.rpo.bencode.BencodeImpl;
import pr.rpo.kbucket.NodeInfo;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;





/**
 * keys:
 * t y v
 */
public class KrpcMsg {
    public enum QueryType {
        ping,
        find_node,
        get_peers,
        announce_peer
    }

    public enum ErrorCode {
        GenericError(201),
        ServerError(202),
        ProtocolError(203),
        MethodUnknown(204);

        public int code;

        ErrorCode(int code) {
            this.code = code;
        }
    }


    public String transactionId;
    public String version;

    public char type;

    public QueryType queryType;
    public Map<String, String> arguments;

    public Map<String, String> responseMsgs;

    public ErrorCode errorCode;
    public String errorMsg;

    public KrpcMsg() {}

    KrpcMsg(char type, QueryType queryType, Map<String, String> arguments) {
        this.type = type;
        this.queryType = queryType;
        this.arguments = arguments;
        this.version = Id.getVersion();

    }

    protected KrpcMsg(char type, Map<String,String> responseMsgs) {
        this.type = type;
        this.responseMsgs = responseMsgs;
        this.version = Id.getVersion();
    }

    private KrpcMsg(char type, ErrorCode errorCode, String errorMsg) {
        this.type = type;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.version = Id.getVersion();

    }

    public static KrpcMsg getQueryMsg(QueryType queryType, Map<String, String> arguments) {
        KrpcMsg msg = new KrpcMsg('q',queryType,arguments);
        msg.transactionId = KRpc.genTid.genTid();
        return msg;
    }

    public static KrpcMsg getResMsg(String transactionId, Map<String, String> responseMsgs) {
        KrpcMsg msg = new KrpcMsg('r',responseMsgs);
        msg.transactionId = transactionId;
        return msg;
    }
    public static KrpcMsg getErrMsg(ErrorCode errorCode, String errorMsg) {
        KrpcMsg msg = new KrpcMsg('e',errorCode,errorMsg);
        msg.transactionId = KRpc.genTid.genTid();
        return msg;
    }

    public String message() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'t'").append(":");
        sb.append("'" + transactionId + "'");

        sb.append(",");

        sb.append("'y'").append(":").append("'" + type + "'");
        sb.append(",");

        sb.append("'v'").append(":").append("'" + version + "'");
        sb.append(",");

        if(type == 'q') {
            sb.append("'q'").append(":").append("'"+ queryType.name()+"'");
            sb.append(",");
            sb.append("'a'").append(":").append("{" + arguments.entrySet().stream().map(e -> "'" + e.getKey() + "'" + ":" + "'" + e.getValue() + "'").collect(Collectors.joining(",")) + "}");

        }else if(type == 'r') {
            sb.append("'r'")
                    .append(":")
                    .append("{" +
                            responseMsgs.entrySet()
                                    .stream()
                                    .map(e -> {
                                        String k = e.getKey();
                                        String v = e.getValue();
                                        return "'" + k + "'" + ":" + "'" + v + "'";
                                    })
                                    .collect(Collectors.joining(",")) + "}");
        }else if(type == 'e') {
            sb.append("'e'").append(":").append("["+errorCode.code + ",'" + errorMsg +"']");
        }else {

        }
        sb.append("}");
        return sb.toString();
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'t'").append(":");
        sb.append("'" + transactionId + "'");

        sb.append(",");

        sb.append("'y'").append(":").append("'" + type + "'");
        sb.append(",");
        if(version != null) {
            sb.append("'v'").append(":").append("'" + BencodeImpl.translateVersion(version) + "'");
            sb.append(",");
        }

        if(type == 'q') {
            sb.append("'q'").append(":").append("'"+ queryType.name()+"'");
            sb.append(",");
            sb.append("'a'").append(":").append("{" + arguments.entrySet().stream().map(e -> "'" + e.getKey() + "'" + ":" + "'" + e.getValue() + "'").collect(Collectors.joining(",")) + "}");

        }else if(type == 'r') {
            sb.append("'r'")
                    .append(":")
                    .append("{" +
                            responseMsgs.entrySet()
                                    .stream()
                                    .map(e -> {
                                        String k = e.getKey();
                                        String v = e.getValue();

                                        if(k.equals("nodes")) {
                                            v = NodeInfo.decodeCompactNodeInfos(v.getBytes(StandardCharsets.ISO_8859_1)).stream().map(element -> Base64.getEncoder().encodeToString(element.getNodeId().getRaw())+"(" + element.getContactInfo().toString() + ")").collect(Collectors.joining(","));
                                        }else if(k.equals("id")) {

                                            v = Base64.getEncoder().encodeToString(v.getBytes(StandardCharsets.ISO_8859_1));
                                        }
                                        return "'" + k + "'" + ":" + "'" + v + "'";
                                    })
                                    .collect(Collectors.joining(",")) + "}");
        }else if(type == 'e') {
            sb.append("'e'").append(":").append("["+errorCode.code + ",'" + errorMsg +"']");
        }else {

        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "KrpcMsg{" +
                prettyPrint() +
                '}';
    }
}
