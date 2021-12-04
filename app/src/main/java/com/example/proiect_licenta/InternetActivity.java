package com.example.proiect_licenta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class InternetActivity extends AppCompatActivity {


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
        setContentView(R.layout.activity_internet);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Verificare conexiune internet
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
                            if(!checkNet) {
                                //Nu este conectat la internet
                                intent = new Intent(InternetActivity.this, InternetActivity.class);
                            }
                            else {
                                //Conectat la internet
                                intent = new Intent(InternetActivity.this, MapsActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        }
                    }
                };
                timer.start();
            }
        });

    }
}