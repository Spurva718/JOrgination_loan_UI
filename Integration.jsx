// src/api/makerApi.js

const BASE_URL = "http://localhost:8080"; // adjust if backend path changes

async function checkResponse(res) {
  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || "API error");
  }
  return res;
}

// Fetch all maker inbox transactions
export async function getMakerInbox() {
  const res = await fetch(`${BASE_URL}/makerInbox`);
  await checkResponse(res);
  return res.json();
}

import React, { useState, useEffect } from "react";
import { Container, Card } from "react-bootstrap";
import Navbar from "../components/Navbar";
import Filters from "../components/Filters";
import TransactionsTable from "../components/TransactionTable";
import { FaInbox } from "react-icons/fa";

// Import API function from central index.js
import { getMakerInbox } from "../api";

export default function MakerInboxPage() {
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
          assignedTo: item.userId,
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
    alert(`Viewing Workflow ${txn.workflowId} (Loan ${txn.loanId})`);
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

  const pageSize = 5;
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

####

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


filter Status
import React, { useState, useEffect } from "react";
import { Form, Row, Col, Button, Collapse, Card, Badge } from "react-bootstrap";
import { FaFilter } from "react-icons/fa";

export default function Filters({ onApply, onReset }) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState(""); // NEW
  const [activeCount, setActiveCount] = useState(0);

  useEffect(() => {
    let count = 0;
    if (search && search !== "") count++;
    if (status && status !== "") count++;
    setActiveCount(count);
  }, [search, status]);

  function applyFilters() {
    onApply({ search, status }); // pass both search + status
    setOpen(false);
  }

  function resetFilters() {
    setSearch("");
    setStatus(""); // reset status too
    onReset();
    setOpen(false);
  }

  return (
    <div>
      <Button
        variant="outline-primary"
        size="sm"
        onClick={() => setOpen(!open)}
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
        <div className="mt-2">
          <Card body className="bg-light">
            <Form className="small">
              <Row className="g-2 align-items-center">
                <Col md={6}>
                  <Form.Control
                    size="sm"
                    placeholder="Search Workflow ID / Loan ID / Applicant"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                </Col>
                <Col md={4}>
                  <Form.Select
                    size="sm"
                    value={status}
                    onChange={(e) => setStatus(e.target.value)}
                  >
                    <option value="">All Status</option>
                    <option value="Moved_To_Maker">Moved To Maker</option>
                    <option value="Flagged_For_Data_ReEntry">Flagged For Data ReEntry</option>
                  </Form.Select>
                </Col>
                <Col md={2} className="d-flex justify-content-end gap-2">
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

  if (filters.status && t.status !== filters.status) {
    return false;
  }

  return true;
});
