package eon.hg.fileserver.model;

import eon.hg.fileserver.enums.FileType;
import lombok.Data;

@Data
public class TbFile {
    private Long id;

    private String name;

    private String path;

    private String url;

    private Long size;

    private String md5;

    private FileType type;

    private String last_ver;

    private boolean status;

    private boolean block;

    private String record;

}