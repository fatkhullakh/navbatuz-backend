package uz.navbatuz.backend.provider.dto;

import uz.navbatuz.backend.common.Status;
import uz.navbatuz.backend.common.WorkerType;

public record EnableOwnerAsWorkerRequest(
        WorkerType workerType,
        Boolean isActive,
        Status status
) {}

