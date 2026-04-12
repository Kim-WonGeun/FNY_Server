package com.mailservice.fny.mailbox.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "fny.agent.enabled=false")
@AutoConfigureMockMvc
@Transactional
class MailboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void overviewReturnsAggregatedMailboxMetrics() throws Exception {
        mockMvc.perform(get("/api/users/USR_260409_A00001/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("USR_260409_A00001")))
                .andExpect(jsonPath("$.totalEmails", is(3)))
                .andExpect(jsonPath("$.unreadEmails", is(2)))
                .andExpect(jsonPath("$.needsReplyEmails", is(2)))
                .andExpect(jsonPath("$.highPriorityEmails", is(3)))
                .andExpect(jsonPath("$.spotlightEmails", hasSize(3)));
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
    void emailDetailReturnsAnalysisLabelsAndJobs() throws Exception {
        mockMvc.perform(get("/api/emails/EML_260409_A00006"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("EML_260409_A00006")))
                .andExpect(jsonPath("$.analysis.category", is("URGENT")))
                .andExpect(jsonPath("$.labels[0].code", is("URGENT")))
                .andExpect(jsonPath("$.analysisJobs", hasSize(1)))
                .andExpect(jsonPath("$.actionItems", hasSize(1)));
    }

    @Test
    void queueAnalysisJobCreatesPendingJob() throws Exception {
        mockMvc.perform(post("/api/emails/EML_260409_A00001/analysis-jobs"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status", is("PENDING")))
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
    }
}
