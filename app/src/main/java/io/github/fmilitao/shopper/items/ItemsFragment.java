package io.github.fmilitao.shopper.items;


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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.Set;
import java.util.Stack;

import io.github.fmilitao.shopper.R;
import io.github.fmilitao.shopper.sql.DBContract;
import io.github.fmilitao.shopper.sql.DatabaseMiddleman;
import io.github.fmilitao.shopper.utils.ListAnimations;
import io.github.fmilitao.shopper.utils.ShakeSensor;
import io.github.fmilitao.shopper.utils.TouchAndClickListener;
import io.github.fmilitao.shopper.utils.UtilFragment;


public class ItemsFragment extends UtilFragment implements ShakeSensor.ShakeListener,
        TouchAndClickListener.ClickListener, TouchAndClickListener.LongClickListener, TouchAndClickListener.SwipeOutListener {

    ListView mListView;
    ShakeSensor mShakeSensor;

    DatabaseMiddleman mDb;
    CursorAdapter mAdapter;

    long mShopId;
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

    protected void addItem(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.item_create_dialog, null);

        builder.setTitle(R.string.NEW_ITEM);
        builder.setView(root);

        // Same code for Positive and Neutral buttons.
        final DialogInterface.OnClickListener aux = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
                EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

                final String p_name = n.getText().toString();

                // nothing to add
                if( p_name.length() == 0 )
                    return;

                int qt = 1; // default quantity is '1'
                try {
                    qt = Integer.parseInt(q.getText().toString());
                } catch (NumberFormatException e) {
                    // ignores error
                }

                final int p_quantity = qt;

                animateAdd(new ListAnimations.Runner() {
                    @Override
                    public void run(Set<Long> set) {
                        long newItem = mDb.createItem(p_name, mShopId, p_quantity, false);
                        if( newItem > 0 ) {
                            set.add(newItem);
                            mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });

                popUp(format(R.string.ITEM_ADDED, p_name, p_quantity));

            }
        };

        builder.setPositiveButton(R.string.CREATE, aux);

        builder.setNeutralButton(R.string.NEXT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aux.onClick(dialog, which);
                addItem();
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // aborted, nothing to do
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_item) {
            addItem();
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

            builder.setTitle(R.string.TRANSFER_ITEMS);
            builder.setView(root);

            builder.setPositiveButton(R.string.TRANSFER, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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
                                if( mDb.transfer(transfers,toShopId) ) {
                                    // no need to animate since order did not change
                                    mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }, transfers);

                        popUp(format(R.string.ITEM_TRANSFERRED, count, fromShopName, toShopName));
                    }
                }
            });

            builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // aborted, nothing to do
                }
            });

            builder.create().show();

            return true;
        }

        if (id == R.id.load_items){
            loadDialog(null);
            return true;
        }
        if (id == R.id.save_items){
            saveDialog(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        // intentionally let app crash if intent not provided (fatal error anyway)
        String shopName = intent.getStringExtra(ItemsActivity.INTENT_SHOP_NAME_STRING);
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

        getActivity().setTitle(shopName);
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
        final Cursor c = (Cursor) listView.getItemAtPosition(position);
        final long itemId = c.getLong(DBContract.SelectItemQuery.INDEX_ID);
        final String itemName = c.getString(DBContract.SelectItemQuery.INDEX_NAME);
        final int itemQuantity = c.getInt(DBContract.SelectItemQuery.INDEX_QUANTITY);
        final String itemQuantityStr = c.getString(DBContract.SelectItemQuery.INDEX_QUANTITY);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.item_create_dialog, null);

        final EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
        final EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

        n.setText(itemName);
        n.setSelection(n.getText().length());
        q.setText(itemQuantityStr); //FIXME test to make sure this work OK!
        q.setSelection(q.getText().length());

        builder.setTitle(R.string.UPDATE);
        builder.setView(root);
        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String p_name = n.getText().toString();
                final int p_quantity = Integer.parseInt(q.getText().toString());

                if (p_name.length() > 0 && (!p_name.equals(itemName) || p_quantity != itemQuantity)) {

                    animateAdd(new ListAnimations.Runner() {
                        @Override
                        public void run(Set<Long> set) {
                            mDb.updateItem(itemId,p_name,p_quantity);
                            mAdapter.changeCursor(mDb.fetchShopItems(mShopId));
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                    popUp(format(R.string.ITEM_UPDATED, p_name, p_quantity));
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

    // TODO
    protected void save(String file) {
        popUp("Saved: " + file);
    }


    // TODO
    protected void load(String file) {
        popUp("Loaded: " + file);
    }
}
