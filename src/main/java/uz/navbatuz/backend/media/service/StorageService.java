package uz.navbatuz.backend.media.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.config.FileStorageProperties;
import uz.navbatuz.backend.media.dto.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final FileStorageProperties props;

    public static final String SCOPE_PROVIDER = "provider";
    public static final String SCOPE_SERVICE  = "service";
    public static final String SCOPE_USER     = "user";
    public static final String SCOPE_MISC     = "misc";

    public UploadedFile store(MultipartFile file, String scope, String ownerId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
        }

        // 1) Validate content type + magic bytes
        String contentType = normalize(file.getContentType());
        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            default -> {
                // trust but verify via magic
                if (isPng(file))      yield ".png";
                else if (isJpeg(file)) yield ".jpg";
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PNG/JPEG are allowed");
            }
        };

        String safeScope = (scope == null || scope.isBlank()) ? SCOPE_MISC : scope.toLowerCase(Locale.ROOT);
        String subdir = ownerId != null && !ownerId.isBlank()
                ? safeScope + "/" + ownerId
                : safeScope;

        Path baseDir = Paths.get(props.getDir());
        Path targetDir = baseDir.resolve(subdir).normalize().toAbsolutePath();

        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create directory");
        }

        String filename = UUID.randomUUID() + ext;
        Path targetFile = targetDir.resolve(filename).normalize();

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file");
        }

        // Build public URL: baseUrl + /uploads/<subdir>/<filename>
        String relative = subdir + "/" + filename;
        String publicUrl = props.getBaseUrl().replaceAll("/+$","") + "/uploads/" + relative;

        return new UploadedFile(publicUrl, "/uploads/" + relative, contentType, file.getSize());
    }

    private static String normalize(String ct) {
        if (ct == null) return "";
        ct = ct.toLowerCase(Locale.ROOT);
        // Some browsers send "image/jpg"
        if (ct.equals("image/jpg")) return "image/jpeg";
        return ct;
    }

    private static boolean isPng(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] sig = in.readNBytes(8);
            byte[] png = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            if (sig.length != 8) return false;
            for (int i=0;i<8;i++) if (sig[i] != png[i]) return false;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isJpeg(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] sig = in.readNBytes(3);
            if (sig.length < 3) return false;
            return (sig[0] & 0xFF) == 0xFF && (sig[1] & 0xFF) == 0xD8 && (sig[2] & 0xFF) == 0xFF;
        } catch (IOException e) {
            return false;
        }
    }
}
