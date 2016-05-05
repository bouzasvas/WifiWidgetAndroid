package android.vassilis.wifionoff;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public void changeDataState(View view) {
        ConnectivityManager conman = (ConnectivityManager)  view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Class conmanClass = null;
        try {
            conmanClass = Class.forName(conman.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Field connectivityManagerField = null;
        try {
            connectivityManagerField = conmanClass.getDeclaredField("mService");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        connectivityManagerField.setAccessible(true);
        Object connectivityManager = null;
        try {
            connectivityManager = connectivityManagerField.get(conman);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Class connectivityManagerClass = null;
        try {
            connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //Check if Data Connection is on or off
        boolean dataEnabled = getDataConnectionStatus(view.getContext());


        //Enable or disable Data Connection
        Method setMobileDataEnabledMethod = null;
        try {
            setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        setMobileDataEnabledMethod.setAccessible(true);

        try {
            setMobileDataEnabledMethod.invoke(connectivityManager, !dataEnabled);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        makeDataToast(view.getContext(), dataEnabled);
    }

    public void makeDataToast(Context context, boolean dataEnabled) {
        if (dataEnabled)
            Toast.makeText(context, "Data Connection turned off!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "Data Connection turned on!", Toast.LENGTH_LONG).show();
    }

    public boolean getDataConnectionStatus(Context context) {
        boolean dataStatus = false;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        Class connectivityClass = null;
        try {
            connectivityClass = Class.forName(manager.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method getStatus = null;
        try {
            getStatus = connectivityClass.getDeclaredMethod("getMobileDataEnabled");
            getStatus.setAccessible(true);
            dataStatus = (Boolean) getStatus.invoke(manager);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return dataStatus;
    }
}
