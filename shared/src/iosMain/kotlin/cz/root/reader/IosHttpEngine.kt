package cz.root.reader

import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.HttpClientEngine

internal actual val httpEngine: HttpClientEngine = Darwin.create {
}
