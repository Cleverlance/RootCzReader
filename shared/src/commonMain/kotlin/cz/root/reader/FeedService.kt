package cz.root.reader

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import nl.adaptivity.xmlutil.serialization.UnknownChildHandler
import nl.adaptivity.xmlutil.serialization.XML

class FeedService {
    private val httpClient = HttpClient(httpEngine)
    private val xml = XML(SerializersModule {}) {
        unknownChildHandler = UnknownChildHandler { _,_,_,_,_ -> emptyList() }
    }

    suspend fun loadItems(): List<FeedItem> {
        val xmlString = httpClient.get(RSS_URL).bodyAsText()
        val rssDto = xml.decodeFromString(serializer<RssDto>(), xmlString)
        return rssDto.channel.items.map(::toDomain)
    }

    private fun toDomain(dto: ItemDto) = FeedItem(
        id = FeedItem.Id(dto.guid),
        title = dto.title,
        description = dto.description,
        author = dto.author,
        web = dto.link?.let(::Web),
        image = dto.enclosure?.url?.let(::Image)
    )

    companion object {
        const val RSS_URL = "https://www.root.cz/rss/clanky"
    }
}

internal expect val httpEngine: HttpClientEngine
