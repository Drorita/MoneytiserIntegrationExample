package io.moneytise.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.TypedValue;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.moneytise.Moneytiser;
import io.moneytise.R;
import io.moneytise.data.DataStore;
import io.moneytise.event.NetworkStateChanged;
import io.moneytise.job.ConfigSyncJob;
import io.moneytise.util.LogUtils;

public class MoneytiserService extends VpnService {

    private static final String TAG = MoneytiserService.class.getSimpleName();

    private ConfigSyncJob configSyncJob;

    private HttpManager httpManager;

    private final IBinder binder = new ProxyServiceBinder();

    public class ProxyServiceBinder extends Binder {
        public MoneytiserService getService() {
            return MoneytiserService.this;
        }
    }


   /* private Notification getWaitingNotification() {
        Intent main = new Intent(this, Moneytiser.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, main, PendingIntent.FLAG_UPDATE_CURRENT);

        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, tv, true);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "foreground");
        builder.setSmallIcon(R.drawable.ic_android_notify)
                .setContentIntent(pi)
                .setColor(tv.data)
                .setOngoing(true)
                .setAutoCancel(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setContentTitle("App Notification");
        else
            builder.setContentTitle("App Notification")
                    .setContentText("Doing some work...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setPriority(NotificationCompat.PRIORITY_MIN);

        return builder.build();
    }*/


    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

//        dror temp startForeground(2, getWaitingNotification());
        try {
            Moneytiser instance = Moneytiser.getInstance(this);
            if (instance != null) {
                httpManager = instance.getHttpManager();
                configSyncJob = new ConfigSyncJob(this, pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG));
                LogUtils.d(TAG, "Service was created");
            }
        }
        catch(Exception ex){
            LogUtils.e(TAG, "Failed to getInstance on MoneytiserService onCreate: ", ex);
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

  /*    boolean needToMoveToForeground = intent.getBooleanExtra(Moneytiser.NEED_FOREGROUND_KEY,false);
        if(needToMoveToForeground) {
            startForeground(2, getWaitingNotification());
        }*/
        try {
            DataStore ds = Moneytiser.getInstance(this).getDataStore();
            String uid = ds.get(getString(R.string.moneytiser_uid_key));
            if (uid != null) {
                LogUtils.d(TAG, "The device is already registered");
                configSyncJob.schedule(uid);
            } else {
                register();
            }
        }
        catch(Exception ex)
        {
            LogUtils.e(TAG, "OnStartCommand failed! ");
        }
        finally {
            return Service.START_STICKY;
        }
    }


    /**
     * Method that will be called when someone posts an event NetworkStateChanged.
     *
     * @param event the intercepted event
     */
    public void onNetworkStateChanged(NetworkStateChanged event) {
        if (!event.isInternetConnected()) {
            LogUtils.d(TAG, "Connected to network!");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        LogUtils.d(TAG, "Task removed");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtils.d(TAG, "Detected low memory");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (httpManager != null) {
            httpManager.stop();
        }
        if (configSyncJob != null) {
            configSyncJob.shutdown();
        }

        LogUtils.d(TAG, "Service was stopped");
    }

    public int getRequestsCounts() {
        return configSyncJob != null ? configSyncJob.getRequestsCounts() : 1;
    }

    public List<Throwable> getErrors() {
        return configSyncJob != null ? configSyncJob.getErrors() : new ArrayList<Throwable>();
    }

    public boolean isRunning() {
        return configSyncJob != null && configSyncJob.isRunning();
    }

    public long getProxyUpTime(TimeUnit unit) {
        return configSyncJob != null ? configSyncJob.getUpTime(unit) : 0;
    }

    private void register() {
        final Moneytiser acp = Moneytiser.getInstance(this);
        final String usr = UUID.randomUUID().toString();
        String pub = acp.getPublisher();
        acp.getDataStore().set(getString(R.string.moneytiser_publisher_key), pub);
        String cat = acp.getCategory();
        String baseUrl = acp.getBaseUrl();
        String regEndpoint = acp.getRegEndpoint();
        if (!baseUrl.endsWith("/") && !regEndpoint.startsWith("/")) {
            baseUrl += "/";
        }
        // Request a string response from the provided URL.
        String url = baseUrl + regEndpoint
                .replace(Moneytiser.PUBLISHER_PLACE_HOLDER, pub)
                .replace(Moneytiser.UID_PLACE_HOLDER, usr)
                .replace(Moneytiser.CID_PLACE_HOLDER, cat)
                ;
        LogUtils.d(TAG, "Trying to register the device %s using url %s", usr, url);
        StringRequest request = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    LogUtils.d(TAG, String.format("Device %s successfully registered", usr));
                    acp.getDataStore().set(getString(R.string.moneytiser_uid_key), usr);
                    configSyncJob.schedule(usr);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    LogUtils.e(TAG, "An error occurred while calling registration service:", error.getCause());
                }
            }
        );
        httpManager.addToRequestQueue(request);
    }

}
