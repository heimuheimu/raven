package com.heimuheimu.raven.clients;

import com.heimuheimu.raven.constant.BeanStatusEnum;
import com.heimuheimu.raven.facility.Methods;
import com.heimuheimu.raven.facility.UnusableServiceNotifier;
import com.heimuheimu.raven.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * IM 客户端管理器列表，提供自动恢复功能。
 *
 * <p><strong>说明：</strong>IMClientManagerList 类是线程安全的，可在多个线程中使用同一个实例。</p>
 */
public class IMClientManagerList implements Closeable {

    private static final Logger RAVEN_IM_CLIENT_MANAGER_LOG = LoggerFactory.getLogger("RAVEN_IM_CLIENT_MANAGER_LOG");
    
    private static final Logger LOGGER = LoggerFactory.getLogger(IMClientManagerList.class);

    /**
     * IM 客户端管理器数量
     */
    private final int poolSize;

    /**
     * IM 客户端管理器使用的配置信息
     */
    private final IMClientManagerConfiguration managerConfiguration;

    /**
     * IM 客户端管理器列表事件监听器，可能为 {@code null}
     */
    private final IMClientManagerListListener managerListListener;

    /**
     * IM 客户端管理器列表，该列表大小为 {@code poolSize}
     */
    private final CopyOnWriteArrayList<IMClientManager> managerList = new CopyOnWriteArrayList<>();

    /**
     * {@link #managerList} 元素发生变更操作时，使用的私有锁
     */
    private final Object managerListUpdateLock = new Object();

    /**
     * IM 客户端管理器恢复任务是否运行，访问该变量需使用锁 {@link #rescueTaskLock}
     */
    private boolean isRescueTaskRunning = false;

    /**
     * IM 客户端管理器恢复任务使用的私有锁
     */
    private final Object rescueTaskLock = new Object();

    /**
     * IM 客户端管理器列表所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * 构造一个 IM 客户端管理器列表。
     *
     * @param poolSize IM 客户端管理器数量，不允许小于等于 0
     * @param managerConfiguration IM 客户端管理器使用的配置信息，不允许为 {@code null}
     * @param managerListListener IM 客户端管理器列表事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果所有的 IM 客户端管理器均不可用，将会抛出此异常
     */
    public IMClientManagerList(int poolSize, IMClientManagerConfiguration managerConfiguration,
                               IMClientManagerListListener managerListListener) throws IllegalStateException {
        this.poolSize = poolSize;
        this.managerConfiguration = managerConfiguration;
        UnusableServiceNotifier<IMClientManager> prevUnusableServiceNotifier = managerConfiguration.getUnusableServiceNotifier();
        managerConfiguration.setUnusableServiceNotifier(manager -> {
            removeUnavailableClient(manager);
            if (prevUnusableServiceNotifier != null) {
                prevUnusableServiceNotifier.onClosed(manager);
            }
        });

        this.managerListListener = managerListListener;

        boolean hasAvailableManager = false;
        boolean isNeedStartRescueTask = false;
        for (int i = 0; i < poolSize; i++) {
            String name = getManagerName(i);
            boolean isSuccess = create(-1, name);
            if (isSuccess) {
                hasAvailableManager = true;
                Methods.invokeIfNotNull("IMClientManagerListListener#onCreated(String name)", buildParamsMap(name),
                        managerListListener, () -> managerListListener.onCreated(name));
            } else {
                isNeedStartRescueTask = true;
                RAVEN_IM_CLIENT_MANAGER_LOG.error("Add `IMClientManager` to `IMClientManagerList` failed. {}",
                        LogBuildUtil.build(buildParamsMap(name)));
                Methods.invokeIfNotNull("IMClientManagerListListener#onClosed(String name)", buildParamsMap(name),
                        managerListListener, () -> managerListListener.onClosed(name));
            }
        }

        if ( !hasAvailableManager ) {
            String errorMessage = "There is no available `IMClientManager`." + LogBuildUtil.build(buildParamsMap(null));
            LOGGER.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        if (isNeedStartRescueTask) {
            startRescueTask();
        }
    }

    /**
     * 获得一个空闲的 IM 客户端管理器，如果当前无可用管理器，将返回 {@code null}。
     *
     * @return IM 客户端管理器，可能为 {@code null}
     */
    public IMClientManager getIdleManager() {
        if (state != BeanStatusEnum.NORMAL) { // 如果已关闭，直接返回 null
            return null;
        }
        int minimumIMClientCount = Integer.MAX_VALUE;
        IMClientManager idleManager = null;
        for (int i = 0; i < poolSize; i++) { // 寻找最空闲的 Manager 后返回（并发情况下，不一定精准）
            IMClientManager manager = managerList.get(i);
            if (manager != null && manager.isActive()) {
                int imClientCount = manager.getClientCount();
                if (imClientCount >= 0 && imClientCount < minimumIMClientCount) {
                    minimumIMClientCount = imClientCount;
                    idleManager = manager;
                }
            }
        }
        return idleManager;
    }

    /**
     * 获得配置的 IM 客户端管理器数量。
     *
     * @return 配置的 IM 客户端管理器数量
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * 获得指定索引位置的 IM 客户端管理器，如果当前列表已关闭，或者索引越界，将会返回 {@code null}。
     *
     * @param index 索引位置
     * @return IM 客户端管理器，可能为 {@code null}
     */
    public IMClientManager get(int index) {
        if (index < 0 || index >= poolSize) { // 索引越界
            return null;
        }
        if (state != BeanStatusEnum.NORMAL) { // 已关闭
            return null;
        }
        return managerList.get(index);
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            for (IMClientManager manager : managerList) {
                if (manager != null) {
                    manager.close();
                }
            }

            LinkedHashMap<String, Object> params = buildParamsMap(null);
            params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
            RAVEN_IM_CLIENT_MANAGER_LOG.info("Closed IMClientManagerList.{}", LogBuildUtil.build(params));
        }
    }

