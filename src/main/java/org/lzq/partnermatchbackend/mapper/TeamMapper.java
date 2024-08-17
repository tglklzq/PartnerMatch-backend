package org.lzq.partnermatchbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.lzq.partnermatchbackend.model.domain.Team;
import org.lzq.partnermatchbackend.model.dto.TeamQuery;

import java.util.List;

/**
* @author liangzhiquan
* @description 针对表【team(队伍)】的数据库操作Mapper
*/
public interface TeamMapper extends BaseMapper<Team> {
    /**
     * 查询队伍列表
     *
     * @param teamQuery 查询关键字
     * @return List<Team>
     */
    List<Team> listByTeamQuery(TeamQuery teamQuery);

    /**
     * 增加队伍加入人数
     *
     * @param teamId 队伍ID
     * @return 是否成功
     */
    boolean addTeamJoinNum(Long teamId);
}




