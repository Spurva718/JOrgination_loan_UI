package com.scb.loanOrigination.controller;

import com.scb.loanOrigination.entity.Documents;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.service.LoanApplicationsServiceImp;
import com.scb.loanOrigination.service.UsersServiceImp;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class CheckerController {
    @Autowired
    private LoanApplicationsServiceImp loanService;

    @Autowired
    private UsersServiceImp userService;

    @Autowired
    private WorkflowServiceImp workflowService;

    @RequestMapping(value="/getWorkItemDocumentDetails/{loanId}",method= RequestMethod.GET)
    public List<Documents> getWorkItemDocumentDetails(@PathVariable("loanId") int loanId){
         return loanService.fetchWorkItemDocumentDetails(loanId);
    }

    @RequestMapping(value="/getFlaggedDocumentsDetails/{loanId}",method= RequestMethod.GET)
    public String getFlaggedDocumentsDetails(@PathVariable("loanId") int loanId){
//        return loanService.getLoanRequestDetails(loanId);
        return "";
    }

    @RequestMapping(value="/flagDocument/{docId}",method= RequestMethod.PUT)
    public String flagDocument(@PathVariable("docId") int loanId){
//        return loanService.getLoanRequestDetails(loanId);
        return "";
    }

    @RequestMapping(value="/assignMaker/{loanId}",method= RequestMethod.POST)
    public String assignMaker(@PathVariable("loanId") int loanId){
//        return loanService.getLoanRequestDetails(loanId);
        return "";
    }

    @RequestMapping(value="/approveLoan/{loanId}",method= RequestMethod.POST)
    public String approveLoan(@PathVariable("loanId") int loanId){
//        return loanService.getLoanRequestDetails(loanId);
        return "";
    }
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

package com.scb.loanOrigination.dto.documentChecker;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnDto {
    private String column_name;
    private String input_type;
}

package com.scb.loanOrigination.dto.documentChecker;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentsCheckerDto {
    private String proof_type;
    private ProofFieldsDto proof_fields;
    private String document_url;
}

package com.scb.loanOrigination.dto.documentChecker;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProofFieldsDto {
    private String heading;
    private List<String> proof_type;
    private List<ColumnDto> columns;
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
    private String user_id;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleNameEnum roleName;

    @Column(nullable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user_id", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List <LoanApplications> loanApplications;

    @OneToMany(mappedBy = "user_id", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Workflow> workflows;

    @OneToMany(mappedBy = "user_id", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Documents> documents;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Customers customer;

}

package com.scb.loanOrigination.exception;

public class MakerException extends RuntimeException{
    public MakerException(String message){super(message);}
}
package com.scb.loanOrigination.repository;


import com.scb.loanOrigination.entity.LoanApplications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<LoanApplications, Integer> {
}

package com.scb.loanOrigination.repository;


import com.scb.loanOrigination.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
}

package com.scb.loanOrigination.repository;


import com.scb.loanOrigination.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {
}

package com.scb.loanOrigination.service;

import com.scb.loanOrigination.entity.Documents;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.exception.MakerException;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public interface ILoanApplications {
    public LoanApplications getLoanRequestDetails(int loanId) throws MakerException;

    public String saveIDProofDetails(int loanId, String type, BigInteger IdNumber, String name, Date dob, Date issueDate, Date expiryDate, String issuingAuthority) throws MakerException;

    public String saveAddressProofDetails(int loanId, String type, String landLordName, String tenantName, String addressLine1, String addressLine2, String city, String state, int postalCode, String country, Date agreementStartDate, Date agreementEndDate) throws MakerException;

    public String saveIncomeProofDetails(int loanId, String type, String employer, String salaryMonth, int grossIncome, int netIncome, String bankName, int accountNumber, Date statementStart, Date statementEnd, double averageBalance) throws MakerException;

    public String saveEmploymentProofDetails(int loanId, String type, String employer, String designation, Date joiningDate, String employeeID) throws MakerException;

    public String saveLoanFormDetails(int loanId, String type, String applicantName, String country, String currency, String loanType, int amount, int loanTenure) throws MakerException;

    public String flagDocument(int loanId, String type) throws MakerException;

    public String flagLoanRequest(int loanId);

    public String approveLoanRequest(int loanId);

//    checker
    public List<Documents> fetchWorkItemDocumentDetails(int loanId);
}
package com.scb.loanOrigination.service;


import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.MakerException;


public interface IWorkflow {

    public Workflow getWorkflowDetails(int workflowId) throws MakerException;
}

package com.scb.loanOrigination.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.loanOrigination.entity.Documents;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.exception.CheckerException;
import com.scb.loanOrigination.exception.MakerException;
import com.scb.loanOrigination.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LoanApplicationsServiceImp implements ILoanApplications{
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private LoanRepository loanRepo;
    public LoanApplications getLoanRequestDetails(int loanId) throws MakerException
    {
        if(loanRepo.existsById(loanId))
        {
            return loanRepo.findById(loanId).get();
        }
        else
        {
            throw new MakerException("No LoanApplication with Loan ID: "+loanId+" found");
        }
    }

    public String saveIDProofDetails(int loanId, String type, BigInteger IdNumber, String name, Date dob, Date issueDate, Date expiryDate, String issuingAuthority) throws MakerException
    {
        String result="";
        LoanApplications loan = getLoanRequestDetails(loanId);
        List<Documents> documents = loan.getDocuments();
        for(Documents doc:documents)
        {
            ObjectMapper mapper = new ObjectMapper();
            if(doc.getDocumentName().equals(type))
            {
                String jsonFilePath = doc.getEntriesFilePath();
                File JsonFile = new File(jsonFilePath);

                try {
                    // Read the existing JSON file into a Map
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = mapper.readValue(JsonFile, java.util.Map.class);

                    // Update the values with the new data
                    data.put("IDNumber", IdNumber);
                    data.put("name", name);
                    data.put("dateOfBirth", dob);
                    data.put("issueDate", issueDate);
                    data.put("expiryDate", expiryDate);
                    data.put("issuingAuthority", issuingAuthority);

                    // Write the updated Map back to the JSON file
                    mapper.writerWithDefaultPrettyPrinter().writeValue(JsonFile, data);

                    result=type+" details successfully updated for loanId: " + loanId;

                } catch (IOException e) {
                    // Handle possible exceptions, such as file not found or invalid JSON
                    e.printStackTrace();
                    throw new MakerException("Failed to save "+type+" details: " + e.getMessage());
                }
            }
        }
        if(result.isEmpty())
        {
            throw new MakerException("No "+type+" document found for loanId: " + loanId);
        }
        return result;

    }

    public String saveAddressProofDetails(int loanId, String type, String landLordName, String tenantName, String addressLine1, String addressLine2, String city, String state, int postalCode, String country, Date agreementStartDate, Date agreementEndDate) throws MakerException
    {
        String result="";
        LoanApplications loan = getLoanRequestDetails(loanId);
        List<Documents> documents = loan.getDocuments();
        for(Documents doc:documents)
        {
            ObjectMapper mapper = new ObjectMapper();
            if(doc.getDocumentName().equals(type))
            {
                String jsonFilePath = doc.getEntriesFilePath();
                File JsonFile = new File(jsonFilePath);

                try {
                    // Read the existing JSON file into a Map
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = mapper.readValue(JsonFile, java.util.Map.class);

                    // Update the values with the new data
                    data.put("landlordName", landLordName);
                    data.put("tenantName", tenantName);
                    data.put("addressLine1", addressLine1);
                    data.put("addressLine2", addressLine2);
                    data.put("city", city);
                    data.put("state", state);
                    data.put("postalCode", postalCode);
                    data.put("country", country);
                    data.put("agreementStartDate", agreementStartDate);
                    data.put("agreementEndDate", agreementEndDate);

                    // Write the updated Map back to the JSON file
                    mapper.writerWithDefaultPrettyPrinter().writeValue(JsonFile, data);

                    result=type+" details successfully updated for loanId: " + loanId;

                } catch (IOException e) {
                    // Handle possible exceptions, such as file not found or invalid JSON
                    e.printStackTrace();
                    throw new MakerException("Failed to save "+type+" details: " + e.getMessage());
                }
            }
        }
        if(result.isEmpty())
        {
            throw new MakerException("No "+type+" document found for loanId: " + loanId);
        }
        return result;
    }

    public String saveIncomeProofDetails(int loanId, String type, String employer, String salaryMonth, int grossIncome, int netIncome, String bankName, int accountNumber, Date statementStart, Date statementEnd, double averageBalance) throws MakerException
    {
        String result="";
        LoanApplications loan = getLoanRequestDetails(loanId);
        List<Documents> documents = loan.getDocuments();
        for(Documents doc:documents)
        {
            ObjectMapper mapper = new ObjectMapper();
            if(doc.getDocumentName().equals(type))
            {
                String jsonFilePath = doc.getEntriesFilePath();
                File JsonFile = new File(jsonFilePath);

                try {
                    // Read the existing JSON file into a Map
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = mapper.readValue(JsonFile, java.util.Map.class);

                    // Update the values with the new data
                    data.put("employer", employer);
                    data.put("salaryMonth", salaryMonth);
                    data.put("grossIncome", grossIncome);
                    data.put("netIncome", netIncome);
                    data.put("bankName", bankName);
                    data.put("accountNumber", accountNumber);
                    data.put("statementStart", statementStart);
                    data.put("statementEnd", statementEnd);
                    data.put("averageBalance", averageBalance);

                    // Write the updated Map back to the JSON file
                    mapper.writerWithDefaultPrettyPrinter().writeValue(JsonFile, data);

                    result=type+" details successfully updated for loanId: " + loanId;

                } catch (IOException e) {
                    // Handle possible exceptions, such as file not found or invalid JSON
                    e.printStackTrace();
                    throw new MakerException("Failed to save "+type+" details: " + e.getMessage());
                }
            }
        }
        if(result.isEmpty())
        {
            throw new MakerException("No "+type+" document found for loanId: " + loanId);
        }
        return result;
    }

    public String saveEmploymentProofDetails(int loanId, String type, String employer, String designation, Date joiningDate, String employeeID) throws MakerException
    {
        String result="";
        LoanApplications loan = getLoanRequestDetails(loanId);
        List<Documents> documents = loan.getDocuments();
        for(Documents doc:documents)
        {
            ObjectMapper mapper = new ObjectMapper();
            if(doc.getDocumentName().equals(type))
            {
                String jsonFilePath = doc.getEntriesFilePath();
                File JsonFile = new File(jsonFilePath);

                try {
                    // Read the existing JSON file into a Map
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = mapper.readValue(JsonFile, java.util.Map.class);

                    // Update the values with the new data
                    data.put("employer", employer);
                    data.put("designation", designation);
                    data.put("joiningDate", joiningDate);
                    data.put("employeeID", employeeID);

                    // Write the updated Map back to the JSON file
                    mapper.writerWithDefaultPrettyPrinter().writeValue(JsonFile, data);

                    result=type+" details successfully updated for loanId: " + loanId;

                } catch (IOException e) {
                    // Handle possible exceptions, such as file not found or invalid JSON
                    e.printStackTrace();
                    throw new MakerException("Failed to save "+type+" details: " + e.getMessage());
                }
            }
        }
        if(result.isEmpty())
        {
            throw new MakerException("No "+type+" document found for loanId: " + loanId);
        }
        return result;
    }

    public String saveLoanFormDetails(int loanId, String type, String applicantName, String country, String currency, String loanType, int amount, int loanTenure) throws MakerException
    {
        String result="";
        LoanApplications loan = getLoanRequestDetails(loanId);
        List<Documents> documents = loan.getDocuments();
        for(Documents doc:documents)
        {
            ObjectMapper mapper = new ObjectMapper();
            if(doc.getDocumentName().equals(type))
            {
                String jsonFilePath = doc.getEntriesFilePath();
                File JsonFile = new File(jsonFilePath);

                try {
                    // Read the existing JSON file into a Map
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = mapper.readValue(JsonFile, java.util.Map.class);

                    // Update the values with the new data
                    data.put("applicantName", applicantName);
                    data.put("country", country);
                    data.put("currency", currency);
                    data.put("loanType", loanType);
                    data.put("amount", amount);
                    data.put("loanTenure", loanTenure);

                    // Write the updated Map back to the JSON file
                    mapper.writerWithDefaultPrettyPrinter().writeValue(JsonFile, data);

                    result=type+" details successfully updated for loanId: " + loanId;

                } catch (IOException e) {
                    // Handle possible exceptions, such as file not found or invalid JSON
                    e.printStackTrace();
                    throw new MakerException("Failed to save "+type+" details: " + e.getMessage());
                }
            }
        }
        if(result.isEmpty())
        {
            throw new MakerException("No "+type+" document found for loanId: " + loanId);
        }
        return result;
    }

    public String flagDocument(int loanId, String type) throws MakerException
    {
        String result="";
        LoanApplications loan = getLoanRequestDetails(loanId);
        List<Documents> documents = loan.getDocuments();
        for(Documents doc:documents)
        {
            if(doc.getDocumentName().equals(type))
            {
                doc.setFlag(true);
                result="Document "+type+" flagged";
                break;
            }
        }
        if(result.isEmpty())
        {
            throw new MakerException("No "+type+" document found for loanId: " + loanId);
        }
        loanRepo.save(loan);
        return result;
    }

    public String flagLoanRequest(int loanId)
    {
        LoanApplications loan = getLoanRequestDetails(loanId);
//        loan.setStatus("Flagged for ReUpload");
        loanRepo.save(loan);
        return "Loan request "+loanId+ " flagged";
    }

    public String approveLoanRequest(int loanId)
    {
        LoanApplications loan = getLoanRequestDetails(loanId);
//        loan.setStatus("Approved");
        loanRepo.save(loan);
        return "Loan request "+loanId+ " approved";
    }

//    Checker APIs
    public List<Documents> fetchWorkItemDocumentDetails(int loanId){
        Optional<LoanApplications> optionalLoan = loanRepo.findById(loanId);

        if (optionalLoan.isEmpty()) {
            throw new CheckerException("Loan ID doesn't exist");
        }

        LoanApplications reqLoan = optionalLoan.get();
        return reqLoan.getDocuments();
    }
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
package com.scb.loanOrigination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoanOrigination {

	public static void main(String[] args) {
		SpringApplication.run(LoanOrigination.class, args);
	}
}
