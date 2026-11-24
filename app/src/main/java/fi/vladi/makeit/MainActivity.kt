package fi.vladi.makeit
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
   var lang by remember { mutableStateOf((prefs.getString("language", "en") ?: "en").takeIf { it in languages } ?: "en") }
   var guest by remember { mutableStateOf(prefs.getBoolean("guest", false)) }
   var page by remember { mutableIntStateOf(0) }
   val labels = translations[lang] ?: translations.getValue("en")
   MaterialTheme(colorScheme = lightColorScheme(primary=Color(0xFF243D35), secondary=Color(0xFFB45D30), background=Color(0xFFF7F3EB), surface=Color(0xFFFFFCF6), surfaceVariant=Color(0xFFE5EAE1), tertiary=Color(0xFFB45D30))) {
    Scaffold(bottomBar = { if(guest) NavigationBar(containerColor=Color(0xFFFFFCF6)) { labels.take(5).forEachIndexed { i, s -> NavigationBarItem(selected=page==i,onClick={page=i},icon={Text(listOf("⌂","☆","+","▣","⚙")[i],fontSize=23.sp)},label={Text(s,fontSize=10.sp)}) } } }) { pad ->
     Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(pad).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(16.dp)) {
      Text("MakeIt.", style=MaterialTheme.typography.displayMedium,color=MaterialTheme.colorScheme.primary)
      if(!guest || page==4) {
       Text(labels[5],style=MaterialTheme.typography.titleLarge)
       languages.forEach { (id,name) -> OutlinedButton(onClick={lang=id;prefs.edit().putString("language",id).apply()},modifier=Modifier.fillMaxWidth()){Text(if(lang==id) "✓ $name" else name)} }
       if(guest) { AccountSettings(lang); BackupSettings(lang) }
       if(!guest) Button(onClick={guest=true;prefs.edit().putBoolean("guest",true).apply()},modifier=Modifier.fillMaxWidth()){Text(backupLabels.getValue(lang)[4])}
      } else if(page==0) {
       Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF243D35)),shape=RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) { Text("✦",color=Color(0xFFF5AD69),fontSize=48.sp); Text(labels[7],color=Color.White,style=MaterialTheme.typography.headlineMedium) }
       }
       listOf(2,1,3).forEachIndexed { j,i -> Card(onClick={page=i}, colors=CardDefaults.cardColors(containerColor=listOf(Color(0xFFFFD9B8),Color(0xFFD8E7DC),Color(0xFFDCE4F4))[j]),shape=RoundedCornerShape(20.dp)){Text(labels[i],Modifier.fillMaxWidth().padding(28.dp),style=MaterialTheme.typography.headlineSmall)} }
      } else if(page==1) { IdeasCatalog(lang) { page=2 } } else if(page==2 || page==3) { key(page){PatternEditor(lang, page==3)} } else { Text(labels[page],style=MaterialTheme.typography.headlineMedium); Text(labels[8]) }
     }
    }
   }
  }
 }
}
private val languages = linkedMapOf("en" to "English","fi" to "Suomi","et" to "Eesti","ru" to "Русский","sv" to "Svenska","de" to "Deutsch","fr" to "Français","pl" to "Polski","es" to "Español","it" to "Italiano","lv" to "Latviešu","lt" to "Lietuvių","nb" to "Norsk bokmål","da" to "Dansk")
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
"lv" to listOf("Sākums","Idejas","Izveidot","Projekti","Iestatījumi","Valoda","Turpināt kā viesim","No idejas līdz īstenošanai","Drīzumā"),
"lt" to listOf("Pradžia","Idėjos","Kurti","Projektai","Nustatymai","Kalba","Tęsti kaip svečiui","Nuo idėjos iki tikrovės","Netrukus"),
"nb" to listOf("Hjem","Ideer","Opprett","Prosjekter","Innstillinger","Språk","Fortsett som gjest","Fra idé til virkelighet","Kommer snart"),
"da" to listOf("Hjem","Idéer","Opret","Projekter","Indstillinger","Sprog","Fortsæt som gæst","Fra idé til virkelighed","Kommer snart"))

@Composable
private fun BackupSettings(lang:String){
 val t=backupLabels[lang] ?: backupLabels.getValue("en")
 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 var busy by remember{mutableStateOf(false)}
 var status by remember{mutableStateOf("")}
 val save=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")){uri->
 if(uri!=null){busy=true;scope.launch{status=try{withContext(Dispatchers.IO){context.contentResolver.openOutputStream(uri)?.use{ProjectBackup.export(context,it)} ?: error("output")};t[2]}catch(e:Exception){t[3]}finally{busy=false}}}
 }
 val load=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
 if(uri!=null){busy=true;scope.launch{status=try{withContext(Dispatchers.IO){context.contentResolver.openInputStream(uri)?.use{ProjectBackup.restore(context,it)} ?: error("input")};t[2]}catch(e:Exception){t[3]}finally{busy=false}}}
 }
 Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){
 Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
 Button(enabled=!busy,onClick={save.launch("MakeIt-backup.zip")},modifier=Modifier.fillMaxWidth()){Text(t[0])}
 OutlinedButton(enabled=!busy,onClick={load.launch(arrayOf("application/zip","application/octet-stream"))},modifier=Modifier.fillMaxWidth()){Text(t[1])}
 if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
 if(status.isNotEmpty())Text(status)
 }
 }
}

private val backupLabels=mapOf(
"en" to listOf("Export backup","Import backup (copies)","Completed","Backup operation failed","Start"),
"fi" to listOf("Vie varmuuskopio","Tuo varmuuskopio (kopiot)","Valmis","Varmuuskopiointi epäonnistui","Aloita"),
"et" to listOf("Ekspordi varukoopia","Impordi varukoopia (koopiad)","Valmis","Varundamine ebaõnnestus","Alusta"),
"ru" to listOf("Экспорт резервной копии","Импорт резервной копии (копии)","Готово","Ошибка резервного копирования","Начать"),
"sv" to listOf("Exportera säkerhetskopia","Importera säkerhetskopia (kopior)","Klart","Säkerhetskopiering misslyckades","Börja"),
"de" to listOf("Sicherung exportieren","Sicherung importieren (Kopien)","Fertig","Sicherung fehlgeschlagen","Starten"),
"fr" to listOf("Exporter la sauvegarde","Importer la sauvegarde (copies)","Terminé","Échec de la sauvegarde","Commencer"),
"pl" to listOf("Eksportuj kopię zapasową","Importuj kopię zapasową (kopie)","Gotowe","Błąd kopii zapasowej","Rozpocznij"),
"es" to listOf("Exportar copia de seguridad","Importar copia de seguridad (copias)","Completado","Error de copia de seguridad","Empezar"),
"it" to listOf("Esporta backup","Importa backup (copie)","Completato","Errore di backup","Inizia"),
"lv" to listOf("Eksportēt dublējumu","Importēt dublējumu (kopijas)","Pabeigts","Dublēšanas kļūda","Sākt"),
"lt" to listOf("Eksportuoti atsarginę kopiją","Importuoti atsarginę kopiją (kopijos)","Atlikta","Atsarginės kopijos klaida","Pradėti"),
"nb" to listOf("Eksporter sikkerhetskopi","Importer sikkerhetskopi (kopier)","Ferdig","Sikkerhetskopiering mislyktes","Start"),
"da" to listOf("Eksportér sikkerhedskopi","Importér sikkerhedskopi (kopier)","Færdig","Sikkerhedskopiering mislykkedes","Start"))
