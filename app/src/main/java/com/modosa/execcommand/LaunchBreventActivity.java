package com.modosa.execcommand;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class LaunchBreventActivity extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String cmd = null;
        Intent intent = getIntent();
        System.out.println(intent);
        if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            cmd = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) + "";
            Log.d("edit PROCESS_TEXT", cmd);
            intent.putExtra(Intent.EXTRA_TEXT, cmd);

        } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            try {
                cmd = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (cmd == null) {
                    finish();
                } else {
                    Log.d("edit text send", cmd);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (cmd != null) {
            try {
                intent.setAction("me.piebridge.brevent.intent.action.COMMAND");
                intent.setComponent(new ComponentName("me.piebridge.brevent" ,"me.piebridge.brevent.ui.BreventCmd"));
                intent.putExtra("me.piebridge.brevent.intent.extra.COMMAND",cmd);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
