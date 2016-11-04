package com.example.shawara.chat.widget;

/**
 * Created by shawara on 9/28/2016.
 */


import android.graphics.BitmapShader;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class CircleTransform implements Transformation {
    public final static int CIRCLE_BITMAP = 1;
    public final static int ROUNDED_EDGES_BITMAP = 2;


    private int mShape = CIRCLE_BITMAP;

    public CircleTransform(int shape) {
        mShape = shape;
    }

    public CircleTransform() {

    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        float r = size / 2f;
        if (mShape == CIRCLE_BITMAP) {
            canvas.drawCircle(r, r, r, paint);
        } else {
            canvas.drawRoundRect(rectF, 15, 15, paint);
        }

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}