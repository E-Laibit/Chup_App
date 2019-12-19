package com.google.firebase.codelab.kit.model.Miscellaneous;

import android.os.Bundle;

import com.google.firebase.codelab.kit.model.R;

public class imagePreviewActivity extends StillImageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_still_image);
    }
}
