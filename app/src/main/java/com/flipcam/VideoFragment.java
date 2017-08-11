package com.flipcam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import static com.flipcam.PermissionActivity.FC_SHARED_PREFERENCE;


public class VideoFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public static final String TAG = "VideoFragment";
    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoFragment.
     */
    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        Log.d(TAG,"Inside video fragment");
        ImageView thumbnail = (ImageView)view.findViewById(R.id.thumbnail);
        fetchMedia(thumbnail);
        return view;
    }

    private void fetchMedia(ImageView thumbnail)
    {
        String removableStoragePath;
        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList)
        {
            if(!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead()) {
                removableStoragePath = file.getAbsolutePath();
                Log.d(TAG,removableStoragePath);
                for(File file1 : new File(removableStoragePath).listFiles())
                {
                    Log.d(TAG,file1.getPath());
                }
            }
        }
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Log.d(TAG,root.getPath());
        File fc = new File(root.getPath()+"/FlipCam");
        if(!fc.exists()){
            fc.mkdir();
            Log.d(TAG,"FlipCam dir created");
            File videos = new File(fc.getPath()+"/FC_Videos");
            if(!videos.exists()){
                videos.mkdir();
                Log.d(TAG,"Videos dir created");
            }
            File images = new File(fc.getPath()+"/FC_Images");
            if(!images.exists()){
                images.mkdir();
                Log.d(TAG,"Images dir created");
            }
        }
        //Use this for sharing files between apps
        File videos = new File(root.getPath()+"/FlipCam/FC_Images/");
        if(videos.listFiles() != null){
            Log.d(TAG,"List = "+videos.listFiles());
            Log.d(TAG,"List length = "+videos.listFiles().length);
            for(File file : videos.listFiles()){
                Log.d(TAG,file.getPath());
                final Bitmap img = BitmapFactory.decodeFile(file.getPath());
                thumbnail.setImageBitmap(img.createScaledBitmap(img,68,68,false));
                final String imgPath = file.getPath();
                thumbnail.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view)
                    {
                        openMedia(imgPath);
                    }
                });
                /*Bitmap vid = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                thumbnail.setImageBitmap(vid.createScaledBitmap(vid,68,68,false));*/
                break;
            }
        }
    }

    private void openMedia(String path)
    {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(FC_SHARED_PREFERENCE,Context.MODE_PRIVATE).edit();
        editor.putBoolean("startCamera",false);
        editor.commit();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://"+path),"image*//*");
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
