package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.TbWarningData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TbWarningDataMapper {

    List<TbWarningData> findByIp(@Param("ip_addr") String ipAddr);
}
