package com.info.idol.community.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.info.idol.community.Class.ResizeFactory;
import com.info.idol.community.R;

import java.io.IOException;
import java.util.ArrayList;

public class RecyclerImageAdapter extends RecyclerView.Adapter<RecyclerImageAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Uri> mUris;

    public RecyclerImageAdapter(Context context, ArrayList<Uri> uris) {
        mContext = context;
        mUris = uris;
    }

    @NonNull
    @Override
    public RecyclerImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerImageAdapter.ViewHolder holder, int position) {
        //먼저 메모리에 올리지 않고 이미지 크기 알아냄
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mUris.get(position).toString(), options);
        //얼마큼 샘플링해야하는지 구함
        options.inSampleSize = ResizeFactory.calculateInSampleSize(options, ResizeFactory.dpToPx(mContext, 150), ResizeFactory.dpToPx(mContext, 150));

        options.inJustDecodeBounds = false;
        Bitmap bmp = BitmapFactory.decodeFile(mUris.get(position).toString(), options);

        holder.mImageView.setImageBitmap(rotateImage(bmp,mUris.get(position)));

    }

    @Override
    public int getItemCount() {
        return mUris.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageView);
        }

        @Override
        public void onClick(View view) {

        }
    }


    private Bitmap rotateImage(Bitmap bitmap, Uri uri) {
        ExifInterface exif = null;
        Bitmap result=null;
        try {
            exif = new ExifInterface(uri.toString());
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            result=rotate(bitmap,exifDegree);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * EXIF정보를 회전각도로 변환하는 메서드
     *
     * @param exifOrientation EXIF 회전각
     * @return 실제 각도
     */
    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }


    /**
     * 이미지를 회전시킵니다.
     *
     * @param bitmap  비트맵 이미지
     * @param degrees 회전 각도
     * @return 회전된 이미지
     */
    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;

    }
}
