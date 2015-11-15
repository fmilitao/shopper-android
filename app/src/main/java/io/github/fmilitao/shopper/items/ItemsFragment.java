package io.github.fmilitao.shopper.items;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import io.github.fmilitao.shopper.R;
import io.github.fmilitao.shopper.sql.DBContract;
import io.github.fmilitao.shopper.sql.DatabaseMiddleman;
import io.github.fmilitao.shopper.utils.ListAnimations;
import io.github.fmilitao.shopper.utils.ShakeSensor;
import io.github.fmilitao.shopper.utils.TouchAndClickListener;
import io.github.fmilitao.shopper.utils.UtilFragment;
import io.github.fmilitao.shopper.utils.Utilities;


public class ItemsFragment extends UtilFragment implements ShakeSensor.ShakeListener,
        TouchAndClickListener.ClickListener, TouchAndClickListener.LongClickListener, TouchAndClickListener.SwipeOutListener {

    ListView mListView;
    ShakeSensor mShakeSensor;

    DatabaseMiddleman mDb;
    CursorAdapter mAdapter;

    long mShopId;
    String mShopName;
    Stack<Pair<String,Long>> undo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        undo = null;
        mShakeSensor = new ShakeSensor(this);
        mShakeSensor.onCreate(getActivity());

        mDb = new DatabaseMiddleman(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        mDb.open();
        // assumes items never changed while we paused.

        mShakeSensor.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mDb.close();
        mShakeSensor.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.items_menu, menu);
    }

    protected String[] getAutoCompleteUnit(){
        return mDb.getAllUnits();
    }

    protected String[] getAutoCompleteCategory(){
        return mDb.getAllCategories();
    }

    protected void addItem(final MenuItem item){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.item_create_dialog, null);

        final AutoCompleteTextView u = (AutoCompleteTextView) root.findViewById(R.id.dialog_product_unit);
        final ArrayAdapter adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_dropdown_item_1line, getAutoCompleteUnit());
        u.setAdapter(adapter);

        //
        // FIXME: testing
        //

        final AutoCompleteTextView c = (AutoCompleteTextView) root.findViewById(R.id.dialog_product_category);
        final String[] array = getAutoCompleteCategory();
        final ArrayAdapter a = new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line, array){
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                View s = super.getView(position, convertView, parent);
//                s.setBackgroundColor(Utilities.color(getContext(),array[position]));
//                return s;
//            }
        };
        c.setAdapter(a);
