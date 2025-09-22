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

 @GetMapping("/api/maker/makerInbox")
    public ResponseEntity<List<MakerInboxDTO>> getMakerInbox() {
        return ResponseEntity.ok(workflowService.getMakerInbox());
    }
}

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
