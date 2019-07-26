package com.jucse.doorlockfingerprintauthentication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

public class FingerprintActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = LoginActivity.class.getSimpleName();
  //  private FingerprintManager fingerprintManager;
 //   private KeyguardManager keyguardManager;
   // private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private TextView mHeadingLabel;
    private ImageView mFingerprintImage;
    private TextView mParaLabel;
    //private IMEIdetect imeidetect;
    private Button Signout;

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    private KeyStore keyStore;

  //  private Cipher cipher;
    private String KEY_NAME = "AndroidKey";
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
   // private FingerprintHandler fingerprintHandler;
    private static final String FINGERPRINT_KEY = "key_name";
    private static final int REQUEST_USE_FINGERPRINT = 300;
    protected static Gson mGson;
    protected static CustomSharedPreference mPref;
    private static UserObject mUser;
    private static String userString;
    public static final int REQUEST_CODE_PHONE_STATE_READ = 100;
    private int checkedPermission = PackageManager.PERMISSION_DENIED;
    private FirebaseAuth firebaseAuth;
    private String Imei;

    private ClientHandler clientHandler;
    private ClientThread clientThread;


    private Button buttonConnect, buttonDisconnect;
    private TextView textViewstate;

    //setting address and port number
    private String addr;
    private String port;
    private SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        checkedPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= 23 && checkedPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else
            checkedPermission = PackageManager.PERMISSION_GRANTED;

        TelephonyManager manager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        Imei=manager.getDeviceId();

        //mHeadingLabel = (TextView) findViewById(R.id.headingLabel);
        mFingerprintImage = (ImageView) findViewById(R.id.fingerprint_image);
        mParaLabel = (TextView) findViewById(R.id.paraLabel);
        Signout=(Button)findViewById(R.id.signout);

        buttonConnect=(Button)findViewById(R.id.buttonConnect);
        buttonDisconnect=(Button)findViewById(R.id.buttonDisconnect);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonDisconnect.setOnClickListener(buttonDisConnectOnClickListener);

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        textViewstate=(TextView)findViewById(R.id.status);

        addr="192.172.2.101";
        port="8000";
      //  buttonSend.setOnClickListener(buttonSendOnClickListener);

        clientHandler = new ClientHandler(this);

        firebaseAuth= FirebaseAuth.getInstance();
        Signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Toast.makeText(FingerprintActivity.this,"Logged out!",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(FingerprintActivity.this,MainActivity.class);

                startActivity(intent);
            }
        });




        // Check 1: Android version should be greater or equal to Marshmallow
        // Check 2: Device has Fingerprint Scanner
        // Check 3: Have permission to use fingerprint scanner in the app
        // Check 4: Lock screen is secured with atleast 1 type of lock
        // Check 5: Atleast 1 Fingerprint is registered

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            if(!fingerprintManager.isHardwareDetected()){

                mParaLabel.setText("Fingerprint Scanner not detected in Device");

            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){

                mParaLabel.setText("Permission not granted to use Fingerprint Scanner");

            } else if (!keyguardManager.isKeyguardSecure()){

                mParaLabel.setText("Add Lock to your Phone in Settings");

            } else if (!fingerprintManager.hasEnrolledFingerprints()){

                mParaLabel.setText("You should add atleast 1 Fingerprint to use this Feature");

            } else {

                mParaLabel.setText("Place your Finger on Scanner to Access .");

                generateKey();

                if (cipherInit()){

                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    FingerprintHandler fingerprintHandler = new FingerprintHandler(this,Imei);
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject);


                }
            }

        }


    }
    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    clientThread = new ClientThread(
                            addr,
                            Integer.parseInt(port),
                            clientHandler);
                    clientThread.start();

                    buttonConnect.setEnabled(false);
                    buttonDisconnect.setEnabled(true);
                    //buttonSend.setEnabled(true);
                }
            };

    View.OnClickListener buttonDisConnectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(clientThread != null){
                clientThread.setRunning(false);
            }

        }
    };
    public void ToastMessage(){
        Toast.makeText(getApplicationContext(),"status Updated",Toast.LENGTH_SHORT).show();
        Toast.makeText(this,"IMEI is is sent to firebase",Toast.LENGTH_LONG).show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }


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
    private void updateState(String state){
        textViewstate.setText(state);
    }

  /*  private void updateRxMsg(String rxmsg){
        textViewRx.append(rxmsg + "\n");
    }*/

    private void clientEnd(){
        clientThread = null;
        textViewstate.setText("clientEnd()");
        buttonConnect.setEnabled(true);
        buttonDisconnect.setEnabled(false);
        //buttonSend.setEnabled(false);

    }

    @Override
    public void onRefresh() {
        finish();
        startActivity(getIntent());
    }

    public static class ClientHandler extends Handler {
        public static final int UPDATE_STATE = 0;
        public static final int UPDATE_MSG = 1;
        public static final int UPDATE_END = 2;
        private FingerprintActivity parent;

        public ClientHandler(FingerprintActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_STATE:
                    parent.updateState((String)msg.obj);
                    break;
               /* case UPDATE_MSG:
                    parent.updateRxMsg((String)msg.obj);
                    break;*/
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }

    }
}



