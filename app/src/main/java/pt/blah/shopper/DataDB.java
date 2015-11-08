package pt.blah.shopper;


import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pt.blah.shopper.utils.Utilities;

// FIXME hacky class. will fix when switched to using SQLite.
// FIXME: remove requirement for file system permission

@Deprecated
public class DataDB implements Serializable {

    public class Shop implements Serializable {
        final public int id;

        private String name;
        private List<Product> products;

        private Shop(String n){
            name = n;
            products = new ArrayList<>();
            id = count++;
        }

        public int getPending(){
            int i=0;
            for(Product p : products){
                if(!p.done)
                    ++i;
            }
            return i;
        }

        public String getName(){
            return name;
        }

        public void rename(String newName){
            ++version;
            name = newName;
        }

        public int getProductCount(){
            return products.size();
        }

        public Iterable<Product> forEachProduct(){
            return products;
        }

        public void addProduct(Product p){
            ++version;
            products.add(p);
            sort(products);
        }

        public Product getProduct(int productId){
            return products.get(productId);
        }

        public Product removeProduct(int productId){
            ++version;
            return products.remove(productId);
        }

        public void sortProducts(){
            ++version;
            sort(products);
        }
    }

    public class Product implements Serializable {
        final public int id;

        private String name;
        private int quantity;
        private boolean done;

        private Product(String n, int q){
            name = n;
            quantity = q;
            done = false;
            id = count++;
        }

        public String getName(){
            return name;
        }

        public void setName(String newName){
            ++version;
            this.name = newName;
        }

        public int getQuantity(){
            return quantity;
        }

        public void setQuantity(int newQuantity){
            ++version;
            this.quantity = newQuantity;
        }

        public boolean isDone(){
            return done;
        }

        public void flipDone(){
            ++version;
            done = !done;
        }
    }

    private List<Shop> list;
    private int count;

    private int version = 0, last_saved = -1;
    private File file;

    public DataDB(@NonNull File f){
        file = f;
        // yes, potential redundant initialization...
        list = new ArrayList<>();
        count = 0;

        load();
    }

    //
    // I/O
    //

    static final String LOG_TG = DataDB.class.toString();

    public void save(){

        if( version <= last_saved ){
            Log.v(LOG_TG, "already saved.");
            return;
        }

        Log.v(LOG_TG,file.getAbsolutePath());
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(this.list);
            out.writeInt(this.count);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(LOG_TG,"file saved.");

        last_saved = version;
    }

    private void load(){

        if( !file.exists() ) {
            Log.v(LOG_TG,"file does not exist");
            return;
        }

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            this.list = (List<DataDB.Shop>) in.readObject();
            this.count = in.readInt();

            //TODO ensures sorted, better with sorted set but also needs to index element
            for(DataDB.Shop s : this.list){
                DataDB.sort( s.products );
            }
            in.close();
            Log.v(LOG_TG, "file loaded");
        } catch (Exception e) {
            e.printStackTrace();
            if( file.delete() )
                Log.v(LOG_TG, "file deleted.");
            // file will be overwritten anyway, even if with empty content
            Log.v(LOG_TG, "fail to load.");
        }

    }

    //
    // Shop
    //

    public int getShopCount(){
        return list.size();
    }

    public Shop getShop(int shopId){
        return list.get(shopId);
    }

    public void addShop(Shop shop){
        ++version;
        list.add(shop);
    }

    public Shop deleteShop(int shopId){
        ++version;
        return list.remove(shopId);
    }

    public Iterable<Shop> forEachShop(){
        return list;
    }

    public Shop newShop(String n, List<Product> products){
        Shop tmp = new Shop(n);
        tmp.products = products;
        // ensure sorted
        tmp.sortProducts();
        return tmp;
    }

    //
    // Product
    //

    public Product newProduct(String n, int q){
        return new Product(n,q);
    }

    //
    // Static Utils
    //

    private static void sort(List<Product> products){
        Collections.sort(products, new Comparator<Product>() {
            @Override
            public int compare(Product lhs, Product rhs) {
                if( !lhs.done && rhs.done )
                    return -1; //lhs is less than rhs
                if( lhs.done && !rhs.done )
                    return 1; //lhs is greater
                // both equally done or not done
                int cp = String.CASE_INSENSITIVE_ORDER.compare(lhs.name, rhs.name);
                return cp == 0 ? lhs.id - rhs.id : cp;
            }
        });
    }

    public static void transfer(Shop from, Shop to, boolean[] set){
        // first copy
        for (int j = 0; j < set.length; ++j) {
            if (set[j]) {
                to.products.add(from.products.get(j));
            }
        }

        // only needs to sort added stuff
        sort(to.products);

        // then remove
        for (int j = set.length - 1; j >= 0; --j) {
            if (set[j]) {
                from.products.remove(j);
            }
        }

        // FIXME: hack!
        Utilities.sData.version++;
    }


}
