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
