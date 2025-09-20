import React, { useState, useEffect } from "react";
import { Container, Card } from "react-bootstrap";
import Navbar from "../components/Navbar";
import Filters from "../components/Filters";
import TransactionsTable from "../components/TransactionTable";
import { FaInbox } from "react-icons/fa";

export default function MakerInboxPage() {
  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(1);
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch data from backend
  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const res = await fetch("http://localhost:8080/api/workflows/makerInbox");
        const json = await res.json();

        // Map backend response into frontend-friendly shape
        const mapped = json.map((w) => ({
          transactionRef: w.workflowId,   // backend workflowId
          loanId: w.loanId,
          assignedTo: w.userId,
          applicant: w.applicantName,
          createdAt: w.createdAt ? w.createdAt.split("T")[0] : "", // only date
          updatedAt: w.updatedAt ? w.updatedAt.split("T")[0] : "", // only date
          status: w.status,
          flags: Array.from({ length: w.flagsCount || 0 }).map((_, i) => ({
            type: "Document",
            message: "Flagged document",
          })),
          remarks: w.remarks
        }));

        setData(mapped);
      } catch (err) {
        console.error("Error fetching data", err);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  function handleApplyFilters(f) { setFilters(f); setPage(1); }
  function handleResetFilters() { setFilters({}); setPage(1); }
  function handlePageChange(p) { setPage(p); }
  function handleView(txn) { alert(`Viewing Workflow ${txn.transactionRef} (Loan ${txn.loanId})`); }

  // Apply filters
  let filteredData = data.filter((t) => {
    if (filters.search) {
      const s = filters.search.toLowerCase();
      if (
        !(
          String(t.transactionRef).toLowerCase().includes(s) ||
          String(t.loanId).toLowerCase().includes(s) ||
          t.applicant.toLowerCase().includes(s)
        )
      ) return false;
    }
    return true;
  });

  const pageSize = 5;
  const totalPages = Math.ceil(filteredData.length / pageSize);
  const pagedData = filteredData.slice((page - 1) * pageSize, page * pageSize);

  return (
    <div style={{
      background: "linear-gradient(180deg,#0055AA 0%,#004488 100%)",
      minHeight: "100vh"
    }}>
      <Navbar />

      <Container fluid className="py-4 px-4">
        <Card className="shadow-lg border-0 rounded-3">
          <Card.Header
            className="d-flex justify-content-between align-items-center text-white"
            style={{
              background: "linear-gradient(90deg,#003366 0%,#005599 100%)",
            }}
          >
            <h5 className="mb-0 d-flex align-items-center gap-2">
              <FaInbox /> Maker's Inbox
            </h5>
            <small>Loan Applications Overview</small>
          </Card.Header>

          <Card.Body>
            <Filters onApply={handleApplyFilters} onReset={handleResetFilters} />
            <hr />
            <TransactionsTable
              data={pagedData}
              loading={loading}
              page={page}
              pages={totalPages}
              onPageChange={handlePageChange}
              onView={handleView}
              headerStyle={{
                background: "linear-gradient(90deg,#003366 0%,#005599 100%)",
                color: "#fff",
                fontWeight:"bold"
              }}
            />
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}


{hasFlags && (
  <tr>
    <td colSpan="9" className="p-0">
      <Alert variant="danger" className="mb-0 p-2 small">
        <strong>Re-upload required:</strong>
        <ul className="mb-0">
          {txn.flags.map((flag, i) => (
            <li key={i}>
              <strong>{flag.type}</strong> - {flag.message}
            </li>
          ))}
        </ul>
        {txn.remarks && <p className="mt-2"><strong>Remarks:</strong> {txn.remarks}</p>}
      </Alert>
    </td>
  </tr>
)}


Iteration 2
import React, { useState, useEffect } from "react";
import { Container, Card } from "react-bootstrap";
import Navbar from "../components/Navbar";
import Filters from "../components/Filters";
import TransactionsTable from "../components/TransactionTable";
import { FaInbox } from "react-icons/fa";

export default function MakerInboxPage() {
  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(1);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch data from backend
  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const res = await fetch("http://localhost:8080/api/workflows/makerInbox");
        const data = await res.json();

        // Map API response to UI-friendly format
        const mappedData = data.map((item, i) => ({
          transactionRef: "TXN" + (1000 + i), // if API has no workItemId, generate one
          loanId: item.loanId,
          assignedTo: item.userId,
          applicant: item.applicantName,
          createdAt: item.createdAt?.split("T")[0] || item.createdAt,
          updatedAt: item.updatedAt?.split("T")[0] || item.updatedAt,
          status: item.status.toUpperCase(),
          flags: item.flagsCount > 0 
            ? [{ type: "ID_PROOF", message: item.remarks || "Issue found" }] 
            : []
        }));

        setTransactions(mappedData);
      } catch (error) {
        console.error("Error fetching maker inbox:", error);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  function handleApplyFilters(f) { setFilters(f); setPage(1); }
  function handleResetFilters() { setFilters({}); setPage(1); }
  function handlePageChange(p) { setPage(p); }
  function handleView(txn) { alert(`Viewing ${txn.transactionRef} (${txn.loanId})`); }

  // Apply filters
  let filteredData = transactions.filter((t) => {
    if (filters.search) {
      const s = filters.search.toLowerCase();
      if (
        !(
          t.transactionRef.toLowerCase().includes(s) ||
          t.loanId.toLowerCase().includes(s) ||
          t.applicant.toLowerCase().includes(s)
        )
      )
        return false;
    }
    return true;
  });

  const pageSize = 5;
  const totalPages = Math.ceil(filteredData.length / pageSize) || 1;
  const pagedData = filteredData.slice((page - 1) * pageSize, page * pageSize);

  return (
    <div style={{
      background: "linear-gradient(180deg,#0055AA 0%,#004488 100%)",
      minHeight: "100vh"
    }}>
      <Navbar />

      <Container fluid className="py-4 px-4">
        <Card className="shadow-lg border-0 rounded-3">
          <Card.Header
            className="d-flex justify-content-between align-items-center text-white"
            style={{
              background: "linear-gradient(90deg,#003366 0%,#005599 100%)",
            }}
          >
            <h5 className="mb-0 d-flex align-items-center gap-2">
              <FaInbox /> Maker's Inbox
            </h5>
            <small>Loan Applications Overview</small>
          </Card.Header>

          <Card.Body>
            <Filters onApply={handleApplyFilters} onReset={handleResetFilters} />
            <hr />
            <TransactionsTable
              data={pagedData}
              loading={loading}
              page={page}
              pages={totalPages}
              onPageChange={handlePageChange}
              onView={handleView}
              headerStyle={{
                background: "linear-gradient(90deg,#003366 0%,#005599 100%)",
                color: "#fff",
                fontWeight:"bold"
              }}
            />
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}

import React from "react";
import { Button, Badge, Alert } from "react-bootstrap";
import { FaEye, FaExclamationCircle } from "react-icons/fa";

export default function TransactionRow({ txn, onView }) {
  const hasFlags = txn.flags && txn.flags.length > 0;

  // Map backend status enums to UI-friendly text + badge color
  function getStatusBadge(status) {
    switch (status) {
      case "Pending":
        return { text: "Pending", color: "warning" };
      case "Flagged_For_ReUpload":
        return { text: "Re-upload Required", color: "danger" };
      case "Moved_To_Checker":
        return { text: "In Progress", color: "info" };
      case "Approved":
        return { text: "Approved", color: "success" };
      default:
        return { text: status, color: "secondary" };
    }
  }

  const statusInfo = getStatusBadge(txn.status);

  return (
    <>
      <style>{`
        .view-btn {
          background-color: #005599;
          border-color: #005599;
          color: white;
          transition: all 0.3s ease;
        }
        .view-btn:hover {
          background-color: #003366 !important;
          color: white !important;
          border-color: #002244 !important;
        }
      `}</style>

      <tr className={hasFlags ? "table-danger" : ""} style={{ cursor: "pointer" }}>
        <td>{txn.transactionRef}</td>   {/* WorkItem ID */}
        <td>{txn.loanId}</td>          {/* Loan ID */}
        <td>{txn.assignedTo || "-"}</td> {/* User ID */}
        <td>{txn.applicant}</td>       {/* Applicant Name */}
        <td>{txn.createdAt}</td>       {/* Created At */}
        <td>{txn.updatedAt}</td>       {/* Updated At */}
        <td>
          <Badge bg={statusInfo.color}>{statusInfo.text}</Badge>
        </td>
        <td>
          {hasFlags ? (
            <Badge bg="danger">
              <FaExclamationCircle /> {txn.flags.length}
            </Badge>
          ) : (
            <Badge bg="secondary">0</Badge>
          )}
        </td>
        <td>
          <Button
            size="sm"
            className="view-btn"
            onClick={() => onView(txn)}
          >
            <FaEye /> View
          </Button>
        </td>
      </tr>

      {hasFlags && (
        <tr>
          <td colSpan="9" className="p-0">
            <Alert variant="danger" className="mb-0 p-2 small">
              <strong>Re-upload required:</strong>
              <ul className="mb-0">
                {txn.flags.map((flag, i) => (
                  <li key={i}>
                    <strong>{flag.type}</strong> - {flag.message}
                  </li>
                ))}
              </ul>
            </Alert>
          </td>
        </tr>
      )}
    </>
  );
}

import React from "react";
import { Button, Badge, Alert } from "react-bootstrap";
import { FaEye, FaExclamationCircle } from "react-icons/fa";

export default function TransactionRow({ txn, onView }) {
  const hasFlags = txn.flags && txn.flags.length > 0;

  // Direct mapping: just decide badge color, keep text same as backend
  function getStatusBadge(status) {
    switch (status) {
      case "Moved_To_Maker":
        return { text: "Moved_To_Maker", color: "warning" }; // 🟡
      case "Flagged_For_ReUpload":
        return { text: "Flagged_For_ReUpload", color: "danger" }; // 🔴
      case "Moved_To_Checker":
        return { text: "Moved_To_Checker", color: "info" }; // 🔵
      default:
        return { text: status, color: "secondary" };
    }
  }

  const statusInfo = getStatusBadge(txn.status);

  return (
    <>
      <style>{`
        .view-btn {
          background-color: #005599;
          border-color: #005599;
          color: white;
          transition: all 0.3s ease;
        }
        .view-btn:hover {
          background-color: #003366 !important;
          color: white !important;
          border-color: #002244 !important;
        }
      `}</style>

      <tr className={hasFlags ? "table-danger" : ""} style={{ cursor: "pointer" }}>
        <td>{txn.transactionRef}</td>
        <td>{txn.loanId}</td>
        <td>{txn.assignedTo || "-"}</td>
        <td>{txn.applicant}</td>
        <td>{txn.createdAt}</td>
        <td>{txn.updatedAt}</td>
        <td>
          <Badge bg={statusInfo.color}>{statusInfo.text}</Badge>
        </td>
        <td>
          {hasFlags ? (
            <Badge bg="danger">
              <FaExclamationCircle /> {txn.flags.length}
            </Badge>
          ) : (
            <Badge bg="secondary">0</Badge>
          )}
        </td>
        <td>
          <Button
            size="sm"
            className="view-btn"
            onClick={() => onView(txn)}
          >
            <FaEye /> View
          </Button>
        </td>
      </tr>

      {hasFlags && (
        <tr>
          <td colSpan="9" className="p-0">
            <Alert variant="danger" className="mb-0 p-2 small">
              <strong>Re-upload required:</strong>
              <ul className="mb-0">
                {txn.flags.map((flag, i) => (
                  <li key={i}>
                    <strong>{flag.type}</strong> - {flag.message}
                  </li>
                ))}
              </ul>
            </Alert>
          </td>
        </tr>
      )}
    </>
  );
}


J3
import React, { useState, useEffect } from "react";
import { Container, Card } from "react-bootstrap";
import Navbar from "../components/Navbar";
import Filters from "../components/Filters";
import TransactionsTable from "../components/TransactionTable";
import { FaInbox } from "react-icons/fa";

export default function MakerInboxPage() {
  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(1);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch data from backend
  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const res = await fetch("http://localhost:8080/api/workflows/makerInbox");
        const data = await res.json();

        // Map API response to UI-friendly format
        const mappedData = data.map((item) => ({
          workflowId: item.workflowId,   // use backend workflowId directly
          loanId: item.loanId,
          assignedTo: item.userId,
          applicant: item.applicantName,
          createdAt: item.createdAt?.split("T")[0] || item.createdAt, // only date
          updatedAt: item.updatedAt?.split("T")[0] || item.updatedAt, // only date
          status: item.status,  // keep backend value (do not uppercase!)
          flags: item.flagsCount > 0 
            ? [{ type: "ID_PROOF", message: item.remark || "Issue found" }] 
            : []
        }));

        setTransactions(mappedData);
      } catch (error) {
        console.error("Error fetching maker inbox:", error);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  function handleApplyFilters(f) { setFilters(f); setPage(1); }
  function handleResetFilters() { setFilters({}); setPage(1); }
  function handlePageChange(p) { setPage(p); }
  function handleView(txn) { alert(`Viewing Workflow ${txn.workflowId} (Loan ${txn.loanId})`); }

  // Apply filters
  let filteredData = transactions.filter((t) => {
    if (filters.search) {
      const s = filters.search.toLowerCase();
      if (
        !(
          String(t.workflowId).toLowerCase().includes(s) ||
          t.loanId.toLowerCase().includes(s) ||
          t.applicant.toLowerCase().includes(s)
        )
      )
        return false;
    }
    return true;
  });

  const pageSize = 5;
  const totalPages = Math.ceil(filteredData.length / pageSize) || 1;
  const pagedData = filteredData.slice((page - 1) * pageSize, page * pageSize);

  return (
    <div style={{
      background: "linear-gradient(180deg,#0055AA 0%,#004488 100%)",
      minHeight: "100vh"
    }}>
      <Navbar />

      <Container fluid className="py-4 px-4">
        <Card className="shadow-lg border-0 rounded-3">
          <Card.Header
            className="d-flex justify-content-between align-items-center text-white"
            style={{
              background: "linear-gradient(90deg,#003366 0%,#005599 100%)",
            }}
          >
            <h5 className="mb-0 d-flex align-items-center gap-2">
              <FaInbox /> Maker's Inbox
            </h5>
            <small>Loan Applications Overview</small>
          </Card.Header>

          <Card.Body>
            <Filters onApply={handleApplyFilters} onReset={handleResetFilters} />
            <hr />
            <TransactionsTable
              data={pagedData}
              loading={loading}
              page={page}
              pages={totalPages}
              onPageChange={handlePageChange}
              onView={handleView}
              headerStyle={{
                background: "linear-gradient(90deg,#003366 0%,#005599 100%)",
                color: "#fff",
                fontWeight:"bold"
              }}
            />
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}


import React from "react";
import { Button, Badge, Alert } from "react-bootstrap";
import { FaEye, FaExclamationCircle } from "react-icons/fa";

export default function TransactionRow({ txn, onView }) {
  const hasFlags = txn.flags && txn.flags.length > 0;

  // Keep backend status values, just assign colors
  function getStatusBadge(status) {
    switch (status) {
      case "Moved_To_Maker":
        return { text: "Moved_To_Maker", color: "warning" };
      case "Flagged_For_ReUpload":
        return { text: "Flagged_For_ReUpload", color: "danger" };
      case "Moved_To_Checker":
        return { text: "Moved_To_Checker", color: "info" };
      default:
        return { text: status, color: "secondary" };
    }
  }

  const statusInfo = getStatusBadge(txn.status);

  return (
    <>
      <tr className={hasFlags ? "table-danger" : ""} style={{ cursor: "pointer" }}>
        <td>{txn.workflowId}</td>
        <td>{txn.loanId}</td>
        <td>{txn.assignedTo || "-"}</td>
        <td>{txn.applicant}</td>
        <td>{txn.createdAt}</td>
        <td>{txn.updatedAt}</td>
        <td>
          <Badge bg={statusInfo.color}>{statusInfo.text}</Badge>
        </td>
        <td>
          {hasFlags ? (
            <Badge bg="danger">
              <FaExclamationCircle /> {txn.flags.length}
            </Badge>
          ) : (
            <Badge bg="secondary">0</Badge>
          )}
        </td>
        <td>
          <Button
            size="sm"
            className="view-btn"
            onClick={() => onView(txn)}
          >
            <FaEye /> View
          </Button>
        </td>
      </tr>

      {hasFlags && (
        <tr>
          <td colSpan="9" className="p-0">
            <Alert variant="danger" className="mb-0 p-2 small">
              <strong>Re-upload required:</strong>
              <ul className="mb-0">
                {txn.flags.map((flag, i) => (
                  <li key={i}>
                    <strong>{flag.type}</strong> - {flag.message}
                  </li>
                ))}
              </ul>
            </Alert>
          </td>
        </tr>
      )}
    </>
  );
}


package com.scb.loanOrigination.dto.makerInbox;

public class MakerInboxDTO {
    private Long workflowId;
    private Long loanId;
    private String userId;
    private String applicantName;
    private String status;
    private String remarks;
    private Integer flagsCount;
    private String createdAt;
    private String updatedAt;

    // ✅ Constructor (needed for JPA mapping)
    public MakerInboxDTO(Long workflowId, Long loanId, String userId,
                         String applicantName, String status, String remarks,
                         Integer flagsCount, String createdAt, String updatedAt) {
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

    // ✅ Getters
    public Long getWorkflowId() { return workflowId; }
    public Long getLoanId() { return loanId; }
    public String getUserId() { return userId; }
    public String getApplicantName() { return applicantName; }
    public String getStatus() { return status; }
    public String getRemarks() { return remarks; }
    public Integer getFlagsCount() { return flagsCount; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}

@Query("""
SELECT new com.scb.loanOrigination.dto.makerInbox.MakerInboxDTO(
       w.workflowId,
       w.loanId,
       w.userId,
       CONCAT(c.firstName, ' ', c.lastName),
       w.status,
       w.remarks,
       COUNT(d),
       CAST(w.createdAt AS string),
       CAST(w.updatedAt AS string)
)
FROM Workflow w
JOIN LoanApplications l ON w.loanId = l.loanId
JOIN Customers c ON l.userId = c.userId
LEFT JOIN Documents d ON d.loanId = w.loanId
WHERE w.stepName = 'Maker'
  AND w.status IN ('Moved_To_Maker','Flagged_For_ReUpload','Flagged_For_Data_ReEntry')
GROUP BY w.workflowId, w.loanId, w.userId, c.firstName, c.lastName,
         w.status, w.remarks, w.createdAt, w.updatedAt
""")
List<MakerInboxDTO> getMakerInbox();



public List<MakerInboxDTO> getMakerInbox() {
    return workflowRepo.getMakerInbox();
}

@GetMapping("/inbox")
public ResponseEntity<List<MakerInboxDTO>> getMakerInbox() {
    return ResponseEntity.ok(workflowService.getMakerInbox());
}

2025-09-21T01:46:23.438+05:30  INFO 38972 --- [           main] c.scb.loanOrigination.LoanOrigination    : Starting LoanOrigination using Java 17.0.8 with PID 38972 (C:\Users\2030304\JLoanOrigination_API\99999-grad-elbrus-loan-origination-repo\target\classes started by 2030304 in C:\Users\2030304\JLoanOrigination_API\99999-grad-elbrus-loan-origination-repo)
2025-09-21T01:46:23.439+05:30  INFO 38972 --- [           main] c.scb.loanOrigination.LoanOrigination    : No active profile set, falling back to 1 default profile: "default"
2025-09-21T01:46:25.498+05:30  INFO 38972 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-09-21T01:46:25.562+05:30  INFO 38972 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 51 ms. Found 4 JPA repository interfaces.
2025-09-21T01:46:26.408+05:30  INFO 38972 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-09-21T01:46:26.422+05:30  INFO 38972 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-09-21T01:46:26.422+05:30  INFO 38972 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.20]
2025-09-21T01:46:26.490+05:30  INFO 38972 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-09-21T01:46:26.492+05:30  INFO 38972 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2975 ms
2025-09-21T01:46:26.732+05:30  INFO 38972 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-09-21T01:46:26.791+05:30  INFO 38972 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.4.Final
2025-09-21T01:46:26.834+05:30  INFO 38972 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-09-21T01:46:27.085+05:30  INFO 38972 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-09-21T01:46:27.108+05:30  INFO 38972 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-09-21T01:46:27.233+05:30  INFO 38972 --- [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@2a99ca99
2025-09-21T01:46:27.235+05:30  INFO 38972 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-09-21T01:46:28.159+05:30  INFO 38972 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2025-09-21T01:46:28.313+05:30  INFO 38972 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-09-21T01:46:28.875+05:30  INFO 38972 --- [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2025-09-21T01:46:29.483+05:30  WARN 38972 --- [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'checkerController': Unsatisfied dependency expressed through field 'loanService': Error creating bean with name 'loanApplicationsServiceImp': Unsatisfied dependency expressed through field 'workflowRepo': Error creating bean with name 'workflowRepository' defined in com.scb.loanOrigination.repository.WorkflowRepository defined in @EnableJpaRepositories declared on JpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration: Could not create query for public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox(); Reason: Validation failed for query for method public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox()
2025-09-21T01:46:29.483+05:30  INFO 38972 --- [           main] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2025-09-21T01:46:29.490+05:30  INFO 38972 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2025-09-21T01:46:29.495+05:30  INFO 38972 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2025-09-21T01:46:29.498+05:30  INFO 38972 --- [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2025-09-21T01:46:29.515+05:30  INFO 38972 --- [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2025-09-21T01:46:29.542+05:30 ERROR 38972 --- [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'checkerController': Unsatisfied dependency expressed through field 'loanService': Error creating bean with name 'loanApplicationsServiceImp': Unsatisfied dependency expressed through field 'workflowRepo': Error creating bean with name 'workflowRepository' defined in com.scb.loanOrigination.repository.WorkflowRepository defined in @EnableJpaRepositories declared on JpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration: Could not create query for public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox(); Reason: Validation failed for query for method public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox()
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:787) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.inject(AutowiredAnnotationBeanPostProcessor.java:767) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.annotation.InjectionMetadata.inject(InjectionMetadata.java:145) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.postProcessProperties(AutowiredAnnotationBeanPostProcessor.java:508) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1419) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:599) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:326) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:324) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:975) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:962) ~[spring-context-6.1.6.jar:6.1.6]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:624) ~[spring-context-6.1.6.jar:6.1.6]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:456) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:334) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1354) ~[spring-boot-3.2.5.jar:3.2.5]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1343) ~[spring-boot-3.2.5.jar:3.2.5]
	at com.scb.loanOrigination.LoanOrigination.main(LoanOrigination.java:10) ~[classes/:na]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'loanApplicationsServiceImp': Unsatisfied dependency expressed through field 'workflowRepo': Error creating bean with name 'workflowRepository' defined in com.scb.loanOrigination.repository.WorkflowRepository defined in @EnableJpaRepositories declared on JpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration: Could not create query for public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox(); Reason: Validation failed for query for method public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox()
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:787) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.inject(AutowiredAnnotationBeanPostProcessor.java:767) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.annotation.InjectionMetadata.inject(InjectionMetadata.java:145) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.postProcessProperties(AutowiredAnnotationBeanPostProcessor.java:508) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1419) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:599) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:326) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:324) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:784) ~[spring-beans-6.1.6.jar:6.1.6]
	... 20 common frames omitted
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'workflowRepository' defined in com.scb.loanOrigination.repository.WorkflowRepository defined in @EnableJpaRepositories declared on JpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration: Could not create query for public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox(); Reason: Validation failed for query for method public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox()
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1786) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:600) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:522) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:326) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:324) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:200) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1443) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1353) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:784) ~[spring-beans-6.1.6.jar:6.1.6]
	... 34 common frames omitted
