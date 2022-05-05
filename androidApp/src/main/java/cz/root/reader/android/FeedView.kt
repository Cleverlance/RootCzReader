package cz.root.reader.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cz.root.reader.FeedItem
import cz.root.reader.FeedService

@Composable
fun FeedView(service: FeedService) {
    val (feedItems, setFeedItems) = remember { mutableStateOf<List<FeedItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        setFeedItems(service.loadItems())
    }

    LazyColumn(
        contentPadding = PaddingValues(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(feedItems) {
            FeedItemView(it)
        }
    }
}

@Composable
private fun FeedItemView(item: FeedItem) {
    Column {
        AsyncImage(
            model = item.image?.url,
            contentDescription = null,
            modifier = Modifier.height(100.dp),
            contentScale = ContentScale.FillWidth
        )
        Text(text = item.title, style = MaterialTheme.typography.h5)
        Text(text = item.author, style = MaterialTheme.typography.caption)
        Spacer(Modifier.height(4.dp))
        Text(text = item.description, style = MaterialTheme.typography.body1)
    }
}
