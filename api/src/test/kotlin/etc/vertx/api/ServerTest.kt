package etc.vertx.api

import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class ServerTest {
    @Test
    fun startUp() {
        val server = startVertx()
        val start = Instant.now()
        while (!RestServerVerticle.isStarted.get() && Duration.between(start, Instant.now()) < Duration.ofMinutes(30)) {
            Thread.sleep(1_000)
        }
    }
}
