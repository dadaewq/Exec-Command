package com.modosa.execcommand;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.modosa.exec.command.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class LaunchShareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApplicationInfo appInfo = null;
        try {
            appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        Objects.requireNonNull(appInfo);

        String meta_AssetsFilename = appInfo.metaData.getString("AssetsFilename");
        String meta_ExternalFilename = appInfo.metaData.getString("ExternalFilename");

        String listfilepath = checkExternalCommandFile(meta_AssetsFilename, meta_ExternalFilename);

        ArrayList<String> CommandList = readCommand(listfilepath);


        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");


        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_TEXT, CommandList.toString().replace("[", "").replace("]", ""));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        System.out.println(intent.getData());

        startActivity(intent);

        finish();
    }

    private void showToast(final String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
    }

    private String checkExternalCommandFile(String AssetsFilename, String ExternalFilename) {

        File ExternalCommandFile = new File(getApplicationContext().getExternalFilesDir(""), ExternalFilename);
        if (!ExternalCommandFile.exists()) {
            System.out.println("ExternalCommandFile is not exsit start creating");
            try {
                InputStream is = getResources().getAssets().open(AssetsFilename);
                System.out.println("hello");
                FileOutputStream fos = new FileOutputStream(ExternalCommandFile);

                byte[] b = new byte[1024];

                int length;
                while ((length = is.read(b)) > 0) {
                    fos.write(b, 0, length);
                }
                is.close();
                fos.close();

            } catch (IOException e2) {
//                e2.printStackTrace();
                try {
                    FileOutputStream fos = new FileOutputStream(ExternalCommandFile);
                    fos.write(getString(R.string.failed_readassets).getBytes());
                    showToast(getString(R.string.failed_readassets) + "\n" + ExternalCommandFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finish();
            }
        } else {
            System.out.println("ExternalCommandFile is exsit");
        }
        return ExternalCommandFile.getPath();
    }

    private ArrayList<String> readCommand(String listfilepath) {
        ArrayList<String> CommandList = new ArrayList<>();
        File CommandFile = new File(listfilepath);
        try {
            InputStream is = new FileInputStream(CommandFile);
            String strLine;

            BufferedReader bufferedReader = new BufferedReader(new FileReader(CommandFile));
            while ((strLine = bufferedReader.readLine()) != null) {
                CommandList.add(strLine);
            }
            System.out.println("cmd:" + CommandList.toString());

            is.close();
            bufferedReader.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        return CommandList;
    }
}
