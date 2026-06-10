package com.example.skillexchange.controller;

import com.example.skillexchange.common.R;
import com.example.skillexchange.common.UserContext;
import com.example.skillexchange.entity.Report;
import com.example.skillexchange.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 举报Controller
 * 
 * 接口：POST /api/v1/report/submit
 * 
 * 功能说明：
 * 1. 用户违规内容一键举报，这是合规免责的重要功能
 * 2. 所有举报均需审核处理
 * 3. 举报时必须填写举报原因
 * 
 * 本项目仅为信息公告板，所有用户线下自主行为产生的所有风险，均由用户自行承担
 * 举报功能用于收集违规线索，不保证实时处理
 */
@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 提交举报
     * 
     * @param params 包含reportedPostId（被举报内容ID）、reportedUserId（被举报用户ID）、reason（举报原因）
     * @return 举报结果
     */
    @PostMapping("/submit")
    public R submit(@RequestBody Map<String, Object> params) {
        // 参数校验
        Object reportedPostIdObj = params.get("reportedPostId");
        Object reportedUserIdObj = params.get("reportedUserId");
        String reason = (String) params.get("reason");

        if (reportedPostIdObj == null) {
            return R.error("被举报内容ID不能为空");
        }
        if (reportedUserIdObj == null) {
            return R.error("被举报用户ID不能为空");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return R.error("举报原因不能为空");
        }
        if (reason.length() > 200) {
            return R.error("举报原因不能超过200字");
        }

        // 构建举报记录
        Report report = new Report();
        report.setReportUserId(UserContext.getUserId());
        report.setReportedPostId(Long.valueOf(reportedPostIdObj.toString()));
        report.setReportedUserId(Long.valueOf(reportedUserIdObj.toString()));
        report.setReason(reason);

        boolean success = reportService.submitReport(report);
        if (success) {
            return R.success("举报提交成功，我们会尽快处理", null);
        }
        return R.error("举报提交失败，请重试");
    }
}
