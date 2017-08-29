package acheng1314.cn.service;

import acheng1314.cn.dao.UserDao;
import acheng1314.cn.domain.User;
import acheng1314.cn.exception.EnterInfoErrorException;
import acheng1314.cn.exception.NotFoundException;
import acheng1314.cn.util.CipherUtils;
import acheng1314.cn.util.DateUtil;
import acheng1314.cn.util.EncryptUtils;
import acheng1314.cn.util.StringUtils;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by pc on 2017/8/11.
 */
@Service("userService")
@Aspect
//@Pointcut()
public class UserServiceImpl extends ServiceImpl<UserDao, User> {

    @Transactional
    public void addOneUser(User entity) throws Exception {
        if (StringUtils.isEmpty(entity.getLoginName(), entity.getPassword()))
            throw new Exception("用户名或密码不能为空！");
        //创建插入时间
        Integer createTime = DateUtil.getIntTime();
        entity.setCreateDate(createTime);
        //MD5密码加盐后再sha256加密
        entity.setPassword(EncryptUtils.encryptPassword(entity.getPassword().toLowerCase()
                , createTime.toString()));
        baseMapper.addUser(entity);
    }

    @Transactional
    public User login(String userLogin, String userPass) throws EnterInfoErrorException, NotFoundException {
        if (StringUtils.isEmpty(userLogin) || StringUtils.isEmpty(userPass)) {
            throw new EnterInfoErrorException("用户名和密码不能为空！请检查！");
        }
        User result = null;
        result = findOneById(userLogin);
        if (null == result) throw new NotFoundException("用户未找到！");
        try {
            userPass = userPass.toLowerCase();  //将大写md5转换为小写md5
            if (userPass.length() > 16 && userPass.length() == 32) {    //32位小写转换为16位小写
                userPass = userPass.substring(8, 24).toLowerCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new EnterInfoErrorException("密码错误！");
        }

        String encryptPassword = EncryptUtils.encryptPassword(userPass, result.getCreateDate().toString());

        if (!encryptPassword.equals(result.getPassword())) {
            throw new EnterInfoErrorException("用户名和密码不匹配！");
        }
        return result;
    }

    @Transactional
    public User findOneById(String userLogin) {
        return baseMapper.findOneByKey(userLogin);
    }
}