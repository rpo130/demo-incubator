package pr.rpo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr.rpo.kbucket.ContactInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class ContactInfoTest {
    private ContactInfo contactInfo;

    @BeforeEach
    public void setUp() throws Exception {
        contactInfo = new ContactInfo();
    }

    @Test
    public void compactInfo() throws UnknownHostException {
        contactInfo = new ContactInfo(InetAddress.getByName("127.0.0.1"),9090);

        assertArrayEquals(new byte[] {0x7f,0x00,0x00,0x01,(byte) (9090>>8 & 0xff), (byte) (9090&0xff)},contactInfo.compactInfo().getBytes(StandardCharsets.ISO_8859_1));
    }
}