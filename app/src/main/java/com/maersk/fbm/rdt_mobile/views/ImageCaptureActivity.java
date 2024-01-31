package com.maersk.fbm.rdt_mobile.views;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.maersk.fbm.rdt_mobile.R;
import com.maersk.fbm.rdt_mobile.entity.ImageEntity;
import com.maersk.fbm.rdt_mobile.viewModel.ImageCaptureViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class ImageCaptureActivity extends AppCompatActivity {

    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    private ImageCaptureViewModel imageCaptureViewModel;
    private LinearLayout container;

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private String mCameraId;
    private Size mCaptureSize;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private static Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageCaptureViewModel = new ViewModelProvider(this).get(ImageCaptureViewModel.class);
        initView();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ImageEntity image = (ImageEntity) msg.obj;
                configureImageViews(image);
            }
        };

    }

    private void initView() {
        mTextureView = findViewById(R.id.textureView);
        container = findViewById(R.id.horizontalScrollViewItemContainer);
        RelativeLayout layoutMain = findViewById(R.id.layout_main);
        layoutMain.setOnClickListener(v -> {
            if (imageCaptureViewModel.isShake) {
                imageCaptureViewModel.isShake = false;
                if (imageCaptureViewModel.quantity == 0) {
                    configureDefaultViews();
                } else {
                    clearDeleteMode();
                }
            }
        });
        ImageButton backButton = findViewById(R.id.back_icon);
        backButton.setOnClickListener(v -> super.onBackPressed());
        Button saveBtn = findViewById(R.id.save_btn);
        ImageButton homeBtn = findViewById(R.id.home_btn);
    }

    private void configureDefaultViews() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dip2px(ImageCaptureActivity.this, 60), dip2px(ImageCaptureActivity.this, 60));
        DeleteImageView deleteImageView = new DeleteImageView(ImageCaptureActivity.this);
        Bitmap bitMap = BitmapFactory.decodeFile("");
        layoutParams.setMargins(0, 25, 0, 0);
        deleteImageView.setLayoutParams(layoutParams);
        deleteImageView.getImg().setImageBitmap(bitMap);
        deleteImageView.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_100));
        container.addView(deleteImageView);
    }

    private void configureImageViews(ImageEntity image) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dip2px(ImageCaptureActivity.this, 74), dip2px(ImageCaptureActivity.this, 74));
        DeleteImageView deleteImageView = new DeleteImageView(ImageCaptureActivity.this);
        Bitmap bitMap = rotateBitmapByDegree(BitmapFactory.decodeFile(image.imagePath), getBitmapDegree(image.imagePath));
        layoutParams.setMargins(-20, 0, 0, 0);
        deleteImageView.setOnLongClickListener(v -> {
            enterDeleteMode();
            return true;
        });
        deleteImageView.setImageEntity(image);
        deleteImageView.setLayoutParams(layoutParams);
        deleteImageView.getImg().setImageBitmap(bitMap);
        if (imageCaptureViewModel.removeFirst) {
            imageCaptureViewModel.removeFirst = false;
            int size = container.getChildCount();
            if (size > 0) {
                container.removeViewAt(0);
            }
        }
        container.addView(deleteImageView, 0);
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void enterDeleteMode() {
        int size = container.getChildCount();
        for (int i = 0; i < size; i++) {
            DeleteImageView deleteImageView = (DeleteImageView) container.getChildAt(i);
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            deleteImageView.startAnimation(shake);
            deleteImageView.setDeleteIconVisible();
            deleteImageView.setDeleteListener(view -> {
                imageCaptureViewModel.quantity--;
                deleteImageView.clearAnimation();
                container.removeView(deleteImageView);
            });
            imageCaptureViewModel.isShake = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraThread();
        if (!mTextureView.isAvailable()) {
            mTextureView.setSurfaceTextureListener(mTextureListener);
        } else {
            setupCamera();
            openCamera();
        }
    }

    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera();
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void setupCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), (lhs, rhs) -> Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth()));
                setupImageReader();
                mCameraId = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private void startPreview() {
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePictures(View view) {
        if (imageCaptureViewModel.quantity < 6) {
            imageCaptureViewModel.quantity++;
            if (imageCaptureViewModel.quantity == 1) {
                imageCaptureViewModel.removeFirst = true;
            }
            lockFocus();
        }
    }

    private void lockFocus() {
        try {
            if (imageCaptureViewModel.isShake && imageCaptureViewModel.quantity == 0) {
                configureDefaultViews();
                imageCaptureViewModel.isShake = false;
            }
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), mCaptureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            capture();
        }
    };

    private void capture() {
        try {
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    Toast.makeText(getApplicationContext(), "Captured!", Toast.LENGTH_SHORT).show();
                    unLockFocus();
                }
            };
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(mCaptureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unLockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            //mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), null, mCameraHandler);
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void setupImageReader() {
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), ImageFormat.JPEG, 50);
        mImageReader.setOnImageAvailableListener(reader -> mCameraHandler.post(new imageSaver(reader.acquireNextImage())), mCameraHandler);
    }

    public class imageSaver implements Runnable {

        private final Image mImage;

        public imageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            Message msg = new Message();
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            ImageEntity image = new ImageEntity();
            File files = Environment.getExternalStorageDirectory();
            File[] pp = files.listFiles();
            for (File file : pp) {
                Log.d("", " file：" + file.getName() + "file path ：" + file.getAbsolutePath());
            }
            String path = Environment.getExternalStorageDirectory() + "/RDT/";
            File mImageFile = new File(path);
            if (!mImageFile.exists()) {
                mImageFile.mkdir();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
            String fileName = path + "IMG_" + timeStamp + ".jpg";
            image.imagePath = fileName;
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileName);
                fos.write(bytes, 0, bytes.length);
                msg.obj = image;
                mHandler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private int getBitmapDegree(String imagePath) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        double scaleWidth = 0.06;
        double scaleHeight = 0.06;
        matrix.postScale((float) scaleWidth, (float) scaleHeight);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w > h) {
            return Bitmap.createBitmap(bitmap, (w - h) / 2, 0, h, h, matrix, true);
        } else {
            return Bitmap.createBitmap(bitmap, 0, (h - w) / 2, w, w, matrix, true);
        }
    }

    private void clearDeleteMode() {
        int size = container.getChildCount();
        for (int i = 0; i < size; i++) {
            container.getChildAt(i).clearAnimation();
            DeleteImageView deleteImageView = (DeleteImageView) container.getChildAt(i);
            deleteImageView.clearAnimation();
            deleteImageView.setDeleteIconInVisible();
            deleteImageView.setOnLongClickListener(v1 -> {
                enterDeleteMode();
                return true;
            });
        }
    }
}
