package cz.root.reader

class FeedService {

    suspend fun loadItems(): List<FeedItem> {
        return listOf(
            FeedItem(
                id = FeedItem.Id("1"),
                title = "Feed Item 1",
                description = "Description of the feed item number one",
                author = "Author",
                web = null,
                image = Image("https://picsum.photos/id/1/200"),
            ),
            FeedItem(
                id = FeedItem.Id("1"),
                title = "Feed Item 1",
                description = "Description of the feed item number one",
                author = "Author",
                web = null,
                image = Image("https://picsum.photos/id/2/200"),
            ),
        )
    }
}
