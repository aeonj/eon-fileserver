package eon.hg.fileserver.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.github.tobato.fastdfs.domain.StorageNode;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.exception.FdfsIOException;
import com.github.tobato.fastdfs.exception.FdfsServerException;
import com.github.tobato.fastdfs.exception.FdfsUnavailableException;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.github.tobato.fastdfs.service.TrackerClient;
import eon.hg.fileserver.config.FileServerProperties;
import eon.hg.fileserver.config.FtpProperties;
import eon.hg.fileserver.enums.FileType;
import eon.hg.fileserver.exception.ResultException;
import eon.hg.fileserver.mapper.TbAppMapper;
import eon.hg.fileserver.mapper.TbFileMapper;
import eon.hg.fileserver.mapper.TbProfessionMapper;
import eon.hg.fileserver.model.TbApp;
import eon.hg.fileserver.model.TbFile;
import eon.hg.fileserver.model.TbProfession;
import eon.hg.fileserver.service.FileService;
import eon.hg.fileserver.util.body.ResultBody;
import eon.hg.fileserver.util.body.ResultCode;
import eon.hg.fileserver.util.cache.CachePool;
import eon.hg.fileserver.util.cache.RedisPool;
import eon.hg.fileserver.util.callback.DownloadInputStreamWriter;
import eon.hg.fileserver.util.callback.FileCallback;
import eon.hg.fileserver.util.constant.FileConstant;
import eon.hg.fileserver.util.dto.FileDTO;
import eon.hg.fileserver.util.file.FtpHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件集中处理接口
 *
 * @author eonook
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileServerProperties fileServerProperties;
    @Autowired
    private FastFileStorageClient fastFileClient;
    @Autowired
    private AppendFileStorageClient storageClient;
    @Autowired
    private RedisPool redisPool;
    @Autowired
    private CachePool cachePool;
    @Autowired
    private TbAppMapper appMapper;
    @Autowired
    private TbFileMapper fileMapper;
    @Autowired
    private TbProfessionMapper professionMapper;

    public Map<String, Object> checkNormalFile(String appNo, String appFileId) {
        TbApp app = appMapper.getByAppNo(appNo);
        if (app == null) {
            throw new ResultException(ResultBody.failed("该业务系统编号未登记"));
        }
        List<TbProfession> tbProfessions = professionMapper.selectByMap(new HashMap<String, Object>() {{
            put("app_no", appNo);
            put("app_file_id", appFileId);
        }});
        Map<String, Object> result = new HashMap();
        if (tbProfessions != null && tbProfessions.size() > 0) {
            TbProfession tbProfession = tbProfessions.get(0);
            Long fileId_int = tbProfession.getFile_id();
            if (fileId_int == null) {
                professionMapper.delete(tbProfessions.get(0).getId());
            } else {
                TbFile tbFile = fileMapper.getOne(fileId_int);
                if (tbFile != null) {
                    result.put("flag", 1);
                    result.put("url", tbFile.getUrl());
                    result.put("name", tbFile.getName());
                    result.put("pipe", tbProfession.getPipe());
                    return result;
                } else {
                    professionMapper.delete(tbProfessions.get(0).getId());
                }
            }
        } else {
            result.put("flag", 0);
        }
        return result;
    }

    @Override
    public TbFile uploadNormalFile(FileDTO fileDTO, MultipartFile multipartFile) {
        return uploadNormalFile(fileDTO, multipartFile, (tbFile, ins) -> tbFile);
    }

    @Override
    public <T> T uploadNormalFile(FileDTO fileDTO, MultipartFile multipartFile, FileCallback<T> callback) {
        TbApp app = appMapper.getByAppNo(fileDTO.getAppNo());
        if (app == null) {
            throw new ResultException(ResultBody.failed("该业务系统编号未登记"));
        }
        List<TbProfession> tbProfessions = professionMapper.selectByMap(new HashMap<String, Object>() {{
            put("app_no", fileDTO.getAppNo());
            put("app_file_id", fileDTO.getFileId());
        }});
        if (tbProfessions != null && tbProfessions.size() > 0) {
            throw new ResultException(ResultBody.failed("该业务系统文件ID已存在"));
        }

        String nowDate = DateUtil.now();

        //判断TbFile是否存在
        InputStream inputStream;
        try {
            fileDTO.setFileSize(multipartFile.getSize());
            fileDTO.setFileMd5(SecureUtil.md5().digestHex(multipartFile.getBytes()));
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResultException(ResultBody.failed("传入的文件为空"));
        }
        List<TbFile> tbFiles = fileMapper.selectByMd5(fileDTO.getFileMd5());
        TbFile tbFile;
        if (tbFiles == null || tbFiles.size() == 0) {
            String url = null;
            if (FileType.FASTDFS.equals(app.getFile_type())) {
                try {
                    String suffix = FileUtil.extName(fileDTO.getFileName());
                    if (StrUtil.isBlank(app.getFast_group()) && StrUtil.isBlank(fileDTO.getFastGroup())) {
                        StorePath storePath = fastFileClient.uploadFile(inputStream, fileDTO.getFileSize(), suffix, null);
                        url = storePath.getFullPath();
                    } else {
                        //指定组上传
                        String groupName = fileDTO.getFastGroup();
                        if (StrUtil.isBlank(groupName)) {
                            groupName = app.getFast_group();
                        }
                        StorePath storePath = fastFileClient.uploadFile(groupName, inputStream, fileDTO.getFileSize(), suffix);
                        url = storePath.getFullPath();
                    }
                } catch (FdfsUnavailableException e) {
                    log.error(e.getMessage());
                    throw new ResultException(ResultBody.failed(ResultCode.FDFS_UNAVAILABLE));
                } catch (FdfsIOException e) {
                    log.error(e.getMessage());
                    throw new ResultException(ResultBody.failed(ResultCode.FDFS_IO_ERROR));
                } catch (FdfsServerException e) {
                    log.error(e.getMessage());
                    throw new ResultException(ResultBody.failed(ResultCode.FDFS_SERVER_ERROR).setMsg(e.getMessage()));
                }
            } else if (FileType.LOCAL.equals(app.getFile_type())) {
                //生成上传文件的路径信息，按天生成
                StringBuilder savePath = new StringBuilder();
                savePath.append(FileConstant.DEFAULT_SAVE_KEY)
                        .append(File.separator)
                        .append(fileDTO.getAppNo())
                        .append(File.separator)
                        .append(DateUtil.today());
                StringBuilder saveDirectory = new StringBuilder();
                saveDirectory.append(fileServerProperties.getUploadFolder())
                        .append(savePath);
//                //验证路径是否存在，不存在则创建目录
//                File path = new File(saveDirectory.toString());
//                if (!path.exists()) {
//                    path.mkdirs();
//                }
                String suffix = FileUtil.extName(fileDTO.getFileName());
                StringBuilder saveName = new StringBuilder();
                saveName.append(fileDTO.getFileId()).append(".").append(suffix);

                File saveFile = new File(saveDirectory.toString(), saveName.toString());
                //验证路径是否存在，不存在则创建目录
                saveFile = FileUtil.touch(saveFile);
                FileUtil.writeFromStream(inputStream, saveFile);
                url = savePath.append(File.separator).append(saveName).toString();
                fileDTO.setFileSize(multipartFile.getSize());
                fileDTO.setFileMd5(SecureUtil.md5(saveFile));
            } else if (FileType.FTP.equals(app.getFile_type())) {
                FtpProperties ftpProp = fileServerProperties.getFtp();
                if (ftpProp != null) {
                    FtpHandler ftpUtils = new FtpHandler(ftpProp.getUsername(), ftpProp.getPassword(), ftpProp.getHost(), ftpProp.getPort(), ftpProp.getBaseDir());
                    StringBuilder ftpPath = new StringBuilder();
                    ftpPath.append(FileConstant.DEFAULT_SAVE_KEY)
                            .append(File.separator)
                            .append(fileDTO.getAppNo())
                            .append(File.separator)
                            .append(DateUtil.today());
                    String suffix = FileUtil.extName(fileDTO.getFileName());
                    StringBuilder saveName = new StringBuilder();
                    saveName.append(fileDTO.getFileId()).append(".").append(suffix);
                    try {
                        ftpUtils.uploadFile(ftpPath.toString(), saveName.toString(), inputStream);
                        url = ftpPath.toString()+File.separator+saveName.toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new ResultException(ResultBody.failed("FTP上传异常"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new ResultException(ResultBody.failed(e.getMessage()));
                    }
                } else {
                    throw new ResultException(ResultBody.failed("FTP参数未定义"));
                }
            } else {
                throw new ResultException(ResultBody.failed("未被支持的文件服务类型"));
            }
            //持久化存储
            tbFile = new TbFile();
            tbFile.setName(fileDTO.getFileName());
            tbFile.setPath(null);
            tbFile.setSize(fileDTO.getFileSize());
            tbFile.setMd5(fileDTO.getFileMd5());
            tbFile.setUrl(url);
            tbFile.setLast_ver(nowDate);
            tbFile.setType(app.getFile_type());
            tbFile.setStatus(true);
            tbFile.setBlock(false);
            fileMapper.insert(tbFile);
        } else {
            tbFile = tbFiles.get(0);
        }

        //初始化业务对象
        TbProfession tbProfession = new TbProfession();
        tbProfession.setApp_no(fileDTO.getAppNo());
        tbProfession.setApp_file_id(fileDTO.getFileId());
        tbProfession.setFile_id(tbFile.getId());
        tbProfession.setLast_ver(nowDate);
        tbProfession.setApp_ip(null);
        tbProfession.setPipe(fileDTO.getPipe());
        tbProfession.setStatus(true);

        professionMapper.insert(tbProfession);
        return callback.recv(tbFile, null);
    }

    public Map<String, Object> checkChunkFile(String appNo, String appFileId) {
        Map<String, Object> result = checkNormalFile(appNo, appFileId);
        if (result != null && ObjectUtil.equal(result.get("flag"), Integer.valueOf(1))) {
            return result;
        }
        //返回正在上传的文件块索引
        result = new HashMap<>();
        result.put("flag", 0);
        TbApp app = appMapper.getByAppNo(appNo);
        if (FileType.FASTDFS.equals(app.getFile_type())) {
            String fileGUID = appNo + "_" + appFileId;
            try {
                //查询锁占用
                String lockName = FileConstant.currLocks + fileGUID;
                Long lock = redisPool.incr(lockName, 1l);
                String lockOwner = FileConstant.lockOwner + fileGUID;
                String chunkCurrkey = FileConstant.chunkCurr + fileGUID;
                String sizeCurrKey = FileConstant.sizeCurr + fileGUID;
                if (lock > 1) {
                    result.put("chunk", 2);
                    //检查是否为锁的拥有者,如果是放行
                    String oWner = redisPool.get(lockOwner);
                    if (StrUtil.isEmpty(oWner)) {
                        new ResultException(ResultBody.failed("无法获取文件锁拥有者"));
                    } else {
                        if (oWner.equals(appNo)) {
                            Integer chunkCurr = redisPool.get(chunkCurrkey);
                            if (ObjectUtil.isNull(chunkCurr)) {
                                new ResultException(ResultBody.failed("无法获取当前文件chunkCurr"));
                            }

                            result.put("chunk", chunkCurr);
                            return result;
                        } else {
                            new ResultException(ResultBody.failed("当前文件已有人在上传,您暂无法上传该文件"));
                        }
                    }
                } else {
                    //初始化锁.分块
                    redisPool.set(lockOwner, appNo);
                    //第一块索引是0,与前端保持一致
                    redisPool.set(chunkCurrkey, Integer.valueOf(1));
                    redisPool.set(sizeCurrKey, Long.valueOf(0));
                    result.put("chunk", 1);
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
                new ResultException(ResultBody.failed("检查文件出错"));
            }
        } else if (FileType.LOCAL.equals(app.getFile_type())) {
            result.put("chunk", 1);
            return result;
        }
        return null;
    }

    public <T> T uploadChunkFile(FileDTO fileDTO, MultipartFile multipartFile, FileCallback<T> callback) {
        if (fileDTO.getChunk() == null)
            throw new ResultException(ResultBody.failed("参数【chunk】不能为空"));
        if (fileDTO.getChunks() == null)
            throw new ResultException(ResultBody.failed("参数【chunks】不能为空"));
        if (fileDTO.getChunks() < 1)
            throw new ResultException(ResultBody.failed("参数【chunk】不能小于1"));
        if (fileDTO.getChunks() < 1)
            throw new ResultException(ResultBody.failed("参数【chunks】不能小于1"));

        TbApp app = appMapper.getByAppNo(fileDTO.getAppNo());
        if (app == null) {
            throw new ResultException(ResultBody.failed("该业务系统编号未登记"));
        }
        List<TbProfession> tbProfessions = professionMapper.selectByMap(new HashMap<String, Object>() {{
            put("app_no", fileDTO.getAppNo());
            put("app_file_id", fileDTO.getFileId());
        }});
        if (tbProfessions != null && tbProfessions.size() > 0) {
            TbProfession profession = tbProfessions.get(0);
            if (profession.isStatus())
                throw new ResultException(ResultBody.failed("该业务系统文件已上传结束"));
        }
        if (FileType.FASTDFS.equals(app.getFile_type())) {
            if (StrUtil.isBlank(fileDTO.getFastGroup())) {
                fileDTO.setFastGroup(app.getFast_group());
            }
            return uploadChunkFile_FastDFS(fileDTO, multipartFile, callback);
        } else if (FileType.LOCAL.equals(app.getFile_type())) {
            return uploadChunkFile_Local(fileDTO, multipartFile, callback);
        }
        return null;
    }

    /**
     * 断点续传到本地路径
     *
     * @param fileDTO       用户请求接口参数
     *                      appNo: 业务系统编号
     *                      fileId: 文件ID
     *                      fileName: 文件名
     *                      chunk: 当前分片
     *                      chunks: 总分片数
     *                      不必需- fileSize: 文件大小
     *                      fileMd5: 文件Md5
     * @param multipartFile
     * @param callback
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T uploadChunkFile_Local(FileDTO fileDTO, MultipartFile multipartFile, FileCallback<T> callback) {

        //生成上传文件的路径信息，按天生成
        StringBuilder savePath = new StringBuilder();
        savePath.append(FileConstant.DEFAULT_SAVE_KEY)
                .append(File.separator)
                .append(fileDTO.getAppNo())
                .append(File.separator)
                .append(DateUtil.today());
        StringBuilder saveDirectory = new StringBuilder();
        saveDirectory.append(fileServerProperties.getUploadFolder())
                .append(savePath)
                .append(File.separator)
                .append(fileDTO.getFileId());
        //验证路径是否存在，不存在则创建目录
        File path = new File(saveDirectory.toString());
        if (!path.exists()) {
            path.mkdirs();
        }
        //文件分片位置
        File file = new File(saveDirectory.toString(), fileDTO.getFileId() + "_" + fileDTO.getChunk());

        //分片上传过程中出错,有残余时需删除分块后,重新上传
        if (file.exists()) {
            file.delete();
        }
        try {
            multipartFile.transferTo(new File(saveDirectory.toString(), fileDTO.getFileId() + "_" + fileDTO.getChunk()));

            if (path.isDirectory()) {
                File[] fileArray = path.listFiles();
                if (fileArray != null) {
                    String suffix = FileUtil.extName(fileDTO.getFileName());
                    if (fileArray.length == fileDTO.getChunks()) {
                        //分块全部上传完毕,合并

                        File newFile = new File(fileServerProperties.getUploadFolder() + savePath, fileDTO.getFileId() + "." + suffix);
                        FileOutputStream outputStream = new FileOutputStream(newFile, true);//文件追加写入
                        byte[] byt = new byte[8192];
                        int len;

                        FileInputStream temp = null;//分片文件
                        for (int i = 0; i < fileDTO.getChunks(); i++) {
                            int j = i + 1;
                            temp = new FileInputStream(new File(saveDirectory.toString(), fileDTO.getFileId() + "_" + j));
                            while ((len = temp.read(byt)) != -1) {
                                System.out.println("-----" + len);
                                outputStream.write(byt, 0, len);
                            }
                        }
                        //关闭流
                        temp.close();
                        outputStream.close();

                        String nowDate = DateUtil.now();
                        TbFile tbFile = new TbFile();
                        tbFile.setName(fileDTO.getFileName());
                        tbFile.setPath(null);
                        tbFile.setSize(FileUtil.size(newFile));
                        tbFile.setMd5(SecureUtil.md5(newFile));
                        tbFile.setUrl(savePath + File.separator + fileDTO.getFileId() + "." + suffix);
                        tbFile.setLast_ver(nowDate);
                        tbFile.setType(FileType.LOCAL);
                        tbFile.setStatus(true);
                        tbFile.setBlock(true);
                        fileMapper.insert(tbFile);

                        //初始化业务对象
                        TbProfession tbProfession = new TbProfession();
                        tbProfession.setApp_no(fileDTO.getAppNo());
                        tbProfession.setApp_file_id(fileDTO.getFileId());
                        tbProfession.setFile_id(tbFile.getId());
                        tbProfession.setLast_ver(nowDate);
                        tbProfession.setApp_ip(null);
                        tbProfession.setPipe(fileDTO.getPipe());
                        tbProfession.setStatus(true);

                        professionMapper.insert(tbProfession);

                        //清楚分片文件及目录
                        FileUtil.del(saveDirectory.toString());

                        return callback.recv(tbFile, null);
                    }
                }
            }
        } catch (IOException e) {
            throw new ResultException(ResultBody.failed(ResultCode.FILE_IO_ERROR));
        }
        return callback.recv(null, null);
    }

    public <T> T uploadChunkFile_FastDFS(FileDTO fileDTO, MultipartFile multipartFile, FileCallback<T> callback) {
        String fileGUID = fileDTO.getAppNo() + "_" + fileDTO.getFileId();
        String chunklockName = FileConstant.chunkLock + fileGUID;

        boolean currOwner = false;//真正的拥有者
        try {

            Long lock = redisPool.incr(chunklockName, 1l);
            if (lock > 1) {
                throw new ResultException(ResultBody.failed("请求块锁失败"));
            }

            //写入锁的当前拥有者
            currOwner = true;

            String chunk = Convert.toStr(fileDTO.getChunk());
            String chunkCurrkey = FileConstant.chunkCurr + fileGUID; //redis中记录当前应该穿第几块(从0开始)
            Integer chunkCurr = redisPool.get(chunkCurrkey);
            if (ObjectUtil.isNull(chunkCurr)) {
                if (fileDTO.getChunk() == 1) {
                    chunkCurr = Integer.valueOf(1);
                    redisPool.set(chunkCurrkey, chunkCurr);
                } else {
                    throw new ResultException(ResultBody.failed("无法获取当前文件chunkCurr"));
                }
            }
            if (fileDTO.getChunk() < chunkCurr) {
                throw new ResultException(ResultBody.failed("当前文件块已上传"));
            } else if (fileDTO.getChunk() > chunkCurr) {
                throw new ResultException(ResultBody.failed("当前文件块需要等待上传,稍后请重试"));
            }

            String sizeCurrKey = FileConstant.sizeCurr + fileGUID; //redis中记录当前已经传了多少内容
            Long sizeCurr = redisPool.get(sizeCurrKey);
            if (ObjectUtil.isNull(sizeCurr)) {
                if (fileDTO.getChunk() == 1) {
                    sizeCurr = Long.valueOf(0);
                    redisPool.set(sizeCurrKey, sizeCurr);
                } else {
                    throw new ResultException(ResultBody.failed("无法获取当前文件sizeCurr"));
                }
            }


            //  System.out.println("***********开始**********");
            //暂时不支持多文件上传,后续版本可以再加上
            if (!multipartFile.isEmpty()) {
                try {
                    long fileSize = multipartFile.getSize();
                    if (fileDTO.getChunk() == 1) {
                        redisPool.set(chunkCurrkey, chunkCurr + 1);
                        log.debug(chunk + ":redis块+1");
                        try {
                            StorePath storePath = storageClient.uploadAppenderFile(fileDTO.getFastGroup(), multipartFile.getInputStream(),
                                    fileSize, FileUtil.extName(fileDTO.getFileName()));
                            log.debug(chunk + ":更新完fastdfs");
                            if (storePath == null) {
                                redisPool.set(chunkCurrkey, chunkCurr);
                                throw new ResultException(ResultBody.failed("获取远程文件路径出错"));
                            }

                            redisPool.set(FileConstant.fastDfsPath + fileGUID, storePath.getFullPath());
                            log.debug("上传文件 result={}", storePath);
                        } catch (Exception e) {
                            redisPool.set(chunkCurrkey, chunkCurr);
                            // e.printStackTrace();
                            //还原历史块
                            log.error("初次上传远程文件出错", e);
                            throw new ResultException(ResultBody.failed("上传远程服务器文件出错"));

                        }
                    } else {
                        redisPool.set(chunkCurrkey, chunkCurr + 1);
                        log.debug(chunk + ":redis块+1");
                        String fullPathCurr = redisPool.get(FileConstant.fastDfsPath + fileGUID);
                        if (fullPathCurr == null) {
                            throw new ResultException(ResultBody.failed("无法获取上传远程服务器文件path路径"));
                        }

                        try {
                            StorePath storePath = StorePath.praseFromUrl(fullPathCurr);
                            String group = storePath.getGroup();
                            String path = storePath.getPath();
                            //追加方式实际实用如果中途出错多次,可能会出现重复追加情况,这里改成修改模式,即时多次传来重复文件块,依然可以保证文件拼接正确
                            storageClient.modifyFile(group, path, multipartFile.getInputStream(),
                                    fileSize, sizeCurr);
                            log.debug(chunk + ":更新完fastdfs");
                        } catch (Exception e) {
                            redisPool.set(chunkCurrkey, chunkCurr);
                            log.error("更新远程文件出错", e);
                            throw new ResultException(ResultBody.failed("更新远程文件出错"));
                        }


                    }
                    Long uploadSize = multipartFile.getSize() + sizeCurr;
                    redisPool.set(sizeCurrKey, uploadSize);

                    //最后一块,清空upload,写入数据库

                    Integer chunks_int = fileDTO.getChunks();
                    if (fileDTO.getChunk() == chunks_int) {
                        String nowDate = DateUtil.now();
                        //持久化存储
                        TbFile tbFile = new TbFile();
                        tbFile.setName(fileDTO.getFileName());
                        tbFile.setPath(null);
                        if (fileDTO.getFileSize() == null || fileDTO.getFileSize() <= 0) {
                            fileDTO.setFileSize(uploadSize);
                        }
                        tbFile.setSize(fileDTO.getFileSize());
                        if (StrUtil.isBlank(fileDTO.getFileMd5())) {
                            fileDTO.setFileMd5("33");
                        }
                        tbFile.setMd5(fileDTO.getFileMd5());
                        tbFile.setUrl(redisPool.get(FileConstant.fastDfsPath + fileGUID));
                        tbFile.setLast_ver(nowDate);
                        tbFile.setType(FileType.FASTDFS);
                        tbFile.setStatus(true);
                        tbFile.setBlock(true);
                        fileMapper.insert(tbFile);

                        //初始化业务对象
                        TbProfession tbProfession = new TbProfession();
                        tbProfession.setApp_no(fileDTO.getAppNo());
                        tbProfession.setApp_file_id(fileDTO.getFileId());
                        tbProfession.setFile_id(tbFile.getId());
                        tbProfession.setLast_ver(nowDate);
                        tbProfession.setApp_ip(null);
                        tbProfession.setPipe(fileDTO.getPipe());
                        tbProfession.setStatus(true);

                        professionMapper.insert(tbProfession);


                        redisPool.del(new String[]{FileConstant.chunkCurr + fileGUID,
                                FileConstant.sizeCurr + fileGUID,
                                FileConstant.fastDfsPath + fileGUID,
                                FileConstant.lockOwner + fileGUID
                        });
                        redisPool.delIncr(FileConstant.currLocks + fileGUID,
                                FileConstant.chunkLock + fileGUID);
                        return callback.recv(tbFile, null);
                    }


                } catch (Exception e) {
                    log.error("上传文件错误", e);
                    //e.printStackTrace();
                    throw new ResultException(ResultBody.failed("上传错误 " + e.getMessage()));
                }
            }

        } finally {
            //锁的当前拥有者才能释放块上传锁
            if (currOwner) {
                redisPool.freeIncr(chunklockName);
            }

        }
        return callback.recv(null, null);
    }

    @Override
    public TbFile getFile(String appNo, String appFileId) {
        if (StrUtil.isBlank(appNo))
            throw new ResultException(ResultBody.failed("参数【appNo】不可为空"));
        if (StrUtil.isBlank(appFileId))
            throw new ResultException(ResultBody.failed("参数【fileId】不可为空"));
        TbApp app = appMapper.getByAppNo(appNo);
        if (app == null) {
            throw new ResultException(ResultBody.failed("该业务系统编号未登记"));
        }
        List<TbProfession> professions = professionMapper.selectByMap(new HashMap<String, Object>() {{
            put("app_no", appNo);
            put("app_file_id", appFileId);
        }});
        if (professions != null && professions.size() > 0) {
            Long tbFileId = professions.get(0).getFile_id();
            if (tbFileId == null) {
                throw new ResultException(ResultBody.failed("对应文件已被删除"));
            } else {
                TbFile tbFile = fileMapper.getOne(tbFileId);
                if (tbFile == null) {
                    throw new ResultException(ResultBody.failed("对应文件ID记录空值"));
                } else {
                    return tbFile;
                }
            }
        } else {
            throw new ResultException(ResultBody.failed("未找到该文件ID记录"));
        }
    }

    @Override
    public TbFile downloadNormalFile(String appNo, String appFileId) {
        return downloadNormalFile(appNo, appFileId, (tbFile, ins) -> tbFile);
    }

    @Override
    public <T> T downloadNormalFile(String appNo, String fileId, FileCallback<T> callback) {
        TbFile tbFile = getFile(appNo, fileId);
        if (FileType.FASTDFS.equals(tbFile.getType())) {

            //return fileServerProperties.getServerUrl()+tbFile.getUrl();
            return callback.recv(tbFile, downloadFastFile(tbFile.getUrl()));
        } else if (FileType.LOCAL.equals(tbFile.getType())) {
            StringBuilder saveFileName = new StringBuilder();
            saveFileName.append(fileServerProperties.getUploadFolder())
                    .append(tbFile.getUrl());
            File file = FileUtil.file(saveFileName.toString());
            try {
                InputStream inputStream = new FileInputStream(file);
                return callback.recv(tbFile, inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new ResultException(ResultBody.failed("未找到该文件"));
            }
        } else if (FileType.FTP.equals(tbFile.getType())) {
            return callback.recv(tbFile, downloadFtpFile(tbFile.getUrl()));
        } else {
            throw new ResultException(ResultBody.failed("未被支持的文件服务类型"));
        }
    }

    public InputStream downloadLocalFile(String url) {
        File file = FileUtil.file(url);
        try {
            InputStream inputStream = new FileInputStream(file);
            return inputStream;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ResultException(ResultBody.failed("未找到该文件"));
        }
    }

    public InputStream downloadFtpFile(String url) {
        FtpProperties ftpProp = fileServerProperties.getFtp();
        if (ftpProp != null) {
            try {
                FtpHandler ftpUtils = new FtpHandler(ftpProp.getUsername(), ftpProp.getPassword(), ftpProp.getHost(), ftpProp.getPort(), ftpProp.getBaseDir());
                String path = FileUtil.getAbsolutePath(url);
                String name = FileUtil.getName(url);
                return ftpUtils.getFileInputStream(path, name);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ResultException(ResultBody.failed("FTP上传异常"));
            } catch (Exception e) {
                e.printStackTrace();
                throw new ResultException(ResultBody.failed(e.getMessage()));
            }
        } else {
            throw new ResultException(ResultBody.failed("FTP参数未定义"));
        }
    }

    public InputStream downloadFastFile(String fileUrl) {
        StorePath storePath = StorePath.praseFromUrl(fileUrl);
        String group = storePath.getGroup();
        String path = storePath.getPath();
        DownloadInputStreamWriter downloadByteArray = new DownloadInputStreamWriter();
        return fastFileClient.downloadFile(group, path, downloadByteArray);
    }

    public InputStream downloadFastChunkFile(String fileUrl, long fileOffset, long fileSize) {
        StorePath storePath = StorePath.praseFromUrl(fileUrl);
        String group = storePath.getGroup();
        String path = storePath.getPath();
        DownloadInputStreamWriter downloadByteArray = new DownloadInputStreamWriter();
        return fastFileClient.downloadFile(group, path, fileOffset, fileSize, downloadByteArray);
    }

    @Override
    public String getFileFullUrl(String appNo, String path) {
        TbApp app = appMapper.getByAppNo(appNo);
        if (app == null) {
            throw new ResultException(ResultBody.failed("该业务系统编号未登记"));
        }
        if (StrUtil.isBlank(path)) {
            throw new ResultException(ResultBody.failed("参数【url】不可为空"));
        }
        if (FileType.FASTDFS.equals(app.getFile_type())) {
            return fileServerProperties.getServerUrl() + path;
        } else if (FileType.LOCAL.equals(app.getFile_type())) {
            return "/file/local/download?url=" + fileServerProperties.getUploadFolder() + path;
        } else {
            throw new ResultException(ResultBody.failed("未被支持的文件服务类型"));
        }
    }

    @Override
    public void deleteFile(String appNo, String appFileId, String fileMd5) {
        if (StrUtil.isBlank(appNo))
            throw new ResultException(ResultBody.failed("参数【appNo】不可为空"));
        if (StrUtil.isBlank(appFileId))
            throw new ResultException(ResultBody.failed("参数【fileId】不可为空"));
        if (StrUtil.isBlank(fileMd5))
            throw new ResultException(ResultBody.failed("参数【fileMd5】不可为空"));
        List<TbProfession> tbProfessions = professionMapper.selectByMap(new HashMap<String, Object>() {{
            put("app_no", appNo);
            put("app_file_id", appFileId);
        }});
        if (tbProfessions.size() == 0) {
            throw new ResultException(ResultBody.failed("没有找到该文件记录"));
        }
        if (tbProfessions.size() > 1) {
            throw new ResultException(ResultBody.failed("传入的系统编号和系统文件id有误"));
        }
        //找到该文件记录
        TbProfession tbProfession = tbProfessions.get(0);
        //得到文件id
        Long fileId = tbProfession.getFile_id();
        //删除文件
        if (fileId == null) {
            throw new ResultException(ResultBody.failed("此业务记录中未找到文件信息"));
        }

        // 业务表中只有一条记录
        TbFile tbFile = fileMapper.getOne(fileId);
        if (tbFile == null) {
            throw new ResultException(ResultBody.failed("服务器中不存在该文件"));
        } else {
            if (!fileMd5.equals(tbFile.getMd5())) {
                throw new ResultException(ResultBody.failed("服务器中不存在该文件"));
            }
        }
        fileMapper.delete(fileId);

        //将该业务记录fileId置为空
        tbProfession.setFile_id(null);
        professionMapper.delete(tbProfession.getId());
        if (FileType.FASTDFS.equals(tbFile.getType())) {
            StorePath storePath = StorePath.praseFromUrl(tbFile.getUrl());
            String group = storePath.getGroup();
            String path = storePath.getPath();
            if (tbFile.isBlock()) {
                storageClient.truncateFile(group, path);
            } else {
                fastFileClient.deleteFile(group, path);
            }
        } else {
            StringBuilder saveFileName = new StringBuilder();
            saveFileName.append(fileServerProperties.getUploadFolder())
                    .append(tbFile.getUrl());
            if (tbFile.isBlock()) {

            }
            FileUtil.del(saveFileName.toString());
        }
    }

}
