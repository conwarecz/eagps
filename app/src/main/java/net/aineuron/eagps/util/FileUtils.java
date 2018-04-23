package net.aineuron.eagps.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 06.09.2017.
 */

public class FileUtils {
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static byte[] fileToByteArray(File file) throws FileNotFoundException, IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        int byteLength = (int) file.length(); //bytecount of the file-content
        byte[] filecontent = new byte[byteLength];
        fileInputStream.read(filecontent, 0, byteLength);
        return filecontent;
    }

    public static byte[] fileToByteArray2(File file) {
        try {
            FileInputStream input = new FileInputStream(file);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[65536];
            int l;

            while ((l = input.read(buffer)) > 0)
                output.write(buffer, 0, l);

            input.close();
            output.close();

            return output.toByteArray();

        } catch (IOException e) {
            System.err.println(e);
            return null;
        }
    }

    public static String imageFileToBase64(Context context, File file) {
        Bitmap bm = null;
        try {
            bm = Glide.with(context).load(file).asBitmap().atMost().override(1500, 1500).fitCenter().into(SimpleTarget.SIZE_ORIGINAL, SimpleTarget.SIZE_ORIGINAL).get();
        } catch (InterruptedException e) {
            bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            Crashlytics.logException(e);
            e.printStackTrace();
        } catch (ExecutionException e) {
            bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        if (bm == null) {
            return null;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] b = baos.toByteArray();
            return Base64.encodeToString(b, Base64.DEFAULT);
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Fotografii se nepodařilo zpracovat", Toast.LENGTH_LONG).show());
            return null;
        }
    }
}
