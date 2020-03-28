package pr.rpo.schloss.realm;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SystemRealm extends AuthorizingRealm {

    private static final Logger log = LoggerFactory.getLogger(SystemRealm.class);

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String user = "guest";
        String passwd = "guest";

        log.info("输入的用户名：{}", authenticationToken.getPrincipal());
        if(!user.equals(authenticationToken.getPrincipal())) {
            throw new UnknownAccountException("用户不存在");
        }

        log.info("输入的密码是：{}", authenticationToken.getCredentials());
        if(!passwd.equals(new String((char[]) authenticationToken.getCredentials()))) {
            throw new IncorrectCredentialsException("密码错误");
        }
        log.info("Realm：{}", this.getName());
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user,passwd, this.getName());
        return info;
    }
}
