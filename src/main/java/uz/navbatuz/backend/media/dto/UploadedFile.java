package uz.navbatuz.backend.media.dto;

public record UploadedFile(
        String url,           // full public URL (e.g. http://localhost:8080/uploads/... )
        String path,          // relative web path (e.g. /uploads/... )
        String contentType,
        long size
) {}
