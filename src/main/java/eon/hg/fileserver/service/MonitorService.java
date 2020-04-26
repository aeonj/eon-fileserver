package eon.hg.fileserver.service;

import eon.hg.fileserver.util.dto.GroupDTO;

import java.util.List;

public interface MonitorService {
    List<GroupDTO> listGroupInfo();
}
