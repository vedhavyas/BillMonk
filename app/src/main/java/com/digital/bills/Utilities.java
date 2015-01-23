package com.digital.bills;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.digital.bills.roundDrawable.ColorGenerator;
import com.digital.bills.roundDrawable.RoundDrawable;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Authored by vedhavyas on 3/12/14.
 * Project JaagrT
 */

public class Utilities {

    private static final String OBJECT_LOG_FILE = "Object_log.txt";
    private static final String HEADERS = "TIME  ------------------  STATUS";




    public static byte[] getBlob(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] blob = null;
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            blob = stream.toByteArray();
        }

        return blob;
    }

    public static Bitmap getBitmapFromBlob(byte[] blob) {
        Bitmap bitmap = null;
        if (blob != null) {
            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
        return bitmap;
    }

    public static Bitmap compressBitmap(Bitmap original) {
        Bitmap compressed = null;
        if (original != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG, 40, out);
            compressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        }

        return compressed;
    }


    public static Bitmap getReSizedBitmap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int maxSize = 200;
        Bitmap reSizedBitmap = null;

        float bitmapRatio = (float) width / (float) height;
        try {
            if (bitmapRatio > 0) {
                width = maxSize;
                height = (int) (width / bitmapRatio);
            } else {
                height = maxSize;
                width = (int) (height * bitmapRatio);
            }
            reSizedBitmap = Bitmap.createScaledBitmap(image, width, height, true);
        } catch (Exception e) {
        }

        return reSizedBitmap;
    }

    public static Drawable getRoundedDrawable(Context context, String data) {
        ColorGenerator colorGenerator = ColorGenerator.DEFAULT;
        int fontSize = 50;

        String finalData = "";
        String[] dataSet = data.split(" ");
        if (dataSet.length > 1) {
            for (int i = 0; i < 2; i++) {
                finalData += dataSet[i].substring(0, 1);
            }
        } else {
            finalData = data.substring(0, 1);
        }

        return RoundDrawable.builder()
                .beginConfig()
                .fontSize(fontSize)
                .toUpperCase()
                .endConfig()
                .buildRect(finalData, colorGenerator.getColor(finalData));
    }


}
