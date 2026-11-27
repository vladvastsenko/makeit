package fi.vladi.makeit
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
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
  val ads=MakeitAds(this)
  setContent {
   var lang by remember { mutableStateOf((prefs.getString("language", "en") ?: "en").takeIf { it in languages } ?: "en") }
   var guest by remember { mutableStateOf(prefs.getBoolean("guest", false)) }
   var page by remember { mutableIntStateOf(0) }
   var slotId by remember { mutableIntStateOf(-1) }
   var creativeId by remember { mutableStateOf<String?>(null) }
   var createMode by remember { mutableIntStateOf(-1) }
   var selectedIdea by remember { mutableStateOf<Int?>(null) }
   var selectedProject by remember { mutableStateOf<String?>(null) }
   LaunchedEffect(guest){if(guest)ads.start()}
   val labels = translations[lang] ?: translations.getValue("en")
   MaterialTheme(colorScheme = lightColorScheme(primary=Color(0xFF243D35), secondary=Color(0xFFB45D30), background=Color(0xFFF7F3EB), surface=Color(0xFFFFFCF6), surfaceVariant=Color(0xFFE5EAE1), tertiary=Color(0xFFB45D30))) {
    Scaffold(bottomBar = { if(guest) Column { if(ads.ready && page in listOf(0,1,3)) MakeitBanner(); NavigationBar(containerColor=Color(0xFFFFFCF6)) { labels.take(5).forEachIndexed { i, s -> NavigationBarItem(selected=page==i,onClick={selectedIdea=null;selectedProject=null;creativeId=null;slotId=-1;createMode=-1;page=i},icon={Text(listOf("⌂","☆","+","▣","⚙")[i],fontSize=23.sp)},label={Text(s,fontSize=10.sp)}) } } } }) { pad ->
     if(!guest){
      Box(Modifier.fillMaxSize().padding(pad)){
       Image(painterResource(R.drawable.welcome_background),contentDescription=null,modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
       Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("makeit",style=MaterialTheme.typography.displayLarge,color=Color(0xFF243D35))
        Text(labels[7],style=MaterialTheme.typography.titleMedium,color=Color(0xFF243D35))
        Card(shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFFCF6))){
         Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
          Text(labels[5],style=MaterialTheme.typography.titleLarge)
          var expanded by remember{mutableStateOf(false)}
          Box(Modifier.fillMaxWidth()){
           OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text(languages.getValue(lang)+"  ▾")}
           DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){
            languages.forEach{(id,name)->DropdownMenuItem(text={Text(name)},onClick={lang=id;prefs.edit().putString("language",id).apply();expanded=false})}
           }
          }
          Button(onClick={guest=true;prefs.edit().putBoolean("guest",true).apply()},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFB45D30))){Text(backupLabels.getValue(lang)[4])}
         }
        }
       }
      }
     } else Box(Modifier.fillMaxSize().padding(pad)){
      Image(painterResource(R.drawable.makeit_background),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
      Box(Modifier.fillMaxSize().background(Color(0xFFF7F3EB).copy(alpha=0.66f)))
      Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(16.dp)) {
      Text("makeit", style=MaterialTheme.typography.displayMedium,color=MaterialTheme.colorScheme.primary)
      if(!guest || page==4) {
       Text(labels[5],style=MaterialTheme.typography.titleLarge)
       var expanded by remember { mutableStateOf(false) }
       Box(Modifier.fillMaxWidth()){
        OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text(languages.getValue(lang)+"  ▾")}
        DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){
         languages.forEach{(id,name)->DropdownMenuItem(text={Text(name)},onClick={lang=id;prefs.edit().putString("language",id).apply();expanded=false})}
        }
       }
       if(guest) {
        if(ads.privacyRequired) OutlinedButton(onClick={ads.privacy()},modifier=Modifier.fillMaxWidth()){Text(privacyLabels.getValue(lang))}
        val guide=manualLabels.getValue(lang)
        var showManual by remember{mutableStateOf(false)}
        Button(onClick={showManual=true},modifier=Modifier.fillMaxWidth()){Text(guide[0])}
        if(showManual)AlertDialog(onDismissRequest={showManual=false},title={Text(guide[0])},text={Column(Modifier.verticalScroll(rememberScrollState())){Text(guide[1])}},confirmButton={TextButton(onClick={showManual=false}){Text("OK")}})
       }
       if(!guest) Button(onClick={guest=true;prefs.edit().putBoolean("guest",true).apply()},modifier=Modifier.fillMaxWidth()){Text(backupLabels.getValue(lang)[4])}
      } else if(page==0) {
       Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF243D35)),shape=RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) { Text("✦",color=Color(0xFFF5AD69),fontSize=48.sp); Text(labels[7],color=Color.White,style=MaterialTheme.typography.headlineMedium) }
       }
       listOf(2,1,3).forEachIndexed { j,i -> Card(onClick={createMode=-1;page=i}, colors=CardDefaults.cardColors(containerColor=listOf(Color(0xFFFFD9B8),Color(0xFFD8E7DC),Color(0xFFDCE4F4))[j]),shape=RoundedCornerShape(20.dp)){Text(labels[i],Modifier.fillMaxWidth().padding(28.dp),style=MaterialTheme.typography.headlineSmall)} }
      } else if(page==1) { IdeasCatalog(lang) { id -> selectedProject=null;selectedIdea=id;createMode=-1;page=2 }; SlotIdeas(lang){id->slotId=id;selectedIdea=null;selectedProject=null;creativeId=null;createMode=3;page=2} } else if(page==2 && createMode==3 && slotId in 0..4) { OutlinedButton(onClick={val own=slotId==4;val saved=creativeId!=null;slotId=-1;creativeId=null;createMode=-1;page=if(saved)3 else if(own)2 else 1}){Text("‹ "+labels[if(creativeId!=null)3 else if(slotId==4)2 else 1])}; val slotContext=LocalContext.current; val slotProject=remember(creativeId){creativeId?.let{runCatching{creativeSettings(java.io.File(creativeRoot(slotContext),it))}.getOrNull()}}; key(creativeId,slotId){SlotEditor(lang,slotId,slotProject,creativeId)} } else if(page==2 && createMode==-1) { if(selectedIdea!=null) OutlinedButton(onClick={selectedIdea=null;page=1}){Text("‹ "+labels[1])}; CreateModeMenu(lang,labels[8],if(selectedIdea==null)slotCreateLabel(lang) else null) { mode->if(mode==3){slotId=4;creativeId=null};createMode=mode } } else if(page==2 && createMode in 1..2) { OutlinedButton(onClick={createMode=-1;creativeId=null}){Text("‹ "+labels[2])}; NumberPaintingEditor(lang,createMode==2,selectedIdea,creativeId) } else if(page==2 || page==3) { if(page==3){ val context=LocalContext.current; var revision by remember{mutableIntStateOf(0)}; val folders=remember(revision){creativeList(context)}; for(folder in folders){val j=remember(folder){runCatching{creativeSettings(folder)}.getOrNull()};if(j!=null) Card(Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){TextButton(onClick={selectedIdea=null;selectedProject=null;creativeId=folder.name;if(j.has("slotId")){slotId=j.optInt("slotId");createMode=3}else{createMode=if(j.optBoolean("fullColor"))2 else 1};page=2},modifier=Modifier.weight(1f)){Text(j.optString("name"))};TextButton(onClick={folder.deleteRecursively();revision++}){Text("×")}}}} }; if(page==2) OutlinedButton(onClick={createMode=-1;selectedProject=null}){Text("‹ "+labels[2])}; key(page,selectedIdea){PatternEditor(lang, page==3, selectedProject, { id -> selectedIdea=null;selectedProject=id;createMode=0;page=2 },selectedIdea)} } else { Text(labels[page],style=MaterialTheme.typography.headlineMedium); Text(labels[8]) }
     }
     }
    }
   }
  }
 }
}
private val languages = linkedMapOf("en" to "🇬🇧 English","fi" to "🇫🇮 Suomi","et" to "🇪🇪 Eesti","ru" to "🇷🇺 Русский","sv" to "🇸🇪 Svenska","de" to "🇩🇪 Deutsch","fr" to "🇫🇷 Français","pl" to "🇵🇱 Polski","es" to "🇪🇸 Español","it" to "🇮🇹 Italiano","lv" to "🇱🇻 Latviešu","lt" to "🇱🇹 Lietuvių","nb" to "🇳🇴 Norsk bokmål","da" to "🇩🇰 Dansk")
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

