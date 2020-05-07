package eon.hg.fileserver.service;

import eon.hg.fileserver.util.dto.GroupDTO;
import eon.hg.fileserver.util.dto.Line;
import eon.hg.fileserver.util.dto.StorageDTO;

import java.util.List;

public interface MonitorService {
    List<GroupDTO> listGroupInfo();

    List<GroupDTO> listGroupInfo(boolean is_current);

    List<GroupDTO> listGroups();

    List<StorageDTO> listStorage(String groupName);

    List<StorageDTO> listStorageTop(String ipaddr);

    List<Line> getNetTrafficLines(String ip, String start, String end);

    Line getListStoragesInfo(String ip, String startTime, String endTime);

    List<Line> listStorageLines(String groupName, String type);

    StorageDTO getStorageByIp(String ip);

    List<Line> getListFileCountStorage(String ip, String startTime,
                                       String endTime);

    List<GroupDTO> getGroupsByName(String groupName);
}
