package eon.hg.fileserver.controller;

import cn.hutool.core.bean.BeanUtil;
import eon.hg.fileserver.config.FileServerProperties;
import eon.hg.fileserver.enums.FileType;
import eon.hg.fileserver.exception.ResultException;
import eon.hg.fileserver.model.TbFile;
import eon.hg.fileserver.service.FileService;
import eon.hg.fileserver.util.body.ResultBody;
import eon.hg.fileserver.util.dto.FileDTO;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件API接口Controller
 * @author eonook
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private FileServerProperties fileServerProperties;

    @GetMapping("test")
    public ResultBody test() {
        return ResultBody.failed();
    }

    @RequestMapping("/normal/check")
    public ResultBody checkNormalFile(String appNo, String fileId) {
        return ResultBody.success().addObject(fileService.checkNormalFile(appNo,fileId));
    }

    @RequestMapping("/chunk/check")
    public ResultBody checkChunkFile(String appNo, String fileId) {
        return ResultBody.success().addObject(fileService.checkChunkFile(appNo,fileId));
    }

    @PostMapping("/normal/upload")
    public ResultBody uploadNormalFile(@RequestParam(value = "file", required = false) MultipartFile file,
                                       @RequestParam Map<String, Object> paramMap) {
        FileDTO fileDTO = BeanUtil.mapToBean(paramMap, FileDTO.class, true);
        fileDTO.valid();
        TbFile tbFile = fileService.uploadNormalFile(fileDTO, file);
        return ResultBody.success().addObject(tbFile.getUrl());
    }

    @PostMapping("/chunk/upload")
    public ResultBody uploadChunkFile(@RequestParam(value = "file", required = false) MultipartFile file,
                                      @RequestParam Map<String, Object> paramMap) {
        FileDTO fileDTO = BeanUtil.mapToBean(paramMap, FileDTO.class, true);
        fileDTO.valid();
        return fileService.uploadChunkFile(fileDTO, file, (tbFile, ins) -> {
            if (tbFile==null) {
                return ResultBody.success();
            } else {
                Map map = new HashMap();
                map.put("url", tbFile.getUrl());
                return ResultBody.success().addObject(tbFile);
            }
        });
    }

    @RequestMapping("/normal/download/{appNo}/{fileId}")
    @ResponseBody
    public ResultBody downloadNormalFileByPath(HttpServletResponse response,
                                        @PathVariable("appNo") String appNo,
                                               @PathVariable("fileId") String fileId){
        return downloadNormalFile(response,appNo,fileId);
    }

    @RequestMapping("/normal/download")
    @ResponseBody
    public ResultBody downloadNormalFile(HttpServletResponse response, String appNo, String fileId) {
        return fileService.downloadNormalFile(appNo, fileId, (tbFile, ins) -> {
            try {
                // 配置文件下载
                response.setHeader("content-type", "application/octet-stream");
                response.setContentType("application/octet-stream");
                // 下载文件能正常显示中文
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(tbFile.getName(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new ResultException(ResultBody.failed("获取文件名错误"));
            }
            // 实现文件下载
            toOutStream(response, ins);
            return null;
        });
    }

    @RequestMapping("/path/download/{appNo}/{url}")
    @ResponseBody
    public void downloadPathFileByPath(HttpServletResponse response,
                                       @PathVariable("appNo") String appNo,
                                       @PathVariable("url") String url){
        downloadPathFile(response,appNo,url);
    }

    @RequestMapping("/path/download")
    @ResponseBody
    public void downloadPathFile(HttpServletResponse response, String appNo, String url) {
        String serverUrl = fileService.getFileFullUrl(appNo,url);
        try {
            response.sendRedirect(serverUrl);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResultException(ResultBody.failed("IO异常"));
        }
    }

    @RequestMapping("/local/download/{url}")
    @ResponseBody
    public void downloadLocalFile(HttpServletResponse response, @PathVariable("url") String url) {
        InputStream ins = fileService.downloadLocalFile(url);
        // 配置文件下载
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        // 下载文件能正常显示中文
        response.setHeader("Content-Disposition", "attachment");
        // 实现文件下载
        toOutStream(response,ins);
    }

    @RequestMapping("/chunk/download/{appNo}/{fileId}")
    @ResponseBody
    public void downloadChunkFileByPath(HttpServletResponse response, HttpServletRequest request,
                                        @PathVariable("appNo") String appNo,
                                        @PathVariable("fileId") String fileId){
        downloadChunkFile(response,request,appNo,fileId);
    }

    @RequestMapping("/chunk/download")
    @ResponseBody
    public void downloadChunkFile(HttpServletResponse response, HttpServletRequest request, String appNo, String fileId){
        TbFile tbFile = fileService.getFile(appNo,fileId);
        BufferedInputStream bis = null;
        try {
            if (tbFile.getType().equals(FileType.LOCAL)) {
                StringBuilder saveFileName = new StringBuilder();
                saveFileName.append(fileServerProperties.getUploadFolder())
                        .append(tbFile.getUrl());
                File file = new File(saveFileName.toString());
                if (file.exists()) {
                    long p = 0L;
                    long toLength = 0L;
                    long contentLength = 0L;
                    // 0,从头开始的全文下载；1,从某字节开始的下载（bytes=27000-）；2,从某字节开始到某字节结束的下载（bytes=27000-39000）
                    int rangeSwitch = 0;
                    long fileLength;
                    String rangBytes = "";
                    fileLength = tbFile.getSize();

                    // get file content
                    InputStream ins = new FileInputStream(file);
                    bis = new BufferedInputStream(ins);

                    // tell the client to allow accept-ranges
                    response.reset();
                    response.setHeader("Accept-Ranges", "bytes");

                    // client requests a file block download start byte
                    String range = request.getHeader("Range");
                    if (range != null && range.trim().length() > 0 && !"null".equals(range)) {
                        response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
                        rangBytes = range.replaceAll("bytes=", "");
                        if (rangBytes.endsWith("-")) {  // bytes=270000-
                            rangeSwitch = 1;
                            p = Long.parseLong(rangBytes.substring(0, rangBytes.indexOf("-")));
                            contentLength = fileLength - p;  // 客户端请求的是270000之后的字节（包括bytes下标索引为270000的字节）
                        } else { // bytes=270000-320000
                            rangeSwitch = 2;
                            String temp1 = rangBytes.substring(0, rangBytes.indexOf("-"));
                            String temp2 = rangBytes.substring(rangBytes.indexOf("-") + 1, rangBytes.length());
                            p = Long.parseLong(temp1);
                            toLength = Long.parseLong(temp2);
                            contentLength = toLength - p + 1; // 客户端请求的是 270000-320000 之间的字节
                        }
                    } else {
                        contentLength = fileLength;
                    }

                    // 如果设设置了Content-Length，则客户端会自动进行多线程下载。如果不希望支持多线程，则不要设置这个参数。
                    // Content-Length: [文件的总大小] - [客户端请求的下载的文件块的开始字节]
                    response.setHeader("Content-Length", new Long(contentLength).toString());

                    // 断点开始
                    // 响应的格式是:
                    // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
                    if (rangeSwitch == 1) {
                        String contentRange = new StringBuffer("bytes ").append(new Long(p).toString()).append("-")
                                .append(new Long(fileLength - 1).toString()).append("/")
                                .append(new Long(fileLength).toString()).toString();
                        response.setHeader("Content-Range", contentRange);
                        bis.skip(p);
                    } else if (rangeSwitch == 2) {
                        String contentRange = range.replace("=", " ") + "/" + new Long(fileLength).toString();
                        response.setHeader("Content-Range", contentRange);
                        bis.skip(p);
                    } else {
                        String contentRange = new StringBuffer("bytes ").append("0-")
                                .append(fileLength - 1).append("/")
                                .append(fileLength).toString();
                        response.setHeader("Content-Range", contentRange);
                    }

                    response.setContentType("application/octet-stream");
                    response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(tbFile.getName(),"UTF-8"));

                    OutputStream out = response.getOutputStream();
                    int n = 0;
                    long readLength = 0;
                    int bsize = 4096;
                    byte[] bytes = new byte[bsize];
                    if (rangeSwitch == 2) {
                        // 针对 bytes=27000-39000 的请求，从27000开始写数据
                        while (readLength <= contentLength - bsize) {
                            n = bis.read(bytes);
                            readLength += n;
                            out.write(bytes, 0, n);
                        }
                        if (readLength <= contentLength) {
                            n = bis.read(bytes, 0, (int) (contentLength - readLength));
                            out.write(bytes, 0, n);
                        }
                    } else {
                        while ((n = bis.read(bytes)) != -1) {
                            out.write(bytes, 0, n);
                        }
                    }
                    out.flush();
                    out.close();
                    bis.close();
                } else {
                    throw new ResultException(ResultBody.failed("文件不存在"));
                }
            } else if (tbFile.getType().equals(FileType.FASTDFS)) {
                long p = 0L;
                long toLength = 0L;
                long contentLength = 0L;
                long fileLength = tbFile.getSize();
                String rangBytes = "";
                response.reset();
                response.setHeader("Accept-Ranges", "bytes");

                // client requests a file block download start byte
                // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
                String range = request.getHeader("Range");
                if (range != null && range.trim().length() > 0 && !"null".equals(range)) {
                    response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
                    rangBytes = range.replaceAll("bytes=", "");
                    if (rangBytes.endsWith("-")) {
                        //从某字节开始的下载
                        p = Long.parseLong(rangBytes.substring(0, rangBytes.indexOf("-")));
                        contentLength = fileLength - p;  // 客户端请求的是270000之后的字节（包括bytes下标索引为270000的字节）
                        String contentRange = new StringBuffer("bytes ").append(new Long(p).toString()).append("-")
                                .append(new Long(fileLength - 1).toString()).append("/")
                                .append(new Long(fileLength).toString()).toString();
                        response.setHeader("Content-Range", contentRange);
                    } else {
                        //从某字节开始到某字节结束的下载
                        String temp1 = rangBytes.substring(0, rangBytes.indexOf("-"));
                        String temp2 = rangBytes.substring(rangBytes.indexOf("-") + 1, rangBytes.length());
                        p = Long.parseLong(temp1);
                        toLength = Long.parseLong(temp2);
                        contentLength = toLength - p + 1; // 客户端请求的是 270000-320000 之间的字节
                        String contentRange = range.replace("=", " ") + "/" + new Long(fileLength).toString();
                        response.setHeader("Content-Range", contentRange);
                    }
                } else {
                    //从头开始的全文下载
                    contentLength = fileLength;
                    String contentRange = new StringBuffer("bytes ").append("0-")
                            .append(fileLength - 1).append("/")
                            .append(fileLength).toString();
                    response.setHeader("Content-Range", contentRange);
                }

                response.setContentType("application/octet-stream");
                response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(tbFile.getName(),"UTF-8"));

                InputStream ins = fileService.downloadFastChunkFile(tbFile.getUrl(),p,contentLength);
                toOutStream(response,ins);
            } else {
                downloadPathFile(response,appNo,tbFile.getUrl());
            }
        } catch (IOException ie) {
            throw new ResultException(ResultBody.failed("IO异常"));
        }
    }

    private void toOutStream(HttpServletResponse response, InputStream ins) {
        // 实现文件下载
        InputStream in = new BufferedInputStream(ins);
        OutputStream out = null;
        try {
            // 通过ioutil 对接输入输出流，实现文件下载
            out = response.getOutputStream();
            IOUtils.copy(in, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResultException(ResultBody.failed("IO异常"));
        } finally {
            // 关闭流
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @RequestMapping("/url")
    public String getUrl(@RequestParam("appNo") String appNo, @RequestParam("fileId") String fileId) {
        TbFile tbFile = fileService.getFile(appNo, fileId);
        return tbFile.getUrl();
    }

    @PostMapping("/delete")
    public ResultBody deleteNormalFile(String appNo, String fileId, String fileMd5) {
        fileService.deleteFile(appNo,fileId,fileMd5);
        return ResultBody.success();
    }

}
