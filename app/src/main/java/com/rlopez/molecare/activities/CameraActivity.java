package com.rlopez.molecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.utils.AutoFitTextureView;
import com.rlopez.molecare.utils.FileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/***** NEEDS TO BE FIXED *****/
public class CameraActivity extends AppCompatActivity {

    // States for the camera (focus locked or not locked)
    private static final int FOCUS_NOT_LOCKED = 0;
    private static final int FOCUS_LOCKED = 1;
    private int focusState = FOCUS_NOT_LOCKED;

    // Side dimension for the central square
    private int sideDimension = 512;

    // To get current configuration
    File configFile;
    Configuration configuration;

    // Path and file
    private String moleName;
    private String bodyPartPath;
    private File moleFolder;

    // To know if create or update a mole
    private String operation;

    // Camera square view and parameters
    private ImageView squareView;
    private Size previewSize;
    private String cameraId;

    // Shows camera preview
    private AutoFitTextureView cameraPreview;

    // Capture button
    private Button captureButton;

    // List of surfaces
    List<Surface> outputSurfaces;

    // Reader for the camera images
    ImageReader reader;

    // Get the camera characteristics
    CameraCharacteristics cameraCharacteristics;

    // Listens to surface changes in camera preview
    private TextureView.SurfaceTextureListener cameraPreviewSurfaceListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // Surface texture is available, setup the camera
            setupCamera(width, height);
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

