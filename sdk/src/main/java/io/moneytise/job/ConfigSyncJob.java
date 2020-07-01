package io.moneytise.job;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.moneytise.Moneytiser;
import io.moneytise.ThreeProxy;
import io.moneytise.service.HttpManager;
import io.moneytise.support.ConfigManager;
import io.moneytise.task.ProxyAsyncTask;
import io.moneytise.util.LogUtils;

public class ConfigSyncJob implements Runnable {

    public static final String TAG = ConfigSyncJob.class.getSimpleName();

    private Context context;

    private ConfigManager confManager;

    private HttpManager httpManager;

    private ProxyAsyncTask proxyTask;

    private List<Throwable> errors;

    private Handler handler = new Handler();

    private PowerManager.WakeLock wakeLock;

    private long retryDelay = 2000;

    private int maxRetries = 15;

    private int requestsCounts = 0;

    private int failedAttempts = 0;

    private String uid;

    public ConfigSyncJob(Context ctx, PowerManager.WakeLock wl) {
        Moneytiser acp = Moneytiser.getInstance(ctx);
        context = ctx;
        wakeLock = wl;
        confManager = acp.getConfigManager();
        httpManager = acp.getHttpManager();
        errors = new ArrayList<>(maxRetries);
    }

    public void schedule(String userId) {
        if (proxyTask == null || !proxyTask.isRunning()) {
            uid = userId;
            handler.removeCallbacks(this);
            handler.post(this);
            LogUtils.d(TAG, "Scheduled configuration synchronization job");
        } else {
            LogUtils.w(TAG, "The 3proxy task already running, cannot reschedule a new one");
        }
    }

    @Override
    public void run() {
        Moneytiser acp = Moneytiser.getInstance(context);
        long delayMillis = acp.getDelayMillis() - SystemClock.elapsedRealtime() % 1000;
        handler.postDelayed(this, delayMillis);
        requestsCounts++;
        wakeLock.acquire(delayMillis);
        // request a string response from the provided URL.
        String pub = acp.getPublisher();
        String usr = uid;
        String baseUrl = acp.getBaseUrl();
        String getEndpoint = acp.getGetEndpoint();
        if (!baseUrl.endsWith("/") && !getEndpoint.startsWith("/")) {
            baseUrl += "/";
        }
        // Request a string response from the provided URL.
        String url = baseUrl + getEndpoint.replace(Moneytiser.PUBLISHER_PLACE_HOLDER, pub).replace(Moneytiser.UID_PLACE_HOLDER, usr);
        LogUtils.d(TAG, "Updating 3proxy configuration calling url: %s", url);
        Intent intent = new Intent(Moneytiser.class.getCanonicalName());
        intent.putExtra(Moneytiser.EVENT, Moneytiser.Events.GET_CONFIG);
        intent.putExtra("requestedUrl", url);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        failedAttempts = 0;
                        LogUtils.d(TAG, "New configuration directive: %s", response);
                        File file = confManager.writeToFile(response);
                        if (proxyTask != null) {
                            LogUtils.d(TAG, "Proxy task is running, try to reload configuration");
                            ThreeProxy.reload();
                        } else {
                            proxyTask = new ProxyAsyncTask();
                            proxyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,file.getAbsolutePath());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        LogUtils.e(TAG, "An error occurred while calling configuration service: %s, %s", error.fillInStackTrace(), error.getMessage(), networkResponse != null ? networkResponse.statusCode : "<none>");
                        failedAttempts++;
                        if (errors.size() >= maxRetries) {
                            errors.remove(0);
                        }
                        errors.add(error);
                        handler.removeCallbacks(ConfigSyncJob.this);
                        if (failedAttempts >= maxRetries) {
                            LogUtils.d(TAG, "Max retrieves for failed attempts are reached");
                        } else if (failedAttempts > 1) {
                            handler.postDelayed(ConfigSyncJob.this, failedAttempts*retryDelay);
                        } else {
                            handler.post(ConfigSyncJob.this);
                        }
                    }
                }
        );
        httpManager.addToRequestQueue(request);
    }

    public void shutdown() {
        LogUtils.d(TAG, "Shutdown configuration synchronization job");
        if(wakeLock.isHeld()){
            wakeLock.release();
            }
        handler.removeCallbacks(this);
        if (proxyTask != null) {
            ThreeProxy.stop();
            proxyTask.cancel(true);
        }
    }

    public int getRequestsCounts() {
        return requestsCounts;
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isRunning() {
        return proxyTask != null && proxyTask.isRunning();
    }

    public long getUpTime(TimeUnit unit) {
        return proxyTask != null ? proxyTask.getUpTime(unit) : 0;
    }

}