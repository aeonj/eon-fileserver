package eon.hg.fileserver.service;

import eon.hg.fileserver.model.App;
import eon.hg.fileserver.model.FileInfo;

import java.util.List;

public interface ManageService {

    List<FileInfo> getAppFiles(String app_no);

    List<App> loadAppList();

    void insertApp(App app);

    App getAppByAppNo(String app_no);

    void updateApp(App app);

    void deleteApp(App app);

    void batchDeleteApp(List<App> apps);
}
