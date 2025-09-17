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
import java.time.format.DateTimeFormatter;
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


    @RequestMapping(value="/getWorkflowsByStatus/{status}",method = RequestMethod.GET) // For my Maker's Inbox Status is pending on postman i will choose right know 
    public List<Workflow> getWorkflowsByStatus(@PathVariable Workflow.WorkFlowStatusEnum status){
        return workflowService.getWorkflowsByStatus(status);
    }
}

package com.scb.loanOrigination.service;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.MakerException;
import com.scb.loanOrigination.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    @Override
    @Transactional
    public List<Workflow> getWorkflowsByStatus(Workflow.WorkFlowStatusEnum status){
        return workflowRepo.findByStatus(status);
    }
}

package com.scb.loanOrigination.repository;
import com.scb.loanOrigination.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {

    List<Workflow> findByStatus(Workflow.WorkFlowStatusEnum status);
}

package com.scb.loanOrigination.service;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.MakerException;

import java.util.List;


public interface IWorkflow {

    public Workflow getWorkflowDetails(int workflowId) throws MakerException;

    List<Workflow> getWorkflowsByStatus(Workflow.WorkFlowStatusEnum status);
}

// --- getters & setters ---

    public String getUser_id() {
        return user_id;
    }
    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

package com.scb.loanOrigination.repository;

import com.scb.loanOrigination.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {

    List<Workflow> findByStatus(Workflow.WorkFlowStatusEnum status);

    /**
     * Returns rows for maker inbox:
     * columns:
     * 0 -> workflow_id
     * 1 -> loan_id
     * 2 -> user_id
     * 3 -> applicant_name (first + last)
     * 4 -> created_at
     * 5 -> updated_at
     * 6 -> status
     * 7 -> flags_count (count of documents.flag = true)
     * 8 -> remark (latest flagged document.comment or null)
     */
    @Query(value =
            "SELECT w.workflow_id, " +
            "       l.loan_id, " +
            "       l.user_id, " +
            "       concat(c.first_name, ' ', c.last_name) AS applicant_name, " +
            "       w.created_at, w.updated_at, w.status, " +
            "       COALESCE(SUM(CASE WHEN d.flag = true THEN 1 ELSE 0 END), 0) AS flags_count, " +
            "       (SELECT d2.comment FROM documents d2 WHERE d2.loan_id = l.loan_id AND d2.flag = true ORDER BY d2.uploaded_at DESC LIMIT 1) AS remark " +
            "FROM workflow w " +
            "JOIN loan_applications l ON w.loan_id = l.loan_id " +
            "JOIN customers c ON c.user_id = l.user_id " +
            "LEFT JOIN documents d ON d.loan_id = l.loan_id " +
            "WHERE w.status = :status " +
            "GROUP BY w.workflow_id, l.loan_id, l.user_id, c.first_name, c.last_name, w.created_at, w.updated_at, w.status",
            nativeQuery = true)
    List<Object[]> findMakerInboxByStatusNative(String status);
}


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkflowServiceImp implements IWorkflow {
    @Autowired
    private WorkflowRepository workflowRepo;

    // existing methods ...

    @Override
    @Transactional
    public List<Workflow> getWorkflowsByStatus(Workflow.WorkFlowStatusEnum status){
        return workflowRepo.findByStatus(status);
    }

    // New method to get maker inbox rows (aggregated)
    public List<Map<String, Object>> getMakerInboxByStatus(Workflow.WorkFlowStatusEnum status) {
        List<Object[]> rows = workflowRepo.findMakerInboxByStatusNative(status.name());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new HashMap<>();
            // index mapping follows repository query comment:
            m.put("workflowId", r[0] == null ? null : ((Number) r[0]).intValue());
            m.put("loanId", r[1] == null ? null : ((Number) r[1]).intValue());
            m.put("userId", r[2] == null ? null : r[2].toString());
            m.put("applicantName", r[3] == null ? "" : r[3].toString());

            // createdAt / updatedAt are typically Timestamp from native query => convert to ISO string
            m.put("createdAt", r[4] == null ? null : r[4].toString());
            m.put("updatedAt", r[5] == null ? null : r[5].toString());

            m.put("status", r[6] == null ? null : r[6].toString());
            m.put("flagsCount", r[7] == null ? 0 : ((Number) r[7]).intValue());
            m.put("remark", r[8] == null ? "" : r[8].toString());

            out.add(m);
        }
        return out;
    }
}


@RequestMapping(value = "/makerInbox/{status}", method = RequestMethod.GET)
public List<Map<String, Object>> getMakerInbox(@PathVariable Workflow.WorkFlowStatusEnum status) {
    return workflowService.getMakerInboxByStatus(status);
}

