/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

package com.rlopez.molecare.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
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
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.views.AutoFitTextureView;
import com.rlopez.molecare.images.ImageProcessor;
import com.rlopez.molecare.utils.FileManager;
import com.rlopez.molecare.images.ImageModel;
import com.rlopez.molecare.images.ImagesInformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE;

public class CameraActivity extends AppCompatActivity {

    private Size imageDimensions;
    private AutoFitTextureView cameraPreview;
    private ImageView squareView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession captureSession;
    private CameraCharacteristics characteristics;
    private File moleFolder;
    private int trimDimension;
    private ImagesInformation imagesInformation;

    private File photoFile;

    ImageView captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        squareView = findViewById(R.id.cameraSquareView);

        // Get extras
        String configurationFilePath = getIntent().getStringExtra("CONFIGURATION_FILE_PATH");
        String folderPath = getIntent().getStringExtra("PATH");
        String moleName = getIntent().getStringExtra("MOLE_NAME");

        // Get trim dimension from configuration
        assert configurationFilePath != null;
        Configuration configuration = Configuration.readConfigurationJSON(new File(configurationFilePath), getApplicationContext());
        trimDimension = Integer.parseInt(configuration.getImageConfiguration().getTrimDimension());

        // Get mole folder
        if (moleName != null) {
            moleFolder = new File(folderPath, moleName);
        } else {
            assert folderPath != null;
            moleFolder = new File(folderPath);
        }

        // Set a listener for the camera preview
        cameraPreview = findViewById(R.id.textureView);
        cameraPreview.setSurfaceTextureListener(surfaceTextureListener);

        // If capture button is pressed, take a photo
        captureButton = findViewById(R.id.shutter_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

    }

