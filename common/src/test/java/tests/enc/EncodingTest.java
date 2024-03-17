package tests.enc;

import com.adamcalculator.dynamicpack.util.Out;
import com.adamcalculator.dynamicpack.util.enc.GPGDetachedSignatureVerifier;
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


        InputStream i2 = new Base64InputStream(new ByteArrayInputStream("""
                iQIzBAEBCgAdFiEEVEY0vLHMGf8Y24yJRRAdXYuqzLEFAmX1LxoACgkQRRAdXYuq
                zLEMLw//SqaEHYopukZqEETrHJTv67MBNLoDeauwKCEc8xEA/bjciLV5sPwECBt7
                OeZfK9RuqvBko+Kfn7UIgbsYMc7mYq8qwwD8q/tgXRmFhuNMUJCBbjemG/CASBky
                W1ZArhQDFUJd/vPkrNWlWwfvicOYHA4sn3N70ZuR5afs4HwJI2G85NQQlW1JLu9d
                dWGkUQ+UAaC8zL5YT7U2rVr2X/BvUDHKGqgHifnkxg9XemSKR8yJ9vaitjuIUynp
                LpRGetDWlbIOVeilVgkzziJ5pntjOc1CKvkLg6t9fePzMbj6TGgY+B35+1tn/mUg
                Motrb9NcTJ9cRi1ZuzD1UG4gUieki8iSp4uLp9sX61CWeJsW65PxfrL6Op9R7UHP
                ndiv9HynmrZSv8WWiHNEVxErU9FdMF2bTN6/8geYuyngJvd2ORaoEvgoQeMq7b9g
                6kADXgNmn2Vmj2BOwTxCq4DSzEfRYOCXCqwlG4jisGqBUs5Kq/SLXE1Vo2+BK8YR
                XR3stGhDyBzk+a5ECgEpxuRCxyyTD9I3OdF2X6G43RlWSLLO43O70NQMjFQC8Khx
                A0SJ5J9X/FhJMdzF4mfZrExFEEWPeCOcOuKhI7bUqbaRrwets42Na4RvkOsisZdA
                yvDRQSTV5xIOg5HOVi/2HBdnCHKkc/ZF/UjpIHP8gC4QoSSYI/0=
                =08ZH
                """.getBytes(StandardCharsets.UTF_8)));

        var verified = GPGDetachedSignatureVerifier.verify(i1, i2, """
                mQINBGX1YNIBEAC3JgBxOPjy1BaQeHFU9fKaS6pCA647RUryfE7A/9mmhDZV6RyJ
                miGAw6QKuVCzez8cbQXC6FgEp/2Iq07OzajHsSZW4ibC6GHp1pe7IqiM1Ad4huIX
                1fLBXkz3SkIt2Ef/L85rKTCx3SJxGrOPY33+dKfv7cOjwD38DiGKiNVundY+CKox
                A44FTBwpXLwaLIRLgtrGwEetQZJZkjMonSQ7UWWIuxITiuffJB37k2Wc44DE7u3J
                PqopHM1IiQe65226aUpLzUxLltEWBwEmMG4mrCLrkV4pWUuIu+A6oG8twEni3AY8
                Q0ABw/LGKYEs3lXUCn/hDc/tlX4xz/6SMOitsloVtVh604KQd30HTFX7tLeKertB
                0zeIEDU3lFAtccN5+8znK9knsYykHVV83UA0XrTDazloWdKzkMMiFgRvOJTKRj7n
                SL5zC3flA5XFa9jW+ik5EHqkkiNtNZ4+eUXacilEWtvcALoCmJAXz9LFq5Bvv8D5
                D0htpY30ws2c/qXob/4SAOR3rcSUSurOIWNF//+rgyakcb7fOkF24hKVvYOfXErq
                wFlhEieto3tPG4t5CaYET9dv8t0ft76CdMwHB7JkD1+jEnNF/QAQUY+W4JcJykUG
                3Ls0qJozex7IWyAgxss+s9Cseq9LlbPTcNffXO9QzDP5Q5FVbX3sUDv66wARAQAB
                tCNbTUNdIGZyb2dwYWNrLmdpdGh1Yi5pbyBkeW5hbWljcGFja4kCTgQTAQoAOBYh
                BJ2+4aAc2C2//Vqt6vrkntmWKWCjBQJl9WDSAhsDBQsJCAcCBhUKCQgLAgQWAgMB
                Ah4BAheAAAoJEPrkntmWKWCjSaYQAJ68XZC79s7vRoHIIQYONZaJgUC6RzMyxvFy
                OcuAwMnH3MRPviDUE9swIS3MYJHfphGT5LMsIs+OinLZIqsg+eB7xd+SZOYFpfjf
                CIo+Tj8T7Pnd3IQ6rPn9cb1fzaxsogVRdM48dNCUNOP0iyG+2bAJhL9pKKvSGx3b
                ynMkUpSOK5amDXqXfzn8O1jvpzxpRHPdlcSUxxTBCNzwMneipq2OG8dh8F8RWOmu
                769D398i9GYbvrw8J4+rSs962LiCF68mnAUEhTH68HyGPNN4Sgn0V6c0eqz8D49E
                dVqVn8LsjQ5ut8cI/IBocLSnz//RIJk0exI78JHZEt0C0ryDGVTNHqTGawomQz7h
                wNDuVxId8k+6cFLnfVKxPjh6apaAgUSqs91dFIxd3+9wTqF3ANQuDkYb91BCUlIP
                q1y3cWf++QOCw+q5qf5PN6OTwfHItWmx2n9zQWm2u+HliW4MZ+f+4TgdTmzRansz
                vSgaJ5zrkOkrM6aJTKqb/C0skYG4edCEwgbQyp7KtdqxSm+ojumbo2fGEFmqe2em
                4D1bWw9TP/wu1YN1p8krSMnTln+/A2BxulDkiah7V6AoqQl9NL54jT+JgdsSql2P
                WQnfqjMGJPw8AHzYbv9UycgEfgJv/uG8R+13I8WkiMQXksXBUoPjyRpNJWh7tn/W
                P/0Q1LZG
                =Id/2
                """);
        System.out.println(verified);
        assertFalse(verified);
    }
}
