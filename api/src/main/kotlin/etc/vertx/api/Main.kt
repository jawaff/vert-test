package etc.vertx.api

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions

fun main(args: Array<String>) {
    startVertx()
}

fun startVertx(): Vertx {
    val vertxOptions = VertxOptions()
    val vertx = Vertx.vertx(vertxOptions)
    vertx.deployVerticle(RestServerVerticle())
        .onComplete {
            println("Started HTTP Server")
        }
    return vertx
}
