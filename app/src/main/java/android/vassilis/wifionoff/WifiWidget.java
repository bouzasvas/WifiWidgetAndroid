package android.vassilis.wifionoff;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.wifi.WifiManager;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Implementation of App Widget functionality.
 */
public class WifiWidget extends AppWidgetProvider {

    public static String CHANGE_WIFI_STATE = "en_wifi";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wifi_widget);


        //ACTION
        Intent intent = new Intent(context, WifiWidget.class);
        intent.setAction(CHANGE_WIFI_STATE);

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
        if (intent.getAction().equals(CHANGE_WIFI_STATE)) {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            boolean wifiState = manager.isWifiEnabled();

            manager.setWifiEnabled(!wifiState);

            if (!wifiState) {
                Toast.makeText(context, "Wifi turned on!", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(context, "Wifi turned off!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