Caused by: org.springframework.data.repository.query.QueryCreationException: Could not create query for public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox(); Reason: Validation failed for query for method public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox()
	at org.springframework.data.repository.query.QueryCreationException.create(QueryCreationException.java:101) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.lookupQuery(QueryExecutorMethodInterceptor.java:115) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.mapMethodsToQuery(QueryExecutorMethodInterceptor.java:99) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.lambda$new$0(QueryExecutorMethodInterceptor.java:88) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at java.base/java.util.Optional.map(Optional.java:260) ~[na:na]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.<init>(QueryExecutorMethodInterceptor.java:88) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.RepositoryFactorySupport.getRepository(RepositoryFactorySupport.java:357) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport.lambda$afterPropertiesSet$5(RepositoryFactoryBeanSupport.java:279) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.util.Lazy.getNullable(Lazy.java:135) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.util.Lazy.get(Lazy.java:113) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport.afterPropertiesSet(RepositoryFactoryBeanSupport.java:285) ~[spring-data-commons-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean.afterPropertiesSet(JpaRepositoryFactoryBean.java:132) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1833) ~[spring-beans-6.1.6.jar:6.1.6]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1782) ~[spring-beans-6.1.6.jar:6.1.6]
	... 44 common frames omitted
Caused by: java.lang.IllegalArgumentException: Validation failed for query for method public abstract java.util.List com.scb.loanOrigination.repository.WorkflowRepository.getMakerInbox()
	at org.springframework.data.jpa.repository.query.SimpleJpaQuery.validateQuery(SimpleJpaQuery.java:100) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.query.SimpleJpaQuery.<init>(SimpleJpaQuery.java:70) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.query.JpaQueryFactory.fromMethodWithQueryString(JpaQueryFactory.java:60) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy$DeclaredQueryLookupStrategy.resolveQuery(JpaQueryLookupStrategy.java:170) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy$CreateIfNotFoundQueryLookupStrategy.resolveQuery(JpaQueryLookupStrategy.java:252) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy$AbstractQueryLookupStrategy.resolveQuery(JpaQueryLookupStrategy.java:95) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.lookupQuery(QueryExecutorMethodInterceptor.java:111) ~[spring-data-commons-3.2.5.jar:3.2.5]
	... 56 common frames omitted
