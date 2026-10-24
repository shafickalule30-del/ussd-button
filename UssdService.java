package com.you.ussdbutton;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;

public class UssdService extends Service {
    private static final String CH = "ussd";

    @Override public IBinder onBind(Intent i) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, notif("Dialing *165#..."));

        final String code = (intent != null && intent.getStringExtra("code") != null)
                ? intent.getStringExtra("code") : "*165#";

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        try {
            tm.sendUssdRequest(code, new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager t, String req, CharSequence resp) {
                    try {
                        FileWriter fw = new FileWriter(new File("/sdcard/Download/ussd_last.txt"));
                        fw.write(req + "\n" + resp);
                        fw.close();
                    } catch (Exception ignored) {}
                    notify2("MTN: " + resp);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        public void run() { stopSelf(); }
                    }, 8000);
                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager t, String req, int code) {
                    notify2("USSD failed (code " + code + ")");
                    Log.e("USSD", "Failed: " + code);
                    stopSelf();
                }
            }, new Handler(Looper.getMainLooper()));
        } catch (SecurityException e) {
            notify2("Missing CALL_PHONE permission");
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private Notification notif(String text) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (mgr.getNotificationChannel(CH) == null) {
                mgr.createNotificationChannel(
                    new NotificationChannel(CH, "USSD", NotificationManager.IMPORTANCE_LOW));
            }
        }
        return new Notification.Builder(this, CH)
                .setContentTitle("MTN USSD")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setOngoing(true)
                .build();
    }

    private void notify2(String text) {
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.notify(1, notif(text));
    }
}