package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalErrorFilesetStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FinalErrorFilesetStudentPaginationRepository extends JpaRepository<FinalErrorFilesetStudentEntity, UUID>, JpaSpecificationExecutor<FinalErrorFilesetStudentEntity> {

}
