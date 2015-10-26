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
public class DataDB implements Serializable {

    public class Shop implements Serializable {
        public String name;
        public List<Product> products;

        Shop(String n){
            name = n;
            products = new ArrayList<Product>();
        }

        int getPending(){
            int i=0;
            for(Product p : products){
                if(!p.done)
                    ++i;
            }
            return i;
        }
    }

    public class Product implements Serializable {
        public String name;
        public int quantity;
        public boolean done;
        public int id;

        Product(String n, int q){
            name = n;
            quantity = q;
            done = false;
            id = count++;
        }
    }

    public List<Shop> list;
    private int count;

    DataDB(){
        list = new ArrayList<Shop>();
        count = 0;
    }

    Shop newShop(String n){
        return new Shop(n);
    }

    Product newProduct(String n, int q){
        return new Product(n,q);
    }

    static void sort(List<Product> products){
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
