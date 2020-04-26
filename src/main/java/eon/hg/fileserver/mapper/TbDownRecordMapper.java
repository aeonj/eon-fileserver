package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.TbDownRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TbDownRecordMapper {
    List<TbDownRecord> getAll();

    TbDownRecord getOne(Long id);

    TbDownRecord getByIpAndFile(@Param("src_ip") String ip, @Param("file_id") Long file_id);

    void insert(TbDownRecord obj);

    void update(TbDownRecord obj);

    void delete(Long id);
}
