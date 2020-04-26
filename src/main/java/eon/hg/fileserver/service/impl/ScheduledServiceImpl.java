package eon.hg.fileserver.service.impl;

import cn.hutool.core.convert.Convert;
import com.jcraft.jsch.JSchException;
import eon.hg.fileserver.mapper.TbDownRecordMapper;
import eon.hg.fileserver.mapper.TbWarningDataMapper;
import eon.hg.fileserver.model.TbDownRecord;
import eon.hg.fileserver.model.TbWarningData;
import eon.hg.fileserver.service.MonitorService;
import eon.hg.fileserver.util.dto.GroupDTO;
import eon.hg.fileserver.util.dto.StorageDTO;
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
    private TbDownRecordMapper downRecordMapper;
    @Resource
    private TbWarningDataMapper warningDataMapper;
    @Autowired
    private MonitorService monitorService;

    Map<String, Date> datemap = new HashMap<>();


    @Scheduled(cron = "0/50 * * * * *")
    public void scheduled(){
        log.info("=====>>>>>使用cron  {}",System.currentTimeMillis());
    }
    @Scheduled(fixedRate = 5000)
    public void scheduled1() {
        log.info("=====>>>>>使用fixedRate{}", System.currentTimeMillis());
    }
    @Scheduled(fixedDelay = 5000)
    public void scheduled2() {
        log.info("=====>>>>>fixedDelay{}",System.currentTimeMillis());
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 0 0/1 * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGroupByDay() {
        log.info("group day data upate begin...");
        List<GroupDTO> groupList =monitorService.listGroupInfo();
        log.info("group day data upated end");
    }

    private void warning(StorageDTO storage) {
        List<TbWarningData> warningDatas = warningDataMapper.findByIp(storage.getIpAddr());
        StringBuffer stringBuffer = new StringBuffer("异常服务器：" + storage.getIpAddr() + "</br>");
        if (!warningDatas.isEmpty()) {
            float wdCup = Float.parseFloat(warningDatas.get(0).getWdCpu());
            float wdMem = warningDatas.get(0).getWdMem();
            long wdFreeMB = warningDatas.get(0).getWdFreeMB();
            boolean res = true;
            if (Float.parseFloat(storage.getCpu()) > wdCup) {
                stringBuffer.append("cpu使用率当前值为： " + storage.getCpu() + "% 大于预警值：" + wdCup + "%</br>");
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
    public void readDataFromLoggerToDataBase()throws JSchException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        Date d = c.getTime();
        String date = df.format(d);
        for (Machine machine : SshTools.machines) {
            String cmd = "cat "+machine.getLogpath()+"/fastdfs_" + date + ".log";
            List<String> strList = new ArrayList<String>();
            if(machine.isConfigType())
                strList = SshTools.exeRemoteConsole(machine.getIp(),
                        machine.getUsername(), machine.getPassword(), cmd);
            else
                strList = new JsshProxy(machine.getIp(),machine.getUsername(),machine.getPort(),machine.getSsh()).execute(cmd).getExecuteLines();
            for (String str : strList) {
                String data[] = str.split(" ");
                if (data[8].equals("200")) {
                    //去数据库对应的表tbdownloadfilerecord中查询有没有fileId和对应的ip的DownloadFileRecord存在；
                    TbDownRecord downloadFileRecord = downRecordMapper.getByIpAndFile(machine.getIp(), Convert.toLong(data[6]));
                    if (downloadFileRecord != null) {
                        downloadFileRecord.setAccessCount(downloadFileRecord.getAccessCount() + 1);
                        downRecordMapper.update(downloadFileRecord);
                    } else {
                        downloadFileRecord = new TbDownRecord();
                        downloadFileRecord.setFileId(Convert.toLong(data[6]));
                        downloadFileRecord.setSrcIp(machine.getIp());
                        downloadFileRecord.setAccessCount(1l);
                        downRecordMapper.insert(downloadFileRecord);
                    }
                }
            }
        }
    }

}
