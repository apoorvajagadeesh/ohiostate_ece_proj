package com.osu.sensoranalytics.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class testtabswitch extends ActivityInstrumentationTestCase2 {
  	private Solo solo;
  	
  	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "com.osu.sensoranalytics.MainActivity";

    private static Class<?> launcherActivityClass;
    static{
        try {
            launcherActivityClass = Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
        } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
        }
    }
  	
  	@SuppressWarnings("unchecked")
    public testtabswitch() throws ClassNotFoundException {
        super(launcherActivityClass);
    }

  	public void setUp() throws Exception {
        super.setUp();
		solo = new Solo(getInstrumentation());
		getActivity();
  	}
  
   	@Override
   	public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
  	}
  
	public void testRun() {
        //Wait for activity: 'com.osu.sensoranalytics.MainActivity'
		solo.waitForActivity("MainActivity", 2000);
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 55));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 56));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 26));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 7));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 24));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 54));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 57));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 6));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 56));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 23));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 57));
        //Click on TabView
		solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 6));
        //Click on ImageView

	}
}
