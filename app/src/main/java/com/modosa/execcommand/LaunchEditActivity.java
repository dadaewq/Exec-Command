package com.modosa.execcommand;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.modosa.exec.command.R;

public class LaunchEditActivity extends Activity {

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
                    Toast.makeText(this, getString(R.string.cannot_edit), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.d("edit text send", cmd);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (cmd != null) {
            intent.setAction("modosa.execcommand.EXTRA_TEXT");
            intent.setComponent(new ComponentName(getPackageName(), getPackageName() + ".MainActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
