package ar.com.utn.proyecto.qremergencias.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

@Service
@Slf4j
public class LoginAttemptCacheService {

    @Autowired
    private CacheManager cacheManager;

    private Cache sessionCache;
    private Cache userCache;

    @Value("${qremergencias.login.attempts.user}")
    private int allowedAttemptsPerUser;

    @Value("${qremergencias.login.attempts.cache.user}")
    private String userCacheName;

    @Value("${qremergencias.login.attempts.cache.session}")
    private String sessionCacheName;

    @Autowired
    private UserService userService;

    @PostConstruct
    public void init() {
        sessionCache = cacheManager.getCache(sessionCacheName);
        userCache = cacheManager.getCache(userCacheName);
    }

    private Integer incrementAttempt(final Object key, final String cacheName) {
        final Cache cache = cacheManager.getCache(cacheName);
        final Integer numberOfAttempts = cache.get(key, Integer.class);
        final Integer failedAttemtps = numberOfAttempts == null ? 1 :  numberOfAttempts + 1;
        cache.put(key, failedAttemtps);
        return failedAttemtps;
    }

    public void failedLogin(final String login) {

        final Integer userAttempts = incrementAttempt(login, userCacheName);

        if (userAttempts >= allowedAttemptsPerUser
                && userService.blockUser(login)) {
            userCache.evict(login);
            log.warn("User " + login + " blocked");
        }

    }

    public void failedLogin(final HttpSession session) {
        incrementAttempt(session.getId(), sessionCacheName);
    }

    public void loginSuccess(final String login) {
        userCache.evict(login);
    }

    public void loginSuccess(final HttpSession session) {
        sessionCache.evict(session.getId());
    }

    public Integer loginAttempts(final HttpSession session) {
        final Integer loginAttempts = sessionCache.get(session.getId(), Integer.class);
        if (loginAttempts != null) {
            return loginAttempts;
        }
        return 0;
    }

}
