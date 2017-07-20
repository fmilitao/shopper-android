package io.github.fmilitao.shopper.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;

import java.util.Map;
import java.util.TreeMap;

import io.github.fmilitao.shopper.R;

//
// Category-Color Cache
//

public class UtilColors {

    // overrides will update shared preferences accordingly
    static public final Map<String, Integer> colorMap = new TreeMap<String, Integer>() {

        @Override
        public Integer remove(Object key) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove((String) key);
            editor.apply();
            return super.remove(key);
        }

        @Override
        public Integer put(String key, Integer value) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(key, value);
            editor.apply();
            return super.put(key, value);
        }

        @Override
        public Integer get(Object key) {
            String str = (String) key;
            Integer res = super.get(str);
            if (res == null && sharedPref.contains(str)) {
                res = sharedPref.getInt(str, -1);
                super.put(str, res);
            }
            return res;
        }

    };

    static String[] colorNames;
    static int[] colorValues;

    static SharedPreferences sharedPref;

    static public int getColorPosition(int color) {
        if (colorValues == null)
            return -1;
        for (int i = 1; i < colorValues.length; ++i) {
            if (colorValues[i] == color)
                return i;
        }
        return -1;
    }

    static public int getColorAt(int position) {
        if (colorValues == null)
            return -1;
        return colorValues[position];
    }

    //
    // initializes all static variables
    //

    static public String[] init(Activity activity) {
        // loads all color names and color values
        if (colorNames == null) {
            // initializes colorNames including default value
            // initializes colorValues, position 0 is 0 for no special reason.
            final TypedArray ta = activity.getResources().obtainTypedArray(R.array.colors);
            final int length = ta.length();
            colorValues = new int[length + 1];
            colorValues[0] = 0;
            colorNames = new String[length + 1];
            colorNames[0] = activity.getResources().getString(R.string.SHOW_TEXT);
            for (int i = 0; i < length; ++i) {
                int id = ta.getResourceId(i, 0);
                colorValues[i + 1] = ContextCompat.getColor(activity, id);
                colorNames[i + 1] = "";
            }
            ta.recycle();
        }

        if (sharedPref == null) {
            sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        }
        return colorNames;
    }
}
