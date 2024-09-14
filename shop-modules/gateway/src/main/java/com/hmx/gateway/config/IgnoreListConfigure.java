package com.hmx.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 白名单列表
 */
@ConfigurationProperties(prefix = "ignore")
@Configuration
@RefreshScope
public class IgnoreListConfigure {

    // pattern
    private List<String> ignoreList = new ArrayList<>();


    public List<String> getIgnoreList() {
        return ignoreList;
    }

    public void setIgnoreList(List<String> ignoreList) {
        this.ignoreList = ignoreList;
    }


    public boolean isIgnorePath(String path){
        for (String ignore : ignoreList) {
            Pattern pattern = Pattern.compile(ignore);
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()){
                return true;
            }
        }
        return false;
    }




}
