package pr.rpo.schloss.config;

import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import pr.rpo.schloss.realm.SystemRealm;

import java.util.LinkedHashMap;
import java.util.Map;

//@Configuration
public class ShiroConfig {

    @Bean
    public FormAuthenticationFilter formAuthenticationFilter() {
        FormAuthenticationFilter bean = new FormAuthenticationFilter();
        return bean;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager, FormAuthenticationFilter formAuthenticationFilter) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);
//        bean.setLoginUrl("/login");
//        bean.setUnauthorizedUrl("/fail");

//        Map<String, Filter> filters = new HashMap<>();
//        filters.put("authc", formAuthenticationFilter);

//        bean.setFilters(filters);

        Map<String, String> filterChains = new LinkedHashMap<>();

        filterChains.put("/loginS", "anon");
//        filterChains.put("/**","authc");

        bean.setFilterChainDefinitionMap(filterChains);

        return bean;
    }

    @Bean
    public SecurityManager securityManager(Realm myRealm) {
//        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
//        WebIniSecurityManagerFactory bean =  new WebIniSecurityManagerFactory(Ini.fromResourcePath("classpath:shiro.ini"));
        DefaultWebSecurityManager bean = new DefaultWebSecurityManager();
        bean.setRealm(myRealm);
        bean.setCacheManager(new MemoryConstrainedCacheManager());
        return bean;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor bean = new AuthorizationAttributeSourceAdvisor();
        bean.setSecurityManager(securityManager);
        return bean;
    }

    @Bean
    public Realm myRealm() {
        SystemRealm bean = new SystemRealm();
        return bean;
    }
}
