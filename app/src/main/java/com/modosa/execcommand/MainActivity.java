package com.modosa.execcommand;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.modosa.exec.command.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<String> CommandList = new ArrayList<>();
    private String path;
    private boolean isfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        System.out.println(intent);
        String action = intent.getAction();
        boolean isedit = "modosa.execcommand.EXTRA_TEXT".equals(action);


        if (Intent.ACTION_MAIN.equals(action) || isedit) {
            setContentView(R.layout.activity_main);

            ImageView imageView = findViewById(R.id.imageView);
            EditText editText = findViewById(R.id.command);

//            editText.setFocusable(true);
//            editText.setFocusableInTouchMode(true);
            editText.requestFocus();//请求焦点
            editText.findFocus();//获取焦点
            if (isedit) {
                setTitle(R.string.edit_command);
                String cmd = intent.getStringExtra(Intent.EXTRA_TEXT) + "";
                Log.d("modosa text is", cmd);
                editText.setText(cmd);
            }

            imageView.setOnClickListener(view -> {
                CommandList.add(editText.getText().toString());
                execCommand(CommandList);

            });

        } else if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            String cmd = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) + "";
            Log.d("process text  is", cmd);
            CommandList.add(cmd);
            execCommand(CommandList);
        } else {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                Object object = bundle.get(Intent.EXTRA_TEXT);
                if (object != null) {
                    String cmd = object + "";
                    Log.d("the text read is", cmd);
                    CommandList.add(cmd);
                    execCommand(CommandList);
                } else {
                    requestWindowFeature(Window.FEATURE_NO_TITLE);
                    path = bundle.get(Intent.EXTRA_STREAM) + "";
                    if (!"".equals(path)) {
                        if (path.contains("file://")) {
                            isfile = true;
                            try {
                                path = URLDecoder.decode(path, "utf-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            parseFile(path);
                        } else if (path.contains("content://")) {
                            parseContent(path);
                        } else {
                            showToast(getString(R.string.cannot_parse), 0);
                            finish();
                        }
                    }
                }
            } else {
                showToast(getString(R.string.cannot_parse), 1);
                finish();
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (isfile) {
            parseFile(path);
        }

    }

    private void parseFile(String filepath) {

        boolean check = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == 0);
//        boolean check1 = isPermissionGranted();

//        boolean check3 = check && check1;
        System.out.println(check + " storage permissions");
//        System.out.println(check1 + " AppOps storage permissions");
        if (check) {
            CommandList = readCommand(filepath.replace("file://", ""));
            execCommand(CommandList);
        } else {
            showToast(getString(R.string.need_permission), 0);
            requestPermission();
        }


    }

    private void parseContent(String filepath) {
        String newpath1 = null;
        try {
            newpath1 = createTempfile(this, Uri.parse(filepath));
            CommandList = readCommand(newpath1);
            execCommand(CommandList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            deleteSingleFile(newpath1);
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                0x233);
    }

    private void showToast(final String text, int length) {
        if (length == 1) {
            runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
        } else {
            runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
        }
    }


    private void deleteSingleFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("Delete temp file", filePath + "成功！");
            }
        }
    }

    //AppOps check permission
//    private boolean isPermissionGranted() {
//        try {
//            Object object = getSystemService(Context.APP_OPS_SERVICE);
//            if (object == null) {
//                return false;
//            }
//            Class localClass = object.getClass();
//            Class[] arrayOfClass = new Class[3];
//            arrayOfClass[0] = Integer.TYPE;
//            arrayOfClass[1] = Integer.TYPE;
//            arrayOfClass[2] = String.class;
//            Method method = localClass.getMethod("checkOp", arrayOfClass);
//
////            if (method == null) {
////                return false;
////            }
//            Object[] arrayOfObject = new Object[3];
//            arrayOfObject[0] = Integer.valueOf("60");
//            arrayOfObject[1] = Binder.getCallingUid();
//            arrayOfObject[2] = getPackageName();
//            int m = (Integer) Objects.requireNonNull(method.invoke(object, arrayOfObject));
//            return (m == AppOpsManager.MODE_ALLOWED) || (m == AppOpsManager.MODE_FOREGROUND);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    private void execCommand(ArrayList<String> CommandList) {

        if (CommandList.isEmpty()) {
            showToast(getString(R.string.empty_content), 1);
            finish();
        } else {
            String realcmd;
//            for (int i = 0; i < CommandList.size(); i++) {
//                Log.e("第" + (i + 1) + "行是 [", CommandList.get(i) + "],长度为" + CommandList.get(i).length());
//            }

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
                    showToast(getString(R.string.word_execute) + " " + realcmd, 1);
                    dataOutputStream.close();
                    outputStream.close();
                } else {
                    showToast(getString(R.string.empty_content), 1);
                }

            } catch (IOException e2) {
                e2.printStackTrace();
            }
            finish();
        }
    }

    private ArrayList<String> readCommand(String listfilepath) {
        ArrayList<String> CommandList = new ArrayList<>();
        System.out.println(listfilepath);
        File CommandFile = new File(listfilepath);
        try {
            InputStream is = new FileInputStream(CommandFile);
            String strLine;

//            InputStreamReader isr = new InputStreamReader(new FileInputStream(CommandFile), StandardCharsets.UTF_8);
//            BufferedReader bufferedReader = new BufferedReader(isr);

            BufferedReader bufferedReader = new BufferedReader(new FileReader(CommandFile));

            while ((strLine = bufferedReader.readLine()) != null) {
                CommandList.add(strLine);
                //读到的一行
//                System.out.println(strLine);

            }
            Log.d("read from path", CommandList.toString());

            is.close();
            bufferedReader.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        return CommandList;
    }

    private String createTempfile(Context context, Uri uri) {

        Log.e("uri", uri + "");
        File tempFile = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".txt");
        try {

            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                OutputStream fos = new FileOutputStream(tempFile);
                byte[] buf = new byte[4096 * 1024];
                int ret;
                while ((ret = is.read(buf)) != -1) {
                    fos.write(buf, 0, ret);
                    fos.flush();
                }
                fos.close();
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile.getAbsolutePath();
    }

}


//
//    private void showToast(final String text) {
//        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
//    }
//
//
//    private void execCommand(ArrayList<String> CommandList) {
//
//        if (CommandList.isEmpty()) {
//            showToast(getString(R.string.empty_content));
//            finish();
//        } else {
//            String realcmd;
//            StringBuilder CMD = new StringBuilder();
//            try {
//                String temp;
//                for (int i = 0; i < CommandList.size(); i++) {
//                    temp = CommandList.get(i).trim();
//                    if (!"".equals(temp)) {
//                        CMD.append(CommandList.get(i)).append(" && ");
//                    }
//                }
//                temp = CMD.toString();
////                System.out.println(temp + " " + temp.length());
//                if (temp.length() > 4) {
//
//                    int end = CMD.toString().length() - 4;
//                    realcmd = temp.substring(0, end);
//                    Log.e("exec all Command", realcmd);
//                    OutputStream outputStream = Runtime.getRuntime().exec("su").getOutputStream();
//                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
//                    dataOutputStream.writeBytes(realcmd);
//                    dataOutputStream.flush();
//                    showToast(getString(R.string.word_execute) + " " + realcmd);
//                    dataOutputStream.close();
//                    outputStream.close();
//                } else {
//                    showToast(getString(R.string.empty_content));
//                }
//
//            } catch (IOException e2) {
//                e2.printStackTrace();
//            }
//            finish();
//        }
//    }
//}