    /**
     * 根据 IM 客户端管理器在列表中的索引位置生成对应的名称。
     *
     * @param index 索引位置
     * @return IM 客户端管理器名称
     */
    private String getManagerName(int index) {
        return "raven-im-client-manager-" + index;
    }

    /**
     * 创建一个 IM 客户端管理器。
     *
     * @param index IM 客户端管理器在列表中的索引位置
     * @param name IM 客户端管理器名称
     * @return 是否创建成功
     */
    private boolean create(int index, String name) {
        IMClientManager manager = null;
        try {
            manager = new IMClientManager(name, managerConfiguration);
            manager.init();
        } catch (Exception ignored) {}

        synchronized (managerListUpdateLock) {
            if (manager != null && manager.isActive()) {
                if (index < 0) {
                    managerList.add(manager);
                } else {
                    managerList.set(index, manager);
                }
                return true;
            } else {
                if (index < 0) {
                    managerList.add(null);
                } else {
                    managerList.set(index, null);
                }
                RAVEN_IM_CLIENT_MANAGER_LOG.error("Add `IMClientManager` to `IMClientManagerList` failed. {}",
                        LogBuildUtil.build(buildParamsMap(name)));
                return false;
            }
        }
    }

    /**
     * 从列表中移除不可用的 IM 客户端管理器。
     *
     * @param unavailableManager 不可用的 IM 客户端管理器
     */
    private void removeUnavailableClient(IMClientManager unavailableManager) throws NullPointerException {
        if (unavailableManager == null) { //should not happen, just for bug detection
            throw new NullPointerException("Remove unavailable manager failed: `null client`." +
                    LogBuildUtil.build(buildParamsMap(null)));
        }
        boolean isRemoveSuccess = false;
        int index;
        synchronized (managerListUpdateLock) {
            index = managerList.indexOf(unavailableManager);
            if (index >= 0) {
                managerList.set(index, null);
                isRemoveSuccess = true;
                RAVEN_IM_CLIENT_MANAGER_LOG.debug("Remove `IMClientManager` from `IMClientManagerList` success.{}",
                        LogBuildUtil.build(buildParamsMap(unavailableManager.getName())));
            }
        }
        if (isRemoveSuccess && (state != BeanStatusEnum.CLOSED)) {
            startRescueTask();
            Methods.invokeIfNotNull("IMClientManagerListListener#onClosed(String name)", buildParamsMap(unavailableManager.getName()),
                    managerListListener, () -> managerListListener.onClosed(unavailableManager.getName()));
        }
    }

    private LinkedHashMap<String, Object> buildParamsMap(String managerName) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        if (managerName != null) {
            params.put("managerName", managerName);
        }
        params.put("poolSize", poolSize);
        params.put("managerConfiguration", managerConfiguration);
        return params;
    }

    /**
     * 启动 IM 客户端管理器恢复任务。
     */
    private void startRescueTask() {
        if (state == BeanStatusEnum.NORMAL) {
            synchronized (rescueTaskLock) {
                if (!isRescueTaskRunning) {
                    Thread rescueThread = new Thread() {

                        @Override
                        public void run() {
                            long startTime = System.currentTimeMillis();
                            RAVEN_IM_CLIENT_MANAGER_LOG.info("`IMClientManager` rescue task has been started.");
                            try {
                                while (state == BeanStatusEnum.NORMAL) {
                                    boolean hasRecovered = true;
                                    for (int i = 0; i < poolSize; i++) {
                                        if (managerList.get(i) == null) {
                                            String managerName = getManagerName(i);
                                            boolean isSuccess = create(i, managerName);
                                            if (isSuccess) {
                                                RAVEN_IM_CLIENT_MANAGER_LOG.info("Rescue `IMClientManager` success.{}", LogBuildUtil.build(buildParamsMap(managerName)));
                                                Methods.invokeIfNotNull("IMClientManagerListListener#onRecovered(String name)", buildParamsMap(managerName),
                                                        managerListListener, () -> managerListListener.onRecovered(managerName));
                                            } else {
                                                hasRecovered = false;
                                                RAVEN_IM_CLIENT_MANAGER_LOG.error("Rescue `IMClientManager` failed. {}", LogBuildUtil.build(buildParamsMap(managerName)));
                                            }
                                        }
                                    }
                                    if (hasRecovered) {
                                        break;
                                    } else {
                                        Thread.sleep(1000); //还有未恢复的管理器，等待 1s 后继续尝试
                                    }
                                }
                                RAVEN_IM_CLIENT_MANAGER_LOG.info("`IMClientManager` rescue task has been finished. `cost`:`{}ms`.",
                                        System.currentTimeMillis() - startTime);
                            } catch (Exception e) { //should not happen, just for bug detection
                                String errorMessage = "`IMClientManager` rescue task execute failed: `unexpected error`. `cost`:`"
                                        + (System.currentTimeMillis() - startTime) + "ms`.";
                                RAVEN_IM_CLIENT_MANAGER_LOG.info(errorMessage, e);
                                LOGGER.error(errorMessage, e);
                            } finally {
                                rescueOver();
                            }
                        }

                        private void rescueOver() {
                            synchronized (rescueTaskLock) {
                                isRescueTaskRunning = false;
                            }
                        }
                    };
                    rescueThread.setName("raven-manager-rescue-task");
                    rescueThread.setDaemon(true);
                    rescueThread.start();
                    isRescueTaskRunning = true;
                }
            }
        }
    }
}
