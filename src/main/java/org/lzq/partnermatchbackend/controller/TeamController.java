package org.lzq.partnermatchbackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lzq.partnermatchbackend.common.BaseResponse;
import org.lzq.partnermatchbackend.common.ErrorCode;
import org.lzq.partnermatchbackend.exception.BusinessException;
import org.lzq.partnermatchbackend.model.domain.Team;
import org.lzq.partnermatchbackend.model.domain.User;
import org.lzq.partnermatchbackend.model.dto.TeamQuery;
import org.lzq.partnermatchbackend.model.request.DeleteRequest;
import org.lzq.partnermatchbackend.model.request.TeamAddRequest;
import org.lzq.partnermatchbackend.model.request.TeamJoinRequest;
import org.lzq.partnermatchbackend.model.request.TeamQuitRequest;
import org.lzq.partnermatchbackend.model.vo.TeamUserVO;
import org.lzq.partnermatchbackend.service.TeamService;
import org.lzq.partnermatchbackend.service.UserService;
import org.lzq.partnermatchbackend.service.UserTeamService;
import org.lzq.partnermatchbackend.utils.RedisUtils;
import org.lzq.partnermatchbackend.utils.ResultUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户接口
 *
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private RedisUtils redisUtils;

    /**
     * 新增队伍
     *
     * @param teamAddRequest 队伍信息
     * @return Long
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        // team新增后mybatis会自动回写id到该team对象上
        return ResultUtils.success(teamId);
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest 队伍 id 请求体
     * @param request         请求数据
     * @return Boolean
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null || teamQuitRequest.getTeamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamId, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 删除队伍
     *
     * @param deleteRequest 队伍 id 请求体
     * @param request       请求数据
     * @return Boolean
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 修改队伍
     *
     * @param team 队伍信息
     * @return Boolean
     */
    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.updateById(team);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据ID查询
     *
     * @param id 队伍 id
     * @return Team
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam("id") long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    /**
     * 查询全部列表
     *
     * @param teamQuery 查询参数
     * @return List<TeamUserVO>
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, request);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询列表
     *
     * @param teamQuery 查询参数
     * @return List<Team>
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teams = teamService.page(page, queryWrapper);
        return ResultUtils.success(teams);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 请求参数
     * @param request         请求数据
     * @return Boolean
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }


    /**
     * 获取我创建的队伍
     *
     * @param teamQuery 队伍查询参数
     * @param request   当前会话
     * @return List<TeamUserVO>
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<TeamUserVO> teamList = teamService.listMyCreateTeams(teamQuery, request);
        return ResultUtils.success(teamList);
    }


    /**
     * 获取我加入的队伍
     *
     * @param teamQuery 队伍查询参数
     * @param request   当前会话
     * @return List<TeamUserVO>
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<TeamUserVO> teamList = teamService.listMyJoinTeams(teamQuery, request);
        return ResultUtils.success(teamList);
    }
}
