package pt.blah.shopper;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    static public void init(Activity main){
        Utilities.file = new File(main.getFilesDir(), Utilities.FILENAME);
        Utilities.context = main.getApplicationContext();

        Utilities.load();
    }

    static final String LOG_TG = Utilities.class.toString();
    static final String INTENT_TAG = "POS_TAG";
    static final String FILENAME = "SHOPPER.DAT";

    //
    // Import from clipboard
    //

    static public List<DataDB.Product> parseProductList(String txt){
        List<DataDB.Product> list = new LinkedList<>();
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
            list.add(sData.newProduct(left, quantity));
        }

        return list;
    }

    static public String stringifyProductList(Iterable<DataDB.Product> products){
        StringBuilder builder = new StringBuilder();
        for(DataDB.Product p : products ){
            builder.append(p.getName());
            builder.append(" ");
            builder.append(p.getQuantity());
            builder.append("\n");
        }
        return builder.toString();
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

    //
    // File stuff
    //

    static File file;

    static void save(){

        if( version <= last_saved ){
            Log.v(LOG_TG,"already saved.");
            return;
        }

        Log.v(LOG_TG,file.getAbsolutePath());
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            sData.save(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(LOG_TG,"file saved.");

        last_saved = version;
    }

    static void load(){

        if( !file.exists() ) {
            Log.v(LOG_TG,"file does not exist");
            return;
        }

        try {
            ObjectInputStream o = new ObjectInputStream(new FileInputStream(file));
            sData.load(o);
            o.close();
            Log.v(LOG_TG, "file loaded");
        } catch (Exception e) {
            e.printStackTrace();
            // file will be overwritten anyway, even if with empty content
            Log.v(LOG_TG, "fail to load.");
        }

    }

    //
    // listeners stuff
    //

    static final List<Pair<Object,BaseAdapter>> listeners = new LinkedList<>();

    static void notifyListeners(){
        ++version;
        for(Pair<Object,BaseAdapter> p : listeners)
            p.second.notifyDataSetChanged();
    }

    static void removeListener(Object a){
        for(int i=0;i<listeners.size();++i){
            Pair<Object,BaseAdapter> pair = listeners.get(i);
            if( pair.first == a ){
                listeners.remove(i);
                break;
            }
        }
    }

    static void addListener(Object a, BaseAdapter b){
        for(int i=0;i<listeners.size();++i){
            Pair<Object,BaseAdapter> pair = listeners.get(i);
            if( pair.first == a ){
                listeners.remove(i);
                break;
            }
        }
        listeners.add( new Pair<>(a,b));
    }

    static final DataDB sData = new DataDB();
    static int version = 0, last_saved = 0;

    //
    // format
    //

    static Context context;

    static String format(int id, Object... args){
        return String.format(context.getResources().getString(id),args);
    }


    static void popUp(Activity activity,String notification) {
        Toast t = Toast.makeText(activity.getApplicationContext(),
                notification,
                Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP,0,0);
        t.show();
    }
}
