package eon.hg.fileserver.util.dto;

import com.github.tobato.fastdfs.domain.GroupState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupDTO extends GroupState{
    private Date created;
    private List<StorageDTO> storageList = new ArrayList<>();

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<StorageDTO> getStorageList() {
        return storageList;
    }

    public GroupDTO addStorage(StorageDTO storage) {
        this.storageList.add(storage);
        return this;
    }

}
