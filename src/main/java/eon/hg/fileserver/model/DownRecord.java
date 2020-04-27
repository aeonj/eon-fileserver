package eon.hg.fileserver.model;

import lombok.Data;

@Data
public class DownRecord {
    private Long id;
    private Long fileId;
    private Long accessCount;//被访问次数
    private String srcIp;//所属机器的ip地址
}
