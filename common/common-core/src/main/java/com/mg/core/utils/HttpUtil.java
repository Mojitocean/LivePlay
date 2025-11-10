package com.mg.core.utils;

import com.mg.core.exception.ServerException;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * packageName com.mg.core.utils
 *
 * @author mj
 * @className HttpUtil
 * @date 2025/5/26
 * @description TODO
 */
@Log4j2
public class HttpUtil {

    // 请求体类型
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    // 绕过SSL证书检测连接池
    private static final OkHttpClient SSL_CLIENT = HttpClientUtil.createUnsafeOkHttpClient();
    // 安全的连接池
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
            .connectTimeout(300, TimeUnit.SECONDS) // 连接超时时间为300秒
            .readTimeout(300, TimeUnit.SECONDS)    // 读取超时时间为300秒
            .writeTimeout(300, TimeUnit.SECONDS)   // 写入超时时间为300秒
            //    NONE：不记录任何日志。
            //    BASIC：仅记录请求方法和URL以及响应码和消息。
            //    HEADERS：除了BASIC级别的信息外，还包括请求和响应头。
            //    BODY：最详细的级别，包括请求和响应的所有内容（头部和主体）。
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build();

    /**
     * get请求不带参数
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String doGet(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doGet--请求失败，返回CODE：{}", response.code());
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doGet--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * get请求不带参数
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static byte[] doGetByte(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doGetBody--请求失败，返回CODE：{}", response.code());
            return response.body().bytes();
        } catch (Exception e) {
            throw new ServerException("--doGetBody--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * get请求带参数
     *
     * @param url
     * @param params
     * @return
     */
    public static String doGetParams(String url, Map<String, String> params) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String fullUrl = urlBuilder.build().toString();
        Request request = new Request.Builder().url(fullUrl).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doGetParams--请求失败，返回CODE：{}", response.code());
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doGetParams--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * get请求带参数
     *
     * @param url
     * @param params
     * @return
     */
    public static String doGetParamsHeaders(String url, Map<String, String> params, Headers headers) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String fullUrl = urlBuilder.build().toString();
        Request request = new Request.Builder().url(fullUrl).headers(headers).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doGetParamsHeaders--请求失败，返回CODE：{}", response.code());
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doGetParamsHeaders--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * 执行带参数和请求头的GET请求
     *
     * @param url     请求的URL
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String doGetParamsWithHeaders(String url, Map<String, String> params, Map<String, String> headers) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String fullUrl = urlBuilder.build().toString();

