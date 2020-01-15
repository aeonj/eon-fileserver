package eon.hg.fileserver.util.dto;

import lombok.Data;

import java.io.InputStream;

@Data
public class DownloadDTO {
    private String originFileName;
    private InputStream inputStream;
}
