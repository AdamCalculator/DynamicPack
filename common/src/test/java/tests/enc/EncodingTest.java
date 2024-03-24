package tests.enc;

import com.adamcalculator.dynamicpack.util.Out;
import com.adamcalculator.dynamicpack.util.enc.GPGSignatureVerifier;
import org.apache.commons.codec.binary.Base64InputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class EncodingTest {

    @Test
    public void d() throws IOException {
        Out.USE_SOUT = true;

        Random random = new Random();
        byte[] bytes1 = new byte[1024*1024*10];
        random.nextBytes(bytes1);
        InputStream i1 = new ByteArrayInputStream(bytes1);


        InputStream i2 = new Base64InputStream(new ByteArrayInputStream(("iQIzBAEBCgAdFiEEVEY0vLHMGf8Y24yJRRAdXYuqzLEFAmX1LxoACgkQRRAdXYuq\n" +
                                                                         "zLEMLw//SqaEHYopukZqEETrHJTv67MBNLoDeauwKCEc8xEA/bjciLV5sPwECBt7\n" +
                                                                         "OeZfK9RuqvBko+Kfn7UIgbsYMc7mYq8qwwD8q/tgXRmFhuNMUJCBbjemG/CASBky\n" +
                                                                         "W1ZArhQDFUJd/vPkrNWlWwfvicOYHA4sn3N70ZuR5afs4HwJI2G85NQQlW1JLu9d\n" +
                                                                         "dWGkUQ+UAaC8zL5YT7U2rVr2X/BvUDHKGqgHifnkxg9XemSKR8yJ9vaitjuIUynp\n" +
                                                                         "LpRGetDWlbIOVeilVgkzziJ5pntjOc1CKvkLg6t9fePzMbj6TGgY+B35+1tn/mUg\n" +
                                                                         "Motrb9NcTJ9cRi1ZuzD1UG4gUieki8iSp4uLp9sX61CWeJsW65PxfrL6Op9R7UHP\n" +
                                                                         "ndiv9HynmrZSv8WWiHNEVxErU9FdMF2bTN6/8geYuyngJvd2ORaoEvgoQeMq7b9g\n" +
                                                                         "6kADXgNmn2Vmj2BOwTxCq4DSzEfRYOCXCqwlG4jisGqBUs5Kq/SLXE1Vo2+BK8YR\n" +
                                                                         "XR3stGhDyBzk+a5ECgEpxuRCxyyTD9I3OdF2X6G43RlWSLLO43O70NQMjFQC8Khx\n" +
                                                                         "A0SJ5J9X/FhJMdzF4mfZrExFEEWPeCOcOuKhI7bUqbaRrwets42Na4RvkOsisZdA\n" +
                                                                         "yvDRQSTV5xIOg5HOVi/2HBdnCHKkc/ZF/UjpIHP8gC4QoSSYI/0=\n" +
                                                                         "=08ZH\n").getBytes(StandardCharsets.UTF_8)));

        var verified = GPGSignatureVerifier.verify(i1, i2, "mQINBGX1YNIBEAC3JgBxOPjy1BaQeHFU9fKaS6pCA647RUryfE7A/9mmhDZV6RyJ\n" +
                                                           "miGAw6QKuVCzez8cbQXC6FgEp/2Iq07OzajHsSZW4ibC6GHp1pe7IqiM1Ad4huIX\n" +
                                                           "1fLBXkz3SkIt2Ef/L85rKTCx3SJxGrOPY33+dKfv7cOjwD38DiGKiNVundY+CKox\n" +
                                                           "A44FTBwpXLwaLIRLgtrGwEetQZJZkjMonSQ7UWWIuxITiuffJB37k2Wc44DE7u3J\n" +
                                                           "PqopHM1IiQe65226aUpLzUxLltEWBwEmMG4mrCLrkV4pWUuIu+A6oG8twEni3AY8\n" +
                                                           "Q0ABw/LGKYEs3lXUCn/hDc/tlX4xz/6SMOitsloVtVh604KQd30HTFX7tLeKertB\n" +
                                                           "0zeIEDU3lFAtccN5+8znK9knsYykHVV83UA0XrTDazloWdKzkMMiFgRvOJTKRj7n\n" +
                                                           "SL5zC3flA5XFa9jW+ik5EHqkkiNtNZ4+eUXacilEWtvcALoCmJAXz9LFq5Bvv8D5\n" +
                                                           "D0htpY30ws2c/qXob/4SAOR3rcSUSurOIWNF//+rgyakcb7fOkF24hKVvYOfXErq\n" +
                                                           "wFlhEieto3tPG4t5CaYET9dv8t0ft76CdMwHB7JkD1+jEnNF/QAQUY+W4JcJykUG\n" +
                                                           "3Ls0qJozex7IWyAgxss+s9Cseq9LlbPTcNffXO9QzDP5Q5FVbX3sUDv66wARAQAB\n" +
                                                           "tCNbTUNdIGZyb2dwYWNrLmdpdGh1Yi5pbyBkeW5hbWljcGFja4kCTgQTAQoAOBYh\n" +
                                                           "BJ2+4aAc2C2//Vqt6vrkntmWKWCjBQJl9WDSAhsDBQsJCAcCBhUKCQgLAgQWAgMB\n" +
                                                           "Ah4BAheAAAoJEPrkntmWKWCjSaYQAJ68XZC79s7vRoHIIQYONZaJgUC6RzMyxvFy\n" +
                                                           "OcuAwMnH3MRPviDUE9swIS3MYJHfphGT5LMsIs+OinLZIqsg+eB7xd+SZOYFpfjf\n" +
                                                           "CIo+Tj8T7Pnd3IQ6rPn9cb1fzaxsogVRdM48dNCUNOP0iyG+2bAJhL9pKKvSGx3b\n" +
                                                           "ynMkUpSOK5amDXqXfzn8O1jvpzxpRHPdlcSUxxTBCNzwMneipq2OG8dh8F8RWOmu\n" +
                                                           "769D398i9GYbvrw8J4+rSs962LiCF68mnAUEhTH68HyGPNN4Sgn0V6c0eqz8D49E\n" +
                                                           "dVqVn8LsjQ5ut8cI/IBocLSnz//RIJk0exI78JHZEt0C0ryDGVTNHqTGawomQz7h\n" +
                                                           "wNDuVxId8k+6cFLnfVKxPjh6apaAgUSqs91dFIxd3+9wTqF3ANQuDkYb91BCUlIP\n" +
                                                           "q1y3cWf++QOCw+q5qf5PN6OTwfHItWmx2n9zQWm2u+HliW4MZ+f+4TgdTmzRansz\n" +
                                                           "vSgaJ5zrkOkrM6aJTKqb/C0skYG4edCEwgbQyp7KtdqxSm+ojumbo2fGEFmqe2em\n" +
                                                           "4D1bWw9TP/wu1YN1p8krSMnTln+/A2BxulDkiah7V6AoqQl9NL54jT+JgdsSql2P\n" +
                                                           "WQnfqjMGJPw8AHzYbv9UycgEfgJv/uG8R+13I8WkiMQXksXBUoPjyRpNJWh7tn/W\n" +
                                                           "P/0Q1LZG\n" +
                                                           "=Id/2\n");
        System.out.println(verified);
        assertFalse(verified);
    }
}
