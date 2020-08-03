package io.moneytise.demo;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.moneytise.Moneytiser;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Moneytiser moneytiser = new Moneytiser.Builder().withPublisher("dror").loggable().build(this);
        try {
            moneytiser.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
