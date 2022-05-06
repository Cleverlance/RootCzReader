# DEMO

Ukážeme si, jak postavit Kotlin Multiplatform projekt, vysvětlíme jak jsou jednotlivé části svázany dohromady, a naprogramujeme jednoduchou čtečku root.cz RSS kanálu pro Android a iOS. Pro tuto ukázku budeme potřebovat:
 - [Android Studio](https://developer.android.com/studio) s nainstalovaným [Kotlin Multiplatform Mobile pluginem](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile).
 - [Xcode](https://apps.apple.com/us/app/xcode) na zařízení s OSX. Toto je nezbýtné pouze pro vytvoření iOS aplikace, ostatní části projektu lze programovat na libovolném PC.

Začneme tím, že si v Anroid Studiu vytvoříme nový projekt dle šablony Kotlin Multiplatform App, kde změníme distribuci iOS knihovny z Cocoapods na regulární framework. Toto zjednodduší konfiguraci iOS aplikace kterou si detailně vysvětlíme níže.

```
.. obrázky konfigurace projektu ..
```

# Gradle
Gradle je nástroj, který slouží na definici toho, jak je náš projekt sestavený a jak se mají jednotlive komponenty kompilovat. I když programujeme v Android Studiu, to nám pouze ulehčuje psaní kódu a pod poklévkou využívá gradle na většinu svých funkcionalit.


Základním kámenem Gradle jsou skripty `gradlew` a `gradlew.bat` které pouštejí binárku `gradle-wrapper.jar` nebo aktualizuju používanou verzi dle konfigurace `gradle-wrapper.properties`

```
.. obrázek souboru gradle binárek / diagram souborove struktury ..
```

Soubor `settings.gradle.kts` je Kotlin skript, ktery definuje základní parametry projektu:
```kotlin
// Definice repozitářů odkud budeme stahovat jednotlivé pluginy
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
// Název projektu
rootProject.name = "Rootcz_Reader"
// Seznam modulů, se kterými bude gradle pracovat.
include(":androidApp")
include(":shared")
```
Zde si můžeme si všimnout, že modul `iosApp` v této konfiguraci chybí. Kotlin Multiplatform technologii dokážeme sdílet znáčnou část zdrojového kódu, no finální binárka musí být vyprodukována nástrojem specifickým pro konkrátní platformu. Pro Android je to stále Gradle, no pro iOS je to Xcode, tudiž Gradle o modulu `iosApp` nic neví.


Soubory `build.gradle.kts` - a máme jich v projektu více - jsou Kotlin skripty sloužící na konfiguraci build natavení jednotlivých modulů. Výjmkou je kořenový skript, kterým konfigurujeme všechny moduly:


```kotlin
buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
        classpath("com.android.tools.build:gradle:7.1.3")
    }
}
```
Tato část je hodně meta: nekonfiguruje totiž moduly samotné, ale konfiguruje nastavení, dle kterých se budou buildovat jednotlivé `build.gradle.kts` skripty: Jde totiž také o Kotlin kód, který může mít rúzne závislosti. V našem případě to je Kotlin Gradle plugin, a Android Tools.

```kotlin
// Zdroje závislostí pro jednotlivé moduly
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
// Definice příkazu na vyčištění projektu. 
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
```
Definovaný `clean` úkol si múžeme rovou vyskoušet z příkazového řádku:
```
$ ./gradlew clean
```
Gradle tímto smaže všechny `build` adresáře našeho projektu.

# Shared
Modul `shared` obsahuje veškerý kód který budeme sdílet mezi implementacemi pro specifické platformy. Jeho produktem budou knihovny, které naintegrujeme do finálních Android a iOS apek.
Zdrojový kód je členěn na dvě části:
- `src/commonMain` obashuje většinu kódu, zdrojáky v něm budou kompilovány do knihoven pro všechny platformy.
- `src/iosMain` , `src/androidMain` obsahujou kód který z nějakého dúvodu nemúže být plně sdílen. Kompilovány jsou jenom do knihoven pro konkrétní platformu.

`build.gradle.kts` obsahuje build nastavení pro modul `shared`:
```kotlin
plugins {
    kotlin("multiplatform")
    id("com.android.library")
}
```
Zde definujeme, že potřebujeme používat kotlin multiplatform, a vytvářet knihovnu pro Android.

```kotlin
kotlin {
    android()
```
Jedným z produktů kompilace kotlin kódu bude knihovna pro android

```kotlin
listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
).forEach {
    it.binaries.framework {
        baseName = "shared"
    }
}
```
Dalšímy produkty budou knihovny pro rúzné architektury, na kterých může iOS aplikace běžet, a budou zabaleny do binárky `shared.framework`
```kotlin
sourceSets {
    val commonMain by getting
    val androidMain by getting
    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by creating {
        dependsOn(commonMain)
        iosX64Main.dependsOn(this)
        iosArm64Main.dependsOn(this)
        iosSimulatorArm64Main.dependsOn(this)
    }
}
```
Dále tady máme definici pravidel pro jednotlivé sady zdrojových kódů - zatim je konfigurace prázdna, pozdeji zde doplníme závislosti na externích knihovnách.
```kotlin
android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }
}
```
A nakonec definice pro kompilaci android knihovny.

Šablona multiplatform aplikace obsahuje také zdrojové soubory `Platform.kt` a `Greeting.kt`, které ale múžeme smáznout a pustit se do implementace naši RSS čtečky.

# Příprava rozhraní
Napočátek by jsme si méli definovat, které části kódu budeme sdílet, a které budou implementovány zvlášť na každé platformě. Aby naše apka fungovala, budeme potřebovat:
1. Načítat data ze služby https://www.root.cz/rss/clanky
2. Naparsovat získané XML 
3. Zobrazit jednotlivé položky z načitaných dat.

Krok 3. musí respektovat UI a UX principy jednotlivých platforem, chceme ho tedy vyrobit separátně. Kroky 1. a 2. ale dokážeme bezproblému implementovat sdíleným kódem. 

Rozhraním mezi sdíleným kódem a kódem jednotlivých platforem tedy bude metoda, kterou získame data připravena na zobrazení.

```kotlin
class FeedService {
    suspend fun loadItems(): List<FeedItem> {

    }
}
```
Jednotlivé položky budou obsahovat titulek, popis, a url obrázku prísluchajíci k zobrazovanému článku:

```kotlin
data class FeedItem(
    val id: Id,
    val title: String,
    val description: String,
    val author: String,
    val image: Image?,
) {
    data class Id(val value: String)
}

data class Image(val url: String)
```
Naplňme ješte službu provizórnimi daty, abychom mohli vyskoušet integraci do UI:

```kotlin
suspend fun loadItems(): List<FeedItem> {
    return listOf(
        FeedItem(
            id = FeedItem.Id("1"),
            title = "Feed Item 1",
            description = "Description of the feed item number one",
            author = "Author",
            image = Image("https://picsum.photos/id/1/1024"),
        ),
        FeedItem(
            id = FeedItem.Id("1"),
            title = "Feed Item 1",
            description = "Description of the feed item number one",
            author = "Author",
            image = Image("https://picsum.photos/id/2/1024"),
        ),
    )
}
```

# Android Aplikace
TODO

# iOS Aplikace
iOS aplikaci budeme stavět na moderním UI framworku `SwiftUI`, který - podobně jako `Jetpack Compose` - umožňuje deklaratívní a velice intuitívní definici dizajnu. Dosud byl všechen kod psán v jazyce Kotlin, který ale (prozatím) nemá pro `SwiftUI` podporu. iOS aplikaci tedy budeme psát v jazyce Swift, který je Kotlinu hodně podobný, doufáme tedy že to čtenáře nebude rušit.

Pojďme si ale nejprve ukázat, jak je sdílený kód integrován do iOS aplikace.

iOS aplikace je buildována nástrojem Xcode, který využívá vlastný build systém. Detailný popis tohto systému je nad rámec článku, budeme se tedy soustredit jenom na specifika multiplatform projektu.

Prvním krokem je speciální build fáze, která volá Gradle s úkolem na vytvoření apple knihovny:
```shell
cd "$SRCROOT/.."
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```
Druhým krokem je nalinkování takhle vytvořené knihovny do iOS aplikace:
```
obrázek nalinkované knihovny
```

Teď je vše připravené, a múžeme s naším `shared` modulem pracovat rovnako jako s libovolnou nativní iOS knihovnou.

Vytvoříme si strukturu, která bude zastřešovat vyřeslení seznamu položek:

```swift
struct FeedView: View {
    // Pracujeme se sdílenou službou na dotažení dat
    let service = FeedService()
    // Držíme si posledně dotažené položky
    @State var items = [FeedItem]()

    var body: some View {
        // Chceme skrolovat obsah obrazovky
        ScrollView { 
            // Jednotlivé položky chceme vykřeslovat dle potřeby
            LazyVStack { 
                // Každou položku vykřeslíme metodou makeItemView
                ForEach(items, id: \.id, content: makeItemView)
            }
        }
        .task {
            // Pri zobrazení obrazovky chceme načítat data
            items = try! await service.loadItems()
        }
    }
}
```
Definujeme dizajn zobrazení jedné položky:
```swift
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
        Text(item.description_).font(.body).padding(.top, 4)
    }
    .padding()
}
```
A napokon definujeme, že chceme mít `FeedView` jakou součást aplikace:
```
struct iOSApp: App {
	var body: some Scene {
		WindowGroup {
			FeedView()
		}
	}
}
```
Hotovo. Po spustení aplikace uvidíme vykřeslen seznam našich provizorních dat:
```
obrázek aplikace
```
# Stažení a parsování dat
Na práci s webovýma službama a parsování XML dat budeme využívat externí knihovny, musíme jich tedy pro Gradle zadefinovat. Upravíme definícii závislostí v souboru `shared/build.gradle.kts`:
```kotlin
sourceSets {
    val commonMain by getting {
        dependencies {
            dependencies {
                // Na stažení dat využijeme knihovnu Ktor
                implementation("io.ktor:ktor-client-core:2.0.0")
                // Na práci s XML daty využijeme knihovnu XmlUtil
                implementation("io.github.pdvrieze.xmlutil:core:0.84.2")
                implementation("io.github.pdvrieze.xmlutil:serialization:0.84.2")
            }
        }
    }
    val androidMain by getting {
        dependencies {
            // Android bude používat OkHttp engine
            implementation("io.ktor:ktor-client-okhttp:2.0.0")
        }
    }
    val iosMain by creating {
        dependsOn(commonMain)
        iosX64Main.dependsOn(this)
        iosArm64Main.dependsOn(this)
        iosSimulatorArm64Main.dependsOn(this)

        dependencies {
            // iOS bude používat Darwin engine
            implementation("io.ktor:ktor-client-darwin:2.0.0")
        }
    }
}   
```
Teď múžeme připravit naší službu pro získaní rss položek na prácu s reálnymy daty:
```kotlin
class FeedService {
    // http klient na stažení dat
    private val httpClient = HttpClient(httpEngine)
    // XML dekoder
    private val xml = XML(SerializersModule {}) {
        // Při dekodováni chceme ignorovat data, která nepotřebujeme
        unknownChildHandler = UnknownChildHandler { _,_,_,_,_ -> emptyList() }
    }
}

internal expect val httpEngine: HttpClientEngine
```
Zde narážíme na dorbný problém: `HttpClient` potřebuje ke korektnímu fungování `HttpEngine`: stroječek který je implementačne na každé platformě jiný. Zadeklarovali jsme proto proměnnú jako `expect`. Tohle klíčové slovo kompajleru indikuje, že reálná hodnota není nastavena ve sdíleném kódě `commonMain` ale je definovaná zvlášť pro každou platformu v `iosMain` a `androidMain`:
```kotlin
// androidMain/AndroidClientEngine.kt
internal actual val httpEngine: HttpClientEngine = OkHttp.create {
}

// iosMain/IosClientEngine.kt
internal actual val httpEngine: HttpClientEngine = Darwin.create {
}
```
Tady klíčové slovo `actual` kompajleru říká, že jde o definici hodnoty pro proměnnú deklarovanú ve sdílenem kóde.


Teď sme téměř připravení na stažení reálného RSS feedu. Ostává nám definovat strukturu RSS dat:
```kotlin
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
```
A nahradit provizórná data voláním webové služby a parsováním dat:
```kotlin
class FeedService {
    suspend fun loadItems(): List<FeedItem> {
        // Stáhneme XML súbor
        val xmlString = httpClient.get("https://www.root.cz/rss/clanky").bodyAsText()
        // Dekodujeme XML do Kotlin struktury
        val rssDto = xml.decodeFromString(serializer<RssDto>(), xmlString)
        // Prevedeme Kotlin XML strukturu do modelu pro zobrazení
        return rssDto.channel.items.map(::toDomain)
    }
    // Konverze jedné položky z XML struktury
    private fun toDomain(dto: ItemDto) = FeedItem(
        id = FeedItem.Id(dto.guid),
        title = dto.title,
        description = dto.description,
        author = dto.author,
        web = dto.link?.let(::Web),
        image = dto.enclosure?.url?.let(::Image)
    )
}
```
Po spustení uprevených aplikací se nám zobrazí data z RSS kanálu:
```
obrazek aplikací
```