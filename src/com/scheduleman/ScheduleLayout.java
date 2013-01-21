package com.scheduleman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import com.scheduleman.Home.Class;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ScheduleLayout extends ViewGroup  implements Serializable{


	private static final long serialVersionUID = 1L;
	private static int MIN_ROW_COUNT = 24;
	private static int DEFAULT_DAY_CONFLICTS = 1;
	private static int MAX_COL_COUNT = 7;
	private static int MIN_COL_COUNT = 5;
	private static int ROW_HEIGHT = 20;
	private static int FONT_SIZE = 9;
	private static int TABLE_TIMES_WIDTH = 40;
	//private static int TABLE_DAYS_HEIGHT = FONT_SIZE + 90;
	private static int TABLE_DAYS_HEIGHT = ROW_HEIGHT;
	private static String[] DAY_NAME = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	
	private int _dayCount = MIN_COL_COUNT;
	private int _rowCount = MIN_ROW_COUNT;
	private int _cellMinimumWidth = -1;
	private class ClassView extends TextView{

		public Class inputClass;
		public ClassView(Context context, Class inputClass) {
			super(context);
			this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
			
			this.inputClass = inputClass;
			this.setBackgroundColor(0xFF000000 | inputClass.color);
			this.setText(
					Html.fromHtml("<b>" + inputClass.classNumber + "</b>" +  "<br />" + 
					inputClass.room));
			this.setTextColor(Color.BLACK);
			this.setGravity(0x11);
			this.setTextSize(FONT_SIZE);
			this.setHeight((ROW_HEIGHT * this.inputClass.classLength) - 1);
		}
	}
	

	private class SynchedDualScroll extends FrameLayout {

		private FrameLayout vertPartner = null;	
		private FrameLayout horizPartner = null;
		
		
		public void setVertPartner(FrameLayout p)
		{
			vertPartner = p;
		}
		public void setHorizPartner(FrameLayout p)
		{
			horizPartner = p;
		}
		
		public SynchedDualScroll(Context context) {
			super(context);
		}

		private int currentX;
		private int currentY;

		@Override 
		public boolean onTouchEvent(MotionEvent event) {
		    switch (event.getAction()) {
		        case MotionEvent.ACTION_DOWN: {
		            currentX = (int) event.getRawX();
		            currentY = (int) event.getRawY();
		            break;
		        }

		        case MotionEvent.ACTION_MOVE: {
		            int x2 = (int) event.getRawX();
		            int y2 = (int) event.getRawY();

		            final int deltaX = currentX - x2;
		            final int deltaY = currentY - y2;
		            
		            currentX = x2;
		            currentY = y2;
		            
		            int finalXScroll = 0;
		            int finalYScroll = 0;
		            
	                 if (deltaX < 0) {
	                     if (getScrollX() > 0) {
                        	 finalXScroll = deltaX;
	                     }
	                 } else if (deltaX > 0) {
	                     final int rightEdge = getWidth() - getPaddingRight();
	                     final int availableToScroll = getChildAt(0).getRight() - getScrollX() - rightEdge;
	                     if (availableToScroll > 0) {
	                         finalXScroll = Math.min(availableToScroll, deltaX);
	                     }
	                 }
	                 if (deltaY < 0) {
	                     if (getScrollY() > 0) {
                        	 finalYScroll = deltaY;
	                     }
	                 } else if (deltaY > 0) {
	                     final int bottomEdge = getHeight() - getPaddingBottom();
	                     final int availableToScroll = getChildAt(0).getHeight() + 200 - getScrollY() - bottomEdge;
	                     if (availableToScroll > 0) {
	                         finalYScroll = Math.min(availableToScroll, deltaY);
	                     }
	                 }

			            this.scrollBy(finalXScroll , finalYScroll);
			            vertPartner.scrollBy(finalXScroll , finalYScroll);
			            horizPartner.scrollBy(finalXScroll , finalYScroll);
			            awakenScrollBars();
		            break;
		        }   
		        case MotionEvent.ACTION_UP: {
		            break;
		        }
		    }
		      return true; 
		  }
		
		public void onScrollChanged(int l, int t, int oldl, int oldt) {

            
			super.onScrollChanged(l, t, oldl, oldt);
	    }

		
		public void scrollTo(int x, int y)
		{
			if (getChildCount() > 0) {
				View child = getChildAt(0);
				x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
				y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight());
				if (x != getScrollX() || y != getScrollY()) {
					super.scrollTo(x, y);
				}
			}
		}
		private int clamp(int n, int my, int child) {
			if (my >= child || n < 0) {
				return 0;
			}
			if ((my + n) > child) {
				return child - my;
			}
			return n;
		}
	}
	private class SynchedHorizontalScroll extends HorizontalScrollView {
		private SynchedDualScroll partner = null;
		public SynchedHorizontalScroll(Context context) {
			super(context);
		}
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			if(l == oldl && t == oldt) return;
	        partner.scrollTo(l, partner.getScrollY());
			super.onScrollChanged(l, t, oldl, oldt);
	    }
		
		public void setPartner(SynchedDualScroll p)
		{
			partner = p;
		}

  	}
	private class SynchedVerticalScroll extends ScrollView {
		private SynchedDualScroll partner = null;
		public SynchedVerticalScroll(Context context) {
			super(context);
		}
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			if(l == oldl && t == oldt) return;
	       partner.scrollTo(partner.getScrollX(), t);
			super.onScrollChanged(l, t, oldl, oldt);
	    }
		
		public void setPartner(SynchedDualScroll p)
		{
			partner = p;
		}

	}

	
	//	TextView text;
	//public ScheduleLayout(Context context, List<Class> classList, HashMap<Short, Pair<Class, Class>> conflictMap) {
	HashMap<Integer, HashMap<Integer, Class>> conflictMap;
	HashMap<Integer, Integer> mostConflictsByDay;

    private int conflictMatrix[][] = new int[7][48];
    private int dayConflictCount[] = new int[7];
    
	public ScheduleLayout(Context context, List<Class> classList) {
		super(context);
		conflictMap = new HashMap<Integer, HashMap<Integer, Class>>();
		mostConflictsByDay = new HashMap<Integer, Integer>();
		//Create horizontal scroll for days
		SynchedHorizontalScroll dayScroll = new SynchedHorizontalScroll(context);
		dayScroll.setId(R.id.DaysHorizScroll);
		dayScroll.setHorizontalScrollBarEnabled(false);
		dayScroll.scrollTo(500, 5000);
		this.addView(dayScroll);
		
		//Create the first Row: Days of week
		LinearLayout tableDays = new LinearLayout(context);
		tableDays.setId(R.id.tableDays);
		tableDays.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		dayScroll.addView(tableDays);
		for(Class curClass : classList)
		{
			_dayCount = Math.max(_dayCount, curClass.day);
			_rowCount = Math.max(_rowCount, curClass.getUnitEnd() + 2);
			
		}
		for(int i = 0; i < _dayCount; i++)
		{
			TextView cell = new TextView(context);
			cell.setGravity(0x11);//Center-horizontal
			cell.setTextSize(FONT_SIZE);
			cell.setBackgroundColor(0xFF222222);
			cell.setText(DAY_NAME[i]);
			_cellMinimumWidth = Math.round(cell.getPaint().measureText("Wednesday"))+5;
			tableDays.addView(cell);
		}
		
		
		//Create vertical scroll element
		SynchedVerticalScroll timeScroll = new SynchedVerticalScroll(context);
		timeScroll.setId(R.id.tableScroll);
		timeScroll.setVerticalScrollBarEnabled(false);
		this.addView(timeScroll);

		//Create scroll element's only child
//		LinearLayout scrollChild = new LinearLayout(context);
//		mainScroll.addView(scrollChild);
		
		//Create the Times column of the table
		LinearLayout tableTimes = new LinearLayout(context);
		tableTimes.setId(R.id.tableTimes);
		timeScroll.addView(tableTimes);
		
		
		//Create scroll for core schedulespace only;  will be synced with day-of-week h-scroll
		SynchedDualScroll tableScroll = new SynchedDualScroll(context);
		tableScroll.setId(R.id.TableHorizScroll);
		tableScroll.setHorizontalScrollBarEnabled(true);
		tableScroll.setVerticalScrollBarEnabled(true);
		tableScroll.setScrollbarFadingEnabled(true);
		tableScroll.setScrollContainer(true);
		tableScroll.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
		this.addView(tableScroll);
		
		//Create scroll element's only child
		LinearLayout hScrollChild = new LinearLayout(context);
		tableScroll.addView(hScrollChild);
		
		//Create the core schedulespace of the table
		LinearLayout tableRoot = new LinearLayout(context);
		tableRoot.setId(R.id.tableRoot);
		hScrollChild.addView(tableRoot);
		//scrollChild.addView(tableRoot);

		//Create the class overlay
		LinearLayout classOverlay = new LinearLayout(context);
		classOverlay.setId(R.id.classOverlay);
		hScrollChild.addView(classOverlay);

		//Add each class to the table
		for(Class curClass : classList)
		{
   		  //Check for conflicts
	        for(int timeIndex = curClass.getUnitStart(); timeIndex < curClass.getUnitEnd(); timeIndex++){
	        	curClass.conflictPos = Math.max(conflictMatrix[curClass.day][timeIndex], curClass.conflictPos);
	        }
	        for(int timeIndex = curClass.getUnitStart(); timeIndex < curClass.getUnitEnd(); timeIndex++){
	        	conflictMatrix[curClass.day][timeIndex] = curClass.conflictPos + 1;
	        	dayConflictCount[curClass.day] = Math.max(dayConflictCount[curClass.day], curClass.conflictPos + 1);
	        }
			classOverlay.addView(new ClassView(context, curClass));
		}
		//scrollChild.addView(classOverlay);
		
		
		//Populate the tableRoot with rows
		for(int i = 0; i < _rowCount; i++)
		{
			LinearLayout row = new LinearLayout(context);
			
			
			//Populate each row with cells
			for(int j = 0; j < _dayCount; j++)
			{
				TextView cell = new TextView(context);
				cell.setGravity(0x11);//Center-horizontal
				cell.setTextSize(FONT_SIZE);
				
	    		if((i+1)% 4 < 2)
	    		{
	    			cell.setBackgroundColor(0xFF555555);
	    		}
	    		else
	    		{
	    			cell.setBackgroundColor(0xFF333333);
	    		}

				//cell.setBackgroundColor(0xFF000000 | (0x001000 * i) | (0x000010 * j));
				row.addView(cell);
			}

			//If this is every other row, add a Time cell to the time column
			if(i % 2 == 1)
			{
				TextView time = new TextView(context);
				time.setGravity(0x15);//Right center-vertical
				time.setTextSize(FONT_SIZE);
				time.setText("time");
				tableTimes.addView(time);
			}
			tableRoot.addView(row);	
			
			//Create synch partnerships!
			tableScroll.setVertPartner(timeScroll);
			tableScroll.setHorizPartner(dayScroll);
			
			timeScroll.setPartner(tableScroll);
			dayScroll.setPartner(tableScroll);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
		//Choose between minimum cell width, or larger if available.

		int colCount = 0;
		for(int curDay = 0; curDay < _dayCount; curDay++) //Top column, with day tags
		{
			int numConflicts = Math.max(dayConflictCount[curDay], 1);
			colCount += numConflicts;
		}
		final int colWidth = Math.max( (r - TABLE_TIMES_WIDTH) / colCount, _cellMinimumWidth);
		
		HorizontalScrollView dayScroll = (HorizontalScrollView) this.findViewById(R.id.DaysHorizScroll);
		dayScroll.layout(TABLE_TIMES_WIDTH, 0, r, TABLE_DAYS_HEIGHT);
		
		LinearLayout tableDays = (LinearLayout) this.findViewById(R.id.tableDays);
		//TABLE_DAYS_HEIGHT = new Float(((TextView) tableDays.getChildAt(0)).getTextSize()).intValue() + 7;
		
		int lastColRight = 0;
		for(int curDay = 0; curDay < _dayCount; curDay++) //Top column, with day tags
		{
			TextView cell = (TextView) tableDays.getChildAt(curDay);

			int numConflicts = Math.max(dayConflictCount[curDay], 1);
			cell.layout(lastColRight + 1, 0, lastColRight + numConflicts * colWidth, ROW_HEIGHT);
			System.out.println(cell.getText() + ": " + (lastColRight + numConflicts * colWidth));
			lastColRight = cell.getRight();
			colCount += numConflicts;
		}
		int scheduleWidth = lastColRight;
		tableDays.layout(0, 0, lastColRight, TABLE_DAYS_HEIGHT);
		lastColRight = 0;
		
		for(int curDay = 0; curDay < _dayCount; curDay++)
		{
			TextView cell = (TextView) tableDays.getChildAt(curDay);

			int cellUnitSize = Math.max(dayConflictCount[curDay], DEFAULT_DAY_CONFLICTS);
			cell.layout(lastColRight + 1, 0, lastColRight + cellUnitSize * colWidth, ROW_HEIGHT);
			System.out.println(cell.getText() + ": " + (lastColRight + cellUnitSize * colWidth));
			lastColRight = cell.getRight();
		}
		//Layout MainScoll to take up all but top row
		ScrollView timeScroll = (ScrollView) findViewById(R.id.tableScroll);
		timeScroll.layout(l, TABLE_DAYS_HEIGHT + 1, r, b);
		
		//Layout ScrollChild to fit the scroller, plus more vertical space
//		LinearLayout scrollChild = (LinearLayout) timeScroll.getChildAt(0);
//		scrollChild.layout(0, 0, r, (_rowCount * ROW_HEIGHT));
		

		LinearLayout tableTimes = (LinearLayout) this.findViewById(R.id.tableTimes);
		tableTimes.layout(0, 0, TABLE_TIMES_WIDTH, (_rowCount * ROW_HEIGHT));
		
		//Layout the Root to take up all but side column of scroller NOTE: may extend past to the right!
		FrameLayout tableScroll = (FrameLayout) this.findViewById(R.id.TableHorizScroll);
		tableScroll.layout(TABLE_TIMES_WIDTH, TABLE_DAYS_HEIGHT, r, b);
		
		LinearLayout hScrollChild = (LinearLayout) tableScroll.getChildAt(0);
		hScrollChild.layout(0, 0, scheduleWidth, (_rowCount * ROW_HEIGHT));
		
		LinearLayout tableRoot = (LinearLayout) findViewById(R.id.tableRoot);
		tableRoot.layout(0, 0, scheduleWidth, (_rowCount * ROW_HEIGHT));
		
		//Layout the Time column to fit the left side of the scroller
		
		//For each row, give place it in the table
		for(int i = 0; i < _rowCount; i++)
		{
			LinearLayout row = (LinearLayout) tableRoot.getChildAt(i);
			int rowTop = ((ROW_HEIGHT) * i);
			row.layout(0, rowTop, scheduleWidth, rowTop + ROW_HEIGHT - 1);
			
			//For each cell, layout in the row
			for(int j = 0; j < _dayCount; j++)
			{
				TextView cell = (TextView) row.getChildAt(j);
				cell.layout(tableDays.getChildAt(j).getLeft(), 0, tableDays.getChildAt(j).getRight(), ROW_HEIGHT);
//				cell.setHeight(rowHeight);
//				cell.setWidth(colWidth);
//				cell.measure(colWidth, (rowHeight) - 1);
			}
			
			//If a time is placed before this, place it between this row and the previous one
			if(i % 2 == 1)
			{
				TextView time = (TextView) tableTimes.getChildAt(i/2);
				int fontSize = new Float(time.getTextSize()).intValue();
				time.layout(0, rowTop - fontSize, TABLE_TIMES_WIDTH, rowTop + fontSize);
			}
		}
		
		//Layout the Root to take up all but side column of scroller
		LinearLayout classOverlay = (LinearLayout) findViewById(R.id.classOverlay);
		classOverlay.layout(0, 0, scheduleWidth, (_rowCount * ROW_HEIGHT));
		
		for(int i = 0; i < classOverlay.getChildCount(); i++)
		{
			ClassView curClass = (ClassView) classOverlay.getChildAt(i);
			View colTitle = tableDays.getChildAt(curClass.inputClass.day);
			int classLeft = colTitle.getLeft();
			int classTop = ROW_HEIGHT * curClass.inputClass.getUnitStart();

			curClass.setHeight((ROW_HEIGHT * curClass.inputClass.classLength) - 1);
			//curClass.setWidth(colWidth);
			//curClass.measure(colWidth, (ROW_HEIGHT * curClass.inputClass.classLength) - 1);
			curClass.layout(classLeft + colWidth * (curClass.inputClass.conflictPos),
							classTop,
							classLeft + colWidth * (1 + curClass.inputClass.conflictPos) - 1,
							classTop + (ROW_HEIGHT * curClass.inputClass.classLength) - 1);
		}
	}

}
