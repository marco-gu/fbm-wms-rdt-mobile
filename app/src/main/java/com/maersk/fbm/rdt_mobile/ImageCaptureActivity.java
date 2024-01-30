package com.maersk.fbm.rdt_mobile;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.maersk.fbm.rdt_mobile.entity.ImageEntity;
import com.maersk.fbm.rdt_mobile.viewModel.ImageCaptureViewModel;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ImageCaptureActivity extends AppCompatActivity {

    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    private LinearLayout container;

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private String mCameraId;
    private Size mPreviewSize;
    private Size mCaptureSize;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private ImageCaptureViewModel imageCaptureViewModel;
    private static Handler mHandler;


    private final String REASON_DAMAGED = "Damaged";
    private final String REASON_WET = "Wet";
    private final String REASON_DEFORMED = "Deformed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageCaptureViewModel = new ViewModelProvider(this).get(ImageCaptureViewModel.class);
        initView();
//        mHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                ImageEntity image = (ImageEntity) msg.obj;
//                configureImageViews(image);
//            }
//        };

    }

    private void initView() {
        mTextureView = (TextureView) findViewById(R.id.textureView);
        container = (LinearLayout) findViewById(R.id.horizontalScrollViewItemContainer);
        RelativeLayout layoutMain = (RelativeLayout) findViewById(R.id.layout_main);
        layoutMain.setOnClickListener(v -> {
            if (imageCaptureViewModel.isShake) {
                imageCaptureViewModel.isShake = false;
                if (imageCaptureViewModel.quantity == 0) {
//                    configureDefaultViews();
                } else {
//                    clearDeleteMode();
                }
            }
        });
//        LiveData<List<ImageEntity>> imageObserver = mDamageCaptureViewModel.retrieveImages(mDamageCaptureViewModel.taskID, mDamageCaptureViewModel.cartonID);
//        imageObserver.observe(this, images -> {
//            if (images.size() > 0) {
//                images.forEach(image -> {
//                    mDamageCaptureViewModel.photoSize++;
//                    configureImageViews(image);
//                });
//            } else {
//                configureDefaultViews();
//            }
//            mDamageCaptureViewModel.reason = images.size() > 0 ? images.get(0).reason : REASON_DAMAGED;
//            initReasonDropDown();
//            imageObserver.removeObservers(this);
//        });
//        ImageButton backButton = findViewById(R.id.back_icon);
//        backButton.setOnClickListener(v -> super.onBackPressed());
//        Button saveBtn = findViewById(R.id.save_btn);
//        saveBtn.setOnClickListener(v -> {
//            new Thread(() -> {
//                mDamageCaptureViewModel.updateImageReason();
//            }).start();
//            super.onBackPressed();
//        });

//        String titleLabels = mDamageCaptureViewModel.cartonID + " | Photos";
//        TextView textView = findViewById(R.id.cargo_label);
//        textView.setText(titleLabels);

//        ImageButton homeBtn = findViewById(R.id.home_btn);
//        homeBtn.setOnClickListener(v -> {
//            final Dialog dialog = new Dialog(this);
//            dialog.setContentView(R.layout.custom_alert_dialog);
//            TextView title = dialog.findViewById(R.id.dialog_title);
//            title.setText("Information");
//            TextView text = dialog.findViewById(R.id.dialog_text);
//            text.setText("Return to Home Page?");
//            Button confirmBtn = dialog.findViewById(R.id.dialog_cancel_button);
//            confirmBtn.setText("Confirm");
//            confirmBtn.setVisibility(View.VISIBLE);
//            confirmBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public  void onClick(View v) {
//                    // when click confirm, return to home page
//                    finish();
//                    EventBus.getDefault().post(new MessageEvent(getApplicationContext().getString(R.string.return_first_page), ""));
//                }
//            });
//            Button cancelBtn = dialog.findViewById(R.id.dialog_confirm_button);
//            cancelBtn.setVisibility(View.VISIBLE);
//            cancelBtn.setText("Cancel");
//            cancelBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//                @Override
//                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                    return keyCode == KeyEvent.KEYCODE_ENTER;
//                }
//            });
//            dialog.show();
//        });
    }

//    private void initReasonDropDown() {
//        ((TextView) findViewById(R.id.reasonView)).setText(imageCaptureViewModel.imageReason);
//        configureReasonView();
//        configureReasonDamaged();
//        configureReasonWet();
//        configureReasonDeformed();
//    }

//    private void configureReasonView() {
//        findViewById(R.id.reasonView).setOnClickListener(v -> {
//            if (findViewById(R.id.reasonDropdown).getVisibility() == View.VISIBLE) {
//                handleCloseDropdown();
//            } else {
//                handleOpenDropdown();
//            }
//        });
//    }

