package pr.rpo.kbucket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class ContactInfo {
    private InetSocketAddress isa;

    public ContactInfo() {}

    public ContactInfo(InetAddress addr, int port) {
        this.isa = new InetSocketAddress(addr, port);
    }

    public ContactInfo(ContactInfo contactInfo) {
        this.isa = new InetSocketAddress(contactInfo.isa.getAddress(),contactInfo.isa.getPort());
    }

    public String compactInfo() {
        InetAddress addr = isa.getAddress();
        int port = isa.getPort();

        StringBuilder sb = new StringBuilder();
        sb.append((char)addr.getAddress()[0]);
        sb.append((char)addr.getAddress()[1]);
        sb.append((char)addr.getAddress()[2]);
        sb.append((char)addr.getAddress()[3]);
        sb.append((char)((port >> 8) & 0xff));
        sb.append((char)(port & 0xff));
        return sb.toString();
    }

    public InetAddress getAddr() {
        return isa.getAddress();
    }

    public int getPort() {
        return isa.getPort();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ContactInfo) {
            ContactInfo cif = (ContactInfo) obj;
            if (!this.getAddr().equals(cif.getAddr())) return false;

            if (this.getPort() != cif.getPort()) return false;

            return true;
        }else {
            return false;
        }
    }

    public static ContactInfo decodeCompactPeerInfo(byte[] bytes, int offset) {
        try {
            ContactInfo cinfo;
            InetAddress ia = InetAddress.getByName(Byte.toUnsignedInt(bytes[offset+0])
                                    +"."+Byte.toUnsignedInt(bytes[offset+1])
                                    +"."+Byte.toUnsignedInt(bytes[offset+2])
                                    +"."+Byte.toUnsignedInt(bytes[offset+3]));
            int port = Byte.toUnsignedInt(bytes[offset+4])*256 + Byte.toUnsignedInt(bytes[offset+5]);
            cinfo = new ContactInfo(ia,port);
            return cinfo;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
                isa.getAddress().getHostAddress() + ":" + isa.getPort() +
                '}';
    }
}
