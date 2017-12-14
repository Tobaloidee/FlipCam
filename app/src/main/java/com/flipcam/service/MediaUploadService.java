package com.flipcam.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by koushick on 13-Dec-17.
 */

public class MediaUploadService extends Service {

    public static final String TAG = "MediaUploadService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand = "+startId);
        int startid = startId;
        userId = (String)intent.getExtras().get("userId");
        String uploadfilepath = (String)intent.getExtras().get("uploadFile");
        Log.i(TAG,"Upload file = "+uploadfilepath);
        new MediaUploadTask(uploadfilepath,startid).start();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"Start NEW UPLOAD TASK");
                //new MediaUploadTask().execute(uploadfilepath,startid+"");
                uploadFile = uploadfilepath;
                Bundle getParams = new Bundle();
                getParams.putInt("uploadID",uploadID);
                GraphRequest meReq = new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", getParams,HttpMethod.GET,getcallback);
                meReq.executeAndWait();
            }
        }).start();*/
        /*uploadFile = uploadfilepath;
        Bundle getParams = new Bundle();
        getParams.putInt("uploadID",uploadID);
        GraphRequest meReq = new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", getParams,HttpMethod.GET,getcallback);
        meReq.executeAsync();*/
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG,"onLowMemory");
        super.onLowMemory();
    }
    String userId;

    class MediaUploadTask extends Thread {
        Boolean success;
        String uploadFile;
        int uploadID;
        RandomAccessFile randomAccessFile = null;
        String upload_session_id = null;
        long startTimeUpload;
        int retryCount = 5;

        MediaUploadTask(String uploadPath, int uploadid){
            uploadFile = uploadPath;
            uploadID = uploadid;
        }

        public void startUpload(){
            try {
                Bundle params = new Bundle();
                params.putString("upload_phase", "start");
                randomAccessFile = new RandomAccessFile(new File(uploadFile), "r");
                params.putString("file_size", randomAccessFile.length() + "");
                Log.i(TAG, "file size = " + randomAccessFile.length());
                GraphRequest postReq = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + userId + "/videos", params, HttpMethod.POST, postcallback);
                postReq.executeAndWait();
                Log.i(TAG, "Request sent");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        GraphRequest.Callback postcallback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Log.i(TAG, "response = " + response.getRawResponse());
                if (response.getError() != null) {
                    Log.i(TAG, "Error msg = " + response.getError().getErrorMessage());
                    Log.i(TAG, "Error code = " + response.getError().getErrorCode());
                    Log.i(TAG, "Error subcode = " + response.getError().getErrorMessage());
                    Log.i(TAG, "Error recovery msg = " + response.getError().getErrorRecoveryMessage());
                    if (retryCount == 0) {
                        Log.i(TAG,"Stopping.. "+uploadID);
                        stopSelf(uploadID);
                    } else {
                        retryCount--;
                        Log.i(TAG, "Retrying...."+retryCount);
                        GraphRequest postReq = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + userId + "/videos",
                                response.getRequest().getParameters(),
                                HttpMethod.POST, postcallback);
                        postReq.executeAndWait();
                    }
                } else {
                    JSONObject jsonObject = response.getJSONObject();
                    try {
                        if (jsonObject.has("upload_session_id") || (jsonObject.has("start_offset") || jsonObject.has("end_offset"))) {
                            if (jsonObject.has("upload_session_id")) {
                                upload_session_id = (String) jsonObject.get("upload_session_id");
                            }
                            String start_offset = (String) jsonObject.get("start_offset");
                            String end_offset = (String) jsonObject.get("end_offset");
                            byte[] buffer = new byte[(int) (Long.parseLong(end_offset) - Long.parseLong(start_offset))];
                            randomAccessFile.seek(Long.parseLong(start_offset));
                            if (Long.parseLong(start_offset) != Long.parseLong(end_offset)) {
                                Bundle params = new Bundle();
                                Log.i(TAG, "Upload from " + start_offset + " to " + end_offset);
                                params.putString("upload_phase", "transfer");
                                params.putString("upload_session_id", upload_session_id);
                                params.putString("start_offset", start_offset);
                                randomAccessFile.read(buffer);
                                params.putByteArray("video_file_chunk", buffer);
                                Integer uploadid = (Integer) response.getRequest().getParameters().get("uploadID");
                                Log.d(TAG, "upload id in POST = " + uploadid);
                                params.putInt("uploadID", uploadid.intValue());
                                GraphRequest postReq = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + userId + "/videos", params, HttpMethod.POST, postcallback);
                                postReq.executeAndWait();
                            } else {
                                Bundle params = new Bundle();
                                Log.i(TAG, "Complete UPLOAD");
                                params.putString("upload_phase", "finish");
                                params.putString("upload_session_id", upload_session_id);
                                GraphRequest postReq = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + userId + "/videos", params, HttpMethod.POST, postcallback);
                                postReq.executeAndWait();
                            }
                        } else {
                            if (jsonObject.has("success")) {
                                success = (Boolean) jsonObject.get("success");
                                Log.i(TAG, "success = " + success);
                                long endtime = System.currentTimeMillis();
                                Log.i(TAG, "time taken = " + (endtime - startTimeUpload) / 1000 + " secs");
                                Integer uploadid = (Integer) response.getRequest().getParameters().get("uploadID");
                                if (uploadid != null) {
                                    Log.d(TAG, "Stop for " + uploadid.intValue());
                                    stopSelf(uploadid.intValue());
                                } else {
                                    stopSelf();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        @Override
        public void run() {
            Log.i(TAG,"Start NEW UPLOAD TASK");
            try {
                startUpload();
            } finally {
                try {
                    Log.d(TAG,"Close file");
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
