package by.chemerisuk.cordova.firebase;

import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class FirebaseMessagingActivity extends AppCompatActivity {

  // Register the permissions callback, which handles the user's response to the
  // system permissions dialog. Save the return value, an instance of
  // ActivityResultLauncher, as an instance variable.
  private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
      new ActivityResultContracts.RequestPermission(),
      isGranted -> {
        if (isGranted) {
          // Permission is granted. Continue the action or workflow in your
          // app.
        } else {
          // Explain to the user that the feature is unavailable because the
          // feature requires a permission that the user has denied. At the
          // same time, respect the user's decision. Don't link to system
          // settings in an effort to convince the user to change their
          // decision.
        }
      });

  public void launchPermissions() {
    requestPermissionLauncher.launch(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
  }
}
