package com.booklog.apigateway.jwt.service;


public interface JwtService {

    boolean checkToken(String jwt);

    String getMemberIdFromAccessToken(String accessToken);

    Object parseClaims(String accessToken);
}
