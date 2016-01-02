package com.osu.sensoranalytics.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class testsensorapoj extends ActivityInstrumentationTestCase2 {
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
    public testsensorapoj() throws ClassNotFoundException {
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
        //Sleep for 3944 milliseconds
        solo.sleep(3944);
        //Assert that: 'Acceleration and Gravity' is shown
        assertTrue("'Acceleration and Gravity' is not shown!", solo.waitForView(solo.getView("accHeader")));
        //Sleep for 715 milliseconds
        solo.sleep(715);
        //Assert that: 'Rotation' is shown
        assertTrue("'Rotation' is not shown!", solo.waitForView(solo.getView("rotHeader")));
        //Sleep for 681 milliseconds
        solo.sleep(681);
        //Assert that: 'Magnetic Field' is shown
        assertTrue("'Magnetic Field' is not shown!", solo.waitForView(solo.getView("magHeader")));
        //Sleep for 706 milliseconds
        solo.sleep(706);
        //Assert that: 'Orientation' is shown
        assertTrue("'Orientation' is not shown!", solo.waitForView(solo.getView("orHeader")));
        //Sleep for 807 milliseconds
        solo.sleep(807);
        //Assert that: 'Miscellaneous' is shown
        assertTrue("'Miscellaneous' is not shown!", solo.waitForView(solo.getView("miscHeader")));
    }
}
