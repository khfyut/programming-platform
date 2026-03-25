package com.programming.mapper;

import com.programming.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SystemMonitorMapper {
    SystemMonitor getLatest();
    List<SystemMonitor> getByMetricName(@Param("metricName") String metricName, @Param("limit") int limit);
    void insertMetric(SystemMonitor monitor);
    
    List<ApiMetric> getApiMetrics();
    List<ApiMetric> getApiMetricsByPath(@Param("apiPath") String apiPath, @Param("limit") int limit);
    void insertApiMetric(ApiMetric metric);
    
    List<SandboxStatus> getSandboxStatus();
    void insertSandboxStatus(SandboxStatus status);
    void updateSandboxStatus(SandboxStatus status);
    
    List<SystemLog> getSystemLogs(int offset, int size);
    List<SystemLog> getSystemLogsByLevel(@Param("level") String level, @Param("page") int page, @Param("size") int size);
    void insertSystemLog(SystemLog log);
    
    List<AlertConfig> getAlertConfigs();
    void insertAlertConfig(AlertConfig config);
    void insertAlertRecord(AlertRecord record);
    List<AlertRecord> getAlertRecords(@Param("page") int page, @Param("size") int size);
}
