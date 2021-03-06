package eon.hg.fileserver.util.constant;

public class FileConstant {
    public static final String DEFAULT_SAVE_KEY = "eon_fs";
    /**
     * fast-dfs常量部分
     */
    private final static String uploading = "uploading_";
    private final static String lock = uploading + "lock_";
    private final static String file = uploading + "file_";
    //当前所有锁(用在不同用户的上传前或重传前对整个文件的锁)
    public final static String currLocks = lock + "currLocks:";
    //当前锁的拥有者
    public final static String lockOwner = lock + "lockOwner:";

    //当前文件传输到第几块
    public final static String chunkCurr = file + "chunkCurr:";

    public final static String sizeCurr = file + "sizeCurr:";

    //当前文件上传到fastdfs路径
    public final static String fastDfsPath = file + "fastDfsPath:";

    //全部上传成功已完成
    public final static String completedList = uploading + "completedList";

    //文件块锁(解决同一个用户正在上传时并发解决,比如后端正在上传一个大文件块,前端页面点击删除按钮,
    // 继续添加删除的文件,这时候要加锁来阻止其上传,否则会出现丢块问题,
    // 因为fastdfs上传不像迅雷下载一样,下载时会创建一个完整的文件,如果上传第一块时,服务器能快速创建一个大文件0填充,那么这样可以支持并发乱序来下载文件块,上传速度会成倍提升,要实现乱序下载文件块,估计就得研究fastdfs源码了)
    public final static String chunkLock = lock + "chunkLock:";

    /**
     * token部分
     */
    /**
     * 存储当前登录用户id的字段名
     */
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";
    /**
     * token有效期（小时）
     */
    public static final int TOKEN_EXPIRES_HOUR = 12;

    /**
     * 存放Authorization的header字段
     */
    public static final String AUTHORIZATION = "authorization";

    public static final String SECRET_KEY ="jshg_#@2018";
}
