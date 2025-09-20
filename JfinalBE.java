package com.scb.loanOrigination.controller;

import com.scb.loanOrigination.entity.Documents;
import com.scb.loanOrigination.exception.AppException;
import com.scb.loanOrigination.service.DocumentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentServiceImpl documentService;

    public DocumentController(DocumentServiceImpl documentService) {
        this.documentService = documentService;
    }


    @Operation(
            summary = "Upload one or more supporting documents for a loan (auto-creates blank entries JSON with DTO keys)"
    )
    @PostMapping(value = "/loans/{loanId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Documents>> upload(
            @PathVariable Long loanId,
            @RequestParam String userId,
            @RequestParam String documentName,
            @RequestParam(required = false) String comment,

            @RequestParam(defaultValue = "false") boolean replaceIfExists,

            @Parameter(description = "Files to upload",
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart("files") MultipartFile[] files
    ) throws Exception {
        if (files == null || files.length == 0) {
            throw AppException.badRequest("No files received in field 'files'.");
        }
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) {
                throw AppException.badRequest("One of the files is empty. Choose a real file.");
            }
        }

        boolean createEntries = true;
        String entriesType = null;
        String entriesBaseName = null;
        List<Documents> result = documentService.upload(
                loanId, userId, documentName, comment, files,
                replaceIfExists, createEntries, entriesType, entriesBaseName
        );
        return ResponseEntity.ok(result);
    }


    @Operation(summary = "List all documents for a loan")
    @GetMapping("/loans/{loanId}/documents")
    public ResponseEntity<List<Documents>> listByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(documentService.listByLoan(loanId));
    }

    @Operation(summary = "Download a document by id")
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long documentId) {
        Resource r = documentService.downloadAsResource(documentId);
        Documents d = documentService.getById(documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + d.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(r);
    }

    @Operation(summary = "Get a document by id")
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<Documents> getById(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getById(documentId));
    }
}

package com.scb.loanOrigination.controller;


import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")

public class WorkflowController {

    private final WorkflowServiceImp workflowService;

    public WorkflowController(WorkflowServiceImp workflowService) {
        this.workflowService = workflowService;
    }

    @Operation(summary = "Get Workflow by loanId")
    @GetMapping("/by-loan/{loanId}")
    public ResponseEntity<Workflow> getByLoan(@PathVariable Long loanId){
        return ResponseEntity.ok(workflowService.getByLoan(loanId));
    }

    @Operation(summary = "List Workflows by userId")
    @GetMapping
    public ResponseEntity<List<Workflow>> listResponseEntity(@RequestParam String userId){
        return ResponseEntity.ok(workflowService.listbyUser(userId));
    }
}

package com.scb.loanOrigination.controller;

import com.scb.loanOrigination.dto.user.LoanApplicationRequest;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.service.LoanApplicationsServiceImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Loans")
public class LoanController {

    private final LoanApplicationsServiceImp loanService;

    public LoanController(LoanApplicationsServiceImp loanService) {
        this.loanService = loanService;
    }

    @Operation(summary = "Create a new loan application")
    @PostMapping("/loans")
    public ResponseEntity<LoanApplications> create(@Valid @RequestBody LoanApplicationRequest req) {
        return ResponseEntity.ok(loanService.create(req));
    }

    @Operation(summary = "Get a loan by loanId")
    @GetMapping("/loans/{loanId}")
    public ResponseEntity<LoanApplications> getById(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getById(loanId));
    }

    @Operation(summary = "List loans by userId")
    @GetMapping("/users/{userId}/loans")
    public ResponseEntity<List<LoanApplications>> listByUser(@PathVariable String userId) {
        return ResponseEntity.ok(loanService.listByUser(userId));
    }

    @Operation(summary = "List all loans")
    @GetMapping("/loans")
    public ResponseEntity<List<LoanApplications>> listAll() {
        return ResponseEntity.ok(loanService.listAll());
    }
}
package com.scb.loanOrigination.controller;


import com.scb.loanOrigination.entity.Users;
import com.scb.loanOrigination.service.UsersServiceImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")
public class UserController {

    private final UsersServiceImp usersServiceImp;

    public UserController(UsersServiceImp usersServiceImp) {
        this.usersServiceImp = usersServiceImp;
    }


