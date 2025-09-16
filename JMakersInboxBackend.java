package com.scb.loanOrigination.dto.makerInbox;

import java.util.List;

public class TransactionDto {
    private String transactionRef;
    private int loanId;
    private String assignedTo;
    private String applicant;
    private String createdAt;
    private String updatedAt;
    private String status;
    private List<FlagDto> flags;

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getApplicant() { return applicant; }
    public void setApplicant(String applicant) { this.applicant = applicant; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<FlagDto> getFlags() { return flags; }
    public void setFlags(List<FlagDto> flags) { this.flags = flags; }
}

package com.scb.loanOrigination.dto.makerInbox;

public class FlagDto {
    private String type;
    private String message;

    public FlagDto() {}
    public FlagDto(String type, String message) { this.type = type; this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

package com.scb.loanOrigination.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="LoanApplications")
public class LoanApplications {

    public enum LoanStatusEnum {
        Initiated,
        In_Progress,
        Rejected,
        Approved
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int loanId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private int loanTenure;

    @Column(nullable = false)
    private double interestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatusEnum status;

    @Column(nullable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable=false)
    private String user_id;

    // OneToMany to Documents (bidirectional)
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Documents> documents;

    // Many loans -> one customer. EAGER fetch for inbox convenience.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @JsonBackReference
    private Customers customer;

    // getters / setters
    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getLoanTenure() { return loanTenure; }
    public void setLoanTenure(int loanTenure) { this.loanTenure = loanTenure; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public LoanStatusEnum getStatus() { return status; }
    public void setStatus(LoanStatusEnum status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public List<Documents> getDocuments() { return documents; }
    public void setDocuments(List<Documents> documents) { this.documents = documents; }

    public Customers getCustomer() { return customer; }
    public void setCustomer(Customers customer) { this.customer = customer; }
}

package com.scb.loanOrigination.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name="Documents")
public class Documents {
    public enum DocumentStatusEnum {
        Flagged_For_ReUpload,
        Moved_To_Checker,
        Approved,
        Flagged_For_Data_ReEntry
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger documentId;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String entriesFilePath;

    @Column(nullable = false)
    private final LocalDateTime uploadedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatusEnum status;

    @Column(nullable = false)
    private Boolean flag = false;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private String user_id;

    // keep raw loan_id column for compatibility
    @Column(nullable = false)
    private int loan_id;

    // Many documents -> one loan (bidirectional). Use insertable=false, updatable=false
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", referencedColumnName = "loanId", insertable = false, updatable = false)
    @JsonBackReference
    private LoanApplications loanApplication;

    // getters and setters (only the ones used)
    public BigInteger getDocumentId() { return documentId; }
    public void setDocumentId(BigInteger documentId) { this.documentId = documentId; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getEntriesFilePath() { return entriesFilePath; }
    public void setEntriesFilePath(String entriesFilePath) { this.entriesFilePath = entriesFilePath; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }

    public DocumentStatusEnum getStatus() { return status; }
    public void setStatus(DocumentStatusEnum status) { this.status = status; }

    public Boolean getFlag() { return flag; }
    public void setFlag(Boolean flag) { this.flag = flag; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public int getLoan_id() { return loan_id; }
    public void setLoan_id(int loan_id) { this.loan_id = loan_id; }

    public LoanApplications getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplications loanApplication) { this.loanApplication = loanApplication; }
}


package com.scb.loanOrigination.repository;

import com.scb.loanOrigination.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {

    // fetch workflow along with loan, documents and customer in one query to avoid lazy init issues
    @Query("select distinct w from Workflow w " +
           "left join fetch w.loanApplication la " +
           "left join fetch la.documents d " +
           "left join fetch la.customer c")
    List<Workflow> findAllWithLoanDocsAndCustomer();
}


package com.scb.loanOrigination.controller;

import com.scb.loanOrigination.dto.makerInbox.TransactionDto;
import com.scb.loanOrigination.dto.makerInbox.FlagDto;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Users;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.entity.Documents;
import com.scb.loanOrigination.service.LoanApplicationsServiceImp;
import com.scb.loanOrigination.service.UsersServiceImp;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import com.scb.loanOrigination.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Date;

@RestController
@CrossOrigin(origins = "*")
public class MakerController {

    @Autowired
    private LoanApplicationsServiceImp loanService;

    @Autowired
    private UsersServiceImp userService;

    @Autowired
    private WorkflowServiceImp workflowService;

    @Autowired
    private WorkflowRepository workflowRepository;

    // ---------------- existing endpoints (unchanged) ---------------
    // (Paste your existing POST/GET methods here — keeping exactly what you already had)
    // For brevity I'm assuming the rest of your existing methods (getLoanRequestDetails, saveIDProofDetails, etc.)
    // remain below exactly as in your current file. Keep them as-is.

