package org.ares.cloud.filter;

import org.ares.cloud.common.constant.MateData;
import org.ares.cloud.common.enums.ResponseCodeEnum;
import org.ares.cloud.common.exception.BaseErrorInfoInterface;
import org.ares.cloud.common.exception.BaseException;
import org.ares.cloud.common.model.Result;
import org.ares.cloud.common.utils.StringUtils;
import org.ares.cloud.properties.GatewayProperties;
import org.ares.cloud.tuils.SecurityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AuthFilter implements WebFilter {
    private final WebClient webClient; // 用于请求认证中心
    // 从 Nacos 中读取公开路径
    private final GatewayProperties gatewayProperties;

    public AuthFilter(@Qualifier("authWebClient") WebClient authWebClient, GatewayProperties gatewayProperties) {
        this.webClient = authWebClient;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        addCorsHeaders(exchange);
        // OPTIONS 请求直接放行
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return handleOptionsRequest(exchange.getResponse());
        }

         String originalHost = exchange.getRequest().getHeaders().getFirst("Host");

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("Custom-Host", originalHost) // 强制设置为 Host 的值
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (isNeedAuth(exchange) || StringUtils.hasText(token)) {
            if (StringUtils.isBlank(token)) {
                // 写入响应体，并返回 Mono<Void> 以符合方法签名
                return withResult(exchange.getResponse(), Result.error(ResponseCodeEnum.RECODE_TOKEN_BE_OVERDUE));
            }
            if (!token.startsWith("Bearer ")) {
                //token格式不正确
                return withResult(exchange.getResponse(), Result.error(ResponseCodeEnum.RECODE_TOKEN_BE_OVERDUE));
            }
            token = token.substring(7);  // 去掉 Bearer 前缀
            return authCheck(exchange,chain,token);
        }
        return chain.filter(mutatedExchange);  // 如果没有 token，继续请求链
    }

    // 处理 4xx 错误
    private Mono<Throwable> handleClientError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new RuntimeException(new RuntimeException("账号无效"))))  // 如果 body 是空的，提供一个默认的空字符串
                .flatMap(errorBody -> {
                    // 构建自定义的 Result 错误响应
                    Result<String> errorResponse = Result.error(response.statusCode().value(), "请求错误: " + errorBody);
                    // 抛出带有 Result 错误响应的异常
                    return Mono.error(new BaseException(HttpStatus.UNAUTHORIZED.value(), errorResponse.getReason()));
                });
    }

    private Authentication authenticateToken(String token, String userId, String role) {
        // 实现你的 token 验证逻辑
        // 验证通过后返回 Authentication 对象，验证失败返回 null
        // 例如：通过 JWT 解码、验证 token 等
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(StringUtils.isBlank(role) ? "ROLE_USER" : role)
        );
        // Create an Authentication object
        User userDetails = new User(userId, "non-empty-password", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
    /**
     * 
     * @param token
     * @return
     */
    private Authentication authenticateToken(String token) {
        // 在这里你可以简单处理 token，比如判断它是否符合某种格式
        // 此处为了演示，创建一个简单的匿名 Authentication 对象
        return new UsernamePasswordAuthenticationToken("anonymousUser", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    }
    /**
     * 是否需要认证
     *
     * @param exchange web
     * @return 是否需要验证
     */
    private boolean isNeedAuth(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getURI().getPath();
        boolean flag = !SecurityUtils.isPathOpen(gatewayProperties.getPublicPaths(), requestPath);
        return flag;
    }

    /**
     * 添加跨域请求头
     *
     * @param exchange web
     */
    private void addCorsHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 获取请求的 Origin
        String origin = request.getHeaders().getFirst("Origin");
        if (origin != null) {
            response.getHeaders().setAccessControlAllowOrigin(origin);
            response.getHeaders().setAccessControlAllowCredentials(true);
            response.getHeaders().setAccessControlAllowMethods(Arrays.asList(
                    HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,
                    HttpMethod.DELETE, HttpMethod.OPTIONS
            ));
            response.getHeaders().setAccessControlAllowHeaders(Arrays.asList(
                    HttpHeaders.AUTHORIZATION,
                    HttpHeaders.CONTENT_TYPE,
                    HttpHeaders.ACCEPT,
                    HttpHeaders.ORIGIN,
                    "X-Requested-With",
                    "timeout",
                    MateData.USER_ID,
                    MateData.TENANT_Id,
                    MateData.ROLE,
                    MateData.IDENTITY,
                    "X-scope"
            ));
        }
    }
       
  
    /**
     * 
     * @param <T>
     * @param response
     * @param result
     * @return
     */
    // 删除 addCorsHeaders 方法，因为现在由网关统一处理
    
    private <T> Mono<Void> withResult(ServerHttpResponse response, Result<T> result) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = bufferFactory.wrap(result.toString().getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }
    /**
     * 处理 OPTIONS 请求
     * @param response 响应对象
     * @return Mono<Void>
     */
    private Mono<Void> handleOptionsRequest(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.OK);
        return response.setComplete();
    }

    /**
     * 认证检查
     * @param chain w
     * @return w
     */
    private Mono<Void> authCheck(ServerWebExchange exchange,WebFilterChain chain,String token) {
        // 请求认证中心校验 token 并获取用户信息
        return this.webClient.post()
                .uri("/validate")
                .bodyValue(token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .bodyToMono(Map.class) // 获取解析后的用户信息
                .flatMap(claims -> {
                    // 获取 code
//                    Object codeObj = claims.get("code");
//                    int code = (codeObj instanceof Number) ? ((Number) codeObj).intValue() : 500; // 默认 500
//
//                    if (code != 200) {
//                        // 如果 code 不是 200，终止执行并返回错误
//                        Result<String> errorResult = Result.error(code, "认证失败，错误码：" + code);
//                        return withResult(exchange.getResponse(), errorResult);
//                    }
                    String userId = claims.get("userId") != null ? claims.get("userId").toString() : "";
                    String role = claims.get("role") != null ? claims.get("role").toString() : "";
                    // 将解析出的用户信息加入请求头
                    exchange.getRequest().mutate()
                            //用户ID
                            .header(MateData.USER_ID, userId)
                            //租户id
                            .header(MateData.TENANT_Id, claims.get("tenantId") != null ? claims.get("tenantId").toString() : "")
                            //角色
                            .header(MateData.ROLE, role)
                            //身份
                            .header(MateData.IDENTITY, claims.get("identity") != null ? claims.get("identity").toString() : "")
                            //权限
                            .header("X-scope", claims.get("scope") != null ? claims.get("scope").toString() : "");
                    Authentication authentication = authenticateToken(token);
                    return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)); // 继续请求链
                }).onErrorResume(e -> {
                    e.printStackTrace();
                    Result<String> errorResult = Result.error(500, "内部服务器错误：" + e.getMessage());
                    // 捕获其他异常并返回自定义响应
                    if (e instanceof BaseErrorInfoInterface) {
                        errorResult = Result.error((BaseErrorInfoInterface) e);
                    }
                    ServerHttpResponse response = exchange.getResponse();
                    // 写入响应体，并返回 Mono<Void> 以符合方法签名
                    return withResult(response, errorResult);
                });
    }
}
