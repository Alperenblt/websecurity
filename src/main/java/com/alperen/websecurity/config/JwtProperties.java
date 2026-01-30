package com.alperen.websecurity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secret;

    private final Access access = new Access();
    private final Refresh refresh = new Refresh();

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Access getAccess() {
        return access;
    }

    public Refresh getRefresh() {
        return refresh;
    }

    public static class Access {
        private long expirationSeconds = 3600;
        private String cookieName = "access_token";

        public long getExpirationSeconds() {
            return expirationSeconds;
        }

        public void setExpirationSeconds(long expirationSeconds) {
            this.expirationSeconds = expirationSeconds;
        }

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }
    }

    public static class Refresh {
        private long expirationSeconds = 604800;
        private String cookieName = "refresh_token";
        private final Cookie cookie = new Cookie();
        private String hashPepper;

        public long getExpirationSeconds() {
            return expirationSeconds;
        }

        public void setExpirationSeconds(long expirationSeconds) {
            this.expirationSeconds = expirationSeconds;
        }

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public Cookie getCookie() {
            return cookie;
        }

        public String getHashPepper() {
            return hashPepper;
        }

        public void setHashPepper(String hashPepper) {
            this.hashPepper = hashPepper;
        }

        public static class Cookie {
            private boolean secure = false;
            private String sameSite = "Strict";

            public boolean isSecure() {
                return secure;
            }

            public void setSecure(boolean secure) {
                this.secure = secure;
            }

            public String getSameSite() {
                return sameSite;
            }

            public void setSameSite(String sameSite) {
                this.sameSite = sameSite;
            }
        }
    }
}
