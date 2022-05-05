package cz.root.reader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("rss")
data class RssDto(
    val version: String,
    @XmlElement(true) val channel: ChannelDto,
)

@Serializable
@SerialName("channel")
data class ChannelDto(
    val items: List<ItemDto>
)

@Serializable
@SerialName("item")
data class ItemDto(
    @XmlElement(true) val guid: String,
    @XmlElement(true) val title: String,
    @XmlElement(true) val description: String,
    @XmlElement(true) val author: String,
    @XmlElement(true) val pubDate: String,
    @XmlElement(true) val link: String?,
    @XmlElement(true) val enclosure: EnclosureDto?,
)

@Serializable
@SerialName("enclosure")
data class EnclosureDto(
    val url: String
)
