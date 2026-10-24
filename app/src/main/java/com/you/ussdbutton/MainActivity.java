package com.you.ussdbutton;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
        }

        // Build the UI programmatically (no XML needed)
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(60, 300, 60, 60);

        TextView tv = new TextView(this);
        tv.setText("Tap to send *165# in background");
        tv.setTextSize(18);
        ll.addView(tv);

        Button b = new Button(this);
        b.setText("Send *165#");
        b.setTextSize(20);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, UssdService.class);
                i.putExtra("code", "*165#");
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(i);
                } else {
                    startService(i);
                }
            }
        });
        ll.addView(b);

        setContentView(ll);
    }
}
