package com.jucse.doorlockfingerprintauthentication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();
    private TextView displayError;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText address;
    private EditText phoneNumber;
    private RadioGroup radioGroup;
    private boolean loginOption;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private UserObject userdata;
    private String usernameValue;
    private String emailValue;
    private String passwordValue;
    private String addressValue;
    private String phonenumberValue;
    private String Imei;
    private Toolbar toolbar;

    public static final int REQUEST_CODE_PHONE_STATE_READ = 100;
    private int checkedPermission = PackageManager.PERMISSION_DENIED;
// ...
// Initialize Firebase Auth


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Android Fingerprint Registration");
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Registration");

       // setSupportActionBar(toolbar);


        checkedPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= 23 && checkedPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else
            checkedPermission = PackageManager.PERMISSION_GRANTED;

        TelephonyManager manager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        Imei=manager.getDeviceId();



        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        address = (EditText) findViewById(R.id.address);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        databaseReference=FirebaseDatabase.getInstance().getReference("student");
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if (id == R.id.with_fingerprint) {
                    loginOption = false;
                }
                if (id == R.id.with_fingerprint_and_password) {
                    loginOption = true;
                }
            }
        });
        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 usernameValue = username.getText().toString();
                  emailValue = email.getText().toString();
                  passwordValue = password.getText().toString();
                 addressValue = address.getText().toString();
                 phonenumberValue = phoneNumber.getText().toString();
                int selectedButtonId = radioGroup.getCheckedRadioButtonId();
                if (TextUtils.isEmpty(usernameValue) || TextUtils.isEmpty(emailValue) || TextUtils.isEmpty(passwordValue)
                        || TextUtils.isEmpty(addressValue) || TextUtils.isEmpty(phonenumberValue)) {
                    Toast.makeText(SignUpActivity.this, "All input fields must be filled", Toast.LENGTH_LONG).show();
                } else if (selectedButtonId == -1) {
                    Toast.makeText(SignUpActivity.this, "Login option must be selected", Toast.LENGTH_LONG).show();
                } else {

                    mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(emailValue, passwordValue)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        UserObject userData = new UserObject(usernameValue, emailValue, passwordValue, addressValue, phonenumberValue, loginOption);
                                       // Toast.makeText(SignUpActivity.this,"lalala",Toast.LENGTH_LONG).show();
                                        FirebaseDatabase.getInstance().getReference("user")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(SignUpActivity.this,"Regestration Complete",Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(SignUpActivity.this, registrationNext.class);
                                                intent.putExtra("imei",Imei);
                                                startActivity(intent);
                                            }
                                        });
                                    } else {
                                            Toast.makeText(SignUpActivity.this,"Registration Failed",Toast.LENGTH_LONG).show();
                                    }

                                    // ...
                                }
                            });

                  /*  Gson gson = ((CustomApplication) getApplication()).getGsonObject();
                    UserObject userData = new UserObject(usernameValue, emailValue, passwordValue, addressValue, phonenumberValue, loginOption);
                    String userDataString = gson.toJson(userData);
                    CustomSharedPreference pref = ((CustomApplication) getApplication()).getShared();
                    pref.setUserData(userDataString);
                    username.setText("");
                    email.setText("");
                    password.setText("");
                    address.setText("");
                    phoneNumber.setText("");
                    Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(loginIntent);*/
                }
            }
        });
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_PHONE_STATE_READ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkedPermission = PackageManager.PERMISSION_GRANTED;
                }
                break;

        }
    }
    private void requestPermission(){
        Toast.makeText(this, "Requesting permission", Toast.LENGTH_SHORT).show();
        this.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_CODE_PHONE_STATE_READ);
    }

}