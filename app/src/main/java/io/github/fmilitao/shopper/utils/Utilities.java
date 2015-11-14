package io.github.fmilitao.shopper.utils;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    static public final class Triple<A,B,C>{
        final public A first;
        final public B second;
        final public C third;

        public Triple(A a, B b, C c){
            first = a;
            second = b;
            third = c;
        }
    }

    // group numbers for pattern: "(1)(2(3))(4)"
    static final Pattern PATTERN = Pattern.compile("(\\D+)(\\d+(\\.\\d+)?)(.*)");
    // indexes for 'name', 'quantity', 'unit'
    static final int[] INDEX = {1,2,4};

    //
    // Import from clipboard
    //

    static public List<Triple<String,Float,String>> parseProductList(String txt){
        List<Triple<String,Float,String>> list = new LinkedList<>();
        if( txt == null )
            return list;

        for(String s : txt.split("\n")){
            String name = s.trim();
            float quantity = 1;
            String unit = null;

            // ignores empty lines/strings, doesn't have a name
            if(name.length() <= 0)
                continue;

            Matcher m = PATTERN.matcher(name);

            // if successful match
            if( m.find() ) {

                if( m.groupCount() >= INDEX[0] ) {
                    name = m.group(INDEX[0]).trim();
                }

                // does it have a quantity
                if (m.groupCount() >= INDEX[1]) {
                    try {
                        quantity = Float.parseFloat(m.group(INDEX[1]).trim());
                    } catch (NumberFormatException e) {
                        // continues
                    }
                }

                    // does it have a unit?
                if (m.groupCount() >= INDEX[2]) {
                    unit = m.group(INDEX[2]).trim();
                    if( unit.length() == 0)
                        unit = null;
                }

                android.util.Log.v("PARSER ORIGINAL:", s);
                android.util.Log.v("PARSER RESULT:", name + "|" + quantity + "|" + unit);

                list.add(new Triple<>(name, quantity,unit));
            }else {
                android.util.Log.v("PARSER NO RESULT:", s);
            }
        }

        return list;
    }

    static public String getClipboardString(Activity activity){
        ClipboardManager clipboard=(ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if( clipboard == null )
            return null;

        ClipData text = clipboard.getPrimaryClip();
        if( text == null )
            return null;

        return text.getItemAt(0).coerceToText(activity).toString();
    }

    static public boolean setClipboardString(Activity activity, String label, String text){
        ClipboardManager clipboard=(ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if( clipboard == null )
            return false;

        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        return true;
    }

}
