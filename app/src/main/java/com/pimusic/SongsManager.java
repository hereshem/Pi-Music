package com.pimusic;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;

public class SongsManager {
	// SDCard Path
	final String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath() + "/mp3/";
	private ArrayList<HashMap<String, String>> songsList;
	OnListSongs action;
	// Constructor
	public SongsManager(){
		
	}
	public SongsManager(OnListSongs action){
		this.action = action;
		new AsyncTask<Void, Void, String>(){
			@Override
			protected String doInBackground(Void... params) {
				return RequestHttp("http://mp3.hemshrestha.com.np/api.php");
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				sendResult(result);
			}
			
		}.execute();
	}
	public static boolean isNetworkAvialable(Context cont) {
		ConnectivityManager cm = (ConnectivityManager) cont
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null){
			return true;
		}else{
			return false;
		}
	}
	protected void sendResult(String result) {
		try{
			JSONArray jArr = new JSONArray(result);
			songsList = new ArrayList<HashMap<String, String>>();
			for (int i = 0 ; i < jArr.length() ; i++) {
				HashMap<String, String> song = new HashMap<String, String>();
				
				song.put("songTitle", jArr.getJSONObject(i).getString("title"));
				song.put("songPath", jArr.getJSONObject(i).getString("path"));
				
				songsList.add(song);
			}
		}catch(Exception e){e.printStackTrace();}
		if(action != null)
			action.onListGet(songsList);
	}
	public interface OnListSongs{
		public void onListGet(ArrayList<HashMap<String, String>> songsList);
	}
	
	public String RequestHttp(String url) {
		try{
			HttpGet request = new HttpGet(new URI(url));
			HttpResponse response =  new DefaultHttpClient().execute(request);
			HttpEntity httpEntity = response.getEntity();
			return EntityUtils.toString(httpEntity);
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	/**
	 * Function to read all mp3 files from sdcard
	 * and store the details in ArrayList
	 * */
	public ArrayList<HashMap<String, String>> getPlayList(){
		File home = new File(MEDIA_PATH);
		songsList = new ArrayList<HashMap<String, String>>();
		if (home.listFiles(new FileExtensionFilter()).length > 0) {
			for (File file : home.listFiles(new FileExtensionFilter())) {
				HashMap<String, String> song = new HashMap<String, String>();
				song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
				song.put("songPath", file.getPath());
				
				// Adding each song to SongList
				songsList.add(song);
			}
		}
		// return songs list array
		return songsList;
	}
    public static boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false; // There are no active networks.
        } else
            return true;
    }
	/**
	 * Class to filter files which are having .mp3 extension
	 * */
	class FileExtensionFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp3") || name.endsWith(".MP3"));
		}
	}
}
