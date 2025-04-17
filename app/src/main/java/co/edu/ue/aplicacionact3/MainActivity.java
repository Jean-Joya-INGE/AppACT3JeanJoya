package co.edu.ue.aplicacionact3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Variables de instancia
    private Context context;
    private Activity activity;
    private TextView tvVersionAndroid;
    private int versionSDK;
    private ProgressBar pbLevelBaterry;
    private TextView tvLevelBaterry;
    private IntentFilter batteryFilter;
    private TextView tvConexion;
    private ConnectivityManager conexion;
    private CameraManager cameraManager;
    private String cameraId;
    private Button onFlash;
    private Button offFlash;
    private EditText nameFile;
    private Button onBlue;
    private Button offBlue;
    private Button btnOpenBluetooth;
    private Button btnOpenCamera;
    private CLFile clfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [Cambio] Manejo de permisos extraído a método separado para limpieza
        handlePermissions();
        // Inicialización de componentes
        initObjects();
        initBluetooth();
        setupFlashlight();
        setupFileSaving();
        setupActivityButtons();
        batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, batteryFilter);
    }
    // Bateria
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBattery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            pbLevelBaterry.setProgress(levelBattery);
            tvLevelBaterry.setText("Nivel de batería: " + levelBattery + "%");
        }
    };

    private void checkConnection() {
        try {
            conexion = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conexion != null) {
                NetworkInfo networkInfo = conexion.getActiveNetworkInfo();
                boolean stateNet = networkInfo != null && networkInfo.isConnectedOrConnecting();
                tvConexion.setText(stateNet ? "Conexión: ACTIVA" : "Conexión: INACTIVA");
            }
        } catch (Exception e) {
            Log.e("CONEXION", e.getMessage());
        }
    }

     // Maneja toda la lógica de solicitud de permisos
    private void handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionManager.checkPermissions(this)) {
                if (shouldShowPermissionRationale()) {
                    // Mostrar explicación si el usuario ya denegó permisos antes
                    showPermissionExplanationDialog();
                } else {
                    // Solicitar permisos directamente si es la primera vez
                    PermissionManager.requestPermissions(this);
                }
            }

        }
    }

     // Determina si se debe mostrar la explicación de permisos
    private boolean shouldShowPermissionRationale() {
        return shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) :
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION));
    }

     // Muestra el diálogo explicativo sobre los permisos
    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos requeridos")
                .setMessage("Esta aplicación necesita permisos para:\n\n" +
                        "- Cámara: Para usar la linterna y tomar fotografías\n" +
                        "- Almacenamiento: Para guardar archivos con información del dispositivo\n" +
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                                "- Bluetooth: Para conectar con dispositivos externos" :
                                "- Ubicación: Requerido por Android para funciones Bluetooth (no usamos su ubicación)"))
                .setPositiveButton("Entendido", (dialog, which) -> {
                    PermissionManager.requestPermissions(this);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Toast.makeText(this, "Algunas funciones no estarán disponibles", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    // variables de los elementos visuales
    private void initObjects() {
        this.context = getApplicationContext();
        this.activity = this;
        this.tvVersionAndroid = findViewById(R.id.tvVersionAndroid);
        this.pbLevelBaterry = findViewById(R.id.pbLevelBaterry);
        this.tvLevelBaterry = findViewById(R.id.tvNivel);
        this.tvConexion = findViewById(R.id.tvConection2);
        this.nameFile = findViewById(R.id.etNombreArchivo);
        this.onFlash = findViewById(R.id.OnFlash);
        this.offFlash = findViewById(R.id.OffFlash);
        this.onBlue = findViewById(R.id.btnActivar);
        this.offBlue = findViewById(R.id.btnDesactivar);
        this.btnOpenBluetooth = findViewById(R.id.btnOpenBluetooth);
        this.btnOpenCamera = findViewById(R.id.btnOpenCamera);
        this.clfile = new CLFile(context);
    }

    private void initBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        onBlue.setOnClickListener(v -> {
            if (bluetoothAdapter == null) {
                Toast.makeText(context, "Bluetooth no soportado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        2);
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 2);
            } else {
                Toast.makeText(context, "Bluetooth ya está activado", Toast.LENGTH_SHORT).show();
            }
        });

        offBlue.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
                Toast.makeText(context, "Bluetooth desactivado", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Configura los listeners para los botones de la linterna
    private void setupFlashlight() {
        onFlash.setOnClickListener(this::turnOnFlashlight);
        offFlash.setOnClickListener(this::turnOffFlashlight);
    }
    // Enciende la linterna
    private void turnOnFlashlight(View view) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        3);
                return;
            }

            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            Log.e("Linterna", "Error al encender: " + e.getMessage());
        }
    }
    // Apaga la linterna
    private void turnOffFlashlight(View view) {
        try {
            if (cameraManager != null &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                cameraManager.setTorchMode(cameraId, false);
            }
        } catch (Exception e) {
            Log.e("Linterna", "Error al apagar: " + e.getMessage());
        }
    }
    // Guarda el archivo con información del dispositivo en descargas
    private void setupFileSaving() {
        Button btnSaveFile = findViewById(R.id.btnGuardarArchivo);
        btnSaveFile.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        4);
                return;
            }

            String fileName = nameFile.getText().toString().trim();
            if (fileName.isEmpty()) {
                Toast.makeText(context, "Ingrese un nombre de archivo", Toast.LENGTH_SHORT).show();
                return;
            }

            String versionSO = Build.VERSION.RELEASE;
            int levelBattery = getBatteryLevel();
            String studentName = "Jean Marco Ortega joya";

            String fileContent = "Estudiante: " + studentName + "\n" +
                    "Versión Android: " + versionSO + "\n" +
                    "Nivel de Batería: " + levelBattery + "%";

            if (clfile.saveFile(fileName + ".txt", fileContent)) {
                Toast.makeText(context, "Archivo guardado con éxito en Descargas", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Configura los listeners para los botones de las activities
    private void setupActivityButtons() {
        btnOpenBluetooth.setOnClickListener(v -> {
            startActivity(new Intent(this, BluetoothActivity.class));
        });

        btnOpenCamera.setOnClickListener(v -> {
            startActivity(new Intent(this, CameraActivity.class));
        });
    }
    // Obtiene el nivel de batería
    private int getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return batteryIntent != null ? batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
    }

    // Arroja la versión de Android y el estado de conexion a internet
    @Override
    protected void onResume() {
        super.onResume();
        String versionSO = Build.VERSION.RELEASE;
        versionSDK = Build.VERSION.SDK_INT;
        tvVersionAndroid.setText("Android: " + versionSO + " (SDK: " + versionSDK + ")");

        checkConnection();
    }
    // verifica el apagado de la linterna y desregistra el receiver de la bateria
    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnOffFlashlight(null);
        unregisterReceiver(broadcastReceiver);
    }
    // Maneja la respuesta de los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.REQUEST_CODE) {
            boolean allPermissionsGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Toast.makeText(this, "Todos los permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permisos requeridos")
                        .setMessage("La aplicación necesita todos los permisos para funcionar correctamente. ¿Desea intentarlo de nuevo?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            PermissionManager.requestPermissions(this);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            Toast.makeText(this, "Algunas funciones pueden no estar disponibles", Toast.LENGTH_LONG).show();
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }
}