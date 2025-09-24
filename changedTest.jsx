package com.scb.loanOrigination.repository;

import com.scb.loanOrigination.dto.MakerInboxDTO;
import com.scb.loanOrigination.entity.Customers;
import com.scb.loanOrigination.entity.LoanApplications;
import com.scb.loanOrigination.entity.Workflow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WorkflowRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Test
    void testGetMakerInbox_returnsResult() {
        // --- Arrange ---
        // Create a Customer
        Customers customer = new Customers();
        customer.setUserId("U123");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        entityManager.persist(customer);

        // Create Loan Application
        LoanApplications loan = new LoanApplications();
        loan.setLoanId(1L);
        loan.setUserId("U123");
        entityManager.persist(loan);

        // Create Workflow
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(101L);
        workflow.setLoanId(1L);
        workflow.setUserId("U123");
        workflow.setStepName(Workflow.WorkflowStepNameEnum.Maker);
        workflow.setStatus(Workflow.WorkFlowStatusEnum.Moved_To_Maker);
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(workflow);

        entityManager.flush();

        // --- Act ---
        List<MakerInboxDTO> result = workflowRepository.getMakerInbox();

        // --- Assert ---
        assertThat(result).hasSize(1);
        MakerInboxDTO dto = result.get(0);
        assertThat(dto.getWorkflowId()).isEqualTo(101L);
        assertThat(dto.getApplicantName()).isEqualTo("John Doe");
        assertThat(dto.getStatus()).isEqualTo(Workflow.WorkFlowStatusEnum.Moved_To_Maker);
    }

    @Test
    void testGetMakerInbox_empty() {
        List<MakerInboxDTO> result = workflowRepository.getMakerInbox();
        assertThat(result).isEmpty();
    }
}


package com.scb.loanOrigination.controller;

import com.scb.loanOrigination.dto.MakerInboxDTO;
import com.scb.loanOrigination.entity.Workflow;
import com.scb.loanOrigination.service.WorkflowServiceImp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MakerController.class)
class MakerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowServiceImp workflowService;

    @Test
    void testGetMakerInbox_success() throws Exception {
        MakerInboxDTO dto = new MakerInboxDTO(
                101L,
                1L,
                "U123",
                "John Doe",
                Workflow.WorkFlowStatusEnum.Moved_To_Maker,
                "2025-01-01",
                "2025-01-02"
        );

        Mockito.when(workflowService.getMakerInbox()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/maker/makerInbox")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workflowId").value(101L))
                .andExpect(jsonPath("$[0].loanId").value(1L))
                .andExpect(jsonPath("$[0].userId").value("U123"))
                .andExpect(jsonPath("$[0].applicantName").value("John Doe"))
                .andExpect(jsonPath("$[0].status").value("Moved_To_Maker"))
                .andExpect(jsonPath("$[0].createdAt").value("2025-01-01"))
                .andExpect(jsonPath("$[0].updatedAt").value("2025-01-02"));
    }

    @Test
    void testGetMakerInbox_emptyList() throws Exception {
        Mockito.when(workflowService.getMakerInbox()).thenReturn(List.of());

        mockMvc.perform(get("/api/maker/makerInbox"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
