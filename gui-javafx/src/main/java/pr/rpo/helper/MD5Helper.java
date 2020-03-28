package pr.rpo.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Helper {


    public static String getMD5CheckSum(File file) throws IOException {
        byte[] buffer = new byte[1024];

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try(InputStream is = Files.newInputStream(Paths.get(file.toURI()));
            DigestInputStream dis = new DigestInputStream(is,md)) {
            while(dis.read(buffer, 0, 1024) != -1) {}
        }

        byte[] digest = md.digest();

        String result = "";

        for (int i=0; i < digest.length; i++) {
            result += Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
}
