package com.example.proiect_licenta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static boolean checkNet = false;
    Thread timer;
    Thread checkInternet;

    public void checkConnection(){
        ConnectivityManager manager = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        getMainLooper();
        Looper.prepare();
        Toast toast, toast1, toast2;
        if(null!=activeNetwork){
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                //Conexiune Wi-Fi
                checkNet = true;
                //Mesaj
                toast = Toast.makeText(this, "Wifi Enabled", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
                toast.show();
            }
            else if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                //Conexiune mobilă
                checkNet = true;
                //Mesaj
                toast1 = Toast.makeText(this, "Wifi Enabled", Toast.LENGTH_LONG);
                toast1.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
                toast1.show();
            }
            else{
                //Fără conexiune la internet
                checkNet = true;
                //Mesaj
                toast2 = Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG);
                toast2.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
                toast2.show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkInternet = new Thread(){
            @Override
            public void run(){
                    checkConnection();
            }
        };

        checkInternet.start();

        timer = new Thread(){
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(5000);
                    }
                }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    } finally {
                    Intent intent;
                    if(checkNet) {
                        //Conectat la internet
                        intent = new Intent(MainActivity.this, MapsActivity.class);
                    }
                    else {
                        //Nu este conectat la internet
                        intent = new Intent(MainActivity.this, InternetActivity.class);
                    }
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.start();
    }
}