private val manualLabels=mapOf(
"en" to listOf("Instructions","Choose an image. Enable background removal and adjust the object bounds. Select cut contours or detailed contours. Check holes and correct lines manually. Set dimensions in cm, choose A4 or A3, save the project and export PDF. Print at 100% without scaling and check the calibration square. Projects are stored on this device. In Create, choose cutting, paint by numbers or color printing. For painting, select colors, detail and minimum region, then generate. For color printing, optionally remove the background. Set dimensions, save and export."),
"ru" to listOf("Инструкция","Выберите изображение. Включите удаление фона и настройте границы объекта. Выберите контуры резки или контуры с деталями. Проверьте отверстия и исправьте линии вручную. Задайте размеры в см, выберите A4 или A3, сохраните проект и экспортируйте PDF. Печатайте в масштабе 100% без подгонки и проверьте контрольный квадрат. Проекты хранятся на устройстве. В разделе «Создать» выберите выкройку, раскраску по номерам или цветную печать. Для раскраски задайте цвета, детализацию и минимальный фрагмент, затем создайте рисунок. Для цветной печати можно удалить фон. Задайте размеры, сохраните проект и экспортируйте PDF."),
"fi" to listOf("Ohjeet","Valitse kuva. Poista tausta ja säädä kohteen rajat. Valitse leikkausviivat tai yksityiskohdat. Tarkista aukot ja korjaa viivat käsin. Aseta mitat senttimetreinä, valitse A4 tai A3, tallenna projekti ja vie PDF. Tulosta 100 % koossa ja tarkista mittaneliö. Projektit tallennetaan laitteelle. Valitse Luo-osiossa leikkausmalli, numeromaalaus tai väritulostus. Valitse maalauksen värit, yksityiskohdat ja pienin alue. Väritulostuksessa voit poistaa taustan. Aseta mitat, tallenna ja vie PDF."),
"et" to listOf("Juhend","Vali pilt. Eemalda taust ja määra objekti piirid. Vali lõikekontuur või detailid. Kontrolli avasid ja paranda jooni käsitsi. Määra mõõdud sentimeetrites, vali A4 või A3, salvesta projekt ja ekspordi PDF. Prindi 100% mõõtkavas ja kontrolli mõõteruutu. Projektid jäävad seadmesse. Vali lõikemall, numbrite järgi värvimine või värviprint. Määra värvid, detailsus ja väikseim ala. Värviprindil saab tausta eemaldada. Määra mõõdud, salvesta ja ekspordi PDF."),
"sv" to listOf("Instruktioner","Välj en bild. Ta bort bakgrunden och justera objektets gränser. Välj skärkonturer eller detaljer. Kontrollera hålen och korrigera linjer manuellt. Ange mått i cm, välj A4 eller A3, spara projektet och exportera PDF. Skriv ut i 100 % och kontrollera kalibreringsrutan. Projekt sparas på enheten. Välj skärmall, måla efter nummer eller färgutskrift. Ange färger, detaljer och minsta område. Vid färgutskrift kan bakgrunden tas bort. Ange mått, spara och exportera PDF."),
"de" to listOf("Anleitung","Bild auswählen, Hintergrund entfernen und Objektgrenzen einstellen. Schnittkonturen oder Details wählen. Löcher prüfen und Linien manuell korrigieren. Maße in cm eingeben, A4 oder A3 wählen, Projekt speichern und PDF exportieren. Bei 100 % drucken und Kontrollquadrat prüfen. Projekte bleiben auf dem Gerät. Wähle Schneidevorlage, Malen nach Zahlen oder Farbdruck. Stelle Farben, Details und Mindestfläche ein. Beim Farbdruck kann der Hintergrund entfernt werden. Maße einstellen, speichern und PDF exportieren."),
"fr" to listOf("Instructions","Choisissez une image, retirez le fond et ajustez les limites. Sélectionnez les contours de découpe ou les détails. Vérifiez les trous et corrigez les lignes manuellement. Indiquez les dimensions en cm, choisissez A4 ou A3, enregistrez et exportez le PDF. Imprimez à 100 % et vérifiez le carré de contrôle. Les projets restent sur l’appareil. Choisissez découpe, peinture par numéros ou impression couleur. Réglez les couleurs, les détails et la zone minimale. Le fond peut être supprimé pour l’impression couleur. Définissez les dimensions, enregistrez et exportez le PDF."),
"pl" to listOf("Instrukcja","Wybierz obraz, usuń tło i ustaw granice obiektu. Wybierz kontury cięcia lub szczegóły. Sprawdź otwory i popraw linie ręcznie. Ustaw wymiary w cm, wybierz A4 lub A3, zapisz projekt i eksportuj PDF. Drukuj w skali 100% i sprawdź kwadrat kontrolny. Projekty pozostają na urządzeniu. Wybierz szablon, malowanie po numerach lub druk kolorowy. Ustaw kolory, szczegóły i minimalny obszar. W druku kolorowym można usunąć tło. Ustaw wymiary, zapisz i eksportuj PDF."),
"es" to listOf("Instrucciones","Elige una imagen, elimina el fondo y ajusta los límites. Selecciona contornos de corte o detalles. Comprueba los agujeros y corrige las líneas manualmente. Define medidas en cm, elige A4 o A3, guarda el proyecto y exporta PDF. Imprime al 100 % y comprueba el cuadrado de calibración. Los proyectos quedan en el dispositivo. Elige plantilla, pintura por números o impresión en color. Ajusta colores, detalle y área mínima. Puedes eliminar el fondo para imprimir en color. Define medidas, guarda y exporta PDF."),
"it" to listOf("Istruzioni","Scegli un’immagine, rimuovi lo sfondo e regola i limiti. Seleziona contorni di taglio o dettagli. Controlla i fori e correggi le linee a mano. Imposta le misure in cm, scegli A4 o A3, salva il progetto ed esporta PDF. Stampa al 100% e verifica il quadrato di controllo. I progetti restano sul dispositivo. Scegli sagoma, pittura per numeri o stampa a colori. Imposta colori, dettagli e area minima. Per la stampa puoi rimuovere lo sfondo. Imposta misure, salva ed esporta PDF."),
"lv" to listOf("Instrukcija","Izvēlies attēlu, noņem fonu un pielāgo objekta robežas. Izvēlies griešanas kontūras vai detaļas. Pārbaudi atveres un labo līnijas manuāli. Norādi izmērus cm, izvēlies A4 vai A3, saglabā projektu un eksportē PDF. Drukā 100% mērogā un pārbaudi kontrolkvadrātu. Projekti paliek ierīcē. Izvēlies veidni, krāsošanu pēc numuriem vai krāsu druku. Norādi krāsas, detalizāciju un minimālo laukumu. Krāsu drukai var noņemt fonu. Norādi izmērus, saglabā un eksportē PDF."),
"lt" to listOf("Instrukcija","Pasirinkite vaizdą, pašalinkite foną ir nustatykite objekto ribas. Pasirinkite pjovimo kontūrus arba detales. Patikrinkite angas ir pataisykite linijas rankiniu būdu. Nurodykite matmenis cm, pasirinkite A4 arba A3, išsaugokite projektą ir eksportuokite PDF. Spausdinkite 100 % masteliu ir patikrinkite kontrolinį kvadratą. Projektai lieka įrenginyje. Pasirinkite šabloną, spalvinimą pagal skaičius arba spalvotą spausdinimą. Nustatykite spalvas, detalumą ir mažiausią sritį. Spausdinant galima pašalinti foną. Nustatykite matmenis, išsaugokite ir eksportuokite PDF."),
"nb" to listOf("Bruksanvisning","Velg et bilde, fjern bakgrunnen og juster objektgrensene. Velg skjærekonturer eller detaljer. Kontroller hull og korriger linjer manuelt. Angi mål i cm, velg A4 eller A3, lagre prosjektet og eksporter PDF. Skriv ut i 100 % og kontroller kalibreringsruten. Prosjekter lagres på enheten. Velg skjæremal, maling etter tall eller fargeutskrift. Velg farger, detaljer og minste område. Ved fargeutskrift kan bakgrunnen fjernes. Angi mål, lagre og eksporter PDF."),
"da" to listOf("Vejledning","Vælg et billede, fjern baggrunden og juster objektets grænser. Vælg skærekonturer eller detaljer. Kontrollér huller og ret linjer manuelt. Angiv mål i cm, vælg A4 eller A3, gem projektet og eksportér PDF. Udskriv i 100 % og kontrollér kalibreringsfeltet. Projekter gemmes på enheden. Vælg skæreskabelon, mal efter tal eller farveudskrift. Vælg farver, detaljer og mindste område. Ved farveudskrift kan baggrunden fjernes. Angiv mål, gem og eksportér PDF."))

private fun slotCreateLabel(lang:String)=mapOf("en" to "3D figure","fi" to "3D-hahmo","et" to "Ruumiline kujund","ru" to "Объёмная фигура","sv" to "3D-figur","de" to "3D-Figur","fr" to "Figure 3D","pl" to "Figura 3D","es" to "Figura 3D","it" to "Figura 3D","lv" to "3D figūra","lt" to "3D figūra","nb" to "3D-figur","da" to "3D-figur").let{it[lang]?:it.getValue("en")}