//    private void configureReasonDamaged() {
//        findViewById(R.id.reasonDamaged).setOnClickListener(v -> {
//            ((TextView) findViewById(R.id.reasonView)).setText(R.string.reason_damaged);
//            imageCaptureViewModel.imageReason = REASON_DAMAGED;
//            handleCloseDropdown();
//        });
//    }

//    private void configureReasonWet() {
//        findViewById(R.id.reasonWet).setOnClickListener(v -> {
//            ((TextView) findViewById(R.id.reasonView)).setText(R.string.reason_wet);
//            imageCaptureViewModel.imageReason = REASON_WET;
//            handleCloseDropdown();
//        });
//    }

//    private void configureReasonDeformed() {
//        findViewById(R.id.reasonDeformed).setOnClickListener(v -> {
//            ((TextView) findViewById(R.id.reasonView)).setText(R.string.reason_deformed);
//            imageCaptureViewModel.imageReason = REASON_DEFORMED;
//            handleCloseDropdown();
//        });
//    }

//    private void handleOpenDropdown() {
//        findViewById(R.id.reasonView).setBackgroundResource(R.drawable.rectangle_3);
//        findViewById(R.id.icon_spinner).animate().rotation(180);
//        findViewById(R.id.reasonDropdown).setVisibility(View.VISIBLE);
//        highlightMatchedReasonText();
//    }

//    private void highlightMatchedReasonText() {
//        TextView reasonDamage = findViewById(R.id.reasonDamaged);
//        TextView reasonWet = findViewById(R.id.reasonWet);
//        TextView reasonDeformed = findViewById(R.id.reasonDeformed);
//        switch (imageCaptureViewModel.imageReason) {
//            case REASON_DAMAGED:
//                reasonDamage.setTextColor(ContextCompat.getColor(this, R.color.blue_300));
//                reasonWet.setTextColor(ContextCompat.getColor(this, R.color.black));
//                reasonDeformed.setTextColor(ContextCompat.getColor(this, R.color.black));
//                break;
//            case REASON_WET:
//                reasonDamage.setTextColor(ContextCompat.getColor(this, R.color.black));
//                reasonWet.setTextColor(ContextCompat.getColor(this, R.color.blue_300));
//                reasonDeformed.setTextColor(ContextCompat.getColor(this, R.color.black));
//                break;
//            case REASON_DEFORMED:
//                reasonDamage.setTextColor(ContextCompat.getColor(this, R.color.black));
//                reasonWet.setTextColor(ContextCompat.getColor(this, R.color.black));
//                reasonDeformed.setTextColor(ContextCompat.getColor(this, R.color.blue_300));
//                break;
//        }
//    }

//    private void handleCloseDropdown() {
//        findViewById(R.id.reasonView).setBackgroundResource(R.drawable.rectangle_1);
//        findViewById(R.id.icon_spinner).animate().rotation(0);
//        findViewById(R.id.reasonDropdown).setVisibility(View.INVISIBLE);
//    }

//    private void configureDefaultViews() {
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dip2px(DamageCaptureActivity.this, 60), dip2px(DamageCaptureActivity.this, 60));
//        DeleteImageView deleteImageView = new DeleteImageView(DamageCaptureActivity.this);
//        Bitmap bitMap = BitmapFactory.decodeFile("");
//        layoutParams.setMargins(0, 25, 0, 0);
//        deleteImageView.setLayoutParams(layoutParams);
//        deleteImageView.getImg().setImageBitmap(bitMap);
//        deleteImageView.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_100));
//        container.addView(deleteImageView);
//    }

//    private void configureImageViews(ImageEntity image) {
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dip2px(DamageCaptureActivity.this, 74), dip2px(DamageCaptureActivity.this, 74));
//        DeleteImageView deleteImageView = new DeleteImageView(DamageCaptureActivity.this);
//        Bitmap bitMap = rotateBitmapByDegree(BitmapFactory.decodeFile(image.imagePath), getBitmapDegree(image.imagePath));
//        layoutParams.setMargins(-20, 0, 0, 0);
//        deleteImageView.setOnLongClickListener(v -> {
//            enterDeleteMode();
//            return true;
//        });
//        deleteImageView.setImageEntity(image);
//        deleteImageView.setLayoutParams(layoutParams);
//        deleteImageView.getImg().setImageBitmap(bitMap);
//        if (mDamageCaptureViewModel.removeFirst) {
//            mDamageCaptureViewModel.removeFirst = false;
//            int size = container.getChildCount();
//            if(size > 0) {
//                container.removeViewAt(0);
//            }
//        }
//        container.addView(deleteImageView, 0);
//    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

