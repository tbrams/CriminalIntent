package dk.incipio.criminalintent;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CrimeCameraFragment extends Fragment {
    private static final String TAG = "CrimeCameraFragment";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // Display the progress indicator
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback mJpgCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Create a filename
            String fileName = UUID.randomUUID().toString() + ".jpg";
            // Save jpg data to disk
            FileOutputStream os = null;
            boolean success = true;
            try {
                os = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
                os.write(data);
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file " + fileName, e);
                success = false;
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error closing file ", e);
                    // e.printStackTrace();
                    success = false;
                }
            }

            if (success) {
                Log.i(TAG, "JPG saved at " + fileName);
            }
            getActivity().finish();
        }
    };



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_camera, container, false);
        Button takePictureButton = (Button) v.findViewById(R.id.take_button);
        mProgressContainer = v.findViewById(R.id.crime_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCamera != null) {
                    mCamera.takePicture(mShutterCallback,null,mJpgCallback);
                }
            }
        });


        mSurfaceView = (SurfaceView) v.findViewById(R.id.crime_camera_surfaceview);
        SurfaceHolder holder = mSurfaceView.getHolder();

        // SetType and SURFACE_TYPE_PUSH_BUFFERS are both deprecated, but needed in order to work on
        // pre-3.0 devices
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // Tell the camera to use this surface for preview
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                }
                catch (IOException e) {
                        Log.e(TAG, "Error setting up SurfaceDisplay ", e);
                    }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
                if (mCamera==null) {
                    return;
                }

                // the surface has changed size / update the surface

            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);

            parameters.setPreviewSize(s.width, s.height);
            s = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
            parameters.setPictureSize(s.width, s.height);

            mCamera.setParameters(parameters);

            try {
                mCamera.startPreview();
            } catch (Exception e) {
                Log.e(TAG, "Could not start preview ", e);
                mCamera.release();
                mCamera=null;
            }


        }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder){
            // We can no longer display anything - stop the preview
            if (mCamera != null) {
                mCamera.stopPreview();
            }

        }
        });

        return v;

    }

    @TargetApi(9)
    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCamera!=null) {
            mCamera.release();
            mCamera=null;
        }
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }
}
