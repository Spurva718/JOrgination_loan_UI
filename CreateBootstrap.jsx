import React, { useEffect, useState } from "react";
import { Container, Card } from "react-bootstrap";
import Filters from "./Filters";
import TransactionsTable from "./TransactionTable";
import { FaInbox } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { getMakerInbox } from "../../../api/maker/index";

export default function MakerInboxPage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(1);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const data = await getMakerInbox();

        function formatDate(dataString) {
          if (!dataString) return "";
          const d = new Date(dataString);
          return d.toISOString().split("T")[0];
        }

        const mappedData = data.map((item) => ({
          workflowId: item.workflowId,
          loanId: item.loanId,
          userId: item.userId,
          applicant: item.applicantName,
          createdAt: formatDate(item.createdAt),
          updatedAt: formatDate(item.updatedAt),
          status: item.status,
          flags:
            item.flagsCount > 0
              ? [{ message: item.remarks }]
              : [],
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
    //alert(`Viewing Workflow ${txn.workflowId} (Loan ${txn.loanId})`);

    navigate("/view_application", { 
      state: {
        workflowId: txn.workflowId,
        loanId: txn.loanId
      }
    });
  }

  let filteredData = transactions.filter((t) => {
    if (filters.search) {
      const s = filters.search.toLowerCase();
      if (
        !(
          String(t.workflowId).toLowerCase().includes(s) ||
          String(t.loanId).toLowerCase().includes(s) ||
          t.applicant.toLowerCase().includes(s)
        )
      ) {
        return false;
      }
    }
    return true;
  });

  const pageSize = 4;
  const totalPages = Math.ceil(filteredData.length / pageSize) || 1;
  const pagedData = filteredData.slice(
    (page - 1) * pageSize,
    page * pageSize
  );

  return (
    <div
      style={{
        background: "linear-gradient(180deg,#0055AA 0%,#004488 100%)",
        minHeight: "100vh",
        overflow:"hidden",
      }}
    >
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
            <Filters
              onApply={handleApplyFilters}
              onReset={handleResetFilters}
            />
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
                fontWeight: "bold",
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
        <td>{txn.userId || "-"}</td>
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
