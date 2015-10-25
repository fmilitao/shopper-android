package pt.blah.shopper;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


// FIXME where all is already sorted and with a UNIQUE id
public class DataDB2 implements Serializable {

    static public class Shop implements Serializable {
        public String name;
        public Set<Product> products;

        Shop(String n){
            name = n;
            products = new TreeSet<Product>(new Comparator<Product>() {
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
        public int id;

        Product(String n, int q){
            name = n;
            quantity = q;
            done = false;
            id = COUNTER++;
        }
    }

    static int COUNTER = 0; // FIXME dont forget to read this separately!

    public List<Shop> list;
    public int count;

    DataDB2(){
        list = new ArrayList<Shop>();
    }

//    static void sort(List<Product> products){
//        Collections.sort(products, new Comparator<Product>() {
//            @Override
//            public int compare(Product lhs, Product rhs) {
//                if( !lhs.done && rhs.done )
//                    return -1; //lhs is less than rhs
//                if( lhs.done && !rhs.done )
//                    return 1; //lhs is greater
//                // both equally done or not done
//                return String.CASE_INSENSITIVE_ORDER.compare(lhs.name, rhs.name);
//            }
//        });
//    }

}
