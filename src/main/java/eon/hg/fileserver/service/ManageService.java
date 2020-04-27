package eon.hg.fileserver.service;

import eon.hg.fileserver.model.FileInfo;

import java.util.List;

public interface ManageService {

    List<FileInfo> getAppFiles(String app_no);
}
