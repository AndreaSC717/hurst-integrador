package com.husrt.service;

import com.husrt.config.AppProperties;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class EstudianteFotoService {

    private static final Set<String> EXTENSIONES = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final Path baseDir;

    public EstudianteFotoService() {
        String configured = AppProperties.get("fotos.estudiantes.dir", "data/fotos-estudiantes");
        baseDir = Path.of(configured).toAbsolutePath().normalize();
    }

    public Path baseDir() {
        return baseDir;
    }

    /**
     * Guarda la imagen y devuelve la ruta relativa almacenada en {@code foto_url}.
     */
    public String guardar(long idEstudiante, Path archivoOrigen) throws IOException {
        if (!Files.isRegularFile(archivoOrigen)) {
            throw new IOException("Archivo de imagen no encontrado.");
        }
        long size = Files.size(archivoOrigen);
        if (size > MAX_BYTES) {
            throw new IOException("La imagen supera el tamaño máximo de 5 MB.");
        }
        String ext = extensionDeArchivo(archivoOrigen);
        if (ext == null) {
            throw new IOException("Formato no permitido. Use JPG, PNG, GIF o WEBP.");
        }
        Files.createDirectories(baseDir);
        String nombre = "est_" + idEstudiante + "." + ext;
        Path destino = baseDir.resolve(nombre);
        Files.copy(archivoOrigen, destino, StandardCopyOption.REPLACE_EXISTING);
        return nombre;
    }

    public void eliminar(String fotoUrl) throws IOException {
        if (fotoUrl == null || fotoUrl.isBlank()) {
            return;
        }
        Optional<Path> p = resolver(fotoUrl);
        if (p.isPresent()) {
            Files.delete(p.get());
        }
    }

    public Optional<Path> resolver(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.isBlank()) {
            return Optional.empty();
        }
        Path p = Path.of(fotoUrl);
        if (p.isAbsolute()) {
            return Files.isRegularFile(p) ? Optional.of(p) : Optional.empty();
        }
        Path local = baseDir.resolve(fotoUrl).normalize();
        if (!local.startsWith(baseDir)) {
            return Optional.empty();
        }
        return Files.isRegularFile(local) ? Optional.of(local) : Optional.empty();
    }

    public Optional<Image> cargarImagen(String fotoUrl) {
        return resolver(fotoUrl).map(path -> new Image(path.toUri().toString(), true));
    }

    public static boolean esImagen(Path path) {
        return extensionDeArchivo(path) != null;
    }

    private static String extensionDeArchivo(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return null;
        }
        String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if ("jpeg".equals(ext)) {
            return "jpg";
        }
        return EXTENSIONES.contains(ext) ? ext : null;
    }

    public static String filtroFileChooser() {
        return "*.jpg;*.jpeg;*.png;*.gif;*.webp";
    }
}
