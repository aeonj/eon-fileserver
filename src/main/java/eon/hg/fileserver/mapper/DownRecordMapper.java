package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.DownRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DownRecordMapper {
    List<DownRecord> getAll();

    DownRecord getOne(Long id);

    DownRecord getByIpAndFile(@Param("src_ip") String ip, @Param("file_id") Long file_id);

    void insert(DownRecord obj);

    void update(DownRecord obj);

    void delete(Long id);
}
