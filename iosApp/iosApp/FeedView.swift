import SwiftUI
import shared

struct FeedView: View {
    let service = FeedService()

    @State var items = [FeedItem]()

    var body: some View {
        ScrollView {
            LazyVStack {
                ForEach(items, id: \.id, content: makeItemView)
            }
        }
        .task {
            items = try! await service.loadItems()
        }
    }

    func makeItemView(for item: FeedItem) -> some View {
        VStack(alignment: .leading) {
            GeometryReader { geometry in
                AsyncImage(url: item.image.flatMap { URL(string: $0.url) })
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .clipped()
            }
            .frame(height: 100)

            Text(item.title).font(.title)
            Text(item.author).font(.caption)
            Text(item.description_).font(.body)
                .padding(.top, 4)
        }
        .padding()
    }
}

struct FeedView_Previews: PreviewProvider {
    static var previews: some View {
        FeedView()
    }
}

