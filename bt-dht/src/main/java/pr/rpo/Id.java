package pr.rpo;

import pr.rpo.util.Bit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Id {
    private final static String prefix = "-BOWA0C-";
    private final static String ASCII_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.-";

    private static Random r = new Random();

    public static byte[] generateToken() {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("sha1");

            return md.digest(Id.generate().getBytes(StandardCharsets.ISO_8859_1));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

    public static String generate() {
//        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for(int i = 0; i < 12; i++) {
             sb.append(ASCII_CHARACTERS.charAt(r.nextInt(ASCII_CHARACTERS.length())));
        }
        return sb.toString().toUpperCase();
    }

    public static int distanceCompare(byte[] a, byte[] b) {
        for(int i=0; i<a.length; i++) {
            if(Byte.toUnsignedInt(a[i]) > Byte.toUnsignedInt(b[i])) {
                return 1;
            }else if(Byte.toUnsignedInt(a[i]) < Byte.toUnsignedInt(b[i])) {
                return -1;
            }else {

            }
        }
        return 0;
    }

    public static byte[] distance(byte[] aId, byte[] bId) {
        return Bit.xor(aId, bId);
    }



//    private static AtomicInteger tid = new AtomicInteger(0);
//
//    public static String genTid() {
//
//        byte a = (byte) (tid.get() >> 8);
//        byte b = (byte) (tid.get() & 0xff);
//        tid.addAndGet(1);
//        return "" + Integer.toHexString(a) + Integer.toHexString(b);
//    }

    public static String getVersion() {
        return "NN00";
    }

    public static int compare(byte[] a, byte[] b) {
        for(int i = 0; i<a.length; i++) {
            int r = Byte.compareUnsigned(a[i],b[i]);
            if(r != 0) {
                return r;
            }
        }
        return 0;
    }

}
