MakerController
package com.scb.loanOrigination.controller;
import com.scb.loanOrigination.dto.*;
import com.scb.loanOrigination.dto.details.*;
import com.scb.loanOrigination.entity.Documents;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Users;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.MakerException;
import com.scb.loanOrigination.service.LoanApplicationsServiceImp;
import com.scb.loanOrigination.service.UsersServiceImp;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import org.hibernate.metamodel.model.domain.internal.PathHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class MakerController {
    @Autowired
    private LoanApplicationsServiceImp loanService;
    @Autowired
    private UsersServiceImp userService;
    @Autowired
    private WorkflowServiceImp workflowService;

    @RequestMapping(value="/getLoanRequestDetails/{loanId}",method= RequestMethod.GET)
    public LoanApplications getLoanRequestDetails(@PathVariable("loanId") long loanId) throws MakerException {
        return loanService.getLoanRequestDetails(loanId);
    }

    @RequestMapping(value="/getUserDetails/{userId}",method= RequestMethod.GET)
    public Users getUserDetails(@PathVariable("userId") String userId) throws MakerException{
        return userService.getUserDetails(userId);
    }

    @RequestMapping(value="/getApplicantName/{userId}",method= RequestMethod.GET)
    public String getApplicantName(@PathVariable("userId") String userId){
        return userService.getApplicantName(userId);
    }

    @RequestMapping(value="/getWorkflowDetails/{workflowId}",method= RequestMethod.GET)
    public Workflow getWorkflowDetails(@PathVariable("workflowId") long workflowId) throws MakerException{
        return workflowService.getWorkflowDetails(workflowId);
    }

    @RequestMapping(value="/saveIDProofDetails/",method= RequestMethod.POST)
    public String saveIDProofDetails(@RequestBody IDProofDetailsDTO idProofDetailsDTO) throws MakerException
    {
        return loanService.saveIDProofDetails(idProofDetailsDTO.getLoanId(), idProofDetailsDTO.getType(), idProofDetailsDTO.getIdNumber(),  idProofDetailsDTO.getName(), idProofDetailsDTO.getDob(), idProofDetailsDTO.getIssueDate(), idProofDetailsDTO.getExpiryDate(), idProofDetailsDTO.getIssuingAuthority());
    }

    @RequestMapping(value="/getIDProofDetails/{loanId}/{type}",method=RequestMethod.GET)
    public IDProofDetailsDTO getIDProofDetails(@PathVariable("loanId") long loanId, @PathVariable("type") String type) throws MakerException
    {
        return loanService.getIDProofDetails(loanId, type);
    }

    @RequestMapping(value="/saveAddressProofDetails/",method= RequestMethod.POST)
    public String saveAddressProofDetails(@RequestBody AddressProofDetailsDTO addressProofDetailsDTO) throws MakerException
    {
        return loanService.saveAddressProofDetails( addressProofDetailsDTO.getLoanId(),addressProofDetailsDTO.getType(),addressProofDetailsDTO.getLandLordName(),addressProofDetailsDTO.getTenantName(), addressProofDetailsDTO.getAddressLine1(), addressProofDetailsDTO.getAddressLine2(), addressProofDetailsDTO.getCity(),  addressProofDetailsDTO.getState(),  addressProofDetailsDTO.getPostalCode(),  addressProofDetailsDTO.getCountry(), addressProofDetailsDTO.getAgreementStartDate(), addressProofDetailsDTO.getAgreementEndDate());
    }
    @RequestMapping(value="/getAddressProofDetails/{loanId}/{type}",method=RequestMethod.GET)
    public AddressProofDetailsDTO getAddressProofDetails(@PathVariable("loanId") long loanId, @PathVariable("type") String type) throws MakerException
    {
        return loanService.getAddressProofDetails(loanId, type);
    }

    @RequestMapping(value="/saveIncomeProofDetails/",method= RequestMethod.POST)
    public String saveIncomeProofDetails(@RequestBody IncomeProofDetailsDTO incomeProofDetailsDTO) throws MakerException
    {
        return loanService.saveIncomeProofDetails( incomeProofDetailsDTO.getLoanId(),incomeProofDetailsDTO.getType(),incomeProofDetailsDTO.getEmployer(), incomeProofDetailsDTO.getSalaryMonth(), incomeProofDetailsDTO.getGrossIncome(), incomeProofDetailsDTO.getNetIncome(), incomeProofDetailsDTO.getBankName(), incomeProofDetailsDTO.getAccountNumber(),  incomeProofDetailsDTO.getStatementStart(), incomeProofDetailsDTO.getStatementEnd(),  incomeProofDetailsDTO.getAverageBalance());
    }

    @RequestMapping(value="/getIncomeProofDetails/{loanId}/{type}",method=RequestMethod.GET)
    public IncomeProofDetailsDTO getIncomeProofDetails(@PathVariable("loanId") long loanId, @PathVariable("type") String type) throws MakerException
    {
        return loanService.getIncomeProofDetails(loanId, type);
    }

    @RequestMapping(value="/saveEmploymentProofDetails/",method= RequestMethod.POST)
    public String saveEmploymentProofDetails(@RequestBody EmploymentProofDetailsDTO employmentProofDetailsDTO) throws MakerException
    {
        return loanService.saveEmploymentProofDetails(employmentProofDetailsDTO.getLoanId(),employmentProofDetailsDTO.getType(),  employmentProofDetailsDTO.getEmployer(),  employmentProofDetailsDTO.getDesignation(), employmentProofDetailsDTO.getJoiningDate(),  employmentProofDetailsDTO.getEmployeeID());
    }

    @RequestMapping(value="/getEmploymentProofDetails/{loanId}/{type}",method=RequestMethod.GET)
    public EmploymentProofDetailsDTO getEmploymentProofDetails(@PathVariable("loanId") long loanId, @PathVariable("type") String type) throws MakerException
    {
        return loanService.getEmploymentProofDetails(loanId, type);
    }

    @RequestMapping(value="/saveLoanFormDetails/",method= RequestMethod.POST)
    public String saveLoanFormDetails(@RequestBody LoanFormDetailsDTO loanFormDetailsDTO) throws MakerException
    {
        return loanService.saveLoanFormDetails( loanFormDetailsDTO.getLoanId(), loanFormDetailsDTO.getType(), loanFormDetailsDTO.getApplicantName(), loanFormDetailsDTO.getCountry(), loanFormDetailsDTO.getCurrency(), loanFormDetailsDTO.getLoanType(), loanFormDetailsDTO.getAmount(), loanFormDetailsDTO.getLoanTenure());
    }

    @RequestMapping(value="/getLoanFormDetails/{loanId}/{type}",method=RequestMethod.GET)
    public LoanFormDetailsDTO getLoanFormDetails(@PathVariable("loanId") long loanId, @PathVariable("type") String type) throws MakerException
    {
        return loanService.getLoanFormDetails(loanId, type);
    }

    @RequestMapping(value="/getProofDocument/{loanId}/{type}", method=RequestMethod.GET)
    public Documents getProofDocument(@PathVariable("loanId") long loanId, @PathVariable("type") String type) throws MakerException
    {
        return loanService.getProofDocument(loanId, type);
    }

    @RequestMapping(value="/flagDocument/",method= RequestMethod.POST)
    public String flagDocument(@RequestBody FlagDocumentDTO flagDocumentDTO) throws MakerException
    {
        return loanService.flagDocument(flagDocumentDTO.getLoanId(), flagDocumentDTO.getType(), flagDocumentDTO.getComment());
    }

    @RequestMapping(value="/flagWorkflowForReUpload/{workflowId}",method= RequestMethod.POST)
    public String flagWorkflowForReUpload(@PathVariable("workflowId") long workflowId)
    {
        return workflowService.flagWorkflowForReUpload(workflowId);
    }

    @RequestMapping(value="/moveWorkflowToChecker/{workflowId}",method= RequestMethod.POST)
    public String moveLoanRequestToChecker(@PathVariable("workflowId") long workflowId)
    {
        return workflowService.moveWorkflowToChecker(workflowId);
    }


    @RequestMapping(value="/getDocumentAsByteArray",method=RequestMethod.GET)
    public ResponseEntity<byte[]> getDocumentAsByteArray(@RequestParam String filePath) throws MakerException {
        byte[] documentBytes = loanService.getDocumentAsByteArray(filePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; file=\"" + filePath + "\"");
        headers.setContentLength(documentBytes.length);

        return new ResponseEntity<>(documentBytes, headers, HttpStatus.OK);
    }
    @GetMapping("/api/maker/makerInbox")
    public ResponseEntity<List<MakerInboxDTO>> getMakerInbox() {
        return ResponseEntity.ok(workflowService.getMakerInbox());
    }
}

