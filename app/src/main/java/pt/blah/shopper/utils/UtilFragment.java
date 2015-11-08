package pt.blah.shopper.utils;


import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Extensions to 'Fragment' to include some convenient methods for formatting string resources
 * and showing pop-up toasts on the top of the screen.
 */
public class UtilFragment extends Fragment{

    public String format(int id, Object... args){
        return String.format(getActivity().getResources().getString(id), args);
    }

    public void popUp(String notification) {
        Toast t = Toast.makeText(getActivity().getApplicationContext(),
                notification,
                Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP,0,0);
        t.show();
    }

}
