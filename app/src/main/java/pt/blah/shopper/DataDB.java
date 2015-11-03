package pt.blah.shopper;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// FIXME hacky class. will fix when switched to using SQLite.

// Probably a bit tricky to switch?
// Build unified interface to better sense what is needed, rather than expose all

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
            name = newName;
        }

        public int getProductCount(){
            return products.size();
        }

        public Iterable<Product> forEachProduct(){
            return products;
        }

        public void addProduct(Product p){
            products.add(p);
            sort(products);
        }

        public Product getProduct(int productId){
            return products.get(productId);
        }

        public Product removeProduct(int productId){
            return products.remove(productId);
        }

        public void sortProducts(){
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
            this.name = newName;
        }

        public int getQuantity(){
            return quantity;
        }

        public void setQuantity(int newQuantity){
            this.quantity = newQuantity;
        }

        public boolean isDone(){
            return done;
        }

        public void flipDone(){
            done = !done;
        }
    }

    private List<Shop> list;
    private int count;

    DataDB(){
        list = new ArrayList<>();
        count = 0;
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
        list.add(shop);
    }

    public Shop deleteShop(int shopId){
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

    static void transfer(Shop from, Shop to, boolean[] set){
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
    }

    //
    // I/O stuff
    //

    public void load(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.list = (List<DataDB.Shop>) in.readObject();
        this.count = in.readInt();

        //TODO ensures sorted, better with sorted set but also needs to index element
        for(DataDB.Shop s : this.list){
            DataDB.sort( s.products );
        }
    }

    public void save(ObjectOutputStream out) throws IOException {
        out.writeObject(this.list);
        out.writeInt(this.count);
    }

}
