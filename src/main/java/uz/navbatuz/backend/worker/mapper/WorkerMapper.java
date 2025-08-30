package uz.navbatuz.backend.worker.mapper;

import org.springframework.stereotype.Component;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.worker.dto.WorkerDetailsDto;
import uz.navbatuz.backend.worker.dto.WorkerPublicDetailsDto;
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

    public WorkerDetailsDto mapToDetails(Worker worker) {
        User user = worker.getUser();
        return new WorkerDetailsDto(
                worker.getId(),
                user.getName() + " " + user.getSurname(),
                worker.getProvider().getName(),
                user.getGender(),
                user.getPhoneNumber(),
                user.getEmail(),
                worker.getWorkerType(),
                worker.getStatus(),
                worker.getAvgRating(),
                worker.getHireDate(),
                worker.isActive(),
                user.getAvatarUrl()
        );
    }

    public WorkerPublicDetailsDto mapToPublicDetails(Worker worker) {
        var u = worker.getUser();
        String fullName = ((u.getName() == null ? "" : u.getName()) + " " +
                (u.getSurname() == null ? "" : u.getSurname())).trim();

        return new WorkerPublicDetailsDto(
                worker.getId(),
                fullName.isEmpty() ? "Worker" : fullName,
                u.getAvatarUrl(),
                worker.getStatus() == null ? null : worker.getStatus().name(),
                worker.getAvgRating(),
                worker.getReviewsCount()
        );
    }
}
