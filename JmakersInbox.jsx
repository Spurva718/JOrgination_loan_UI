import React, { useState } from "react";
import { Form, Row, Col, Button, Collapse, Card } from "react-bootstrap";

export default function Filters({ onApply, onReset }) {
  const [open, setOpen] = useState(false);

  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [currStep, setCurrStep] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");

  function applyFilters() {
    onApply({ search, status, currStep, dateFrom, dateTo });
    setOpen(false);
  }

  function resetFilters() {
    setSearch("");
    setStatus("");
    setCurrStep("");
    setDateFrom("");
    setDateTo("");
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
      >
        {open ? "Hide Filters" : "Show Filters"}
      </Button>

      <Collapse in={open}>
        <div id="filters-collapse" className="mt-3">
          <Card body className="bg-light">
            <Form className="small">
              <Row className="g-2 align-items-center">
                <Col md={3}>
                  <Form.Control
                    size="sm"
                    placeholder="Search Txn / Loan / Applicant"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                </Col>
                <Col md={2}>
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
                <Col md={2}>
                  <Form.Select
                    size="sm"
                    value={currStep}
                    onChange={(e) => setCurrStep(e.target.value)}
                  >
                    <option value="">Curr Step</option>
                    <option value="MAKER">Maker</option>
                    <option value="CHECKER">Checker</option>
                  </Form.Select>
                </Col>
                <Col md={2}>
                  <Form.Control
                    size="sm"
                    type="date"
                    value={dateFrom}
                    onChange={(e) => setDateFrom(e.target.value)}
                  />
                </Col>
                <Col md={2}>
                  <Form.Control
                    size="sm"
                    type="date"
                    value={dateTo}
                    onChange={(e) => setDateTo(e.target.value)}
                  />
                </Col>
                <Col md={1} className="d-grid">
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


import React, { useState } from "react";
import { Container, Card } from "react-bootstrap";
import Filters from "../components/Filters";
import TransactionsTable from "../components/TransactionsTable";

// Generate mock dataset
const MOCK_TRANSACTIONS = Array.from({ length: 42 }).map((_, i) => ({
  transactionRef: "TXN" + (1000 + i),
  loanId: "LN" + (100 + i),
  applicant: ["John Doe", "Alice Smith", "Rahul Kumar", "Maria Lopez"][i % 4],
  amount: Math.floor(Math.random() * 100000) + 5000,
  currency: ["USD", "EUR", "INR"][i % 3],
  createdAt: "2025-09-" + String((i % 30) + 1).padStart(2, "0"),
  currStep: i % 2 === 0 ? "MAKER" : "CHECKER",
  lastStep: ["INITIATED", "DOCS VERIFIED", "APPROVED"][i % 3],
  processDate: "2025-09-" + String((i % 30) + 1).padStart(2, "0") + " 10:00:00",
  status: ["PENDING", "IN_PROGRESS", "APPROVED"][i % 3],
  assignedTo: i % 5 === 0 ? "user123" : "",
  flags:
    i % 6 === 0
      ? [{ type: "ID_PROOF", message: "Document issue found" }]
      : [],
}));

export default function MakerInboxPage() {
  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(1);

  function handleApplyFilters(f) {
    setFilters(f);
    setPage(1);
  }

  function handleResetFilters() {
    setFilters({});
    setPage(1);
  }

  function handlePageChange(p) {
    setPage(p);
  }

  function handleView(txn) {
    alert(`Viewing ${txn.transactionRef} (${txn.loanId})`);
  }

  // Apply filters on mock data
  let filteredData = MOCK_TRANSACTIONS.filter((t) => {
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
    if (filters.status && t.status !== filters.status) return false;
    if (filters.currStep && t.currStep !== filters.currStep) return false;
    return true;
  });

  const pageSize = 10;
  const totalPages = Math.ceil(filteredData.length / pageSize);
  const pagedData = filteredData.slice((page - 1) * pageSize, page * pageSize);

  return (
    <Container className="py-4">
      <Card className="shadow-lg">
        <Card.Header className="bg-dark text-white d-flex justify-content-between">
          <h5 className="mb-0">Maker Inbox</h5>
          <small className="text-light">Transactions Overview</small>
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
          />
        </Card.Body>
      </Card>
    </Container>
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
}) {
  return (
    <>
      <Table hover responsive className="align-middle text-center mb-0">
        <thead className="table-dark">
          <tr>
            <th>Txn Ref No</th>
            <th>Loan ID</th>
            <th>Applicant</th>
            <th>Amount</th>
            <th>Currency</th>
            <th>Created At</th>
            <th>Curr Step</th>
            <th>Last Step</th>
            <th>Process Date</th>
            <th>Status</th>
            <th>Assigned To</th>
            <th>Flags</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan="13">Loading...</td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan="13">No results</td>
            </tr>
          ) : (
            data.map((txn) => (
              <TransactionRow key={txn.transactionRef} txn={txn} onView={onView} />
            ))
          )}
        </tbody>
      </Table>

      <div className="d-flex justify-content-center mt-3">
        <Pagination>
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

export default function TransactionRow({ txn, onView }) {
  const hasFlags = txn.flags && txn.flags.length > 0;

  return (
    <>
      <tr className={hasFlags ? "table-danger" : ""} style={{ cursor: "pointer" }}>
        <td>{txn.transactionRef}</td>
        <td>{txn.loanId}</td>
        <td>{txn.applicant}</td>
        <td>{txn.amount.toLocaleString()}</td>
        <td>{txn.currency}</td>
        <td>{txn.createdAt}</td>
        <td>{txn.currStep}</td>
        <td>{txn.lastStep}</td>
        <td>{txn.processDate}</td>
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
        <td>{txn.assignedTo || "-"}</td>
        <td>
          {hasFlags ? (
            <Badge bg="danger">{txn.flags.length}</Badge>
          ) : (
            <Badge bg="secondary">0</Badge>
          )}
        </td>
        <td>
          <Button size="sm" variant="outline-dark" onClick={() => onView(txn)}>
            View
          </Button>
        </td>
      </tr>

      {hasFlags && (
        <tr>
          <td colSpan="13" className="p-0">
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
