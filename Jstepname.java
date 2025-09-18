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

9-18T17:16:34.361+05:30  INFO 34752 --- [           main] c.scb.loanOrigination.LoanOrigination    : Starting LoanOrigination using Java 17.0.8 with PID 34752 (C:\Users\2030304\Repo_Capstone_Project\99999-grad-elbrus-loan-origination-repo\Backend\target\classes started by 2030304 in C:\Users\2030304\Repo_Capstone_Project\99999-grad-elbrus-loan-origination-repo\Backend)
2025-09-18T17:16:34.366+05:30  INFO 34752 --- [           main] c.scb.loanOrigination.LoanOrigination    : No active profile set, falling back to 1 default profile: "default"
2025-09-18T17:16:35.924+05:30  INFO 34752 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-09-18T17:16:36.033+05:30  INFO 34752 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 92 ms. Found 3 JPA repository interfaces.
2025-09-18T17:16:36.841+05:30  INFO 34752 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-09-18T17:16:36.860+05:30  INFO 34752 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-09-18T17:16:36.861+05:30  INFO 34752 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.20]
2025-09-18T17:16:36.955+05:30  INFO 34752 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-09-18T17:16:36.957+05:30  INFO 34752 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2505 ms
2025-09-18T17:16:37.272+05:30  INFO 34752 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-09-18T17:16:37.371+05:30  INFO 34752 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.4.Final
2025-09-18T17:16:37.428+05:30  INFO 34752 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-09-18T17:16:37.718+05:30  INFO 34752 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-09-18T17:16:37.750+05:30  INFO 34752 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-09-18T17:16:37.897+05:30  INFO 34752 --- [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@5e193ef5
2025-09-18T17:16:37.900+05:30  INFO 34752 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-09-18T17:16:39.372+05:30  INFO 34752 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: alter table if exists customers drop constraint if exists FKrh1g1a20omjmn6kurd35o3eit
2025-09-18T17:16:39.391+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.392+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : relation "customers" does not exist, skipping
Hibernate: alter table if exists documents drop constraint if exists FK5po5skiro6gtj5kv771yjj1fg
2025-09-18T17:16:39.392+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.392+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : relation "documents" does not exist, skipping
Hibernate: alter table if exists loan_applications drop constraint if exists FKmkoa5awuujoadi1bvfvkl05ee
2025-09-18T17:16:39.394+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.394+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : relation "loan_applications" does not exist, skipping
Hibernate: alter table if exists workflow drop constraint if exists FK7kht9sdfdufaybqs13uul5f89
2025-09-18T17:16:39.396+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.396+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : relation "workflow" does not exist, skipping
Hibernate: drop table if exists customers cascade
2025-09-18T17:16:39.397+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.397+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "customers" does not exist, skipping
Hibernate: drop table if exists documents cascade
2025-09-18T17:16:39.398+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.398+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "documents" does not exist, skipping
Hibernate: drop table if exists loan_applications cascade
2025-09-18T17:16:39.400+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.400+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "loan_applications" does not exist, skipping
Hibernate: drop table if exists users cascade
2025-09-18T17:16:39.400+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.400+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "users" does not exist, skipping
Hibernate: drop table if exists workflow cascade
2025-09-18T17:16:39.401+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2025-09-18T17:16:39.401+05:30  WARN 34752 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : table "workflow" does not exist, skipping
Hibernate: create table customers (aadhaar numeric(38,0) not null, gender char(1) not null, mobile_number numeric(38,0) not null, dob timestamp(6) not null, address varchar(255) not null, email varchar(255) not null, first_name varchar(255) not null, last_name varchar(255) not null, pan varchar(255) not null, user_id varchar(255) not null, primary key (user_id))
Hibernate: create table documents (flag boolean not null, loan_id integer not null, document_id bigserial not null, uploaded_at timestamp(6) not null, comment varchar(255) not null, document_name varchar(255) not null, entries_file_path varchar(255) not null, file_name varchar(255) not null, file_path varchar(255) not null, status varchar(255) not null check (status in ('Flagged_For_ReUpload','Moved_To_Checker','Approved','Flagged_For_Data_ReEntry')), user_id varchar(255) not null, primary key (document_id))
Hibernate: create table loan_applications (amount float(53) not null, interest_rate float(53) not null, loan_id serial not null, loan_tenure integer not null, created_at timestamp(6) not null, currency varchar(255) not null, status varchar(255) not null check (status in ('Initiated','In_Progress','Rejected','Approved')), user_id varchar(255) not null, primary key (loan_id))
Hibernate: create table users (created_at timestamp(6) not null, password varchar(255) not null, role_name varchar(255) not null check (role_name in ('Customer','Maker','Checker')), user_id varchar(255) not null, primary key (user_id))
Hibernate: create table workflow (loan_id integer unique, workflow_id serial not null, created_at timestamp(6) not null, updated_at timestamp(6) not null, remarks varchar(255), status varchar(255) not null check (status in ('Pending','Flagged_For_ReUpload','Moved_To_Checker','Approved','Flagged_For_Data_ReEntry')), step_name varchar(255) not null check (step_name in ('Maker','Checker','Approval')), user_id varchar(255), primary key (workflow_id))
Hibernate: alter table if exists customers add constraint FKrh1g1a20omjmn6kurd35o3eit foreign key (user_id) references users
Hibernate: alter table if exists documents add constraint FK5po5skiro6gtj5kv771yjj1fg foreign key (loan_id) references loan_applications
Hibernate: alter table if exists loan_applications add constraint FKmkoa5awuujoadi1bvfvkl05ee foreign key (user_id) references customers
Hibernate: alter table if exists workflow add constraint FK7kht9sdfdufaybqs13uul5f89 foreign key (loan_id) references loan_applications
2025-09-18T17:16:39.503+05:30  INFO 34752 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-09-18T17:16:39.919+05:30  INFO 34752 --- [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2025-09-18T17:16:40.041+05:30  WARN 34752 --- [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2025-09-18T17:16:40.611+05:30  INFO 34752 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2025-09-18T17:16:40.626+05:30  INFO 34752 --- [           main] c.scb.loanOrigination.LoanOrigination    : Started LoanOrigination in 6.925 seconds (process running for 7.474)
2025-09-18T17:20:11.737+05:30  INFO 34752 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-09-18T17:20:11.737+05:30  INFO 34752 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-09-18T17:20:11.738+05:30  INFO 34752 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
Hibernate:   SELECT w.workflow_id,
         l.loan_id,
         l.user_id,
         concat(c.first_name, ' ', c.last_name) AS applicant_name,
         w.created_at,
         w.updated_at,
         w.status,
         COALESCE(SUM(CASE WHEN d.flag = true THEN 1 ELSE 0 END), 0) AS flags_count,
         COALESCE(json_agg(d.comment) FILTER (WHERE d.flag = true), '[]':json) AS remarks
    FROM workflow w
    JOIN loan_applications l ON w.loan_id = l.loan_id
    JOIN customers c ON c.user_id = l.user_id
    LEFT JOIN documents d ON d.loan_id = l.loan_id
   WHERE w.step_name = 'Maker'
GROUP BY w.workflow_id, l.loan_id, l.user_id,
         c.first_name, c.last_name, w.created_at, w.updated_at, w.status

2025-09-18T17:20:11.852+05:30  WARN 34752 --- [nio-8080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42601
2025-09-18T17:20:11.853+05:30 ERROR 34752 --- [nio-8080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: syntax error at or near ":"
  Position: 354
2025-09-18T17:20:11.863+05:30 ERROR 34752 --- [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.dao.InvalidDataAccessResourceUsageException: JDBC exception executing SQL [  SELECT w.workflow_id,
         l.loan_id,
         l.user_id,
         concat(c.first_name, ' ', c.last_name) AS applicant_name,
         w.created_at,
         w.updated_at,
         w.status,
         COALESCE(SUM(CASE WHEN d.flag = true THEN 1 ELSE 0 END), 0) AS flags_count,
         COALESCE(json_agg(d.comment) FILTER (WHERE d.flag = true), '[]':json) AS remarks
    FROM workflow w
    JOIN loan_applications l ON w.loan_id = l.loan_id
    JOIN customers c ON c.user_id = l.user_id
    LEFT JOIN documents d ON d.loan_id = l.loan_id
   WHERE w.step_name = 'Maker'
GROUP BY w.workflow_id, l.loan_id, l.user_id,
         c.first_name, c.last_name, w.created_at, w.updated_at, w.status
] [ERROR: syntax error at or near ":"
  Position: 354] [n/a]; SQL [n/a]] with root cause

org.postgresql.util.PSQLException: ERROR: syntax error at or near ":"
  Position: 354
	at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2675) ~[postgresql-42.3.10.jar:42.3.10]
	at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2365) ~[postgresql-42.3.10.jar:42.3.10]
	at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:355) ~[postgresql-42.3.10.jar:42.3.10]
	at org.postgresql.jdbc.PgStatement.executeInternal(PgStatement.java:490) ~[postgresql-42.3.10.jar:42.3.10]
	at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:408) ~[postgresql-42.3.10.jar:42.3.10]
	at org.postgresql.jdbc.PgPreparedStatement.executeWithFlags(PgPreparedStatement.java:167) ~[postgresql-42.3.10.jar:42.3.10]
	at org.postgresql.jdbc.PgPreparedStatement.executeQuery(PgPreparedStatement.java:119) ~[postgresql-42.3.10.jar:42.3.10]
	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeQuery(ProxyPreparedStatement.java:52) ~[HikariCP-5.0.1.jar:na]
	at com.zaxxer.hikari.pool.HikariProxyPreparedStatement.executeQuery(HikariProxyPreparedStatement.java) ~[HikariCP-5.0.1.jar:na]
	at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.executeQuery(DeferredResultSetAccess.java:246) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.sql.results.jdbc.internal.DeferredResultSetAccess.getResultSet(DeferredResultSetAccess.java:167) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.sql.results.jdbc.internal.AbstractResultSetAccess.getMetaData(AbstractResultSetAccess.java:36) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.sql.results.jdbc.internal.AbstractResultSetAccess.getColumnCount(AbstractResultSetAccess.java:52) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.results.ResultSetMappingImpl.resolve(ResultSetMappingImpl.java:193) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.resolveJdbcValuesSource(JdbcSelectExecutorStandardImpl.java:325) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:115) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:83) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:76) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:65) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.sql.internal.NativeSelectQueryPlanImpl.performList(NativeSelectQueryPlanImpl.java:138) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.sql.internal.NativeQueryImpl.doList(NativeQueryImpl.java:621) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.spi.AbstractSelectionQuery.list(AbstractSelectionQuery.java:427) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.Query.getResultList(Query.java:120) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.springframework.data.jpa.repository.query.JpaQueryExecution$CollectionExecution.doExecute(JpaQueryExecution.java:129) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.query.JpaQueryExecution.execute(JpaQueryExecution.java:92) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.query.AbstractJpaQuery.doExecute(AbstractJpaQuery.java:149) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.query.AbstractJpaQuery.execute(AbstractJpaQuery.java:137) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:170) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:158) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:164) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:143) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(DefaultMethodInvokingMethodInterceptor.java:70) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:123) ~[spring-tx-6.1.6.jar:6.1.6]
	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:392) ~[spring-tx-6.1.6.jar:6.1.6]
	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119) ~[spring-tx-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:137) ~[spring-tx-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor$CrudMethodMetadataPopulatingMethodInterceptor.invoke(CrudMethodMetadataPostProcessor.java:135) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:223) ~[spring-aop-6.1.6.jar:6.1.6]
	at jdk.proxy2/jdk.proxy2.$Proxy125.findMakerInbox(Unknown Source) ~[na:na]
	at com.scb.loanOrigination.service.WorkflowServiceImp.getMakerInbox(WorkflowServiceImp.java:96) ~[classes/:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:354) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:123) ~[spring-tx-6.1.6.jar:6.1.6]
	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:392) ~[spring-tx-6.1.6.jar:6.1.6]
	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119) ~[spring-tx-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.6.jar:6.1.6]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:720) ~[spring-aop-6.1.6.jar:6.1.6]
	at com.scb.loanOrigination.service.WorkflowServiceImp$$SpringCGLIB$$0.getMakerInbox(<generated>) ~[classes/:na]
	at com.scb.loanOrigination.controller.MakerController.getMakerInbox(MakerController.java:115) ~[classes/:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:255) ~[spring-web-6.1.6.jar:6.1.6]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:188) ~[spring-web-6.1.6.jar:6.1.6]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:926) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:831) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564) ~[tomcat-embed-core-10.1.20.jar:6.0]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.1.6.jar:6.1.6]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.20.jar:6.0]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:206) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:150) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:175) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:150) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.1.6.jar:6.1.6]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.1.6.jar:6.1.6]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:175) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:150) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.1.6.jar:6.1.6]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.1.6.jar:6.1.6]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:175) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:150) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.1.6.jar:6.1.6]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.1.6.jar:6.1.6]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:175) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:150) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:391) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:896) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1736) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.20.jar:10.1.20]
	at java.base/java.lang.Thread.run(Thread.java:833) ~[na:na]

     @RequestMapping(value = "/makerInbox", method = RequestMethod.GET)
    public List<Map<String, Object>> getMakerInbox() {
        return workflowService.getMakerInbox();
    }

{
    "timestamp": "2025-09-18T11:50:11.876+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/makerInbox"
}


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
