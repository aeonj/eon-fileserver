package eon.hg.fileserver.model;

import lombok.Data;

@Data
public class Profession {
    private Long id;

    private String app_no;

    private String app_file_id;

    private Long file_id;

    private String app_ip;

    private String pipe;

    private boolean status;

    private String last_ver;

}