        Request.Builder requestBuilder = new Request.Builder().url(fullUrl);

        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.info("--doGetParamsWithHeaders--请求失败，返回CODE：{}", response.code());
                throw new ServerException("--doGetParamsWithHeaders--请求失败，返回CODE：" + response.code());
            }
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doGetParamsWithHeaders--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * get非安全请求带参数
     *
     * @param url
     * @param params
     * @return
     */
    public static String doGetParamsSSL(String url, Map<String, String> params) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String fullUrl = urlBuilder.build().toString();
        Request request = new Request.Builder().url(fullUrl).build();
        try (Response response = SSL_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doGetParamsSSL--请求失败，返回CODE：{}", response.code());
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doGetParamsSSL--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * get请求带请求体
     *
     * @param url
     * @param json
     * @return
     */
    public static String doGetByte(String url, String json) {
        // 创建请求体
        RequestBody body = RequestBody.create(json, MEDIA_TYPE);
        // 构建GET请求
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .get()  // 显式指定GET方法
                .post(body)  // 使用POST方法来添加请求体
                .build();
        // 执行请求
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new ServerException("--doGetBody--请求失败，返回CODE：" + response.code());
            // 返回响应体的内容
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doGetBody--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * get非安全请求带请求体
     *
     * @param url
     * @param json
     * @return
     */
    public static String doGetBodySSL(String url, String json) {
        // 创建请求体
        RequestBody body = RequestBody.create(json, MEDIA_TYPE);
        // 构建GET请求
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .get()  // 显式指定GET方法
                .post(body)  // 使用POST方法来添加请求体
                .build();
        // 执行请求
        try (Response response = SSL_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doGetBodySSL--请求失败，返回CODE：{}", response.code());
            // 返回响应体的内容
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doGetBodySSL--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * post请求
     *
     * @param url
     * @param json
     * @return
     * @throws Exception
     */
    public static String doPostJson(String url, String json) {
        RequestBody body = RequestBody.create(json, MEDIA_TYPE);
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response != null && !response.isSuccessful())
                log.info("--doPostJson--请求失败，返回CODE：{}", response);
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doPostJson--请求异常，异常信息：" + e.getMessage());
        }
    }


    public static String doPostJsonList(String url, String json) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.info("--doPostJson--请求失败，返回CODE：{}", response.code());
                return null;
            }

            // 读取响应体
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null || responseBody.contentLength() == 0) {
                    return null;
                }
                return responseBody.string();
            }

        } catch (IOException e) {
            log.info("--doPostJson--请求异常，异常信息：{}", e.getMessage());
            return null;
        }
    }

    /**
     * post请求-带请求头
     *
     * @param url
     * @param json
     * @param headers
     * @return
     */
    public static String doPostJsonHeaders(String url, String json, Headers headers) {
        RequestBody body = RequestBody.create(json, MEDIA_TYPE);
        Request request = new Request.Builder().url(url).post(body).headers(headers).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doPostJsonHeaders--请求失败，返回CODE：{}", response.code());
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doPostJsonHeaders--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * POST非安全请求-带请求头
     *
     * @param url
     * @param json
     * @param headers
     * @return
     */
    public static String doPostJsonHeadersSSL(String url, String json, Headers headers) {
        RequestBody body = RequestBody.create(json, MEDIA_TYPE);
        Request request = new Request.Builder().url(url).post(body).headers(headers).build();
        try (Response response = SSL_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doPostJsonHeadersSSL--请求失败，返回CODE：{}", response.code());
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doPostJsonHeadersSSL--请求异常，异常信息：" + e.getMessage());
        }
    }

    /**
     * POST请求-表单参数
     *
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static String doPostFormData(String url, Map<String, String> params, Headers headers) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .headers(headers)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doPostFormData--请求失败，返回CODE：{}", response.code());
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doPostFormData--请求异常，异常信息：" + e.getMessage());
        }
    }


    /**
     * POST非安全请求-表单参数-带多附件
     *
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static String doPostFormDataSSL(String url, Map<String, String> params, List<MultipartFile> files, String keyName, Headers headers) {
        // 创建MultipartBody.Builder对象
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        // 添加表单字段
        for (Map.Entry<String, String> entry : params.entrySet()) {
            multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        // 添加文件
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    multipartBuilder.addFormDataPart(
                            keyName,  // 这里可以指定服务器端接收文件的键名
                            file.getOriginalFilename(),
                            RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType()))
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        RequestBody requestBody = multipartBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .headers(headers)
                .build();
        try (Response response = SSL_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful())
                log.info("--doPostFormDataSSL--请求失败，返回CODE：{}", response.code());
            return response.body().string();
        } catch (Exception e) {
            throw new ServerException("--doPostFormDataSSL--请求异常，异常信息：" + e.getMessage());
        }
    }


    /**
     * 内部类，用于绕过SSL检测
     */
    private static class HttpClientUtil {

        // 创建一个允许所有主机名的主机名验证器
        private static final HostnameVerifier trustAllHosts = (hostname, session) -> true;

        // 创建一个信任所有证书的信任管理器
        private static final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // 信任所有客户端证书
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // 信任所有服务器证书
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        /**
         * 创建一个绕过SSL证书的OkHttpClient
         *
         * @return
         */
        private static OkHttpClient createUnsafeOkHttpClient() {
            try {
                // 获取默认的SSL上下文
                SSLContext sslContext = SSLContext.getInstance("TLS");
                // 使用信任所有证书的信任管理器初始化SSL上下文
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // 创建一个SSLSocketFactory，它使用我们刚刚初始化的SSL上下文
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                // 创建一个新的OkHttpClient.Builder
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                // 设置自定义的SSLSocketFactory和主机名验证器
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier(trustAllHosts);
                return builder
                        .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 连接超时时间为10秒
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)    // 读取超时时间为30秒
                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)   // 写入超时时间为30秒
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

}