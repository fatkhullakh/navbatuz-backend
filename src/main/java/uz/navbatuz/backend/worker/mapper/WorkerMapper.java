package uz.navbatuz.backend.worker.mapper;

import org.springframework.stereotype.Component;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.worker.dto.WorkerResponse;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;
import uz.navbatuz.backend.worker.model.Worker;

@Component
public class WorkerMapper {
    public WorkerResponseForService mapToWorkerResponse(Worker worker) {
        return new WorkerResponseForService(
                worker.getId(),
                worker.getUser().getName(),
                worker.getUser().getSurname()
        );
    }

    public WorkerResponse mapToResponse(Worker worker) {
        User user = worker.getUser();
        return new WorkerResponse(
                worker.getId(),
                user.getName() + " " + user.getSurname(),
                worker.getProvider().getName(),
                worker.getWorkerType(),
                worker.getStatus(),
                worker.getAvgRating(),
                worker.getHireDate(),
                worker.isActive()
        );
    }
}
