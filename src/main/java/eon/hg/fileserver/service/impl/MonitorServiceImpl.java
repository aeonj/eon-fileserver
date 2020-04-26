package eon.hg.fileserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.tobato.fastdfs.domain.GroupState;
import com.github.tobato.fastdfs.domain.StorageState;
import com.github.tobato.fastdfs.service.TrackerClient;
import com.jcraft.jsch.JSchException;
import eon.hg.fileserver.exception.ResultException;
import eon.hg.fileserver.service.MonitorService;
import eon.hg.fileserver.util.dto.GroupDTO;
import eon.hg.fileserver.util.dto.StorageDTO;
import eon.hg.fileserver.util.ssh.JsshProxy;
import eon.hg.fileserver.util.ssh.Machine;
import eon.hg.fileserver.util.ssh.SshTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    private TrackerClient trackerClient;

    @Override
    public List<GroupDTO> listGroupInfo() {
        List<GroupDTO> result = new ArrayList<>();
        List<GroupState> groupStates = trackerClient.listGroups();
        if (groupStates == null) {
            log.error("ERROR! list groups error, groups is null! ");
            return null;
        }
        log.info("group count: " + groupStates.size());
        Date date = new Date();

        for (GroupState groupStat : groupStates) {
            GroupDTO group = BeanUtil.toBean(groupStat, GroupDTO.class);
            List<StorageState> storageStateList = trackerClient.listStorages(groupStat.getGroupName());
            for (StorageState storageState : storageStateList) {
                StorageDTO storage = BeanUtil.toBean(storageState, StorageDTO.class);
                group.addStorage(storage);
            }
            result.add(group);
        }

        String cmd = "ps -aux|grep fdfs";
        for (Machine machine : SshTools.machines) {
            List<String> strList;
            if(machine.isConfigType())
                strList = SshTools.exeRemoteConsole(machine.getIp(),
                        machine.getUsername(), machine.getPassword(), cmd);
            else {
                try {
                    strList = new JsshProxy(machine.getIp(), machine.getUsername(), machine.getPort(), machine.getSsh()).execute(cmd).getExecuteLines();
                } catch (JSchException e) {
                    e.printStackTrace();
                    throw new ResultException(e);
                }
            }
            for (String str : strList) {
                if (str.contains("storage.conf")) {
                    for (GroupDTO group : result) {
                        group.setCreated(date);
                        for (StorageDTO storage : group.getStorageList()) {
                            if (machine.getIp().equalsIgnoreCase(
                                    storage.getIpAddr())) {
                                String[] strArrray = str.replaceAll(" +", ",")
                                        .split(",");
                                storage.setCpu(strArrray[2]);
                                storage.setMem(Float.parseFloat(strArrray[3]));


                            }

                        }
                    }
                }
            }
        }
        return result;
    }
}
