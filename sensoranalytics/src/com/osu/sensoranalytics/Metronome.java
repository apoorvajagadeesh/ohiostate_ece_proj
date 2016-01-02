package com.osu.sensoranalytics;

import org.apache.commons.lang3.ArrayUtils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.util.jar.Manifest;


public class Metronome extends Thread {

    Metronome(Context context) {

    }

    @Override
    public void run() {
        while (MainActivity.bpm != -1)
        {

            int[] time = ArrayUtils.toPrimitive((Integer[]) MainActivity.bpmQueue.toArray(new Integer[0]));
            int bpm = 1000;

            if (MainActivity.bpmQueue.size() > 0)
            {
                bpm = 0;


                for (Object b : MainActivity.bpmQueue)
                {
                    bpm += (Integer)b;
                }
                bpm = bpm / MainActivity.bpmQueue.size();


            }


            try {
                int msPerBeat = (int) (60f /(bpm + 1) * 1000);
                Log.d(MainActivity.TAG,"Average BPM:"+ bpm + " msPerBeat:" + msPerBeat);
                int sleep = Math.max(200, Math.min(2000, msPerBeat));
                Log.d(MainActivity.TAG, "sleeping: " + sleep);
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                Log.e(MainActivity.TAG,"error", e);
            }

        }


    }
}