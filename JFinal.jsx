import React, { useState, useEffect } from "react";
import { Form, Row, Col, Button, Collapse, Card, Badge } from "react-bootstrap";
import { FaFilter } from "react-icons/fa";

export default function Filters({ onApply, onReset }) {
  const [open, setOpen] = useState(false);

  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [currStep, setCurrStep] = useState("");
  const [activeCount, setActiveCount] = useState(0);

  useEffect(() => {
    const count = [search, status, currStep].filter((f) => f && f !== "").length;
    setActiveCount(count);
  }, [search, status, currStep]);

  function applyFilters() {
    onApply({ search, status, currStep });
    setOpen(false);
  }

  function resetFilters() {
    setSearch("");
    setStatus("");
    setCurrStep("");
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
        {open ? "Hide Filters" : "Show Filters"}
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
                <Col md={4}>
                  <Form.Control
                    size="sm"
                    placeholder="Search WorkItem / Loan / Applicant"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                </Col>
                <Col md={3}>
                  <Form.Select
                    size="sm"
                    value={status}
                    onChange={(e) => setStatus(e.target.value)}
                  >
                    <option value="">Status</option>
                    <option value="PENDING">Pending</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="APPROVED">Approved</option>
                  </Form.Select>
                </Col>
                <Col md={3}>
                  <Form.Select
                    size="sm"
                    value={currStep}
                    onChange={(e) => setCurrStep(e.target.value)}
                  >
                    <option value="">Step Name</option>
                    <option value="MAKER">Maker</option>
                    <option value="CHECKER">Checker</option>
                  </Form.Select>
                </Col>
                <Col md={2} className="d-grid">
                  <Button variant="primary" size="sm" onClick={applyFilters}>
                    Apply
                  </Button>
                </Col>
              </Row>
              <Row className="mt-2">
                <Col className="text-end">
                  <Button variant="link" size="sm" onClick={resetFilters}>
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

import React from "react";
import { Button, Badge, Alert } from "react-bootstrap";
import { FaEye, FaExclamationCircle } from "react-icons/fa";

export default function TransactionRow({ txn, onView }) {
  const hasFlags = txn.flags && txn.flags.length > 0;

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
        <td>{txn.currStep}</td>        {/* Step Name */}
        <td>{txn.createdAt}</td>       {/* Created At */}
        <td>{txn.updatedAt}</td>       {/* Updated At */}
        <td>
          <Badge
            bg={
              txn.status === "PENDING"
                ? "warning"
                : txn.status === "IN_PROGRESS"
                ? "info"
                : "success"
            }
          >
            {txn.status}
          </Badge>
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
          <td colSpan="10" className="p-0">
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
    "WorkItem ID",
    "Loan ID",
    "User ID",
    "Applicant Name",
    "Step Name",
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
              <td colSpan="10">Loading...</td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan="10">No results</td>
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

import React, { useState } from "react";
import { Container, Card } from "react-bootstrap";
import Navbar from "../components/Navbar";
import Filters from "../components/Filters";
import TransactionsTable from "../components/TransactionTable";
import { FaInbox } from "react-icons/fa";

export default function MakerInboxPage() {
  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(1);

  // Mock data (only required fields)
  const MOCK_TRANSACTIONS = Array.from({ length: 20 }).map((_, i) => {
    const createdDay = (i % 28) + 1;
    const updatedDay = createdDay + Math.floor(Math.random() * 5) + 1; // later than createdAt

    return {
      transactionRef: "TXN" + (1000 + i),   // WorkItem ID
      loanId: "LN" + (100 + i),            // Loan ID
      assignedTo: "user" + (100 + i),      // ✅ unique User ID
      applicant: ["John Doe", "Alice Smith", "Rahul Kumar", "Maria Lopez"][i % 4], // Applicant Name
      currStep: i % 2 === 0 ? "MAKER" : "CHECKER",   // Step Name
      createdAt: `2025-09-${String(createdDay).padStart(2, "0")}`, // Created At
      updatedAt: `2025-09-${String(Math.min(updatedDay, 30)).padStart(2, "0")}`, // ✅ Updated At later
      status: ["PENDING", "IN_PROGRESS", "APPROVED"][i % 3],
      flags: i % 6 === 0 ? [{ type: "ID_PROOF", message: "Document issue found" }] : [],
    };
  });

  function handleApplyFilters(f) { setFilters(f); setPage(1); }
  function handleResetFilters() { setFilters({}); setPage(1); }
  function handlePageChange(p) { setPage(p); }
  function handleView(txn) { alert(`Viewing ${txn.transactionRef} (${txn.loanId})`); }

  // Apply filters
  let filteredData = MOCK_TRANSACTIONS.filter((t) => {
    if (filters.search) {
      const s = filters.search.toLowerCase();
      if (
        !(
          t.transactionRef.toLowerCase().includes(s) ||
          t.loanId.toLowerCase().includes(s) ||
          t.applicant.toLowerCase().includes(s)
        )
      ) return false;
    }
    if (filters.status && t.status !== filters.status) return false;
    if (filters.currStep && t.currStep !== filters.currStep) return false;
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
              loading={false}
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