-- ============= USER 1 =============
INSERT INTO users (user_id, password, role_name, created_at)
VALUES ('user100','pass123','Customer', NOW());

INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number)
VALUES ('user100', '123 Main St, City', '1990-01-01', 'M', 'john.doe@example.com', 'John', 'Doe', 'ABCDE1234F', 123456789012, 9876543210);

INSERT INTO loan_applications (loan_id, amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (100, 500000, 'INR', 24, 7.5, 'Initiated', NOW(), 'user100');

INSERT INTO documents (document_id, document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES (1, 'ID_PROOF', 'id_100.jpg', '/files/id_100.jpg', '/entries/id_100.csv', NOW(), 'Flagged_For_ReUpload', true, 'ID blurry - reupload required', 'user100', 100),
       (2, 'Address_PROOF', 'addr_100.jpg', '/files/addr_100.jpg', '/entries/addr_100.csv', NOW(), 'Approved', false, '', 'user100', 100);

INSERT INTO workflow (workflow_id, step_name, status, created_at, updated_at, remarks, user_id, loan_id)
VALUES (1, 'Maker', 'Pending', NOW(), NOW(), 'Please re-check documents', 'user100', 100);


-- ============= USER 2 =============
INSERT INTO users (user_id, password, role_name, created_at)
VALUES ('user200','pass456','Customer', NOW());

INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number)
VALUES ('user200', '456 Second St, City', '1992-05-15', 'F', 'jane.smith@example.com', 'Jane', 'Smith', 'PQRSX6789Z', 234567890123, 9876501234);

INSERT INTO loan_applications (loan_id, amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (200, 1000000, 'INR', 36, 8.2, 'Initiated', NOW(), 'user200');

INSERT INTO documents (document_id, document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES (3, 'Income_PROOF', 'income_200.pdf', '/files/income_200.pdf', '/entries/income_200.csv', NOW(), 'Approved', false, '', 'user200', 200),
       (4, 'Employment_PROOF', 'emp_200.pdf', '/files/emp_200.pdf', '/entries/emp_200.csv', NOW(), 'Flagged_For_ReUpload', true, 'Joining date mismatch', 'user200', 200);

INSERT INTO workflow (workflow_id, step_name, status, created_at, updated_at, remarks, user_id, loan_id)
VALUES (2, 'Maker', 'Pending', NOW(), NOW(), 'Check employment proof', 'user200', 200);

-- ============= USER 3 =============
INSERT INTO users (user_id, password, role_name, created_at)
VALUES ('user300','pass789','Customer', NOW());

INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number)
VALUES ('user300', '789 Third St, City', '1995-07-20', 'M', 'alex.johnson@example.com', 'Alex', 'Johnson', 'LMNOP3456Q', 345678901234, 9876512345);

INSERT INTO loan_applications (loan_id, amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (300, 750000, 'INR', 48, 9.0, 'Initiated', NOW(), 'user300');

-- Documents: both approved, none flagged
INSERT INTO documents (document_id, document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES (5, 'PAN_PROOF', 'pan_300.pdf', '/files/pan_300.pdf', '/entries/pan_300.csv', NOW(), 'Approved', false, '', 'user300', 300),
       (6, 'Bank_Statement', 'bank_300.pdf', '/files/bank_300.pdf', '/entries/bank_300.csv', NOW(), 'Approved', false, '', 'user300', 300);

INSERT INTO workflow (workflow_id, step_name, status, created_at, updated_at, remarks, user_id, loan_id)
VALUES (3, 'Maker', 'Pending', NOW(), NOW(), 'All docs look fine', 'user300', 300);

GET http://localhost:8080/makerInbox/Pending


package com.scb.loanOrigination.repository;

import com.scb.loanOrigination.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {

    // Old method (can be commented if unused)
    // List<Workflow> findByStatus(Workflow.WorkFlowStatusEnum status);

    @Query(value = """
        SELECT w.workflow_id,
               l.loan_id,
               l.user_id,
               concat(c.first_name, ' ', c.last_name) AS applicant_name,
               w.created_at,
               w.updated_at,
               w.status,
               COALESCE(SUM(CASE WHEN d.flag = true THEN 1 ELSE 0 END), 0) AS flags_count,
               (SELECT d2.comment
                  FROM documents d2
                 WHERE d2.loan_id = l.loan_id
                   AND d2.flag = true
                 ORDER BY d2.uploaded_at DESC
                 LIMIT 1) AS remark
          FROM workflow w
          JOIN loan_applications l ON w.loan_id = l.loan_id
          JOIN customers c ON c.user_id = l.user_id
          LEFT JOIN documents d ON d.loan_id = l.loan_id
         WHERE w.status = :status
      GROUP BY w.workflow_id, l.loan_id, l.user_id,
               c.first_name, c.last_name, w.created_at, w.updated_at, w.status
        """,
        nativeQuery = true)
    List<Object[]> findMakerInboxByStatusNative(String status);
}



-- ============= USER 1 =============
INSERT INTO users (user_id, password, role_name, created_at)
VALUES ('user100','pass123','Customer', NOW());

INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number)
VALUES ('user100', '123 Main St, City', '1990-01-01', 'M', 'john.doe@example.com', 'John', 'Doe', 'ABCDE1234F', 123456789012, 9876543210);

INSERT INTO loan_applications (loan_id, amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (100, 500000, 'INR', 24, 7.5, 'Initiated', NOW(), 'user100');

INSERT INTO documents (document_id, document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES (1, 'ID_PROOF', 'id_100.jpg', '/files/id_100.jpg', '/entries/id_100.csv', NOW(), 'Flagged_For_ReUpload', true, 'ID blurry - reupload required', 'user100', 100),
       (2, 'Address_PROOF', 'addr_100.jpg', '/files/addr_100.jpg', '/entries/addr_100.csv', NOW(), 'Approved', false, '', 'user100', 100);

INSERT INTO workflow (workflow_id, step_name, status, created_at, updated_at, remarks, user_id, loan_id)
VALUES (1, 'Maker', 'Pending', NOW(), NOW(), 'Please re-check documents', 'user100', 100);


-- ============= USER 2 =============
INSERT INTO users (user_id, password, role_name, created_at)
VALUES ('user200','pass456','Customer', NOW());

INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number)
VALUES ('user200', '456 Second St, City', '1992-05-15', 'F', 'jane.smith@example.com', 'Jane', 'Smith', 'PQRSX6789Z', 234567890123, 9876501234);

INSERT INTO loan_applications (loan_id, amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (200, 1000000, 'INR', 36, 8.2, 'Initiated', NOW(), 'user200');

INSERT INTO documents (document_id, document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES (3, 'Income_PROOF', 'income_200.pdf', '/files/income_200.pdf', '/entries/income_200.csv', NOW(), 'Approved', false, '', 'user200', 200),
       (4, 'Employment_PROOF', 'emp_200.pdf', '/files/emp_200.pdf', '/entries/emp_200.csv', NOW(), 'Flagged_For_ReUpload', true, 'Joining date mismatch', 'user200', 200);

INSERT INTO workflow (workflow_id, step_name, status, created_at, updated_at, remarks, user_id, loan_id)
VALUES (2, 'Maker', 'Pending', NOW(), NOW(), 'Check employment proof', 'user200', 200);


-- ============= USER 3 (no flags) =============
INSERT INTO users (user_id, password, role_name, created_at)
VALUES ('user300','pass789','Customer', NOW());

INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number)
VALUES ('user300', '789 Third St, City', '1995-07-20', 'M', 'alex.johnson@example.com', 'Alex', 'Johnson', 'LMNOP3456Q', 345678901234, 9876512345);

INSERT INTO loan_applications (loan_id, amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (300, 750000, 'INR', 48, 9.0, 'Initiated', NOW(), 'user300');

INSERT INTO documents (document_id, document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES (5, 'PAN_PROOF', 'pan_300.pdf', '/files/pan_300.pdf', '/entries/pan_300.csv', NOW(), 'Approved', false, '', 'user300', 300),
       (6, 'Bank_Statement', 'bank_300.pdf', '/files/bank_300.pdf', '/entries/bank_300.csv', NOW(), 'Approved', false, '', 'user300', 300);

INSERT INTO workflow (workflow_id, step_name, status, created_at, updated_at, remarks, user_id, loan_id)
VALUES (3, 'Maker', 'Pending', NOW(), NOW(), 'All docs look fine', 'user300', 300);


[
    {
        "createdAt": "2025-09-17 13:29:32.399148",
        "remark": "ID blurry - reupload required",
        "applicantName": "John Doe",
        "userId": "user100",
        "workflowId": 1,
        "loanId": 100,
        "updatedAt": "2025-09-17 13:29:32.399148",
        "status": "Pending",
        "flagsCount": 1
    },
    {
        "createdAt": "2025-09-17 13:29:32.399148",
        "remark": "Joining date mismatch",
        "applicantName": "Jane Smith",
        "userId": "user200",
        "workflowId": 2,
        "loanId": 200,
        "updatedAt": "2025-09-17 13:29:32.399148",
        "status": "Pending",
        "flagsCount": 1
    },
    {
        "createdAt": "2025-09-17 13:29:32.399148",
        "remark": "",
        "applicantName": "Alex Johnson",
        "userId": "user300",
        "workflowId": 3,
        "loanId": 300,
        "updatedAt": "2025-09-17 13:29:32.399148",
        "status": "Pending",
        "flagsCount": 0
    }
]
