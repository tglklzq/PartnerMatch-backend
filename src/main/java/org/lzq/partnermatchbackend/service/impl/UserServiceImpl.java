package org.lzq.partnermatchbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.lzq.partnermatchbackend.common.ErrorCode;
import org.lzq.partnermatchbackend.constant.UserConstant;
import org.lzq.partnermatchbackend.exception.BusinessException;
import org.lzq.partnermatchbackend.mapper.UserMapper;
import org.lzq.partnermatchbackend.model.domain.User;
import org.lzq.partnermatchbackend.model.request.*;
import org.lzq.partnermatchbackend.service.UserService;
import org.lzq.partnermatchbackend.utils.AddressUtils;
import org.lzq.partnermatchbackend.utils.AlgorithmUtils;
import org.lzq.partnermatchbackend.utils.DesensitizationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.lzq.partnermatchbackend.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "candy-ling";

    @Override
    public Boolean userRegister(UserRegisterRequest requestParam) {
        String userAccount = requestParam.getUserAccount();
        String userPassword = requestParam.getUserPassword();
        String checkPassword = requestParam.getCheckPassword();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword,checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和重复密码不相同");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return false;
        }

        // 账户不能重复
        long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserAccount, userAccount)
        );

        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        //密码进行 MD5加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        requestParam.setUserPassword(encryptPassword);
        // 3. 插入数据
        User user =BeanUtil.toBean(requestParam, User.class);
        user.setUsername(userAccount);
        user.setAvatarUrl("https://s21.ax1x.com/2024/08/17/pACo2Bd.png");
        int inserted =baseMapper.insert(user);
        return inserted >= 1;
    }



    @Override
    public User userLogin(UserLoginRequest requestParam, HttpServletRequest request) {
        String userAccount = requestParam.getUserAccount();
        String userPassword = requestParam.getUserPassword();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUserAccount,userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 获取 IP 地址
        String location = Objects.requireNonNull(AddressUtils.getIp()).toString();
        user.setLocation(location);
        userMapper.updateById(user);
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safeUser = new User();
        safeUser.setUserId(originUser.getUserId());
        safeUser.setUsername(originUser.getUsername());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
        safeUser.setPhone(DesensitizationUtils.desensitizePhone(originUser.getPhone()));
        safeUser.setEmail(DesensitizationUtils.desensitizeEmail(originUser.getEmail()));
        safeUser.setProfile(originUser.getProfile());
        safeUser.setLocation(originUser.getLocation());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setUserStatus(originUser.getUserStatus());
        safeUser.setCreateTime(originUser.getCreateTime());
        safeUser.setTags(originUser.getTags());
        return safeUser;
    }


    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }



    /**
     * 根据标签搜索用户 (SQL分页查询版)
     *
     * @param tagNameList 用户所拥有的的标签
     * @param username
     * @return
     */
    @Override
    public Page<User> searchUsersByTags(List<String> tagNameList, String username, long pageSize, long pageNum) {
        if (StringUtils.isBlank(username) && CollectionUtils.isEmpty(tagNameList)) {
            return new Page<>();
        }
        // SQL查询方式
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (!CollectionUtils.isEmpty(tagNameList)) {
            // 拼接 and 查询
            for (String tagName : tagNameList) {
                qw = qw.like("tags", tagName);
            }
        }
        if (!StringUtils.isBlank(username)) {
            qw.like("username", username);
        }
        Page<User> userPage = this.page(new Page<>(pageNum, pageSize), qw);
        List<User> collect = userPage.getRecords().stream().map(this::getSafetyUser).collect(Collectors.toList());
        userPage.setRecords(collect);
        return userPage;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getUserId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行 update 语句
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getUserId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 伙伴匹配 使用的是Jaccard相似度
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id", "tags","location");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {}.getType());

        PriorityQueue<Pair<User, Double>> maxHeap = new PriorityQueue<>(Comparator.comparingDouble(Pair::getValue));

        for (User user : userList) {
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getUserId(), loginUser.getUserId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {}.getType());
            double similarity=AlgorithmUtils.minDistance(tagList, userTagList);
            if (maxHeap.size() < num) {
                maxHeap.add(new Pair<>(user, similarity));
            } else if (maxHeap.peek() != null && similarity > maxHeap.peek().getValue()) {
                maxHeap.poll();
                maxHeap.add(new Pair<>(user, similarity));
            }
        }

        List<Long> userIdList = maxHeap.stream()
                .map(pair -> pair.getKey().getUserId())
                .collect(Collectors.toList());

        if (userIdList.isEmpty()) {
            return new ArrayList<>();
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("user_id", userIdList);
        Map<Long, User> userIdUserMap = this.list(userQueryWrapper).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        return userIdList.stream()
                .map(userIdUserMap::get)
                .collect(Collectors.toList());
    }




    /**
     * 首页用户推荐
     *
     * @param pageSize 分页大小
     * @param pageNum  页码
     * @param request  当前会话
     * @return
     */
    @Override
    public List<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        // 1. 获取当前用户
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return Collections.emptyList();
        }

        // 2. 随机获取当前用户的一个标签
        String randomTag = getRandomTagFromUser(loginUser);

        // 3. 查询匹配该标签的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 排除当前用户
        queryWrapper.ne("user_id", loginUser.getUserId());

        // 根据随机选择的标签进行匹配查询
        if (randomTag != null) {
            queryWrapper.like("tags", randomTag);
        }

        // 分页查询
        Page<User> userPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<User> allUsers = userPage.getRecords();

        // 4. 筛选在线用户

        return allUsers.stream()
                .filter(user -> request.getSession().getAttribute(USER_LOGIN_STATE) != null)
                .map(this::getSafetyUser)
                .collect(Collectors.toList());
    }


    private String getRandomTagFromUser(User user) {
        String tags = user.getTags(); // 从数据库中获取标签字段
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        // 将标签分割为集合
        List<String> tagList = Arrays.asList(tags.split(","));
        // 随机获取一个标签
        Random random = new Random();
        return tagList.get(random.nextInt(tagList.size()));
    }



    /**
     * 修改用户信息
     *
     * @param userEditRequest      用户信息
     * @param loginUser
     * @return 结果
     */
    @Override
    public int updateUser(UserEditRequest userEditRequest, User loginUser) {
        long userId = userEditRequest.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 不是管理员/本人，则报错
        if (!isAdmin(loginUser) && userId != loginUser.getUserId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 管理员/本人，则更新
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        User user = new User();
        StringJoiner tags = new StringJoiner(",", "[", "]");
        for (String str : userEditRequest.getTags()) {
            tags.add("\"" + str + "\"");
        }
        user.setTags(tags.toString());
        BeanUtils.copyProperties(userEditRequest, user);

        return userMapper.updateById(user);
    }

    @Override
    public Boolean addTag(UserTagAddRequest request) {
        if (request.getTag() == null) throw new BusinessException(ErrorCode.INSERT_ERROR, "添加标签内容为空");
        User user = this.getById(request.getUserId());
        String tags = user.getTags();
        // 更新JSON格式的tag标签
        //JSONArray tagsArr = JSON.parseArray(tags);
        JSONArray tagsArr;
        if(tags==null||tags.isEmpty()){
            tagsArr = new JSONArray();
        }else {
            tagsArr = JSON.parseArray(tags);
        }
        tagsArr.add(request.getTag());
        user.setTags(tagsArr.toJSONString());
        return this.updateById(user);
    }

    @Override
    public Boolean removeTag(UserTagRemoveRequest request) {
        List<String> oldTags = request.getOldTags();
        List<String> tagsList = new ArrayList<>();
        // 遍历oldTags，如果有相同的tag，则不添加
        for (String oldTag : oldTags) {
            if (!oldTag.equals(request.getTag())) {
                tagsList.add(oldTag);
            }
        }
        String newTags = JSON.toJSONString(tagsList);

        User user = new User();
        user.setUserId(request.getUserId());
        user.setTags(newTags);
        return this.updateById(user);
    }

    @Override
    public User getUserById(Integer userId) {
        return this.getById(userId);
    }

    /**
     * 根据标签搜索用户（SQL 查询版）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like '%Python%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

}




