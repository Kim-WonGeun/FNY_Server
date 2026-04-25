package com.mailservice.fny.mailbox.repository;

import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyMailReportRepository extends JpaRepository<WeeklyMailReport, String> {

    List<WeeklyMailReport> findTop20ByMailAccountIdOrderByCreatedAtDesc(String mailAccountId);

    Optional<WeeklyMailReport> findByIdAndMailAccount_User_Id(String id, String userId);
}
