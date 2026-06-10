package com.example.skillexchange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillexchange.entity.Report;
import com.example.skillexchange.mapper.ReportMapper;
import com.example.skillexchange.service.ReportService;
import org.springframework.stereotype.Service;

/**
 * 用户举报表 Service实现类
 * 
 * 合规免责的重要功能：用户违规内容一键举报
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    /**
     * 提交举报
     */
    @Override
    public boolean submitReport(Report report) {
        report.setStatus(0);  // 0-待处理
        report.setHandleResult("");
        return save(report);
    }
}
