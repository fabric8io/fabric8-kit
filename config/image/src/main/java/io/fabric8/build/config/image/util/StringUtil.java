package io.fabric8.build.config.image.util;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author roland
 * @since 31.05.17
 */
public class StringUtil {

    private static final Function<String, Iterable<String>> COMMA_SPLITTER = new Function<String, Iterable<String>>() {
        private Splitter COMMA_SPLIT = Splitter.on(",").trimResults().omitEmptyStrings();

        @Override
        public Iterable<String> apply(String input) {
            return COMMA_SPLIT.split(input);
        }
    };

    /**
     * Split each element of an Iterable<String> at commas.
     * @param input Iterable over strings.
     * @return An Iterable over string which breaks down each input element at comma boundaries
     */
    public static List<String> splitAtCommasAndTrim(Iterable<String> input) {
        if(input==null) {
            return Collections.emptyList();
        }
        Iterable<String> nonEmptyInputs = Iterables.filter(input, Predicates.notNull());
        return Lists.newArrayList(Iterables.concat(Iterables.transform(nonEmptyInputs, COMMA_SPLITTER)));
    }

    public static String[] splitOnSpaceWithEscape(String toSplit) {
        String[] split = toSplit.split("(?<!" + Pattern.quote("\\") + ")\\s+");
        String[] res = new String[split.length];
        for (int i = 0; i < split.length; i++) {
            res[i] = split[i].replaceAll("\\\\ "," ");
        }
        return res;
    }

        private static final Function<String, String[]> SPLIT_ON_LAST_COLON = new Function<String, String[]>() {
        @Override
        public String[] apply(String element) {
          int colon = element.lastIndexOf(':');
          if (colon < 0) {
              return new String[] {element, element};
          } else {
              return new String[] {element.substring(0, colon), element.substring(colon + 1)};
          }
        }
    };

    /**
     * Splits every element in the given list on the last colon in the name and returns a list with
     * two elements: The left part before the colon and the right part after the colon. If the string
     * doesn't contain a colon, the value is used for both elements in the returned arrays.
     *
     * @param listToSplit list of strings to split
     * @return return list of 2-element arrays or an empty list if the given list is empty or null
     */
    public static List<String[]> splitOnLastColon(List<String> listToSplit) {
        if (listToSplit != null) {
          return Lists.transform(listToSplit, SPLIT_ON_LAST_COLON);
        }
        return Collections.emptyList();
    }

    /**
     * Compare to version strings and return the larger version strings. This is used in calculating
     * the minimal required API version for this plugin. Version strings must be comparable as floating numbers.
     * The versions must be given in the format in a semantic version foramt (e.g. "1.23"
     *
     * If either version is <code>null</code>, the other version is returned (which can be null as well)
     *
     * @param versionA first version number
     * @param versionB second version number
     * @return the larger version number
     */
    public static String extractLargerVersion(String versionA, String versionB) {
        if (versionB == null || versionA == null) {
            return versionA == null ? versionB : versionA;
        } else {
            String partsA[] = versionA.split("\\.");
            String partsB[] = versionB.split("\\.");
            for (int i = 0; i < (partsA.length < partsB.length ? partsA.length : partsB.length); i++) {
                int pA = Integer.parseInt(partsA[i]);
                int pB = Integer.parseInt(partsB[i]);
                if (pA > pB) {
                    return versionA;
                } else if (pB > pA) {
                    return versionB;
                }
            }
            return partsA.length > partsB.length ? versionA : versionB;
        }
    }

    /**
     * Check whether the first given API version is larger or equals the second given version
     *
     * @param versionA first version to check against
     * @param versionB the second version
     * @return true if versionA is greater or equals versionB, false otherwise
     */
    public static boolean greaterOrEqualsVersion(String versionA, String versionB) {
        String largerVersion = extractLargerVersion(versionA, versionB);
        return largerVersion != null && largerVersion.equals(versionA);
    }

}
