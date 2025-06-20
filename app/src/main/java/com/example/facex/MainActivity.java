package com.example.facex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button cameraButton;
    private final static int REQUEST_IMAGE_CAPTURE = 124;
    private final static int CAMERA_PERMISSION_CODE = 101;

    FaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        cameraButton = findViewById(R.id.camera_button);

        // ✅ Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        // ✅ Open camera without resolveActivity check
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Camera button clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });
    }

    // ✅ Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ After capturing image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap) extra.get("data");
            detectFace(bitmap);
        }
    }

    // ✅ Face detection using ML Kit
    private void detectFace(Bitmap bitmap) {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        detector = FaceDetection.getClient(options);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        String resultText = "";
                        int i = 1;
                        for (Face face : faces) {
                            resultText += "\nFACE " + i + ": ";
                            resultText += "\nSmile: " + (face.getSmilingProbability() != null ? face.getSmilingProbability() * 100 : "N/A") + "%";
                            resultText += "\nLeft eye open: " + (face.getLeftEyeOpenProbability() != null ? face.getLeftEyeOpenProbability() * 100 : "N/A") + "%";
                            resultText += "\nRight eye open: " + (face.getRightEyeOpenProbability() != null ? face.getRightEyeOpenProbability() * 100 : "N/A") + "%\n";
                            i++;
                        }

                        if (faces.isEmpty()) {
                            Toast.makeText(MainActivity.this, "NO FACE DETECTED", Toast.LENGTH_SHORT).show();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("RESULT_TEXT", resultText);
                            DialogFragment resultDialog = new ResultDialog();
                            resultDialog.setArguments(bundle);
                            resultDialog.setCancelable(true);
                            resultDialog.show(getSupportFragmentManager(), "RESULT_DIALOG");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Oops, Something went wrong", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
    }
}
