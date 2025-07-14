package uz.navbatuz.backend.worker.dto;

import java.util.UUID;

public record WorkerResponseForService(
        UUID id,
        String name,
        String surname
) {}
