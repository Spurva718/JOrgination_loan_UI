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


package com.scb.loanOrigination.controller;
import com.scb.loanOrigination.dto.makerInbox.TransactionDto;
import com.scb.loanOrigination.dto.makerInbox.FlagDto;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Users;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.repository.WorkflowRepository;
import com.scb.loanOrigination.service.LoanApplicationsServiceImp;
import com.scb.loanOrigination.service.UsersServiceImp;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.*;

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

    @RequestMapping(value="/getLoanRequestDetails/{loanId}",method= RequestMethod.GET)
    public LoanApplications getLoanRequestDetails(@PathVariable("loanId") int loanId){
        return loanService.getLoanRequestDetails(loanId);
    }

    @RequestMapping(value="/getUserDetails/{userId}",method= RequestMethod.GET)
    public Users getUserDetails(@PathVariable("userId") String userId){
        return userService.getUserDetails(userId);
    }

    @RequestMapping(value="/getWorkflowDetails/{workflowId}",method= RequestMethod.GET)
    public Workflow getWorkflowDetails(@PathVariable("workflowId") int workflowId){
        return workflowService.getWorkflowDetails(workflowId);
    }

    @RequestMapping(value="/saveIDProofDetails/{loanId}/{type}/{IdNumber}/{name}/{dob}/{issueDate}/{expiryDate}/{issuingAuthority}",method= RequestMethod.POST)
    public String saveIDProofDetails(@PathVariable("loanId") int loanId, @PathVariable("type") String type, @PathVariable("IdNumber") BigInteger IdNumber, @PathVariable("name") String name, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date dob, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date issueDate, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date expiryDate, @PathVariable("issuingAuthority") String issuingAuthority)
    {
        return loanService.saveIDProofDetails(loanId, type, IdNumber,  name, dob, issueDate, expiryDate, issuingAuthority);
    }

    @RequestMapping(value="/saveAddressProofDetails/{loanId}/{type}/{landLordName}/{tenantName}/{addressLine1}/{addressLine2}/{city}/{state}/{postalCode}/{country}/{agreementStartDate}/{agreementEndDate}",method= RequestMethod.POST)
    public String saveAddressProofDetails(@PathVariable("loanId") int loanId,@PathVariable("type") String type, @PathVariable("landLordName") String landLordName, @PathVariable("tenantName") String tenantName, @PathVariable("addressLine1")String addressLine1, @PathVariable("addressLine2")String addressLine2, @PathVariable("city") String city, @PathVariable("state") String state, @PathVariable("postalCode") int postalCode, @PathVariable("country") String country, @PathVariable("agreementStartDate") Date agreementStartDate, @PathVariable("agreementEndDate") Date agreementEndDate)
    {
        return loanService.saveAddressProofDetails( loanId,type,landLordName,tenantName, addressLine1, addressLine2, city,  state,  postalCode,  country, agreementStartDate, agreementEndDate);
    }

    @RequestMapping(value="/saveIncomeProofDetails/{loanId}/{type}/{employer}/{salaryMonth}/{grossIncome}/{netIncome}/{bankName}/{accountNumber}/{statementStart}/{statementEnd}/{averageBalance}",method= RequestMethod.POST)
    public String saveIncomeProofDetails(@PathVariable("loanId") int loanId,@PathVariable("type") String type, @PathVariable("employer") String employer, @PathVariable("salaryMonth") String salaryMonth, @PathVariable("grossIncome")int grossIncome, @PathVariable("netIncome")int netIncome, @PathVariable("bankName") String bankName, @PathVariable("accountNumber") int accountNumber, @PathVariable("statementStart") Date statementStart, @PathVariable("statementEnd") Date statementEnd, @PathVariable("averageBalance") double averageBalance)
    {
        return loanService.saveIncomeProofDetails(  loanId,type, employer, salaryMonth, grossIncome, netIncome, bankName, accountNumber,  statementStart, statementEnd,  averageBalance);
    }

    @RequestMapping(value="/saveEmploymentProofDetails/{loanId}/{type}/{employer}/{designation}/{joiningDate}/{employeeID}",method= RequestMethod.POST)
    public String saveEmploymentProofDetails(@PathVariable("loanId") int loanId,@PathVariable("type") String type, @PathVariable("employer") String employer, @PathVariable("designation") String designation,@PathVariable("joiningDate") Date joiningDate, @PathVariable("employeeID") String employeeID)
    {
        return loanService.saveEmploymentProofDetails(loanId,type,  employer,  designation, joiningDate,  employeeID);
    }

    @RequestMapping(value="/saveLoanFormDetails/{loanId}/{type}/{applicantName}/{country}/{currency}/{loanType}/{amount}/{loanTenure}",method= RequestMethod.POST)
    public String saveLoanFormDetails(@PathVariable("loanId") int loanId,@PathVariable("type") String type, @PathVariable("applicantName") String applicantName, @PathVariable("country") String country,@PathVariable("currency") String currency, @PathVariable("loanType") String loanType, @PathVariable("amount") int amount, @PathVariable("loanTenure") int loanTenure)
    {
        return loanService.saveLoanFormDetails( loanId, type,  applicantName, country, currency, loanType, amount, loanTenure);
    }

    @RequestMapping(value="/flagDocument/{loanId}/{type}",method= RequestMethod.POST)
    public String flagDocument(@PathVariable("loanId") int loanId, @PathVariable("type") String type)
    {
        return loanService.flagDocument(loanId, type);
    }

    @RequestMapping(value="/flagLoanRequest/{loanId}",method= RequestMethod.POST)
    public String flagLoanRequest(@PathVariable("loanId") int loanId)
    {
        return loanService.flagLoanRequest(loanId);
    }

    @RequestMapping(value="/approveLoanRequest/{loanId}",method= RequestMethod.POST)
    public String approveLoanRequest(@PathVariable("loanId") int loanId)
    {
        return loanService.approveLoanRequest(loanId);
    }
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


