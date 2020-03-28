package pr.rpo.util;

public class Bit {

    public static byte[] inversion(byte[] bytes) {
        byte[] ret = new byte[bytes.length];
        for(int i=bytes.length-1; i>=0; i--) {
            ret[i] = (byte) ~bytes[i];
        }
        return ret;
    }

    public static void clear(byte[] bytes) {
        for(int i=0; i<bytes.length; i++) {
            bytes[i] &= 0x00;
        }
    }

    public static byte[] and(byte[] a, byte[] b) {
        byte[] r = a.clone();
        for(int i=r.length-1; i>=0; i--) {
            r[i] &= b[i];
        }
        return r;
    }

    public static byte[] or(byte[] a, byte[] b) {
        byte[] r = a.clone();
        for(int i=r.length-1; i>=0; i--) {
            r[i] |= b[i];
        }
        return r;
    }

    public static byte[] xor(byte[] a, byte[] b) {
        byte[] r = a.clone();
        for(int i=r.length-1; i>=0; i--) {
            r[i] ^= b[i];
        }
        return r;
    }

    public static int compareBigEndian(byte[] a, byte[] b) {
        for(int i=0; i<a.length; i++) {
            int r = Byte.compareUnsigned(a[i],b[i]);
            if(r != 0) {
                return r;
            }
        }
        return 0;
    }

    public static int compareLittleEndian(byte[] a, byte[] b) {
        for(int i=a.length-1; i>=0; i--) {
            int r = Byte.compareUnsigned(a[i],b[i]);
            if(r != 0) {
                return r;
            }
        }
        return 0;
    }

    public static void shiftRightWithOne(byte[] b) {
        byte[] a = b;
        for(int i=0; i<a.length-1; i++) {
            a[i] = (byte) ((a[i+1] << 7) | (a[i] >>> 1));
        }
        a[a.length-1] >>>= 1;
        a[a.length-1] |= 0x80;
    }

    public static byte[] shiftRightWithZero(byte[] b) {
        byte[] a = b.clone();
        for(int i=0; i<a.length-1; i++) {
            a[i] = (byte) ((a[i+1] << 7) | (a[i] >>> 1));
        }
        a[a.length-1] >>>= 1;
        return a;
    }

    public static byte[] shiftLeftWithZero(byte[] b) {
        byte[] a = b.clone();
        for(int i=a.length-1; i>0; i--) {
            a[i] = (byte) ((a[i] << 1) | (a[i-1] >>> 7));
        }
        a[0] <<= 1;
        return a;
    }

    //little endian
    //MSB...LSB
    public static int getPrefixOneNumFromLow(byte[] a) {
        int count = 0;
        for(int i=0; i<a.length;i++) {
            int t = getPrefixOneNumFromLow(a[i]);
            count +=t;

            if(t != 8) {
                break;
            }
        }
        return count;
    }

    public static int getPrefixOneNumFromLow(byte a) {
        int count=0;
        for (int i=7; i>=0; i--) {
            byte b = a;
            b >>>= i;

            if((b & 0x01) == 0x01) {
                count++;
            }else {
                break;
            }
        }
        return count;
    }

    public static void clearBitInFromLow(byte[] a, int bitLocation) {
        byte[] b = a;
        for(int i=0; i<b.length;i++) {

            byte bitMask = 0x00;
            if(bitLocation == 1) {
                bitMask = 0x7f;
            }else if(bitLocation == 2) {
                bitMask = (byte) 0xbf;
            }else if(bitLocation == 3) {
                bitMask = (byte) 0xdf;
            }else if(bitLocation == 4) {
                bitMask = (byte) 0xef;
            }else if(bitLocation == 5) {
                bitMask = (byte) 0xf7;
            }else if(bitLocation == 6) {
                bitMask = (byte) 0xfb;

            }else if(bitLocation == 7) {
                bitMask = (byte) 0xfd;

            }else if(bitLocation == 8) {
                bitMask = (byte) 0xfe;

            }else {
                bitLocation -= 8;
                continue;
            }

            b[i] &= bitMask;
            return;
        }
        return;
    }

