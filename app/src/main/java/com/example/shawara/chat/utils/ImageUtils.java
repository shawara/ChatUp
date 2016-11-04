package com.example.shawara.chat.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Pair;

import com.example.shawara.chat.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import id.zelory.compressor.Compressor;

/**
 * Created by shawara on 4/24/2016.
 */
public class ImageUtils {
    public static File mFile;


    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        return mFile = new File(storageDir, timeStamp);
    }


    public static Bitmap getFileBitmap(String path) {
        return BitmapFactory.decodeFile(path);
    }


    public static Pair<Integer, Integer> getDimentions(String path, float destWidth) {
        // read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        float destHeight;
        if (destWidth > (int) Math.min(srcHeight, srcHeight))
            return new Pair<>(new Integer((int) srcWidth), new Integer((int) srcHeight));


        if (srcWidth < srcHeight) {
            destHeight = (destWidth / srcWidth) * srcHeight;
        } else {
            destHeight = (destWidth / srcHeight) * -srcWidth;
        }


        if (destHeight >= 0)
            return new Pair<>(new Integer((int) destWidth), new Integer((int) destHeight));
        else
            return new Pair<>(new Integer((int) -destHeight), new Integer((int) destWidth));
    }


    public static File CompressImage(int w, Context context, String path) {
        Pair<Integer, Integer> p = getDimentions(/*mFile.getAbsolutePath()*/ path, w);
        return new Compressor.Builder(context)
                .setMaxWidth(p.first)
                .setMaxHeight(p.second)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .build()
                .compressToFile(/*ImageConverter.mFile*/new File(path));
    }


    public static int getPrefWidth(Context context) {
        String q = PreferenceManager.getDefaultSharedPreferences(context).
                getString(context.getResources().
                        getString(R.string.pref_quality_key), "720");
        return Integer.parseInt(q);
    }


    public static Uri getImageFileCompressedUri(Uri uri, int w, Context c) {
        String path = getPath(c.getContentResolver(), uri);
        File file = CompressImage(w, c, path);
        return Uri.fromFile(file);
    }


    public static String getPath(ContentResolver cr, Uri uri) {
        // just some safety built in
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = cr.query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    /*
    public static String ConvertPicToStr(String path) {
        //       Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
        //             R.drawable.back);//your image
        Bitmap bmp = getFileBitmap(path);
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        bmp.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return imageFile;
    }

    public static Bitmap ConvertStrToPic(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
*/

    /*
    public static File compressPhoto() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BitmapFactory.decodeFile(ImageConverter.mFile.getPath()).compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        mFile = null;
        mFile = createImageFile();
        FileOutputStream fo = new FileOutputStream(mFile);
        fo.write(bytes.toByteArray());

// remember close de FileOutput
        fo.close();
        return mFile;
    }
*/

}
