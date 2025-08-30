package uz.navbatuz.backend.receptionist.dto;


public record ReceptionistUpdateReq(
        String name,
        String surname,
        String phoneNumber,
        String email
) {}
