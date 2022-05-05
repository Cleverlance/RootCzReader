package cz.root.reader

data class FeedItem(
    val id: Id,
    val title: String,
    val description: String,
    val author: String,
    val web: Web?,
    val image: Image?,
) {
    data class Id(val value: String)
}

data class Image(val url: String)
data class Web(val url: String)
