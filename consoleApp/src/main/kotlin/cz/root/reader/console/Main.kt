package cz.root.reader.console

import com.jakewharton.mosaic.runMosaic
import cz.root.reader.FeedService

fun main() = runMosaic {
    val service = FeedService()
    val feedItems = service.loadItems()
    setContent {
        FeedView(feedItems)
    }
}