MakerInboxDTO.java
package com.scb.loanOrigination.dto;

import com.scb.loanOrigination.entity.Workflow;

public class MakerInboxDTO {
    private Long workflowId;
    private Long loanId;
    private String userId;
    private String applicantName;
    private Workflow.WorkFlowStatusEnum status;
    private String remarks;
    private Long flagsCount;
    private String createdAt;
    private String updatedAt;

    public MakerInboxDTO(Long workflowId, Long loanId, String userId,
                         String applicantName, Workflow.WorkFlowStatusEnum status, String remarks,
                         Long flagsCount, String createdAt, String updatedAt) {
        this.workflowId = workflowId;
        this.loanId = loanId;
        this.userId = userId;
        this.applicantName = applicantName;
        this.status = status;
        this.remarks = remarks;
        this.flagsCount = flagsCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getWorkflowId() { return workflowId; }
    public Long getLoanId() { return loanId; }
    public String getUserId() { return userId; }
    public String getApplicantName() { return applicantName; }
    public Workflow.WorkFlowStatusEnum getStatus() { return status; }
    public String getRemarks() { return remarks; }
    public Long getFlagsCount() { return flagsCount; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}

WorkflowRespository.java
package com.scb.loanOrigination.repository;

import com.scb.loanOrigination.dto.MakerInboxDTO;
import com.scb.loanOrigination.entity.Workflow;
//import com.scb.loanOrigination.dto.MakerInboxProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    Workflow findByloanId(long loanId);
    Optional<Workflow> findByLoanApplication_LoanId(Long loanId);
    List<Workflow> findByUserId(String userId);
    List<Workflow> findByStatus(Workflow.WorkFlowStatusEnum status);
    //List<Workflow> findByStepName(Workflow.WorkflowStepNameEnum stepName);

//    @Query("""
//    SELECT w.workflowId AS workflowId,
//           w.loanId AS loanId,
//           w.userId AS userId,
//           CONCAT(c.firstName, ' ', c.lastName) AS applicantName,
//           w.status AS status,
//           w.remarks AS remarks,
//           COUNT(d) FILTER (WHERE d.flag = true) AS flagsCount,
//           CAST(w.createdAt AS string) AS createdAt,
//           CAST(w.updatedAt AS string) AS updatedAt
//    FROM Workflow w
//    JOIN LoanApplications l ON w.loanId = l.loanId
//    JOIN Customers c ON l.userId = c.userId
//    LEFT JOIN Documents d ON d.loanId = w.loanId
//    WHERE w.stepName = 'Maker'
//      AND w.status IN ('Moved_To_Maker','Flagged_For_ReUpload','Flagged_For_Data_ReEntry')
//    GROUP BY w.workflowId, w.loanId, w.userId, c.firstName, c.lastName,
//             w.status, w.remarks, w.createdAt, w.updatedAt
//    """)
//    List<MakerInboxProjection> getMakerInbox();
@Query("""
SELECT new com.scb.loanOrigination.dto.MakerInboxDTO(
       w.workflowId,
       w.loanId,
       w.userId,
       CONCAT(c.firstName, ' ', c.lastName),
       w.status,
       w.remarks,
       COUNT(d) FILTER (WHERE d.flag = true) AS flagsCount,
       CAST(w.createdAt AS string),
       CAST(w.updatedAt AS string)
)
FROM Workflow w
JOIN LoanApplications l ON w.loanId = l.loanId
JOIN Customers c ON l.userId = c.userId
LEFT JOIN Documents d ON d.loanId = w.loanId
WHERE w.stepName = 'Maker'
  AND w.status IN ('Moved_To_Maker','Flagged_For_Data_ReEntry')
GROUP BY w.workflowId, w.loanId, w.userId, c.firstName, c.lastName,
         w.status, w.remarks, w.createdAt, w.updatedAt
""")
List<MakerInboxDTO> getMakerInbox();
}


package com.scb.loanOrigination.service;


import com.scb.loanOrigination.dto.MakerInboxDTO;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.MakerException;

import java.util.List;


public interface IWorkflow {
    Workflow createInitial(Long loanId,String userId,String remarks);

