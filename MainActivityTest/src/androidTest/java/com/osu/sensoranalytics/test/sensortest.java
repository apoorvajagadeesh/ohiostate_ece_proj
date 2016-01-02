package com.osu.sensoranalytics.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class sensortest extends ActivityInstrumentationTestCase2 {
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
    public sensortest() throws ClassNotFoundException {
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
        //Sleep for 3569 milliseconds
        solo.sleep(3569);
        //Assert that: 'Acceleration and Gravity' is shown
        assertTrue("'Acceleration and Gravity' is not shown!", solo.waitForView(solo.getView("accHeader")));
        //Sleep for 2061 milliseconds
        solo.sleep(2061);
        //Assert that: 'Rotation' is shown
        assertTrue("'Rotation' is not shown!", solo.waitForView(solo.getView("rotHeader")));
        //Sleep for 1911 milliseconds
        solo.sleep(1911);
        //Assert that: 'Magnetic Field' is shown
        assertTrue("'Magnetic Field' is not shown!", solo.waitForView(solo.getView("magHeader")));
        //Sleep for 1167 milliseconds
        solo.sleep(1167);
        //Assert that: 'Orientation' is shown
        assertTrue("'Orientation' is not shown!", solo.waitForView(solo.getView("orHeader")));
        //Sleep for 1300 milliseconds
        solo.sleep(1300);
        //Assert that: 'Miscellaneous' is shown
        assertTrue("'Miscellaneous' is not shown!", solo.waitForView(solo.getView("miscHeader")));
        //Sleep for 4600 milliseconds
        solo.sleep(4600);
        //Click on TabView
        solo.clickOnView(solo.getView(android.widget.LinearLayout.class, 55));
        //Sleep for 2399 milliseconds
        solo.sleep(2399);
        //Assert that: 'Latitude' is shown
        assertTrue("'Latitude' is not shown!", solo.waitForView(solo.getView("textView2")));
        //Sleep for 1267 milliseconds
        solo.sleep(1267);
        //Assert that: 'Longitude' is shown
        assertTrue("'Longitude' is not shown!", solo.waitForView(solo.getView("textView4")));
        //Sleep for 803 milliseconds
        solo.sleep(803);
        //Assert that: 'Elevation' is shown
        assertTrue("'Elevation' is not shown!", solo.waitForView(solo.getView("textView8")));
        //Sleep for 1253 milliseconds
        solo.sleep(1253);
        //Assert that: 'Bearing' is shown
        assertTrue("'Bearing' is not shown!", solo.waitForView(solo.getView("textView12")));
        //Sleep for 870 milliseconds
        solo.sleep(870);
        //Assert that: 'Speed' is shown
        assertTrue("'Speed' is not shown!", solo.waitForView(solo.getView("textView6")));
        //Sleep for 698 milliseconds
        solo.sleep(698);
        //Assert that: 'Error' is shown
        assertTrue("'Error' is not shown!", solo.waitForView(solo.getView("textView16")));
        //Sleep for 658 milliseconds
        solo.sleep(658);
        //Assert that: 'Satellites' is shown
        assertTrue("'Satellites' is not shown!", solo.waitForView(solo.getView("textView17")));
        //Sleep for 875 milliseconds
        solo.sleep(875);
        //Assert that: 'Decl.' is shown
        assertTrue("'Decl.' is not shown!", solo.waitForView(solo.getView("textView32")));
        //Sleep for 634 milliseconds
        solo.sleep(634);
        //Assert that: 'TTFF (s)' is shown
        assertTrue("'TTFF (s)' is not shown!", solo.waitForView(solo.getView("textView19", 1)));
        //Sleep for 714 milliseconds
        solo.sleep(714);
        //Assert that: 'Last Fix Obtained' is shown
        assertTrue("'Last Fix Obtained' is not shown!", solo.waitForView(solo.getView("textView10")));
    }
}
