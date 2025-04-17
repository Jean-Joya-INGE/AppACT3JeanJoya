package co.edu.ue.aplicacionact3;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    // Componentes de la interfaz de usuario
    private BluetoothAdapter bluetoothAdapter;
    private Button btnToggleBluetooth;
    private Button btnListDevices;
    private ListView listPairedDevices;
    private TextView tvBluetoothStatus;

    //  solicitud para permisos Bluetooth
    private static final int BLUETOOTH_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // Inicializar vistas
        initViews();

        // Obtener el adaptador Bluetooth del dispositivo
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Verificar si el dispositivo soporta Bluetooth
        checkBluetoothSupport();

        // Configurar listeners para los botones
        setupListeners();
    }

    //Inicializa los componentes de la interfaz de usuario
    private void initViews() {
        btnToggleBluetooth = findViewById(R.id.btnToggleBluetooth);
        btnListDevices = findViewById(R.id.btnListDevices);
        listPairedDevices = findViewById(R.id.listPairedDevices);
        tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
    }

    //Verifica si el dispositivo tiene soporte para Bluetooth
    //y actualiza la interfaz en consecuencia
    private void checkBluetoothSupport() {
        if (bluetoothAdapter == null) {
            // Dispositivo no soporta Bluetooth
            tvBluetoothStatus.setText("Bluetooth no soportado");
            btnToggleBluetooth.setEnabled(false);
            btnListDevices.setEnabled(false);
        } else {
            // Actualizar estado inicial del Bluetooth
            updateBluetoothStatus();
        }
    }

    //Configura los listeners para los botones de la interfaz

    private void setupListeners() {
        btnToggleBluetooth.setOnClickListener(v -> toggleBluetooth());
        btnListDevices.setOnClickListener(v -> listPairedDevices());
    }

    // Alterna el estado del Bluetooth (activar/desactivar)

    private void toggleBluetooth() {
        // Verificar permisos antes de cualquier operación Bluetooth
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            // Desactivar Bluetooth
            try {
                bluetoothAdapter.disable();
                Toast.makeText(this, "Bluetooth desactivado", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Toast.makeText(this, "Error: Permiso denegado para desactivar Bluetooth", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Activar Bluetooth
            try {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            } catch (SecurityException e) {
                Toast.makeText(this, "Error: Permiso denegado para activar Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
        updateBluetoothStatus();// actualiza el estado del Bluetooth
    }

    // Actualiza la interfaz según el estado actual del Bluetooth

    private void updateBluetoothStatus() {
        if (bluetoothAdapter == null) return;

        if (bluetoothAdapter.isEnabled()) {
            tvBluetoothStatus.setText("Bluetooth: ACTIVADO");
            btnToggleBluetooth.setText("Desactivar Bluetooth");
        } else {
            tvBluetoothStatus.setText("Bluetooth: DESACTIVADO");
            btnToggleBluetooth.setText("Activar Bluetooth");
        }
    }

    //Lista los dispositivos Bluetooth emparejados

    private void listPairedDevices() {
        // Verificar permisos antes de listar dispositivos
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Activa el Bluetooth primero", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Obtener dispositivos emparejados
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            ArrayList<String> devicesList = new ArrayList<>();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    devicesList.add(device.getName() + "\n" + device.getAddress());
                }
            } else {
                devicesList.add("No hay dispositivos emparejados");
            }

            // Mostrar dispositivos en la lista
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    devicesList
            );
            listPairedDevices.setAdapter(adapter);
        } catch (SecurityException e) {
            Toast.makeText(this, "Error: Permiso denegado para acceder a dispositivos Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Verifica si se tienen los permisos necesarios para usar Bluetooth
     * según la versión de Android
     * @return true si se tienen todos los permisos necesarios, false en caso contrario
     */
    private boolean checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requiere BLUETOOTH_CONNECT y BLUETOOTH_SCAN
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Versiones anteriores requieren BLUETOOTH, BLUETOOTH_ADMIN y ACCESS_FINE_LOCATION
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    //Solicita los permisos necesarios para usar Bluetooth según la versión de Android

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                    },
                    BLUETOOTH_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    BLUETOOTH_REQUEST_CODE);
        }
    }
    //perseverar la respuesta a la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Manejar la respuesta a la solicitud de permisos Bluetooth
        if (requestCode == BLUETOOTH_REQUEST_CODE) {
            boolean allGranted = true;
            // Verificar si todos los permisos fueron concedidos
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                // Mensaje más descriptivo sobre qué permisos se necesitan
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Toast.makeText(this, "Se necesitan permisos de Bluetooth para usar esta función", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Se necesitan permisos de Bluetooth y ubicación para usar esta función", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    // Actualizar el estado del Bluetooth después de que el usuario responda al diálogo de activación
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            updateBluetoothStatus();
        }
    }
}