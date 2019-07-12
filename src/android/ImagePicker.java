package cordova.plugin.gallery.picker;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class ImagePicker extends CordovaPlugin {
	public static CallbackContext callbackContext;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
        if (action.equals("coolMethod")) {
            this.coolMethod(args);
            return true;
        }
        return false;
    }

   private void coolMethod(JSONArray args) {
		Context context = this.cordova.getActivity().getApplicationContext();
		openNewActivity(context,args);
    }

	private void openNewActivity(Context context,JSONArray args) {
        Intent intent = new Intent(context, MainActivity.class);
		    intent.putExtra("PARAMETER", (Parcelable) args);
        this.cordova.getActivity().startActivity(intent);
    }
}
