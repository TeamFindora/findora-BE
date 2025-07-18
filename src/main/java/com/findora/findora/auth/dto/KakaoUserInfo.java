package com.findora.findora.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoUserInfo {
    private Long id; // 카카오 회원번호
    
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;
    
    @JsonProperty("properties")
    private Properties properties;
    
    @Data
    public static class KakaoAccount {
        private String email;
        
        @JsonProperty("email_verified")
        private Boolean emailVerified;
        
        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;
        
        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;
        
        private Profile profile;
    }
    
    @Data
    public static class Properties {
        private String nickname;
        
        @JsonProperty("profile_image")
        private String profileImage;
        
        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }
    
    @Data
    public static class Profile {
        private String nickname;
        
        @JsonProperty("profile_image_url")
        private String profileImageUrl;
        
        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;
        
        @JsonProperty("is_default_image")
        private Boolean isDefaultImage;
    }
} 