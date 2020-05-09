package eon.hg.fileserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import com.jcraft.jsch.JSchException;
import eon.hg.fileserver.mapper.DownRecordMapper;
import eon.hg.fileserver.mapper.SqlMapper;
import eon.hg.fileserver.mapper.WarningMapper;
import eon.hg.fileserver.model.DownRecord;
import eon.hg.fileserver.model.WarningData;
import eon.hg.fileserver.service.MonitorService;
import eon.hg.fileserver.util.SqlTools;
import eon.hg.fileserver.util.dto.GroupDTO;
import eon.hg.fileserver.util.dto.StorageDTO;
import eon.hg.fileserver.util.file.FDfsHandler;
import eon.hg.fileserver.util.ssh.JsshProxy;
import eon.hg.fileserver.util.ssh.Machine;
import eon.hg.fileserver.util.ssh.SshTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Async
@Service
public class ScheduledServiceImpl {
    @Resource
    private DownRecordMapper downRecordMapper;
    @Resource
    private SqlMapper sqlMapper;
    @Resource
    private WarningMapper warningDataMapper;
    @Autowired
    private MonitorService monitorService;

    Map<String, Date> datemap = new HashMap<>();

    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    public void scheduledGroupByMinutes() {
        log.info("group minutes data update begin...");
        updateGroup("");
        log.info("group minutes data updated end");
    }

    @Scheduled(cron = "0 0 */1 * * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    public void scheduledGroupByHour() {
        log.info("group hour data update begin...");
        updateGroup("hour");
        log.info("group hour data updated end");
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    public void scheduledGroupByDay() {
        log.info("group day data update begin...");
        updateGroup("day");
        log.info("group day data updated end");
    }

    private void updateGroup(String flag) {
        List<GroupDTO> groupList =monitorService.listGroupInfo(true);
        for (GroupDTO group : groupList) {
            Map mapGroup = BeanUtil.beanToMap(group);
            String group_id = IdUtil.fastSimpleUUID();
            mapGroup.put("id",group_id);
            mapGroup.remove("storageList");
            sqlMapper.insert(SqlTools.getInsertSql("tb_group"+flag,mapGroup));
            for (StorageDTO storage : group.getStorageList()) {
                Map mapStorage = BeanUtil.beanToMap(storage);
                mapStorage.put("id",IdUtil.fastSimpleUUID());
                mapStorage.put("group_id",group_id);
                mapStorage.put("curStatus", FDfsHandler.getStorageStatusCaption(storage.getStatus()));
                mapStorage.put("groupName",group.getGroupName());
                mapStorage.put("created",new Date());

                sqlMapper.insert(SqlTools.getInsertSql("tb_storage"+flag,mapStorage));
            }
        }
    }

    private void warning(StorageDTO storage) {
        WarningData warningData = warningDataMapper.getWarningDataByIpAddr(storage.getIpAddr());
        StringBuffer stringBuffer = new StringBuffer("异常服务器：" + storage.getIpAddr() + "</br>");
        if (warningData!=null) {
            float wdCpu = warningData.getWdCpu();
            float wdMem = warningData.getWdMem();
            long wdFreeMB = warningData.getWdFreeMB();
            boolean res = true;
            if (storage.getCpu() > wdCpu) {
                stringBuffer.append("cpu使用率当前值为： " + storage.getCpu() + "% 大于预警值：" + wdCpu + "%</br>");
                res = false;
            }
            if (storage.getMem() > wdMem) {
                stringBuffer.append("内存使用率当前值为： " + storage.getMem() + "% 大于预警值：" + wdMem + "%</br>");
                res = false;
            }
            if (storage.getFreeMB() < wdFreeMB) {
                stringBuffer.append("可用空间当前值为： " + storage.getFreeMB() + "MB 小于预警值：" + wdFreeMB + "MB</br>");
                res = false;
            }
            if (!res) {

            }
        }
    }

    public void warningOffline(StorageDTO storage) {
        boolean res = false;
        StringBuffer stringBuffer = new StringBuffer("异常服务器 ：" + storage.getIpAddr() + "</br>");
        if (storage.getCurStatus().equals("OFFLINE")) {
            stringBuffer.append("服务器停止工作");
            if (datemap.containsKey(storage.getIpAddr())) {
                Date offdate = datemap.get(storage.getIpAddr());
                Date now = new Date();
                long temp = now.getTime() - offdate.getTime();
                if (temp >= 1000 * 60 * 60) {
                    datemap.put(storage.getIpAddr(), new Date());
                    res = true;
                }
            } else {
                datemap.put(storage.getIpAddr(), new Date());
                res = true;
            }
        } else {
            if (datemap.containsKey(storage.getIpAddr())) {
                datemap.remove(storage.getIpAddr());
            }
        }
        if (res) {

        }
    }

    /**
     * 每天定时读取logger日志，并入库
     */
    @Scheduled(cron = "0 0 01 * * ?")
    public void readDataFromLoggerToDataBase() throws JSchException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        Date d = c.getTime();
        String date = df.format(d);
        for (Machine machine : SshTools.machines) {
            String cmd = "cat "+machine.getLogpath()+"/fastdfs_" + date + ".log";
            List<String> strList;
            if(machine.isConfigType())
                strList = SshTools.exeRemoteConsole(machine.getIp(),machine.getPort(),
                        machine.getUsername(), machine.getPassword(), cmd);
            else
                strList = new JsshProxy(machine.getIp(),machine.getUsername(),machine.getPort(),machine.getSsh()).execute(cmd).getExecuteLines();
            for (String str : strList) {
                String data[] = str.split(" ");
                if (data[8].equals("200")) {
                    //去数据库对应的表tb_downrecord中查询有没有fileId和对应的ip的DownloadFileRecord存在；
                    DownRecord downloadFileRecord = downRecordMapper.getByIpAndFile(machine.getIp(), Convert.toLong(data[6]));
                    if (downloadFileRecord != null) {
                        downloadFileRecord.setAccessCount(downloadFileRecord.getAccessCount() + 1);
                        downRecordMapper.update(downloadFileRecord);
                    } else {
                        downloadFileRecord = new DownRecord();
                        downloadFileRecord.setFileId(data[6]);
                        downloadFileRecord.setSrcIp(machine.getIp());
                        downloadFileRecord.setAccessCount(1l);
                        downRecordMapper.insert(downloadFileRecord);
                    }
                }
            }
        }
    }

}
