package com.booklog.apigateway.filter;

import com.booklog.apigateway.jwt.service.JwtService;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CustomAuthFilter extends AbstractGatewayFilterFactory<CustomAuthFilter.Config> {



    private JwtService jwtService;

    public CustomAuthFilter() {
        super(CustomAuthFilter.Config.class);
    }

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    @Override
    public GatewayFilter apply(CustomAuthFilter.Config config) {

        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info(":::::::::::::::::::::::::;;filter in:::::::::::::::::::::::::;;");
            System.out.println("filter in");

            if (!request.getHeaders().containsKey("access-token")) {
                System.out.println("no-access");
                return handleUnAuthorized(exchange); // 401 Error
            }

            List<String> token = request.getHeaders().get("access-token");
            String tokenString = Objects.requireNonNull(token).get(0);
            System.out.println(tokenString);

            if (!jwtService.checkToken(tokenString)) {
                System.out.println("jwt exception");
                return handleUnAuthorized(exchange); // 토큰이 일치하지 않을 때
            }
            //토큰이 일치하면 헤더에 memberId를 보내줌
            Long memberId = Long.parseLong(jwtService.getMemberIdFromAccessToken(tokenString));
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("Member-Id", memberId.toString())
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build()); // 토큰이 일치할 때
        });
    }


    private Mono<Void> handleUnAuthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    public static class Config {

    }
}