    @Operation(summary = "Login via query params {GET}")
    @GetMapping("/login")
    public ResponseEntity<?> loginGet(
            @Parameter(in = ParameterIn.QUERY)
            @RequestParam String userId,
            @Parameter(in = ParameterIn.QUERY)
            @RequestParam String password
    ) {
        Users u = usersServiceImp.authenticate(userId, password);
        if (u == null || !password.equals(u.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid Credentials Entered"));
        }
        return ResponseEntity.ok(Map.of(
                "message", "login succesful",
                "userId", u.getUserId(),
                "role", u.getRoleName()
        ));
    }



    @Operation(summary = "List all users")
    @GetMapping("/users")
    public ResponseEntity<List<Users>> list(){
        List<Users> list = usersServiceImp.listAll();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get user by business userId")
    @GetMapping("/users/{userId}")
    public ResponseEntity<Users> getUserById(@PathVariable("userId") String userid){
        Users u = usersServiceImp.getByUserId(userid);
        return ResponseEntity.ok(u);
    }

}
package com.scb.loanOrigination.dto;

public class FlagDocumentDTO {
    private long loanId;
    private String type;
    private String comment;

    public long getLoanId() {
        return loanId;
    }

    public void setLoanId(long loanId) {
        this.loanId = loanId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
Entity 
package com.scb.loanOrigination.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.text.DateFormat;
import java.util.Date;

@Entity
@Table(name="Customers")
@RequiredArgsConstructor
@Getter
@Setter
public class Customers {
    @Id
    private String userId;

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

    private String lastName;

    @Column(nullable = false)
    private String pan;

    @Column(nullable = false)
    private long aadhaar;

    @Column(nullable = false)
    private long mobileNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Maps the user_id field to the Users' primary key
    @JoinColumn(name = "userId")
    @JsonBackReference("user-customer")
    private Users user;
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public long getAadhaar() {
        return aadhaar;
    }

    public void setAadhaar(long aadhaar) {
        this.aadhaar = aadhaar;
    }

    public long getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(long mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}

package com.scb.loanOrigination.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="Documents")
@RequiredArgsConstructor
@Getter
@Setter
public class Documents {
    public enum DocumentStatusEnum {
        Flagged_For_ReUpload,
        Maker_Approved,
        Checker_Approved,
        Flagged_For_Data_ReEntry,
        Uploaded
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long documentId;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String entriesFilePath;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatusEnum status;

    @Column(nullable = false)
    private Boolean flag = false;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private long loanId;


    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }
    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getEntriesFilePath() {
        return entriesFilePath;
    }

    public void setEntriesFilePath(String entriesFilePath) {
        this.entriesFilePath = entriesFilePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public DocumentStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DocumentStatusEnum status) {
        this.status = status;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public long getLoanId() {
        return loanId;
    }

    public void setLoanId(long loanId) {
        this.loanId = loanId;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
package com.scb.loanOrigination.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="LoanApplications")
@RequiredArgsConstructor
@Getter
@Setter
public class LoanApplications {

    public enum LoanStatusEnum {
        Initiated,
        In_Progress,
        Rejected,
        Approved
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long loanId;

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Column(nullable=false)
    private String userId;

    @OneToMany(mappedBy = "loanId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("loanApplication-documents")
    private List<Documents> documents;


    public long getLoanId() {
        return loanId;
    }

    public void setLoanId(long loanId) {
        this.loanId = loanId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getLoanTenure() {
        return loanTenure;
    }

    public void setLoanTenure(int loanTenure) {
        this.loanTenure = loanTenure;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public LoanStatusEnum getStatus() {
        return status;
    }

    public void setStatus(LoanStatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Documents> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Documents> documents) {
        this.documents = documents;
    }

}

package com.scb.loanOrigination.entity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="Users")
@RequiredArgsConstructor
@Getter
@Setter
public class Users
{
    public enum RoleNameEnum {
        Customer,
        Maker,
        Checker
    }
    @Id
    private String userId;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleNameEnum roleName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    @JsonManagedReference("user-loanApplications")
    private List <LoanApplications> loanApplications;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    @JsonManagedReference("user-workflows")
    private List<Workflow> workflows;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    @JsonManagedReference("user-documents")
    private List<Documents> documents;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-customer")
    private Customers customer;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleNameEnum getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleNameEnum roleName) {
        this.roleName = roleName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<LoanApplications> getLoanApplications() {
        return loanApplications;
    }

    public void setLoanApplications(List<LoanApplications> loanApplications) {
        this.loanApplications = loanApplications;
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }

    public List<Documents> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Documents> documents) {
        this.documents = documents;
    }

    public Customers getCustomer() {
        return customer;
    }

    public void setCustomer(Customers customer) {
        this.customer = customer;
    }
}
package com.scb.loanOrigination.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.scb.loanOrigination.LoanOrigination;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="Workflow")
@RequiredArgsConstructor
@Getter
@Setter
public class Workflow {
    public enum WorkflowStepNameEnum {
        Maker,
        Checker,
        Approval,
        Customer
    }

    public enum WorkFlowStatusEnum {
        Flagged_For_ReUpload,
        Moved_To_Checker,

        Moved_To_Maker,

        Approved,
        Flagged_For_Data_ReEntry
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long workflowId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStepNameEnum stepName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkFlowStatusEnum status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "loanId", insertable = false, updatable = false, nullable = false)
    private long loanId;

    private String remarks;

    private String userId;

    @OneToOne
    @JoinColumn(name="loanId")
    @JsonBackReference("user-workflows")
    private LoanApplications loanApplication;

    public long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(long workflowId) {
        this.workflowId = workflowId;
    }

    public WorkflowStepNameEnum getStepName() {
        return stepName;
    }

    public void setStepName(WorkflowStepNameEnum stepName) {
        this.stepName = stepName;
    }

    public WorkFlowStatusEnum getStatus() {
        return status;
    }

    public void setStatus(WorkFlowStatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getLoanId() {
        return loanId;
    }

    public void setLoanId(long loanId) {
        this.loanId = loanId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LoanApplications getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(LoanApplications loanApplication) {
        this.loanApplication = loanApplication;
    }
}

package com.scb.loanOrigination.repository;


import com.scb.loanOrigination.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    public Workflow findByloanId(long loanId);
    Optional<Workflow> findByLoanApplication_LoanId(Long loanId);

    List<Workflow> findByUserId(String userId);
}

package com.scb.loanOrigination.service;


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
}

package com.scb.loanOrigination.service;

import com.scb.loanOrigination.LoanOrigination;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.AppException;
import com.scb.loanOrigination.exception.CheckerException;
import com.scb.loanOrigination.exception.MakerException;

import com.scb.loanOrigination.repository.LoanRepository;
import com.scb.loanOrigination.repository.WorkflowRepository;
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
}

CHANGED THE CONTROLLER FOR SWAGGER

package com.scb.loanOrigination.service;

import com.scb.loanOrigination.LoanOrigination;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.AppException;
import com.scb.loanOrigination.exception.CheckerException;
import com.scb.loanOrigination.exception.MakerException;

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
    @Transactional
    public List<Workflow> getWorkflowsByStatus(Workflow.WorkFlowStatusEnum status){
        return workflowRepo.findByStatus(status);
    }
}

package com.scb.loanOrigination.controller;


import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.service.WorkflowServiceImp;
//import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")

public class WorkflowController {

    private final WorkflowServiceImp workflowService;

    public WorkflowController(WorkflowServiceImp workflowService) {
        this.workflowService = workflowService;
    }

    //@Operation(summary = "Get Workflow by loanId")
    @GetMapping("/by-loan/{loanId}")
    public ResponseEntity<Workflow> getByLoan(@PathVariable Long loanId){
        return ResponseEntity.ok(workflowService.getByLoan(loanId));
    }

    //@Operation(summary = "List Workflows by userId")
    @GetMapping
    public ResponseEntity<List<Workflow>> listResponseEntity(@RequestParam String userId){
        return ResponseEntity.ok(workflowService.listbyUser(userId));
    }

    @RequestMapping(value="/getWorkflowsByStatus/{status}",method = RequestMethod.GET)
    public List<Workflow> getWorkflowsByStatus(@PathVariable Workflow.WorkFlowStatusEnum status){
        return workflowService.getWorkflowsByStatus(status);
    }
}

package com.scb.loanOrigination.repository;


import com.scb.loanOrigination.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    public Workflow findByloanId(long loanId);
    Optional<Workflow> findByLoanApplication_LoanId(Long loanId);

    List<Workflow> findByUserId(String userId);

    List<Workflow> findByStatus(Workflow.WorkFlowStatusEnum status);
}

package com.scb.loanOrigination.service;


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

    List<Workflow> getWorkflowsByStatus(Workflow.WorkFlowStatusEnum status);
}

-- Insert Users
INSERT INTO Users (userId, password, roleName, createdAt)
VALUES 
('user_maker1', 'pass123', 'Maker', NOW()),
('user_checker1', 'pass123', 'Checker', NOW()),
('user_customer1', 'pass123', 'Customer', NOW());

-- Insert Customers (linked with user_customer1)
INSERT INTO Customers (userId, address, dob, gender, email, firstName, lastName, pan, aadhaar, mobileNumber)
VALUES 
('user_customer1', 'Mumbai', '1995-06-15', 'F', 'customer1@mail.com', 'Priya', 'Sharma', 'ABCDE1234F', 123456789012, 9876543210);

-- Insert Loan Applications
INSERT INTO LoanApplications (amount, currency, loanTenure, interestRate, status, createdAt, userId)
VALUES
(500000, 'INR', 24, 8.5, 'Initiated', NOW(), 'user_customer1'),
(800000, 'INR', 36, 9.0, 'In_Progress', NOW(), 'user_customer1'),
(1200000, 'INR', 60, 10.0, 'Rejected', NOW(), 'user_customer1'),
(250000, 'INR', 12, 7.5, 'Approved', NOW(), 'user_customer1'),
(400000, 'INR', 18, 8.0, 'In_Progress', NOW(), 'user_customer1');

-- Insert Workflows (5 test cases with different stepName + status)
INSERT INTO Workflow (stepName, status, createdAt, updatedAt, loanId, remarks, userId)
VALUES
('Maker', 'Moved_To_Maker', NOW(), NOW(), 1, 'Initial review by Maker', 'user_maker1'),
('Maker', 'Flagged_For_ReUpload', NOW(), NOW(), 2, 'Maker flagged document issue', 'user_maker1'),
('Checker', 'Moved_To_Checker', NOW(), NOW(), 3, 'Sent to Checker for validation', 'user_checker1'),
('Approval', 'Approved', NOW(), NOW(), 4, 'Loan approved by Approver', 'user_checker1'),
('Customer', 'Flagged_For_Data_ReEntry', NOW(), NOW(), 5, 'Customer needs to correct data', 'user_customer1');

###
-- USERS
INSERT INTO Users (userId, password, roleName, createdAt) VALUES
('maker1', 'pass123', 'Maker', NOW()),
('maker2', 'pass123', 'Maker', NOW()),
('maker3', 'pass123', 'Maker', NOW()),
('maker4', 'pass123', 'Maker', NOW());

-- CUSTOMERS
INSERT INTO Customers (userId, address, dob, gender, email, firstName, lastName, pan, aadhaar, mobileNumber) VALUES
('maker1', 'Delhi', '1990-01-15', 'M', 'john.doe@mail.com', 'John', 'Doe', 'ABCDE1234F', 123456789012, 9876543210),
('maker2', 'Mumbai', '1992-03-20', 'F', 'alice.smith@mail.com', 'Alice', 'Smith', 'PQRSX9876L', 223456789012, 9876543211),
('maker3', 'Bangalore', '1991-07-25', 'M', 'rahul.kumar@mail.com', 'Rahul', 'Kumar', 'LMNOP3456Z', 323456789012, 9876543212),
('maker4', 'Chennai', '1993-12-05', 'F', 'maria.lopez@mail.com', 'Maria', 'Lopez', 'GHJKL5432Q', 423456789012, 9876543213);

-- LOAN APPLICATIONS
INSERT INTO LoanApplications (amount, currency, loanTenure, interestRate, status, createdAt, userId) VALUES
(500000, 'INR', 24, 8.5, 'Initiated', NOW(), 'maker1'),
(600000, 'INR', 36, 9.0, 'In_Progress', NOW(), 'maker2'),
(750000, 'INR', 48, 8.0, 'In_Progress', NOW(), 'maker3'),
(400000, 'INR', 18, 7.5, 'Initiated', NOW(), 'maker4');

-- WORKFLOWS (only Maker-relevant statuses)
INSERT INTO Workflow (stepName, status, createdAt, updatedAt, loanId, remarks, userId) VALUES
('Maker', 'Moved_To_Maker', NOW(), NOW(), 1, 'Loan submitted for review', 'maker1'),
('Maker', 'Flagged_For_ReUpload', NOW(), NOW(), 2, 'Address proof needs resubmission', 'maker2'),
('Maker', 'Flagged_For_Data_ReEntry', NOW(), NOW(), 3, 'Salary slip mismatch', 'maker3'),
('Maker', 'Moved_To_Maker', NOW(), NOW(), 4, 'Fresh loan assigned to Maker', 'maker4');

-- DOCUMENTS (flags for testing)
INSERT INTO Documents (documentName, fileName, filePath, entriesFilePath, uploadedAt, status, flag, comment, userId, loanId) VALUES
('ID_PROOF', 'id1.pdf', '/docs/id1.pdf', '/entries/id1.json', NOW(), 'Flagged_For_ReUpload', true, 'Photo unclear', 'maker1', 1),
('ADDRESS_PROOF', 'addr2.pdf', '/docs/addr2.pdf', '/entries/addr2.json', NOW(), 'Uploaded', false, NULL, 'maker2', 2),
('BANK_STATEMENT', 'bank2.pdf', '/docs/bank2.pdf', '/entries/bank2.json', NOW(), 'Flagged_For_ReUpload', true, 'Old statement', 'maker2', 2),
('SALARY_SLIP', 'salary3.pdf', '/docs/salary3.pdf', '/entries/salary3.json', NOW(), 'Flagged_For_Data_ReEntry', true, 'Mismatch in salary', 'maker3', 3),
('PAN_CARD', 'pan4.pdf', '/docs/pan4.pdf', '/entries/pan4.json', NOW(), 'Uploaded', false, NULL, 'maker4', 4);


###changed one 

-- USERS
INSERT INTO users (user_id, password, role_name, created_at) VALUES
('maker1', 'pass123', 'Maker', NOW()),
('maker2', 'pass123', 'Maker', NOW()),
('maker3', 'pass123', 'Maker', NOW()),
('maker4', 'pass123', 'Maker', NOW());

-- CUSTOMERS
INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number) VALUES
('maker1', 'Delhi', '1990-01-15', 'M', 'john.doe@mail.com', 'John', 'Doe', 'ABCDE1234F', 123456789012, 9876543210),
('maker2', 'Mumbai', '1992-03-20', 'F', 'alice.smith@mail.com', 'Alice', 'Smith', 'PQRSX9876L', 223456789012, 9876543211),
('maker3', 'Bangalore', '1991-07-25', 'M', 'rahul.kumar@mail.com', 'Rahul', 'Kumar', 'LMNOP3456Z', 323456789012, 9876543212),
('maker4', 'Chennai', '1993-12-05', 'F', 'maria.lopez@mail.com', 'Maria', 'Lopez', 'GHJKL5432Q', 423456789012, 9876543213);

-- LOAN APPLICATIONS
INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id) VALUES
(500000, 'INR', 24, 8.5, 'Initiated', NOW(), 'maker1'),
(600000, 'INR', 36, 9.0, 'In_Progress', NOW(), 'maker2'),
(750000, 'INR', 48, 8.0, 'In_Progress', NOW(), 'maker3'),
(400000, 'INR', 18, 7.5, 'Initiated', NOW(), 'maker4');

-- WORKFLOWS (only Maker-relevant statuses)
INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id) VALUES
('Maker', 'Moved_To_Maker', NOW(), NOW(), 1, 'Loan submitted for review', 'maker1'),
('Maker', 'Flagged_For_ReUpload', NOW(), NOW(), 2, 'Address proof needs resubmission', 'maker2'),
('Maker', 'Flagged_For_Data_ReEntry', NOW(), NOW(), 3, 'Salary slip mismatch', 'maker3'),
('Maker', 'Moved_To_Maker', NOW(), NOW(), 4, 'Fresh loan assigned to Maker', 'maker4');

