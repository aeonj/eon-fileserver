package eon.hg.fileserver.authorization.model;

/**
 * com.ecaray.authority.authorization.annotation
 * Author ：zhxy
 * 2017/2/20 20:19
 * 说明：Token的Model类，可以增加字段提高安全性，例如时间戳、url签名
 */

public class TokenModel {

    //用户id
    private String userId;

    //随机生成的uuid
    private String token;

    private String realname;

    public TokenModel(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }
}
