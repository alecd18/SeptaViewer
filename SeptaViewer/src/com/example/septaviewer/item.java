package com.example.septaviewer;


import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.os.Build;

public class item {
	private static String trainNum;
	private String depTime;
	private String arrTime;

	public item(){

	}

	// Initialize new item
	public item(String tn, String dt, String at){
		this.trainNum = tn;
		this.depTime = dt;
		this.arrTime = at;
	}

	// Get date
	public static String getTrainNum() {
		return trainNum;
	}

	// Set Date
	public void setTrainNum(String num) {
		this.trainNum = num;
	}

	// Get Weather Conditions
	public String getDepTime() {
		return depTime;
	}

	// Set Weather Conditions
	public void setDepTime(String depT) {
		this.depTime = depT;
	}

	// Get Temperature
	public String getArrTime() {
		return arrTime;
	}

	// Set Temperature
	public void setArrTime(String arrT) {
		this.arrTime = arrT;
	}
	
}
	



