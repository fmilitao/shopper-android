package pt.blah.shopper;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import static pt.blah.shopper.Utilities.format;
import static pt.blah.shopper.Utilities.sData;


public class ProductsFragment extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    ProductsListAdapter mAdapter;
    ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.addListener(this, mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Utilities.removeListener(this);
        Utilities.save();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.product_menu, menu);
    }

    protected void addItem(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View root = inflater.inflate(R.layout.product_dialog, null);

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
                if( p_name == null || p_name.length() == 0 )
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
                        mAdapter.shop.products.add(p);
                        DataDB.sort(mAdapter.shop.products);
                        Utilities.notifyListeners();
                    }
                }, p.id);

                Utilities.popUp(getActivity(), format(R.string.ITEM_ADDED, p_name, p_quantity));

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

            final View root = inflater.inflate(R.layout.move_dialog, null);

            // SPINNER
            final Spinner spinner = (Spinner) root.findViewById(R.id.shop_pick);
            String[] shops = new String[sData.list.size()];
            int i =0;
            for(DataDB.Shop s : sData.list){
                shops[i++] = s.name;
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                    this.getActivity(), R.layout.move_spinner, shops);
                     //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(mAdapter.pos);
            // SPINNER

            // LIST
            final ProductsMoveAdapter moveAdapter = new ProductsMoveAdapter(getActivity(),mAdapter.pos);
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
                        Utilities.popUp(getActivity(), getString(R.string.TRANSFER_FAIL));
                    } else {
                        final DataDB.Shop from = moveAdapter.shop;
                        final DataDB.Shop to = sData.list.get(i);

                        // pick elements for transfer
                        int[] transfers = new int[count];
                        for(int x=0,y=0; x < set.length; ++x ){
                            if( set[x] ){
                                transfers[y++] = from.products.get(x).id;
                            }
                        }

                        animateDelete(new Runnable() {
                            @Override
                            public void run() {

                                // first copy
                                for (int j = 0; j < set.length; ++j) {
                                    if (set[j]) {
                                        to.products.add(from.products.get(j));
                                    }
                                }

                                // then remove
                                for (int j = set.length - 1; j >= 0; --j) {
                                    if (set[j]) {
                                        from.products.remove(j);
                                    }
                                }

                                // no need to animate since order did not change
                                Utilities.notifyListeners();
                            }
                        }, transfers);

                        Utilities.notifyListeners();
                        Utilities.popUp(getActivity(), format(R.string.ITEM_TRANSFERRED, count, from.name, to.name));
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
        mAdapter = new ProductsListAdapter(getActivity(),pos);

        View rootView = inflater.inflate(R.layout.product_fragment, container, false);
        mListView = (ListView) rootView.findViewById(R.id.product_list);
        mListView.setAdapter(mAdapter);

        mListView.setClickable(true);
        mListView.setOnItemClickListener(this);

        mListView.setLongClickable(true);
        mListView.setOnItemLongClickListener(this);

        DataDB.Shop shop = sData.list.get(pos);
        getActivity().setTitle(shop.name);
        return rootView;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View root = inflater.inflate(R.layout.product_dialog, null);
        final EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
        final EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

        final DataDB.Product product = mAdapter.shop.products.get(position);

        n.setText(product.name);
        n.setSelection(n.getText().length());
        q.setText(Integer.toString(product.quantity));
        q.setSelection(q.getText().length());

        builder.setTitle(R.string.UPDATE);
        builder.setView(root);
        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String p_name = n.getText().toString();
                final int p_quantity = Integer.parseInt(q.getText().toString());

                if (p_name.length() > 0 && (!p_name.equals(product.name) || p_quantity != product.quantity)) {

                    animateAdd(new Runnable() {
                        @Override
                        public void run() {
                            product.name = p_name;
                            product.quantity = p_quantity;
                            DataDB.sort(mAdapter.shop.products);
                            Utilities.notifyListeners();
                        }
                    });

                    Utilities.popUp(getActivity(), format(R.string.ITEM_UPDATED, p_name, p_quantity));
                }
            }
        });

        builder.setNegativeButton(R.string.DELETE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataDB.Product p = sData.list.get(mAdapter.pos).products.get(position);

                animateDelete(new Runnable() {
                    @Override
                    public void run() {
                        animateAdd(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.shop.products.remove(position);
                                DataDB.sort(mAdapter.shop.products);
                                Utilities.notifyListeners();
                            }
                        });
                    }
                }, p.id);

                Utilities.popUp(getActivity(), format(R.string.ITEM_DELETED, product.name));
            }
        });
        builder.create().show();

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DataDB.Product pp = mAdapter.shop.products.get(position);
        pp.done = !pp.done;

        animateAdd(new Runnable() {
            @Override
            public void run() {
                DataDB.sort(mAdapter.shop.products);
                Utilities.notifyListeners();
            }
        });
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

}
