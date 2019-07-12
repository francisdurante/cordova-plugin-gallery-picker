package cordova.plugin.gallery.picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class VideoFragment extends Fragment {

  private FakeR faker;
  static GridView gallery;
  private ArrayList<String> video;
  private ArrayList<String> selectedImage;
  private String selectedFromCamera;
  private Spinner spinner;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    faker = new FakeR(getContext());
    View view = inflater.inflate(faker.getId("layout","fragment_video"), container, false);

    TextView cancel = view.findViewById(faker.getId("id","cancel"));
    TextView next = view.findViewById(faker.getId("id","next"));
    spinner = view.findViewById(faker.getId("id","spinner"));

    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().finish();
      }
    });

    next.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //return all selected images to app.\
        JSONArray response = new JSONArray();
        for(int x = 0 ; x < selectedImage.size(); x++)
        {
          response.put(selectedImage.get(x));
        }
        ImagePicker.callbackContext.success(response);
        ImagePicker.callbackContext.success(response);
        getActivity().finish();
      }
    });

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(spinner.getSelectedItem().toString().equals("Camera"))
        {
//          getActivity().startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE), 1000);
          //return success callback call videoEffects plugin

			ImagePicker.callbackContext.success("plugin_video_effects");
			getActivity().finish();
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    gallery = view.findViewById(faker.getId("id","gallery_grid"));
    gallery.setAdapter(new ImageAdapterGallery(getActivity()));
    gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getContext(),position,Toast.LENGTH_LONG).show();
      }
    });
    return view;
  }
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1000 && resultCode == RESULT_OK) {
      Bitmap bitmap = (Bitmap)data.getExtras().get("data") ;
      selectedFromCamera = getRealPathFromURI(getContext(),getImageUri(getContext(),bitmap));
      //return Callback success;
    }
    else
    {
      spinner.setSelection(0);
    }
  }

  public Uri getImageUri(Context inContext, Bitmap inImage) {
    String path =
      MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,
        "Title", null);
    return Uri.parse(path);
  }
  public String getRealPathFromURI(Context context, Uri contentUri) {
    Cursor cursor = null;
    try {
      String[] proj = { MediaStore.Images.Media.DATA };
      cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }
  public void setSelectedIndex(int index, boolean selected) {
    if (selected) {
      selectedImage.add(video.get(index));
    } else {
      selectedImage.remove(video.get(index));
    }
  }

  private class ImageAdapterGallery extends BaseAdapter {
    private Activity context;
    private Object flag[];

    public ImageAdapterGallery(Activity localContext) {
      context = localContext;
      video = getAllShownImagesPath(context);
      selectedImage = new ArrayList<>();
    }

    @Override
    public int getCount() {
      flag = new Object[video.size()];
      return video.size();
    }

    @Override
    public Object getItem(int position) {
      return position;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      final ImageView picturesView = new ImageView(context);
      ImageView playButton = new ImageView(getContext());
      LinearLayout layout = new LinearLayout(getContext());
      RelativeLayout relativeLayout = new RelativeLayout(getContext());

      if (flag[position] == null) {
        layout.setLayoutParams(new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.HORIZONTAL);


        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT));

        RelativeLayout.LayoutParams checkParam = new RelativeLayout.LayoutParams(
          RelativeLayout.LayoutParams.WRAP_CONTENT,
          RelativeLayout.LayoutParams.WRAP_CONTENT);

        final CheckBox checkBox = new CheckBox(getContext());
        if (selectedImage.contains(video.get(position))) {
          checkBox.setChecked(true);
          picturesView.setAlpha(0.3f);
        } else {
          checkBox.setChecked(false);
          picturesView.setAlpha(1f);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setSelectedIndex(position, isChecked);
            if(isChecked)
              picturesView.setAlpha(0.3f);
            else
              picturesView.setAlpha(1f);
          }
        });

        RelativeLayout.LayoutParams playButtonParam = new RelativeLayout.LayoutParams(
          RelativeLayout.LayoutParams.WRAP_CONTENT,
          RelativeLayout.LayoutParams.WRAP_CONTENT);

        checkParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        checkParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        picturesView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        picturesView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if(selectedImage.contains(video.get(position))){
              checkBox.setChecked(false);
              picturesView.setAlpha(1f);
            }else if(selectedImage.size() == 0){
              checkBox.setChecked(true);
              picturesView.setAlpha(0.3f);
            }else{
              checkBox.setChecked(true);
              picturesView.setAlpha(0.3f);
            }
          }
        });

        playButton.setBackground(getResources().getDrawable(faker.getId("drawable","ic_play_button")));
        playButton.setAdjustViewBounds(true);
        playButtonParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        playButtonParam.addRule(RelativeLayout.CENTER_IN_PARENT);


        picturesView.setLayoutParams(new LinearLayout.LayoutParams(270, 230));

        relativeLayout.addView(picturesView);
        relativeLayout.addView(playButton,playButtonParam);
        relativeLayout.addView(checkBox, checkParam);


      } else {
        layout = (LinearLayout) convertView;
      }


      layout.addView(relativeLayout);
      Glide.with(context)
        .asBitmap()
        .load(video.get(position))
        .into(picturesView);

      return layout;
    }
    private ArrayList<String> getAllShownImagesPath(Activity activity) {
      Uri uri;
      Cursor cursor;
      final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
      int column_index_data, column_index_folder_name;
      ArrayList<String> listOfAllImages = new ArrayList<String>();
      String absolutePathOfImage = "";
      uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

      String[] projection = {MediaStore.MediaColumns.DATA,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

      cursor = activity.getContentResolver().query(uri, projection, null,
        null, orderBy + " DESC");

      column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
      column_index_folder_name = cursor
        .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
      while (cursor.moveToNext()) {
        absolutePathOfImage = cursor.getString(column_index_data);

        listOfAllImages.add(absolutePathOfImage);
      }
      return listOfAllImages;
    }
  }
}
