package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentLightEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourseStudentMapperTest {

    private final CourseStudentMapper mapper = CourseStudentMapper.mapper;

    @Test
    void toCourseStudent_whenNumberOfCreditsIsNull_shouldMapToZero() {
        // Given
        IncomingFilesetEntity incomingFileset = new IncomingFilesetEntity();
        incomingFileset.setIncomingFilesetID(UUID.randomUUID());

        CourseStudentEntity entity = new CourseStudentEntity();
        entity.setCourseStudentID(UUID.randomUUID());
        entity.setIncomingFileset(incomingFileset);
        entity.setPen("123456789");
        entity.setCourseCode("MATH");
        entity.setCourseLevel("10");
        entity.setNumberOfCredits(null);
        entity.setCreateUser("TEST");
        entity.setUpdateUser("TEST");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());

        // When
        CourseStudent result = mapper.toCourseStudent(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumberOfCredits()).isEqualTo("0");
    }

    @Test
    void toCourseStudent_whenNumberOfCreditsHasValue_shouldPreserveValue() {
        // Given
        IncomingFilesetEntity incomingFileset = new IncomingFilesetEntity();
        incomingFileset.setIncomingFilesetID(UUID.randomUUID());

        CourseStudentEntity entity = new CourseStudentEntity();
        entity.setCourseStudentID(UUID.randomUUID());
        entity.setIncomingFileset(incomingFileset);
        entity.setPen("123456789");
        entity.setCourseCode("MATH");
        entity.setCourseLevel("10");
        entity.setNumberOfCredits("4");
        entity.setCreateUser("TEST");
        entity.setUpdateUser("TEST");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());

        // When
        CourseStudent result = mapper.toCourseStudent(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumberOfCredits()).isEqualTo("4");
    }

    @Test
    void toCourseStudent_fromLightEntity_whenNumberOfCreditsIsNull_shouldMapToZero() {
        // Given
        IncomingFilesetEntity incomingFileset = new IncomingFilesetEntity();
        incomingFileset.setIncomingFilesetID(UUID.randomUUID());

        CourseStudentLightEntity entity = new CourseStudentLightEntity();
        entity.setCourseStudentID(UUID.randomUUID());
        entity.setIncomingFileset(incomingFileset);
        entity.setPen("123456789");
        entity.setCourseCode("MATH");
        entity.setCourseLevel("10");
        entity.setNumberOfCredits(null);
        entity.setCreateUser("TEST");
        entity.setUpdateUser("TEST");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());

        // When
        CourseStudent result = mapper.toCourseStudent(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumberOfCredits()).isEqualTo("0");
    }

    @Test
    void toCourseStudent_fromLightEntity_whenNumberOfCreditsHasValue_shouldPreserveValue() {
        // Given
        IncomingFilesetEntity incomingFileset = new IncomingFilesetEntity();
        incomingFileset.setIncomingFilesetID(UUID.randomUUID());

        CourseStudentLightEntity entity = new CourseStudentLightEntity();
        entity.setCourseStudentID(UUID.randomUUID());
        entity.setIncomingFileset(incomingFileset);
        entity.setPen("123456789");
        entity.setCourseCode("MATH");
        entity.setCourseLevel("10");
        entity.setNumberOfCredits("3");
        entity.setCreateUser("TEST");
        entity.setUpdateUser("TEST");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());

        // When
        CourseStudent result = mapper.toCourseStudent(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumberOfCredits()).isEqualTo("3");
    }

    @Test
    void toCourseStudent_shouldMapIncomingFilesetIDCorrectly() {
        // Given
        UUID expectedFilesetId = UUID.randomUUID();
        IncomingFilesetEntity incomingFileset = new IncomingFilesetEntity();
        incomingFileset.setIncomingFilesetID(expectedFilesetId);

        CourseStudentEntity entity = new CourseStudentEntity();
        entity.setCourseStudentID(UUID.randomUUID());
        entity.setIncomingFileset(incomingFileset);
        entity.setPen("123456789");
        entity.setNumberOfCredits("4");
        entity.setCreateUser("TEST");
        entity.setUpdateUser("TEST");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());

        // When
        CourseStudent result = mapper.toCourseStudent(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIncomingFilesetID()).isEqualTo(expectedFilesetId.toString());
    }
}

