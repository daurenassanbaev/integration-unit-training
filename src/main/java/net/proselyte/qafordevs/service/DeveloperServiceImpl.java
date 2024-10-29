package net.proselyte.qafordevs.service;

import lombok.RequiredArgsConstructor;
import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.entity.Status;
import net.proselyte.qafordevs.exception.DeveloperNotFoundException;
import net.proselyte.qafordevs.exception.DeveloperWithDuplicateEmailException;
import net.proselyte.qafordevs.repository.DeveloperRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {
    private final DeveloperRepository developerRepository;
    @Override
    public DeveloperEntity saveDeveloper(DeveloperEntity entity) {
        DeveloperEntity duplicateCandidate = developerRepository.findByEmail(entity.getEmail());
        if (Objects.nonNull(duplicateCandidate)) {
            throw new DeveloperWithDuplicateEmailException("Developer with defined email is already exists");
        }
        entity.setStatus(Status.ACTIVE);
        return developerRepository.save(entity);
    }

    @Override
    public DeveloperEntity updateDeveloper(DeveloperEntity entity) {
        boolean isExists = developerRepository.existsById(entity.getId());
        if (!isExists) {
            throw new DeveloperNotFoundException("Developer not found");
        }
        return developerRepository.save(entity);
    }

    @Override
    public DeveloperEntity getDeveloperById(Integer id) {
        return developerRepository.findById(id).orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));
    }

    @Override
    public DeveloperEntity getDeveloperByEmail(String email) {
        DeveloperEntity obtainedDeveloper = developerRepository.findByEmail(email);
        if (Objects.isNull(obtainedDeveloper)) {
            throw new DeveloperNotFoundException("Developer not found");
        }
        return obtainedDeveloper;
    }

    @Override
    public List<DeveloperEntity> getAllDevelopers() {

        return developerRepository.findAll()
                .stream()
                .filter(d -> {
                    return d.getStatus().equals(Status.ACTIVE);
                })
                .toList();
    }

    @Override
    public List<DeveloperEntity> getAllActiveBySpecialty(String specialty) {
        return developerRepository.findAllActiveBySpecialty(specialty);
    }

    @Override
    public void softDeleteById(Integer id) {
        DeveloperEntity obtainedDeveloper = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));
        obtainedDeveloper.setStatus(Status.DELETED);
        developerRepository.save(obtainedDeveloper);
    }

    @Override
    public void hardDeleteById(Integer id) {
        DeveloperEntity obtainedDeveloper = developerRepository.findById(id).orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));
        developerRepository.deleteById(obtainedDeveloper.getId());

    }
}
