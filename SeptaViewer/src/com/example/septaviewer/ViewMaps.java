package com.example.septaviewer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;

public class ViewMaps extends ActionBarActivity {
	WebView googleMaps;
	String latitude = "";
	String longitude = "";
	String trainNum = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_maps);
		
		new getLatLong().execute();
		
		
	}
	
	private class getLatLong extends AsyncTask<Void, Void, JsonArray>
	{
		JsonArray latlong;
		protected JsonArray doInBackground(Void... params) {
			try 
			{
				String sURL = "http://www3.septa.org/hackathon/TrainView/";
				URL url;
				url = new URL(sURL);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.connect();
				
				JsonParser jp = new JsonParser();
		    	JsonElement root = jp.parse(new InputStreamReader((InputStream) connection.getContent()));
		    	latlong = root.getAsJsonArray();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return latlong;
		}
		
		protected void onPostExecute(JsonArray result) {
			
			for (int i = 0; i < result.size(); i++)
			{
				JsonObject root = result.get(i).getAsJsonObject();
				String lat = root.get("lat").getAsString();
				String lon = root.get("lon").getAsString();
				String train = root.get("trainno").getAsString();
				
				latitude = latitude + lat + ",";
				longitude = longitude + lon + ",";
				trainNum = trainNum + train + ",";
				
			}
			
			latitude = latitude.substring(0, latitude.length() - 1);
			Log.i("AsyncTAsk",latitude);
			longitude = longitude.substring(0, longitude.length() - 1);
			Log.i("AsyncTAsk",longitude);
			trainNum = trainNum.substring(0, trainNum.length() - 1);
			Log.i("AsyncTAsk", trainNum);
			
			googleMaps = (WebView)findViewById(R.id.map);
			googleMaps.getSettings().setJavaScriptEnabled(true);
		    googleMaps.loadUrl("file:///android_asset/GoogleMaps.html?lat=" + latitude +  "&lng=" + longitude + "&trn=" + trainNum);
		}	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_maps, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
