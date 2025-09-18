package com.scb.loanOrigination.repository;

import com.scb.loanOrigination.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {

    @Query(value = """
        SELECT w.workflow_id,
               l.loan_id,
               l.user_id,
               concat(c.first_name, ' ', c.last_name) AS applicant_name,
               w.created_at,
               w.updated_at,
               w.status,
               COALESCE(SUM(CASE WHEN d.flag = true THEN 1 ELSE 0 END), 0) AS flags_count,
               COALESCE(json_agg(d.comment) FILTER (WHERE d.flag = true), '[]'::json) AS remarks
          FROM workflow w
          JOIN loan_applications l ON w.loan_id = l.loan_id
          JOIN customers c ON c.user_id = l.user_id
          LEFT JOIN documents d ON d.loan_id = l.loan_id
         WHERE w.step_name = 'Maker'
      GROUP BY w.workflow_id, l.loan_id, l.user_id,
               c.first_name, c.last_name, w.created_at, w.updated_at, w.status
        """,
        nativeQuery = true)
    List<Object[]> findMakerInbox();
}


package com.scb.loanOrigination.service;

import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.exception.MakerException;
import java.util.List;
import java.util.Map;

public interface IWorkflow {
    public Workflow getWorkflowDetails(int workflowId) throws MakerException;

    // new: returns Maker inbox rows (no status param)
    List<Map<String, Object>> getMakerInbox();
}


package com.scb.loanOrigination.service;

import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.repository.WorkflowRepository;
import com.scb.loanOrigination.exception.MakerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowServiceImp implements IWorkflow {

    @Autowired
    private WorkflowRepository workflowRepo;

    // ... other methods (getWorkflowDetails etc.) remain

    @Override
    @Transactional
    public List<Map<String, Object>> getMakerInbox() {
        List<Object[]> rows = workflowRepo.findMakerInbox();
        List<Map<String, Object>> out = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Object[] r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("workflowId", r[0] == null ? null : ((Number) r[0]).intValue());
            m.put("loanId", r[1] == null ? null : ((Number) r[1]).intValue());
            m.put("userId", r[2] == null ? null : r[2].toString());
            m.put("applicantName", r[3] == null ? "" : r[3].toString());
            m.put("createdAt", r[4] == null ? null : r[4].toString());
            m.put("updatedAt", r[5] == null ? null : r[5].toString());
            m.put("status", r[6] == null ? null : r[6].toString());
            m.put("flagsCount", r[7] == null ? 0 : ((Number) r[7]).intValue());

            // r[8] -> 'remarks' JSON array
            List<String> remarksList = new ArrayList<>();
            Object remarksObj = r.length > 8 ? r[8] : null;

            try {
                if (remarksObj != null) {
                    String json = null;
                    if (remarksObj instanceof String) {
                        json = (String) remarksObj;
                    } else if (remarksObj instanceof org.postgresql.util.PGobject) {
                        json = ((org.postgresql.util.PGobject) remarksObj).getValue();
                    } else if (remarksObj instanceof Array) {
                        Array sqlArr = (Array) remarksObj;
                        Object[] arr = (Object[]) sqlArr.getArray();
                        for (Object ao : arr) if (ao != null) remarksList.add(ao.toString());
                        m.put("remarks", remarksList);
                        out.add(m);
                        continue;
                    } else {
                        json = remarksObj.toString();
                    }

                    if (json != null && !json.isBlank()) {
                        remarksList = objectMapper.readValue(json, new TypeReference<List<String>>() {});
                    }
                }
            } catch (Exception ex) {
                // fallback: put raw string if parsing fails
                remarksList = new ArrayList<>();
                if (remarksObj != null) remarksList.add(remarksObj.toString());
            }

            m.put("remarks", remarksList);
            out.add(m);
        }

        return out;
    }
}


@RestController
@CrossOrigin(origins = "*")
public class MakerController {

    @Autowired
    private WorkflowServiceImp workflowService;

    // other endpoints...

    // New Maker inbox endpoint (no status parameter)
    @GetMapping("/makerInbox")
    public List<Map<String, Object>> getMakerInbox() {
        return workflowService.getMakerInbox();
    }
}


-- ============= USER 1 ============
INSERT INTO users (user_id, password, role_name, created_at)
VALUES ('user100','pass123','Customer', NOW());

INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number)
VALUES ('user100', '123 Main St, City', '1990-01-01', 'M', 'john.doe@example.com', 'John', 'Doe', 'ABCDE1234F', 123456789012, 9876543210);

INSERT INTO loan_applications (loan_id, amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (100, 500000, 'INR', 24, 7.5, 'Initiated', NOW(), 'user100');

-- multiple flagged documents for loan 100
INSERT INTO documents (document_id, document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES (1, 'ID_PROOF', 'id_100.jpg', '/files/id_100.jpg', '/entries/id_100.csv', NOW()-interval '3 days', 'Flagged_For_ReUpload', true, 'ID blurry - reupload required', 'user100', 100),
       (7, 'PAN_PROOF', 'pan_100.pdf', '/files/pan_100.pdf', '/entries/pan_100.csv', NOW()-interval '2 days', 'Flagged_For_ReUpload', true, 'PAN mismatch - last 4 digits', 'user100', 100),
       (8, 'SIGN_PROOF', 'sign_100.jpg', '/files/sign_100.jpg', '/entries/sign_100.csv', NOW()-interval '1 days', 'Flagged_For_ReUpload', true, 'Signature unclear', 'user100', 100),
       (2, 'Address_PROOF', 'addr_100.jpg', '/files/addr_100.jpg', '/entries/addr_100.csv', NOW(), 'Approved', false, '', 'user100', 100);

INSERT INTO workflow (workflow_id, step_name, status, created_at, updated_at, remarks, user_id, loan_id)
VALUES (1, 'Maker', 'Flagged_For_ReUpload', NOW()-interval '3 days', NOW(), 'Please re-check documents', 'user100', 100);


-- ============= USER 2 ============
INSERT INTO users (user_id, password, role_name, created_at)
VALUES ('user200','pass456','Customer', NOW());

INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number)
VALUES ('user200', '456 Second St, City', '1992-05-15', 'F', 'jane.smith@example.com', 'Jane', 'Smith', 'PQRSX6789Z', 234567890123, 9876501234);

INSERT INTO loan_applications (loan_id, amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
VALUES (200, 1000000, 'INR', 36, 8.2, 'Initiated', NOW(), 'user200');

INSERT INTO documents (document_id, document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
VALUES (3, 'Income_PROOF', 'income_200.pdf', '/files/income_200.pdf', '/entries/income_200.csv', NOW()-interval '2 days', 'Approved', false, '', 'user200', 200),
       (4, 'Employment_PROOF', 'emp_200.pdf', '/files/emp_200.pdf', '/entries/emp_200.csv', NOW()-interval '1 days', 'Flagged_For_ReUpload', true, 'Joining date mismatch', 'user200', 200);

INSERT INTO workflow (workflow_id, step_name, status, created_at, updated_at, remarks, user_id, loan_id)
VALUES (2, 'Maker', 'Flagged_For_ReUpload', NOW()-interval '2 days', NOW(), 'Check employment proof', 'user200', 200);


-- ============= USER 3 (no flags) ============
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


GET http://localhost:8080/makerInbox
