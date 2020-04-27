package eon.hg.fileserver.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import eon.hg.fileserver.config.FileServerProperties;
import eon.hg.fileserver.model.FileInfo;
import eon.hg.fileserver.service.FileService;
import eon.hg.fileserver.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/file/manage")
public class ManageController {
    @Autowired
    private FileServerProperties fileServerProperties;
    @Autowired
    private FileService fileService;
    @Autowired
    private ManageService manageService;

    @Value("${spring.servlet.multipart.maxFileSize}")
    private String maxSize;

    /**
     * 获取配置
     *
     * @return
     */
    @RequestMapping("/config")
    @ResponseBody
    public String config() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileServerUrl", "http:\\\\" + fileServerProperties.getServerUrl() + "/");
        jsonObject.put("maxSize", maxSize);
        return jsonObject.toString();
    }

    @RequestMapping("/upload_list")
    public String upload_list(HttpServletRequest request, String appNo) {
        List<FileInfo> fileList = manageService.getAppFiles(appNo);
        List<JSONObject> jsonObjects = new ArrayList<>();
        if (CollUtil.isNotEmpty(fileList)) {
            for (FileInfo e : fileList) {
                JSONObject jsonObject = JSONUtil.parseObj(e);
                jsonObjects.add(jsonObject);

            }
        }

        request.setAttribute("fileServerUrl", fileServerProperties.getServerUrl());
        request.setAttribute("fileList", jsonObjects);
        return "upload_list";
    }

    @RequestMapping("/to_upload")
    public String to_upload() {
        return "toUpload";
    }
}
