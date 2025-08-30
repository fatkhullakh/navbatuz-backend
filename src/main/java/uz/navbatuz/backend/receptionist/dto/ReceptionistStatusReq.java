package uz.navbatuz.backend.receptionist.dto;

import uz.navbatuz.backend.receptionist.model.Receptionist;

import java.time.LocalDate;

public record ReceptionistStatusReq(
        Receptionist.ReceptionistStatus status,
        LocalDate terminationDate // required when TERMINATED
) {}
