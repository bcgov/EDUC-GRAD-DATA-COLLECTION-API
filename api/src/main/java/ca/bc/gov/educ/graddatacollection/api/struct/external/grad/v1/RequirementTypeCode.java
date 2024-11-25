package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequirementTypeCode {

	private String reqTypeCode;
	private String label;
	private int displayOrder; 
	private String description;
	private Date effectiveDate; 
	private Date expiryDate;
	private String createUser;
	private String createDate;
	private String updateUser;
	private String updateDate;
}
