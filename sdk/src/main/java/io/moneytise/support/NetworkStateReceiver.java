package io.moneytise.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.moneytise.event.NetworkStateChanged;
import io.moneytise.job.ConfigSyncJob;
import io.moneytise.util.LogUtils;

public class NetworkStateReceiver extends BroadcastReceiver {
    ConfigSyncJob subscriber = null;
    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        return intentFilter;
    }
    public static final String TAG = NetworkStateReceiver.class.getSimpleName();

    // post event if there is no Internet connection
    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi!=null && wifi.isConnected() || mobile != null && mobile.isConnected()) {
            LogUtils.w(TAG, "reconnect to network");
            if(subscriber != null){
                subscriber.reschedule();
            }
        }

    }

    public void setSubscriber(ConfigSyncJob job)
    {
        subscriber = job;
    }
}
