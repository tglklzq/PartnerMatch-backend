package org.lzq.partnermatchbackend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.lzq.partnermatchbackend.model.domain.Team;
import org.lzq.partnermatchbackend.model.domain.User;
import org.lzq.partnermatchbackend.model.dto.TeamQuery;
import org.lzq.partnermatchbackend.model.request.TeamJoinRequest;
import org.lzq.partnermatchbackend.model.vo.TeamUserVO;

import java.util.List;

/**
 * 队伍服务
 *
 */
public interface TeamService extends IService<Team> {

    /**
     * 查询队伍列表
     *
     * @param teamQuery 查询关键字
     * @param request 当前会话
     * @return List<TeamUserVO>
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request);


    /**
     * 创建队伍
     *
     * @param team 队伍信息
     * @param loginUser 当前用户信息
     * @return 队伍ID
     */
    long addTeam(Team team, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 加入队伍的信息
     * @param loginUser 登陆用户
     * @return boolean
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 删除队伍
     *
     * @param id 队伍 id
     * @param loginUser 当前用户
     * @return boolean
     */
    boolean deleteTeam(long id, User loginUser);

    /**
     * 退出队伍
     *
     * @param teamId 队伍 id
     * @param loginUser 当前用户
     * @return boolean
     */
    boolean quitTeam(Long teamId, User loginUser);

    /**
     * 获取我加入的队伍列表
     * @param teamQuery 查询参数
     * @param request 当前会话
     * @return List<TeamUserVO>
     */
    List<TeamUserVO> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request);

    /**
     * 获取我创建的队伍列表
     * @param teamQuery 查询参数
     * @return List<TeamUserVO>
     */
    List<TeamUserVO> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request);
}
