package com.hql.cacheutils;

/**
 * @author ly-huangql
 * <br /> Create time : 2019/6/28
 * <br /> Description :
 */
public class DataBean {
    private String title;
    private String url;

    public DataBean(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
