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

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const res = await fetch("http://localhost:8080/makerInbox");
        const data = await res.json();

        function formatDate(dataString){
          if(!dataString) return "";
          const d = new Date(dataString);
          return d.toISOString().split("T")[0];
        }

        const mappedData = data.map((item) => ({
          workflowId: item.workflowId,   
          loanId: item.loanId,
          assignedTo: item.userId,
          applicant: item.applicantName,
          createdAt: formatDate(item.createdAt), 
          updatedAt: formatDate(item.updatedAt), 
          status: item.status,
          flags: item.flagsCount > 0 
            ? [{message: item.remarks}] 
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

  let filteredData = transactions.filter((t) => {
    if (filters.search) {
      const s = filters.search.toLowerCase();
      if (
        !(
          String(t.workflowId).toLowerCase().includes(s) ||
          String(t.loanId).toLowerCase().includes(s) ||
          t.applicant.toLowerCase().includes(s)
        )
      ){
        return false;
    }
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
import { Table, Pagination } from "react-bootstrap";
import TransactionRow from "./TransactionRow";

export default function TransactionsTable({
  data,
  loading,
  page,
  pages,
  onPageChange,
  onView,
  headerStyle,
}) {
  const headers = [
    "WorkFlow ID",
    "Loan ID",
    "User ID",
    "Applicant Name",
    "Created At",
    "Updated At",
    "Status",
    "Flags",
    "Actions",
  ];

  return (
    <>
      <style>{`
        .custom-pagination .page-link {
          color: #003366;
          border-radius: 6px;
          transition: all 0.3s ease;
        }
        .custom-pagination .page-link:hover {
          background-color: rgba(0, 51, 102, 0.1);
          border-color: #003366;
        }
        .custom-pagination .page-item.active .page-link {
          background-color: #003366;
          border-color: #003366;
          color: white;
        }
        .table-hover > tbody > tr:hover > * {
          background-color: #e0f7ff;
        }
      `}</style>

      <Table hover responsive className="align-middle text-center mb-0">
        <thead>
          <tr>
            {headers.map((headerText, index) => (
              <th key={index} style={headerStyle}>
                {headerText}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan="9">Loading...</td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan="9">No results</td>
            </tr>
          ) : (
            data.map((txn) => (
              <TransactionRow
                key={txn.transactionRef}
                txn={txn}
                onView={onView}
              />
            ))
          )}
        </tbody>
      </Table>

      <div className="d-flex justify-content-center mt-3">
        <Pagination className="custom-pagination">
          {Array.from({ length: pages }).map((_, i) => (
            <Pagination.Item
              key={i + 1}
              active={i + 1 === page}
              onClick={() => onPageChange(i + 1)}
            >
              {i + 1}
            </Pagination.Item>
          ))}
        </Pagination>
      </div>
    </>
  );
}

import React from "react";
import { Button, Badge, Alert } from "react-bootstrap";
import { FaEye, FaExclamationCircle } from "react-icons/fa";

export default function TransactionRow({ txn, onView }) {
  const hasFlags = txn.flags && txn.flags.length > 0;

  function getStatusBadge(status) {
    switch (status) {
      case "Moved_To_Maker":
        return { text: "Moved_To_Maker", color: "warning" };
      case "Flagged_For_Data_ReEntry":
          return { text: "Flagged_For_Data_ReEntry", color: "danger" };
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
              <strong>Remark:</strong>
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

import React, { useState, useEffect } from "react";
import { Form, Row, Col, Button, Collapse, Card, Badge } from "react-bootstrap";
import { FaFilter } from "react-icons/fa";

export default function Filters({ onApply, onReset }) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [activeCount, setActiveCount] = useState(0);

  useEffect(() => {
    setActiveCount(search && search !== "" ? 1 : 0);
  }, [search]);

  function applyFilters() {
    onApply({ search });
    setOpen(false);
  }

  function resetFilters() {
    setSearch("");
    onReset();
    setOpen(false);
  }

  return (
    <div>
      <Button
        variant="outline-primary"
        size="sm"
        onClick={() => setOpen(!open)}
        aria-controls="filters-collapse"
        aria-expanded={open}
        className="mb-2 d-flex align-items-center gap-2"
      >
        <FaFilter />
        {open ? "Hide Filter" : "Show Filter"}
        {activeCount > 0 && (
          <Badge bg="danger" pill>
            {activeCount}
          </Badge>
        )}
      </Button>

      <Collapse in={open}>
        <div id="filters-collapse" className="mt-2">
          <Card body className="bg-light">
            <Form className="small">
              <Row className="g-2 align-items-center">
                <Col md={8}>
                  <Form.Control
                    size="sm"
                    placeholder="Search Workflow ID / Loan ID "
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                </Col>
                <Col md={4} className="d-flex justify-content-end gap-2">
                  <Button variant="primary" size="sm" onClick={applyFilters}>
                    Apply
                  </Button>
                  <Button variant="secondary" size="sm" onClick={resetFilters}>
                    Reset
                  </Button>
                </Col>
              </Row>
            </Form>
          </Card>
        </div>
      </Collapse>
    </div>
  );
}

Data
-- INSERT INTO users (user_id, password, role_name, created_at) VALUES
-- ('maker1', 'pass123', 'Maker', NOW()),
-- ('maker2', 'pass123', 'Maker', NOW()),
-- ('maker3', 'pass123', 'Maker', NOW()),
-- ('maker4', 'pass123', 'Maker', NOW());

-- INSERT INTO customers (user_id, address, dob, gender, email, first_name, last_name, pan, aadhaar, mobile_number) VALUES
-- ('maker1', 'Delhi', '1990-01-15', 'M', 'john.doe@mail.com', 'John', 'Doe', 'ABCDE1234F', 123456789012, 9876543210),
-- ('maker2', 'Mumbai', '1992-03-20', 'F', 'alice.smith@mail.com', 'Alice', 'Smith', 'PQRSX9876L', 223456789012, 9876543211),
-- ('maker3', 'Bangalore', '1991-07-25', 'M', 'rahul.kumar@mail.com', 'Rahul', 'Kumar', 'LMNOP3456Z', 323456789012, 9876543212),
-- ('maker4', 'Chennai', '1993-12-05', 'F', 'maria.lopez@mail.com', 'Maria', 'Lopez', 'GHJKL5432Q', 423456789012, 9876543213);

-- INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
-- VALUES (500000, 'INR', 24, 8.5, 'Initiated', NOW(), 'maker1')
-- RETURNING loan_id

-- INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
-- VALUES (600000, 'INR', 36, 9.0, 'In_Progress', NOW(), 'maker2')
-- RETURNING loan_id

-- INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
-- VALUES (750000, 'INR', 48, 8.0, 'In_Progress', NOW(), 'maker3')
-- RETURNING loan_id

-- INSERT INTO loan_applications (amount, currency, loan_tenure, interest_rate, status, created_at, user_id)
-- VALUES (400000, 'INR', 18, 7.5, 'Initiated', NOW(), 'maker4')
-- RETURNING loan_id

-- INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id)
-- VALUES ('Maker', 'Moved_To_Maker', NOW(), NOW(), 9, 'Loan submitted for review', 'maker1');

-- INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id)
-- VALUES ('Maker', 'Flagged_For_ReUpload', NOW(), NOW(), 10, 'Address proof needs resubmission', 'maker2');

-- INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id)
-- VALUES ('Maker', 'Flagged_For_Data_ReEntry', NOW(), NOW(), 11, 'Salary slip mismatch', 'maker3');

-- INSERT INTO workflow (step_name, status, created_at, updated_at, loan_id, remarks, user_id)
-- VALUES ('Maker', 'Moved_To_Maker', NOW(), NOW(), 12, 'Fresh loan assigned to Maker', 'maker4');

-- INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
-- VALUES ('ID_PROOF', 'id1.pdf', '/docs/id1.pdf', '/entries/id1.json', NOW(), 'Flagged_For_ReUpload', true, 'Photo unclear', 'maker1', 9);

-- INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
-- VALUES ('ADDRESS_PROOF', 'addr2.pdf', '/docs/addr2.pdf', '/entries/addr2.json', NOW(), 'Uploaded', false, 'OK', 'maker2', 10);

-- INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
-- VALUES ('BANK_STATEMENT', 'bank2.pdf', '/docs/bank2.pdf', '/entries/bank2.json', NOW(), 'Flagged_For_ReUpload', true, 'Old statement', 'maker2', 10);

-- INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
-- VALUES ('SALARY_SLIP', 'salary3.pdf', '/docs/salary3.pdf', '/entries/salary3.json', NOW(), 'Flagged_For_Data_ReEntry', true, 'Mismatch in salary', 'maker3', 11);

-- INSERT INTO documents (document_name, file_name, file_path, entries_file_path, uploaded_at, status, flag, comment, user_id, loan_id)
-- VALUES ('PAN_CARD', 'pan4.pdf', '/docs/pan4.pdf', '/entries/pan4.json', NOW(), 'Uploaded', false, 'OK', 'maker4', 12);
