package com.byteshaft.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DetailsActivity extends AppCompatActivity {

    EditText etIpAddress;
    EditText etRealm;
    Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        etIpAddress = findViewById(R.id.ip_address);
        etRealm = findViewById(R.id.realm);
        etIpAddress.setText(AppGlobals.getStringFromSharedPreferences("ip"));
        etRealm.setText(AppGlobals.getStringFromSharedPreferences("realm"));
        buttonSave = findViewById(R.id.button_save);


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppGlobals.firstTimeLaunch(true);
                String address = etIpAddress.getText().toString();
                String realm = etRealm.getText().toString();
                System.out.println("click");
                AppGlobals.saveDataToSharedPreferences("ip", address);
                AppGlobals.saveDataToSharedPreferences("realm", realm);
                startActivity(new Intent(DetailsActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
