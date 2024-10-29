package net.proselyte.qafordevs.service;

import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.exception.DeveloperNotFoundException;
import net.proselyte.qafordevs.exception.DeveloperWithDuplicateEmailException;
import net.proselyte.qafordevs.repository.DeveloperRepository;
import net.proselyte.qafordevs.util.DataUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DeveloperServiceImplTests {
    @Mock
    private DeveloperRepository developerRepository;
    @InjectMocks
    private DeveloperServiceImpl serviceUnderTest;

    @Test
    @DisplayName("Test save developer functionality")
    public void givenDeveloperToSave_whenSaveDeveloper_thenRepositoryCalled() {
        //given
        DeveloperEntity developerToSave = DataUtils.getJohnDoeTransient();
        BDDMockito.given(developerRepository.findByEmail(anyString())).willReturn(null);
        BDDMockito.given(developerRepository.save(any(DeveloperEntity.class))).willReturn(DataUtils.getJohnDoePersisted());
        // when
        DeveloperEntity savedDeveloper = serviceUnderTest.saveDeveloper(developerToSave);
        // then
        Assertions.assertThat(developerRepository).isNotNull();
    }
    @Test
    @DisplayName("Test save developer with duplicate email functionality")
    public void givenDeveloperToSaveWithDuplicateEmail_whenSaveDeveloper_thenExceptionIsThrown() {
        // given
        DeveloperEntity developerToSave = DataUtils.getJohnDoeTransient();
        BDDMockito.given(developerRepository.findByEmail(anyString())).willReturn(DataUtils.getJohnDoePersisted());
        // when
        assertThrows(
                DeveloperWithDuplicateEmailException.class, () -> serviceUnderTest.saveDeveloper(developerToSave)
        );
        // then
        verify(developerRepository, never()).save(any(DeveloperEntity.class));
    }
    @Test
    @DisplayName("Test update developer functionality")
    public void givenDeveloperToUpdate_whenUpdateDeveloper_thenRepositoryIsCalled() {
        // given
        DeveloperEntity developerToUpdate = DataUtils.getJohnDoePersisted();
        BDDMockito.given(developerRepository.existsById(anyInt())).willReturn(true);
        BDDMockito.given(developerRepository.save(any(DeveloperEntity.class))).willReturn(developerToUpdate);
        // when
        DeveloperEntity updatedDeveloper = serviceUnderTest.updateDeveloper(developerToUpdate);
        // then
        Assertions.assertThat(updatedDeveloper).isNotNull();
        verify(developerRepository, times(1)).save(any(DeveloperEntity.class));
    }

    @Test
    @DisplayName("Test update developer with incorrect id functionality")
    public void givenDeveloperToUpdateWithIncorrectId_whenUpdateDeveloper_thenExceptionIsThrown() {
        // given
        DeveloperEntity developerToUpdate = DataUtils.getJohnDoePersisted();
        BDDMockito.given(developerRepository.existsById(anyInt())).willReturn(false);
        // when
        assertThrows(
                DeveloperNotFoundException.class, () -> serviceUnderTest.updateDeveloper(developerToUpdate)
        );
        // then
        verify(developerRepository, never()).save(any(DeveloperEntity.class));
    }
    @Test
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetById_thenDeveloperIsReturned() {
        // given
        BDDMockito.given(developerRepository.findById(anyInt())).willReturn(Optional.of(DataUtils.getJohnDoePersisted()));
        // when
        DeveloperEntity developer = serviceUnderTest.getDeveloperById(1);
        // then
        Assertions.assertThat(developer).isNotNull();
    }
    @Test
    @DisplayName("Test get developer by id functionality")
    public void givenIncorrectId_whenGetById_thenExceptionIsThrown() {
        // given
        BDDMockito.given(developerRepository.findById(anyInt())).willThrow(DeveloperNotFoundException.class);
        // when
        assertThrows(
             DeveloperNotFoundException.class, () -> serviceUnderTest.getDeveloperById(1)
        );
        // then
    }

    @Test
    @DisplayName("Test get developer by email functionality")
    public void givenEmail_whenGetDeveloperByEmail_then() {
        // given
        String email = "john.doe@gmail.com";
        BDDMockito.given(developerRepository.findByEmail(anyString())).willReturn(DataUtils.getJohnDoePersisted());
        // when
        DeveloperEntity obtainedDeveloper = serviceUnderTest.getDeveloperByEmail(email);
        // then
        Assertions.assertThat(obtainedDeveloper).isNotNull();
    }
    @Test
    @DisplayName("Test get developer by incorrect email functionality")
    public void givenIncorrectEmail_whenGetDeveloperByEmail_thenExceptionIsThrown() {
        // given
        String email = "null";
        BDDMockito.given(developerRepository.findByEmail(anyString())).willThrow(DeveloperNotFoundException.class);
        // when
        assertThrows(
                DeveloperNotFoundException.class, () -> serviceUnderTest.getDeveloperByEmail(email)
        );
        // then
    }
    @Test
    @DisplayName("Test get all developers functionality")
    public void givenThreeDevelopers_whenGetDevelopers_thenOnlyActiveAreReturned() {
        // given
        DeveloperEntity developerEntity2 = DataUtils.getJohnDoePersisted();
        DeveloperEntity developerEntity1 = DataUtils.getFrankJonesPersisted();
        DeveloperEntity developerEntity3 = DataUtils.getMikeSmithPersisted();
        BDDMockito.given(developerRepository.findAll()).willReturn(List.of(developerEntity2, developerEntity3, developerEntity1));
        // when
        List<DeveloperEntity> list = serviceUnderTest.getAllDevelopers();
        // then
        Assertions.assertThat(list.size()).isEqualTo(2);
    }


    @Test
    @DisplayName("Test get all active by specialty functionality")
    public void givenThreeDevelopersAndTwoActive_whenGetAllActiveBySpecialty_thenDevelopersAreReturned() {
        // given
        DeveloperEntity developerEntity2 = DataUtils.getJohnDoePersisted();
        DeveloperEntity developerEntity1 = DataUtils.getFrankJonesPersisted();
        DeveloperEntity developerEntity3 = DataUtils.getMikeSmithPersisted();
        BDDMockito.given(developerRepository.findAllActiveBySpecialty(anyString())).willReturn(List.of(developerEntity2, developerEntity3)) ;
        // when
        List<DeveloperEntity> list = serviceUnderTest.getAllActiveBySpecialty(anyString());
        // then
        Assertions.assertThat(CollectionUtils.isEmpty(list)).isFalse();
        Assertions.assertThat(list.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Test soft delete by id functionality")
    public void givenId_whenSoftDeleteById_thenRepositorySaveMethodIsCalled() {
        // given
        BDDMockito.given(developerRepository.findById(anyInt())).willReturn(Optional.of(DataUtils.getJohnDoePersisted()));
        // when
        serviceUnderTest.softDeleteById(1);
        // then
        verify(developerRepository, times(1)).save(any(DeveloperEntity.class));
        verify(developerRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Test soft delete by incorrect id functionality")
    public void givenIncorrectId_whenSoftDeleteById_thenExceptionIsThrown() {
        // given
        BDDMockito.given(developerRepository.findById(anyInt())).willReturn(Optional.empty());
        // when
        assertThrows(
                DeveloperNotFoundException.class, () -> serviceUnderTest.softDeleteById(1)
        );
        // then
        verify(developerRepository, never()).save(any(DeveloperEntity.class));
    }

    @Test
    @DisplayName("Test hard delete by id functionality")
    public void givenId_whenHardDeleteById_thenDeleteRepoMethodIsCalled() {
        // given
        BDDMockito.given(developerRepository.findById(anyInt())).willReturn(Optional.of(DataUtils.getJohnDoePersisted()));
        // when
        serviceUnderTest.hardDeleteById(1);
        // then
        verify(developerRepository, times(1)).deleteById(anyInt());
    }

    @Test
    @DisplayName("Test hard delete by incorrect id functionality")
    public void givenIncorrectId_whenHardDeleteById_thenExceptionIsThrown() {
        // given
        BDDMockito.given(developerRepository.findById(anyInt())).willReturn(Optional.empty());
        // when
        assertThrows(
                DeveloperNotFoundException.class, () -> serviceUnderTest.hardDeleteById(1)
        );
        // then
        verify(developerRepository, never()).deleteById(anyInt());
    }
}
