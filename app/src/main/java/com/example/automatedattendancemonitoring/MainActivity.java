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

    /**
     * This method transliterates cyrillic string to latin
     * Taken from https://stackoverflow.com/questions/16273318/transliteration-from-cyrillic-to-latin-icu4j-java
     * and slightly modificated
     *
     * @param str cyrillic/latin string to transliterate to latin
     * @return transliterated latin string
     */
    private static String transliterate(String str) {
        char[] abcCyr = {' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String[] abcLat = {" ", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "yu", "ya", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Yu", "Ya", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++) {
                if (str.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
            }
        }
        return builder.toString();
    }

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
                if (isTeacher) {
                    nextActivityIntent.putExtra("googleId", account.getId());
                } else {
                    nextActivityIntent.putExtra("fullname", transliterate(account.getGivenName() + " " + account.getFamilyName()));
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
