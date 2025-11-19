package com.mg.service;

import com.mg.redis.utils.RedisUtil;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.redisson.api.RBucket;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * packageName com.mg.service
 *
 * @author mj
 * @className FFmpegRelayTask
 * @date 2025/11/19
 * @description TODO
 */
public class FFmpegRelayTask implements Callable<String> {
    private final String rtspUrl;
    private final String rtmpUrl;
    private final String channelKey;
    private final String cameraId;


    public FFmpegRelayTask(String rtspUrl, String rtmpUrl, String channelKey, String cameraId) {
        this.rtspUrl = rtspUrl;
        this.rtmpUrl = rtmpUrl;
        this.channelKey = channelKey;
        this.cameraId = cameraId;

    }

    @Override
    public String call() throws Exception {
        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;
        try {
            // 初始化 grabber
            grabber = FFmpegFrameGrabber.createDefault(rtspUrl);
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setOption("stimeout", "2000000");
            grabber.start();

            int width = grabber.getImageWidth() > 0 ? grabber.getImageWidth() : 640;
            int height = grabber.getImageHeight() > 0 ? grabber.getImageHeight() : 480;
            double frameRate = grabber.getFrameRate() > 0 ? grabber.getFrameRate() : 25;

            // 初始化 recorder
            recorder = new FFmpegFrameRecorder(rtmpUrl, width, height, 0);
            recorder.setInterleaved(true);
            recorder.setGopSize((int) Math.max(1, Math.round(frameRate)));
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("crf", "28");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("flv");
            recorder.setFrameRate(frameRate);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.start();

            // 推流循环：以 redis key 存在为继续条件，同时响应中断
            RBucket<String> bucket = RedisUtil.getCacheObject(channelKey);
            while (!Thread.currentThread().isInterrupted() && bucket.isExists()) {
                // 抓一帧（阻塞）
                org.bytedeco.javacv.Frame frame = grabber.grab();
                if (frame != null) {
                    try {
                        recorder.record(frame);
                    } catch (Exception e) {
                        // 单帧异常：记录并继续
                        e.printStackTrace();
                    }
                } else {
                    // 当 grab 返回 null 时，短暂休眠避免 busy-loop
                    Thread.sleep((long) (1000.0 / Math.max(1, Math.round(frameRate))));
                }
                // 定期刷新 redis TTL，避免因为任务运行时间长被误回收
                bucket.expire(60, TimeUnit.SECONDS);
            }

            return "finished";
        } catch (InterruptedException ie) {
            // 处理中断：优雅退出
            Thread.currentThread().interrupt();
            throw ie;
        } finally {
            // 清理资源：删除 redis 键并释放 grabber/recorder
            try {
                RedisUtil.deleteObject(channelKey);
            } catch (Exception ignored) {
            }
            try {
                RedisUtil.deleteObject("camera:map:" + cameraId);
            } catch (Exception ignored) {
            }

            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception ignored) {
                }
            }
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                } catch (Exception ignored) {
                }
            }
        }
    }
}