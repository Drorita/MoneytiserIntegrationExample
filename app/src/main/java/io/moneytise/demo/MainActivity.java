package io.moneytise.demo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.moneytise.Moneytiser;
import io.moneytise.util.TimeUtils;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private Button startButton;

    private Button stopButton;

    private Thread uiRefresher;

    static public int updatei = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


      //  startActivity(new Intent(MainActivity.this, AsyncTaskActivity.class));
      //  startActivity(new Intent(MainActivity.this, TestActivity.class));

      //  final Moneytiser moneytiser = null;
        final Moneytiser moneytiser = new Moneytiser.Builder().withPublisher("dror").loggable().build(this);
/*
        // dror from here
        try {
            moneytiser.start();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
            // do nothing
        }
        stopButton.setEnabled(true);

        startButton = findViewById(R.id.start_service_button);
        startButton.setText(R.string.running);
        startButton.setEnabled(false);
    //    stopButton = findViewById(R.id.stop_service_button);
        stopButton.setEnabled(true);
        uptime();*/

        // dror till here




        startButton = findViewById(R.id.start_service_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    moneytiser.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startButton.setText(R.string.running);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                uptime();
            }
        });
        stopButton = findViewById(R.id.stop_service_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moneytiser.stop();
                startButton.setText(R.string.start);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
        stopButton.setEnabled(false);

        String versionBuilder =
                "SDK: v" + io.moneytise.BuildConfig.VERSION_NAME + "\n" +
                "APP: v" + BuildConfig.VERSION_NAME + "\n";
        TextView version = findViewById(R.id.version);
        version.setText(versionBuilder);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uiRefresher != null) {
            uiRefresher.interrupt();
        }
    }

    private void refreshInfo() {
        uptimeUpdate();
        Moneytiser acp = Moneytiser.getInstance(true);
        if (acp != null && acp.isRunning() && startButton.isEnabled()) {
            startButton.setText(R.string.running);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    private void uptimeUpdate() {
        long upTime = 0;
        int requests = 0;
        int errors = 0;
        Moneytiser acp = Moneytiser.getInstance(true);
        if (acp != null) {
            upTime = acp.getUpTime();
            errors = acp.getErrors().size();
            requests = acp.getRequestsCounts();
        }
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(String.format("%s\n[UpTime: %s]\nRequests: %s\nErrors: %s", stringFromJNI(), TimeUtils.millisToShortDHMS(upTime), requests, errors));

    }

    private void uptime() {
        uiRefresher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!startButton.isEnabled() && !Thread.interrupted()) {
                        Thread.sleep(1000);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uptimeUpdate();
                            }
                        });
                    }
                } catch (InterruptedException ignore) { }
            }
        });
        uiRefresher.start();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


}
