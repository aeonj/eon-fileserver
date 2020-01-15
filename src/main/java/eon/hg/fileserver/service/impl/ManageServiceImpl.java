package eon.hg.fileserver.service.impl;

import eon.hg.fileserver.mapper.TbAppMapper;
import eon.hg.fileserver.mapper.TbFileMapper;
import eon.hg.fileserver.mapper.TbProfessionMapper;
import eon.hg.fileserver.model.TbFile;
import eon.hg.fileserver.service.ManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    private TbAppMapper appMapper;
    @Autowired
    private TbFileMapper fileMapper;
    @Autowired
    private TbProfessionMapper professionMapper;

    @Override
    public List<TbFile> getAppFiles(String app_no) {
        List<TbFile> files = fileMapper.selectByAppNo(app_no);
        return files;
    }


}
