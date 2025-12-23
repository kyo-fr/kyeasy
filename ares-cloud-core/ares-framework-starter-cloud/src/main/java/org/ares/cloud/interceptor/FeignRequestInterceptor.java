package org.ares.cloud.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.ares.cloud.common.constant.Constants;
import org.ares.cloud.common.constant.MateData;
import org.ares.cloud.common.context.ApplicationContext;
import org.ares.cloud.common.utils.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * @author hugo
 * @version 1.0
 * @description: feign请求扩展
 * @date 2024/10/3 22:20
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // 设置 Accept 头，确保服务器返回 JSON 格式
        template.header(HttpHeaders.ACCEPT, "application/json");
        // 将 JWT token 放入 Authorization 请求头中
        template.header(HttpHeaders.AUTHORIZATION, Constants.TOKEN_PREFIX + ApplicationContext.getToken());
        //用户id
        if (StringUtils.isNotBlank(ApplicationContext.getUserId())) {
            template.header(MateData.USER_ID, ApplicationContext.getUserId());
        }
        // powers
        if (StringUtils.isNotBlank(ApplicationContext.getPowers())) {
            template.header(MateData.POWERS, ApplicationContext.getPowers());
        }
        // 应用主键
        if (StringUtils.isNotBlank(ApplicationContext.getAppId())) {
            template.header(MateData.APP_ID, ApplicationContext.getAppId());
        }
        //客户端id
        if (StringUtils.isNotBlank(ApplicationContext.getClientId())){
            template.header(MateData.CLIENT_ID, ApplicationContext.getClientId());
        }
        // 真实的客户端地址
        if (StringUtils.isNotBlank(ApplicationContext.getRealIpAddress())){
            template.header(MateData.REAL_IP_ADDRESS, ApplicationContext.getRealIpAddress());
        }
        //链路的id
        if (StringUtils.isNotBlank(ApplicationContext.getTraceId())){
            template.header(MateData.TRACE_Id,ApplicationContext.getTraceId());
        }
        // 用户身份
        if (StringUtils.isNotBlank(ApplicationContext.getStrIdentity())){
            template.header(MateData.IDENTITY,ApplicationContext.getStrIdentity());
        }
        // 租户id
        if (StringUtils.isNotBlank(ApplicationContext.getTenantId())){
            template.header(MateData.TENANT_Id,ApplicationContext.getTenantId());
        }
        //是否忽略租户
        template.header(MateData.IGNORE_TENANT_ID,ApplicationContext.getIgnoreTenant().toString());
        if (ApplicationContext.getLocale() != null) {
            Locale locale = ApplicationContext.getLocale();
            String localeString = locale.getLanguage() + "_" + locale.getCountry();
            template.header(MateData.Accept_Language,localeString);
        }
        //自定义的扩展
        if (ApplicationContext.getMateDataMap() != null) {
            for (Map.Entry<String, Object> entry : ApplicationContext.getMateDataMap().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(MateData.MATE_PREFIX)) {
                    template.header(key, entry.getValue().toString());
                }
            }
        }
    }
}
