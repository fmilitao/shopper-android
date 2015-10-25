package pt.blah.shopper;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataDB implements Serializable {

    static public class Shop implements Serializable {
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

    static public class Product implements Serializable {
        public String name;
        public int quantity;
        public boolean done;

        Product(String n, int q){
            name = n;
            quantity = q;
            done = false;
        }
    }

    public List<Shop> list;

    DataDB(){
        list = new ArrayList<Shop>();
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
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.name, rhs.name);
            }
        });
    }

}
