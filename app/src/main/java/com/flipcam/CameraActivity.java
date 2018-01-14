package com.flipcam;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.flipcam.service.GoogleDriveUploadService;

public class CameraActivity extends AppCompatActivity implements VideoFragment.PermissionInterface, PhotoFragment.PhotoPermission,VideoFragment.SwitchInterface,
PhotoFragment.SwitchPhoto, VideoFragment.StartUploadService{

    private static final String TAG = "CameraActivity";
    private static final String VIDEO = "1";
    VideoFragment videoFragment = null;
    PhotoFragment photoFragment = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_camera);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(savedInstanceState == null) {
            //Start with video fragment
            showVideoFragment();
        }
    }

    public void goToSettings(View view){
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
    }

    @Override
    public void switchToPhoto() {
        showPhotoFragment();
    }

    @Override
    public void switchToVideo() {
        showVideoFragment();
    }

    public void showVideoFragment()
    {
        if(videoFragment == null) {
            Log.d(TAG,"creating videofragment");
            videoFragment = VideoFragment.newInstance();
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(photoFragment!=null) {
            fragmentTransaction.replace(R.id.cameraPreview, videoFragment).commit();
            Log.d(TAG,"photofragment removed");
        }
        else{
            fragmentTransaction.add(R.id.cameraPreview, videoFragment, VIDEO).commit();
            Log.d(TAG,"videofragment added");
        }
    }

    public void showPhotoFragment()
    {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(photoFragment == null) {
            Log.d(TAG,"creating photofragment");
            photoFragment = PhotoFragment.newInstance();
        }
        fragmentTransaction.replace(R.id.cameraPreview, photoFragment).commit();
        Log.d(TAG,"photofragment added");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    public void askPermission() {
        askCameraPermission();
    }

    @Override
    public void askPhotoPermission() {
        askCameraPermission();
    }

    public void askCameraPermission(){
        Log.d(TAG,"start permission act to get permissions");
        Intent permission = new Intent(this,PermissionActivity.class);
        permission.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(permission);
        finish();
    }

    @Override
    public void startAutoUpload(String filename) {
        Intent googleDriveUploadIntent = new Intent(getApplicationContext(), GoogleDriveUploadService.class);
        googleDriveUploadIntent.putExtra("uploadFile", filename);
        Log.d(TAG, "Uploading file = "+filename);
        startService(googleDriveUploadIntent);
    }
}
