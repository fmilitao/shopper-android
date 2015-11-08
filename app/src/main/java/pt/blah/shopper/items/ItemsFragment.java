package pt.blah.shopper.items;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.Stack;

import pt.blah.shopper.DataDB;
import pt.blah.shopper.R;
import pt.blah.shopper.utils.ListAnimations;
import pt.blah.shopper.utils.ShakeSensor;
import pt.blah.shopper.utils.TouchAndClickListener;
import pt.blah.shopper.utils.UtilFragment;
import pt.blah.shopper.utils.Utilities;

import static pt.blah.shopper.utils.Utilities.sData;


public class ItemsFragment extends UtilFragment implements ShakeSensor.ShakeListener,
        TouchAndClickListener.ClickListener, TouchAndClickListener.LongClickListener, TouchAndClickListener.SwipeOutListener {

    ItemsListAdapter mAdapter;
    ListView mListView;
    ShakeSensor mShakeSensor;
    Stack<DataDB.Product> undo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        undo = new Stack<>();
        mShakeSensor = new ShakeSensor(this);
        mShakeSensor.onCreate(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        mShakeSensor.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        sData.save();

        mShakeSensor.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.product_menu, menu);
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

                String p_name = n.getText().toString();
                int p_quantity = 1;

                // nothing to add
                if( p_name.length() == 0 )
                    return;

                try {
                    p_quantity = Integer.parseInt(q.getText().toString());
                } catch (NumberFormatException e) {
                    // ignores error
                }

                final DataDB.Product p = sData.newProduct(p_name, p_quantity);

                animateAdd(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.shop.addProduct(p);
                        mAdapter.notifyDataSetChanged();
                    }
                }, p.id);

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

            // SPINNER
            final Spinner spinner = (Spinner) root.findViewById(R.id.shop_pick);
            String[] shops = new String[sData.getShopCount()];
            int i =0;
            for(DataDB.Shop s : sData.forEachShop()){
                shops[i++] = s.getName();
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                    this.getActivity(), R.layout.item_move_spinner, shops);
                     //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(mAdapter.pos);
            // SPINNER

            // LIST
            final ItemsMoveAdapter moveAdapter = new ItemsMoveAdapter(getActivity(),mAdapter.pos);
            final ListView listView = (ListView) root.findViewById(R.id.product_list);
            listView.setAdapter(moveAdapter);
            // LIST

            builder.setTitle(R.string.TRANSFER_ITEMS);
            builder.setView(root);

            builder.setPositiveButton(R.string.TRANSFER, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // ...
                    final boolean[] set = moveAdapter.getSelected();
                    final int i = spinner.getSelectedItemPosition();

                    int count = 0;
                    for(boolean b : set ){
                        count += b ? 1 : 0;
                    }

                    if( i == mAdapter.pos || count == 0 ) {
                        popUp(getString(R.string.TRANSFER_FAIL));
                    } else {
                        final DataDB.Shop from = moveAdapter.shop;
                        final DataDB.Shop to = sData.getShop(i);

                        // pick elements for transfer
                        int[] transfers = new int[count];
                        for(int x=0,y=0; x < set.length; ++x ){
                            if( set[x] ){
                                transfers[y++] = from.getProduct(x).id;
                            }
                        }

                        animateDelete(new Runnable() {
                            @Override
                            public void run() {
                                DataDB.transfer(from, to, set);
                                // no need to animate since order did not change
                                mAdapter.notifyDataSetChanged();
                            }
                        }, transfers);

                        popUp(format(R.string.ITEM_TRANSFERRED, count, from.getName(), to.getName()));
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int pos = 0;
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Utilities.INTENT_TAG)) {
            pos = intent.getIntExtra(Utilities.INTENT_TAG, 0);
        }

        View rootView = inflater.inflate(R.layout.item_list_fragment, container, false);
        mListView = (ListView) rootView.findViewById(R.id.product_list);

        TouchAndClickListener t = new TouchAndClickListener(ViewConfiguration.get(this.getContext()),mListView);
        mAdapter = new ItemsListAdapter(getActivity(),pos, t);
        t.setOnClick(this);
        t.setOnLongClick(this);
        t.setOnSwipeOut(this);

        mListView.setAdapter(mAdapter);

        DataDB.Shop shop = sData.getShop(pos);
        getActivity().setTitle(shop.getName());
        return rootView;
    }

    private void animateAdd(Runnable action, int added){
        ListAnimations.animateAdd(mAdapter, mListView, action, added);
    }

    private void animateAdd(Runnable action){
        ListAnimations.animateAdd(mAdapter, mListView, action, -1);
    }

    private void animateDelete(Runnable andThen, int... deletes){
        ListAnimations.animateDelete(mAdapter, mListView, andThen, deletes);
    }

    @Override
    public void onShake() {
        if (undo.isEmpty()) {
            popUp(getString(R.string.SHAKE_FAIL));
            return;
        }

        final DataDB.Product product = undo.pop();

        animateAdd(new Runnable() {
            @Override
            public void run() {
                mAdapter.shop.addProduct(product);
                mAdapter.notifyDataSetChanged();
            }
        }, product.id);

        popUp(format(R.string.SHAKE_UNDO, product.getName()));
    }

    @Override
    public void onClick(ListView listView, View view) {
        int position = listView.getPositionForView(view);
        DataDB.Product pp = mAdapter.shop.getProduct(position);
        pp.flipDone();

        animateAdd(new Runnable() {
            @Override
            public void run() {
                mAdapter.shop.sortProducts();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onLongClick(ListView listView, View view) {
        final int position = listView.getPositionForView(view);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.item_create_dialog, null);

        final EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
        final EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

        final DataDB.Product product = mAdapter.shop.getProduct(position);

        n.setText(product.getName());
        n.setSelection(n.getText().length());
        q.setText(format(R.string.NUMBER, product.getQuantity()));
        q.setSelection(q.getText().length());

        builder.setTitle(R.string.UPDATE);
        builder.setView(root);
        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String p_name = n.getText().toString();
                final int p_quantity = Integer.parseInt(q.getText().toString());

                if (p_name.length() > 0 && (!p_name.equals(product.getName()) || p_quantity != product.getQuantity())) {

                    animateAdd(new Runnable() {
                        @Override
                        public void run() {
                            product.setName( p_name );
                            product.setQuantity( p_quantity );
                            mAdapter.shop.sortProducts();
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
        animateAdd(new Runnable() {
            @Override
            public void run() {
                undo.push(mAdapter.shop.removeProduct(position));
                mAdapter.notifyDataSetChanged();
            }
        }, -1);
    }
}
