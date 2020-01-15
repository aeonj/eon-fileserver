package eon.hg.fileserver.service;

import eon.hg.fileserver.model.TbFile;

import java.util.List;

public interface ManageService {

    List<TbFile> getAppFiles(String app_no);
}
