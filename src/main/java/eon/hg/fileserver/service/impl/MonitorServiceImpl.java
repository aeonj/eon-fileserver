package eon.hg.fileserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import com.github.tobato.fastdfs.domain.GroupState;
import com.github.tobato.fastdfs.domain.StorageState;
import com.github.tobato.fastdfs.service.TrackerClient;
import eon.hg.fileserver.mapper.SqlMapper;
import eon.hg.fileserver.service.MonitorService;
import eon.hg.fileserver.util.dto.GroupDTO;
import eon.hg.fileserver.util.dto.Line;
import eon.hg.fileserver.util.dto.StorageDTO;
import eon.hg.fileserver.util.file.FDfsHandler;
import eon.hg.fileserver.util.ssh.JsshProxy;
import eon.hg.fileserver.util.ssh.Machine;
import eon.hg.fileserver.util.ssh.SshTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {
    @Resource
    private SqlMapper sqlMapper;
    @Autowired
    private TrackerClient trackerClient;

    @Override
    public List<GroupDTO> listGroupInfo() {
        return listGroupInfo(false);
    }

    @Override
    public List<GroupDTO> listGroupInfo(boolean is_current) {
        List<GroupDTO> result = new ArrayList<>();
        List<GroupState> groupStates = trackerClient.listGroups();
        if (groupStates == null) {
            log.error("ERROR! list groups error, groups is null! ");
            return null;
        }
        log.info("group count: " + groupStates.size());

        for (GroupState groupStat : groupStates) {
            GroupDTO group = BeanUtil.toBean(groupStat, GroupDTO.class);
            List<StorageState> storageStateList = trackerClient.listStorages(groupStat.getGroupName());
            for (StorageState storageState : storageStateList) {
                StorageDTO storage = BeanUtil.toBean(storageState, StorageDTO.class);
                storage.setCurStatus(FDfsHandler.getStorageStatusCaption(storage.getStatus()));
                group.addStorage(storage);
            }
            result.add(group);
        }

        if (is_current) {
            Date date = new Date();
            String cmd = "ps -aux|grep fdfs";
            for (Machine machine : SshTools.machines) {
                List<String> strList;
                if (machine.isConfigType())
                    strList = SshTools.exeRemoteConsole(machine.getIp(),machine.getPort(),
                            machine.getUsername(), machine.getPassword(), cmd);
                else {
                        strList = new JsshProxy(machine.getIp(), machine.getUsername(), "root123456",machine.getPort()).execute(cmd).getExecuteLines();
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
                                    storage.setCpu(Convert.toFloat(strArrray[2]));
                                    storage.setMem(Convert.toFloat(strArrray[3]));


                                }

                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<GroupDTO> toGroupBean(List<Map<String, Object>> list) {
        List<GroupDTO> groups = new ArrayList<>();
        for (Map<String, Object> map : list) {
            groups.add(BeanUtil.mapToBeanIgnoreCase(map,GroupDTO.class,false));
        }
        return groups;
    }

    private List<StorageDTO> toStorageBean(List<Map<String, Object>> list) {
        List<StorageDTO> storages = new ArrayList<>();
        for (Map<String, Object> map : list) {
            storages.add(BeanUtil.mapToBeanIgnoreCase(map,StorageDTO.class,false));
        }
        return storages;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<GroupDTO> listGroups() {
        StringBuilder queryString = new StringBuilder(
                "select g.* from tb_group as g GROUP BY groupName");
        return toGroupBean(sqlMapper.selectList(queryString.toString()));
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<StorageDTO> listStorage(String groupName) {
        StringBuilder queryString = new StringBuilder(
                "select s.* from tb_storage as s where  s.groupName='" + groupName
                        + "' group by s.ipAddr");
        return toStorageBean(sqlMapper.selectList(queryString.toString()));

    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<StorageDTO> listStorageTop(String ipaddr) {
        StringBuilder queryString = new StringBuilder(
                "select s.* from tb_storage as s where  s.ipAddr='" + ipaddr
                        + "' order by s.created desc limit 0,10");
        return toStorageBean(sqlMapper.selectList(queryString.toString()));

    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Line> getNetTrafficLines(String ip, String start, String end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Line> lines = new ArrayList<Line>();
        Date starttime, endtime;
        String entity = "TB_Storage";
        try {
            starttime = sdf.parse(start);
            endtime = sdf.parse(end);
            long second = endtime.getTime() - starttime.getTime();
            if (second > 3600 * 1000 * 5 && second < 3600 * 1000 * 24 * 7) {
                entity = "TB_StorageHour";
            } else if (second >= 3600 * 1000 * 24 * 7) {
                entity = "TB_StorageDay";
            }
            log.info(second + "");
        } catch (Exception e) {
            log.info("date parse error use default!");
            Calendar calendar = Calendar.getInstance();
            endtime = calendar.getTime();
            calendar.add(Calendar.HOUR, -6);
            starttime = calendar.getTime();
        }
        String sql = "select * from "
                + entity
                + " s where s.ipAddr='"+ip+"' and s.created between '"+starttime+"' and '"+endtime+"' order by s.created";
        List<StorageDTO> storages = toStorageBean(sqlMapper.selectList(sql));

        Line uploadLine = new Line("上传流量");
        Line downLoadLine = new Line("下载流量");
        lines.add(uploadLine);
        lines.add(downLoadLine);
        for (int i = 0; i < storages.size(); i++) {
            StorageDTO storage = storages.get(i);
            Date created;
            long upload;
            long download;
                created = storage.getCreated();
                upload = storage.getTotalUploadBytes() / (1024 * 1024);
                download = storage.getTotalDownloadloadBytes() / (1024 * 1024);

            uploadLine.getData().add(new Long[]{created.getTime(), upload});
            downLoadLine.getData().add(
                    new Long[]{created.getTime(), download});
        }
        return lines;
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public Line getListStoragesInfo(String ip, String startTime, String endTime)  {
        Line sc = new Line(ip);
        sc.setName(ip);
        System.out.println(startTime + "!!!!!!!!!!!!!!!!!!!" + endTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String start = null, end = null, d = sdf.format(new Date());
        try {
            start = (startTime == null || startTime.equals("")) ? "0000-00-00 00:00"
                    : startTime;
            end = (endTime == null || endTime.equals("")) ? d : endTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        String str = "from TB_StorageHour as s where s.ipAddr='" + ip
                + "' and s.created between '" + start + "' and '" + end + "'"
                + " order by s.created desc";
        System.out.println(str);
        List<StorageDTO> s = toStorageBean(sqlMapper.selectList(str));
        for (int i = s.size() - 1; i >= 0; i--) {
            Date created = s.get(i).getCreated();
            if (created == null) {
                continue;
            }
            sc.getData().add(
                    new Long[]{created.getTime(), s.get(i).getFreeMB()});
        }
        return sc;
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Line> listStorageLines(String groupName,String type) {
        List<Line> lines = new ArrayList<Line>();
        List<StorageDTO> storages = listStorage(groupName);
        for (StorageDTO s : storages) {
            String sql="select s.* from TB_Storage s where s.ipAddr='"+s.getIpAddr()+"' order by s.created desc limit 10";
            List<StorageDTO> results = toStorageBean(sqlMapper.selectList(sql));
            Line line = new Line(s.getIpAddr());
            for (int i = results.size() - 1; i >= 0; i--) {
                StorageDTO ss = results.get(i);
                if ("mem".equals(type)) {
                    line.getData().add(new Object[]{ss.getCreated().getTime(),
                                    ss.getMem()});
                } else {
                    line.getData().add(new Object[]{ss.getCreated().getTime(),
                            ss.getCpu()});
                }

            }
            lines.add(line);
        }
        return lines;
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public StorageDTO getStorageByIp(String ip) {
        System.out.println(ip);
        String str = "from Tb_StorageHour as s where s.ipAddr='" + ip
                + "' order by s.created desc limit 1,1";
        List<StorageDTO> dtoList = toStorageBean(sqlMapper.selectList(str));
        return dtoList.get(0);
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Line> getListFileCountStorage(String ip, String startTime,
                                              String endTime) {
        List<Line> lines = new ArrayList<Line>();
        Date start = null, end = null, d1 = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String format = sdf.format(d1);
            start = (startTime == "" || startTime == null) ? sdf
                    .parse("0000-00-00 00:00") : sdf.parse(startTime);
            end = (endTime == "" || endTime == null) ? sdf.parse(format) : sdf
                    .parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String str = "select * from TB_StorageHour as s where s.ipAddr='"+ip+"' and s.created between '"+start+"' and '"+end+"' order by s.created";
        List<StorageDTO> storages = toStorageBean(sqlMapper.selectList(str));
        Line uploadLine = new Line("上传文件数量");
        Line downLoadLine = new Line("下载文件数量");
        lines.add(uploadLine);
        lines.add(downLoadLine);
        for (int i = 0; i <= storages.size() - 1; i++) {
            long u = 0;
            long d = 0;
            if (i > 0) {
                u = storages.get(i - 1).getSuccessUploadCount();
                d = storages.get(i - 1).getSuccessDownloadCount();
            }
            StorageDTO storage = storages.get(i);
            Date created = storage.getCreated();
            if (created == null) {
                continue;
            }
            long totalUpload = storage.getSuccessUploadCount();
            long totalDownload = storage.getSuccessDownloadCount();
            uploadLine.getData().add(
                    new Long[]{created.getTime(), (totalUpload - u)});
            downLoadLine.getData().add(
                    new Long[]{created.getTime(), (totalDownload - d)});
        }

        return lines;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<GroupDTO> getGroupsByName(String groupName){
        String str = "select gd.* from TB_GroupDay as gd where gd.groupName='"+groupName+"' order by gd.created asc";
        return toGroupBean(sqlMapper.selectList(str));
    }

}
