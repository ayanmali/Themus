package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommitVerification {
    private boolean verified;
    private String reason;
    private String signature;
    private String payload;
    private String verifiedAt;

    public GithubCommitVerification() {}

    public GithubCommitVerification(boolean verified, String reason, String signature, String payload, String verifiedAt) {
        this.verified = verified;
        this.reason = reason;
        this.signature = signature;
        this.payload = payload;
        this.verifiedAt = verifiedAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(String verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
