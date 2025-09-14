package uz.navbatuz.backend.media.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uz.navbatuz.backend.config.FileStorageProperties;
import uz.navbatuz.backend.media.dto.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final FileStorageProperties props;
    private final S3Client s3;

    public static final String SCOPE_PROVIDER = "provider";
    public static final String SCOPE_SERVICE  = "service";
    public static final String SCOPE_USER     = "user";
    public static final String SCOPE_MISC     = "misc";

    public UploadedFile store(MultipartFile file, String scope, String ownerId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
        }

        // --- validate type + extension ---
        String contentType = normalize(file.getContentType());
        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            default -> {
                if (isPng(file))      yield ".png";
                else if (isJpeg(file)) yield ".jpg";
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PNG/JPEG are allowed");
            }
        };

        String safeScope = (scope == null || scope.isBlank())
                ? SCOPE_MISC : scope.toLowerCase(Locale.ROOT);
        String subdir = (ownerId != null && !ownerId.isBlank())
                ? safeScope + "/" + ownerId : safeScope;

        String filename = UUID.randomUUID() + ext;
        String key = subdir + "/" + filename;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Read failed");
        }

        try {
            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(props.getS3Bucket())
                    .key(key)
                    .contentType(contentType)
                    .cacheControl("public, max-age=31536000, immutable")
                    .build();
            s3.putObject(put, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 upload failed");
        }

        String base = props.getPublicBaseUrl().replaceAll("/+$", "");
        String publicUrl = base + "/" + key;

        // NOTE: relativePath now equals the S3 key (no /uploads prefix in CDN)
        return new UploadedFile(publicUrl, "/" + key, contentType, file.getSize());
    }

    private static String normalize(String ct) {
        if (ct == null) return "";
        ct = ct.toLowerCase(Locale.ROOT);
        return ct.equals("image/jpg") ? "image/jpeg" : ct;
    }

    private static boolean isPng(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] sig = in.readNBytes(8);
            byte[] png = new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            if (sig.length != 8) return false;
            for (int i = 0; i < 8; i++) if (sig[i] != png[i]) return false;
            return true;
        } catch (IOException e) { return false; }
    }

    private static boolean isJpeg(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] sig = in.readNBytes(3);
            if (sig.length < 3) return false;
            return (sig[0] & 0xFF) == 0xFF && (sig[1] & 0xFF) == 0xD8 && (sig[2] & 0xFF) == 0xFF;
        } catch (IOException e) { return false; }
    }
}
