package android.vassilis.wifionoff;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.appwidget.AppWidgetManager.getInstance;

/**
 * Implementation of App Widget functionality.
 */
public class WifiWidget extends AppWidgetProvider {

    public static String CHANGE_WIFI_STATE = "en_wifi";
    public static String CHANGE_DATA_STATE = "en_data";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wifi_widget);

        //ACTION
        views.setOnClickPendingIntent(R.id.wifi_widget, actionToPerform(context, 1));
        views.setOnClickPendingIntent(R.id.data_widget, actionToPerform(context, 2));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Toast toast = Toast.makeText(context, "Widget Created!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (intent.getAction().equals(CHANGE_WIFI_STATE)) {
            boolean wifiState = manager.isWifiEnabled();

            manager.setWifiEnabled(!wifiState);


            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WifiWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);


            if (!wifiState) {
                Toast.makeText(context, "Wifi turned on!", Toast.LENGTH_LONG).show();
                while (!isConnected(context)) {
                    //Wait to connect
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                WifiInfo connection = manager.getConnectionInfo();
                int signal = manager.calculateSignalLevel(connection.getRssi(), 4);
                updateWidget(context, connection.getSSID(), signal);
            }
            else {
                Toast.makeText(context, "Wifi turned off!", Toast.LENGTH_LONG).show();
                updateWidget(context, "Disconnected", -1);
            }
        }
        else if (intent.getAction().equals(CHANGE_DATA_STATE)) {
            changeDataState(context);
        }
    }

    //Set which action will perform on Widget Button Click
    public static PendingIntent actionToPerform(Context context, int button) {
        Intent intent = new Intent(context, WifiWidget.class);
        PendingIntent pendingIntent = null;

        if (button == 1) {
            intent.setAction(CHANGE_WIFI_STATE);
        }
        else if (button == 2) {
            intent.setAction(CHANGE_DATA_STATE);
        }
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }

    public void updateWidget(Context context, String ssid, int signal) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wifi_widget);

        views.setTextViewText(R.id.connectedNetwork, ssid);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName projectWidget = new ComponentName(context, WifiWidget.class);

        //for wifi updates
        if (signal != -2) {
            if (signal == -1) {
                views.setImageViewResource(R.id.signalStrength, R.drawable.no_signal);
            } else if (signal == 0) {
                views.setImageViewResource(R.id.signalStrength, R.drawable.low_signal);
            } else if (signal == 1) {
                views.setImageViewResource(R.id.signalStrength, R.drawable.signal_1);
            } else if (signal == 2) {
                views.setImageViewResource(R.id.signalStrength, R.drawable.signal_2);
            } else if (signal == 3) {
                views.setImageViewResource(R.id.signalStrength, R.drawable.full_signal);
            }
        }
        //for Data updates
        else {
            views.setImageViewResource(R.id.signalStrength, R.drawable.sim_card);
        }


        appWidgetManager.updateAppWidget(projectWidget, views);
    }

    public void changeDataState(Context context) {
        ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        boolean dataEnabled = getDataConnectionStatus(context);


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
        updateDataWidget(context, dataEnabled);
    }

    public void updateDataWidget(Context context, boolean dataEnabled) {
        if (dataEnabled) {
            updateWidget(context, "Disconnected", -1);
            Toast.makeText(context, "Data Connection turned off!", Toast.LENGTH_LONG).show();
        }
        else {
            updateWidget(context, "Data Connection", -2);
            Toast.makeText(context, "Data Connection turned on!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean getDataConnectionStatus(Context context) {
        boolean dataStatus = false;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

