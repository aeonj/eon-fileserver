package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.WarningData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WarningDataMapper {

    List<WarningData> findByIp(@Param("ip_addr") String ipAddr);
}
