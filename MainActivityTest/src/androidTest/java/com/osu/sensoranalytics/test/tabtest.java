package com.osu.sensoranalytics.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class tabtest extends ActivityInstrumentationTestCase2 {
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
    public tabtest() throws ClassNotFoundException {
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
        //Sleep for 3917 milliseconds
        solo.sleep(3917);
        //Click on TabView
        solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 55));
        //Sleep for 916 milliseconds
        solo.sleep(916);
        //Click on TabView
        solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 54));
        //Sleep for 566 milliseconds
        solo.sleep(566);
        //Click on TabView
        solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 55));
        //Click on TabView
        solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 54));
        //Sleep for 534 milliseconds
        solo.sleep(534);
        //Click on TabView
        solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 55));
        //Click on TabView
        solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 54));
    }
}
