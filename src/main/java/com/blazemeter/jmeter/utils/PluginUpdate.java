package com.blazemeter.jmeter.utils;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 8/22/12
 * Time: 14:41
 */
public class PluginUpdate {
    public PluginVersion getVersion() {
        return version;
    }

    public void setVersion(PluginVersion version) {
        this.version = version;
    }

    public String getMoreInfoUrl() {
        return moreInfoUrl;
    }

    public void setMoreInfoUrl(String moreInfoUrl) {
        this.moreInfoUrl = moreInfoUrl;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    PluginVersion version;
    String moreInfoUrl;
    String changes;
    String downloadUrl;

    public PluginUpdate(PluginVersion version, String downloadUrl, String changes, String moreInfoUrl) {
        this.version = version;
        this.moreInfoUrl = moreInfoUrl;
        this.changes = changes;
        this.downloadUrl = downloadUrl;
    }

}
