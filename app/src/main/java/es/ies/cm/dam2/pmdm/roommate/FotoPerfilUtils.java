package es.ies.cm.dam2.pmdm.roommate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilidades para guardar y mostrar las fotos de perfil de los companeros.
 */
public final class FotoPerfilUtils {

    private static final String CARPETA_FOTOS = "fotos_companeros";
    private static final String FOTO_POR_DEFECTO = "ic_persona";

    private FotoPerfilUtils() {
    }

    /**
     * Copia la imagen elegida al almacenamiento interno de la aplicacion.
     * Devuelve la ruta del archivo o null si no se ha podido guardar.
     */
    public static String guardarFotoPerfil(Context context, Uri origen) {
        if (context == null || origen == null) {
            return null;
        }

        File carpeta = new File(context.getFilesDir(), CARPETA_FOTOS);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            return null;
        }

        File destino = new File(
                carpeta,
                "foto_" + System.currentTimeMillis() + ".jpg"
        );

        try (InputStream entrada = context
                .getContentResolver()
                .openInputStream(origen);
             OutputStream salida = new FileOutputStream(destino)) {

            if (entrada == null) {
                return null;
            }

            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = entrada.read(buffer)) != -1) {
                salida.write(buffer, 0, leidos);
            }

            return destino.getAbsolutePath();
        } catch (Exception e) {
            if (destino.exists()) {
                destino.delete();
            }
            return null;
        }
    }

    /**
     * Muestra la foto indicada. Si no hay foto o no existe el archivo,
     * se muestra el icono por defecto.
     */
    public static void mostrarFotoPerfil(ImageView imageView, String fotoPerfil) {
        if (imageView == null) {
            return;
        }

        // Recorta la imagen con la forma ovalada del fondo en cualquier version.
        imageView.setClipToOutline(true);

        File archivo = null;
        if (fotoPerfil != null
                && !fotoPerfil.trim().isEmpty()
                && !FOTO_POR_DEFECTO.equals(fotoPerfil)) {
            archivo = new File(fotoPerfil);
        }

        if (archivo == null || !archivo.exists()) {
            mostrarFotoPorDefecto(imageView);
            return;
        }

        Bitmap bitmap = cargarBitmap(imageView.getContext(), archivo);
        if (bitmap == null) {
            mostrarFotoPorDefecto(imageView);
            return;
        }

        imageView.setPadding(0, 0, 0, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Borra una foto de perfil que ya no se utiliza.
     */
    public static void borrarFotoPerfil(String fotoPerfil) {
        if (fotoPerfil == null
                || fotoPerfil.trim().isEmpty()
                || FOTO_POR_DEFECTO.equals(fotoPerfil)) {
            return;
        }

        File archivo = new File(fotoPerfil);
        if (archivo.exists()) {
            archivo.delete();
        }
    }

    private static void mostrarFotoPorDefecto(ImageView imageView) {
        int padding = dp(imageView, 28);
        imageView.setImageResource(R.drawable.ic_persona);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    private static Bitmap cargarBitmap(Context context, File archivo) {
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivo.getAbsolutePath(), opciones);

        int tamanoPx = (int) (320 * context
                .getResources()
                .getDisplayMetrics()
                .density);
        int escala = 1;

        while (opciones.outWidth / escala > tamanoPx
                && opciones.outHeight / escala > tamanoPx) {
            escala *= 2;
        }

        opciones.inJustDecodeBounds = false;
        opciones.inSampleSize = escala;

        return BitmapFactory.decodeFile(archivo.getAbsolutePath(), opciones);
    }

    private static int dp(ImageView imageView, int valor) {
        return (int) (valor * imageView
                .getResources()
                .getDisplayMetrics()
                .density);
    }
}
