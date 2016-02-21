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
import io.github.fmilitao.shopper.utils.ColorAdapter;
import io.github.fmilitao.shopper.utils.ListAnimations;
import io.github.fmilitao.shopper.utils.ShakeSensor;
import io.github.fmilitao.shopper.utils.TouchAndClickListener;
import io.github.fmilitao.shopper.utils.UtilColors;
import io.github.fmilitao.shopper.utils.UtilEditorActionListener;
import io.github.fmilitao.shopper.utils.UtilFragment;
import io.github.fmilitao.shopper.utils.UtilTextWatcher;
import io.github.fmilitao.shopper.utils.Utilities;


public class ItemsFragment extends UtilFragment implements ShakeSensor.ShakeListener,
        TouchAndClickListener.ClickListener, TouchAndClickListener.LongClickListener, TouchAndClickListener.SwipeOutListener {

    ListView mListView;
    ShakeSensor mShakeSensor;

    DatabaseMiddleman mDb;
    CursorAdapter mAdapter;

    long mShopId;
    String mShopName;
    Stack<Pair<String, Long>> undo;

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        item.setEnabled(false);

        if (id == R.id.add_item) {
            addItem(item); // 'item' must be re-enabled in function
            return true;
        }

        if (id == R.id.transfer_products) {
            transferItems(item); // 'item' must be re-enabled in function
            return true;
        }

        // all following should not need double-tap protection.
        item.setEnabled(true);

        if (id == R.id.load_items) {
            //
            // shows 'load' dialog
            //
            loadDialog(null);
            return true;
        }
        if (id == R.id.save_items) {
            //
            // shows 'save' dialog
            //
            saveDialog(mShopName);
            return true;
        }
        if (id == R.id.save_clipboard) {
            //
            // copies items to clipboard
            //
            String text = mDb.stringifyItemList(mShopId);
            Utilities.setClipboardString(getActivity(), mShopName, text);
            popUp(format(R.string.ITEMS_COPIED, mShopName));
            return true;
        }
        if (id == R.id.load_clipboard) {
            //
            // pastes items from clipboard
            //
            final List<Utilities.Triple<String, Float, String>> tmp = Utilities.parseProductList(Utilities.getClipboardString(getActivity()));
            if (tmp != null && !tmp.isEmpty()) {

                animateAdd(new ListAnimations.Runner() {
                    @Override
                    public void run(Set<Long> set) {
                        mDb.loadShopItems(tmp, mShopId, set);
                        updateListDependencies();
                        popUp(format(R.string.ITEMS_PASTED, tmp.size()));
                    }
                });

            }
            return true;
        }
        if (id == R.id.undo_delete_items ){
            // fake shake action to undo deletion
            onShake();
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
        if (undo == null) {
            mDb.gcItems();
            undo = new Stack<>();
        }

        TouchAndClickListener t = new TouchAndClickListener(ViewConfiguration.get(getContext()), mListView);
        //t.setOnClick(this);
        t.setOnLongClick(this);
        t.setOnSwipeOut(this);

        mAdapter = new ItemsAdapter(getActivity(), mDb.fetchShopItems(mShopId), 0, t);
        mListView.setAdapter(mAdapter);

        updateActivityTitle();
        return rootView;
    }

    private void updateActivityTitle(){
        Cursor c = mDb.fetchShopDetails(mShopId);
        final int notDoneItems = c.getInt(DBContract.SelectShopItemsQuantitiesQuery.INDEX_NOT_DONE);
        c.close();

        getActivity().setTitle("(" + notDoneItems + ") " + mShopName);
    }

    private void updateListDependencies(){
        updateActivityTitle();

        mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
        mAdapter.notifyDataSetChanged();
    }

    private void animateDelete(Runnable andThen, long... deletes) {
        ListAnimations.animateDelete(mAdapter, mListView, andThen, deletes);
    }

    @Override
    public void onShake() {
        //
        // undoes deleted item
        //
        if (undo.isEmpty()) {
            popUp(getString(R.string.SHAKE_FAIL));
            return;
        }

        final Pair<String, Long> u = undo.pop();
        final long itemId = u.second;
        final String itemName = u.first;

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                if (mDb.updateItemDeleted(itemId, false)) {
                    set.add(itemId);
                    updateListDependencies();
                }
            }
        });

        popUp(format(R.string.SHAKE_UNDO, itemName));
    }

    @Override
    public void onClick(ListView listView, View view) {
        //
        // flips done marker on item
        //
        final int position = listView.getPositionForView(view);
        final Cursor c = (Cursor) listView.getItemAtPosition(position);
        final long itemId = c.getLong(DBContract.SelectShopItemsQuery.INDEX_ID);
        final int itemDone = c.getInt(DBContract.SelectShopItemsQuery.INDEX_IS_DONE);

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                mDb.flipItem(itemId, itemDone);
                updateListDependencies();
            }
        });
    }

    @Override
    public void onSwipeOut(ListView listView, View view, TouchAndClickListener.Direction direction) {

        switch (direction) {
            case LEFT:
                //
                // deletes item from list
                //
                final int position = listView.getPositionForView(view);
                final Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                final long itemId = cursor.getLong(DBContract.SelectShopItemsQuery.INDEX_ID);
                final String itemName = cursor.getString(DBContract.SelectShopItemsQuery.INDEX_NAME);

                animateAdd(new ListAnimations.Runner() {
                    @Override
                    public void run(Set<Long> set) {
                        if (mDb.updateItemDeleted(itemId, true)) {
                            undo.push(new Pair<>(itemName, itemId));
                            updateListDependencies();
                        }
                    }
                });
                return;
            case RIGHT:
                onClick(listView, view);
                return;
            default:
                // does nothing
        }
    }

    private void animateAdd(ListAnimations.Runner action) {
        // FIXME animation is now inconsistent with swipe out movement
        // FIXME display some feedback on what the action will do (delete/mark as done).
        ListAnimations.animateAdd(mAdapter, mListView, action);
    }

    @Override
    public void onLongClick(ListView listView, View view) {
        //
        // edits item on list
        //
        final int position = listView.getPositionForView(view);
        final Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        final long itemId = cursor.getLong(DBContract.SelectShopItemsQuery.INDEX_ID);
        final String itemName = cursor.getString(DBContract.SelectShopItemsQuery.INDEX_NAME);
        final float itemQuantity = cursor.getFloat(DBContract.SelectShopItemsQuery.INDEX_QUANTITY);
        final String itemQuantityStr = cursor.getString(DBContract.SelectShopItemsQuery.INDEX_QUANTITY);
        final String itemUnit = cursor.getString(DBContract.SelectShopItemsQuery.INDEX_UNIT);
        final String itemCategory = cursor.getString(DBContract.SelectShopItemsQuery.INDEX_CATEGORY);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.item_create_dialog, null);

        final EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
        final EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

        final AutoCompleteTextView u = (AutoCompleteTextView) root.findViewById(R.id.dialog_product_unit);
        u.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, mDb.getAllUnits()));

        final AutoCompleteTextView c = (AutoCompleteTextView) root.findViewById(R.id.dialog_product_category);
        c.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, mDb.getAllCategories()));

        final Spinner spinner = (Spinner) root.findViewById(R.id.dialog_product_color);
        setupColorSpinner(spinner, c);

        // initial values, MUST be after adding listeners to trigger changed event
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
                final int pos = spinner.getSelectedItemPosition();

                updateCategoryColors(pos, p_cat);

                // checks if valid update: different name, quantity, unit, or category
                if (p_name.length() > 0 && (!p_name.equals(itemName) || p_quantity != itemQuantity
                        || !p_unit.equals(itemUnit) || !p_cat.equals(itemCategory))) {

                    animateAdd(new ListAnimations.Runner() {
                        @Override
                        public void run(Set<Long> set) {
                            mDb.updateItem(itemId, p_name, p_quantity, p_unit, p_cat);
                            updateListDependencies();
                        }
                    });

                    popUp(format(R.string.ITEM_UPDATED, p_name, Float.toString(p_quantity)));
                }
            }
        });

        builder.setNegativeButton(R.string.CANCEL, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }


    private void addItem(final MenuItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.item_create_dialog, null);

        final EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
        final EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

        final AutoCompleteTextView u = (AutoCompleteTextView) root.findViewById(R.id.dialog_product_unit);
        final ArrayAdapter adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, mDb.getAllUnits());
        u.setAdapter(adapter);

        final AutoCompleteTextView c = (AutoCompleteTextView) root.findViewById(R.id.dialog_product_category);
        final String[] array = mDb.getAllCategories();
        final ArrayAdapter a = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, array);
        c.setAdapter(a);

        // Color spinner
        final Spinner spinner = (Spinner) root.findViewById(R.id.dialog_product_color);
        setupColorSpinner(spinner, c);

        builder.setTitle(R.string.NEW_ITEM);
        builder.setView(root);

        // Same code for Positive and Neutral buttons.
        final DialogInterface.OnClickListener aux = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String p_name = n.getText().toString().trim();
                final String p_unit = u.getText().toString().trim();
                final String p_cat = c.getText().toString().trim();
                final int pos = spinner.getSelectedItemPosition();

                updateCategoryColors(pos, p_cat);

                // check if valid add (i.e. just some non-empty name)
                if (p_name.length() > 0) {

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
                            long newItem = mDb.createItem(p_name, mShopId, p_quantity, false, p_unit, p_cat);
                            if (newItem > 0) {
                                set.add(newItem);
                                updateListDependencies();
                            }
                        }
                    });

                    popUp(format(R.string.ITEM_ADDED, p_name, Float.toString(p_quantity)));
                }
            }
        };

        builder.setPositiveButton(R.string.CREATE, aux);

        builder.setNeutralButton(R.string.NEXT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aux.onClick(dialog, which);
                addItem(item); //recur
            }
        });

        // nothing to do
        builder.setNegativeButton(R.string.CANCEL, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new EnableOnShow(item));

        n.addTextChangedListener(new UtilTextWatcher(dialog, true)); // has neutral too
        n.setOnEditorActionListener(new UtilEditorActionListener(dialog));

        dialog.show();
        n.setText(""); // default value used to trigger listeners and show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void setupColorSpinner(final Spinner spinner, final AutoCompleteTextView c) {
        final ColorAdapter colorAdapter = new ColorAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(colorAdapter);

        c.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // intentionally empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
                // updates spinner when TextView is updated
                //
                String cat = c.getText().toString();
                Integer color = UtilColors.colorMap.get(cat);
                if (color != null) {
                    int position = UtilColors.getColorPosition(color);
                    if (position != -1) {
                        spinner.setSelection(position, true);
                        return;
                    }
                }
                // something failed, use default
                spinner.setSelection(0, true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // intentionally empty
            }
        });
    }

    private void updateCategoryColors(int pos, String category) {
        if (pos == 0) {
            // no color for category
            UtilColors.colorMap.remove(category);
        } else {
            // updates if already exists
            UtilColors.colorMap.put(category, UtilColors.getColorAt(pos));
        }
    }

    private void transferItems(final MenuItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.item_move_dialog, null);

        // aux
        final Pair<Long, String>[] shopArray = mDb.makeAllShopPair();
        String[] shops = new String[shopArray.length];
        int i = 0;
        int pos = -1;
        for (Pair<Long, String> p : shopArray) {
            if (p.first == mShopId)
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
        final ItemsMoveAdapter moveAdapter = new ItemsMoveAdapter(getActivity(), mDb.fetchShopItems(mShopId), 0);
        final ListView listView = (ListView) root.findViewById(R.id.product_list);
        listView.setAdapter(moveAdapter);
        // LIST

        builder.setTitle(R.string.TRANSFER_TITLE);
        builder.setView(root);

        builder.setPositiveButton(R.string.TRANSFER, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Long[] set = moveAdapter.getSelectedItemIds();
                final int i = spinner.getSelectedItemPosition();

                int count = 0;
                for (Long b : set) {
                    count += b != null ? 1 : 0;
                }

                if (i == position || count == 0) {
                    popUp(getString(R.string.TRANSFER_FAIL));
                } else {
                    final long toShopId = shopArray[i].first;
                    final String toShopName = shopArray[i].second;
                    final String fromShopName = shopArray[position].second;

                    // pick elements for transfer
                    final long[] transfers = new long[count];
                    for (int x = 0, y = 0; x < set.length; ++x) {
                        if (set[x] != null) {
                            transfers[y++] = set[x];
                        }
                    }

                    animateDelete(new Runnable() {
                        @Override
                        public void run() {
                            for (long id : transfers) {
                                mDb.updateItemShopId(id, toShopId);
                            }

                            // no need to animate since order did not change
                            updateListDependencies();
                        }
                    }, transfers);

                    popUp(format(R.string.ITEM_TRANSFERRED, count, fromShopName, toShopName));
                }
            }
        });

        // nothing to do on cancel
        builder.setNegativeButton(R.string.CANCEL, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new EnableOnShow(item));
        dialog.show();
    }

    //
    // List I/O
    //

    protected File getSaveFile(String name){
        if (!name.endsWith(".txt")) {
            name = name + ".txt";
        }

        if (!name.startsWith("/")) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            return new File(path, name);
        } else {
            // note that when given 'any' file name writing may fail due to filesystem permissions
            return new File(name);
        }
    }

    protected void save(String name) {
        // either saves given file to downloads directory, or attempts given absolute path
        try {
//            if (!name.endsWith(".txt")) {
//                name = name + ".txt";
//            }

            Context c = !name.startsWith("/") ? getActivity() : null;
            File file = getSaveFile(name);

//            if (!name.startsWith("/")) {
//                c = getActivity();
//                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                file = new File(path, name);
//            } else {
//                // note that when given 'any' file name writing may fail due to filesystem permissions
//                file = new File(name);
//            }

            Log.w(">>", file.getAbsolutePath());

            PrintWriter pw = new PrintWriter(new FileOutputStream(file));
            mDb.saveShopItems(pw, mShopId);
            pw.close();

            if (c != null) {
                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(c, new String[]{file.toString()}, null, null);
            }

            popUp(format(R.string.SAVED_FILE, file.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
            popUp(format(R.string.ERROR_FILE, e));
        }
    }


    protected File getLoadFile(String file){
        File tmp = new File(file);
        if (!tmp.exists()) {
            // attempts download directory
            tmp = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file);
        }
        return tmp;
    }

    protected void load(final String file) {
        try {
            final File tmp = getLoadFile(file);
            final Scanner sc = new Scanner(tmp);

            animateAdd(new ListAnimations.Runner() {
                @Override
                public void run(Set<Long> set) {
                    mDb.loadShopItems(sc, mShopId, set);
                    sc.close();

                    updateListDependencies();
                    popUp(format(R.string.LOADED_FILE, tmp.getAbsolutePath()));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            popUp(format(R.string.ERROR_FILE, e));
        }
    }
}