-- DOCUMENTS (document_id is auto-generated, so we don’t insert it)
INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id) VALUES
('ID_PROOF', 'id1.pdf', '/docs/id1.pdf', '/entries/id1.json', NOW(), 'Flagged_For_ReUpload', true, 'Photo unclear', 'maker1', 1),
('ADDRESS_PROOF', 'addr2.pdf', '/docs/addr2.pdf', '/entries/addr2.json', NOW(), 'Uploaded', false, NULL, 'maker2', 2),
('BANK_STATEMENT', 'bank2.pdf', '/docs/bank2.pdf', '/entries/bank2.json', NOW(), 'Flagged_For_ReUpload', true, 'Old statement', 'maker2', 2),
('SALARY_SLIP', 'salary3.pdf', '/docs/salary3.pdf', '/entries/salary3.json', NOW(), 'Flagged_For_Data_ReEntry', true, 'Mismatch in salary', 'maker3', 3),
('PAN_CARD', 'pan4.pdf', '/docs/pan4.pdf', '/entries/pan4.json', NOW(), 'Uploaded', false, NULL, 'maker4', 4);

INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id) VALUES
('ID_PROOF', 'id1.pdf', '/docs/id1.pdf', '/entries/id1.json', NOW(), 'Flagged_For_ReUpload', true, 'Photo unclear', 'maker1', 1),
('ADDRESS_PROOF', 'addr2.pdf', '/docs/addr2.pdf', '/entries/addr2.json', NOW(), 'Uploaded', false, 'OK', 'maker2', 2),
('BANK_STATEMENT', 'bank2.pdf', '/docs/bank2.pdf', '/entries/bank2.json', NOW(), 'Flagged_For_ReUpload', true, 'Old statement', 'maker2', 2),
('SALARY_SLIP', 'salary3.pdf', '/docs/salary3.pdf', '/entries/salary3.json', NOW(), 'Flagged_For_Data_ReEntry', true, 'Mismatch in salary', 'maker3', 3),
('PAN_CARD', 'pan4.pdf', '/docs/pan4.pdf', '/entries/pan4.json', NOW(), 'Uploaded', false, 'OK', 'maker4', 4);


