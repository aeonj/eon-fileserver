package eon.hg.fileserver.util.dto;

import com.github.tobato.fastdfs.domain.StorageState;

import java.util.Date;

public class StorageDTO extends StorageState {
    private String cpu;
    private float mem;
    private Date created;
    private String curStatus;

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public float getMem() {
        return mem;
    }

    public void setMem(float mem) {
        this.mem = mem;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCurStatus() {
        return curStatus;
    }

    public void setCurStatus(String curStatus) {
        this.curStatus = curStatus;
    }
}