    // Example existing method (keep your real ones too)
    @RequestMapping(value="/getLoanRequestDetails/{loanId}",method= RequestMethod.GET)
    public LoanApplications getLoanRequestDetails(@PathVariable("loanId") int loanId){
        return loanService.getLoanRequestDetails(loanId);
    }

    // ... (keep all other methods you already have, unchanged) ...
    // ----------------------------------------------------------------

    // NEW: Maker's Inbox API
    @GetMapping("/maker/inbox")
    public List<TransactionDto> getMakerInbox() {
        // use repository method that fetches associations to avoid lazy errors
        List<Workflow> workflows = workflowRepository.findAllWithLoanDocsAndCustomer();

        List<TransactionDto> out = new ArrayList<>();
        for (Workflow wf : workflows) {
            LoanApplications loan = wf.getLoanApplication();

            TransactionDto dto = new TransactionDto();
            dto.setTransactionRef("TXN" + wf.getWorkflowId());
            dto.setLoanId(loan != null ? loan.getLoanId() : 0);
            dto.setAssignedTo(wf.getUser_id());

            // applicant name from Customers (firstName + lastName) if available
            String applicantName = wf.getUser_id();
            if (loan != null && loan.getCustomer() != null) {
                try {
                    String fn = loan.getCustomer().getFirstName() != null ? loan.getCustomer().getFirstName() : "";
                    String ln = loan.getCustomer().getLastName() != null ? loan.getCustomer().getLastName() : "";
                    applicantName = (fn + " " + ln).trim();
                } catch (Exception e) {
                    // fallback to user_id
                    applicantName = loan.getUser_id() != null ? loan.getUser_id() : wf.getUser_id();
                }
            } else if (loan != null && loan.getUser_id() != null) {
                applicantName = loan.getUser_id();
            }
            dto.setApplicant(applicantName);

            dto.setCreatedAt(loan != null && loan.getCreatedAt() != null ? loan.getCreatedAt().toString() : "");
            dto.setUpdatedAt(wf.getUpdatedAt() != null ? wf.getUpdatedAt().toString() : "");
            dto.setStatus(wf.getStatus() != null ? wf.getStatus().name() : "");

            // Build flags from Documents (counted) + append Workflow remarks (if any)
            List<FlagDto> flags = new ArrayList<>();

            if (loan != null && loan.getDocuments() != null) {
                List<FlagDto> docFlags = loan.getDocuments().stream()
                        .filter(doc -> doc.getFlag() != null && doc.getFlag())
                        .map(doc -> new FlagDto(doc.getDocumentName(), doc.getComment()))
                        .collect(Collectors.toList());
                flags.addAll(docFlags);
            }

            // Append workflow remarks as an extra flag entry (so UI can show it too)
            if (wf.getRemarks() != null && !wf.getRemarks().trim().isEmpty()) {
                flags.add(new FlagDto("Remarks", wf.getRemarks()));
            }

            dto.setFlags(flags);
            out.add(dto);
        }

        return out;
    }
}



INSERT INTO "Users" (user_id, password, roleName, createdAt)
VALUES ('user101', 'pass123', 'Maker', now());

INSERT INTO "Customers" (user_id, address, dob, gender, email, firstName, lastName, pan, aadhaar, mobileNumber)
VALUES ('user101', 'Some Address', now() - interval '30 years', 'M', 'rahul@example.com', 'Rahul', 'Kumar', 'ABCDE1234F', 123456789012, 9876543210);


INSERT INTO "LoanApplications" (amount, currency, loanTenure, interestRate, status, createdAt, user_id)
VALUES (500000, 'INR', 60, 7.5, 'In_Progress', now(), 'user101');


SELECT loanId FROM "LoanApplications" WHERE user_id = 'user101' ORDER BY createdAt DESC LIMIT 1;
-- Note the loanId value returned (e.g. 1)


INSERT INTO "Documents" (documentName, fileName, filePath, entriesFilePath, status, flag, comment, user_id, loan_id, uploadedAt)
VALUES ('ID_PROOF','aadhaar.pdf','/files/aadhaar.pdf','/entries/aadhaar.csv','Flagged_For_ReUpload', true, 'Aadhaar blurry', 'user101', :LOAN_ID, now());

INSERT INTO "Documents" (documentName, fileName, filePath, entriesFilePath, status, flag, comment, user_id, loan_id, uploadedAt)
VALUES ('INCOME_PROOF','salary.pdf','/files/salary.pdf','/entries/salary.csv','Flagged_For_ReUpload', true, 'Missing salary page', 'user101', :LOAN_ID, now());

INSERT INTO "Workflow" (stepName, status, createdAt, updatedAt, remarks, user_id, loan_id)
VALUES ('Maker','Pending', now(), now(), 'Please verify employment docs', 'user101', :LOAN_ID);


SELECT * FROM "Workflow";
SELECT * FROM "LoanApplications";
SELECT * FROM "Documents";
SELECT * FROM "Customers";
