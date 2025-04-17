package co.edu.ue.aplicacionact3;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
// Manejar operaciones relacionadas con archivos
public class CLFile {

    private final Context context;

    public CLFile(Context context) {
        this.context = context;
    }
    //Guarda un archivo en el directorio de Descargas del almacenamiento externo
    public boolean saveFile(String fileName, String content) {
        // Verificar si el almacenamiento externo está disponible
        if (!isExternalStorageWritable()) {
            Toast.makeText(context, "Almacenamiento externo no disponible", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Obtener el directorio de Descargas
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Crear el archivo
        File file = new File(downloadsDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
            return true;
        } catch (IOException e) {
            Toast.makeText(context, "Error al guardar el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    //Verifica si el almacenamiento externo está disponible para escritura
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}