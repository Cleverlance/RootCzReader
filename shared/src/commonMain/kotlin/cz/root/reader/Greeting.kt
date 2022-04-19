package cz.root.reader

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}
