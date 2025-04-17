package co.edu.ue.aplicacionact3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView ivCapturedImage;
    private Button btnTakePicture;
    private TextView tvPhotoPlaceholder; // Cambio: Variable añadida para el TextView de placeholder

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initViews();// Inicializa los componentes de la vista
        setupListeners(); // Configura los listeners de los botones
    }
    // iniciar elementos de la interfaz de usuario
    private void initViews() {
        ivCapturedImage = findViewById(R.id.ivCapturedImage);
        btnTakePicture = findViewById(R.id.btnTakePicture);
        tvPhotoPlaceholder = findViewById(R.id.tvPhotoPlaceholder); // Cambio: Inicialización del TextView
    }
    //listeners para los componentes
    private void setupListeners() {
        btnTakePicture.setOnClickListener(v -> dispatchTakePictureIntent());
    }
    // Prepara y lanza la intención para capturar una imagen usando la cámara del dispositivo

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show();
        }
    }
    //Maneja el resultado de la actividad de cámara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivCapturedImage.setImageBitmap(imageBitmap);

            //  Ocultar el TextView de placeholder cuando se muestra la imagen
            tvPhotoPlaceholder.setVisibility(View.GONE);
        }
    }
}