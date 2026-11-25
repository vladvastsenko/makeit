package fi.vladi.makeit
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.PathParser
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
private val paths=listOf("M40 25 Q40 5 60 5 Q80 5 80 25 L78 35 L110 48 L104 65 L80 58 L80 78 L95 112 L75 118 L60 88 L45 118 L25 112 L40 78 L40 58 L16 65 L10 48 L42 35 Z M50 23 Q60 12 70 23 Q60 34 50 23 Z",
"M60 5 L85 40 L74 40 L100 75 L85 75 L115 105 L70 105 L70 118 L50 118 L50 105 L5 105 L35 75 L20 75 L46 40 L35 40 Z",
"M42 28 Q32 5 60 5 Q88 5 78 28 Q105 42 88 65 Q120 100 90 115 L30 115 Q0 100 32 65 Q15 42 42 28 Z",
"M35 45 L15 15 L25 10 L40 30 L40 5 L50 5 L50 35 L70 35 L70 5 L80 5 L80 30 L95 10 L105 15 L85 45 Q110 70 80 85 L75 115 L45 115 L40 85 Q10 70 35 45 Z",
"M60 5 L76 43 L117 47 L86 74 L95 115 L60 93 L25 115 L34 74 L3 47 L44 43 Z",
"M40 48 Q10 0 35 5 Q50 8 55 43 Q70 0 85 5 Q105 10 80 48 Q115 80 90 110 Q60 125 30 110 Q5 80 40 48 Z",
"M5 80 L25 65 Q40 40 65 55 L80 20 Q100 5 115 25 L112 42 L95 42 L85 80 L90 115 L75 115 L65 88 L45 88 L40 115 L25 115 L25 85 Z",
"M5 65 L20 60 L38 35 L80 35 L100 60 L115 65 L115 90 L100 90 Q90 115 75 90 L45 90 Q30 115 15 90 L5 90 Z",
"M60 5 Q90 30 80 75 L105 105 L75 95 L60 115 L45 95 L15 105 L40 75 Q30 30 60 5 Z M50 45 Q60 25 70 45 Q60 65 50 45 Z",
"M60 55 Q15 0 5 40 Q0 65 35 70 Q0 105 30 115 Q50 115 60 80 Q70 115 90 115 Q120 105 85 70 Q120 65 115 40 Q105 0 60 55 Z",
"M30 30 Q5 5 25 5 Q40 5 45 20 Q60 10 75 20 Q80 5 95 5 Q115 5 90 30 Q120 65 90 95 Q60 125 30 95 Q0 65 30 30 Z",
"M20 110 L25 65 L10 45 L35 35 L45 10 L55 35 L70 5 L75 40 Q115 55 105 85 L80 90 L75 115 Z",
"M5 45 L60 5 L115 45 L105 45 L105 115 L15 115 L15 45 Z M45 60 Q60 40 75 60 Q60 80 45 60 Z",
"M10 80 Q20 50 50 65 Q60 20 85 30 L115 45 L90 55 Q90 100 50 100 L40 115 L30 115 L35 100 L10 100 Z",
"M10 115 L10 40 L60 5 L110 40 L110 115 Z M45 115 L45 75 L75 75 L75 115",
"M5 30 L115 30 L115 85 L70 85 L70 115 L50 115 L50 85 L5 85 Z",
"M60 5 Q110 40 95 80 Q80 110 65 105 L65 118 L55 118 L55 105 Q15 110 20 65 Q25 30 60 5 Z",
"M10 80 Q0 35 35 35 Q55 5 80 35 Q120 35 110 80 L95 105 L25 105 Z",
"M60 5 Q80 30 70 45 Q105 20 115 60 Q100 85 75 75 Q100 110 60 115 Q20 110 45 75 Q5 85 5 60 Q15 20 50 45 Q40 30 60 5 Z",
"M60 5 L115 60 L60 115 L5 60 Z M60 30 L90 60 L60 90 L30 60 Z",
"M60 5 Q115 5 115 60 Q115 115 60 115 Q5 115 5 60 Q5 5 60 5 Z M60 25 Q95 25 95 60 Q95 95 60 95 Q25 95 25 60 Q25 25 60 25 Z",
"M60 110 Q0 70 10 35 Q20 5 60 40 Q100 5 110 35 Q120 70 60 110 Z",
"M10 90 Q65 0 110 10 Q115 70 30 105 L10 115 Z M25 100 L90 30",
"M5 85 L30 35 L60 65 L90 35 L115 85 L115 105 L5 105 Z",
"M10 110 L10 100 L55 10 L70 10 L45 75 L85 75 L85 90 L115 90 L115 110 Z",
"M10 20 L110 20 L110 100 L10 100 Z M25 35 L95 35 L95 85 L25 85 Z",
"M10 10 L110 10 L110 110 L10 110 Z M25 25 L50 25 L50 50 L25 50 Z M70 25 L95 25 L95 50 L70 50 Z M25 70 L95 70 L95 95 L25 95 Z",
"M10 30 L110 30 L110 90 L10 90 Z M25 60 Q35 40 45 60 Q35 80 25 60 Z M75 60 Q85 40 95 60 Q85 80 75 60 Z",
"M10 110 L10 10 L110 10 L110 110 L85 110 L85 40 L35 40 L35 110 Z",
"M5 30 L115 30 L115 90 L5 90 Z M20 45 L100 45 L100 75 L20 75 Z",
"M5 115 L45 5 L75 5 L115 115 L90 115 L80 85 L40 85 L30 115 Z M48 65 L72 65 L60 30 Z",
"M20 5 L65 5 Q110 5 105 35 Q105 50 85 55 Q115 65 110 90 Q110 115 65 115 L20 115 Z M45 25 L65 25 Q85 25 85 40 Q85 50 65 50 L45 50 Z M45 70 L65 70 Q90 70 90 85 Q90 95 65 95 L45 95 Z",
"M110 20 L95 40 Q60 5 35 40 Q10 85 60 95 Q80 95 95 80 L110 100 Q75 130 35 110 Q0 90 10 45 Q25 0 70 5 Q95 5 110 20 Z",
"M45 25 L20 40 L20 15 L50 5 L75 5 L75 95 L100 95 L100 115 L20 115 L20 95 L45 95 Z",
"M10 30 Q20 0 65 5 Q115 5 110 45 Q105 65 45 95 L110 95 L110 115 L10 115 L10 95 Q85 45 85 35 Q80 15 25 45 Z",
"M10 20 Q60 -5 95 15 Q120 40 85 55 Q120 65 105 95 Q80 130 10 105 L20 85 Q80 110 85 80 Q85 65 45 65 L45 45 Q90 45 80 30 Q65 15 20 40 Z")
private val names=mapOf("en" to "Holidays|Children|Home and garden|Decor|Useful items|Letters and numbers|Gingerbread|Tree|Snowman|Reindeer|Star|Rabbit|Dinosaur|Car|Rocket|Butterfly|Bear|Unicorn|Birdhouse front|Bird|House silhouette|Garden sign|Leaf|Cloud|Flower|Diamond|Ring|Heart|Feather|Crown|Phone stand profile|Frame|Organizer panel|Handle plate|Stand profile|Label frame|A|B|C|1|2|3".split('|'),
"ru" to "Праздники|Для детей|Дом и сад|Декор|Полезные вещи|Буквы и цифры|Пряник|Ёлка|Снеговик|Олень|Звезда|Кролик|Динозавр|Машина|Ракета|Бабочка|Медведь|Единорог|Фасад скворечника|Птица|Домик|Садовая табличка|Лист|Облако|Цветок|Ромб|Кольцо|Сердце|Перо|Корона|Профиль подставки телефона|Рамка|Панель органайзера|Панель с ручками|Профиль подставки|Рамка таблички|A|B|C|1|2|3".split('|'),
"fi" to "Juhlat|Lapset|Koti ja puutarha|Koristeet|Hyödylliset esineet|Kirjaimet ja numerot|Piparkakku|Kuusi|Lumiukko|Poro|Tähti|Kani|Dinosaurus|Auto|Raketti|Perhonen|Karhu|Yksisarvinen|Linnunpöntön etuosa|Lintu|Talo|Puutarhakyltti|Lehti|Pilvi|Kukka|Vinoneliö|Rengas|Sydän|Sulka|Kruunu|Puhelintelineen sivu|Kehys|Lokerikkolevy|Kahvalevy|Telineen sivu|Nimikyltti|A|B|C|1|2|3".split('|'),
"et" to "Pühad|Lapsed|Kodu ja aed|Kaunistused|Tarbeesemed|Tähed ja numbrid|Piparkook|Kuusk|Lumememm|Põhjapõder|Täht|Jänes|Dinosaurus|Auto|Rakett|Liblikas|Karu|Ükssarvik|Linnumaja esikülg|Lind|Maja|Aiasilt|Leht|Pilv|Lill|Romb|Rõngas|Süda|Sulg|Kroon|Telefonihoidiku külg|Raam|Sahtlipaneel|Käepidemeplaat|Aluse külg|Sildiraam|A|B|C|1|2|3".split('|'),
"sv" to "Högtider|Barn|Hem och trädgård|Dekor|Nyttiga saker|Bokstäver och siffror|Pepparkaka|Gran|Snögubbe|Ren|Stjärna|Kanin|Dinosaurie|Bil|Raket|Fjäril|Björn|Enhörning|Fågelholkens front|Fågel|Hus|Trädgårdsskylt|Löv|Moln|Blomma|Romb|Ring|Hjärta|Fjäder|Krona|Mobilställsprofil|Ram|Organisatörspanel|Handtagsplatta|Ställprofil|Skyltram|A|B|C|1|2|3".split('|'),
"de" to "Feste|Kinder|Haus und Garten|Dekor|Nützliche Dinge|Buchstaben und Zahlen|Lebkuchen|Tannenbaum|Schneemann|Rentier|Stern|Hase|Dinosaurier|Auto|Rakete|Schmetterling|Bär|Einhorn|Vogelhausfront|Vogel|Haus|Gartenschild|Blatt|Wolke|Blume|Raute|Ring|Herz|Feder|Krone|Handyhalterprofil|Rahmen|Organizerplatte|Griffplatte|Ständerprofil|Schildrahmen|A|B|C|1|2|3".split('|'),
"fr" to "Fêtes|Enfants|Maison et jardin|Décoration|Objets utiles|Lettres et chiffres|Pain d’épice|Sapin|Bonhomme de neige|Renne|Étoile|Lapin|Dinosaure|Voiture|Fusée|Papillon|Ours|Licorne|Façade de nichoir|Oiseau|Maison|Panneau de jardin|Feuille|Nuage|Fleur|Losange|Anneau|Cœur|Plume|Couronne|Profil de support téléphone|Cadre|Panneau de rangement|Plaque à poignées|Profil de support|Cadre de plaque|A|B|C|1|2|3".split('|'),
"pl" to "Święta|Dzieci|Dom i ogród|Dekoracje|Przydatne rzeczy|Litery i cyfry|Piernik|Choinka|Bałwan|Renifer|Gwiazda|Królik|Dinozaur|Samochód|Rakieta|Motyl|Niedźwiedź|Jednorożec|Front budki|Ptak|Dom|Tabliczka ogrodowa|Liść|Chmura|Kwiat|Romb|Pierścień|Serce|Pióro|Korona|Profil stojaka telefonu|Ramka|Panel organizera|Płyta z uchwytami|Profil stojaka|Ramka tabliczki|A|B|C|1|2|3".split('|'),
"es" to "Fiestas|Niños|Casa y jardín|Decoración|Objetos útiles|Letras y números|Galleta de jengibre|Abeto|Muñeco de nieve|Reno|Estrella|Conejo|Dinosaurio|Coche|Cohete|Mariposa|Oso|Unicornio|Frente de pajarera|Pájaro|Casa|Cartel de jardín|Hoja|Nube|Flor|Rombo|Anillo|Corazón|Pluma|Corona|Perfil de soporte móvil|Marco|Panel organizador|Placa con asas|Perfil de soporte|Marco de cartel|A|B|C|1|2|3".split('|'),
"it" to "Feste|Bambini|Casa e giardino|Decorazioni|Oggetti utili|Lettere e numeri|Omino di pan di zenzero|Abete|Pupazzo di neve|Renna|Stella|Coniglio|Dinosauro|Auto|Razzo|Farfalla|Orso|Unicorno|Fronte casetta uccelli|Uccello|Casa|Cartello da giardino|Foglia|Nuvola|Fiore|Rombo|Anello|Cuore|Piuma|Corona|Profilo supporto telefono|Cornice|Pannello organizer|Piastra con maniglie|Profilo supporto|Cornice targhetta|A|B|C|1|2|3".split('|'),
"lv" to "Svētki|Bērni|Māja un dārzs|Dekori|Noderīgas lietas|Burti un cipari|Piparkūka|Egle|Sniegavīrs|Ziemeļbriedis|Zvaigzne|Trusis|Dinozaurs|Auto|Raķete|Tauriņš|Lācis|Vienradzis|Putnu būra priekšpuse|Putns|Māja|Dārza zīme|Lapa|Mākonis|Zieds|Rombs|Gredzens|Sirds|Spalva|Kronis|Tālruņa statīva profils|Rāmis|Organizatora panelis|Rokturu plāksne|Statīva profils|Zīmes rāmis|A|B|C|1|2|3".split('|'),
"lt" to "Šventės|Vaikai|Namai ir sodas|Dekoras|Naudingi daiktai|Raidės ir skaičiai|Meduolis|Eglė|Sniego senis|Šiaurės elnias|Žvaigždė|Triušis|Dinozauras|Automobilis|Raketa|Drugelis|Meška|Vienaragis|Inkilo priekis|Paukštis|Namas|Sodo lentelė|Lapas|Debesis|Gėlė|Rombas|Žiedas|Širdis|Plunksna|Karūna|Telefono stovo profilis|Rėmas|Dėklo skydelis|Rankenų plokštė|Stovo profilis|Lentelės rėmas|A|B|C|1|2|3".split('|'),
"nb" to "Høytider|Barn|Hjem og hage|Dekor|Nyttige ting|Bokstaver og tall|Pepperkake|Gran|Snømann|Reinsdyr|Stjerne|Kanin|Dinosaur|Bil|Rakett|Sommerfugl|Bjørn|Enhjørning|Fuglekassefront|Fugl|Hus|Hageskilt|Blad|Sky|Blomst|Rombe|Ring|Hjerte|Fjær|Krone|Mobilstativprofil|Ramme|Organiseringsplate|Håndtaksplate|Stativprofil|Skiltramme|A|B|C|1|2|3".split('|'),
"da" to "Højtider|Børn|Hjem og have|Dekoration|Nyttige ting|Bogstaver og tal|Peberkage|Gran|Snemand|Rensdyr|Stjerne|Kanin|Dinosaur|Bil|Raket|Sommerfugl|Bjørn|Enhjørning|Fuglekassefront|Fugl|Hus|Haveskilt|Blad|Sky|Blomst|Rombe|Ring|Hjerte|Fjer|Krone|Mobilholderprofil|Ramme|Organiseringsplade|Håndtagsplade|Stativprofil|Skilteramme|A|B|C|1|2|3".split('|'))
internal fun ideaName(lang:String,id:Int)=(names[lang]?:names.getValue("en"))[id+6]
internal fun ideaBitmap(id:Int,size:Int=960):Bitmap {
 val path=PathParser.createPathFromPathData(paths[id])
 val bounds=android.graphics.RectF()
 path.computeBounds(bounds,true)
 val b=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888)
 val c=Canvas(b)
 c.drawColor(android.graphics.Color.WHITE)
 val scale=(size-8f)/maxOf(bounds.width(),bounds.height())
 c.translate((size-bounds.width()*scale)/2-bounds.left*scale,(size-bounds.height()*scale)/2-bounds.top*scale)
 c.scale(scale,scale)
 c.drawPath(path,Paint().apply{color=android.graphics.Color.BLACK;style=Paint.Style.STROKE;strokeWidth=1f/scale;strokeJoin=Paint.Join.ROUND;strokeCap=Paint.Cap.ROUND})
 return b
}
@Composable
fun IdeasCatalog(lang:String,onCreate:(Int)->Unit){
 val t=names[lang]?:names.getValue("en")
 for(category in 0..5){
 Text(t[category],style=MaterialTheme.typography.titleLarge)
 for(row in 0..2){
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
 for(col in 0..1){
 val id=category*6+row*2+col
 val preview=remember(id){ideaBitmap(id,240).asImageBitmap()}
 Card(onClick={onCreate(id)},modifier=Modifier.weight(1f)){
 Column(Modifier.padding(8.dp)){
 Image(preview,t[id+6],Modifier.fillMaxWidth().aspectRatio(1f))
 Text(t[id+6],style=MaterialTheme.typography.bodyMedium)
 }
 }
 }
 }
 }
 }
}
