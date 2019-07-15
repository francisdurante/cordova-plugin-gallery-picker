package cordova.plugin.gallery.picker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

  private FakeR faker;
  private Context context = this;
  private static final int REQUEST_WRITE_PERMISSION = 411;

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED
        && requestCode == REQUEST_WRITE_PERMISSION && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
      loadFragment(new SlideShowFragment());
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION);
    } else {
      loadFragment(new SlideShowFragment());
    }
  }

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
    = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getTitle().toString()) {
          case "Slide Show":
            fragment = new SlideShowFragment();
            break;

          case "Video":
            fragment = new VideoFragment();
            break;

          case "Pending":
            //Pending fragment
            break;
        }

        return loadFragment(fragment);
      }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    faker = new FakeR(this);
    Intent intent = getIntent();
    setContentView(faker.getId("layout","activity_main_gallery"));
    requestPermission();
    BottomNavigationView navigation = (BottomNavigationView) findViewById(faker.getId("id","navigation"));
    if(intent.getStringExtra("PROCESS").equals("comment"))
    {
      Menu nav_Menu = navigation.getMenu();
      nav_Menu.findItem(faker.getId("id","action_pending")).setVisible(false);
    }
    else if(intent.getStringExtra("PROCESS").equals("add_photo"))
    {
      Menu nav_Menu = navigation.getMenu();
      nav_Menu.findItem(faker.getId("id","action_pending")).setVisible(false);
      nav_Menu.findItem(faker.getId("id","action_video")).setVisible(false);
    }
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
  }

  private boolean loadFragment(Fragment fragment) {
    //switching fragment
    if (fragment != null) {
      getSupportFragmentManager()
        .beginTransaction()
        .replace(faker.getId("id","fragment_container"), fragment)
        .commit();
      return true;
    }
    return false;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Fragment fragment = getSupportFragmentManager().findFragmentById(faker.getId("id", "fragment_container"));
    fragment.onActivityResult(requestCode, resultCode, data);
  }
}
