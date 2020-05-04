package eon.hg.fileserver.controller;

import eon.hg.fileserver.service.MonitorService;
import eon.hg.fileserver.util.body.ResultBody;
import eon.hg.fileserver.util.dto.GroupDTO;
import eon.hg.fileserver.util.dto.Line;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/manage")
public class MonitorController {
    @Autowired
    private MonitorService monitorService;

    @RequestMapping("/performance")
    public ModelAndView performance() {
        ModelAndView mv = new ModelAndView("/monitor/performance");
        List<GroupDTO> groups = monitorService.listGroupInfo();

        mv.addObject("groups", groups);

        return mv;
    }

    @RequestMapping("/performance/init")
    public ResultBody initPerformance() {
        return ResultBody.success().addObject(monitorService.listGroupInfo());
    }

    @RequestMapping("/capactity/init")
    public ResultBody initCapactity() {
        return ResultBody.success().addObject(monitorService.listGroupInfo());
    }



    @RequestMapping("/performance/getLine")
    public List<Line> getPerformanceLine(String groupName,String type) {
        return monitorService.listStorageLines(groupName,type);
    }

    @RequestMapping("/capactity/getLine")
    public List<Line> capactityStorage(String ip, String startTime,
                                       String endTime) {
        System.out.println(ip);
        List<Line> result = new ArrayList<>();
        result.add(monitorService.getListStoragesInfo(ip, startTime, endTime));
        return result;
    }
}