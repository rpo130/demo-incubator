package pr.rpo.msg;

import java.util.concurrent.atomic.AtomicInteger;

public interface KRpc {
    KrpcMsg query();
    KrpcMsg respond();
    KrpcMsg error();
    String genTid();


    class genTid {
        private static AtomicInteger tid = new AtomicInteger(0);

        public static String genTid() {

            byte a = (byte) (tid.get() >> 8);
            byte b = (byte) (tid.get() & 0xff);
            tid.addAndGet(1);
            return "" + Integer.toHexString(a) + Integer.toHexString(b);
        }
    }
}
