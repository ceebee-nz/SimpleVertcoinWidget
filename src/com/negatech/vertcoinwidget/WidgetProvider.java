package com.negatech.vertcoinwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider
{

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		for(int widgetId : appWidgetIds)
		{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			{
				Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
				int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
				Prefs.setWidth(context, widgetId, width - 56);
			}
			setAlarm(context, widgetId);
			Intent i = new Intent(context, PriceBroadcastReceiver.class);
			i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			PendingIntent pi = PendingIntent.getBroadcast(context, widgetId, i, 0);
			views.setOnClickPendingIntent(R.id.vertcoinParent, pi);
			appWidgetManager.updateAppWidget(widgetId, views);
		}
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
	{
		int min = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		Prefs.setWidth(context, appWidgetId, min - 56);

		// /////////////////////////////////////
		// the text may change size on resize
		// refresh the widget

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		String currencyCode = Prefs.getCurrency(context, appWidgetId);
		Currency currency = Currency.valueOf(currencyCode);

		String amount = Double.toString(Prefs.getLastAmount(context, appWidgetId));

		int providerInt = Prefs.getProvider(context, appWidgetId);
		VTCProvider provider = VTCProvider.values()[providerInt];

		WidgetViews.setText(context, views, currency, amount, true, provider.getLabel(), appWidgetId);

		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		for(int widgetId : appWidgetIds)
		{
			Prefs.delete(context, widgetId);
			Intent i = new Intent(context, PriceBroadcastReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(context, widgetId + 1000, i, 0);
			AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarm.cancel(pi);
		}
	}

	private void setAlarm(Context context, int widgetId)
	{
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, PriceBroadcastReceiver.class);
		i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		PendingIntent pi = PendingIntent.getBroadcast(context, widgetId + 1000, i, 0);
		int update = Prefs.getInterval(context, widgetId);
		context.sendBroadcast(i);
		alarm.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, update * 60000, pi);
	}

}
