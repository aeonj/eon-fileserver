package eon.hg.fileserver.controller;

import eon.hg.fileserver.authorization.annotation.Authorization;
import eon.hg.fileserver.model.App;
import eon.hg.fileserver.model.WarningData;
import eon.hg.fileserver.model.WarningUser;
import eon.hg.fileserver.service.ManageService;
import eon.hg.fileserver.util.body.PageBody;
import eon.hg.fileserver.util.body.ResultBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage")
public class ManageController {
    @Autowired
    private ManageService manageService;

    @Authorization
    @GetMapping("/app/list")
    public PageBody appList(int page,int limit) {
        return PageBody.success().addPageInfo(manageService.loadAppList(),page,manageService.loadAppList().size());
    }

    @Authorization
    @PostMapping("/app/insert")
    public ResultBody appInsert(App app) {
        App vf = manageService.getAppByAppNo(app.getApp_no());
        if (vf!=null) {
            return ResultBody.failed("应用编号已经存在了");
        }
        manageService.insertApp(app);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/app/update")
    public ResultBody appUpdate(App app) {
        App vf = manageService.getAppByAppNo(app.getApp_no());
        if (vf!=null && vf.getId()!=app.getId()) {
            return ResultBody.failed("应用编号已经存在了");
        }
        manageService.updateApp(app);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/app/delete")
    public ResultBody appDelete(App app) {
        manageService.deleteApp(app);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/app/batchdel")
    public ResultBody appBatchDelete(@RequestBody List<App> apps) {
        manageService.batchDeleteApp(apps);
        return ResultBody.success();
    }

    @Authorization
    @GetMapping("/warning/data/list")
    public PageBody warningDataList(int page,int limit) {
        return PageBody.success().addPageInfo(manageService.loadWarningDataList(),page,manageService.loadWarningDataList().size());
    }

    @Authorization
    @PostMapping("/warning/data/insert")
    public ResultBody warningDataInsert(WarningData obj) {
        WarningData vf = manageService.getWarningDataByIpAddr(obj.getWdIpAddr());
        if (vf!=null) {
            return ResultBody.failed("服务器IP已经存在了");
        }
        manageService.insertWarningData(obj);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/warning/data/update")
    public ResultBody warningDataUpdate(WarningData obj) {
        WarningData vf = manageService.getWarningDataByIpAddr(obj.getWdIpAddr());
        if (vf!=null && vf.getId()!=obj.getId()) {
            return ResultBody.failed("服务器IP已经存在了");
        }
        manageService.updateWarningData(obj);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/warning/data/delete")
    public ResultBody warningDataDelete(WarningData obj) {
        manageService.deleteWarningData(obj);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/warning/data/batchdel")
    public ResultBody warningDataBatchDelete(@RequestBody List<WarningData> objs) {
        manageService.batchDeleteWarningData(objs);
        return ResultBody.success();
    }

    @Authorization
    @GetMapping("/warning/user/list")
    public PageBody warningUserList(int page,int limit) {
        return PageBody.success().addPageInfo(manageService.loadWarningUserList(),page,manageService.loadWarningUserList().size());
    }

    @Authorization
    @PostMapping("/warning/user/insert")
    public ResultBody warningUserInsert(WarningUser obj) {
        WarningUser vf = manageService.getWarningUserByName(obj.getName());
        if (vf!=null) {
            return ResultBody.failed("用户名已经存在了");
        }
        manageService.insertWarningUser(obj);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/warning/user/update")
    public ResultBody warningUserUpdate(WarningUser obj) {
        WarningUser vf = manageService.getWarningUserByName(obj.getName());
        if (vf!=null && vf.getId()!=obj.getId()) {
            return ResultBody.failed("用户名已经存在了");
        }
        manageService.updateWarningUser(obj);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/warning/user/delete")
    public ResultBody warningUserDelete(WarningUser obj) {
        manageService.deleteWarningUser(obj);
        return ResultBody.success();
    }

    @Authorization
    @PostMapping("/warning/user/batchdel")
    public ResultBody warningUserBatchDelete(@RequestBody List<WarningUser> objs) {
        manageService.batchDeleteWarningUser(objs);
        return ResultBody.success();
    }

}
