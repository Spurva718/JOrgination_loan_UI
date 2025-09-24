// Fetch Maker Inbox data
export async function getMakerInbox() {
  const response = await fetch("http://localhost:8080/api/maker/makerInbox");
  if (!response.ok) {
    throw new Error("Failed to fetch Maker Inbox");
  }
  return response.json();
}

// Fetch flagged documents for a loan
export async function getFlaggedDocuments(loanId) {
  const response = await fetch(
    `http://localhost:8080/api/checker/getFlaggedDocumentsDetails/${loanId}`
  );
  if (!response.ok) {
    throw new Error("Failed to fetch flagged documents");
  }
  return response.json();
}


import React, { useEffect, useState } from "react";
import { Container, Card } from "react-bootstrap";
import Filters from "./Filters";
import TransactionsTable from "./TransactionTable";
import { FaInbox } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { getMakerInbox, getFlaggedDocuments } from "../../../api/maker/index";

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

        // Fetch workflow + flagged docs
        const mappedData = await Promise.all(
          data.map(async (item) => {
            let comments = [];
            let flagsCount = 0;

            try {
              const flaggedDocs = await getFlaggedDocuments(item.loanId);
              flagsCount = flaggedDocs.length;
              comments = flaggedDocs.map((doc) => ({
                type: doc.documentName,
                message: doc.comment,
              }));
            } catch (err) {
              console.error(
                `Error fetching flagged docs for loan ${item.loanId}`,
                err
              );
            }

            return {
              workflowId: item.workflowId,
              loanId: item.loanId,
              userId: item.userId,
              applicant: item.applicantName,
              createdAt: formatDate(item.createdAt),
              updatedAt: formatDate(item.updatedAt),
              status: item.status,
              flagsCount: flagsCount, // total flagged documents
              flags: comments, // all document comments
              remarks: item.remarks, // still keep workflow remarks if needed
            };
          })
        );

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
    navigate("/view_application", {
      state: {
        workflowId: txn.workflowId,
        loanId: txn.loanId,
      },
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

  const pageSize = 3;
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
import { Button, Badge, Alert } from "react-bootstrap";
import { FaEye, FaExclamationCircle } from "react-icons/fa";

export default function TransactionRow({ txn, onView }) {
  const hasFlags = txn.flagsCount && txn.flagsCount > 0;

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
        <td>{txn.workflowId}</td>
        <td>{txn.loanId}</td>
        <td>{txn.userId || "-"}</td>
        <td>{txn.applicant}</td>
        <td>{txn.createdAt}</td>
        <td>{txn.updatedAt}</td>
        <td>
          <Badge
            bg={
              txn.status === "Moved_To_Maker"
                ? "info"
                : txn.status === "Flagged_For_ReUpload"
                ? "danger"
                : txn.status === "Flagged_For_Data_ReEntry"
                ? "warning"
                : "secondary"
            }
          >
            {txn.status}
          </Badge>
        </td>
        <td>
          {hasFlags ? (
            <Badge bg="danger">
              <FaExclamationCircle /> {txn.flagsCount}
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

      {/* Extra row to show flags and remarks */}
      {(hasFlags || txn.remarks) && (
        <tr>
          <td colSpan="9" className="p-0">
            <Alert variant={hasFlags ? "danger" : "info"} className="mb-0 p-2 small">
              {txn.remarks && (
                <>
                  <strong>Workflow Remark:</strong> {txn.remarks}
                  <br />
                </>
              )}
              {hasFlags && (
                <>
                  <strong>Flagged Documents:</strong>
                  <ul className="mb-0">
                    {txn.flags.map((flag, i) => (
                      <li key={i}>
                        <strong>{flag.type}</strong> - {flag.message}
                      </li>
                    ))}
                  </ul>
                </>
              )}
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
                key={txn.workflowId}
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
