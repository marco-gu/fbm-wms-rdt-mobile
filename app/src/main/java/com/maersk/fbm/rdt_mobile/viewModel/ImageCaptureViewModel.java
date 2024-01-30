package com.maersk.fbm.rdt_mobile.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class ImageCaptureViewModel extends AndroidViewModel {

    public boolean isShake = false;
    public int quantity = 0;
    public String imageReason;
    public boolean removeFirst = false;

    public ImageCaptureViewModel(@NonNull Application application) {
        super(application);
    }
}
