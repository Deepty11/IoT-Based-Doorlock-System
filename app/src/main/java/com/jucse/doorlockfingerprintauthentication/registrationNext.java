package com.jucse.doorlockfingerprintauthentication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class registrationNext extends AppCompatActivity {
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_next);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Registration");

        setSupportActionBar(toolbar);

        Intent intent=getIntent();
        final String imei=intent.getStringExtra("imei");
       // TextView textView=(TextView)findViewById(R.id.showImei);


        TextView textView1=(TextView)findViewById(R.id.Imei);
        textView1.setText("IMEI:"+imei);
        //textView1.setText(" Register your IMEI using the keypad click 'Continue' to proceed ");
        Button button=(Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(registrationNext.this,"Registration Successful!",Toast.LENGTH_LONG).show();
                Intent intent1=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent1);
            }
        });
    }
}
