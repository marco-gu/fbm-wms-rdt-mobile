package com.maersk.fbm.rdt_mobile.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.maersk.fbm.rdt_mobile.R;
import com.maersk.fbm.rdt_mobile.entity.ImageEntity;

public class DeleteImageView extends RelativeLayout {

    private ImageView mImg;
    private ImageView mIv_delete;
    private ImageEntity imageEntity;

    public DeleteImageView(Context context) {
        this(context, null);
    }

    public DeleteImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_delete_image_view, this);
        mImg = view.findViewById(R.id.iv_img);
        mIv_delete = view.findViewById(R.id.iv_delete);
    }

    public ImageView getImg() {
        return mImg;
    }

    public void setImageEntity(ImageEntity imageEntity) {
        this.imageEntity = imageEntity;
    }

    public ImageEntity getImageEntity() {
        return this.imageEntity;
    }

    public void setDeleteIconVisible() {
        mIv_delete.setVisibility(VISIBLE);
    }

    public void setDeleteIconInVisible() {
        mIv_delete.setVisibility(INVISIBLE);
    }

    public void setDeleteListener(OnClickListener onClickListener) {
        mIv_delete.setOnClickListener(onClickListener);
    }

}
