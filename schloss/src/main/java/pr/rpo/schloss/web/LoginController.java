package pr.rpo.schloss.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @ResponseBody
    @RequestMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("text", "success");
        redisTemplate.opsForValue().set("a", "b");
        return "success";
    }

    @RequestMapping("/login")
    public String loginPage() {
        log.debug("test log");

        Subject currentUser = SecurityUtils.getSubject();
        log.info("{}",currentUser.isAuthenticated());

        return "login";
    }

    @ResponseBody
    @RequestMapping("/loginS")
    public String loginOK(Model model, String username, String password) {
        log.debug("loginS");
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        Subject subject = SecurityUtils.getSubject();

        try {
            subject.login(token);
            model.addAttribute("text", "login ok");
        }catch (UnknownAccountException e) {
            log.info(e.toString());
            model.addAttribute("text", "login fail1");
        }catch (IncorrectCredentialsException e) {
            log.info(e.toString());
            model.addAttribute("text", "login fail2");
        }

        return model.getAttribute("text").toString();
    }

    @ResponseBody
    @RequestMapping("/fail")
    public String fail(Model model) {
        model.addAttribute("text","fail");
        return "fail";
    }

    @ResponseBody
    @RequestMapping("/logout")
    public String logout(Model model) {
        SecurityUtils.getSubject().logout();
        model.addAttribute("text", "退出登录");
        return "退出登录";
    }
}
