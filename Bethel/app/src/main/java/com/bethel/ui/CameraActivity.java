package com.bethel.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bethel.R;
import com.bethel.base.BaseActivity;
import com.bethel.utils.CommonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends BaseActivity implements Camera.PictureCallback, SurfaceHolder
        .Callback
{
    public static final String EXTRA_CAMERA_DATA = "camera_data";
    private static final String KEY_IS_CAPTURING = "is_capturing";
    private Camera mCamera;
    private ImageView mCameraImage;
    private SurfaceView mCameraPreview;
    private Button mCaptureImageButton;
    private byte[] mCameraData;
    private boolean mIsCapturing;
    private ProgressDialog mDialog;
    boolean imageClick;
    //    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private ArrayList<String> pathArray = new ArrayList<>();

    private View.OnClickListener mCaptureImageButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            captureImage();
        }
    };

    private View.OnClickListener mRecaptureImageButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            setupImageCapture();
        }
    };

    private View.OnClickListener mDoneButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mCameraData != null)
            {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_CAMERA_DATA, mCameraData);
                setResult(RESULT_OK, intent);
            }
            else
            {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_camera);

        mCameraImage = (ImageView) findViewById(R.id.camera_image_view);
        mCameraImage.setVisibility(View.GONE);

        mCameraPreview = (SurfaceView) findViewById(R.id.preview_view);

        final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mCaptureImageButton = (Button) findViewById(R.id.capture_image_button);
        mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);

        final Button doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(mDoneButtonClickListener);

        mIsCapturing = true;

        mDialog = CommonUtils.initIndeterminateDialog(CameraActivity.this);

        tvCancel = (TextView) findViewById(R.id.canceltv);
        tvCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (tvCancel.getText().toString().equalsIgnoreCase("Cancel"))
                {
                    finish();
                }
                else
                {
                    mCameraImage.setVisibility(View.GONE);
                    mCameraPreview.setVisibility(View.VISIBLE);

                    pathArray.remove(pathArray.size() - 1);
                    //  mCamera.stopPreview();
                    //  mCameraPreview.setVisibility(View.INVISIBLE);
                    // mCameraImage.setVisibility(View.VISIBLE);
                    // tvCancel.setText("Cancel");
                    if (pathArray.size() > 0)
                    {
                        prepareCameraSurfaceView("+ Add section");
//                        btnTakeImage.setText("+ Add section");
//                        tvHelp.setText("Done");
                    }
                    else
                    {
                        prepareCameraSurfaceView("Take picture");
//                        tvHelp.setText("Help");
//                        btnTakeImage.setText("Take picture");
                    }
                }
            }
        });

        btnTakeImage = (Button) findViewById(R.id.takeimagetv);
        btnTakeImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if(imageClick){
                    new SaveCroppedImageTask().execute();
                  /*  tvCancel.setText("Cancel");
                    btnTakeImage.setText("Take picture");
                    ((LinearLayout)findViewById(R.id.overlaylayout)).setVisibility(View.VISIBLE);
                    tvHelp.setText("Help");
                    mCameraImage.setVisibility(View.GONE);
                    mCameraPreview.setVisibility(View.VISIBLE);
                    mCamera.startPreview();
                   */ /*if (pathArray.size() > 0) {
                        ((ImageView)findViewById(R.id.overlayimage)).setVisibility(View.VISIBLE);
                        ((FrameLayout)findViewById(R.id.overlaylayoutview)).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.imagenumber)).setText(pathArray.size()+"");

//                        ((ImageView)findViewById(R.id.overlayimage)).setImageBitmap(BitmapFactory.decodeFile(pathArray.get(pathArray.size()-1)));
                        Bitmap croppedBitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(pathArray.get(pathArray.size()-1)), 0,
                                (int) (BitmapFactory.decodeFile(pathArray.get(pathArray.size()-1)).getHeight() * 0.8),
                                (BitmapFactory.decodeFile(pathArray.get(pathArray.size()-1))).getWidth(),
                                (BitmapFactory.decodeFile(pathArray.get(pathArray.size()-1)).getHeight())-
                                        (int) (BitmapFactory.decodeFile(pathArray.get(pathArray.size()-1)).getHeight() * 0.8));
                        ((ImageView)findViewById(R.id.overlayimage)).setImageBitmap(croppedBitmap);
                    }
                    imageClick=false;
                    ((TextView)findViewById(R.id.overlaytext)).setText("Line up this photo with the previous one taken.");
               */ }else{


                    captureImage();
                }
            }
        });

        tvHelp = (TextView) findViewById(R.id.helptv);
        tvHelp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (tvHelp.getText().toString().equalsIgnoreCase("Help"))
                {
                    Intent intent=new Intent(CameraActivity.this,HelpActivity.class);
                    intent.putExtra("fromCamera",true);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    if(getIntent().getExtras()!=null){
                        Intent resultIntent = new Intent();
                        Bundle bundle=new Bundle();
                        bundle.putSerializable("imagepath",pathArray);
                        resultIntent.putExtras(bundle);
                        setResult(100, resultIntent);
                    }else {
                        Intent intent = new Intent(CameraActivity.this, AddReceiptActivity.class);
                        intent.putStringArrayListExtra("user", pathArray);
                        startActivity(intent);
                    }
                    finish();
                }
            }
        });
    }

    @Override
    public int getLayout() {
        return R.layout.custom_camera;
    }


    class SaveCroppedImageTask extends AsyncTask<Void,Void,Void>{
        Bitmap croppedBitmap;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (pathArray.size() > 0) {
                try {
                    croppedBitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(pathArray.get(pathArray.size() - 1)), 0,
                            (int) (BitmapFactory.decodeFile(pathArray.get(pathArray.size() - 1)).getHeight() * 1),
                            (BitmapFactory.decodeFile(pathArray.get(pathArray.size() - 1))).getWidth(),
                            (BitmapFactory.decodeFile(pathArray.get(pathArray.size() - 1)).getHeight()) -
                                    (int) (BitmapFactory.decodeFile(pathArray.get(pathArray.size() - 1)).getHeight() *1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tvCancel.setText("Cancel");
            btnTakeImage.setText("Take picture");
            ((LinearLayout)findViewById(R.id.overlaylayout)).setVisibility(View.VISIBLE);
            tvHelp.setText("Help");
            mCameraImage.setVisibility(View.GONE);
            mCameraPreview.setVisibility(View.VISIBLE);
            mCamera.startPreview();
            if (pathArray.size() > 0) {
                ((ImageView)findViewById(R.id.overlayimage)).setVisibility(View.VISIBLE);
                ((FrameLayout)findViewById(R.id.overlaylayoutview)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.imagenumber)).setText(pathArray.size()+"");

//                        ((ImageView)findViewById(R.id.overlayimage)).setImageBitmap(BitmapFactory.decodeFile(pathArray.get(pathArray.size()-1)));

                ((ImageView)findViewById(R.id.overlayimage)).setImageBitmap(croppedBitmap);
            }
            imageClick=false;
            ((TextView)findViewById(R.id.overlaytext)).setText("Line up this photo with the previous one taken.");

            hideProgress();
        }
    }

    void prepareCameraSurfaceView(String strCamera){
        tvCancel.setText("Cancel");
        btnTakeImage.setText(strCamera);
        tvHelp.setText("Help");

        if (mCamera == null)
        {
            try
            {
                mCamera = Camera.open();
                //set camera to continually auto-focus
                Camera.Parameters params = mCamera.getParameters();
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                List<Camera.Size> sizes = params.getSupportedPictureSizes();
                Camera.Size size = sizes.get(0);
                for(int i=0;i<sizes.size();i++)
                {
                    if(sizes.get(i).width > size.width)
                        size = sizes.get(i);
                }
                params.setPictureSize(size.width, size.height);
                mCamera.setParameters(params);
                mCamera.setPreviewDisplay(mCameraPreview.getHolder());
                if (mCamera == null)
                {
                    try
                    {
                        mCamera = Camera.open();

                        mCamera.setPreviewDisplay(mCameraPreview.getHolder());
                        if (mIsCapturing)
                        {
                            mCamera.startPreview();
                            mCamera.setDisplayOrientation(0);
                        }
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(CameraActivity.this, "Unable to open camera.",
                                Toast.LENGTH_LONG).show();
                    }
                }
                mCameraImage.setVisibility(View.GONE);
                mCameraPreview.setVisibility(View.VISIBLE);

                if (mIsCapturing)
                {
                    //  mCamera.startPreview();
                    //  mCamera.setDisplayOrientation(0);
                }
            }
            catch (Exception e)
            {
                Toast.makeText(CameraActivity.this, "Unable to open camera.", Toast
                        .LENGTH_LONG).show();
            }
        }
    }

//    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation)
//    {
//        Matrix matrix = new Matrix();
//        switch (orientation)
//        {
//            case ExifInterface.ORIENTATION_NORMAL:
//                return bitmap;
//            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
//                matrix.setScale(-1, 1);
//                break;
//            case ExifInterface.ORIENTATION_ROTATE_180:
//                matrix.setRotate(180);
//                break;
//            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
//                matrix.setRotate(180);
//                matrix.postScale(-1, 1);
//                break;
//            case ExifInterface.ORIENTATION_TRANSPOSE:
//                matrix.setRotate(90);
//                matrix.postScale(-1, 1);
//                break;
//            case ExifInterface.ORIENTATION_ROTATE_90:
//                matrix.setRotate(90);
//                break;
//            case ExifInterface.ORIENTATION_TRANSVERSE:
//                matrix.setRotate(-90);
//                matrix.postScale(-1, 1);
//                break;
//            case ExifInterface.ORIENTATION_ROTATE_270:
//                matrix.setRotate(-90);
//                break;
//            default:
//                return bitmap;
//        }
//        try
//        {
//            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
//                    .getHeight(), matrix, true);
//            bitmap.recycle();
//            return bmRotated;
//        }
//        catch (OutOfMemoryError e)
//        {
//            e.printStackTrace();
//            return null;
//        }
//    }

    TextView tvCancel, tvHelp;
    Button btnTakeImage;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(KEY_IS_CAPTURING, mIsCapturing);
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        mIsCapturing = savedInstanceState.getBoolean(KEY_IS_CAPTURING, mCameraData == null);
//        if (mCameraData != null)
//        {
//        setupImageDisplay();
//        }
//        else
//        {
        setupImageCapture();
//        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (mCamera == null)
        {
            try
            {
                mCamera = Camera.open();
                Camera.Parameters params = mCamera.getParameters();
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                List<Camera.Size> sizes = params.getSupportedPictureSizes();
                Camera.Size size = sizes.get(0);
                for(int i=0;i<sizes.size();i++)
                {
                    if(sizes.get(i).width > size.width)
                        size = sizes.get(i);
                }
                params.setPictureSize(size.width, size.height);
                mCamera.setParameters(params);

                mCamera.setPreviewDisplay(mCameraPreview.getHolder());
                if (mIsCapturing)
                {
                    mCamera.startPreview();
                    mCamera.setDisplayOrientation(0);
                }
            }
            catch (Exception e)
            {
                Toast.makeText(CameraActivity.this, "Unable to open camera.", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (mCamera != null)
        {
            try
            {
                mCamera.setPreviewDisplay(holder);
                if (mIsCapturing)
                {
                    mCamera.setDisplayOrientation(90);
                    mCamera.startPreview();

                }
            }
            catch (IOException e)
            {
                Toast.makeText(CameraActivity.this, "Unable to start camera preview.", Toast
                        .LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    private void captureImage()
    { try {
        mCamera.takePicture(null, null, this);
    }catch (Exception e){
        e.printStackTrace();
    }
    }

    private void setupImageCapture()
    {
        mCameraImage.setVisibility(View.GONE);
        mCameraPreview.setVisibility(View.VISIBLE);
        mCamera.startPreview();
//         mCaptureImageButton.setText(R.string.capture_image);
        mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {
//        mCameraData = data;
//        byte[] byteArray;
//        ByteArrayOutputStream stream = null;
        try {
            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

            new SaveImageTask(realImage,info.orientation).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
//        byteArray = stream.toByteArray();
    }

    class SaveImageTask extends AsyncTask<Byte, Void, String>
    {
        Bitmap bitmap;
        int orientation;

        public SaveImageTask(Bitmap bitmap, int orientation)
        {
            this.bitmap = bitmap;
            this.orientation = orientation;
        }

        @Override
        protected void onPreExecute()
        {
            CommonUtils.showDialog(mDialog);
        }

        @Override
        protected String doInBackground(Byte... params)
        {
            Bitmap rotatedBitmap = rotate(bitmap, orientation);
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                    System.currentTimeMillis() + ".jpg";
            File pictureFile = new File(path);
            if (pictureFile == null)
            {
                return null;
            }
            try
            {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                fos.flush();
                fos.close();
                return path;
            }
            catch (FileNotFoundException e)
            {
                return null;
            }
            catch (IOException e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s)
        {
            CommonUtils.dismissDialog(mDialog);
            if (s != null)
            {
                setupImageDisplay(s);
            }
        /*    mCameraPreview.setVisibility(View.GONE);
          //  mCameraImage.setVisibility(View.VISIBLE);
            tvCancel.setText("Re-Take");
            btnTakeImage.setText("+ Add section");
            tvHelp.setText("Done");*/
            imageClick=true;


        }
    }



    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
    private void setupImageDisplay(String path)
    {
//        Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
        //  bitmap=rotateBitmap(bitmap,270);
        try
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            Bitmap bmp = BitmapFactory.decodeFile(path);
         /*   mCameraImage.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(path),
                    null, null));*/
            mCameraImage.setImageBitmap(bmp);

        }
        catch (Exception e)
        {
        }
//        bitmaps.add(bitmap);
        pathArray.add(path);
        //mCamera.stopPreview();
//      //mCamera=null;
        //mCameraPreview.setVisibility(View.GONE);
        mCamera.stopPreview();
        mCameraPreview.setVisibility(View.GONE);
        mCameraImage.setVisibility(View.VISIBLE);
        tvCancel.setText("Re-Take");
        btnTakeImage.setText("+ Add section");
        tvHelp.setText("Done");
//        ((LinearLayout)findViewById(R.id.overlaylayout)).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.overlaytext)).setText("Can you clearly read the date and vendor? if not, press RETAKE.");
        // mCamera = null;
        //mCaptureImageButton.setText(R.string.recapture_image);
        mCaptureImageButton.setOnClickListener(mRecaptureImageButtonClickListener);
    }
    ////////////////////////////////////////////////////

//    public void handleImageOrientation(@NonNull final String path)
//    {
//        try
//        {
//            CommonUtils.showDialog(mDialog);
//            ExifInterface exifInterface = new ExifInterface(path);
//            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_NORMAL);
//            if (orientation != ExifInterface.ORIENTATION_NORMAL)
//            {
//                new Thread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        rotateImage(path);
//                        runOnUiThread(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                CommonUtils.dismissDialog(mDialog);
//                                setupImageDisplay(path);
//                            }
//                        });
//                    }
//                }).start();
//            }
//            else
//            {
//                CommonUtils.dismissDialog(mDialog);
//                setupImageDisplay(path);
//            }
//        }
//        catch (Exception e)
//        {
//            CommonUtils.dismissDialog(mDialog);
//        }
//    }
//
//    private void rotateImage(@NonNull final String imagePath)
//    {
//        try
//        {
//            ExifInterface exifInterface = new ExifInterface(imagePath);
//            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_NORMAL);
//            int angle = 0;
//
//            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
//            {
//                angle = 90;
//            }
//            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
//            {
//                angle = 180;
//            }
//            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
//            {
//                angle = 270;
//            }
//            if (angle != 0)
//            {
//                Matrix mat = new Matrix();
//                mat.postRotate(angle);
//
//                Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(imagePath), null, null);
//                Bitmap correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight
//                        (), mat, true);
//
//                FileOutputStream out = new FileOutputStream(imagePath);
//                correctBmp.compress(Bitmap.CompressFormat.JPEG, 75, out);
//                out.flush();
//                out.close();
//            }
//        }
//        catch (Exception e)
//        {
//        }
//    }
}