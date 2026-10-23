package tuhljin.automagy.audit;

import java.util.Random;
import java.util.regex.Pattern;
import org.junit.Test;
import tuhljin.automagy.lib.TjUtil;
import static org.junit.Assert.*;

public class NameFilterTest {
    @Test(timeout = 2000) public void adversarialFilterDoesNotBacktrackExponentially() {
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < 15; i++) mask.append("*a");
        mask.append("*b");
        Pattern filter = TjUtil.getSafePatternUsingAsteriskForWildcard(mask.toString());
        for (int i = 0; i < 500; i++) assertFalse(filter.matcher("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").matches());
    }

    @Test public void wildcardAndLiteralSemanticsMatchReference() {
        Random random = new Random(7);
        String alphabet = "ab.[]\\\n*";
        for (int sample = 0; sample < 4000; sample++) {
            StringBuilder mask = new StringBuilder(), name = new StringBuilder(), reference = new StringBuilder();
            for (int i = random.nextInt(9); i > 0; i--) mask.append(alphabet.charAt(random.nextInt(alphabet.length())));
            for (int i = random.nextInt(9); i > 0; i--) name.append(alphabet.charAt(random.nextInt(alphabet.length())));
            for (int i = 0; i < mask.length(); i++) reference.append(mask.charAt(i) == '*' ? ".*" : Pattern.quote(mask.substring(i, i + 1)));
            assertEquals(mask + " / " + name, Pattern.matches(reference.toString(), name),
                TjUtil.getSafePatternUsingAsteriskForWildcard(mask.toString()).matcher(name).matches());
        }
    }

    @Test public void earliestInteriorMatchLeavesRoomForAnchoredSuffix() {
        assertTrue(TjUtil.getSafePatternUsingAsteriskForWildcard("ab*ab*ab").matcher("abababab").matches());
        assertFalse(TjUtil.getSafePatternUsingAsteriskForWildcard("ab*ab*ab").matcher("abababa").matches());
        assertTrue(TjUtil.getSafePatternUsingAsteriskForWildcard("a***b").matcher("a.b").matches());
        assertFalse(TjUtil.getSafePatternUsingAsteriskForWildcard("a*b").matcher("a\nb").matches());
    }
}
