package com.example.automatedattendancemonitoring;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    // Checking Bluetooth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BluetoothHelper.ADAPTER == null) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.no_bluetooth_title)
                    .setMessage(R.string.no_bluetooth_message)
                    .setOnDismissListener(dialog -> finish())
                    .show();
        }
    }


    // Google Sign In

    private static final int REQUEST_SIGN_IN = 921;

    public void trySignIn(View v) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_SIGN_IN);
        findViewById(R.id.userTypeSwitch).setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null) {
                    Toast.makeText(MainActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                    return;
                }

                // Signed in, starting corresponding activity
                boolean isTeacher = ((Switch) findViewById(R.id.userTypeSwitch)).isChecked();
                Class<? extends Activity> nextActivity = isTeacher ? TeacherActivity.class : StudentActivity.class;
                Intent nextActivityIntent = new Intent(MainActivity.this, nextActivity);
                if (!isTeacher) {
                    String fullname = account.getGivenName() + " " + account.getFamilyName();
                    nextActivityIntent.putExtra("fullname", fullname);
                }
                startActivity(nextActivityIntent);
            } catch (ApiException e) {
                Log.w("OAuth", "sign in failed: " + e.getStatusCode());
                findViewById(R.id.userTypeSwitch).setEnabled(true);
                Toast.makeText(MainActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
            }
            findViewById(R.id.userTypeSwitch).setEnabled(true);
        }
    }
}
