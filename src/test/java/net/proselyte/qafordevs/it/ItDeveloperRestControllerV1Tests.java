package net.proselyte.qafordevs.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.proselyte.qafordevs.dto.DeveloperDto;
import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.entity.Status;
import net.proselyte.qafordevs.exception.DeveloperNotFoundException;
import net.proselyte.qafordevs.exception.DeveloperWithDuplicateEmailException;
import net.proselyte.qafordevs.repository.DeveloperRepository;
import net.proselyte.qafordevs.service.DeveloperService;
import net.proselyte.qafordevs.util.DataUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItDeveloperRestControllerV1Tests extends AbstractRestControllerBaseTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeveloperRepository developerRepository;

    @BeforeEach
    public void setUp() {
        developerRepository.deleteAll();
    }

    @Test
    @DisplayName("Test create developer functionality")
    public void givenDeveloperDto_whenCreateDeveloper_thenSuccessResponse() throws Exception{
        // given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();
        // when
        ResultActions result = mockMvc.perform(post("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        );
        // then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));
    }
    @Test
    @DisplayName("Test create developer with duplicate email functionality")
    public void givenDeveloperDtoWithDuplicateEmail_whenCreateDeveloper_thenErrorResponse() throws Exception {
        //given
        String duplicateEmail = "duplicate@mail.com";
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();
        developer.setEmail(duplicateEmail);
        developerRepository.save(developer);
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();
        dto.setEmail(duplicateEmail);
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
        //then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer with defined email is already exists")));
    }
    @Test
    @DisplayName("Test update developer functionality")
    public void givenDeveloperDto_whenUpdateDeveloper_thenSuccessResponse() throws Exception{
        // given
        String updatedEmail = "updated@gmail.com";
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity);
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
        dto.setId(entity.getId());
        dto.setEmail(updatedEmail);
        // when
        ResultActions result = mockMvc.perform(put("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        );
        // then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(updatedEmail)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));
    }
    @Test
    @DisplayName("Test update developer with incorrect id functionality")
    public void givenDeveloperDtoWithIncorrectEmail_whenUpdateDeveloper_thenErrorResponse() throws Exception {
        // given

        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
        // when
        ResultActions result = mockMvc.perform(put("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        );
        // then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)));
    }

    @Test
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetById_thenSuccessResponse() throws Exception {
        // given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity);
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/developers/" + entity.getId())
                .contentType(MediaType.APPLICATION_JSON));
        // then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));
    }
    @Test
    @DisplayName("Test get developer by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenExceptionIsThrown() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/developers/1")
                .contentType(MediaType.APPLICATION_JSON));
        // then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)));
    }

    @Test
    @DisplayName("Test soft delete by id functionality")
    public void givenId_whenSoftDelete_thenSuccessResponse() throws Exception {
        // given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity);
        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/" + entity.getId()).accept(MediaType.APPLICATION_JSON));
        // then
        DeveloperEntity obtainedDeveloper = developerRepository.findById(entity.getId()).orElse(null);
        Assertions.assertThat(obtainedDeveloper).isNotNull();
        Assertions.assertThat(obtainedDeveloper.getStatus()).isEqualTo(Status.DELETED);
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Test soft delete by incorrect id functionality")
    public void givenIncorrectId_whenSoftDelete_thenExceptionIsThrown() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/1").accept(MediaType.APPLICATION_JSON));
        // then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")));
    }

    @Test
    @DisplayName("Test hard delete by id functionality")
    public void givenId_whenHardDelete_thenSuccessResponse() throws Exception {
        // given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity);
        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/"+entity.getId()+"?isHard=true").accept(MediaType.APPLICATION_JSON));
        // then
        DeveloperEntity obtainedDeveloper = developerRepository.findById(entity.getId()).orElse(null);
        Assertions.assertThat(obtainedDeveloper).isNull();
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Test hard delete by incorrect id functionality")
    public void givenIncorrectId_whenHardDelete_thenExceptionIsThrown() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/1?isHard=true").accept(MediaType.APPLICATION_JSON));
        // then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")));
    }
}
