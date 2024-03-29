package com.modosa.execcommand;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Objects;

class CommandUtil {

    private final Context context;
    private String realcmd;
    private int meta_Length_CommandInToast;
    private boolean meta_isLimitCommandInToast;

    CommandUtil(Context context) {
        this.context = context;
    }

    public String init() {


        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        Objects.requireNonNull(appInfo);

        String meta_AssetsFilename = appInfo.metaData.getString("AssetsFilename");
        String meta_ExternalFilename = appInfo.metaData.getString("ExternalFilename");

        meta_isLimitCommandInToast = appInfo.metaData.getBoolean("isLimitCommandInToast");
        if (meta_isLimitCommandInToast) {
            meta_Length_CommandInToast = appInfo.metaData.getInt("Length_CommandInToast");
        }

//        Log.e("AssetsFilename", meta_AssetsFilename );
//        Log.e("ExternalFilename", meta_ExternalFilename );

        return checkExternalCommandFile(meta_AssetsFilename, meta_ExternalFilename);

    }

    private void showToast(final String text, int length) {
        if (length == 1) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    public void execCommand(ArrayList<String> CommandList, String listfilepath) {
        boolean isfailed = CommandList.toString().contains("读取assets文件失败") || CommandList.toString().contains("Failed to read Assets file");
        if (CommandList.isEmpty()) {
            showToast(context.getString(R.string.empty_content), 1);
        } else if (isfailed) {
            showToast(context.getString(R.string.failed_readassets) + "\n" + new File(listfilepath).getAbsolutePath(), 1);
        } else {

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
                    showcmdToast();
                    dataOutputStream.close();
                    outputStream.close();
                } else {
                    showToast(context.getString(R.string.empty_content), 1);
                }


            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public ArrayList<String> readCommand(String listfilepath) {
        ArrayList<String> CommandList = new ArrayList<>();
        File CommandFile = new File(listfilepath);
        try {
            InputStream is = new FileInputStream(CommandFile);
            String strLine;

//            DataInputStream dataIO = new DataInputStream(is);
//            BufferedReader bufferedReader1=new BufferedReader(new InputStreamReader(new FileInputStream(CommandFile)));

//            long startTime=System.nanoTime(); //获取开始时间
//            doSomeThing(); //测试的代码段
//            long endTime=System.nanoTime(); //获取结束时间
//            System.out.println("程序运行时间： "+(endTime-startTime)+"ns");


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

    private void showcmdToast() {
        int totallength = realcmd.length();
        if (meta_isLimitCommandInToast) {
            if (meta_Length_CommandInToast == 0) {
                showToast(context.getString(R.string.word_executing), 0);
            } else if (meta_Length_CommandInToast < 0) {
                showToast("Toast中显示的指令长度设置错误", 0);
            } else {
                if (totallength > meta_Length_CommandInToast) {
                    showToast(context.getString(R.string.word_execute) + " " + realcmd.substring(0, meta_Length_CommandInToast) + "...", 1);
                } else {
                    showToast(context.getString(R.string.word_execute) + " " + realcmd, 1);
                }
            }
        } else {
            showToast(context.getString(R.string.word_execute) + " " + realcmd, 1);
        }

    }

    private String checkExternalCommandFile(String AssetsFilename, String ExternalFilename) {

        File ExternalCommandFile = new File(context.getExternalFilesDir(""), ExternalFilename);
        if (!ExternalCommandFile.exists()) {
            System.out.println("ExternalCommandFile is not exsit start creating");
            try {
                InputStream is = context.getResources().getAssets().open(AssetsFilename);
                FileOutputStream fos = new FileOutputStream(ExternalCommandFile);

                byte[] b = new byte[1024];

                int length;
                while ((length = is.read(b)) > 0) {
                    fos.write(b, 0, length);
                }
                is.close();
                fos.close();

            } catch (IOException e2) {
                e2.printStackTrace();
                try {
                    FileOutputStream fos = new FileOutputStream(ExternalCommandFile);
                    fos.write(context.getString(R.string.failed_readassets).getBytes());
                    showToast(context.getString(R.string.failed_readassets) + "\n" + ExternalCommandFile.getAbsolutePath(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("ExternalCommandFile is exsit");
        }
        return ExternalCommandFile.getPath();
    }

}


//    public String crratexmlfile() {
//
//        File xmlfile = new File(getApplicationContext().getExternalFilesDir(""), "app.xml");
//        if (!xmlfile.exists()) {
//            System.out.println("不存在 开始创建");
//            try {
//                InputStream is = getResources().getAssets().open("app.xml");
//                FileOutputStream fos = new FileOutputStream(xmlfile);
//
//                byte[] b = new byte[1024];
//
//                int length;
//                while ((length = is.read(b)) > 0) {
//                    fos.write(b, 0, length);
//                }
//                is.close();
//                fos.close();
//
//            } catch (IOException e2) {
//                e2.printStackTrace();
//            }
//        }else {
//            System.out.println("已存在");
//        }
//        return xmlfile.getPath();
//    }


//    public void creatXml() {
//        try {
//            OutputStream outputStream = Runtime.getRuntime().exec("su").getOutputStream();
//
//            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
//            dataOutputStream.writeBytes("am force-stop com.guoshi.httpcanary&&"+cmd1+"&&"+cmd2);
//            dataOutputStream.flush();
//            System.out.println(cmd1);
//            dataOutputStream.close();
//            System.out.println("am force-stop com.guoshi.httpcanary &&"+cmd1+"&&"+cmd2);
////            DataOutputStream dataOutputStream1 = new DataOutputStream(outputStream);
////            dataOutputStream1.writeBytes(cmd2);
////            dataOutputStream1.flush();
//            System.out.println(cmd2);
////            dataOutputStream1.close();
//
//            outputStream.close();
//        } catch (IOException e2) {
//            e2.printStackTrace();
//        }
//        finish();
//
//    }



