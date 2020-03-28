package pr.rpo.kbucket;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import pr.rpo.Id;
import pr.rpo.util.Bit;

import java.util.Arrays;
import java.util.Base64;

public class NameSpace {
    private final static int BITLENGTH = 160;

    private byte[] id;

    public NameSpace(NameSpace nameSpace) {
        this.id = nameSpace.id.clone();
    }

    public NameSpace() {
        this.id = new byte[BITLENGTH / Byte.SIZE];
    }

    public NameSpace(byte[] bytes) {
        this.id = bytes;
    }

    public byte[] getRaw() {
        return this.id;
    }

    public void setRaw(byte[] bytes) {
        this.id = bytes;
    }

    public static int compare(NameSpace a, NameSpace b) {
        return Id.compare(a.getRaw(),b.getRaw());
    }

    public static NameSpace getSplitMask(NameSpace[] nameSpaces) {
        byte[] bytes = getBitMask(
                Arrays.stream(nameSpaces)
                        .map(e -> e.getRaw())
                        .toArray(byte[][]::new));
        Bit.shiftRightWithOne(bytes);
        NameSpace nameSpace = new NameSpace();
        nameSpace.setRaw(bytes);
        return nameSpace;
    }

    /**
     * 获取公共最长前缀
     * @param bytes
     * @return
     */
    private static byte[] getBitMask(byte[][] bytes) {
        int len = bytes.length;
        byte[] andR= bytes[0].clone(),orR= bytes[0].clone();
        for (int i = 0; i < len; i++) {
            andR = Bit.and(andR,bytes[i]);
            orR = Bit.or(orR,bytes[i]);
        }

        byte[] notXorR = Bit.inversion(Bit.xor(andR,orR));
        int pre1len = Bit.getPrefixOneNumFromLow(notXorR);

        Bit.clear(notXorR);
        while(pre1len > 0) {
            Bit.setBitInFromLow(notXorR,pre1len--);
        }

        return notXorR;

    }

    @Deprecated
    private static byte getBitMask(byte[] bytes) {
        int len = bytes.length;

        byte andR=bytes[0],orR=bytes[0];
        for (int i = 0; i < len; i++) {
            andR &= bytes[i];
            orR |= bytes[i];
        }

        int r = 0;
        byte xorR = (byte) (andR ^ orR);
        r = Byte.toUnsignedInt(xorR);
        if(r >= 128) {
            return (byte) 0b1000_0000;
        }else if(r >= 64) {
            return (byte) 0b1100_0000;
        }else if(r >= 32) {
            return (byte) 0b1110_0000;
        }else if(r >= 16) {
            return (byte) 0b1111_0000;
        }else if(r >= 8) {
            return (byte) 0b1111_1000;
        }else if(r >= 4) {
            return (byte) 0b1111_1100;
        }else if(r >= 2) {
            return (byte) 0b1111_1110;
        }else if(r >= 1){
            return (byte) 0b1111_1111;
        }else {
            return (byte) 0b0000_0000;
        }
    }

    public String hexString() {
        return ByteArrayUtil.toHexString(id);
    }

    public String base64String() {
        return Base64.getEncoder().encodeToString(id);
    }
}
