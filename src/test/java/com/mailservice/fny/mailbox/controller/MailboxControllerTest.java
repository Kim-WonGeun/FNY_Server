package com.mailservice.fny.mailbox.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "fny.agent.enabled=false",
        "fny.security.dev-user-header-enabled=true"
})
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class MailboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void overviewReturnsAggregatedMailboxMetrics() throws Exception {
        mockMvc.perform(get("/api/users/USR_260409_A00001/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("USR_260409_A00001")))
                .andExpect(jsonPath("$.totalEmails", is(100)))
                .andExpect(jsonPath("$.unreadEmails", is(67)))
                .andExpect(jsonPath("$.needsReplyEmails", is(2)))
                .andExpect(jsonPath("$.highPriorityEmails", is(3)))
                .andExpect(jsonPath("$.spotlightEmails", hasSize(5)));
    }

    @Test
    void myOverviewCanResolveCurrentUserFromHeader() throws Exception {
        mockMvc.perform(get("/api/me/overview")
                        .header("X-FNY-USER-ID", "USR_260409_A00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("USR_260409_A00001")))
                .andExpect(jsonPath("$.totalEmails", is(100)))
                .andExpect(jsonPath("$.spotlightEmails", hasSize(5)));
    }

    @Test
    void inboxCanBeFilteredToUnreadHighPriorityEmails() throws Exception {
        mockMvc.perform(get("/api/users/USR_260409_A00001/emails")
                        .param("unreadOnly", "true")
                        .param("highPriorityOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].priorityLevel", is("P1")))
                .andExpect(jsonPath("$[0].attentionReasons[0]", is("HIGH_PRIORITY")));
    }

    @Test
    void mailboxReturnsAllEmailsByDefault() throws Exception {
        mockMvc.perform(get("/api/users/USR_260409_A00001/emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(100)));
    }

    @Test
    void emailDetailReturnsAnalysisLabelsAndJobs() throws Exception {
        mockMvc.perform(get("/api/emails/EML_260409_A00006"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("EML_260409_A00006")))
                .andExpect(jsonPath("$.attentionResolved", is(false)))
                .andExpect(jsonPath("$.attentionStatus", is("NEEDS_ATTENTION")))
                .andExpect(jsonPath("$.analysis.category", is("URGENT")))
                .andExpect(jsonPath("$.labels[0].code", is("URGENT")))
                .andExpect(jsonPath("$.analysisJobs", hasSize(1)))
                .andExpect(jsonPath("$.actionItems", hasSize(1)));
    }

    @Test
    void resolvedAttentionEmailIsRemovedFromPrioritySurface() throws Exception {
        mockMvc.perform(patch("/api/emails/EML_260409_A00001/attention-status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attentionResolved", is(true)))
                .andExpect(jsonPath("$.attentionResolvedAt").exists())
                .andExpect(jsonPath("$.attentionStatus", is("COMPLETED")))
                .andExpect(jsonPath("$.attentionStatusUpdatedAt").exists());

        mockMvc.perform(get("/api/users/USR_260409_A00001/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsReplyEmails", is(1)))
                .andExpect(jsonPath("$.highPriorityEmails", is(2)))
                .andExpect(jsonPath("$.spotlightEmails[?(@.id == 'EML_260409_A00001')]", hasSize(0)));

        mockMvc.perform(patch("/api/emails/EML_260409_A00001/attention-status")
                        .param("status", "NEEDS_ATTENTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attentionResolved", is(false)))
                .andExpect(jsonPath("$.attentionStatus", is("NEEDS_ATTENTION")));
    }

    @Test
    void queueAnalysisJobCreatesPendingJob() throws Exception {
        mockMvc.perform(post("/api/emails/EML_260409_A00001/analysis-jobs"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status", is("WAITING_AGENT")))
                .andExpect(jsonPath("$.jobId").exists());
    }

    @Test
    void analysisJobResultCanBeStoredAndReturnedInEmailDetail() throws Exception {
        mockMvc.perform(post("/api/analysis-jobs/JOB_260409_A00001/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email_id": "EML_260409_A00001",
                                  "model_name": "rule-based-agent",
                                  "prompt_version": "rule-v1",
                                  "short_summary": "긴급도 critical / 우선순위 P1",
                                  "detailed_summary": "제목과 본문에서 URGENT 성격의 메일로 판단했습니다.",
                                  "category": "URGENT",
                                  "priority_level": "P1",
                                  "importance_score": 93.0,
                                  "urgency_score": 100.0,
                                  "confidence_score": 55.0,
                                  "needs_reply": true,
                                  "has_deadline": false,
                                  "suggested_action": "내용 확인 후 회신",
                                  "reasoning": "긴급 키워드와 회신 요청이 감지되었습니다.",
                                  "action_items": [
                                    {
                                      "action_text": "발신자 요청 확인 후 회신 여부 결정",
                                      "action_type": "REPLY",
                                      "priority_level": "P1"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId", is("JOB_260409_A00001")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.actionItemCount", is(1)));

        mockMvc.perform(get("/api/emails/EML_260409_A00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.category", is("URGENT")))
                .andExpect(jsonPath("$.analysis.priorityLevel", is("P1")))
                .andExpect(jsonPath("$.actionItems[0].actionType", is("REPLY")));

        mockMvc.perform(get("/api/emails/EML_260409_A00001/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].analysisVersion", is(2)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].analysisVersion", is(1)));
    }

    @Test
    void analysisFeedbackCanBeSavedAndUpdated() throws Exception {
        mockMvc.perform(put("/api/email-analyses/ANL_260409_A00001/feedbacks/USR_260409_A00001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "feedbackType": "ACCEPTED",
                                  "reasonCode": "GOOD_SUMMARY",
                                  "memo": "요약과 우선순위가 적절합니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId", is("ANL_260409_A00001")))
                .andExpect(jsonPath("$.emailId", is("EML_260409_A00001")))
                .andExpect(jsonPath("$.userId", is("USR_260409_A00001")))
                .andExpect(jsonPath("$.feedbackType", is("ACCEPTED")))
                .andExpect(jsonPath("$.reasonCode", is("GOOD_SUMMARY")))
                .andExpect(jsonPath("$.memo", is("요약과 우선순위가 적절합니다.")));

        mockMvc.perform(put("/api/email-analyses/ANL_260409_A00001/feedbacks/USR_260409_A00001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "feedbackType": "NEEDS_FIX",
                                  "reasonCode": "PRIORITY_TOO_HIGH",
                                  "memo": "긴급도는 조금 낮아도 됩니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackType", is("NEEDS_FIX")))
                .andExpect(jsonPath("$.reasonCode", is("PRIORITY_TOO_HIGH")))
                .andExpect(jsonPath("$.memo", is("긴급도는 조금 낮아도 됩니다.")));
    }

    @Test
    void generateWeeklyReportSummarizesRecentMailbox() throws Exception {
        var result = mockMvc.perform(post("/api/users/USR_260409_A00001/mail-accounts/MAC_260409_A00001/weekly-reports")
                        .param("startDate", LocalDate.now().minusDays(1).toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailCount", greaterThan(0)))
                .andExpect(jsonPath("$.mailAccountId", is("MAC_260409_A00001")))
                .andExpect(jsonPath("$.source", is("FALLBACK")))
                .andExpect(jsonPath("$.highlights", hasSize(2)))
                .andExpect(jsonPath("$.risksBlockers", hasSize(1)))
                .andExpect(jsonPath("$.nextWeekSuggestions", hasSize(1)))
                .andExpect(jsonPath("$.threadSummaries", hasSize(40)))
                .andExpect(jsonPath("$.threadSummaries[0].reportSection").exists())
                .andExpect(jsonPath("$.threadSummaries[0].evidenceText").exists())
                .andExpect(jsonPath("$.threadSummaries[0].fromEmail").exists())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String reportId = body.get("reportId").asText();

        mockMvc.perform(get("/api/users/USR_260409_A00001/weekly-reports/" + reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId", is(reportId)))
                .andExpect(jsonPath("$.emailCount", is(body.get("emailCount").asInt())));

        mockMvc.perform(put("/api/me/weekly-reports/" + reportId + "/workspace")
                        .header("X-FNY-USER-ID", "USR_260409_A00001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "draftText": "편집한 주간보고 초안",
                                  "saveStatus": "DRAFT",
                                  "excludedSourceIds": ["EML_260409_A00010"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId", is(reportId)))
                .andExpect(jsonPath("$.userId", is("USR_260409_A00001")))
                .andExpect(jsonPath("$.draftText", is("편집한 주간보고 초안")))
                .andExpect(jsonPath("$.saveStatus", is("DRAFT")))
                .andExpect(jsonPath("$.excludedSourceIds[0]", is("EML_260409_A00010")));

        mockMvc.perform(get("/api/me/weekly-reports/" + reportId + "/workspace")
                        .header("X-FNY-USER-ID", "USR_260409_A00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.draftText", is("편집한 주간보고 초안")))
                .andExpect(jsonPath("$.excludedSourceIds[0]", is("EML_260409_A00010")));

        mockMvc.perform(get("/api/users/USR_260409_A00001/mail-accounts/MAC_260409_A00001/weekly-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workspaceStatus", is("DRAFT")));

        mockMvc.perform(patch("/api/me/weekly-reports/" + reportId + "/workspace/status")
                        .header("X-FNY-USER-ID", "USR_260409_A00001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saveStatus": "ARCHIVED"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me/weekly-reports/" + reportId + "/workspace")
                        .header("X-FNY-USER-ID", "USR_260409_A00001"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/USR_260409_A00001/mail-accounts/MAC_260409_A00001/weekly-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reportId", is(reportId)))
                .andExpect(jsonPath("$[0].workspaceStatus", is("NONE")));
    }

    @Test
    void generateWeeklyReportRejectsForeignMailAccount() throws Exception {
        mockMvc.perform(post("/api/users/USR_260409_A00002/mail-accounts/MAC_260409_A00001/weekly-reports"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateWeeklyReportRejectsInvalidPeriod() throws Exception {
        mockMvc.perform(post("/api/users/USR_260409_A00001/mail-accounts/MAC_260409_A00001/weekly-reports")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isBadRequest());
    }
}
