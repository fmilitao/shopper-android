package pt.blah.shopper.utils;


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

    //
    // Import from clipboard
    //

    static public List<Pair<String,Integer>> parseProductList(String txt){
        List<Pair<String,Integer>> list = new LinkedList<>();
        if( txt == null )
            return list;

        for(String s : txt.split("\n")){
            String name = s.trim();
            int quantity = 1;

            // ignores empty lines/strings
            if(name.length() <= 0)
                continue;

            // we must cycle through all names until list, since some could be invalid
            // although the pattern should ensure that never occurs...
            String left = name;
            for(String d : name.split("\\D+")){
                if( d.length() <= 0 )
                    continue;
                try {
                    // cleans string
                    d = d.trim();
                    int tmp = Integer.parseInt(d);
                    // ensures positive number
                    quantity = Math.abs(tmp);

                    // attempts to remove number from string
                    int last = name.lastIndexOf(d);
                    String removed = name.substring(last,name.length());
                    // if removed string contains words, better not remove it!
                    Matcher matcher = Pattern.compile("[a-zA-Z]").matcher(removed);
                    //Log.v("REMOVE", "\'"+removed + "\'" + matcher.find());

                    if( !matcher.find() ){
                        left = name.substring(0,last);
                    }

                }catch(NumberFormatException e){
                    // continues to next one
                }
            }

            //Log.v("PARSER RESULT:", name+" :: "+left+" || "+quantity);
            list.add(new Pair<>(left, quantity));
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
