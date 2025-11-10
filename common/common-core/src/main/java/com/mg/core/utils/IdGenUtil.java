package com.mg.core.utils;


import com.mg.core.exception.ServerException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * packageName com.mg.core.utils
 *
 * @author mj
 * @className IdGenUtil
 * @date 2025/5/27
 * @description TODO
 */

/**
 * 雪花算法 ID 生成工具类
 * 提供基于雪花算法的分布式唯一 ID 生成能力
 * 支持生成 Long 和 String 类型的 ID
 */
public class IdGenUtil {
    // 2000-01-01 00:00:00 (北京时间)
    private static final long START_TIMESTAMP = 946656000000L;
    // 各部分位数定义
    private static final long SEQUENCE_BITS = 12;
    private static final long MACHINE_BITS = 5;
    private static final long DATACENTER_BITS = 5;
    // 最大值限制
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_BITS);
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    // 位移常量
    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS + DATACENTER_BITS;
    // 当前数据中心ID和机器ID
    private static final long datacenterId;
    private static final long machineId;
    // 当前序列号和上一次生成ID的时间戳
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    static {
        // 初始化数据中心ID和机器ID
        datacenterId = initDatacenterId();
        machineId = initMachineId();
    }

    /**
     * 私有构造函数防止外部实例化
     */
    private IdGenUtil() {
    }

    /**
     * 生成下一个 Long 类型的雪花ID
     *
     * @return 返回一个全局唯一的 Long 类型ID
     * @throws RuntimeException 如果系统时钟回拨则抛出异常
     */
    public static synchronized long nextId() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new ServerException("系统时钟回拨，拒绝生成分布式ID");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(timestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }

    /**
     * 生成下一个 String 类型的雪花ID
     *
     * @return 返回一个全局唯一的 String 类型ID
     */
    public static String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 等待下一毫秒直到时间大于当前时间戳
     *
     * @param currentMillis 当前时间戳
     * @return 返回新的时间戳
     */
    private static long waitNextMillis(long currentMillis) {
        long now = currentTimeMillis();
        while (now <= currentMillis) {
            now = currentTimeMillis();
        }
        return now;
    }

    /**
     * 获取当前系统时间戳
     *
     * @return 当前时间戳（毫秒）
     */
    private static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 根据本机网卡MAC地址计算并初始化机器ID
     *
     * @return 返回计算后的机器ID
     */
    private static long initMachineId() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nic = interfaces.nextElement();
                byte[] mac = nic.getHardwareAddress();
                if (mac != null) {
                    int hash = 0;
                    for (byte b : mac) {
                        hash = (hash << 5) - hash + b;
                    }
                    return (hash & 0xfffffff) % (MAX_MACHINE_ID + 1);
                }
            }
        } catch (SocketException ignored) {
        }
        return 1L;
    }

    /**
     * 根据本机主机名计算并初始化数据中心ID
     *
     * @return 返回计算后的数据中心ID
     */
    private static long initDatacenterId() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return (hostname.hashCode() & 0xfffffff) % (MAX_DATACENTER_ID + 1);
        } catch (UnknownHostException ignored) {
        }
        return 1L;
    }
}