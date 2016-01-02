

package com.osu.sensoranalytics.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GpsSatellite;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GpsSnrView extends View {
	private String TAG = "GpsSnrView";

	private final int MAX_NMEA_ID = 235;

	private Iterable<GpsSatellite> mSats;

	private Paint activePaint;
	private Paint inactivePaint;
	private Paint gridPaint;
	private Paint gridPaintStrong;
	private Paint gridPaintNone;


	private int gridStrokeWidth = 2;


	private boolean draw_1_32 = false;
	private boolean draw_33_54 = false;
	private boolean draw_55_64 = false;
	private boolean draw_65_88 = false;
	private boolean draw_89_96 = false;
	private boolean draw_97_192 = false;
	private boolean draw_193_195 = false;
	private boolean draw_196_200 = false;
	private boolean draw_201_235 = false;



	public GpsSnrView(Context context) {
		super(context);
		doInit();
	}

	public GpsSnrView(Context context, AttributeSet attrs) {
		super(context, attrs);
		doInit();
	}

	public GpsSnrView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		doInit();
	}

	private void doInit() {
		activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		activePaint.setColor(Color.parseColor("#FF33B5E5"));
		activePaint.setStyle(Paint.Style.FILL);

		inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		inactivePaint.setColor(Color.parseColor("#FFFF4444"));
		inactivePaint.setStyle(Paint.Style.FILL);

		gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		gridPaint.setColor(Color.parseColor("#FF4D4D4D"));
		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setStrokeWidth(gridStrokeWidth);

		gridPaintStrong = new Paint(gridPaint);
		gridPaintStrong.setColor(Color.parseColor("#FFFFFFFF"));

		gridPaintNone = new Paint(gridPaint);
		gridPaintNone.setColor(Color.parseColor("#00000000"));
	}


	private void drawGrid(Canvas canvas) {

		int w = getWidth();
		int h = getHeight();

		// left boundary
		canvas.drawLine((float) gridStrokeWidth / 2, 0, (float) gridStrokeWidth / 2, h, gridPaintStrong);
		
		// range boundaries and auxiliary lines (after every 4th satellite)
		for (int nmeaID = 1; nmeaID < MAX_NMEA_ID; nmeaID++) {
			int pos = getGridPos(nmeaID);
			if (pos > 0) {
				float x = (float) gridStrokeWidth / 2
						+ pos * (w - gridStrokeWidth) / getNumBars();
				Paint paint = gridPaintNone;
				switch(nmeaID) {
				case 32:
				case 64:
				case 96:
				case 192:
				case 200:
				case 235:
					paint = gridPaintStrong;
					break;
				case 54:
					if (!draw_55_64)
						paint = gridPaintStrong;
					break;
				case 88:
					if (!draw_89_96)
						paint = gridPaintStrong;
					else
						paint = gridPaint;
					break;
				case 195:
					if (!draw_196_200)
						paint = gridPaintStrong;
				default:
					if ((nmeaID % 4) == 0)
						paint = gridPaint;
					break;
				}
				canvas.drawLine(x, 0, x, h, paint);
			}
		}
		
		// right boundary
		canvas.drawLine(w - (float) gridStrokeWidth / 2, h, w - (float) gridStrokeWidth / 2, 0, gridPaintStrong);
		
		// bottom line
		canvas.drawLine(0, h - (float) gridStrokeWidth / 2, w, h - (float) gridStrokeWidth / 2, gridPaintStrong);
	}
	

	private void drawSat(Canvas canvas, int nmeaID, float snr, boolean used) {
		int w = getWidth();
		int h = getHeight();

		int i = getGridPos(nmeaID);

		int x0 = (i - 1) * (w - gridStrokeWidth) / getNumBars() + gridStrokeWidth / 2;
		int x1 = i * (w - gridStrokeWidth) / getNumBars() - gridStrokeWidth / 2;

		int y0 = h - gridStrokeWidth;
		int y1 = (int) (y0 * (1 - Math.min(snr, 60) / 60));

		canvas.drawRect(x0, y1, x1, h, used?activePaint:inactivePaint);
	}


	private int getGridPos(int nmeaID) {
		if (nmeaID < 1) return -1;
		
		int skip = 0;
		if (nmeaID > 32) {
			if (!draw_1_32) skip+=32;
			if (nmeaID > 54) {
				if (!draw_33_54) skip+=22;
				if (nmeaID > 64) {
					if (!draw_55_64) skip+=10;
					if (nmeaID > 88) {
						if (!draw_65_88) skip+=24;
						if (nmeaID > 96) {
							if (!draw_89_96) skip+=8;
							if (nmeaID > 192) {
								if (!draw_97_192) skip+=96;
								if (nmeaID > 195) {
									if (!draw_193_195) skip+=3;
									if (nmeaID > 200) {
										if (nmeaID > MAX_NMEA_ID) return -1;
										else if (!draw_201_235) return -1;
										else if (!draw_196_200) skip+=5;
									} else {
										// 195 < nmeaID <= 200
										if (!draw_196_200) return -1;
									}
								} else {
									// 192 < nmeaID <= 195
									if (!draw_193_195) return -1;
								}
							} else {
								// 96 < nmeaID <= 192
								if (!draw_97_192) return -1;
							}
						} else {
							// 88 < nmeaID <= 96
							if (!draw_89_96) return -1;
						}
					} else {
						// 64 < nmeaID <= 88
						if (!draw_65_88) return -1;
					}
				} else {
					// 54 < nmeaID <= 64
					if (!draw_55_64) return -1;
				}
			} else {
				// 32 < nmeaID <= 54
				if (!draw_33_54) return -1;
			}
		} else {
			// nmeaID <= 32
			if (!draw_1_32) return -1;
		}
		
		return nmeaID - skip;
	}
	

	private int getNumBars() {
		return (draw_1_32 ? 32 : 0) 
				+ (draw_33_54 ? 22 : 0)
				+ (draw_55_64 ? 10 : 0)
				+ (draw_65_88 ? 24 : 0)
				+ (draw_89_96 ? 8 : 0)
				+ (draw_97_192 ? 96 : 0)
				+ (draw_193_195 ? 3 : 0)
				+ (draw_196_200 ? 5 : 0)
				+ (draw_201_235 ? 35 : 0);
	}


	protected void initializeGrid() {
		// iterate through list to find out how many bars to draw
		if (mSats != null)
			for (GpsSatellite sat : mSats) {
				int prn = sat.getPrn();
				if (prn < 1) {
					Log.wtf(TAG, String.format("Got satellite with invalid NMEA ID %d", prn));
				} else if (prn <= 32) {
					draw_1_32 = true;
				} else if (prn <= 54) {
					draw_33_54 = true;
				} else if (prn <= 64) {
					// most likely an extended SBAS range, display the lower range, too
					draw_33_54 = true;
					draw_55_64 = true;
				} else if (prn <= 88) {
					draw_65_88 = true;
				} else if (prn <= 96) {
					// most likely an extended GLONASS range, display the lower range, too
					draw_65_88 = true;
					draw_89_96 = true;
				} else if (prn <= 192) {
					draw_97_192 = true; // TODO: do we really want to enable this huge 96-sat block?
					Log.w(TAG, String.format("Got satellite with NMEA ID %d (from the huge unassigned 97-192 range)", prn));
				} else if (prn <= 195) {
					draw_193_195 = true;
				} else if (prn <= 200) {
					// most likely an extended QZSS range, display the lower range, too
					draw_193_195 = true;
					draw_196_200 = true;
				} else if (prn <= 235) {
					draw_201_235 = true;
				} else {
					Log.w(TAG, String.format("Got satellite with NMEA ID %d, possibly unsupported system", prn));
				}
			}

		if (!(draw_1_32 || draw_33_54 || draw_65_88 || draw_97_192 || draw_193_195 || draw_201_235))
			draw_1_32 = true;
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		initializeGrid();
		
		// draw the SNR bars
		if (mSats != null)
			for (GpsSatellite sat : mSats)
				drawSat(canvas, sat.getPrn(), sat.getSnr(), sat.usedInFix());
		
		// draw the grid on top
		drawGrid(canvas);
	}

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		int mHeight = (int) Math.min(MeasureSpec.getSize(widthMeasureSpec) * 0.15f, MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
	}


	public void showSats(Iterable<GpsSatellite> sats) {
		mSats = sats;
		invalidate();
	}
}
