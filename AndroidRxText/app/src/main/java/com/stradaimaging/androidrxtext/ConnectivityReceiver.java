package com.stradaimaging.androidrxtext;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import io.reactivex.schedulers.Schedulers;

public class ConnectivityReceiver extends BroadcastReceiver {

    public static ConnectivityReceiverListener connectivityReceiverListener;
    private boolean isOnline = false;

    public ConnectivityReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        isOnline();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                boolean isReallyConnected = capabilities != null && isOnline;
                connectivityReceiverListener.onNetworkConnectionChanged(isReallyConnected);
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isReallyConnected = activeNetwork != null && isOnline;
                connectivityReceiverListener.onNetworkConnectionChanged(isReallyConnected);
            }
        }
    }

    @SuppressLint("CheckResult")
    public void isOnline() {
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process process = runtime.exec("system/bin/ping -c 1 8.8.8.8");
//            int exitValue = process.waitFor();
//            return exitValue == 0;
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
        ReactiveNetwork
                .observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .subscribe(connectivity -> {
                    System.out.println(connectivity.toString());
                    isOnline = connectivity;
                });
    }

    public static boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) MyApp
                .getInstance()
                .getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
