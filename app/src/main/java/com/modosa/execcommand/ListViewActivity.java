package com.modosa.execcommand;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;

import java.io.File;

public class ListViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //Method
        //builder.detectFileUriExposure();


        String listfilepath = new CommandUtil(this).init();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

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

        startActivity(intent);

        finish();
    }

}
