package com.modosa.execcommand;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import com.modosa.exec.command.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class LaunchEditActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //Method
        //builder.detectFileUriExposure();

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

        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");

        /*
         *
         * Seems that fromFile() uses A file pointer, which I suppose could be insecure when memory addresses are exposed to all apps.
         * But A file path String never hurt anybody, so it works without throwing FileUriExposedException.
         * Tested on API levels 9 to 27! Successfully opens the text file for editing in another app. Does not require FileProvider, nor the Android Support Library at all.
         */


        //Uri uri=Uri.parse(new File(listfilepath).getPath()+"");
        //以上是path android6.0的miui10用不了 雷电5.1部分不可用和魔趣9.0可用

        // Uri uri=Uri.parse("file://"+listfilepath);
        //魔趣9.0不可用，雷电5.1、android6.0的miui10可用上下

        Uri uri = Uri.fromFile(new File(listfilepath));

        intent.setDataAndType(uri, "text/plain");

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
        return ExternalCommandFile.getAbsolutePath();
    }
}
