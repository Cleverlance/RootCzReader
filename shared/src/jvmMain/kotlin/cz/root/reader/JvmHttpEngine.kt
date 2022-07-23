package cz.root.reader

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

internal actual val httpEngine: HttpClientEngine = OkHttp.create {
}
