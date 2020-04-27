package eon.hg.fileserver.service.impl;

import eon.hg.fileserver.mapper.AppMapper;
import eon.hg.fileserver.mapper.FileMapper;
import eon.hg.fileserver.mapper.ProfessionMapper;
import eon.hg.fileserver.model.FileInfo;
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
    private ProfessionMapper professionMapper;

    @Override
    public List<FileInfo> getAppFiles(String app_no) {
        List<FileInfo> files = fileMapper.selectByAppNo(app_no);
        return files;
    }


}
