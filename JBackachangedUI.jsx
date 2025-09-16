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
  const [loading, setLoading] = useState(false);

  // Fetch from backend
  useEffect(() => {
    setLoading(true);
    fetch("http://localhost:8080/maker/inbox")
      .then((res) => {
        if (!res.ok) throw new Error("Network response not ok");
        return res.json();
      })
      .then((data) => {
        setTransactions(Array.isArray(data) ? data : []);
        setPage(1);
      })
      .catch((err) => {
        console.error("Error fetching maker inbox:", err);
        setTransactions([]);
      })
      .finally(() => setLoading(false));
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
    alert(`Viewing ${txn.transactionRef} (${txn.loanId})`);
  }

  // Apply filters
  let filteredData = transactions.filter((t) => {
    if (filters.search) {
      const s = filters.search.toLowerCase();
      if (
        !(
          (t.transactionRef && t.transactionRef.toLowerCase().includes(s)) ||
          (t.loanId && String(t.loanId).toLowerCase().includes(s)) ||
          (t.applicant && t.applicant.toLowerCase().includes(s))
        )
      )
        return false;
    }
    return true;
  });

  const pageSize = 5;
  const totalPages = Math.max(1, Math.ceil(filteredData.length / pageSize));
  const pagedData = filteredData.slice((page - 1) * pageSize, page * pageSize);

  return (
    <div
      style={{
        background: "linear-gradient(180deg,#0055AA 0%,#004488 100%)",
        minHeight: "100vh",
      }}
    >
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
import { Button, Badge, Alert } from "react-bootstrap";
import { FaEye, FaExclamationCircle } from "react-icons/fa";

export default function TransactionRow({ txn, onView }) {
  const flags = txn.flags || [];
  const docFlags = flags.filter((f) => f.type !== "Remarks");
  const remarks = flags.filter((f) => f.type === "Remarks").map((r) => r.message);

  // highlight row only if document flags exist
  const rowClass = docFlags.length > 0 ? "table-danger" : "";

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

      <tr className={rowClass} style={{ cursor: "pointer" }}>
        <td>{txn.transactionRef}</td>
        <td>{txn.loanId}</td>
        <td>{txn.assignedTo || "-"}</td>
        <td>{txn.applicant}</td>
        <td>{txn.createdAt}</td>
        <td>{txn.updatedAt}</td>
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
          {docFlags.length > 0 ? (
            <Badge bg="danger">
              <FaExclamationCircle /> {docFlags.length}
            </Badge>
          ) : (
            <Badge bg="secondary">0</Badge>
          )}
        </td>
        <td>
          <Button size="sm" className="view-btn" onClick={() => onView(txn)}>
            <FaEye /> View
          </Button>
        </td>
      </tr>

      {(docFlags.length > 0 || remarks.length > 0) && (
        <tr>
          <td colSpan="9" className="p-0">
            {docFlags.length > 0 && (
              <Alert variant="danger" className="mb-2 p-2 small">
                <strong>Flagged Documents:</strong>
                <ul className="mb-1">
                  {docFlags.map((f, i) => (
                    <li key={i}>
                      <strong>{f.type}</strong> - {f.message}
                    </li>
                  ))}
                </ul>
              </Alert>
            )}
            {remarks.length > 0 && (
              <Alert variant="info" className="p-2 small mb-0">
                <strong>Remarks:</strong>
                <ul className="mb-0">
                  {remarks.map((r, i) => (
                    <li key={i}>{r}</li>
                  ))}
                </ul>
              </Alert>
            )}
          </td>
        </tr>
      )}
    </>
  );
}
