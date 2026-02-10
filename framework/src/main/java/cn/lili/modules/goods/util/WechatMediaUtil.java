package cn.lili.modules.goods.util;

import cn.hutool.json.JSONObject;
import cn.lili.common.enums.ClientTypeEnum;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.modules.wechat.util.WechatAccessTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信媒体工具
 *
 * @author Bulbasaur
 * @since 2021/5/19 8:02 下午
 */
@Slf4j
@Component
public class WechatMediaUtil {

    private static final String MEDIA_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";

    private static final String BOUNDARY = "----WebKitFormBoundaryOYXo8heIv9pgpGjT";

    private static final int MAX_RETRY = 1;

    private static final int BUFFER_SIZE = 4096;

    private static final Map<String, String> CONTENT_TYPE_EXT_MAP = new HashMap<>();

    static {
        CONTENT_TYPE_EXT_MAP.put("image/png", ".png");
        CONTENT_TYPE_EXT_MAP.put("image/jpeg", ".jpeg");
        CONTENT_TYPE_EXT_MAP.put("image/jpg", ".jpg");
    }

    @Autowired
    private WechatAccessTokenUtil wechatAccessTokenUtil;

    /**
     * 上传多媒体数据到微信服务器
     *
     * @param type         媒体文件类型
     * @param mediaFileUrl 来自网络上面的媒体文件地址
     * @return media_id
     */
    public String uploadMedia(String type, String mediaFileUrl) {
        return doUploadMedia(type, mediaFileUrl, 0);
    }

    private String doUploadMedia(String type, String mediaFileUrl, int retryCount) {
        String accessToken = wechatAccessTokenUtil.cgiAccessToken(ClientTypeEnum.WECHAT_MP);
        String mediaStr = MEDIA_URL.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);

        String result = doPost(mediaStr, mediaFileUrl);

        JSONObject jsonObject = new JSONObject(result);
        log.info("微信媒体上传：{}", jsonObject);

        Object errcode = jsonObject.get("errcode");
        if (errcode != null) {
            if ("40001".equals(String.valueOf(errcode)) && retryCount < MAX_RETRY) {
                wechatAccessTokenUtil.removeAccessToken(ClientTypeEnum.WECHAT_MP);
                return doUploadMedia(type, mediaFileUrl, retryCount + 1);
            }
            throw new ServiceException(String.valueOf(jsonObject.get("errmsg")));
        }
        return jsonObject.get("media_id").toString();
    }

    private String doPost(String uploadUrl, String mediaFileUrl) {
        HttpURLConnection uploadConn = null;
        HttpURLConnection mediaConn = null;
        try {
            // 打开媒体文件连接，获取内容类型
            mediaConn = (HttpURLConnection) new URL(mediaFileUrl).openConnection();
            mediaConn.setRequestMethod("GET");
            mediaConn.setDoInput(true);
            String contentType = mediaConn.getHeaderField("Content-Type");
            String fileExt = judgeType(contentType);

            // 打开上传连接
            uploadConn = (HttpURLConnection) new URL(uploadUrl).openConnection();
            uploadConn.setDoInput(true);
            uploadConn.setDoOutput(true);
            uploadConn.setUseCaches(false);
            uploadConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            uploadConn.setRequestMethod("POST");

            // 写入 multipart 请求体
            try (OutputStream out = uploadConn.getOutputStream();
                 InputStream in = mediaConn.getInputStream();
                 BufferedInputStream bufferedIn = new BufferedInputStream(in)) {

                out.write(("--" + BOUNDARY + "\r\n").getBytes());
                out.write(("Content-Disposition: form-data; name=\"media\";\r\n"
                        + "filename=\"" + System.currentTimeMillis() + fileExt + "\"\r\n"
                        + "Content-Type: " + contentType + "\r\n\r\n").getBytes());

                byte[] buffer = new byte[BUFFER_SIZE];
                int size;
                while ((size = bufferedIn.read(buffer)) != -1) {
                    out.write(buffer, 0, size);
                }
                // 换行符不能少，否则将会报41005错误
                out.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());
                out.flush();
            }

            // 读取响应
            try (InputStream resultIn = uploadConn.getInputStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resultIn))) {
                StringBuilder resultStr = new StringBuilder();
                String tempStr;
                while ((tempStr = bufferedReader.readLine()) != null) {
                    resultStr.append(tempStr);
                }
                return resultStr.toString();
            }
        } catch (IOException e) {
            log.error("微信媒体上传失败", e);
            throw new ServiceException(ResultCode.ERROR);
        } finally {
            if (mediaConn != null) {
                mediaConn.disconnect();
            }
            if (uploadConn != null) {
                uploadConn.disconnect();
            }
        }
    }

    /**
     * 通过传过来的contentType判断是哪一种类型
     *
     * @param contentType 获取来自连接的contentType
     * @return 文件扩展名
     */
    public String judgeType(String contentType) {
        String fileExt = CONTENT_TYPE_EXT_MAP.get(contentType);
        if (fileExt == null) {
            throw new ServiceException(ResultCode.IMAGE_FILE_EXT_ERROR);
        }
        return fileExt;
    }
}
