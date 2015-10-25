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
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import static pt.blah.shopper.Utilities.sData;
import static pt.blah.shopper.Utilities.format;


public class ProductsFragment extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    static final int MOVE_SPEED = 250;

    ProductsListAdapter mAdapter;
    ListView mListView;

    public ProductsFragment() {
        // intentionally empty
    }

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
        final int position = mAdapter.pos;

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

                try {
                    p_quantity = Integer.parseInt(q.getText().toString());
                } catch (NumberFormatException e) {
                    // ignores error
                }

                sData.list.get(position).products.add(new DataDB.Product(p_name, p_quantity));

                Utilities.notifyListeners();
                Utilities.popUp(getActivity(), format(R.string.ITEM_ADDED, p_name, p_quantity));

                autoSort(mListView); //FIXME!
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

//        if( id == R.id.sort_products ){
////            DataDB.sort( sData.list.get(mAdapter.pos).products );
////            Utilities.popUp(getActivity(), getString(R.string.LIST_SORTED));
////            Utilities.notifyListeners();
//            autoSort(mListView); //TODO
//            return true;
//        }

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
            ListView listView = (ListView) root.findViewById(R.id.product_list);
            listView.setAdapter(moveAdapter);
            // LIST

            builder.setTitle(R.string.TRANSFER_ITEMS);
            builder.setView(root);

            builder.setPositiveButton(R.string.TRANSFER, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // ...
                    boolean[] set = moveAdapter.getSelected();
                    int i = spinner.getSelectedItemPosition();

                    if( i == mAdapter.pos ) {
                        Utilities.popUp(getActivity(), getString(R.string.TRANSFER_FAIL));
                    } else {
                        DataDB.Shop from = sData.list.get(moveAdapter.pos);
                        DataDB.Shop to = sData.list.get(i);

                        // first copy
                        for(int j = 0 ; j < set.length ; ++ j ){
                            if( set[j]) {
                                to.products.add(from.products.get(j));
                            }
                        }

                        int count = 0;
                        for(int j = set.length-1 ; j >= 0 ; --j ){
                            if( set[j]) {
                                from.products.remove(j);
                                ++count;
                            }
                        }

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
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View root = inflater.inflate(R.layout.product_dialog, null);
        final EditText n = (EditText) root.findViewById(R.id.dialog_product_name);
        final EditText q = (EditText) root.findViewById(R.id.dialog_product_quantity);

        final DataDB.Product product = sData.list.get(mAdapter.pos).products.get(position);

        n.setText(product.name);
        n.setSelection(n.getText().length());
        q.setText(Integer.toString(product.quantity));
        q.setSelection(q.getText().length());

        builder.setTitle(R.string.UPDATE);
        builder.setView(root);
        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String p_name = n.getText().toString();
                int p_quantity = Integer.parseInt(q.getText().toString());

                if (p_name.length() > 0) {
                    product.name = p_name;
                    product.quantity = p_quantity;

                    // FIXME needs to update unique ids
                    Utilities.notifyListeners();
                    Utilities.popUp(getActivity(), format(R.string.ITEM_ADDED, p_name, p_quantity));
                }
            }
        });

        builder.setNegativeButton(R.string.DELETE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // aborted, nothing to do
                sData.list.get(mAdapter.pos).products.remove(position);
                Utilities.notifyListeners();
                Utilities.popUp(getActivity(), format(R.string.ITEM_DELETED, product.name));
            }
        });
        builder.create().show();

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int p = mAdapter.pos;
        DataDB.Product pp = sData.list.get(p).products.get(position);
        pp.done = !pp.done;

        //Utilities.notifyListeners();
        autoSort(mListView); //TODO
    }


    //
    //
    //

    private void autoSort(final ListView listview) {
        final Map<Long,Integer> map = new HashMap<>();
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            int position = firstVisiblePosition + i;
            long itemId = mAdapter.getItemId(position);

            // stores old top of a view on a Map
            map.put(itemId, child.getTop());

        }

        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);

                int firstVisiblePosition = listview.getFirstVisiblePosition();

                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = mAdapter.getItemId(position);

                    // is null on non existing views (i.e. outside screen)
                    Integer oldTop = map.get(itemId);
                    int newTop = child.getTop(); // i.e. the new position!

                    // already in correct position
                    if (oldTop != null && oldTop == newTop)
                        continue;

                    // child not previously present
                    if (oldTop == null) {
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        oldTop = newTop + (i > 0 ? childHeight : -childHeight);
                    }

                    int delta = oldTop - newTop;
                    child.setTranslationY(delta);
                    child.animate().setDuration(MOVE_SPEED).translationY(0);
                }
                map.clear();
                return true;
            }
        });

        DataDB.sort(sData.list.get(mAdapter.pos).products );
        //Utilities.popUp(getActivity(), getString(R.string.LIST_SORTED));
        Utilities.notifyListeners();
    }
}