-- 1. USERS
INSERT INTO users (user_id, password, role_name, created_at) VALUES
('maker1', 'pass123', 'Maker', NOW()),
('maker2', 'pass123', 'Maker', NOW()),
('maker3', 'pass123', 'Maker', NOW()),
('maker4', 'pass123', 'Maker', NOW());

-- 2. CUSTOMERS
INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number) VALUES
('maker1', 'Delhi', '1990-01-15', 'M', 'john.doe@mail.com', 'John', 'Doe', 'ABCDE1234F', 123456789012, 9876543210),
('maker2', 'Mumbai', '1992-03-20', 'F', 'alice.smith@mail.com', 'Alice', 'Smith', 'PQRSX9876L', 223456789012, 9876543211),
('maker3', 'Bangalore', '1991-07-25', 'M', 'rahul.kumar@mail.com', 'Rahul', 'Kumar', 'LMNOP3456Z', 323456789012, 9876543212),
('maker4', 'Chennai', '1993-12-05', 'F', 'maria.lopez@mail.com', 'Maria', 'Lopez', 'GHJKL5432Q', 423456789012, 9876543213);

-- 3. LOAN APPLICATIONS (capture loan_id)
INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (500000, 'INR', 24, 8.5, 'Initiated', NOW(), 'maker1')
RETURNING loan_id INTO loan1;

INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (600000, 'INR', 36, 9.0, 'In_Progress', NOW(), 'maker2')
RETURNING loan_id INTO loan2;

INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (750000, 'INR', 48, 8.0, 'In_Progress', NOW(), 'maker3')
RETURNING loan_id INTO loan3;

INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (400000, 'INR', 18, 7.5, 'Initiated', NOW(), 'maker4')
RETURNING loan_id INTO loan4;

-- 4. WORKFLOWS (use captured loan_ids)
INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id)
VALUES ('Maker', 'Moved_To_Maker', NOW(), NOW(), loan1, 'Loan submitted for review', 'maker1');

INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id)
VALUES ('Maker', 'Flagged_For_ReUpload', NOW(), NOW(), loan2, 'Address proof needs resubmission', 'maker2');

INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id)
VALUES ('Maker', 'Flagged_For_Data_ReEntry', NOW(), NOW(), loan3, 'Salary slip mismatch', 'maker3');

INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id)
VALUES ('Maker', 'Moved_To_Maker', NOW(), NOW(), loan4, 'Fresh loan assigned to Maker', 'maker4');

-- 5. DOCUMENTS (link to same loan_ids, comment NOT NULL)
INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES ('ID_PROOF', 'id1.pdf', '/docs/id1.pdf', '/entries/id1.json', NOW(), 'Flagged_For_ReUpload', true, 'Photo unclear', 'maker1', loan1);

INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES ('ADDRESS_PROOF', 'addr2.pdf', '/docs/addr2.pdf', '/entries/addr2.json', NOW(), 'Uploaded', false, 'OK', 'maker2', loan2);

INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES ('BANK_STATEMENT', 'bank2.pdf', '/docs/bank2.pdf', '/entries/bank2.json', NOW(), 'Flagged_For_ReUpload', true, 'Old statement', 'maker2', loan2);

INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES ('SALARY_SLIP', 'salary3.pdf', '/docs/salary3.pdf', '/entries/salary3.json', NOW(), 'Flagged_For_Data_ReEntry', true, 'Mismatch in salary', 'maker3', loan3);

INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES ('PAN_CARD', 'pan4.pdf', '/docs/pan4.pdf', '/entries/pan4.json', NOW(), 'Uploaded', false, 'OK', 'maker4', loan4);


package com.scb.loanOrigination.dto.makerInbox;

public interface MakerInboxProjection {
    Long getWorkflowId();
    Long getLoanId();
    String getUserId();
    String getApplicantName();
    String getStatus();
    String getRemarks();
    Integer getFlagsCount();
    String getCreatedAt();
    String getUpdatedAt();
}

package com.scb.loanOrigination.repository;

import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.dto.makerInbox.MakerInboxProjection;
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
    List<Workflow> findByStepName(Workflow.WorkflowStepNameEnum stepName);

    // ✅ New Maker Inbox query
    @Query("""
    SELECT w.workflowId AS workflowId,
           w.loanId AS loanId,
           w.userId AS userId,
           CONCAT(c.firstName, ' ', c.lastName) AS applicantName,
           w.status AS status,
           w.remarks AS remarks,
           COUNT(d) FILTER (WHERE d.flag = true) AS flagsCount,
           CAST(w.createdAt AS string) AS createdAt,
           CAST(w.updatedAt AS string) AS updatedAt
    FROM Workflow w
    JOIN LoanApplications l ON w.loanId = l.loanId
    JOIN Customers c ON l.userId = c.userId
    LEFT JOIN Documents d ON d.loanId = w.loanId
    WHERE w.stepName = 'Maker'
      AND w.status IN ('Moved_To_Maker','Flagged_For_ReUpload','Flagged_For_Data_ReEntry')
    GROUP BY w.workflowId, w.loanId, w.userId, c.firstName, c.lastName,
             w.status, w.remarks, w.createdAt, w.updatedAt
    """)
    List<MakerInboxProjection> getMakerInbox();
}

package com.scb.loanOrigination.service;

import com.scb.loanOrigination.dto.makerInbox.MakerInboxProjection;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowServiceImp implements IWorkflow {

    private final WorkflowRepository workflowRepo;

    public WorkflowServiceImp(WorkflowRepository workflowRepo) {
        this.workflowRepo = workflowRepo;
    }

    // Existing methods...

    // ✅ New service method
    @Transactional
    public List<MakerInboxProjection> getMakerInbox() {
        return workflowRepo.getMakerInbox();
    }
}


package com.scb.loanOrigination.controller;

import com.scb.loanOrigination.dto.makerInbox.MakerInboxProjection;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")
public class WorkflowController {

    private final WorkflowServiceImp workflowService;

    public WorkflowController(WorkflowServiceImp workflowService) {
        this.workflowService = workflowService;
    }

    // Existing endpoints...

    // ✅ New Maker Inbox endpoint
    @GetMapping("/makerInbox")
    public ResponseEntity<List<MakerInboxProjection>> getMakerInbox() {
        return ResponseEntity.ok(workflowService.getMakerInbox());
    }
}
