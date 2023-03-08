package etc.vertx.api

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.TemplateHandler
import io.vertx.ext.web.openapi.RouterBuilder
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

class RestServerVerticle : AbstractVerticle() {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(RestServerVerticle::class.java)
        val isStarted = AtomicBoolean(false)

        private const val API_PATH = "api"

        private val SPEC_PATHS = listOf(
            "api/v1/openapi/testApi.yaml"
        )
    }

    override fun start() {
        val globalRouter: Router = Router.router(vertx)

        globalRouter.route("/$API_PATH/*")
            // Serves the yaml files in resources/openapi/ and alters them with the handlebars template engine.
            .handler(
                TemplateHandler.create(
                    HandlebarsTemplateEngine.create(this.vertx, "yaml"),
                    "api",
                    "text/yaml"
                )
            )

        var curFuture: Future<*> = setupSubRouter(globalRouter, 0)
        for (i in 1..100) {
            curFuture = curFuture.compose { setupSubRouter(globalRouter, 1) }
        }

        curFuture.andThen {
            val server = vertx.createHttpServer(HttpServerOptions().setPort(8080).setHost("localhost"))
                .requestHandler(globalRouter)
            server.listen()
                .onSuccess { isStarted.set(true); println("Server started on port " + server.actualPort()) }
                .onFailure(Throwable::printStackTrace)
        }
    }

    private fun setupSubRouter(globalRouter: Router, index: Int): Future<*> {
        val specIndex = index % SPEC_PATHS.size
        val specPath = SPEC_PATHS[specIndex]
        println(specPath)

        return RouterBuilder.create(this.vertx, specPath)
            .onFailure(Throwable::printStackTrace)
            .andThen { result -> globalRouter.route("/v$index/*").subRouter(result.result().createRouter()) }
    }
}