//    private void enterDeleteMode() {
//        int size = container.getChildCount();
//        for (int i = 0; i < size; i++) {
//            DeleteImageView deleteImageView = (DeleteImageView) container.getChildAt(i);
//            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
//            deleteImageView.startAnimation(shake);
//            deleteImageView.setDeleteIconVisible();
//            deleteImageView.setDeleteListener(view -> {
//                mDamageCaptureViewModel.photoSize--;
//                deleteImageView.clearAnimation();
//                container.removeView(deleteImageView);
//                ImageEntity deleteImage = deleteImageView.getImageEntity();
//                new Thread(() -> {
//                    // TODO Remove file
//                    mDamageCaptureViewModel.removeImage(deleteImage);
//                }).start();
//            });
//            mDamageCaptureViewModel.isShake = true;
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraThread();
        if (!mTextureView.isAvailable()) {
            mTextureView.setSurfaceTextureListener(mTextureListener);
        } else {
//            startPreview();
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
//            setupCamera(width, height);
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
//    private void setupCamera(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
//                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
//                mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
                mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                    }
                });
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
//        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
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
        if(imageCaptureViewModel.quantity < 6) {
            imageCaptureViewModel.quantity++;
            if(imageCaptureViewModel.quantity == 1) {
                imageCaptureViewModel.removeFirst = true;
            }
            lockFocus();
        }
//        else if(!mDamageCaptureViewModel.isPopUpAlert){
        else {
            final Dialog dialog = new Dialog(this);
//            dialog.setContentView(R.layout.custom_alert_dialog);
//            TextView title = dialog.findViewById(R.id.dialog_title);
//            title.setText(R.string.alert_bold);
//            ImageView titleIcon = dialog.findViewById(R.id.dialog_icon);
//            titleIcon.setImageResource(R.drawable.icon_popup_alert);
//            TextView text = dialog.findViewById(R.id.dialog_text);
//            text.setText(getResources().getString(R.string.exceed_max));
//            Button confirmBtn = dialog.findViewById(R.id.dialog_confirm_button);
//            confirmBtn.setText(R.string.add);
//            confirmBtn.setVisibility(View.VISIBLE);
//            confirmBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });
//            Button cancelBtn = dialog.findViewById(R.id.dialog_cancel_button);
//            cancelBtn.setOnClickListener(v -> {
//                dialog.dismiss();
//            });
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();
//            mDamageCaptureViewModel.isPopUpAlert = true;
//            AlertDialog.Builder builder = new AlertDialog.Builder(DamageCaptureActivity.this);
//            builder.setTitle(R.string.alert)
//                    .setMessage("Don't exceed max limited images for each carton")
//                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                        }
//                    }).create();
//            builder.show();
        }
    }

    private void lockFocus() {
        try {
            if(imageCaptureViewModel.isShake && imageCaptureViewModel.quantity == 0) {
//                configureDefaultViews();
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
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
//                mCameraHandler.post(new imageSaver(reader.acquireNextImage()));
            }
        }, mCameraHandler);
    }

//    public class imageSaver implements Runnable {
//
//        private final Image mImage;
//
//        public imageSaver(Image image) {
//            mImage = image;
//        }
//
//        @Override
//        public void run() {
//            Message msg = new Message();
//            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
//            byte[] bytes = new byte[buffer.capacity()];
//            buffer.get(bytes);
//            ImageEntity image = new ImageEntity();
//            String path = Environment.getExternalStorageDirectory() + "/DCIM/LNS/";
//            File mImageFile = new File(path);
//            if (!mImageFile.exists()) {
//                mImageFile.mkdir();
//            }
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
//            String fileName = path + "IMG_" + timeStamp + ".jpg";
//            image.imagePath = fileName;
//            image.taskId = mDamageCaptureViewModel.taskID;
//            image.cartonId = mDamageCaptureViewModel.cartonID;
//            image.reason = mDamageCaptureViewModel.reason;
//            image.createUser = (String) SharedPreferenceUtil.getInstance().get(getApplicationContext(), "UID", "");
//            image.createDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
//            FileOutputStream fos = null;
//            try {
//                fos = new FileOutputStream(fileName);
//                fos.write(bytes, 0, bytes.length);
//                msg.obj = image;
//                mDamageCaptureViewModel.insertImage(image);
//                mHandler.sendMessage(msg);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (fos != null) {
//                    try {
//                        fos.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

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

    private void checkTransparentBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
            int color = getResources().getColor(R.color.black);
            getWindow().setStatusBarColor(color);
        }
    }

//    private void clearDeleteMode() {
//        int size = container.getChildCount();
//        for (int i = 0; i < size; i++) {
//            container.getChildAt(i).clearAnimation();
//            DeleteImageView deleteImageView = (DeleteImageView) container.getChildAt(i);
//            deleteImageView.clearAnimation();
//            deleteImageView.setDeleteIconInVisible();
//            deleteImageView.setOnLongClickListener(v1 -> {
//                enterDeleteMode();
//                return true;
//            });
//        }
//    }
}
