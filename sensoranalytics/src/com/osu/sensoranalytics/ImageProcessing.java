package com.osu.sensoranalytics;


public abstract class ImageProcessing {
    final static int max_pixel = 262143;
    final static int r_min = (0 << 6) & 0xff0000 ;
    final static int r_max = (max_pixel<<6) & 0xff0000;
    final static int g_min = (0 >> 2) & 0xff00;
    final static int g_max = (max_pixel >> 2) & 0xff00;
    final static int b_min = (0 >> 10) & 0xff;
    final static int b_max = (max_pixel >> 10) & 0xff;
    private static int decodeYUV420SPtoRedSum(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) return 0;

        final int frameSize = width * height;

        int sum = 0;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & yuv420sp[yp]) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                int pixel = 0xff000000 ;
                if (r < 0) {
                    //r = 0;
                    pixel = pixel | r_min;
                }
                else if (r > max_pixel) {
                    // r = max_pixel;
                    pixel = pixel | r_max;
                }
                if (g < 0) {
                    // g = 0;
                    pixel = pixel | g_min;
                }
                else if (g > max_pixel) {
                    //g = max_pixel;
                    pixel = pixel | g_max;
                }
                if (b < 0) {
                    //b = 0;
                    pixel = pixel | b_min;
                }
                else if (b > max_pixel){
                    //b = max_pixel;
                    pixel = pixel | b_max;
                }

                //int pixel = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                // int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                sum += green; // I hear green works better than RED!
            }
        }
        return sum;
    }


    public static int decodeYUV420SPtoRedAvg(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) return 0;

        final int frameSize = width * height;

        int sum = decodeYUV420SPtoRedSum(yuv420sp, width, height);
        return (sum / frameSize);
    }
}
