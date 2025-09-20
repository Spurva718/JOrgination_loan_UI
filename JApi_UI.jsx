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