    // Represents the camera device
    private CameraDevice cameraDevice;
    // Listens to camera device changes
    private CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // Get the camera when opened
            cameraDevice = camera;
            // Create a session for the camera preview
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            // Close camera
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            // Close camera and notify error
            camera.close();
            cameraDevice = null;
            // Notify error and get back to the previous view
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
            finish();
        }

    };
    // Needed to attach the surface with the camera session
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession captureSession;
    private CameraCaptureSession.CaptureCallback captureSessionCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };

    // To do background operations
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    // File for the photo
    private static File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.textureView);
        squareView = findViewById(R.id.cameraSquareView);
        captureButton = findViewById(R.id.captureButton);

        // Get the type of operation to do
        operation = getIntent().getStringExtra("OPERATION");

        // Get configuration file and read it
        configFile = new File(getIntent().getStringExtra("CONFIGURATION_FILE_PATH"));
        configuration = Configuration.readConfigurationJSON(configFile, getApplicationContext());

        // Get side dimension from configuration
        sideDimension = Integer.parseInt(configuration.getImageParameters().getTrimDimension());

        // Get paths
        bodyPartPath = getIntent().getStringExtra("BODY_PART_PATH");
        moleName = getIntent().getStringExtra("MOLE_NAME");
        moleFolder = new File(bodyPartPath, moleName);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Create a new background thread
        openBackgroundThread();

        if (cameraPreview.isAvailable()) {
            // If the texture view is available setup and open camera
            setupCamera(cameraPreview.getWidth(), cameraPreview.getHeight());
            openCamera();
        } else {
            // Set the surface listener for the preview if needed
            cameraPreview.setSurfaceTextureListener(cameraPreviewSurfaceListener);
        }

        // Lock or unlock focus when user touches the screen
        cameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (focusState == 0) {
                    lockFocus();
                } else {
                    unlockFocus();
                }
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create image file
                createImageFile();
                // Take a new photo
                takePicture();
            }
        });
    }

    @Override
    protected void onPause() {
        // Close the camera
        closeCamera();
        // Close the background thread
        closeBackgroundThread();
        super.onPause();
    }

    // Create image file and parent folder if needed
    private void createImageFile() {
        if (operation.equals("create")) {
            // Create a folder for the new mole
            moleFolder.mkdirs();
            try {
                // Create the temp file
                imageFile = FileManager.createTempPhotoFile(moleFolder.getAbsolutePath());
            } catch (IOException e) {
                moleFolder.delete();
                Toast.makeText(getApplicationContext(), R.string.error_creating_file, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Create the temp file
            try {
                imageFile = FileManager.createTempPhotoFile(moleFolder.getAbsolutePath());
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), R.string.error_creating_file, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Lock focus
    private void lockFocus() {
        focusState = FOCUS_LOCKED;
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            captureSession.capture(captureRequestBuilder.build(), captureSessionCallback, backgroundHandler);
            Toast.makeText(getApplicationContext(), R.string.focus_locked, Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Unlock focus
    private void unlockFocus() {
        focusState = FOCUS_NOT_LOCKED;
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
        try {
            captureSession.capture(captureRequestBuilder.build(), captureSessionCallback, backgroundHandler);
            Toast.makeText(getApplicationContext(), R.string.focus_unlocked, Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    // Close the camera and free resources
    private void closeCamera() {
        // Close the capture session if needed
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        // Close camera device if needed
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    // Configure and use back camera
    private void setupCamera(int width, int height) {
        // Access to camera resources
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Find back camera and get the configuration map (containing, for example, sizes)
            for (String id : cameraManager.getCameraIdList()) {
                cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                // Get available sizes and adjust the preview, save back camera id
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                previewSize = adjustPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                cameraPreview.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                cameraId = id;
                // Set camera square size depending on the image size
                int newSideDimension = (sideDimension / getSquareDimensionFactor());
                squareView.getLayoutParams().width = newSideDimension;
                squareView.getLayoutParams().height = newSideDimension;
                // Set image reader with best sizes to take just one photo in JPEG format
                reader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);
                outputSurfaces = new ArrayList<>(2);
                outputSurfaces.add(reader.getSurface());
                outputSurfaces.add(new Surface(cameraPreview.getSurfaceTexture()));
                return;
            }
        } catch (CameraAccessException e) {
            // Notify error and get back to the previous view
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Get the factor to resize the central square
    private int getSquareDimensionFactor() {
        return (previewSize.getHeight() * previewSize.getWidth()) / (cameraPreview.getHeight() * cameraPreview.getWidth());
    }

    private void openCamera() {
        // Access to camera resources
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // If camera permissions are not granted, get back to previous view
                finish();
            }
            // Open back camera. Use background handler to manage images
            cameraManager.openCamera(cameraId, cameraDeviceStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            // Notify error and get back to the previous view
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Create a session for the camera preview attaching the view to a surface
    private void createCameraPreviewSession() {
        try {
            // Get the surface texture (captures frames from the images stream)
            SurfaceTexture surfaceTexture = cameraPreview.getSurfaceTexture();
            // Set buffer size for the images
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            // Handler for the unprocessed buffer
            Surface previewSurface = new Surface(surfaceTexture);
            // Create a new capture request
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // Add the surfaces to the camera session
            captureRequestBuilder.addTarget(previewSurface);
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    try {
                        // Build the capture request
                        captureRequest = captureRequestBuilder.build();
                        // Get the session
                        captureSession = session;
                        // Request images constantly. Use the background thread to manage the session
                        captureSession.setRepeatingRequest(captureRequest, captureSessionCallback, backgroundHandler);
                    } catch (CameraAccessException e) {
                        // Notify error and get back to the previous view
                        Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    // Notify error and get back to the previous view
                    Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            // Notify error and get back to the previous view
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Adjust the preview to fit the camera ratio in it
    private Size adjustPreviewSize(Size[] mapSizes, int width, int height) {
        List<Size> sizesList = new ArrayList<>();
        // Get possible size options (Bigger resolutions than the screen)
        for (Size size : mapSizes) {
            if (width > height) {
                if (size.getWidth() > width && size.getHeight() > height) {
                    sizesList.add(size);
                }
            } else {
                if (size.getWidth() > height && size.getHeight() > width) {
                    sizesList.add(size);
                }
            }
        }
        // Select the best one (the biggest)
        if (sizesList.size() > 0) {
            return Collections.max(sizesList, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Long.signum(o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight());
                }
            });
        } else {
            return mapSizes[0];
        }

    }

    // Create and start a background thread
    private void openBackgroundThread() {
        backgroundThread = new HandlerThread("Camera2 background thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    // Close and destroy the background thread
    private void closeBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        if(cameraDevice == null) {
            Toast.makeText(getApplicationContext(), R.string.error_camera_not_found, Toast.LENGTH_SHORT).show();
        } else {
            lockFocus();
            final CaptureRequest.Builder captureRequestBuilder;
            try {
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureRequestBuilder.addTarget(reader.getSurface());
                //captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);

                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = null;
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        try {
                            saveFile(bytes);
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), R.string.error_creating_file, Toast.LENGTH_SHORT).show();
                            imageFile.delete();
                        } finally {
                            image.close();
                        }
                    }
                };
                reader.setOnImageAvailableListener(readerListener, backgroundHandler);

                final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        Toast.makeText(getApplicationContext(), R.string.photo_taken, Toast.LENGTH_SHORT).show();
                        CameraActivity.this.finish();
                    }
                };

            } catch (CameraAccessException e) {
                Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveFile(byte[] bytes) throws IOException {
        OutputStream outputStream = null;
        outputStream = new FileOutputStream(imageFile);
        outputStream.write(bytes);
    }

}

