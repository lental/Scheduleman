package com.scheduleman;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.scheduleman.Home.Class;
import com.scheduleman.Home.Time;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Schedule extends Activity{

	public static  String FILENAME_PREFIX = "scheduleman_schedule";
    public List<Class> classList = null;
    private int maxDay = 0;
    private int maxTime = 0;

	public static final short MONDAY = 0x0000;
	public static final short TUESDAY = 0x00040;
	public static final short WEDNESDAY = 0x0080;
	public static final short THURSDAY = 0x00C0;
	public static final short FRIDAY = 0x0100;
	public static final short SATURDAY = 0x0140;
	public static final short SUNDAY = 0x0180;
	public static final short[] days = {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
	

    String scheduleName = "";
	//1 11|| 11 1111
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.schedule);

        String url = this.getIntent().getStringExtra("url");
        
    	//If using saved file, read from that file!
        showSchedule(url);
        

    }

    private void showSchedule(String url)
    {
    	try{
	        if(url == null || url == "") classList = new LinkedList<Class>();
	        else classList = parseSite(url);

	        /*
	        int conflictMatrix[][] = new int[7][];
	        
//	    	HashMap<Short, List<Class>> conflictList = new HashMap<Short,List<Class>>();
	        for(Class classIter : classList){
	        	int dayIndex = classIter.day;
	        	int conflictSlot = 0;
	        	for(int timeIndex = classIter.getUnitStart(); timeIndex < classIter.getUnitEnd(); timeIndex++){
	        		if(conflictMatrix[dayIndex][timeIndex] == null) conflictMatrix[dayIndex][timeIndex] = 0;
	        		conflictSlot = Math.max(conflictMatrix[dayIndex][timeIndex], conflictSlot);
	        	}
	        	for(int timeIndex = classIter.getUnitStart(); timeIndex < classIter.getUnitEnd(); timeIndex++){
	        		conflictMatrix[dayIndex][timeIndex] = conflictSlot + 1;
	        	}
	        	classIter.conflictPos = conflictSlot;
	        }
	        */
        	ScheduleLayout scheduleLayout = new ScheduleLayout(this, classList);
            setContentView(scheduleLayout);
            
            
	        LinearLayout tableRoot = (LinearLayout)findViewById(R.id.tableRoot);
	        LinearLayout tableTimes = (LinearLayout) findViewById(R.id.tableTimes);
	        for(int i = 0; i < tableRoot.getChildCount(); i++)
	        {
	        	if(i % 2 == 1){
	        		((TextView) tableTimes.getChildAt(i/2)).setText(Time.getTimeForUnit(i));
	        	}
	        }
        }
        catch (FileNotFoundException e)
        {
        	System.out.println("catching");
			e.printStackTrace();
        }
        //TODO: what is this?
        catch (NullPointerException e)
        {
        	deleteFile(FILENAME_PREFIX + "." + scheduleName); 
        	System.out.println("null pointer");
        	e.printStackTrace();

        	finish();
        }
    }
    public List<Class> parseSite(String url) throws FileNotFoundException{
     	List<Class> classList = new LinkedList<Class>();
        System.out.println("url: " + url);
        
    	if(url == null || url.compareTo("") == 0) throw new FileNotFoundException("Blank Input");

    	else if (url.contains("saved://"))
         {
 			try {
 		        scheduleName = url.substring(url.lastIndexOf('.', url.length()-2) + 1);
 		        System.out.println("schedulename: " + scheduleName);
 	        	FileInputStream fis =  openFileInput(url.substring(8));
 	        	ObjectInputStream ois;
 				ois = new ObjectInputStream(fis);
 				Class current = (Class)ois.readObject();
 				while(current != null)
 				{
 					classList.add(current);
 					current = (Class)ois.readObject();
 				}
 			}
 			catch (EOFException e){}
 			catch (FileNotFoundException e){System.out.println("Could not find stored file! " + e);} 
 			// StreamCorruptedException IOException ClassNotFoundException
 			catch (Exception e) {e.printStackTrace();}
 			return classList;
         }
    	//if it is NOT https call, assume its a schedule key, and attach the site to it
		else if(!url.contains("https://")) url = "https://scheduleman.org/schedules/" + url;
    	
        int conflictMatrix[][] = new int[7][48];
        scheduleName = url.substring(url.lastIndexOf('/', url.length()-2) + 1);
        System.out.println("schedulename: " + scheduleName);
        
    	HttpClient httpclient = new DefaultHttpClient();
    	HttpGet httpget = new HttpGet(url);
    	try { 
           HttpResponse response = httpclient.execute(httpget);
           
           System.out.println(response.getStatusLine().getStatusCode());
           if(response.getStatusLine().getStatusCode() == 404) throw new FileNotFoundException(url);
           else if(response.getStatusLine().getStatusCode() != 200) throw new HttpException("HTTP call fail. status code was" + response.getStatusLine().getStatusCode() ); 
           
           HttpEntity entity = response.getEntity();
           
           InputStream stream = entity.getContent();
           BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
           String line = null;
           Time curTime = null;
           Class curClass = null;
           int curDay = -1;
           boolean isBeforeHour = true;
           boolean isAfterNoon = false;
           while((line = reader.readLine()) != null){
         	  line = line.trim();
         	  //Starts the next hour
         	  if(line.contains("class=\"time\" rowspan=\"2\"")){
         		  String strTime = line.subSequence(29, line.length()- 5).toString();
         		  String[] sep = strTime.split(":");
         		  curDay = -1;
         		  if(Integer.decode(sep[1].trim()) != 0) throw new FileNotFoundException("NOT! FAIL TIME FOUND");

         		  curTime = new Time(Integer.decode(sep[0].trim()), false);
         		  curTime = curTime.subtractTime(1);

         		  if(isAfterNoon){
         			  curTime = curTime.addTime(24);
         			  }
         		  isBeforeHour = true;
         	  }
         	  //It goes to the second half hour
         	  else if(line.contains("tr class="))
         	  {
         		  if(curTime != null && isBeforeHour)
         		  {
             		 curDay = -1;
             		 curTime = curTime.addTime(1);
             		 if(curTime.hour == 13) {
             			 isAfterNoon = true;
             		 }
         		  }
         	  }
         	  //A new day, a new if
         	  else if(line.contains("dayspacer") && curTime != null)
         	  {
         		  curDay++;
         	  }
         	  
         	  //Finds the beginning of class, gets time, day, length, and bg
         	  else if(line.contains("td class=\"class\""))
         	  {
         		  curClass = new Class(curTime);
         		  curClass.day = curDay;
         		  curClass.classLength = Integer.decode(line.substring(27,28)); //
         		  if(line.indexOf("#") < 0)
         		  {
         			 curClass.color = 0xFFFFFF;
         		  }  
         		  else
         		  {  
         			  curClass.color = Integer.parseInt(line.substring(line.indexOf("#")+1, line.indexOf("#") + 7),16);
         		  }
         		  
         		  //Check for conflicts
	  	          for(int timeIndex = curClass.getUnitStart(); timeIndex < curClass.getUnitEnd(); timeIndex++){
		        	  curClass.conflictPos = Math.max(conflictMatrix[curClass.day][timeIndex], curClass.conflictPos);
		          }
	  	          for(int timeIndex = curClass.getUnitStart(); timeIndex < curClass.getUnitEnd(); timeIndex++){
		        	  conflictMatrix[curClass.day][timeIndex] = curClass.conflictPos + 1;
		          }
	  	          
         		  //TODO: background length
         	  }
         	  
         	  //Figure out class name and room.
         	  else if(line.contains("div class=\"number\"") && curClass != null){
         		  curClass.className = line.substring(27, line.length()-2).toString();
         		  line = reader.readLine();
         		  curClass.classNumber = line.trim();
         	  }
         	  
         	  //Last thing, find room number
         	  else if(line.contains("class=\"room\""))
         	  {
         		  curClass.room = line.subSequence(line.indexOf(">")+1,line.length()-6).toString();
         		 
         		  classList.add(curClass);
         	  }
           }
           
           stream.close();

           FileOutputStream fos = this.openFileOutput(FILENAME_PREFIX + "." + scheduleName, 0);
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           for(Class index : classList)
        	   oos.writeObject(index);
           
       	   oos.flush(); 
           oos.close(); 
           fos.close();
           
           SharedPreferences settings = getSharedPreferences(Home.PREFS_NAME, 0);
           SharedPreferences.Editor editor = settings.edit();
           editor.putString("fileName", FILENAME_PREFIX + "." + scheduleName);
           editor.commit();
         } catch (Exception e) {
 			e.printStackTrace();
         }
 		return classList;
 	
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getTitle().equals(getString(R.string.ScheduleDeleteSave))){
        	deleteFile(FILENAME_PREFIX + "." + scheduleName);

	    	Intent goHomeIntent = new Intent(this, Home.class);
			startActivity(goHomeIntent);
        	finish();
    	}
    	else
    	{
        	deleteFile(FILENAME_PREFIX + "." + scheduleName);
        	showSchedule(scheduleName);
    		
    	}
    	return true;
    }
    
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
    	menu.add(R.string.ScheduleDeleteSave);
    	menu.add(R.string.ScheduleRefresh);
    	return true;
    }
}
