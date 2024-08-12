package org.lzq.partnermatchbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lzq.partnermatchbackend.model.domain.Team;
import org.lzq.partnermatchbackend.service.TeamService;
import org.lzq.partnermatchbackend.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author liangzhiquan
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-08-12 13:35:58
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




