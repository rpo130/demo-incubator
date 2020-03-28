package pr.rpo.bencode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * rule
 * 1. 'string' -> [length]:string
 * 2. int -> i{num}e ,'-0' '0xx' is not allow
 * 3. list -> l{elements}e only String
 * 4. dict -> d{elements}e
 */
public class BencodeImpl implements Bencode {
    private final static Logger logger = LoggerFactory.getLogger(BencodeImpl.class);


    public static BencodeImpl getEncoderAndDecoder() {
        return new BencodeImpl();
    }

    @Override
    public ByteBuffer encode(BencodeObject obj) {
        switch(obj.getType()) {
            case string:
                return encodeStringObj(obj);
            case integer:
                return encodeIntegerObj(obj);
            case list:
                return encodeListObj(obj);
            case dictionary:
                return encodeDictionaryObj(obj);
            default:
                logger.error("encode wrong");
                System.exit(-1);
                return null;
        }
    }

    private ByteBuffer encodeDictionaryObj(BencodeObject obj) {
        try {
            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                baos.writeBytes("d".getBytes(StandardCharsets.ISO_8859_1));
                for(Map.Entry<String, BencodeObject> kvObj : obj.getKv().entrySet()) {
                    baos.writeBytes(String.join("",Integer.toString(kvObj.getKey().length()),":",kvObj.getKey()).getBytes(StandardCharsets.ISO_8859_1));
                    baos.writeBytes(encode(kvObj.getValue()).array());
                }
                baos.writeBytes("e".getBytes(StandardCharsets.ISO_8859_1));

                return ByteBuffer.wrap(baos.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    private ByteBuffer encodeListObj(BencodeObject obj) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();){
            baos.writeBytes("l".getBytes(StandardCharsets.ISO_8859_1));
            for(BencodeObject listObj : obj.getElements()) {
                baos.writeBytes(encode(listObj).array());
            }
            baos.writeBytes("e".getBytes(StandardCharsets.ISO_8859_1));

            return ByteBuffer.wrap(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    private ByteBuffer encodeIntegerObj(BencodeObject obj) {
        return ByteBuffer.wrap(String.join("","i",Integer.toString(obj.getNum()),"e").getBytes(StandardCharsets.ISO_8859_1));
    }

    private ByteBuffer encodeStringObj(BencodeObject obj) {
        return ByteBuffer.wrap(String.join("",Integer.toString(obj.getStr().length()),":",obj.getStr()).getBytes(StandardCharsets.ISO_8859_1));
    }

    @Override
    public BencodeObject decode(ByteBuffer buffer) {
        return decodeBSO(buffer, 0);
    }


    private BencodeObject.BStringType getType(char c) {
        if(c == '\'') {
            return BencodeObject.BStringType.string;
        }else if(c == '{') {
            return BencodeObject.BStringType.dictionary;
        }else if(c == '[') {
            return BencodeObject.BStringType.list;
        }else if((c >= '0' && c <= '9') || c == '-' ) {
            return BencodeObject.BStringType.integer;
        }else {
            return null;
        }
    }

    public BencodeObject getSymbols(String text) {
        char[] chars = text.toCharArray();
        int index = 0;

        BencodeObject.BStringType type = getType(text.charAt(index));
        if(type == BencodeObject.BStringType.string) {
            index++;
            StringBuffer sb = new StringBuffer();
            while(chars[index] != '\'') {
                sb.append(chars[index++]);
            }
            index++;
            BencodeObject bs = new BencodeObject();
            bs.setType(BencodeObject.BStringType.string);
            bs.setStr(sb.toString());
            return bs;
        }else if(type == BencodeObject.BStringType.integer) {
            boolean isNegative = false;
            if(chars[index] == '-') {
                isNegative = true;
                index++;
            }

            int num = Integer.parseInt(Character.toString(chars[index]));
            index++;
            while(index < chars.length && (chars[index] >= '0' && chars[index] <= '9')) {
                num *= 10;
                num += Integer.parseInt(Character.toString(chars[index]));
                index++;
            }
            if(isNegative) {
                num = 0 - num;
            }

            BencodeObject bs = new BencodeObject();
            bs.setType(BencodeObject.BStringType.integer);
            bs.setNum(num);
            return bs;
        }else if(type == BencodeObject.BStringType.list) {
            List<BencodeObject> elements = new LinkedList<>();
            int ls = index;
            int le = getSymbolEndIndex(text, ls);
            while(ls < le) {
                int e = getSymbolEndIndex(text, ls+1);
                BencodeObject bs = getSymbols(text.substring(ls+1,e+1));
                elements.add(bs);
                ls = e+1;
            }
            BencodeObject bs = new BencodeObject();
            bs.setType(BencodeObject.BStringType.list);
            bs.setElements(elements);
            return bs;
        }else if(type == BencodeObject.BStringType.dictionary) {
            int matchFlag = 0;
            Map<String, BencodeObject> map = new HashMap<>();
            do {
                if(chars[index] == '{') {
                    matchFlag++;
                }

                int s = text.indexOf('\'', index);
                int e = text.indexOf('\'', s+1);
                BencodeObject key = new BencodeObject();
                key.setType(BencodeObject.BStringType.string);
                key.setStr(text.substring(s+1, e));

                s = text.indexOf(':',e)+1;

                e = getSymbolEndIndex(text, s);
                BencodeObject value = new BencodeObject();
                value.setType(getType(text.charAt(s)));
                switch (value.getType()) {
                    case string: map.put(key.getStr(),getSymbols(text.substring(s, e+1)));break;
                    case integer:map.put(key.getStr(),getSymbols(text.substring(s, e+1)));break;
                    case list:;map.put(key.getStr(),getSymbols(text.substring(s, e+1)));break;
                    case dictionary:map.put(key.getStr(),getSymbols(text.substring(s, e+1)));break;
                    default:logger.info("error");
                }

                index = e + 1;
                if(chars[index] == '}') {
                    matchFlag--;
                }
            } while(matchFlag != 0);

            BencodeObject bs = new BencodeObject();
            bs.setType(BencodeObject.BStringType.dictionary);
            bs.setKv(map);
            return bs;
        }else {
            logger.error("error");
            return null;
        }
    }



    public int getSymbolEndIndex(String text, int fromIndex) {
        char c = text.charAt(fromIndex);
        if(c == '\'') {
            return text.indexOf('\'',fromIndex+1);
        }else if(c == '[') {
            int matchFlag = 0;
            do {
                if(text.charAt(fromIndex) == '[') matchFlag++;
                if(text.charAt(fromIndex) == ']') matchFlag--;
                fromIndex++;
            }while (matchFlag != 0);
            return fromIndex-1;
        }else if(c == '{') {
            int matchFlag = 0;
            do {
                if(text.charAt(fromIndex) == '{') matchFlag++;
                if(text.charAt(fromIndex) == '}') matchFlag--;
                fromIndex++;
            }while (matchFlag != 0);
            return fromIndex-1;
        }else if(c >= '0' && c <= '9') {
            fromIndex++;
            while(text.charAt(fromIndex) >= '0' && text.charAt(fromIndex) <= '9') {
                fromIndex++;
            }
            return fromIndex-1;
        }else {
            return -1;
        }
    }

    private int decodeIndex = 0;

    private BencodeObject decodeBSO(ByteBuffer buffer, int di) {
        decodeIndex = di;
        char c = (char) ((0x00 << 8) | buffer.get(decodeIndex));
        if(c >= '1' && c <= '9') {
            return decodeBSOString(buffer);
        }else if(c == 'i') {
            return decodeBSOInteger(buffer);
        }else if(c == 'l') {
            return decodeBSOList(buffer);
        }else if(c == 'd') {
            return decodeBSODictionary(buffer);
        }else {
            return null;
        }
    }

    private BencodeObject decodeBSODictionary(ByteBuffer buffer) {
        BencodeObject bencodeObject = new BencodeObject();
        bencodeObject.setType(BencodeObject.BStringType.dictionary);
        bencodeObject.setKv(new HashMap<>());


        decodeIndex++;
        while(decodeIndex <buffer.limit() && buffer.get(decodeIndex) != 'e') {
            bencodeObject.getKv().put(decodeBSOString(buffer).getStr(),decodeBSO(buffer,decodeIndex));
        }

        decodeIndex++;
        return bencodeObject;
    }

    private BencodeObject decodeBSOList(ByteBuffer buffer) {
        BencodeObject bencodeObject = new BencodeObject();
        bencodeObject.setType(BencodeObject.BStringType.list);
        bencodeObject.setElements(new ArrayList<>());

        decodeIndex++;
        while(buffer.get(decodeIndex) != 'e') {
            bencodeObject.getElements().add(decodeBSO(buffer,decodeIndex));
        }

        decodeIndex++;

        return bencodeObject;
    }

//    private String decodeInteger(char[] text) {
//        decodeIndex++;
//        StringBuilder sb = new StringBuilder();
//        while((decodeIndex < text.length) && text[decodeIndex] != 'e') {
//            sb.append(text[decodeIndex]);
//            decodeIndex++;
//        }
//
//        decodeIndex++;
//        return sb.toString();
//    }

    private BencodeObject decodeBSOInteger(ByteBuffer buffer) {
        BencodeObject bencodeObject = new BencodeObject();
        bencodeObject.setType(BencodeObject.BStringType.integer);

        decodeIndex++;
        StringBuilder sb = new StringBuilder();
        while((decodeIndex < buffer.limit()) && buffer.get(decodeIndex) != 'e') {
            sb.append((char)buffer.get(decodeIndex));
            decodeIndex++;
        }

        decodeIndex++;
        bencodeObject.setNum(Integer.parseInt(sb.toString()));
        return bencodeObject;
    }

//    private String decodeString(char[] text) {
//        int num = 0;
//        while(text[decodeIndex] != ':') {
//            num *= 10;
//            num += text[decodeIndex] - '0';
//            decodeIndex++;
//        }
//        decodeIndex++;
//        StringBuffer sb = new StringBuffer();
//        sb.append("'");
//        while((decodeIndex < text.length) && num-- != 0) {
//            sb.append(text[decodeIndex]);
//            decodeIndex++;
//        }
//        sb.append("'");
//
//        return sb.toString();
//    }

    private BencodeObject decodeBSOString(ByteBuffer buffer) {
        BencodeObject bencodeObject = new BencodeObject();
        bencodeObject.setType(BencodeObject.BStringType.string);

        int num = 0;
        while(buffer.get(decodeIndex) != ':') {
            num *= 10;
            num += buffer.get(decodeIndex) - '0';
            decodeIndex++;
        }
        decodeIndex++;
        StringBuffer sb = new StringBuffer();
        while((decodeIndex < buffer.limit()) && num-- != 0) {
            sb.append((char)buffer.get(decodeIndex));
            decodeIndex++;
        }
        bencodeObject.setStr(sb.toString());
        return bencodeObject;
    }


    public static String translatePeerContackInfo(String contackInfo) {
        byte[] bytes = contackInfo.getBytes(StandardCharsets.ISO_8859_1);
        StringBuffer sb = new StringBuffer();
        sb.append(Byte.toUnsignedInt(bytes[0]));
        sb.append(".");
        sb.append(Byte.toUnsignedInt(bytes[1]));
        sb.append(".");
        sb.append(Byte.toUnsignedInt(bytes[2]));
        sb.append(".");
        sb.append(Byte.toUnsignedInt(bytes[3]));
        sb.append(":");
        sb.append(Byte.toUnsignedInt(bytes[4])*256 + Byte.toUnsignedInt(bytes[5]));
        return sb.toString();
    }

    public static String translatePeerContackInfo(byte[] contackInfo) {
        if(contackInfo.length != 6) logger.info("contact info length is wrong : {}",contackInfo.length);

        byte[] bytes = contackInfo;
        StringBuffer sb = new StringBuffer();
        sb.append(Byte.toUnsignedInt(bytes[0]));
        sb.append(".");
        sb.append(Byte.toUnsignedInt(bytes[1]));
        sb.append(".");
        sb.append(Byte.toUnsignedInt(bytes[2]));
        sb.append(".");
        sb.append(Byte.toUnsignedInt(bytes[3]));
        sb.append(":");
        sb.append(Byte.toUnsignedInt(bytes[4])*256 + Byte.toUnsignedInt(bytes[5]));
        return sb.toString();
    }



    public static String translateNodeContactInfo(byte[] contactInfo) {
        if(contactInfo.length != 26) logger.error("node contact info length is wrong : {}", contactInfo.length);
        StringBuilder sb = new StringBuilder();
        sb.append(translateId(Arrays.copyOfRange(contactInfo,0,20)));
        sb.append("-");
        sb.append(translatePeerContackInfo(Arrays.copyOfRange(contactInfo,20,26)));
        return sb.toString();
    }

    //TODO need further develop
    public static String translateVersion(String version) {
        byte[] bytes = version.getBytes(StandardCharsets.ISO_8859_1);
        StringBuffer sb = new StringBuffer();
        sb.append((char)bytes[0]);
        sb.append((char)bytes[1]);
        sb.append("-");
        sb.append(bytes[2]);
        sb.append("-");
        sb.append(Byte.toString(bytes[3]));
        return sb.toString();
    }

    public static String translateId(String id) {
        return id;
    }

    public static String translateId(byte[] bytes) {
        if(bytes.length != 20) logger.error("node id length is wrong : {}", bytes.length);
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }
}
