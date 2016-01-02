package com.osu.sensoranalytics.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class sensortest2 extends ActivityInstrumentationTestCase2 {
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
    public sensortest2() throws ClassNotFoundException {
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
        //Sleep for 22540 milliseconds
        solo.sleep(22540);
        //Assert that: 'Acceleration and Gravity' is shown
        assertTrue("'Acceleration and Gravity' is not shown!", solo.waitForView(solo.getView("accHeader")));
        //Sleep for 1179 milliseconds
        solo.sleep(1179);
        //Assert that: 'Rotation' is shown
        assertTrue("'Rotation' is not shown!", solo.waitForView(solo.getView("rotHeader")));
        //Sleep for 1040 milliseconds
        solo.sleep(1040);
        //Assert that: 'Magnetic Field' is shown
        assertTrue("'Magnetic Field' is not shown!", solo.waitForView(solo.getView("magHeader")));
        //Sleep for 802 milliseconds
        solo.sleep(802);
        //Assert that: 'Orientation' is shown
        assertTrue("'Orientation' is not shown!", solo.waitForView(solo.getView("orHeader")));
        //Sleep for 867 milliseconds
        solo.sleep(867);
        //Assert that: 'Miscellaneous' is shown
        assertTrue("'Miscellaneous' is not shown!", solo.waitForView(solo.getView("miscHeader")));
        //Sleep for 8794 milliseconds
        solo.sleep(8794);
        //Click on TabView
        solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 55));
        //Sleep for 1706 milliseconds
        solo.sleep(1706);
        //Assert that: 'Latitude' is shown
        assertTrue("'Latitude' is not shown!", solo.waitForView(solo.getView("textView2")));
        //Sleep for 915 milliseconds
        solo.sleep(915);
        //Assert that: 'Longitude' is shown
        assertTrue("'Longitude' is not shown!", solo.waitForView(solo.getView("textView4")));
        //Sleep for 610 milliseconds
        solo.sleep(610);
        //Assert that: 'Elevation' is shown
        assertTrue("'Elevation' is not shown!", solo.waitForView(solo.getView("textView8")));
        //Sleep for 874 milliseconds
        solo.sleep(874);
        //Assert that: 'Satellites' is shown
        assertTrue("'Satellites' is not shown!", solo.waitForView(solo.getView("textView17")));
        //Sleep for 595 milliseconds
        solo.sleep(595);
        //Assert that: 'Error' is shown
        assertTrue("'Error' is not shown!", solo.waitForView(solo.getView("textView16")));
        //Sleep for 637 milliseconds
        solo.sleep(637);
        //Assert that: 'Speed' is shown
        assertTrue("'Speed' is not shown!", solo.waitForView(solo.getView("textView6")));
        //Sleep for 561 milliseconds
        solo.sleep(561);
        //Assert that: 'Bearing' is shown
        assertTrue("'Bearing' is not shown!", solo.waitForView(solo.getView("textView12")));
        //Sleep for 627 milliseconds
        solo.sleep(627);
        //Assert that: 'Decl.' is shown
        assertTrue("'Decl.' is not shown!", solo.waitForView(solo.getView("textView32")));
        //Sleep for 569 milliseconds
        solo.sleep(569);
        //Assert that: 'TTFF (s)' is shown
        assertTrue("'TTFF (s)' is not shown!", solo.waitForView(solo.getView("textView19", 1)));
        //Sleep for 578 milliseconds
        solo.sleep(578);
        //Assert that: 'Last Fix Obtained' is shown
        assertTrue("'Last Fix Obtained' is not shown!", solo.waitForView(solo.getView("textView10")));
    }
}
