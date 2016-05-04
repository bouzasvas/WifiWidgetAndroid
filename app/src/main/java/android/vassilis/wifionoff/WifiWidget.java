package android.vassilis.wifionoff;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

import static android.appwidget.AppWidgetManager.getInstance;

/**
 * Implementation of App Widget functionality.
 */
public class WifiWidget extends AppWidgetProvider {

    private static WifiManager WIFI_MANAGER;
    private static WifiInfo WIFI_INFO;

    public static String CHANGE_WIFI_STATE = "en_wifi";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wifi_widget);

        //ACTION
        Intent intent = new Intent(context, WifiWidget.class);
        intent.setAction(CHANGE_WIFI_STATE);

        WIFI_MANAGER = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WIFI_INFO = WIFI_MANAGER.getConnectionInfo();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.wifi_widget, pendingIntent);
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

        boolean wifiState = false;

        if (intent.getAction().equals(CHANGE_WIFI_STATE)) {

            try {
                wifiState = WIFI_MANAGER.isWifiEnabled();
            }
            catch (NullPointerException ex) {
                System.exit(-1);
            }

            WIFI_MANAGER.setWifiEnabled(!wifiState);

            if (!wifiState) {
                Toast.makeText(context, "Wifi turned on!", Toast.LENGTH_LONG).show();
                int wifiSignal = WIFI_MANAGER.calculateSignalLevel(WIFI_INFO.getRssi(), 4);
                updateWidget(context, WIFI_INFO.getSSID(), wifiSignal);
            }
            else {
                Toast.makeText(context, "Wifi turned off!", Toast.LENGTH_LONG).show();
                updateWidget(context, "Disconnected", -100);
            }
        }
    }

    public void updateWidget(Context context, String ssid, int signal) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wifi_widget);

        views.setTextViewText(R.id.connectedNetwork, ssid);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName projectWidget = new ComponentName(context, WifiWidget.class);

        if (signal == -100) {
            views.setImageViewResource(R.id.signalStrength, R.drawable.no_signal);
        }
        else if (signal < -70) {
            views.setImageViewResource(R.id.signalStrength, R.drawable.low_signal);
        }
        else if (signal < -60 && signal >= -70) {
            views.setImageViewResource(R.id.signalStrength, R.drawable.signal_1);
        }
        else if (signal < -50 && signal >= -60) {
            views.setImageViewResource(R.id.signalStrength, R.drawable.signal_2);
        }
        else if (signal > -50) {
            views.setImageViewResource(R.id.signalStrength, R.drawable.full_signal);
        }

        appWidgetManager.updateAppWidget(projectWidget, views);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

