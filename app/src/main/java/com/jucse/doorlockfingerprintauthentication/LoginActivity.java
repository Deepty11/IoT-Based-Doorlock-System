package com.jucse.doorlockfingerprintauthentication;
import android.app.ProgressDialog;
import android.Manifest;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class LoginActivity extends AppCompatActivity {

    protected static Gson mGson;
    protected static CustomSharedPreference mPref;
    private static UserObject mUser;
    private static String userString;
    public static final int REQUEST_CODE_PHONE_STATE_READ = 100;
    private int checkedPermission = PackageManager.PERMISSION_DENIED;
    private EditText email;
    private EditText password;
    private Button login;
    //private String emailText;
    //private String passwordText;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private IMEIdetect imei;
    private String Imei;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Android  Login");
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login_button);
       /* checkedPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= 23 && checkedPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else
            checkedPermission = PackageManager.PERMISSION_GRANTED;

        TelephonyManager manager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        Imei=manager.getDeviceId();*/

        /*Toast.makeText(this,"IMEI is"+Imei,Toast.LENGTH_LONG).show();
         firebaseDatabase=FirebaseDatabase.getInstance();
         databaseReference=firebaseDatabase.getReference("user");
        imei= new IMEIdetect(Imei);
        databaseReference.child("imei").push().setValue(imei);*/
        //String emailText;
         //String passwordText;
        mAuth= FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        if(mAuth.getCurrentUser()!=null) {
            //that means user is already logged in
            //so close this activity
            finish();
            //and open profile activity
            startActivity(new Intent(this,FingerprintActivity.class));
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText=email.getText().toString();
                String passwordText=password.getText().toString();
                if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
                    Toast.makeText(LoginActivity.this, "All input fields must be filled", Toast.LENGTH_LONG).show();
                }
                else{
                    progressDialog.setMessage("Logging in. Please Wait...");
                    progressDialog.show();
                    //startActivity(new Intent(getApplicationContext(), FingerprintActivity.class));
                    //Toast.makeText(LoginActivity.this,"FAUL",Toast.LENGTH_LONG).show();
                    mAuth.signInWithEmailAndPassword(emailText, passwordText)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                 //Toast.makeText(LoginActivity.this,"FAUL2",Toast.LENGTH_LONG).show();
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Toast.makeText(LoginActivity.this,"authentication STARTS",Toast.LENGTH_LONG).show();
                                    //checking if success
                                    //startActivity(new Intent(getApplicationContext(), FingerprintActivity.class));

                                    if(task.isSuccessful()){

                                        Toast.makeText(LoginActivity.this,"authentication successful",Toast.LENGTH_LONG).show();
                                        finish();
                                        Intent intent=new Intent(getApplicationContext(),FingerprintActivity.class);
                                        //intent.putExtra(mAuth);
                                       // Toast.makeText(LoginActivity.this,"IMEI is"+Imei,Toast.LENGTH_LONG).show();
                                        /*firebaseDatabase=FirebaseDatabase.getInstance();
                                        databaseReference=firebaseDatabase.getReference("user");
                                        imei= new IMEIdetect(Imei);
                                        databaseReference.child("IMEI_NUMBER").push().setValue(imei);*/
                                        startActivity(intent);
                                        Toast.makeText(getApplicationContext(),"Login Successful!",Toast.LENGTH_SHORT).show();
                                       // startActivity(new Intent(getApplicationContext(), FingerprintActivity.class));
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),"Login Unsuccessful!",Toast.LENGTH_SHORT).show();

                                    }
                                    progressDialog.dismiss();

                                }
                            });
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