Changed the contains 
package com.scb.loanOrigination.controller;

import com.scb.loanOrigination.dto.makerInbox.TransactionDto;
import com.scb.loanOrigination.dto.makerInbox.FlagDto;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Users;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.entity.Documents;
import com.scb.loanOrigination.repository.WorkflowRepository;
import com.scb.loanOrigination.service.LoanApplicationsServiceImp;
import com.scb.loanOrigination.service.UsersServiceImp;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class MakerController {

    private static final Logger log = LoggerFactory.getLogger(MakerController.class);

    @Autowired
    private LoanApplicationsServiceImp loanService;

    @Autowired
    private UsersServiceImp userService;

    @Autowired
    private WorkflowServiceImp workflowService;

    @Autowired
    private WorkflowRepository workflowRepository;

    // ---------------- existing endpoints (kept as-is) ---------------

    @RequestMapping(value="/getLoanRequestDetails/{loanId}",method= RequestMethod.GET)
    public LoanApplications getLoanRequestDetails(@PathVariable("loanId") int loanId){
        return loanService.getLoanRequestDetails(loanId);
    }

    @RequestMapping(value="/getUserDetails/{userId}",method= RequestMethod.GET)
    public Users getUserDetails(@PathVariable("userId") String userId){
        return userService.getUserDetails(userId);
    }

    @RequestMapping(value="/getWorkflowDetails/{workflowId}",method= RequestMethod.GET)
    public Workflow getWorkflowDetails(@PathVariable("workflowId") int workflowId){
        return workflowService.getWorkflowDetails(workflowId);
    }

    @RequestMapping(value="/saveIDProofDetails/{loanId}/{type}/{IdNumber}/{name}/{dob}/{issueDate}/{expiryDate}/{issuingAuthority}",method= RequestMethod.POST)
    public String saveIDProofDetails(@PathVariable("loanId") int loanId, @PathVariable("type") String type, @PathVariable("IdNumber") BigInteger IdNumber, @PathVariable("name") String name, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date dob, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date issueDate, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date expiryDate, @PathVariable("issuingAuthority") String issuingAuthority)
    {
        return loanService.saveIDProofDetails(loanId, type, IdNumber,  name, dob, issueDate, expiryDate, issuingAuthority);
    }

    @RequestMapping(value="/saveAddressProofDetails/{loanId}/{type}/{landLordName}/{tenantName}/{addressLine1}/{addressLine2}/{city}/{state}/{postalCode}/{country}/{agreementStartDate}/{agreementEndDate}",method= RequestMethod.POST)
    public String saveAddressProofDetails(@PathVariable("loanId") int loanId,@PathVariable("type") String type, @PathVariable("landLordName") String landLordName, @PathVariable("tenantName") String tenantName, @PathVariable("addressLine1")String addressLine1, @PathVariable("addressLine2")String addressLine2, @PathVariable("city") String city, @PathVariable("state") String state, @PathVariable("postalCode") int postalCode, @PathVariable("country") String country, @PathVariable("agreementStartDate") Date agreementStartDate, @PathVariable("agreementEndDate") Date agreementEndDate)
    {
        return loanService.saveAddressProofDetails( loanId,type,landLordName,tenantName, addressLine1, addressLine2, city,  state,  postalCode,  country, agreementStartDate, agreementEndDate);
    }

    @RequestMapping(value="/saveIncomeProofDetails/{loanId}/{type}/{employer}/{salaryMonth}/{grossIncome}/{netIncome}/{bankName}/{accountNumber}/{statementStart}/{statementEnd}/{averageBalance}",method= RequestMethod.POST)
    public String saveIncomeProofDetails(@PathVariable("loanId") int loanId,@PathVariable("type") String type, @PathVariable("employer") String employer, @PathVariable("salaryMonth") String salaryMonth, @PathVariable("grossIncome")int grossIncome, @PathVariable("netIncome")int netIncome, @PathVariable("bankName") String bankName, @PathVariable("accountNumber") int accountNumber, @PathVariable("statementStart") Date statementStart, @PathVariable("statementEnd") Date statementEnd, @PathVariable("averageBalance") double averageBalance)
    {
        return loanService.saveIncomeProofDetails(  loanId,type, employer, salaryMonth, grossIncome, netIncome, bankName, accountNumber,  statementStart, statementEnd,  averageBalance);
    }

    @RequestMapping(value="/saveEmploymentProofDetails/{loanId}/{type}/{employer}/{designation}/{joiningDate}/{employeeID}",method= RequestMethod.POST)
    public String saveEmploymentProofDetails(@PathVariable("loanId") int loanId,@PathVariable("type") String type, @PathVariable("employer") String employer, @PathVariable("designation") String designation,@PathVariable("joiningDate") Date joiningDate, @PathVariable("employeeID") String employeeID)
    {
        return loanService.saveEmploymentProofDetails(loanId,type,  employer,  designation, joiningDate,  employeeID);
    }

    @RequestMapping(value="/saveLoanFormDetails/{loanId}/{type}/{applicantName}/{country}/{currency}/{loanType}/{amount}/{loanTenure}",method= RequestMethod.POST)
    public String saveLoanFormDetails(@PathVariable("loanId") int loanId,@PathVariable("type") String type, @PathVariable("applicantName") String applicantName, @PathVariable("country") String country,@PathVariable("currency") String currency, @PathVariable("loanType") String loanType, @PathVariable("amount") int amount, @PathVariable("loanTenure") int loanTenure)
    {
        return loanService.saveLoanFormDetails( loanId, type,  applicantName, country, currency, loanType, amount, loanTenure);
    }

    @RequestMapping(value="/flagDocument/{loanId}/{type}",method= RequestMethod.POST)
    public String flagDocument(@PathVariable("loanId") int loanId, @PathVariable("type") String type)
    {
        return loanService.flagDocument(loanId, type);
    }

    @RequestMapping(value="/flagLoanRequest/{loanId}",method= RequestMethod.POST)
    public String flagLoanRequest(@PathVariable("loanId") int loanId)
    {
        return loanService.flagLoanRequest(loanId);
    }

    @RequestMapping(value="/approveLoanRequest/{loanId}",method= RequestMethod.POST)
    public String approveLoanRequest(@PathVariable("loanId") int loanId)
    {
        return loanService.approveLoanRequest(loanId);
    }

    // ---------------- new endpoint: Maker's Inbox -------------------
    @GetMapping("/maker/inbox")
    public List<TransactionDto> getMakerInbox() {
        // Prefer repository method with join fetch to avoid lazy-init; fallback to findAll() if needed
        List<Workflow> workflows;
        try {
            workflows = workflowRepository.findAllWithLoanDocsAndCustomer();
        } catch (Throwable t) {
            log.warn("findAllWithLoanDocsAndCustomer() not available or failed; falling back to findAll(). Reason: {}", t.toString());
            workflows = workflowRepository.findAll();
        }

        List<TransactionDto> out = new ArrayList<>();
        for (Workflow wf : workflows) {
            LoanApplications loan = wf.getLoanApplication();

            TransactionDto dto = new TransactionDto();
            dto.setTransactionRef("TXN" + wf.getWorkflowId());
            dto.setLoanId(loan != null ? loan.getLoanId() : 0);
            dto.setAssignedTo(wf.getUser_id());

            // applicant name from Customers (firstName + lastName) if available
            String applicantName = wf.getUser_id();
            if (loan != null) {
                try {
                    if (loan.getCustomer() != null) {
                        String fn = loan.getCustomer().getFirstName() != null ? loan.getCustomer().getFirstName() : "";
                        String ln = loan.getCustomer().getLastName() != null ? loan.getCustomer().getLastName() : "";
                        String full = (fn + " " + ln).trim();
                        if (!full.isEmpty()) applicantName = full;
                        else if (loan.getUser_id() != null) applicantName = loan.getUser_id();
                    } else if (loan.getUser_id() != null) {
                        applicantName = loan.getUser_id();
                    }
                } catch (Exception e) {
                    log.warn("Error reading customer name for loan {}, falling back to user_id. Reason: {}", loan.getLoanId(), e.toString());
                    applicantName = loan.getUser_id() != null ? loan.getUser_id() : wf.getUser_id();
                }
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



package com.scb.loanOrigination.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="Workflow")
public class Workflow {

    public enum WorkflowStepNameEnum { Maker, Checker, Approval }
    public enum WorkFlowStatusEnum {
        Pending,
        Flagged_For_ReUpload,
        Moved_To_Checker,
        Approved,
        Flagged_For_Data_ReEntry
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int workflowId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStepNameEnum stepName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkFlowStatusEnum status;

    @Column(nullable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String remarks;

    private String user_id;

    @OneToOne
    @JoinColumn(name="loan_id")
    @JsonBackReference
    private LoanApplications loanApplication;

    // --- explicit getters / setters ---

    public int getWorkflowId() { return workflowId; }
    public void setWorkflowId(int workflowId) { this.workflowId = workflowId; }

    public WorkflowStepNameEnum getStepName() { return stepName; }
    public void setStepName(WorkflowStepNameEnum stepName) { this.stepName = stepName; }

    public WorkFlowStatusEnum getStatus() { return status; }
    public void setStatus(WorkFlowStatusEnum status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public LoanApplications getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplications loanApplication) { this.loanApplication = loanApplication; }
}


package com.scb.loanOrigination.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Table(name="Customers")
public class Customers {

    @Id
    private String user_id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Date dob;

    @Column(nullable = false)
    private char gender;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String pan;

    @Column(nullable = false)
    private BigInteger aadhaar;

    @Column(nullable = false)
    private BigInteger mobileNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private Users user;

    // --- explicit getters / setters ---

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public char getGender() { return gender; }
    public void setGender(char gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public BigInteger getAadhaar() { return aadhaar; }
    public void setAadhaar(BigInteger aadhaar) { this.aadhaar = aadhaar; }

    public BigInteger getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(BigInteger mobileNumber) { this.mobileNumber = mobileNumber; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
}


2025-09-16T15:12:49.990+05:30  INFO 17660 --- [           main] c.scb.loanOrigination.LoanOrigination    : Starting LoanOrigination using Java 17.0.8 with PID 17660 (C:\Users\2030304\Repo_Capstone_Project\99999-grad-elbrus-loan-origination-repo\Backend\target\classes started by 2030304 in C:\Users\2030304\Repo_Capstone_Project\99999-grad-elbrus-loan-origination-repo\Backend)
2025-09-16T15:12:49.996+05:30  INFO 17660 --- [           main] c.scb.loanOrigination.LoanOrigination    : No active profile set, falling back to 1 default profile: "default"
2025-09-16T15:12:51.346+05:30  INFO 17660 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-09-16T15:12:51.442+05:30  INFO 17660 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 82 ms. Found 3 JPA repository interfaces.
2025-09-16T15:12:52.384+05:30  INFO 17660 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-09-16T15:12:52.401+05:30  INFO 17660 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-09-16T15:12:52.402+05:30  INFO 17660 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.20]
2025-09-16T15:12:52.506+05:30  INFO 17660 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-09-16T15:12:52.520+05:30  INFO 17660 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2444 ms
2025-09-16T15:12:52.784+05:30  INFO 17660 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-09-16T15:12:52.895+05:30  INFO 17660 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.4.Final
2025-09-16T15:12:52.956+05:30  INFO 17660 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-09-16T15:12:53.305+05:30  INFO 17660 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-09-16T15:12:53.340+05:30  INFO 17660 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-09-16T15:12:53.496+05:30  INFO 17660 --- [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@624b3544
2025-09-16T15:12:53.499+05:30  INFO 17660 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-09-16T15:12:55.126+05:30  INFO 17660 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: alter table if exists customers drop constraint if exists FKrh1g1a20omjmn6kurd35o3eit
2025-09-16T15:12:55.153+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-16T15:12:55.153+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "fkrh1g1a20omjmn6kurd35o3eit" of relation "customers" does not exist, skipping
Hibernate: alter table if exists documents drop constraint if exists FK5po5skiro6gtj5kv771yjj1fg
2025-09-16T15:12:55.154+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-16T15:12:55.154+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : relation "documents" does not exist, skipping
Hibernate: alter table if exists loan_applications drop constraint if exists FKmkoa5awuujoadi1bvfvkl05ee
2025-09-16T15:12:55.155+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-16T15:12:55.156+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : relation "loan_applications" does not exist, skipping
Hibernate: alter table if exists workflow drop constraint if exists FK7kht9sdfdufaybqs13uul5f89
2025-09-16T15:12:55.157+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-16T15:12:55.157+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : relation "workflow" does not exist, skipping
Hibernate: drop table if exists customers cascade
Hibernate: drop table if exists documents cascade
2025-09-16T15:12:55.168+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-16T15:12:55.169+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "documents" does not exist, skipping
Hibernate: drop table if exists loan_applications cascade
2025-09-16T15:12:55.170+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-16T15:12:55.170+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "loan_applications" does not exist, skipping
Hibernate: drop table if exists users cascade
2025-09-16T15:12:55.171+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-16T15:12:55.171+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "users" does not exist, skipping
Hibernate: drop table if exists workflow cascade
2025-09-16T15:12:55.172+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-16T15:12:55.172+05:30  WARN 17660 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "workflow" does not exist, skipping
Hibernate: create table customers (aadhaar numeric(38,0) not null, gender char(1) not null, mobile_number numeric(38,0) not null, dob timestamp(6) not null, address varchar(255) not null, email varchar(255) not null, first_name varchar(255) not null, last_name varchar(255) not null, pan varchar(255) not null, user_id varchar(255) not null, primary key (user_id))
2025-09-16T15:12:55.200+05:30 ERROR 17660 --- [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.MappingException: Error creating SQL 'create' commands for table 'documents' [illegal identity column type]
2025-09-16T15:12:55.201+05:30  WARN 17660 --- [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.MappingException: Error creating SQL 'create' commands for table 'documents' [illegal identity column type]
2025-09-16T15:12:55.202+05:30  INFO 17660 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2025-09-16T15:12:55.212+05:30  INFO 17660 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2025-09-16T15:12:55.216+05:30  INFO 17660 --- [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2025-09-16T15:12:55.242+05:30  INFO 17660 --- [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2025-09-16T15:12:55.278+05:30 ERROR 17660 --- [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.MappingException: Error creating SQL 'create' commands for table 'documents' [illegal identity column type]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1786) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:600) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:326) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:324) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.context.support.AbstractApplicationContext.getBean(AbstractApplicationContext.java:1234) ~[spring-context-6.1.6.jar:6.1.6]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:952) ~[spring-context-6.1.6.jar:6.1.6]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:624) ~[spring-context-6.1.6.jar:6.1.6]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:334) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1354) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1343) ~[spring-boot-3.2.5.jar:3.2.5]
	at com.scb.loanOrigination.LoanOrigination.main(LoanOrigination.java:9) ~[classes/:na]
Caused by: jakarta.persistence.PersistenceException: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.MappingException: Error creating SQL 'create' commands for table 'documents' [illegal identity column type]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:421) ~[spring-orm-6.1.6.jar:6.1.6]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.afterPropertiesSet(AbstractEntityManagerFactoryBean.java:396) ~[spring-orm-6.1.6.jar:6.1.6]
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.afterPropertiesSet(LocalContainerEntityManagerFactoryBean.java:366) ~[spring-orm-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1833) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1782) ~[spring-beans-6.1.6.jar:6.1.6]
	... 16 common frames omitted
Caused by: org.hibernate.MappingException: Error creating SQL 'create' commands for table 'documents' [illegal identity column type]
	at org.hibernate.tool.schema.internal.StandardTableExporter.getSqlCreateStrings(StandardTableExporter.java:121) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.StandardTableExporter.getSqlCreateStrings(StandardTableExporter.java:41) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.SchemaCreatorImpl.createTables(SchemaCreatorImpl.java:421) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.SchemaCreatorImpl.createSequencesTablesConstraints(SchemaCreatorImpl.java:340) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.SchemaCreatorImpl.createFromMetadata(SchemaCreatorImpl.java:239) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.SchemaCreatorImpl.performCreation(SchemaCreatorImpl.java:172) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.SchemaCreatorImpl.doCreation(SchemaCreatorImpl.java:142) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.SchemaCreatorImpl.doCreation(SchemaCreatorImpl.java:118) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.performDatabaseAction(SchemaManagementToolCoordinator.java:256) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.lambda$process$5(SchemaManagementToolCoordinator.java:145) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at java.base/java.util.HashMap.forEach(HashMap.java:1421) ~[na:na]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.process(SchemaManagementToolCoordinator.java:142) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.boot.internal.SessionFactoryObserverForSchemaExport.sessionFactoryCreated(SessionFactoryObserverForSchemaExport.java:37) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.SessionFactoryObserverChain.sessionFactoryCreated(SessionFactoryObserverChain.java:35) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.SessionFactoryImpl.<init>(SessionFactoryImpl.java:315) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.boot.internal.SessionFactoryBuilderImpl.build(SessionFactoryBuilderImpl.java:450) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.build(EntityManagerFactoryBuilderImpl.java:1507) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider.createContainerEntityManagerFactory(SpringHibernateJpaPersistenceProvider.java:75) ~[spring-orm-6.1.6.jar:6.1.6]
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.createNativeEntityManagerFactory(LocalContainerEntityManagerFactoryBean.java:390) ~[spring-orm-6.1.6.jar:6.1.6]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:409) ~[spring-orm-6.1.6.jar:6.1.6]
	... 20 common frames omitted
Caused by: org.hibernate.MappingException: illegal identity column type
	at org.hibernate.dialect.identity.PostgreSQLIdentityColumnSupport.getIdentityColumnString(PostgreSQLIdentityColumnSupport.java:39) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.ColumnDefinitions.appendColumnDefinition(ColumnDefinitions.java:177) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.ColumnDefinitions.appendColumn(ColumnDefinitions.java:105) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.tool.schema.internal.StandardTableExporter.getSqlCreateStrings(StandardTableExporter.java:84) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	... 39 common frames omitted


Process finished with exit code 1

package com.scb.loanOrigination.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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
    private Long documentId;   // changed from BigInteger to Long

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

    // getters and setters
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

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