    Workflow getByLoan(Long loanId);

    List<Workflow> listbyUser(String userId);
    public Workflow getWorkflowDetails(long workflowId) throws MakerException;

    public String flagWorkflowForReUpload(long workflowId);

    public String moveWorkflowToChecker(long workflowId);

    List<MakerInboxDTO> getMakerInbox();
    
}

package com.scb.loanOrigination.service;

import com.scb.loanOrigination.LoanOrigination;
import com.scb.loanOrigination.dto.MakerInboxDTO;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.AppException;
import com.scb.loanOrigination.exception.CheckerException;
import com.scb.loanOrigination.exception.MakerException;
//import com.scb.loanOrigination.dto.MakerInboxProjection;
import com.scb.loanOrigination.repository.LoanRepository;
import com.scb.loanOrigination.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkflowServiceImp implements IWorkflow
{
    @Autowired
    private WorkflowRepository workflowRepo;
    @Autowired
    private LoanRepository loanRepo;

    public WorkflowServiceImp(WorkflowRepository workflowRepo, LoanRepository loanRepo) {
        this.workflowRepo = workflowRepo;
        this.loanRepo = loanRepo;
    }

    public Workflow createInitial(Long loanId, String userId, String remarks) {
        LoanApplications loan = loanRepo.findById(loanId).orElseThrow(()-> AppException.notFound("Loan Not Found"));

        workflowRepo.findByLoanApplication_LoanId(loanId).ifPresent(wf ->{
            throw AppException.badRequest("Workflow already exists for loan" + loanId);
        });

        Workflow wf = new Workflow();
        wf.setLoanApplication(loan);
        wf.setUserId(userId);
        wf.setStepName(Workflow.WorkflowStepNameEnum.Maker);
        wf.setStatus(Workflow.WorkFlowStatusEnum.Moved_To_Maker);
        wf.setRemarks(remarks);
        wf.setUpdatedAt(LocalDateTime.now());

        return workflowRepo.save(wf);
    }

    public Workflow getByLoan(Long loanId) {
        return workflowRepo.findByLoanApplication_LoanId(loanId).orElseThrow(()->AppException.notFound("Workflow not found for loan" + loanId));
    }

    public List<Workflow> listbyUser(String userId) {
        return workflowRepo.findByUserId(userId);
    }

    public Workflow getWorkflowDetails(long workflowId) throws MakerException{
        if(workflowRepo.existsById(workflowId))
        {
            return workflowRepo.findById(workflowId).get();
        }
        else
        {
            throw new MakerException("No Workflow with workflow ID: "+workflowId+" found");
        }
    }

    public String flagWorkflowForReUpload(long workflowId)
    {
        Workflow workflow = getWorkflowDetails(workflowId);
        workflow.setStatus(Workflow.WorkFlowStatusEnum.Flagged_For_ReUpload);
        workflow.setStepName(Workflow.WorkflowStepNameEnum.Customer);
        workflowRepo.save(workflow);
        return "Loan request flagged";
    }

    public String moveWorkflowToChecker(long workflowId)
    {
        Workflow workflow = getWorkflowDetails(workflowId);
        workflow.setStatus(Workflow.WorkFlowStatusEnum.Moved_To_Checker);
        workflow.setStepName(Workflow.WorkflowStepNameEnum.Checker);
        workflowRepo.save(workflow);
        return "Loan request flagged";
    }

//    Checker APIs
    public String assignMaker(long loanId){
        Workflow wf = workflowRepo.findByloanId(loanId);
        wf.setStepName(Workflow.WorkflowStepNameEnum.Maker);
        wf.setStatus(Workflow.WorkFlowStatusEnum.Flagged_For_Data_ReEntry);
        workflowRepo.save(wf);
        return "WorkItem Assigned to Maker successfully";
    }

    public String approveLoanRequest(long loanId){
        Workflow wf = workflowRepo.findByloanId(loanId);
        wf.setStatus(Workflow.WorkFlowStatusEnum.Approved);
        wf.setStepName(Workflow.WorkflowStepNameEnum.Approval);

        Optional<LoanApplications> optionLoanElem = loanRepo.findById(loanId);
        if (optionLoanElem.isEmpty()) {
            throw new CheckerException("Requested Loan doesn't exist");
        }
        LoanApplications loanElem = optionLoanElem.get();
        loanElem.setStatus(LoanApplications.LoanStatusEnum.Approved);
        workflowRepo.save(wf);
        loanRepo.save(loanElem);
        return "Loan approved successfully";
    }

    @Override
    public List<MakerInboxDTO> getMakerInbox() {
        return workflowRepo.getMakerInbox();
    }

}



package com.scb.loanOrigination.service;

import com.scb.loanOrigination.dto.MakerInboxDTO;
import com.scb.loanOrigination.repository.WorkflowRepository;
import com.scb.loanOrigination.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class WorkflowServiceImpTest {

    @Mock
    private WorkflowRepository workflowRepo;

    @Mock
    private LoanRepository loanRepo;

    @InjectMocks
    private WorkflowServiceImp workflowService;

    WorkflowServiceImpTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMakerInbox() {
        // Mocking
        MakerInboxDTO dto = new MakerInboxDTO(
                1L, 101L, "user001", "John Doe",
                null, "Test Remark", 2L,
                "2025-09-21", "2025-09-22"
        );
        when(workflowRepo.getMakerInbox()).thenReturn(List.of(dto));

        // Call service
        List<MakerInboxDTO> result = workflowService.getMakerInbox();

        // Verify
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getApplicantName());
        assertEquals("Test Remark", result.get(0).getRemarks());
    }
}

package com.scb.loanOrigination.controller;

import com.scb.loanOrigination.dto.MakerInboxDTO;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MakerController.class)
class MakerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowServiceImp workflowService;

    @Test
    void testGetMakerInbox() throws Exception {
        MakerInboxDTO dto = new MakerInboxDTO(
                1L, 101L, "user001", "John Doe",
                null, "Remark", 1L,
                "2025-09-21", "2025-09-22"
        );

        when(workflowService.getMakerInbox()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/maker/makerInbox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicantName").value("John Doe"))
                .andExpect(jsonPath("$[0].remarks").value("Remark"));
    }
}
