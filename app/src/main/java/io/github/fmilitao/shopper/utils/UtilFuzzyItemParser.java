package io.github.fmilitao.shopper.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.fmilitao.shopper.utils.model.Item;

/**
 * Used to parse fuzzy clipboard content.
 */
// TODO wite tests and add option to importer.
public class UtilFuzzyItemParser {

    // group numbers for pattern: "(1)(2(3))(4)"
    private static final Pattern PATTERN = Pattern.compile("(\\D+)(\\d+(\\.\\d+)?)(.*)");
    // indexes for 'name', 'quantity', 'unit'
    private static final int[] INDEX = {1, 2, 4};
    // FIXME: this is ready to include additional PATTERNs
    // FIXME: also include categories here

    //
    // Import from clipboard
    //

    public static List<Item> parseProductList(String txt) {
        List<Item> list = new LinkedList<>();
        if (txt == null)
            return list;

        for (String s : txt.split("\n")) {
            String name = s.trim();
            double quantity = 1;
            String unit = null;

            // ignores empty lines/strings, doesn't have a name
            if (name.length() <= 0)
                continue;

            Matcher m = PATTERN.matcher(name);

            // if successful match
            if (m.find()) {

                if (m.groupCount() >= INDEX[0]) {
                    name = m.group(INDEX[0]).trim();
                }

                // does it have a quantity
                if (m.groupCount() >= INDEX[1]) {
                    try {
                        quantity = Double.parseDouble(m.group(INDEX[1]).trim());
                    } catch (NumberFormatException e) {
                        // continues
                    }
                }

                // does it have a unit?
                if (m.groupCount() >= INDEX[2]) {
                    unit = m.group(INDEX[2]).trim();
                    if (unit.length() == 0)
                        unit = null;
                }

                android.util.Log.v("PARSER ORIGINAL:", s);
                android.util.Log.v("PARSER RESULT:", name + "|" + quantity + "|" + unit);

                list.add(new Item(-1, name, unit, quantity, null, false));
            } else {
                android.util.Log.v("PARSER NO RESULT:", s);
            }
        }

        return list;
    }
}
