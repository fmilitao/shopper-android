package io.github.fmilitao.shopper.shops;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import io.github.fmilitao.shopper.R;
import io.github.fmilitao.shopper.items.ItemsActivity;
import io.github.fmilitao.shopper.sql.DatabaseMiddleman;
import io.github.fmilitao.shopper.utils.ListAnimations;
import io.github.fmilitao.shopper.utils.ShakeSensor;
import io.github.fmilitao.shopper.utils.TouchAndClickListener;
import io.github.fmilitao.shopper.utils.UtilEditorActionListener;
import io.github.fmilitao.shopper.utils.UtilFragment;
import io.github.fmilitao.shopper.utils.UtilTextWatcher;
import io.github.fmilitao.shopper.utils.Utilities;

import static io.github.fmilitao.shopper.sql.DBContract.JoinShopItemQuery.INDEX_ID;
import static io.github.fmilitao.shopper.sql.DBContract.JoinShopItemQuery.INDEX_NAME;


public class ShopsFragment extends UtilFragment implements ShakeSensor.ShakeListener,
        TouchAndClickListener.ClickListener, TouchAndClickListener.LongClickListener, TouchAndClickListener.SwipeOutListener {

    ListView mListView;
    ShakeSensor mShakeSensor;

    DatabaseMiddleman mDb;
    CursorAdapter mAdapter;

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

        // this may be unnecessary, but we just force the notification to recompute
        // each list's done and total counts
        mDb.open();
        // since items table may have changed while we paused.
        mAdapter.changeCursor(mDb.fetchAllShops());
        mAdapter.notifyDataSetChanged();

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
        inflater.inflate(R.menu.shops_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        // to prevent double tap on item menu
        item.setEnabled(false);

        if (id == R.id.import_list) {
            //
            // creates new list with name + include clipboard
            //
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            @SuppressLint("InflateParams")
            final View root = inflater.inflate(R.layout.shop_create_dialog, null);

            final CheckBox box = (CheckBox) root.findViewById(R.id.dialog_shop_clipboard);
            final EditText text = (EditText) root.findViewById(R.id.dialog_shop_name);

            final List<Utilities.Triple<String, Float, String>> list = new LinkedList<>();

            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (box.isChecked()) {
                        List<Utilities.Triple<String, Float, String>> tmp = Utilities.parseProductList(Utilities.getClipboardString(getActivity()));
                        if (tmp != null) {
                            list.addAll(tmp);
                            box.setText(format(R.string.INCLUDE_ITEMS, tmp.size()));
                        }
                    } else {
                        list.clear();
                        box.setText(R.string.INCLUDE_CLIPBOARD);
                    }
                }
            });

            builder.setTitle(R.string.NEW_LIST).setView(root);
            builder.setNegativeButton(R.string.CANCEL, null); // nothing to do
            builder.setPositiveButton(R.string.CREATE, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String name = text.getText().toString().trim();
                    if (name.length() > 0) {

                        animateAdd(new ListAnimations.Runner() {
                            @Override
                            public void run(Set<Long> set) {
                                long newShop = mDb.createShop(name, list);
                                if (newShop > 0) {
                                    set.add(newShop);
                                    mAdapter.changeCursor(mDb.fetchAllShops());
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                        popUp(format(R.string.LIST_ADDED, name));
                    }
                }
            });

            final AlertDialog dialog = builder.create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    // after dialog is showing, restore button to enabled
                    item.setEnabled(true);
                }
            });

            text.addTextChangedListener(new UtilTextWatcher(dialog));
            text.setOnEditorActionListener(new UtilEditorActionListener(dialog));

            dialog.show();
            text.setText(""); // also ensures keyboard pops-up

            return true;
        }

        item.setEnabled(true);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.shop_list_fragment, container, false);
        mListView = (ListView) rootView.findViewById(R.id.shops_list);

        mDb.open();

        if (undo == null) {
            // should only run once in the lifetime of this Fragment
            mDb.gcShops();
            undo = new Stack<>();
        }

        TouchAndClickListener t = new TouchAndClickListener(ViewConfiguration.get(getContext()), mListView);
        t.setOnClick(this);
        t.setOnLongClick(this);
        t.setOnSwipeOut(this);

        mAdapter = new ShopsAdapter(getActivity(), mDb.fetchAllShops(), 0, t);

        mListView.setAdapter(mAdapter);

        return rootView;
    }

    private void animateAdd(ListAnimations.Runner action) {
        ListAnimations.animateAdd(mAdapter, mListView, action);
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
        final long shopId = u.second;
        final String shopName = u.first;

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                if (mDb.updateShopDeleted(shopId, false)) {
                    set.add(shopId);
                    mAdapter.changeCursor(mDb.fetchAllShops());
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        popUp(format(R.string.SHAKE_UNDO, shopName));
    }

    @Override
    public void onClick(ListView listView, View view) {
        //
        // launches item activity
        //
        int position = mListView.getPositionForView(view);
        Cursor c = (Cursor) mListView.getItemAtPosition(position);
        long shopId = c.getLong(INDEX_ID);
        String shopName = c.getString(INDEX_NAME);

        Intent intent = new Intent(getActivity(), ItemsActivity.class);
        // links to position in the DB
        intent.putExtra(ItemsActivity.INTENT_SHOP_ID_LONG, shopId);
        intent.putExtra(ItemsActivity.INTENT_SHOP_NAME_STRING, shopName);
        startActivity(intent);

        // for animation
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void onLongClick(ListView listView, View view) {
        //
        // edits existing list name
        //
        final int position = listView.getPositionForView(view);
        final Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        final String oldName = cursor.getString(INDEX_NAME);
        final long shopId = cursor.getLong(INDEX_ID);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.shop_edit_dialog, null);

        final EditText text = (EditText) root.findViewById(R.id.dialog_shop_name);

        builder.setTitle(R.string.EDIT_LIST).setView(root);
        builder.setNegativeButton(R.string.CANCEL, null); // nothing to do on cancel
        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = text.getText().toString().trim();
                if (mDb.renameShop(shopId, newName)) {
                    mAdapter.changeCursor(mDb.fetchAllShops());
                    mAdapter.notifyDataSetChanged();

                    popUp(format(R.string.LIST_RENAMED, oldName, newName));
                } else {
                    popUp(format(R.string.LIST_RENAME_FAILED, oldName));
                }
            }
        });

        final AlertDialog dialog = builder.create();
        text.addTextChangedListener(new UtilTextWatcher(dialog));
        text.setOnEditorActionListener(new UtilEditorActionListener(dialog));

        dialog.show();
        // after showing to trigger TextChangedListener appropriately (must happen AFTER show)
        text.setText(oldName);
        text.setSelection(text.getText().length());
    }

    @Override
    public void onSwipeOut(ListView listView, View view) {
        //
        // deletes swiped out item
        //
        final int position = listView.getPositionForView(view);
        final Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        final long shopId = cursor.getLong(INDEX_ID);
        final String shopName = cursor.getString(INDEX_NAME);

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                if (mDb.updateShopDeleted(shopId, true)) {
                    undo.push(new Pair<>(shopName, shopId));
                    mAdapter.changeCursor(mDb.fetchAllShops());
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

}
