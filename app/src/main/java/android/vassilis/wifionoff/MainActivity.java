package android.vassilis.wifionoff;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {

    ImageButton wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifi = (ImageButton) findViewById(R.id.wifi_button);
    }

    public void changeWifiState(View view) {
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        boolean wifiState = manager.isWifiEnabled();

        manager.setWifiEnabled(!wifiState);

        if (!wifiState) {
            Toast.makeText(this.getApplicationContext(), "Wifi turned on!", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this.getApplicationContext(), "Wifi turned off!", Toast.LENGTH_LONG).show();
        }
    }
}
