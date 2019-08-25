package com.modosa.execcommand;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;

public class ListExecActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String listfilepath = new CommandUtil(this).init();

        ArrayList<String> CommandList = new CommandUtil(this).readCommand(listfilepath);

        new CommandUtil(this).execCommand(CommandList, listfilepath);

        finish();

    }

}

