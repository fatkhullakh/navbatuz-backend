package uz.navbatuz.backend.media.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.navbatuz.backend.media.dto.UploadedFile;
import uz.navbatuz.backend.media.service.StorageService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/uploads")
public class UploadController {

    private final StorageService storage;

    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN','CUSTOMER')")
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadedFile> upload(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "ownerId", required = false) String ownerId
    ) {
        var saved = storage.store(file, scope, ownerId);
        return ResponseEntity.ok(saved);
    }
}
