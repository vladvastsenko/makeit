package fi.vladi.makeit
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val prefs = getSharedPreferences("makeit", MODE_PRIVATE)
  setContent {
   var lang by remember { mutableStateOf(prefs.getString("language", "en") ?: "en") }
   var guest by remember { mutableStateOf(prefs.getBoolean("guest", false)) }
   var page by remember { mutableIntStateOf(0) }
   val labels = translations[lang] ?: translations.getValue("en")
   MaterialTheme(colorScheme = lightColorScheme(primary=Color(0xFF243D35), secondary=Color(0xFFB45D30), background=Color(0xFFF7F3EB))) {
    Scaffold(bottomBar = { if(guest) NavigationBar { labels.take(5).forEachIndexed { i, s -> NavigationBarItem(selected=page==i,onClick={page=i},icon={Text(listOf("⌂","☆","+","▣","⚙")[i],fontSize=23.sp)},label={Text(s,fontSize=10.sp)}) } } }) { pad ->
     Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(pad).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(16.dp)) {
      Text("MakeIt.", style=MaterialTheme.typography.displayMedium,color=MaterialTheme.colorScheme.primary)
      if(!guest || page==4) {
       Text(labels[5],style=MaterialTheme.typography.titleLarge)
       languages.forEach { (id,name) -> OutlinedButton(onClick={lang=id;prefs.edit().putString("language",id).apply()},modifier=Modifier.fillMaxWidth()){Text(if(lang==id) "✓ $name" else name)} }
       if(!guest) Button(onClick={guest=true;prefs.edit().putBoolean("guest",true).apply()},modifier=Modifier.fillMaxWidth()){Text(labels[6])}
      } else if(page==0) {
       Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF243D35)),shape=RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) { Text("✦",color=Color(0xFFF5AD69),fontSize=48.sp); Text(labels[7],color=Color.White,style=MaterialTheme.typography.headlineMedium) }
       }
       listOf(2,1,3).forEachIndexed { j,i -> Card(onClick={page=i}, colors=CardDefaults.cardColors(containerColor=listOf(Color(0xFFFFD9B8),Color(0xFFD8E7DC),Color(0xFFDCE4F4))[j]),shape=RoundedCornerShape(20.dp)){Text(labels[i],Modifier.fillMaxWidth().padding(28.dp),style=MaterialTheme.typography.headlineSmall)} }
      } else { Text(labels[page],style=MaterialTheme.typography.headlineMedium); Text(labels[8]) }
     }
    }
   }
  }
 }
}
private val languages = linkedMapOf("en" to "English","fi" to "Suomi","et" to "Eesti","ru" to "Русский","sv" to "Svenska","de" to "Deutsch","fr" to "Français","pl" to "Polski","es" to "Español","it" to "Italiano","uk" to "Українська","lv" to "Latviešu")
private val translations = mapOf(
"en" to listOf("Home","Ideas","Create","Projects","Settings","Language","Continue as guest","From idea to reality","Coming soon"),
"fi" to listOf("Etusivu","Ideat","Luo","Projektit","Asetukset","Kieli","Jatka vieraana","Ideasta todeksi","Tulossa pian"),
"et" to listOf("Avaleht","Ideed","Loo","Projektid","Seaded","Keel","Jätka külalisena","Ideest tegelikkuseks","Varsti tulemas"),
"ru" to listOf("Главная","Идеи","Создать","Проекты","Настройки","Язык","Продолжить как гость","От идеи к изделию","Скоро появится"),
"sv" to listOf("Hem","Idéer","Skapa","Projekt","Inställningar","Språk","Fortsätt som gäst","Från idé till verklighet","Kommer snart"),
"de" to listOf("Start","Ideen","Erstellen","Projekte","Einstellungen","Sprache","Als Gast fortfahren","Von der Idee zur Realität","Demnächst"),
"fr" to listOf("Accueil","Idées","Créer","Projets","Paramètres","Langue","Continuer en invité","De l’idée à la réalité","Bientôt disponible"),
"pl" to listOf("Start","Pomysły","Utwórz","Projekty","Ustawienia","Język","Kontynuuj jako gość","Od pomysłu do realizacji","Wkrótce"),
"es" to listOf("Inicio","Ideas","Crear","Proyectos","Ajustes","Idioma","Continuar como invitado","De la idea a la realidad","Próximamente"),
"it" to listOf("Home","Idee","Crea","Progetti","Impostazioni","Lingua","Continua come ospite","Dall’idea alla realtà","Prossimamente"),
"uk" to listOf("Головна","Ідеї","Створити","Проєкти","Налаштування","Мова","Продовжити як гість","Від ідеї до виробу","Незабаром"),
"lv" to listOf("Sākums","Idejas","Izveidot","Projekti","Iestatījumi","Valoda","Turpināt kā viesim","No idejas līdz īstenošanai","Drīzumā"))
