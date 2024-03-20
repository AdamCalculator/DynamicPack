package tests;

import com.adamcalculator.dynamicpack.InputValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
