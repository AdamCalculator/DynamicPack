package tests;

import com.adamcalculator.dynamicpack.InputValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class InputValidatorTest {

    @Test
    public void testContentId() {
        Assertions.assertFalse(InputValidator.isContentIdValid(""));
        Assertions.assertFalse(InputValidator.isContentIdValid(" "));
        Assertions.assertFalse(InputValidator.isContentIdValid("  "));
        Assertions.assertFalse(InputValidator.isContentIdValid("   32"));
        Assertions.assertFalse(InputValidator.isContentIdValid("test\ntest"));

        Assertions.assertTrue(InputValidator.isContentIdValid("__"));
        Assertions.assertTrue(InputValidator.isContentIdValid("_-"));
        Assertions.assertTrue(InputValidator.isContentIdValid("pack:megapack"));
        Assertions.assertTrue(InputValidator.isContentIdValid("1234567890"));
        Assertions.assertTrue(InputValidator.isContentIdValid("01"));
        Assertions.assertTrue(InputValidator.isContentIdValid("test_pack"));
        Assertions.assertTrue(InputValidator.isContentIdValid("super:mega_puper:"));
    }

    @Test
    public void testRemoteName() {
        Assertions.assertFalse(InputValidator.isPackNameValid("\n"));
        Assertions.assertTrue(InputValidator.isPackNameValid("__"));
    }

    @Test
    public void testPaths() {
        InputValidator.validOrThrownPath("");
        InputValidator.validOrThrownPath(" ");
        InputValidator.validOrThrownPath("/file/p.txt");

        Assertions.assertThrows(SecurityException.class, () -> {
            InputValidator.validOrThrownPath("!/file/p.txt");
        });

        Assertions.assertThrows(SecurityException.class, () -> {
            InputValidator.validOrThrownPath(null);
        });
        Assertions.assertThrows(SecurityException.class, () -> {
            InputValidator.validOrThrownPath("$@#");
        });
        Assertions.assertThrows(SecurityException.class, () -> {
            InputValidator.validOrThrownPath("!\"/file/p.txt");
        });        Assertions.assertThrows(SecurityException.class, () -> {
            InputValidator.validOrThrownPath("!/fil*&^%544e/p.txt");
        });
        Assertions.assertThrows(SecurityException.class, () -> {
            byte[] b = new byte[128];
            new Random().nextBytes(b);
            InputValidator.validOrThrownPath(new String(b));
        });


        try {
            byte[] b = new byte[128];
            new Random().nextBytes(b);
            InputValidator.validOrThrownPath(new String(b));
        } catch (Exception e) {
            System.out.println(e);
        }

        Assertions.assertThrows(SecurityException.class, () -> {
            InputValidator.validOrThrownPath("~");
        });
        Assertions.assertThrows(SecurityException.class, () -> {
            InputValidator.validOrThrownPath("()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)()*)*&YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY)");
        });

    }
}
