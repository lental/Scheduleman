package com.scheduleman;


import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity {
	private static final int startingHour = 7;
	//starting half hour is 0 when schedule starts at x:00
	private static final int startingHalfHour = 1;
	public static  String PREFS_NAME = "scheduleman_prefs";
	private String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
	
	public static class Time implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int hour;
		public int day;
		public boolean isHalfHour;
		public Time(int hour, boolean isHalfHour){
			if(hour == 0) hour = 12;
			this.hour = hour;
			this.isHalfHour = isHalfHour; 
		}
		
		public int getHour(){
			return hour;
		}
		public int getMinute(){
			if(isHalfHour) return 30;
			else return 0;
		}
		public String toString(){
			int printHour;
			printHour = hour;
			if(hour >= 13) printHour = (hour % 12);
			if(hour == 0) printHour = 12;
			if(isHalfHour){
				return printHour + ":30";
			}
			else{
				return (printHour) + ":" + "00";
			}
		}
		
		public Time subtractTime(int units){
			Time newTime = new Time(hour, isHalfHour);
			newTime.hour -= units / 2;
			if(isHalfHour)
				newTime.isHalfHour = false;
			else
			{
				newTime.hour--;
				newTime.isHalfHour = true;
			}
			return newTime;
			
		}
		
		public Time addTime(int units){
			Time newTime = new Time(hour, isHalfHour);
			newTime.hour += units / 2;
			if(units % 2 == 1)
			{
				if(isHalfHour)
				{
					newTime.hour++;
					newTime.isHalfHour = false;
				}
				else
				{
					newTime.isHalfHour = true;
				}
			}
			return newTime; 
		}
		
		public int getUnitStart(){
			if(hour - startingHour < 0) hour += 12;
			if(isHalfHour)
				return (hour - startingHour) * 2 + 1 + startingHalfHour;
			else
				return  (hour - startingHour) * 2 + startingHalfHour;
		}
		
		public static String getTimeForUnit(int unit){
			int hour = startingHour + (unit / 2);
			int isHalfHour = ((unit + startingHalfHour) % 2);
			if(hour >= 13) hour = (hour % 12);
			
			if(isHalfHour == 0){
				return hour + ":00";
			}
			else{
				return (hour) + ":" + "30";
			}
		}
	}
	
	public static class Class implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Time time;
		public int day;
		public String room;
		public String className;
		public String classNumber;
		public int classLength;
		public int color;
		
		public int conflictPos;
		public int conflictCount;
		public Class(Time time){
			this.time = time;
			room = "";
			className = "";
		}
		public int getUnitStart(){
			return time.getUnitStart();
		}
		public int getUnitEnd(){
			return time.addTime(classLength).getUnitStart();
		}
		public String getTime(){
			return time.toString();
		}
		
		public String endTime(){
			return time.addTime(classLength).toString();
		}
	}

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String fileName = settings.getString("fileName", Schedule.FILENAME_PREFIX);
        System.out.println("Filename: " + fileName);
    	File file = getFileStreamPath(fileName);
        if(fileName != null)
        {
        	//File file = getFileStreamPath(fileName);
	        if(file.exists())
	        {
		    	Intent goScheduleIntent = new Intent(this, Schedule.class);
		        goScheduleIntent.putExtra("url", "saved://" + fileName);
				startActivity(goScheduleIntent);
				finish();
	        }
        }
        	
        setContentView(R.layout.main);
        
        Button urlButton = (Button) findViewById(R.id.changeUrlButton);
        
        urlButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

	            Toast loadingToast;
	            loadingToast = Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_LONG);
	            loadingToast.show();
	            
	            startIntent();
			}
			private void startIntent(){

				Intent goScheduleIntent = new Intent(Home.this, Schedule.class);

		        EditText urlEdit = (EditText) findViewById(R.id.HomeUrlEdit);
		        goScheduleIntent.putExtra("url", urlEdit.getText().toString());

		        startActivity(goScheduleIntent);
			}
		});
        
        TextView homeInfoText = (TextView) findViewById(R.id.homeInfoText);

        //Class List on Home Page
        /*
    	List<Class> classList = new LinkedList<Class>();
        try {
        	FileInputStream fis =  this.openFileInput(fileName);
        	ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(fis);
				Class current = (Class)ois.readObject();
				while(current != null)
				{
					classList.add(current);
					current = (Class)ois.readObject();
				}
			}
			catch (EOFException e){}
			// StreamCorruptedException IOException ClassNotFoundException
			catch (Exception e) {e.printStackTrace();}
			 
			
			
	        String str = "";
	        if(classList != null)
	        for(Class classIter : classList){
	         	  str += "Class " + classIter.className + " at  " + classIter.getTime() + " on " + days[classIter.day] + " until " + classIter.endTime() + " in " + classIter.room + "\n\n";
	        }
	        homeInfoText.setText(str);
        }
        catch (FileNotFoundException e)
        {
        	System.out.println("catching");
        	homeInfoText.setText("No Saved Schedule: " + e.getMessage().toString());
        }
        */
        
    }
    

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getTitle().equals(getString(R.string.HomeMenuPrevious))){
	    	Intent goScheduleIntent = new Intent(this, Schedule.class);
	        goScheduleIntent.putExtra("url", "saved://scheduleman_schedule"); //TODO: Fix
			startActivity(goScheduleIntent);
    	}
    	if (item.getTitle().equals("test")){
    		//setContentView(new ScheduleLayout(this,null));
    	}
    	return true;
    }
    
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
 //   	menu.add(R.string.HomeMenuPrevious);
//    	menu.add("test");
    	
    	return true;
    }
}