Caused by: java.lang.IllegalArgumentException: org.hibernate.query.SemanticException: Could not resolve class 'com.scb.loanOrigination.dto.makerInbox.MakerInboxDTO' named for instantiation
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:143) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:167) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:173) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.AbstractSharedSessionContract.createQuery(AbstractSharedSessionContract.java:848) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.AbstractSharedSessionContract.createQuery(AbstractSharedSessionContract.java:753) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.AbstractSharedSessionContract.createQuery(AbstractSharedSessionContract.java:136) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]
	at org.springframework.orm.jpa.ExtendedEntityManagerCreator$ExtendedEntityManagerInvocationHandler.invoke(ExtendedEntityManagerCreator.java:364) ~[spring-orm-6.1.6.jar:6.1.6]
	at jdk.proxy2/jdk.proxy2.$Proxy120.createQuery(Unknown Source) ~[na:na]
	at org.springframework.data.jpa.repository.query.SimpleJpaQuery.validateQuery(SimpleJpaQuery.java:94) ~[spring-data-jpa-3.2.5.jar:3.2.5]
	... 62 common frames omitted
Caused by: org.hibernate.query.SemanticException: Could not resolve class 'com.scb.loanOrigination.dto.makerInbox.MakerInboxDTO' named for instantiation
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitInstantiation(SemanticQueryBuilder.java:1374) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitInstantiation(SemanticQueryBuilder.java:269) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.grammars.hql.HqlParser$InstantiationContext.accept(HqlParser.java:3761) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitSelectableNode(SemanticQueryBuilder.java:1355) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitSelection(SemanticQueryBuilder.java:1309) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitSelectClause(SemanticQueryBuilder.java:1302) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitQuery(SemanticQueryBuilder.java:1154) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitQuerySpecExpression(SemanticQueryBuilder.java:941) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitQuerySpecExpression(SemanticQueryBuilder.java:269) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.grammars.hql.HqlParser$QuerySpecExpressionContext.accept(HqlParser.java:1869) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitSimpleQueryGroup(SemanticQueryBuilder.java:926) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitSimpleQueryGroup(SemanticQueryBuilder.java:269) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.grammars.hql.HqlParser$SimpleQueryGroupContext.accept(HqlParser.java:1740) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitSelectStatement(SemanticQueryBuilder.java:443) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.visitStatement(SemanticQueryBuilder.java:402) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.SemanticQueryBuilder.buildSemanticModel(SemanticQueryBuilder.java:311) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.hql.internal.StandardHqlTranslator.translate(StandardHqlTranslator.java:71) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.internal.QueryInterpretationCacheStandardImpl.createHqlInterpretation(QueryInterpretationCacheStandardImpl.java:165) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.query.internal.QueryInterpretationCacheStandardImpl.resolveHqlInterpretation(QueryInterpretationCacheStandardImpl.java:147) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.AbstractSharedSessionContract.interpretHql(AbstractSharedSessionContract.java:790) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	at org.hibernate.internal.AbstractSharedSessionContract.createQuery(AbstractSharedSessionContract.java:840) ~[hibernate-core-6.4.4.Final.jar:6.4.4.Final]
	... 71 common frames omitted


Process finished with exit code 1
