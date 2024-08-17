package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.UserTeam;
import generator.service.UserTeamService;
import generator.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author liangzhiquan
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2024-08-13 22:55:19
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




