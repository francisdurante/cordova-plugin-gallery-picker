package cordova.plugin.gallery.picker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static android.app.Activity.RESULT_OK;


public class SlideShowFragment extends Fragment {
  private FakeR faker;
  static GridView gallery;
  private ArrayList<String> images;
  private ArrayList<String> selectedImage;
  private ArrayList<String> selectedFromCamera;
  private Spinner spinner;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    faker = new FakeR(getContext());
    View view = inflater.inflate(faker.getId("layout","fragment_slide_show"), container, false);
    TextView cancel = view.findViewById(faker.getId("id","cancel"));
    TextView next = view.findViewById(faker.getId("id","next"));
    spinner = view.findViewById(faker.getId("id","spinner"));

    cancel.setOnClickListener(v -> {
      ImagePicker.callbackContext.error("USER_CANCEL");
      getActivity().finish();
    });

    next.setOnClickListener(v -> {
      //return all selected images to app.\
      JSONArray response = new JSONArray();
      for(int x = 0 ; x < selectedImage.size(); x++)
      {
        response.put(selectedImage.get(x));
      }
      ImagePicker.callbackContext.success(response);
      getActivity().finish();
    });

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(spinner.getSelectedItem().toString().equals("Camera"))
        {
//          getActivity().startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1000);
          ImagePicker.callbackContext.success("camera_request");
          getActivity().finish();
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    gallery = view.findViewById(faker.getId("id","gallery_grid"));
    gallery.setAdapter(new ImageAdapterGallery(getActivity()));
    gallery.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
    gallery.setDrawSelectorOnTop(false);
    gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1,
                              int position, long arg3) {

        if (null != images && !images.isEmpty()) {
          Toast.makeText(
            getContext(),
            "position " + position + " " + images.get(position),
            Toast.LENGTH_LONG).show();
        }
      }

    });

    return view;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1000 && resultCode == RESULT_OK) {
      selectedFromCamera = new ArrayList<>();
      Bitmap bitmap = (Bitmap)data.getExtras().get("data") ;
//      saveImage(bitmap);
        selectedFromCamera.add(saveImage(bitmap));
      JSONArray response = new JSONArray();
      for(int x = 0 ; x < selectedFromCamera.size(); x++)
      {
        response.put(selectedFromCamera.get(x));
      }
      ImagePicker.callbackContext.success(response);
      getActivity().finish();
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

  private Bitmap getBitmap(String path) {

    Uri uri = Uri.fromFile(new File(path));
    InputStream in = null;
    try {
      final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
      in = getContext().getContentResolver().openInputStream(uri);

      // Decode image size
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(in, null, o);
      in.close();


      int scale = 1;
      while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
        IMAGE_MAX_SIZE) {
        scale++;
      }
//      Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

      Bitmap b = null;
      in = getContext().getContentResolver().openInputStream(uri);
      if (scale > 1) {
        scale--;
        // scale to max possible inSampleSize that still yields an image
        // larger than target
        o = new BitmapFactory.Options();
        o.inSampleSize = scale;
        b = BitmapFactory.decodeStream(in, null, o);

        // resize to desired dimensions
        int height = b.getHeight();
        int width = b.getWidth();
//        Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

        double y = Math.sqrt(IMAGE_MAX_SIZE
          / (((double) width) / height));
        double x = (y / height) * width;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
          (int) y, true);
        b.recycle();
        b = scaledBitmap;

        System.gc();
      } else {
        b = BitmapFactory.decodeStream(in);
      }
      in.close();

//      Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " +
//        b.getHeight());
      return b;
    } catch (IOException e) {
//      Log.e("", e.getMessage(), e);
      return null;
    }
  }

  private String saveImage(Bitmap segg) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 3;
    options.inJustDecodeBounds = true;
    String path = "";
    OutputStream fOut = null;
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String fileName = "Mystorya-"+ timeStamp +".jpg";
    String root = Environment.getExternalStorageDirectory().toString();
    final File imageRoot = new File(root, "Android/data/"+getContext().getPackageName()+"/mystory_images");
    imageRoot.mkdirs();
    final File file = new File(imageRoot, fileName);
    try {
      fOut = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    segg.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
    try {
      Toast.makeText(getActivity(),
        file.getAbsolutePath(),
        Toast.LENGTH_LONG).show();
      path = file.getAbsolutePath();
      BitmapFactory.decodeFile(path, options);
      fOut.flush();
      fOut.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return path;

//    ContentValues values = new ContentValues();
//    values.put(MediaStore.Images.Media.TITLE,"stego");
//    values.put(MediaStore.Images.Media.DESCRIPTION, "stego description");
//    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//    values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
//    values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
//    values.put("_data", file.getAbsolutePath());
//    Toast.makeText(ExtractActivity.this,
//      file.getAbsolutePath(),
//      Toast.LENGTH_LONG).show();
//    ContentResolver cr = getContext().getContentResolver();
//    cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//    Toast.makeText(ExtractActivity.this, "The Image thumbnail created in Gallery ", Toast.LENGTH_LONG).show();
  }
  public static String savebitmap(Bitmap bmp) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    File f = new File(Environment.getExternalStorageDirectory()
      + File.separator + "testimage.jpg");
    f.createNewFile();
    FileOutputStream fo = new FileOutputStream(f);
    fo.write(bytes.toByteArray());
    fo.close();
    return f.getAbsolutePath();
  }
  public void setSelectedIndex(int index, boolean selected) {
    if (selected) {
      selectedImage.add(images.get(index));
    } else {
      selectedImage.remove(images.get(index));
    }
  }

  private class ImageAdapterGallery extends BaseAdapter {
    private Activity context;
    private Object flag[];

    public ImageAdapterGallery(Activity localContext) {
      context = localContext;
      images = getAllShownImagesPath(context);
      selectedImage = new ArrayList<>();
    }

    @Override
    public int getCount() {
      flag = new Object[images.size()];
      return images.size();
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
        if (selectedImage.contains(images.get(position))) {
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

        checkParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        checkParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        picturesView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        picturesView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if(selectedImage.contains(images.get(position))){
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
        picturesView.setLayoutParams(new LinearLayout.LayoutParams(230, 230));
        relativeLayout.addView(picturesView);
        relativeLayout.addView(checkBox, checkParam);


      } else {
        layout = (LinearLayout) convertView;
      }

      layout.addView(relativeLayout);

      Glide.with(context).load(images.get(position))
        .placeholder(faker.getId("drawable","placeholder")).centerCrop()
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
      uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

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

