package uz.navbatuz.backend.worker.dto;

import uz.navbatuz.backend.common.WorkerType;

import java.util.UUID;

public record CreateWorkerRequest(UUID user, UUID provider, WorkerType workerType) {
}
