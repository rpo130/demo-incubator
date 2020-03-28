package pr.rpo.bencode;

import java.nio.ByteBuffer;

public interface Bencode {
    ByteBuffer encode(BencodeObject obj);
    BencodeObject decode(ByteBuffer buffer);
}