    public static byte[] clearBitInFromHigh(byte[] a, int bitLocation) {
        byte[] b = a.clone();
        for(int i=a.length-1;i>=0;i--) {

            byte bitMask = 0x00;
            if(bitLocation == 1) {
                bitMask = 0x7f;
            }else if(bitLocation == 2) {
                bitMask = (byte) 0xbf;
            }else if(bitLocation == 3) {
                bitMask = (byte) 0xdf;
            }else if(bitLocation == 4) {
                bitMask = (byte) 0xef;
            }else if(bitLocation == 5) {
                bitMask = (byte) 0xf7;
            }else if(bitLocation == 6) {
                bitMask = (byte) 0xfb;

            }else if(bitLocation == 7) {
                bitMask = (byte) 0xfd;

            }else if(bitLocation == 8) {
                bitMask = (byte) 0xfe;

            }else {
                bitLocation -= 8;
                continue;
            }

            b[i] &= bitMask;
            return b;
        }
        return b;
    }

    public static void setBitInFromLow(byte[] a, int bitLocation) {
        byte[] b = a;
        for(int i=0; i<a.length; i++) {

            byte bitMask = 0x00;
            if(bitLocation == 1) {
                bitMask = ~0x7f;
            }else if(bitLocation == 2) {
                bitMask = (byte) ~0xbf;
            }else if(bitLocation == 3) {
                bitMask = (byte) ~0xdf;
            }else if(bitLocation == 4) {
                bitMask = (byte) ~0xef;
            }else if(bitLocation == 5) {
                bitMask = (byte) ~0xf7;
            }else if(bitLocation == 6) {
                bitMask = (byte) ~0xfb;

            }else if(bitLocation == 7) {
                bitMask = (byte) ~0xfd;

            }else if(bitLocation == 8) {
                bitMask = (byte) ~0xfe;

            }else {
                bitLocation -= 8;
                continue;
            }

            b[i] |= bitMask;
            return;
        }
    }

    public static byte[] setBitInFromHigh(byte[] a, int bitLocation) {
        byte[] b = a.clone();
        for(int i=a.length-1;i>=0;i--) {

            byte bitMask = 0x00;
            if(bitLocation == 1) {
                bitMask = ~0x7f;
            }else if(bitLocation == 2) {
                bitMask = (byte) ~0xbf;
            }else if(bitLocation == 3) {
                bitMask = (byte) ~0xdf;
            }else if(bitLocation == 4) {
                bitMask = (byte) ~0xef;
            }else if(bitLocation == 5) {
                bitMask = (byte) ~0xf7;
            }else if(bitLocation == 6) {
                bitMask = (byte) ~0xfb;

            }else if(bitLocation == 7) {
                bitMask = (byte) ~0xfd;

            }else if(bitLocation == 8) {
                bitMask = (byte) ~0xfe;

            }else {
                bitLocation -= 8;
                continue;
            }

            b[i] |= bitMask;
            return b;
        }
        return b;
    }

    public static String toHexString(byte[] bytes) {
        final char[] DICT = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

        char[] hex = new char[bytes.length * 2];
        for(int i=bytes.length-1; i>=0; i--) {
            hex[i * 2 + 1] = DICT[bytes[i] >>> 4];
            hex[i * 2] = DICT[bytes[i] & 0x0f];
        }
        return hex.toString();
    }

    //a+b compare c+d
    public static int compareLittleEndian(byte[] a, byte[] b, byte[] c, byte[] d) {
        byte[] c1 = add(a,b);
        byte[] c2 = add(c,d);
        if(c1.length != c2.length) {
            return c1.length-c2.length;
        }else {
            return compareLittleEndian(c1,c2);
        }
    }

    public static int compareBigEndian(byte[] a, byte[] b, byte[] c, byte[] d) {
        byte[] c1 = add(a,b);
        byte[] c2 = add(c,d);
        if(c1.length != c2.length) {
            return c1.length-c2.length;
        }else {
            return compareBigEndian(c1,c2);
        }
    }

    //TODO jinwei
    public static byte[] add(byte[] a, byte[] b) {
        byte[] c = new byte[a.length];
        for(int i=0; i<a.length; i++) {
            c[i] = (byte) (Byte.toUnsignedInt(a[i]) + Byte.toUnsignedInt(b[i]));
        }
        return c;
    }
}
