package com.negatech.vertcoinwidget;

import java.security.KeyStore;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public enum VTCProvider
{
	CRYPTSY(R.array.currencies_cryptsy, "Cryptsy")
	{
		@Override
		public String getValue(String currencyCode) throws Exception
		{
			JSONObject obj = getJSONObject("http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=151");
			if(obj.getInt("success") == 1)
			{
				return obj.getJSONObject("return").getJSONObject("markets").getJSONObject("VTC").getString("lasttradeprice");
			}
			return null;
		}
	},
	VIRCUREX(R.array.currencies_vircurex, "Vircurex")
	{
		@Override
		public String getValue(String currencyCode) throws Exception
		{
			JSONObject obj = getJSONObject(String.format("https://api.vircurex.com/api/get_last_trade.json?base=VTC&alt=%s", currencyCode));
			return obj.getString("value");
		}
	},
	POLONIEX(R.array.currencies_poloniex, "Poloniex")
	{
		@Override
		public String getValue(String currencyCode) throws Exception
		{
			JSONObject obj = getJSONObject("https://poloniex.com/public?command=returnTicker");
			return obj.getString(currencyCode + "_VTC");
		}
	},
	BTER(R.array.currencies_bter, "Bter")
	{
		@Override
		public String getValue(String currencyCode) throws Exception
		{
			JSONObject obj = getJSONObject(String.format("https://data.bter.com/api/1/ticker/vtc_%s", currencyCode));
			return obj.getString("last");
		}
	};

	private final int currencyArrayID;
	private String label;

	VTCProvider(int currencyArrayID, String label)
	{
		this.currencyArrayID = currencyArrayID;
		this.label = label;
	}

	public abstract String getValue(String currencyCode) throws Exception;

	public int getCurrencies()
	{
		return currencyArrayID;
	}

	public String getLabel()
	{
		return label;
	}

	private static JSONObject getJSONObject(String url) throws Exception
	{
		return new JSONObject(getString(url));
	}

	private static String getString(String url) throws Exception
	{
		HttpGet get = new HttpGet(url);

		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", sf, 443));

		ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
		HttpClient client = new DefaultHttpClient(ccm, params);

		return client.execute(get, new BasicResponseHandler());
	}

}
