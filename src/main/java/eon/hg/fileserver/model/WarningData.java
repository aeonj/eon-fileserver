package eon.hg.fileserver.model;

import java.io.Serializable;

public class WarningData implements Serializable {
    private Long id;
    private long wdFreeMB;  //free disk storage in MB
    private float wdCpu;
    private float wdMem;
    private String wdIpAddr;
    private String wdGroupName;

    public WarningData() {
    }

    public WarningData(Long id, long wdFreeMB, float wdCpu, float wdMem, String wdIpAddr, String wdGroupName) {
        this.id = id;
        this.wdFreeMB = wdFreeMB;
        this.wdCpu = wdCpu;
        this.wdMem = wdMem;
        this.wdIpAddr = wdIpAddr;
        this.wdGroupName = wdGroupName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getWdFreeMB() {
        return wdFreeMB;
    }

    public void setWdFreeMB(long wdFreeMB) {
        this.wdFreeMB = wdFreeMB;
    }

    public float getWdCpu() {
        return wdCpu;
    }

    public void setWdCpu(float wdCpu) {
        this.wdCpu = wdCpu;
    }

    public float getWdMem() {
        return wdMem;
    }

    public void setWdMem(float wdMem) {
        this.wdMem = wdMem;
    }

    public String getWdIpAddr() {
        return wdIpAddr;
    }

    public void setWdIpAddr(String wdIpAddr) {
        this.wdIpAddr = wdIpAddr;
    }

    public String getWdGroupName() {
        return wdGroupName;
    }

    public void setWdGroupName(String wdGroupName) {
        this.wdGroupName = wdGroupName;
    }
}
