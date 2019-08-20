package com.modosa.execcommand;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.modosa.exec.command.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ProcessTextActivity extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> CommandList = new ArrayList<>();
        if ("android.intent.action.PROCESS_TEXT".equals(getIntent().getAction())) {
            String cmd = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) + "";
            Log.d("the text read is", cmd);
            CommandList.add(cmd);
            execCommand(CommandList);
        }
    }

    private void showToast(final String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }


    private void execCommand(ArrayList<String> CommandList) {

        if (CommandList.isEmpty()) {
            showToast(getString(R.string.empty_content));
            finish();
        } else {
            String realcmd;
            StringBuilder CMD = new StringBuilder();
            try {
                String temp;
                for (int i = 0; i < CommandList.size(); i++) {
                    temp = CommandList.get(i).trim();
                    if (!"".equals(temp)) {
                        CMD.append(CommandList.get(i)).append(" && ");
                    }
                }
                temp = CMD.toString();
//                System.out.println(temp + " " + temp.length());
                if (temp.length() > 4) {

                    int end = CMD.toString().length() - 4;
                    realcmd = temp.substring(0, end);
                    Log.e("exec all Command", realcmd);
                    OutputStream outputStream = Runtime.getRuntime().exec("su").getOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                    dataOutputStream.writeBytes(realcmd);
                    dataOutputStream.flush();
                    showToast(getString(R.string.word_execute) + " " + realcmd);
                    dataOutputStream.close();
                    outputStream.close();
                } else {
                    showToast(getString(R.string.empty_content));
                }

            } catch (IOException e2) {
                e2.printStackTrace();
            }
            finish();
        }
    }
}
