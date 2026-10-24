package com.fube.clientes.servicios;

import com.statsig.sdk.Statsig;
import com.statsig.sdk.StatsigUser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class StatsigService {

    private static final Logger log = LoggerFactory.getLogger(StatsigService.class);

    @Value("${statsig.server-secret-key:secret-key-placeholder}")
    private String serverSecretKey;

    @Value("${statsig.default-mask-pii:false}")
    private boolean defaultMaskPii;

    @PostConstruct
    public void init() {
        try {
            if (serverSecretKey != null && !serverSecretKey.isBlank() && !serverSecretKey.equals("secret-key-placeholder")) {
                Statsig.initializeAsync(serverSecretKey).get(3, TimeUnit.SECONDS);
                log.info("Statsig inicializado correctamente.");
            } else {
                log.info("Statsig: sin clave configurada, operando con valor por defecto: {}", defaultMaskPii);
            }
        } catch (Exception e) {
            log.warn("No se pudo inicializar Statsig, se usará valor por defecto: {}", e.getMessage());
        }
    }

    public boolean isMaskPiiEnabled() {
        return isMaskPiiEnabled("default_user");
    }

    public boolean isMaskPiiEnabled(String userId) {
        try {
            if (Statsig.isInitialized()) {
                return Statsig.checkGateSync(new StatsigUser(userId != null ? userId : "anonymous"), "mask-pii");
            }
        } catch (Exception e) {
            log.warn("Error al evaluar gate 'mask-pii' en Statsig: {}", e.getMessage());
        }
        return defaultMaskPii;
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (Statsig.isInitialized()) {
                Statsig.shutdown();
            }
        } catch (Exception e) {
            log.warn("Error al cerrar Statsig: {}", e.getMessage());
        }
    }
}
