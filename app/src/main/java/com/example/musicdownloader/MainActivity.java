package com.example.musicdownloader;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MUSIC;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_WRITE_STORAGE = 140;
    //public  String storeDir = getFilesDir().getAbsolutePath()+"/Music";
    public String storeDir;
    public static final int URL_LENGTH_LIMIT = 200;
    public static final String DEFAULT_ERROR_MESSAGE = "Download failed, verify your input or network";
    Button dbutton;
    TextView status;
    TextView dropPath;
    EditText urlText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = (TextView) findViewById(R.id.txtStatus);
        dropPath = (TextView) findViewById(R.id.drop_path);
        dbutton = (Button) findViewById(R.id.dbutton);
        urlText = (EditText) findViewById(R.id.txtUrl);
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }
        Python py = Python.getInstance();
        PyObject obj = py.getModule("youtubeimporter");
        storeDir = /*getApplicationContext().getExternalFilesDir(DIRECTORY_MUSIC).*/
         Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();//+"/youtube_music";

        dbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String youtube_link = urlText.getText().toString();//"https://www.youtube.com/watch?v=Sy2poz1__14";
                String s= "";
                if(validateModel((youtube_link)))
                    s= obj.callAttr("main", youtube_link,storeDir).toString();

                String path = s.contains("successfully") ? "Check in "+storeDir : DEFAULT_ERROR_MESSAGE;
                status.setText(s);
                dropPath.setText(path);
                Log.v("EditText", youtube_link);
    }
        });
    }
    public boolean validateModel(String in){
        if(!URLUtil.isValidUrl(in) || !(in != null && !in.trim().isEmpty()) || in.length()>URL_LENGTH_LIMIT)
            return false;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "The app was allowed to write to your storage!", Toast.LENGTH_LONG).show();
                    // Reload the activity with permission granted or use the features what required the permission
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void requestPermission() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            // You are allowed to write external storage:
            File storageDir = new File(getExternalFilesDir(DIRECTORY_MUSIC).getAbsolutePath());

            if (!storageDir.exists() && !storageDir.mkdirs()) {
                String msg = "Failed to find the storage directory and creating the directory.";
                //Store in log first
                Log.e("PATH_NOT_FOUND", msg);
                throw new Resources.NotFoundException(msg);
            }
        }
    }
}
