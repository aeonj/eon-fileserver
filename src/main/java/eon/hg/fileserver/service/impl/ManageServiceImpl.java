package eon.hg.fileserver.service.impl;

import eon.hg.fileserver.mapper.AppMapper;
import eon.hg.fileserver.mapper.FileMapper;
import eon.hg.fileserver.mapper.WarningMapper;
import eon.hg.fileserver.model.App;
import eon.hg.fileserver.model.FileInfo;
import eon.hg.fileserver.model.WarningData;
import eon.hg.fileserver.model.WarningUser;
import eon.hg.fileserver.service.ManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ManageServiceImpl implements ManageService {
    @Resource
    private AppMapper appMapper;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private WarningMapper warningMapper;

    @Override
    public List<FileInfo> getAppFiles(String app_no) {
        List<FileInfo> files = fileMapper.selectByAppNo(app_no);
        return files;
    }

    @Override
    public List<App> loadAppList() {
        return appMapper.getAll();
    }

    @Override
    public void insertApp(App app) {
        appMapper.insert(app);
    }

    @Override
    public App getAppByAppNo(String app_no) {
        return appMapper.getByAppNo(app_no);
    }

    @Override
    public void updateApp(App app) {
        appMapper.update(app);
    }

    @Override
    public void deleteApp(App app) {
        appMapper.delete(app.getId());
    }

    @Override
    public void batchDeleteApp(List<App> apps) {
        for (App app: apps) {
            appMapper.delete(app.getId());
        }
    }

    @Override
    public List<WarningData> loadWarningDataList() {
        return warningMapper.getAllWarningData();
    }

    @Override
    public WarningData getWarningDataByIpAddr(String ipAddr) {
        return warningMapper.getWarningDataByIpAddr(ipAddr);
    }

    @Override
    public void insertWarningData(WarningData obj) {
        warningMapper.insertWarningData(obj);
    }

    @Override
    public void updateWarningData(WarningData obj) {
        warningMapper.updateWarningData(obj);
    }

    @Override
    public void deleteWarningData(WarningData obj) {
        warningMapper.deleteWarningData(obj.getId());
    }

    @Override
    public void batchDeleteWarningData(List<WarningData> objs) {
        for (WarningData obj: objs) {
            warningMapper.deleteWarningData(obj.getId());
        }
    }

    @Override
    public List<WarningUser> loadWarningUserList() {
        return warningMapper.getAllWarningUser();
    }

    @Override
    public WarningUser getWarningUserByName(String name) {
        return warningMapper.getWarningUserByName(name);
    }

    @Override
    public void insertWarningUser(WarningUser obj) {
        warningMapper.insertWarningUser(obj);
    }

    @Override
    public void updateWarningUser(WarningUser obj) {
        warningMapper.updateWarningUser(obj);
    }

    @Override
    public void deleteWarningUser(WarningUser obj) {
        warningMapper.deleteWarningUser(obj.getId());
    }

    @Override
    public void batchDeleteWarningUser(List<WarningUser> objs) {
        for (WarningUser obj: objs) {
            warningMapper.deleteWarningUser(obj.getId());
        }
    }
    
}
