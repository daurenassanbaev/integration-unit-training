package net.proselyte.qafordevs.service;

import net.proselyte.qafordevs.entity.DeveloperEntity;

import java.util.List;

public interface DeveloperService {
    DeveloperEntity saveDeveloper(DeveloperEntity entity);
    DeveloperEntity updateDeveloper(DeveloperEntity entity);
    DeveloperEntity getDeveloperById(Integer id);
    DeveloperEntity getDeveloperByEmail(String email);
    List<DeveloperEntity> getAllDevelopers();
    List<DeveloperEntity> getAllActiveBySpecialty(String specialty);

    // change status
    void softDeleteById(Integer id);
    void hardDeleteById(Integer id);
}
