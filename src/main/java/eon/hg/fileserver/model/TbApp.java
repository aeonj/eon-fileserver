package eon.hg.fileserver.model;

import eon.hg.fileserver.enums.FileType;
import lombok.Data;

@Data
public class TbApp {
    private Long id;

    private String app_no;

    private String app_desc;

    private FileType file_type;

}