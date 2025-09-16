package com.scb.loanOrigination.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
        Approval
    }

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
}

package com.scb.loanOrigination.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigInteger;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name="Documents")
@RequiredArgsConstructor
@Getter
@Setter
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

    @Column(nullable = false)
    private int loan_id;


    public BigInteger getDocumentId() {
        return documentId;
    }

    public void setDocumentId(BigInteger documentId) {
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

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getLoan_id() {
        return loan_id;
    }

    public void setLoan_id(int loan_id) {
        this.loan_id = loan_id;
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

    @OneToMany(mappedBy = "loan_id", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Documents> documents;


    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
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

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<Documents> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Documents> documents) {
        this.documents = documents;
    }

}

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

    @OneToOne(fetch = FetchType.LAZY) // Consider lazy fetching for performance
    @MapsId // Maps the user_id field to the Users' primary key
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private Users user;

}

package com.scb.loanOrigination.controller;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Users;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.service.LoanApplicationsServiceImp;
import com.scb.loanOrigination.service.UsersServiceImp;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
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
}

package com.scb.loanOrigination.service;


import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.MakerException;


public interface IWorkflow {

    public Workflow getWorkflowDetails(int workflowId) throws MakerException;
}
package com.scb.loanOrigination.service;

import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.MakerException;

import com.scb.loanOrigination.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImp implements IWorkflow
{
    @Autowired
    private WorkflowRepository workflowRepo;
    public Workflow getWorkflowDetails(int workflowId) throws MakerException{
        if(workflowRepo.existsById(workflowId))
        {
            return workflowRepo.findById(workflowId).get();
        }
        else
        {
            throw new MakerException("No Usern with user ID: "+workflowId+" found");
        }
    }
}
