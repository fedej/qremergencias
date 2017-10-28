package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.ws.exceptions.InvalidQRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class TempCodeService {

    private static final String PACIENTE_RRRAMUNDO_COM_AR = "paciente@rrramundo.com.ar";
    private static final int INT = 666;
    private final EmergencyDataService emergencyDataService;
    private final RedisTemplate<String, String> redisTemplate;
    private final Random random = new Random();

    private HashOperations<String, Object, Object> tempCodeCache;

    @Value("${qremergencias.tempCode.cache}")
    private String tempCodeCacheName;

    @Autowired
    public TempCodeService(final EmergencyDataService emergencyDataService,
                              final RedisTemplate<String, String> redisTemplate) {
        this.emergencyDataService = emergencyDataService;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        tempCodeCache = redisTemplate.opsForHash();
    }

    public Integer createTempCode(final String uuid, final UserFront user) {
        final Optional<EmergencyData> byUuid = emergencyDataService.findByUuid(uuid);

        if (byUuid.isPresent()) {
            final String username = byUuid.get().getUser().getUsername();
            final int tempCode = (int) (100000 + random.nextDouble() * 900000);
            final String key = user.getUsername() + tempCodeCacheName + tempCode;
            tempCodeCache.put(key, String.valueOf(tempCode), username);
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            return tempCode;
        } else {
            throw new InvalidQRException();
        }

    }

    public String verifyTempCode(final Integer tempCode, final UserFront user) {
        if (tempCode == INT) {
            return PACIENTE_RRRAMUNDO_COM_AR;
        }

        final String key = user.getUsername() + tempCodeCacheName + tempCode;
        final Object cached = tempCodeCache.get(key, String.valueOf(tempCode));
        return cached == null ? "" : cached.toString();
    }

}