    // Take the photo
    private void takePhoto() {

        if (cameraDevice == null) {
            return;
        }
        try {
            // Create an image reader with max resolution to take a JPEG picture
            ImageReader reader = ImageReader.newInstance(imageDimensions.getWidth(), imageDimensions.getHeight(), ImageFormat.JPEG, 1);

            // List of surfaces for a new capture session (later in code)
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(cameraPreview.getSurfaceTexture()));

            // Capture builder to build a single capture
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            //captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            MeteringRectangle focusArea = new MeteringRectangle(2000,
                    1500,
                    150,
                    150,
                    1000);
            captureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{focusArea});
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);


            // Set the listener for the image reader
            ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // Get the last image bytes and save it into a file
                    try (Image image = reader.acquireLatestImage()) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    }
                }

                // Save a bytes array into a file
                void save(byte[] bytes) {
                    // Save bytes to the new photo file
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(photoFile);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ImageProcessor.cropAndRotateBitmap(bitmap, trimDimension).compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), R.string.error_creating_file, Toast.LENGTH_SHORT).show();
                    } finally {
                        try {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            CameraActivity.this.finish();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), R.string.error_creating_file, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

            // New thread to handle picture taking
            HandlerThread handlerThread = new HandlerThread("Take picture handler");
            handlerThread.start();
            final Handler handler = new Handler(handlerThread.getLooper());
            reader.setOnImageAvailableListener(imageAvailableListener, handler);

            // Handle capture started and completed
            final CameraCaptureSession.CaptureCallback previewSession = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    // If creating a new mole, create needed folder
                    if (!moleFolder.exists()) {
                        moleFolder.mkdirs();
                    }

                    // Create the photo file with current timestamp
                    try {
                        photoFile = FileManager.createTempPhotoFile(moleFolder.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Save the focus used to take the photo into a JSON file
                    File imagesInformationFile = new File(moleFolder.getAbsolutePath(), "ImagesInformation.json");
                    ImageModel imageModel = new ImageModel(photoFile.getName(), String.valueOf(result.get(CaptureResult.LENS_FOCUS_DISTANCE)),
                            String.valueOf(result.get(CaptureResult.LENS_FOCAL_LENGTH)), String.valueOf(characteristics.get(SENSOR_INFO_PHYSICAL_SIZE).getHeight()),
                            String.valueOf(imageDimensions.getHeight()));
                    Gson gson = new Gson();
                    if (!imagesInformationFile.exists()) {
                        try {
                            List<ImageModel> imageModelList = new ArrayList<>();
                            imageModelList.add(imageModel);
                            imagesInformation = new ImagesInformation(imageModelList);
                            String imagesInformationJSON = gson.toJson(imagesInformation);

                            // Create the new JSON file
                            FileWriter JSONWriter = new FileWriter(imagesInformationFile);
                            JSONWriter.append(imagesInformationJSON);
                            JSONWriter.flush();
                            JSONWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            imagesInformation = ImagesInformation.readImagesInformationJSON(imagesInformationFile, getApplicationContext());
                            imagesInformation.addImageModel(imageModel);
                            String imagesInformationJSON = gson.toJson(imagesInformation);

                            // Update the JSON file
                            FileWriter JSONWriter = new FileWriter(imagesInformationFile);
                            JSONWriter.write(imagesInformationJSON);
                            JSONWriter.flush();
                            JSONWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), previewSession, handler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, handler);
        } catch (CameraAccessException e) {
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
        }
    }

    // Open back camera with max resolution
    public void openCamera() {
        // Get camera service
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Get back camera and its characteristics
            assert manager != null;
            String cameraId = manager.getCameraIdList()[0];
            characteristics = manager.getCameraCharacteristics(cameraId);

            // Check permissions and get back if necessary
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                CameraActivity.this.finish();
                return;
            }
            // Open back camera
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
        }

        // Informative dialog
        AlertDialog alertDialog = new AlertDialog.Builder(CameraActivity.this).create();
        alertDialog.setTitle(R.string.photo_instructions_title);
        alertDialog.setMessage(getString(R.string.photo_instructions));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    // Configure the listener for the camera preview
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // When surface is available open the camera
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Not used
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // Not used
        }
    };

    // Manage camera device callbacks
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // When camera device is opened, get it and start it
            cameraDevice = camera;
            startCamera();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            // Not used
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            // Not used
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        // Close camera device and get back
        if (cameraDevice != null) {
            cameraDevice.close();
        }
        CameraActivity.this.finish();
    }

    // Start the camera
    void startCamera() {
        if (cameraDevice == null || !cameraPreview.isAvailable()) {
            return;
        }

        // Get the texture from the preview
        SurfaceTexture texture = cameraPreview.getSurfaceTexture();
        if (texture == null) {
            return;
        }

        // Set buffer size and preview aspect ratio
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        assert map != null;
        imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];
        if (imageDimensions.getHeight() > imageDimensions.getWidth()) {
            texture.setDefaultBufferSize(imageDimensions.getHeight(), imageDimensions.getWidth());
            cameraPreview.setAspectRatio(imageDimensions.getWidth(), imageDimensions.getHeight());
        } else {
            texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
            cameraPreview.setAspectRatio(imageDimensions.getHeight(), imageDimensions.getWidth());
        }

        // Set central square adjusted dimension
        int imagePixels = imageDimensions.getHeight() * imageDimensions.getWidth();
        int previewPixels = cameraPreview.getHeight() * cameraPreview.getWidth();
        int trimPixels = trimDimension * trimDimension;
        int adjustedTrimPixels = previewPixels / (imagePixels / trimPixels);
        squareView.getLayoutParams().width = (int) Math.round(Math.sqrt(adjustedTrimPixels));
        squareView.getLayoutParams().height = (int) Math.round(Math.sqrt(adjustedTrimPixels));

        // Get the surface and attach it to the camera request builder
        Surface surface = new Surface(texture);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
        }
        captureRequestBuilder.addTarget(surface);

        // Create a capture session and manage configuration
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = Objects.requireNonNull(session);
                    getChangedPreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
        }
    }

    // Manage preview change (configured)
    void getChangedPreview() {
        if (cameraDevice == null) {
            return;
        }

        // Set capture request builder control mode to auto
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        // Create a new thread to handle background operations
        HandlerThread thread = new HandlerThread("Background thread");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        try {
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
        } catch (CameraAccessException e) {
            Toast.makeText(getApplicationContext(), R.string.error_access_camera, Toast.LENGTH_SHORT).show();
        }
    }

}