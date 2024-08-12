package org.lzq.partnermatchbackend.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.lzq.partnermatchbackend.common.ErrorCode;
import org.lzq.partnermatchbackend.exception.BusinessException;
import org.lzq.partnermatchbackend.mapper.UserMapper;
import org.lzq.partnermatchbackend.model.domain.User;
import org.lzq.partnermatchbackend.model.request.UserLoginRequest;
import org.lzq.partnermatchbackend.model.request.UserRegisterRequest;
import org.lzq.partnermatchbackend.model.request.UserUpdateRequest;
import org.lzq.partnermatchbackend.model.response.UserLoginResponse;
import org.lzq.partnermatchbackend.service.UserService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.lzq.partnermatchbackend.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.lzq.partnermatchbackend.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
* @author liangzhiquan
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-08-12 02:11:46
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    @Resource
    private UserMapper userMapper;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 盐值。密码加密
     */
    private  static final String SALT = "LiangZhiquan";

    @Override
    public void userRegister(UserRegisterRequest requestParam) {
        String email = requestParam.getEmail();
        String userName = requestParam.getUsername();
        String userPassword = requestParam.getUserpassword();

        // 1. 校验
        if (!isValidEmail(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱包含特殊字符");
        }
        if (StringUtils.isAnyBlank(email,userName,userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名称过短");
        }

        RLock usernameLock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        RLock emailLock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getEmail());
        try {
            boolean locked = usernameLock.tryLock(10, 5, TimeUnit.SECONDS) && emailLock.tryLock(10, 5, TimeUnit.SECONDS);
            if (!locked) {throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取锁失败");}
            // 检查布隆过滤器中是否存在该用户名或电子邮件
            if (hasUsername(requestParam.getUsername())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
            }
            if (hasEmail(requestParam.getEmail())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "电子邮件已存在");
            }
            //密码信息加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            requestParam.setUserpassword(encryptPassword);
            int result = baseMapper.insert(BeanUtil.toBean(requestParam, User.class));
            if (result<1) {throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册用户失败");}
            // 将用户名和电子邮件添加到布隆过滤器中
            String usernameKey = "user:" + requestParam.getUsername();
            String emailKey = "email:" + requestParam.getEmail();
            userRegisterCachePenetrationBloomFilter.add(usernameKey);
            userRegisterCachePenetrationBloomFilter.add(emailKey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取锁中断");
        } finally {
            // 确保锁的释放
            if (emailLock.isHeldByCurrentThread()) {emailLock.unlock();}
            if (usernameLock.isHeldByCurrentThread()) {usernameLock.unlock();}
        }
    }

    @Override
    public UserLoginResponse userLogin(UserLoginRequest requestParam) {

        String userName=requestParam.getUsername();
        String userPassword=requestParam.getUserpassword();
        if (StrUtil.isAllEmpty(userName, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, userName)
                .eq(User::getUserpassword, DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes()));
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + requestParam.getUsername());
        if(CollUtil.isNotEmpty(hasLoginMap)){
            stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR,"用户登录错误"));
            return new UserLoginResponse(token);
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + requestParam.getUsername(), uuid, JSON.toJSONString(user));
        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginResponse(uuid);
    }

    @Override
    public void updateUser(UserUpdateRequest requestParam) {
        //todo 验证用户是否登录
//        if (!Objects.equals(requestParam.getEmail(), UserContext.getUsername())) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"当前登录用户修改请求异常");
//        }
        if (requestParam.getUserpassword()!=null){
            requestParam.setUserpassword(DigestUtils.md5DigestAsHex((SALT + requestParam.getUserpassword()).getBytes()));
        }
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getUsername, requestParam.getUsername());

        baseMapper.update(BeanUtil.toBean(requestParam, User.class), updateWrapper);
    }
    @Override
    public Boolean checkLogin(String userName, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + userName, token) != null;
    }

    @Override
    public void logout(String userName, String token) {
        if (checkLogin(userName, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + userName);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户Token不存在或用户未登录");
    }










    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email != null && email.matches(emailRegex);
    }

    public Boolean hasUsername(String username){
        if(StringUtils.isBlank(username)){throw new BusinessException(ErrorCode.NULL_ERROR);}
        //return this.baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,username)) == null;
        //todo 注册用户时，将信息放入布隆过滤器
        String usernameKey = "user:" + username;
        //判断布隆过滤器中是否存在 存在返回true，代表用户名不可用 不存在返回false，代表用户名可用
        return userRegisterCachePenetrationBloomFilter.contains(usernameKey);
    }


    public Boolean hasEmail(String email){
        if(StringUtils.isBlank(email)){throw new BusinessException(ErrorCode.NULL_ERROR);}
        String emailKey = "email:" + email;
        return userRegisterCachePenetrationBloomFilter.contains(emailKey);
    }
}




