package uz.navbatuz.backend.auth.rate;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ForgotLimiter {
    private final ConcurrentHashMap<String, Long> last = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 60_000; // 1/min per email

    public boolean allow(String emailLower) {
        long now = System.currentTimeMillis();
        Long res = last.compute(emailLower, (k, v) ->
                (v == null || now - v > COOLDOWN_MS) ? now : v);
        return res != null && res == now;
    }
}
