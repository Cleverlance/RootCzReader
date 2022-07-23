package cz.root.reader.console

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.Column
import com.jakewharton.mosaic.Text
import cz.root.reader.FeedItem

@Composable
fun FeedView(feedItems: List<FeedItem>) {
    Column {
        feedItems.forEach {
            FeedItemView(it)
        }
    }
}

@Composable
private fun FeedItemView(item: FeedItem) {
    Text(value = item.title)
    Text(value = item.author)
    Text(value = item.description)
    Text(value = "-----------------------")
}