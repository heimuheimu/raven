package com.heimuheimu.raven.monitor.falcon;

import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.falcon.support.AbstractFalconDataCollector;
import com.heimuheimu.raven.IMServer;
import com.heimuheimu.raven.IMServerHolder;
import com.heimuheimu.raven.constant.FalconDataCollectorConstant;
import com.heimuheimu.raven.monitor.IMClientMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IM 客户端信息 Falcon 监控数据采集器。该采集器采集周期为 30 秒，每次采集将会返回以下数据项：
 * <ul>
 *     <li>raven_client_established_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 当前保持连接的 IM 客户端数量</li>
 *     <li>raven_client_created_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内创建的 IM 客户端数量</li>
 *     <li>raven_client_closed_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内关闭的 IM 客户端数量</li>
 *     <li>raven_client_established_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 IM 客户端创建失败的次数</li>
 *     <li>raven_client_closed_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 IM 客户端关闭失败的次数</li>
 *     <li>raven_client_timeout_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生超时错误的 IM 客户端数量</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class IMClientDataCollector extends AbstractFalconDataCollector {

    /**
     * 上一次已创建的 IM 客户端数量
     */
    private volatile long lastCreatedCount = 0;

    /**
     * 上一次已关闭的 IM 客户端数量
     */
    private volatile long lastClosedCount = 0;

    /**
     * 上一次 IM 客户端发生错误的次数，Key 为错误代码，Value 为上一次错误次数
     */
    private final ConcurrentHashMap<Integer, Long> lastErrorCountMap = new ConcurrentHashMap<>();

    private final Map<Integer, String> errorMetricSuffixMap;

    public IMClientDataCollector() {
        errorMetricSuffixMap = new HashMap<>();
        errorMetricSuffixMap.put(IMClientMonitor.ERROR_CODE_FAILS_TO_ESTABLISH, "_client_established_error_count");
        errorMetricSuffixMap.put(IMClientMonitor.ERROR_CODE_FAILS_TO_CLOSE, "_client_closed_error_count");
        errorMetricSuffixMap.put(IMClientMonitor.ERROR_CODE_TIMEOUT, "_client_timeout_count");
    }

    @Override
    public List<FalconData> getList() {
        List<FalconData> falconDataList = new ArrayList<>();

        long establishedClientCount = 0;
        IMServer server = IMServerHolder.get();
        if (server != null) {
            establishedClientCount = Math.max(server.getEstablishedClientCount(), 0);
        }
        falconDataList.add(create("_client_established_count", establishedClientCount));

        IMClientMonitor monitor = IMClientMonitor.getInstance();
        long createdCount = monitor.getCreatedCount();
        falconDataList.add(create("_client_created_count", createdCount - lastCreatedCount));
        lastCreatedCount = createdCount;

        long closedCount = monitor.getClosedCount();
        falconDataList.add(create("_client_closed_count", closedCount - lastClosedCount));
        lastClosedCount = closedCount;

        for (Integer errorCode : errorMetricSuffixMap.keySet()) {
            long errorCount = monitor.getErrorCount(errorCode);
            Long lastErrorCount = lastErrorCountMap.get(errorCode);
            if (lastErrorCount == null) {
                lastErrorCount = 0L;
            }
            falconDataList.add(create(errorMetricSuffixMap.get(errorCode), errorCount - lastErrorCount));
            lastErrorCountMap.put(errorCode, errorCount);
        }
        return falconDataList;
    }

    @Override
    protected String getModuleName() {
        return FalconDataCollectorConstant.MODULE_NAME;
    }

    @Override
    public int getPeriod() {
        return FalconDataCollectorConstant.REPORT_PERIOD;
    }
}
