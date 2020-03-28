package pr.rpo.bencode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BencodeTest {

    private Bencode bencoderImpl;
    private String[] oriText = {
            "123",
            "-123",
            "'abc'",
            "['hi','hello']",
            "[123,'hi']",
            "{'abc':'def'}",
            "{'abc':'def','ghi':'jkl'}",
            "{'abc':123}",
            "{'ab':'cd','e':{'abc':123}}",
            "{'abc':['a','bc','d']}",
            "{'abc':['a','bc','d'],'d':'e'}"
    };

    private byte[][] encodedText = {
            "i123e".getBytes(StandardCharsets.ISO_8859_1),
            "i-123e".getBytes(StandardCharsets.ISO_8859_1),
            "3:abc".getBytes(StandardCharsets.ISO_8859_1),
            "l2:hi5:helloe".getBytes(StandardCharsets.ISO_8859_1),
            "li123e2:hie".getBytes(StandardCharsets.ISO_8859_1),
            "d3:abc3:defe".getBytes(StandardCharsets.ISO_8859_1),
            "d3:abc3:def3:ghi3:jkle".getBytes(StandardCharsets.ISO_8859_1),
            "d3:abci123ee".getBytes(StandardCharsets.ISO_8859_1),
            "d2:ab2:cd1:ed3:abci123eee".getBytes(StandardCharsets.ISO_8859_1),
            "d3:abcl1:a2:bc1:dee".getBytes(StandardCharsets.ISO_8859_1),
            "d3:abcl1:a2:bc1:de1:d1:ee".getBytes(StandardCharsets.ISO_8859_1)
    };

    @BeforeEach
    public void setUp() throws Exception {
        bencoderImpl = new BencodeImpl();
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void encode() {
        for(int i=0; i<oriText.length;i++) {
            assertArrayEquals(encodedText[i], bencoderImpl.encode(BencodeObject.newFromText(oriText[i])).array());

        }
    }

    @Test
    public void decode() {
        for(int i=0; i<oriText.length; i++) {
            assertEquals(oriText[i], bencoderImpl.decode(ByteBuffer.wrap(encodedText[i])).decodeToString());
        }
    }
}