package com.news.nytesttimes.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

/**
 * Created by vikasrathour on 30/09/15.
 */
public class NewsHttpUtil {

    static int _1KB = 1024;
    private static final int[] BUFFER_SIZES = {8 * _1KB, 16 * _1KB, 32 * _1KB,
            64 * _1KB};

    public static int getNormalisedBufferSize(long fileSize) {
        long fivePercent = (fileSize / 100) * 5;
        for (int i = 0; i < BUFFER_SIZES.length; i++) {
            if (fivePercent <= BUFFER_SIZES[i]) {
                return BUFFER_SIZES[i];
            }
        }
        return BUFFER_SIZES[0];

    }

    public static String getARandomImagePath() {

        Random random = new Random();
        return imagePaths[random.nextInt(imagePaths.length)];
    }

    public static String[] imagePaths = {"http://farm8.staticflickr.com/7315/9046944633_881f24c4fa_s.jpg",
            "http://farm4.staticflickr.com/3777/9049174610_bf51be8a07_s.jpg",
            "http://farm8.staticflickr.com/7324/9046946887_d96a28376c_s.jpg",
            "http://farm3.staticflickr.com/2828/9046946983_923887b17d_s.jpg",
            "http://farm4.staticflickr.com/3810/9046947167_3a51fffa0b_s.jpg"};

    public static Bitmap getLasTImageBitmap(byte[] image) {
        Bitmap bitmap = null;
        if (image != null) {
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        }
        return bitmap;
    }

}
