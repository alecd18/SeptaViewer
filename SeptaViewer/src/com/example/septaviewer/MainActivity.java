package com.example.septaviewer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.*;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.app.ListActivity;
import android.content.Intent;

public class MainActivity extends ActionBarActivity {
	Spinner source;
	Spinner destination;
	ArrayList<String> trains = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	String sourceText;
	String destinationText;
	private ArrayList<item> m_parts = new ArrayList<item>();
	private Runnable viewParts;
	private ItemAdapter m_adapter;
	ListView lv;
	String clickedTrainNum;
	String lastDeparted;
	String scheduleDeparted;
	String actualDeparted;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, trains);
		source = (Spinner)findViewById(R.id.Source);
		source.setAdapter(adapter);
		destination = (Spinner)findViewById(R.id.Destination);
		destination.setAdapter(adapter);
		
		
		new getStations().execute();
	}

	public void getTrainTimes(View v)
	{
		sourceText = source.getSelectedItem().toString().replace(" ", "%20");
		destinationText = destination.getSelectedItem().toString().replace(" ", "%20");
		
		// instantiate our ItemAdapter class
		m_parts.clear();
        
        new getTrains().execute();
	}
	
	public void viewMaps(View v)
	{
		Intent intent = new Intent(this, ViewMaps.class);
		startActivity(intent);
	}
	
	private class getTrains extends AsyncTask<Void, Void, JsonArray>
	{
		JsonArray trainTimes;
		protected JsonArray doInBackground(Void... params) {
			try 
			{
				String sURL = "http://www3.septa.org/hackathon/NextToArrive/" 
				+ sourceText + "/" + destinationText + "/5";
				URL url;
				url = new URL(sURL);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.connect();
				
				JsonParser jp = new JsonParser();
		    	JsonElement root = jp.parse(new InputStreamReader((InputStream) connection.getContent()));
		    	trainTimes = root.getAsJsonArray();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return trainTimes;
		}
		
		protected void onPostExecute(JsonArray result) {
			for (int i = 0; i < result.size(); i++)
			{
				JsonObject train = result.get(i).getAsJsonObject();
				String trainNum = train.get("orig_train").getAsString();
				String depTime = train.get("orig_departure_time").getAsString();
				String arrTime = train.get("orig_arrival_time").getAsString();
				m_parts.add(new item(trainNum, depTime, arrTime));
			}
			setListView();
		}	
	}
	
	private class getCurrentTrain extends AsyncTask<Void, Void, JsonArray>
	{
		JsonArray trainCurrent;
		protected JsonArray doInBackground(Void... params) {
			try
			{
				String sURL = "http://www3.septa.org/hackathon/RRSchedules/" + clickedTrainNum;
				URL url;
				url = new URL(sURL);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.connect();
				
				JsonParser jp = new JsonParser();
		    	JsonElement root = jp.parse(new InputStreamReader((InputStream) connection.getContent()));
		    	trainCurrent = root.getAsJsonArray();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return trainCurrent;
		}
		
		protected void onPostExecute(JsonArray result) {
			String firstStopAT  = result.get(0).getAsJsonObject().get("act_tm").getAsString();
			String na = "na";
			if (firstStopAT.equals(na))
			{
				lastDeparted = result.get(0).getAsJsonObject().get("station").getAsString();
				scheduleDeparted = result.get(0).getAsJsonObject().get("sched_tm").getAsString();
				actualDeparted = "This train hasn't left yet!";
				
			}
			else
			{
				int i = 0;
				while (!result.get(i).getAsJsonObject().get("act_tm").getAsString().equals(na))
				{
					i++;
				}
				
				lastDeparted = result.get(i - 1).getAsJsonObject().get("station").getAsString();
				scheduleDeparted = result.get(i - 1).getAsJsonObject().get("sched_tm").getAsString();
				actualDeparted = result.get(i - 1).getAsJsonObject().get("act_tm").getAsString();
			}
		}	
	}
	
	
	public void setListView()
	{
		lv = (ListView)findViewById(R.id.list);
        m_adapter = new ItemAdapter(this, R.layout.list_item, m_parts);
        lv.setAdapter(m_adapter);
        lv.setLongClickable(true); 
        
        lv.setOnItemLongClickListener(new OnItemLongClickListener() 
	    {
	        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) 
	        {	
	        	item train = (item)lv.getItemAtPosition(position);
	        	clickedTrainNum = item.getTrainNum();
	        	
	        	new getCurrentTrain().execute();
	        	
	        	Toast.makeText(getApplicationContext(), "STATION LAST DEPARTED: " + lastDeparted + "\n"
	        	+ "\n" + "SCHEDULED DEPARTURE TIME: " + scheduleDeparted + "\n" 
	        	+ "\n" + "ACTUAL DEPARTURE TIME: " + actualDeparted ,1).show();
	        	
	        	return true; 
	        } 
	    });
	}
	
	
	 private class getStations extends AsyncTask<Void, Void, String> 
	 {
		String trains;
		protected String doInBackground(Void... params) {
			try 
			{
				String sURL = "http://www3.septa.org/hackathon/Arrivals/station_id_name.csv";
				URL url;
				url = new URL(sURL);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.connect();
				
				BufferedReader r = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
				StringBuilder sb = new StringBuilder();
				int n;
				while ((n = r.read()) != -1)
				{
					sb.append((char)n);
				}
				
				trains = sb.toString();
				
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
				
			return trains;
	        
		}
			
		@Override
		protected void onPostExecute(String result) {
			
			String[] lines = result.split(System.getProperty("line.separator"));
			for (int i = 0; i < lines.length; i ++)
			{
				String station = lines[i].substring(lines[i].indexOf(",") + 1, lines[i].length());
				adapter.add(station);
			}
					
			
		}
	 }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	

}
