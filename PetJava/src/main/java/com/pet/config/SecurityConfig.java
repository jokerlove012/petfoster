package com.pet.config;

import com.pet.security.JwtAuthenticationEntryPoint;
import com.pet.security.JwtAuthenticationFilter;
import com.pet.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置类
 * 负责配置应用的安全策略，包括JWT认证、权限控制、密码加密等
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造函数，注入必要的依赖
     * @param userDetailsService 用户详情服务
     * @param jwtAuthenticationEntryPoint JWT认证入口点
     * @param jwtAuthenticationFilter JWT认证过滤器
     */
    public SecurityConfig(@Lazy UserDetailsServiceImpl userDetailsService,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置密码加密器
     * 使用BCrypt算法对用户密码进行加密存储
     * @return PasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器Bean
     * @return AuthenticationManager实例
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 配置认证管理器
     * 设置用户详情服务和密码加密器
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * 配置HTTP安全策略
     * 包括CORS、CSRF、会话管理、请求授权等
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 启用CORS支持
                .cors().and()
                // 禁用CSRF保护（因为使用JWT认证）
                .csrf().disable()
                // 配置异常处理，使用自定义的JWT认证入口点
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                // 配置会话管理为无状态（不使用Session）
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 配置请求授权规则
                .authorizeRequests()
                // 允许匿名访问的接口
                .antMatchers("/auth/**").permitAll()           // 认证相关接口
                .antMatchers("/institutions/**").permitAll()    // 机构相关接口
                .antMatchers("/uploads/**").permitAll()          // 上传文件访问
                .antMatchers("/ai/**").permitAll()               // AI相关接口
                .antMatchers("/admin/**").permitAll()            // 管理员接口
                // 其他所有请求都需要认证
                .anyRequest().authenticated();

        // 在用户名密码认证过滤器之前添加JWT认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
