package eon.hg.fileserver.controller;

import eon.hg.fileserver.authorization.annotation.Authorization;
import eon.hg.fileserver.model.App;
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

}
