package uz.navbatuz.backend.auth.dto;

import java.util.UUID;

public record WorkerRegisterResponse(String token, UUID userId) {

}
