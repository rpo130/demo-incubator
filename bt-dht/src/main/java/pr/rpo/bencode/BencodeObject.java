package pr.rpo.bencode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BencodeObject {
    public enum BStringType { string, integer, list, dictionary }

    private BStringType type;
    private Integer num;
    private String str;
    private List<BencodeObject> elements;
    private Map<String, BencodeObject> kv;

    public BencodeObject() {}

    public static BencodeObject newFromText(String text) {
        return BencodeImpl.getEncoderAndDecoder().getSymbols(text);
    }

    public Map<String, String> getKVInString() {
        return kv.entrySet().stream().filter(e -> e.getValue().str != null).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().str));
    }


    public String decodeToString() {
        StringBuilder sb = new StringBuilder();
        switch(type) {
            case string:sb.append("'" + str + "'");break;
            case integer:sb.append(num);break;
            case list:
                sb.append("[");
                sb.append(elements.stream().map(e -> e.decodeToString()).collect(Collectors.joining(",")));
                sb.append("]");
                break;
            case dictionary:
                sb.append("{");
                sb.append(kv.entrySet().stream().map(e -> "'" + e.getKey() + "'" + ":" + e.getValue().decodeToString()).collect(Collectors.joining(",")));
                sb.append("}");
                break;
            default:
                return null;
        }

        return sb.toString();
    }

    public String decodeToStringWithTranslate() {
        StringBuilder sb = new StringBuilder();
        switch(type) {
            case string:sb.append("'" + str + "'");break;
            case integer:sb.append(num);break;
            case list:
                sb.append("[");
                sb.append(elements.stream().map(e -> e.decodeToString()).collect(Collectors.joining(",")));
                sb.append("]");
                break;
            case dictionary:
                sb.append("{");
                sb.append(kv.entrySet().stream().map(e -> {
                    String k = e.getKey();
                    String v;
                    if(k.equals("ip")) {
                        v = "'" + BencodeImpl.translatePeerContackInfo(e.getValue().str) + "'";
                    }else if(k.equals("v")){
                        v = "'" + BencodeImpl.translateVersion(e.getValue().str) + "'";
                    }else if(k.equals("id")) {
                        v = "'" + BencodeImpl.translateId(e.getValue().str) + "'";
                    }else if(k.equals("nodes")) {
                        int num = e.getValue().str.getBytes(StandardCharsets.ISO_8859_1).length/26;
                        StringBuilder sbb = new StringBuilder();
                        sbb.append("'");
                        for(int i =0; i < num;i++) {
                            sbb.append(BencodeImpl.translateNodeContactInfo(Arrays.copyOfRange(e.getValue().str.getBytes(StandardCharsets.ISO_8859_1),i * 26,i * 26 + 26)));
                            sbb.append("::");
                        }
                        sbb.deleteCharAt(sbb.length()-1);
                        sbb.append("'");
                        v = sbb.toString();

                    }else {
                        v = e.getValue().decodeToStringWithTranslate();
                    }

                    return "'" + k + "'" + ":" + v;
                }).collect(Collectors.joining(",")));
                sb.append("}");
                break;
            default:
                return null;
        }

        return sb.toString();
    }

    public BStringType getType() {
        return type;
    }

    public void setType(BStringType type) {
        this.type = type;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public List<BencodeObject> getElements() {
        return elements;
    }

    public void setElements(List<BencodeObject> elements) {
        this.elements = elements;
    }

    public Map<String, BencodeObject> getKv() {
        return kv;
    }

    public void setKv(Map<String, BencodeObject> kv) {
        this.kv = kv;
    }
}
