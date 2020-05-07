package eon.hg.fileserver.mapper;

import eon.hg.fileserver.model.WarningData;
import eon.hg.fileserver.model.WarningUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WarningMapper {

    List<WarningData> getAllWarningData();

    WarningData getOneWarningData(Long id);

    WarningData getWarningDataByIpAddr(@Param("ip_addr") String ipAddr);

    void insertWarningData(WarningData obj);

    void updateWarningData(WarningData obj);

    void deleteWarningData(Long id);

    List<WarningUser> getAllWarningUser();

    WarningUser getOneWarningUser(Long id);

    WarningUser getWarningUserByName(@Param("name") String name);

    void insertWarningUser(WarningUser obj);

    void updateWarningUser(WarningUser obj);

    void deleteWarningUser(Long id);
}
