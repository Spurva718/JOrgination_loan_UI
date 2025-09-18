@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {

    @Query(value = """
        SELECT w.workflow_id,
               l.loan_id,
               l.user_id,
               concat(c.first_name, ' ', c.last_name) AS applicant_name,
               w.created_at,
               w.updated_at,
               w.status,
               COALESCE(SUM(CASE WHEN d.flag = true THEN 1 ELSE 0 END), 0) AS flags_count,
               (SELECT d2.comment
                  FROM documents d2
                 WHERE d2.loan_id = l.loan_id
                   AND d2.flag = true
                 ORDER BY d2.uploaded_at DESC
                 LIMIT 1) AS remark
          FROM workflow w
          JOIN loan_applications l ON w.loan_id = l.loan_id
          JOIN customers c ON c.user_id = l.user_id
          LEFT JOIN documents d ON d.loan_id = l.loan_id
         WHERE w.step_name = 'Maker'
      GROUP BY w.workflow_id, l.loan_id, l.user_id,
               c.first_name, c.last_name, w.created_at, w.updated_at, w.status
        """,
        nativeQuery = true)
    List<Object[]> findMakerInbox();
}


@Service
public class WorkflowServiceImp implements IWorkflow {
    @Autowired
    private WorkflowRepository workflowRepo;

    @Override
    @Transactional
    public List<Map<String, Object>> getMakerInboxByStatus(Workflow.WorkFlowStatusEnum status) {
        throw new UnsupportedOperationException("Use getMakerInbox instead");
    }

    @Transactional
    public List<Map<String, Object>> getMakerInbox() {
        List<Object[]> rows = workflowRepo.findMakerInbox();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("workflowId", r[0] == null ? null : ((Number) r[0]).intValue());
            m.put("loanId", r[1] == null ? null : ((Number) r[1]).intValue());
            m.put("userId", r[2] == null ? null : r[2].toString());
            m.put("applicantName", r[3] == null ? "" : r[3].toString());
            m.put("createdAt", r[4] == null ? null : r[4].toString());
            m.put("updatedAt", r[5] == null ? null : r[5].toString());
            m.put("status", r[6] == null ? null : r[6].toString());
            m.put("flagsCount", r[7] == null ? 0 : ((Number) r[7]).intValue());
            m.put("remark", r[8] == null ? "" : r[8].toString());
            out.add(m);
        }
        return out;
    }
}

public interface IWorkflow {
    Workflow getWorkflowDetails(int workflowId) throws MakerException;

    List<Map<String, Object>> getMakerInbox();
}

@RestController
@CrossOrigin(origins = "*")
public class MakerController {

    @Autowired
    private WorkflowServiceImp workflowService;

    // Old endpoint — comment it out
    // @RequestMapping(value = "/makerInbox/{status}", method = RequestMethod.GET)
    // public List<Map<String, Object>> getMakerInbox(@PathVariable Workflow.WorkFlowStatusEnum status) {
    //     return workflowService.getMakerInboxByStatus(status);
    // }

    // New endpoint — no status filter
    @RequestMapping(value = "/makerInbox", method = RequestMethod.GET)
    public List<Map<String, Object>> getMakerInbox() {
        return workflowService.getMakerInbox();
    }
}

GET http://localhost:8080/makerInbox
