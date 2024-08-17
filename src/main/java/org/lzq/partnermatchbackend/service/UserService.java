package org.lzq.partnermatchbackend.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.lzq.partnermatchbackend.model.domain.User;
import org.lzq.partnermatchbackend.model.request.*;

import java.util.List;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @return 待定
     */
    Boolean userRegister(UserRegisterRequest requestParam);

    /**
     * 用户登录
     *
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(UserLoginRequest requestParam, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);



    Page<User> searchUsersByTags(List<String> tagNameList, String username, long pageSize, long pageNum);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

    /**
     * 推荐相似用户（未实现）
     *
     * @param pageSize 每页大小
     * @param pageNum 页码
     * @param request 当前会话
     * @return Page<User>
     */
    List<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request);

    /**
     * 修改用户信息
     *
     * @param user      用户信息
     * @param loginUser
     * @return 结果
     */
    int updateUser(UserEditRequest user, User loginUser);

    /**
     * 添加用户标签
     *
     * @param request 请求参数
     * @return Integer
     */
    Boolean addTag(UserTagAddRequest request);

    /**
     * 修改用户标签
     *
     * @param request 请求参数
     * @return Integer
     */
    Boolean removeTag(UserTagRemoveRequest request);
    /**
     * 根据id查询用户
     * @param userId 用户id
     * @return User
     */
    User getUserById(Integer userId);
}
