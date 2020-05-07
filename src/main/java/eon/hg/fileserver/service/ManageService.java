package eon.hg.fileserver.service;

import eon.hg.fileserver.model.App;
import eon.hg.fileserver.model.FileInfo;
import eon.hg.fileserver.model.WarningData;
import eon.hg.fileserver.model.WarningUser;

import java.util.List;

public interface ManageService {

    List<FileInfo> getAppFiles(String app_no);

    List<App> loadAppList();

    void insertApp(App app);

    App getAppByAppNo(String app_no);

    void updateApp(App app);

    void deleteApp(App app);

    void batchDeleteApp(List<App> apps);

    List<WarningData> loadWarningDataList();

    WarningData getWarningDataByIpAddr(String ipAddr);

    void insertWarningData(WarningData obj);

    void updateWarningData(WarningData obj);

    void deleteWarningData(WarningData obj);

    void batchDeleteWarningData(List<WarningData> objs);

    List<WarningUser> loadWarningUserList();

    WarningUser getWarningUserByName(String name);

    void insertWarningUser(WarningUser obj);

    void updateWarningUser(WarningUser obj);

    void deleteWarningUser(WarningUser obj);

    void batchDeleteWarningUser(List<WarningUser> objs);
}