//        c.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // intentionally empty
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // intentionally empty
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                c.setBackgroundColor(Utilities.color(getContext(),u.getText().toString()));
//            }
//        });

        //
        // ========
        //

        builder.setTitle(R.string.NEW_ITEM);
        builder.setView(root);

        // Same code for Positive and Neutral buttons.
        final DialogInterface.OnClickListener aux = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
                EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

                final String p_name = n.getText().toString().trim();
                final String p_unit = u.getText().toString().trim();
                final String p_cat = c.getText().toString().trim();

                // something to add
                if( p_name.length() > 0 ) {

                    float qt = 1; // default quantity is '1'
                    try {
                        qt = Float.parseFloat(q.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        // ignores error
                    }

                    final float p_quantity = qt;

                    animateAdd(new ListAnimations.Runner() {
                        @Override
                        public void run(Set<Long> set) {
                            long newItem = mDb.createItem(p_name, mShopId, p_quantity, false, p_unit,p_cat);
                            if (newItem > 0) {
                                set.add(newItem);
                                mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });

                    popUp(format(R.string.ITEM_ADDED, p_name, Float.toString(p_quantity)));
                }

                // ok to re-enable on first create because item is not visible anyway
                item.setEnabled(true);
            }
        };

        builder.setPositiveButton(R.string.CREATE, aux);

        builder.setNeutralButton(R.string.NEXT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aux.onClick(dialog, which);
                addItem(item);
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                item.setEnabled(true);
                // aborted, nothing to do
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        item.setEnabled(false);

        if (id == R.id.add_item) {
            addItem(item); // item must be re-enables in function
            return true;
        }

        if( id == R.id.transfer_products ){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            @SuppressLint("InflateParams")
            final View root = inflater.inflate(R.layout.item_move_dialog, null);

            // aux
            final Pair<Long,String>[] shopArray = mDb.makeAllShopPair();
            String[] shops = new String[shopArray.length];
            int i =0;
            int pos = -1;
            for(Pair<Long,String> p : shopArray){
                if( p.first == mShopId )
                    pos = i;
                shops[i] = p.second;
                ++i;
            }

            final int position = pos;

            // SPINNER
            final Spinner spinner = (Spinner) root.findViewById(R.id.shop_pick);

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_move_spinner, shops);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(pos);
            // SPINNER

            // LIST
            final ItemsMoveAdapter moveAdapter = new ItemsMoveAdapter(getActivity(),mDb.fetchShopItems(mShopId),0);
            final ListView listView = (ListView) root.findViewById(R.id.product_list);
            listView.setAdapter(moveAdapter);
            // LIST

            builder.setTitle(R.string.TRANSFER_TITLE);
            builder.setView(root);

            builder.setPositiveButton(R.string.TRANSFER, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    item.setEnabled(true);
                    // ...
                    final Long[] set = moveAdapter.getSelectedItemIds();
                    final int i = spinner.getSelectedItemPosition();

                    int count = 0;
                    for(Long b : set ){
                        count += b != null ? 1 : 0;
                    }

                    if( i == position || count == 0 ) {
                        popUp(getString(R.string.TRANSFER_FAIL));
                    } else {
                        final long toShopId = shopArray[i].first;
                        final String toShopName = shopArray[i].second;
                        final String fromShopName = shopArray[position].second;

                        // pick elements for transfer
                        final long[] transfers = new long[count];
                        for(int x=0,y=0; x < set.length; ++x ){
                            if( set[x] != null ){
                                transfers[y++] = set[x];
                            }
                        }

                        animateDelete(new Runnable() {
                            @Override
                            public void run() {
                                for(long id : transfers) {
                                    mDb.updateItemShopId(id,toShopId);
                                }

                                // no need to animate since order did not change
                                mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                                mAdapter.notifyDataSetChanged();
                            }
                        }, transfers);

                        popUp(format(R.string.ITEM_TRANSFERRED, count, fromShopName, toShopName));
                    }
                }
            });

            builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    item.setEnabled(true);
                    // aborted, nothing to do
                }
            });

            builder.create().show();

            return true;
        }

        // all following should not need double-tap protection.
        item.setEnabled(true);

        if (id == R.id.load_items){
            loadDialog(null);
            return true;
        }
        if (id == R.id.save_items){
            saveDialog(mShopName);
            return true;
        }
        if( id == R.id.save_clipboard){
            String text = mDb.stringifyItemList(mShopId);
            Utilities.setClipboardString(getActivity(), mShopName, text);
            popUp(format(R.string.ITEMS_COPIED, mShopName));
            return true;
        }
        if( id == R.id.load_clipboard ){
            final List<Utilities.Triple<String,Float,String>> tmp = Utilities.parseProductList(Utilities.getClipboardString(getActivity()));
            if( tmp != null && !tmp.isEmpty()){

                animateAdd(new ListAnimations.Runner() {
                    @Override
                    public void run(Set<Long> set) {
                        mDb.loadShopItems(tmp, mShopId, set);
                        mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                        mAdapter.notifyDataSetChanged();
                        popUp(format(R.string.ITEMS_PASTED, tmp.size()) );
                    }
                });

            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        // intentionally let app crash if intent not provided (fatal error anyway)
        mShopName = intent.getStringExtra(ItemsActivity.INTENT_SHOP_NAME_STRING);
        mShopId = intent.getLongExtra(ItemsActivity.INTENT_SHOP_ID_LONG, 0);


        View rootView = inflater.inflate(R.layout.item_list_fragment, container, false);
        mListView = (ListView) rootView.findViewById(R.id.product_list);

        mDb.open();
        if( undo == null ) {
            mDb.gcItems();
            undo = new Stack<>();
        }

        TouchAndClickListener t = new TouchAndClickListener(ViewConfiguration.get(getContext()),mListView);
        t.setOnClick(this);
        t.setOnLongClick(this);
        t.setOnSwipeOut(this);

        mAdapter = new ItemsAdapter(getActivity(),mDb.fetchShopItems(mShopId), 0, t);
        mListView.setAdapter(mAdapter);

        getActivity().setTitle(mShopName);
        return rootView;
    }

    private void animateDelete(Runnable andThen, long... deletes){
        ListAnimations.animateDelete(mAdapter, mListView, andThen, deletes);
    }

    @Override
    public void onShake() {
        if (undo.isEmpty()) {
            popUp(getString(R.string.SHAKE_FAIL));
            return;
        }

        final Pair<String,Long> u = undo.pop();
        final long itemId = u.second;
        final String itemName = u.first;

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                if (mDb.updateItemDeleted(itemId, false)) {
                    set.add(itemId);
                    mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        popUp(format(R.string.SHAKE_UNDO, itemName));
    }

    @Override
    public void onClick(ListView listView, View view) {
        int position = listView.getPositionForView(view);
        Cursor c = (Cursor) listView.getItemAtPosition(position);
        final long itemId = c.getLong(DBContract.SelectItemQuery.INDEX_ID);
        final int itemDone = c.getInt(DBContract.SelectItemQuery.INDEX_IS_DONE);

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                mDb.flipItem(itemId, itemDone);
                mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onLongClick(ListView listView, View view) {
        final int position = listView.getPositionForView(view);
        final Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        final long itemId = cursor.getLong(DBContract.SelectItemQuery.INDEX_ID);
        final String itemName = cursor.getString(DBContract.SelectItemQuery.INDEX_NAME);
        final float itemQuantity = cursor.getFloat(DBContract.SelectItemQuery.INDEX_QUANTITY);
        final String itemQuantityStr = cursor.getString(DBContract.SelectItemQuery.INDEX_QUANTITY);
        final String itemUnit = cursor.getString(DBContract.SelectItemQuery.INDEX_UNIT);
        final String itemCategory = cursor.getString(DBContract.SelectItemQuery.INDEX_CATEGORY);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.item_create_dialog, null);

        final EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
        final EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

        final AutoCompleteTextView u = (AutoCompleteTextView) root.findViewById(R.id.dialog_product_unit);
        u.setAdapter(new ArrayAdapter<>(getContext(),android.R.layout.simple_dropdown_item_1line, getAutoCompleteUnit()));

        final AutoCompleteTextView c = (AutoCompleteTextView) root.findViewById(R.id.dialog_product_category);
        c.setAdapter(new ArrayAdapter<>(getContext(),android.R.layout.simple_dropdown_item_1line, getAutoCompleteCategory()));

        n.setText(itemName);
        n.setSelection(n.getText().length());
        q.setText(itemQuantityStr);
        u.setText(itemUnit);
        c.setText(itemCategory);

        builder.setTitle(R.string.UPDATE);
        builder.setView(root);
        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String p_name = n.getText().toString().trim();
                final float p_quantity = Float.parseFloat(q.getText().toString().trim());
                final String p_unit = u.getText().toString().trim();
                final String p_cat = c.getText().toString().trim();

                if (p_name.length() > 0 && (!p_name.equals(itemName) || p_quantity != itemQuantity
                        || !p_unit.equals(itemUnit) || !p_cat.equals(itemCategory) )) {

                    animateAdd(new ListAnimations.Runner() {
                        @Override
                        public void run(Set<Long> set) {
                            mDb.updateItem(itemId,p_name,p_quantity,p_unit,p_cat);
                            mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                    popUp(format(R.string.ITEM_UPDATED, p_name, Float.toString(p_quantity)));
                }
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // does nothing
            }
        });
        builder.create().show();
    }

    @Override
    public void onSwipeOut(ListView listView, View view) {
        final int position = listView.getPositionForView(view);
        final Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        final long itemId = cursor.getLong(DBContract.SelectItemQuery.INDEX_ID);
        final String itemName = cursor.getString(DBContract.SelectItemQuery.INDEX_NAME);

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                if( mDb.updateItemDeleted(itemId, true) ) {
                    undo.push(new Pair<>(itemName,itemId));
                    mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void animateAdd(ListAnimations.Runner action){
        ListAnimations.animateAdd(mAdapter, mListView, action);
    }

    //
    // List I/O
    //

    protected void save(String name) {
        // either saves given file to downloads directory, or attempts given absolute path
        try {
            if( !name.endsWith(".txt") ) {
                name = name + ".txt";
            }

            Context c = null;
            File file;

            if( !name.startsWith("/")){
                c = getActivity();
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                file = new File(path, name);
            }else{
                // note that when given 'any' file name writing may fail due to filesystem permissions
                file = new File(name);
            }

            Log.w(">>", file.getAbsolutePath());

            PrintWriter pw = new PrintWriter(new FileOutputStream(file));
            mDb.saveShopItems(pw, mShopId);
            pw.close();

            if( c != null ) {
                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(c, new String[]{file.toString()}, null, null);
            }

            popUp(format(R.string.SAVED_FILE, file.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
            popUp(format(R.string.ERROR_FILE,e));
        }
    }


    protected void load(final String file) {
        try {
            File tmp = new File(file);
            if( !tmp.exists() ){
                // attempt download directory
                tmp = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file);
            }

            final Scanner sc = new Scanner(tmp);
            final File finalTmp = tmp;

            animateAdd(new ListAnimations.Runner() {
                @Override
                public void run(Set<Long> set) {
                    mDb.loadShopItems(sc, mShopId, set);
                    sc.close();

                    mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                    mAdapter.notifyDataSetChanged();
                    popUp(format(R.string.LOADED_FILE,finalTmp.getAbsolutePath()));
                }
            });

        } catch(Exception e){
            e.printStackTrace();
            popUp(format(R.string.ERROR_FILE,e));
        }
    }
}
