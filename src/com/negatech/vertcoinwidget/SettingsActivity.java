package com.negatech.vertcoinwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity
{

	private ListPreference refresh;
	private ListPreference currency;
	private ListPreference provider;
	private CheckBoxPreference label;
	private int appWidgetId;
	private int refreshValue;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		if(extras != null)
		{
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		addPreferencesFromResource(R.xml.preferences);
		refresh = (ListPreference) findPreference(getString(R.string.key_refresh_interval));
		currency = (ListPreference) findPreference(getString(R.string.key_currency));
		provider = (ListPreference) findPreference(getString(R.string.key_provider));
		label = (CheckBoxPreference) findPreference(getString(R.string.key_label));

		setRefresh(Prefs.getInterval(this, appWidgetId));
		refresh.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference p, Object value)
			{
				setRefresh(Integer.valueOf(value.toString()));
				return true;
			}
		});

		provider.setSummary(getString(R.string.summary_provider, provider.getEntry()));
		provider.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object value)
			{
				ListPreference p = (ListPreference) preference;
				CharSequence[] entryValues = p.getEntryValues();
				int v = 0;
				for(int i = 0; i < entryValues.length; i++)
				{
					if(entryValues[i].equals(value))
					{
						v = i;
					}
				}

				preference.setSummary(getString(R.string.summary_provider, p.getEntries()[v]));

				VTCProvider provider = VTCProvider.values()[v];

				currency.setEntries(provider.getCurrencies());
				currency.setEntryValues(provider.getCurrencies());
				currency.setValueIndex(0);
				currency.setSummary(getString(R.string.summary_currency, currency.getEntry()));

				return true;
			}
		});

		currency.setSummary(getString(R.string.summary_currency, currency.getEntry()));

		currency.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference p, Object value)
			{
				p.setSummary(getString(R.string.summary_currency, value));
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuItem menuItem = menu.add(0, 0, 0, "Save");
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == 0)
		{
			save();
		}
		return true;
	}

	public void save()
	{
		Intent broadcast = new Intent(this, WidgetProvider.class);
		broadcast.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		broadcast.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
		sendBroadcast(broadcast);
		Prefs.setValues(this, appWidgetId, currency.getValue(), refreshValue, Integer.valueOf(provider.getValue()), label.isChecked());
		Intent result = new Intent();
		result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(RESULT_OK, result);
		finish();
	}

	private void setRefresh(int rate)
	{
		refreshValue = rate;
		if(rate < 60)
		{
			refresh.setSummary(getResources().getQuantityString(R.plurals.summary_refresh_interval_minute, rate, rate));
		}
		else
		{
			refresh.setSummary(getResources().getQuantityString(R.plurals.summary_refresh_interval_hour, rate / 60, rate / 60));
		}
	}

	@Override
	public void onBackPressed()
	{
		save();
	}

}
