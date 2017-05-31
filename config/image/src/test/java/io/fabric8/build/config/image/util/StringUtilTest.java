package io.fabric8.build.config.image.util;

import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author roland
 * @since 14.10.14
 */
@RunWith(JUnitParamsRunner.class)
public class StringUtilTest {


    @Test
    public void splitPath() {
        Iterator<String[]> it = StringUtil.splitOnLastColon(Arrays.asList("db", "postgres:9:db", "postgres:db", "atlast:")).iterator();
        String[][] expected = new String[][] {
                { "db", "db"},
                { "postgres:9","db"},
                { "postgres", "db"},
                { "atlast", ""}
        };
        for (int i = 0; i < expected.length; i++) {
            String[] got = it.next();
            assertEquals(2,got.length);
            assertEquals(expected[i][0],got[0]);
            assertEquals(expected[i][1],got[1]);
        }
        assertFalse(it.hasNext());
    }

    @Test
    public void splitAtCommas() {
        Iterable<String> it = StringUtil.splitAtCommasAndTrim(Arrays.asList("db,postgres:9:db", "postgres:db"));
        Iterable<String> expected = ImmutableList.of("db", "postgres:9:db", "postgres:db");
        assertTrue(Iterables.elementsEqual(it, expected));
    }

    public void assertEmptyList(Iterable<String> actual) {
        assertTrue(Iterables.elementsEqual(Collections.emptyList(), actual));
    }
    @Test
    public void splitAtCommasEmpty() {
        assertEmptyList(StringUtil.splitAtCommasAndTrim(Collections.<String>emptyList()));
    }

    @Test
    public void splitAtCommasSingleEmpty() {
        assertEmptyList(StringUtil.splitAtCommasAndTrim(Arrays.asList("")));
    }

    @Test
    public void splitAtCommasNullList() {
        assertEmptyList(StringUtil.splitAtCommasAndTrim(null));
    }

    // null occurs when <links><link></link></links>
    @Test
    public void splitAtCommasNullInList() {
        assertEmptyList(StringUtil.splitAtCommasAndTrim(Collections.<String>singletonList(null)));
    }

    @Test
    @TestCaseName("{method}: input \"{0}\" splits to {1}")
    @Parameters
    public void splitOnSpace(String input, String[] expected) {
        String[] result = StringUtil.splitOnSpaceWithEscape(input);
        assertEquals(expected.length, result.length);
        for (int j = 0; j < expected.length; j++) {
            assertEquals(expected[j],result[j]);
        }
    }

    private Object parametersForSplitOnSpace() {
        return $(
            $("bla blub", new String[] { "bla", "blub"}),
            $("bla\\ blub", new String[] {"bla blub"}),
            $("bla blub\\ blubber", new String[] { "bla", "blub blubber"})
                );
    }


    @Test
    @TestCaseName("{method}: max({0},{1}) = {2}, {0} >= {1} ? {3}")
    @Parameters
    public void versionChecks(String versionA, String versionB, String largerVersion, boolean isGreaterOrEquals) {
        assertEquals(largerVersion, StringUtil.extractLargerVersion(versionA,versionB));
        assertEquals(isGreaterOrEquals, StringUtil.greaterOrEqualsVersion(versionA,versionB));
    }

    private Object parametersForVersionChecks() {
        return $(
            $(null, null, null, false),
            $("1.10", null, "1.10", true),
            $(null, "1.10", "1.10", false),
            $("1.22", "1.10", "1.22", true),
            $("1.10", "1.25", "1.25", false),
            $("1.23", "1.23", "1.23", true),
            $("1.23.1", "1.23", "1.23.1", true),
            $("1.25", "1.25.1", "1.25.1", false),
            $("1.23.1", "2.0", "2.0", false)
                );
    }


    private Properties getTestProperties(String ... vals) {
        Properties ret = new Properties();
        for (int i = 0; i < vals.length; i+=2) {
            ret.setProperty(vals[i],vals[i+1]);
        }
        return ret;
    }

    private Object $(Object ... o) { return o; }
}
