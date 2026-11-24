package fi.vladi.makeit
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun AccountSettings(lang:String){
 val t=accountLabels[lang] ?: accountLabels.getValue("en")
 val context=LocalContext.current
 val auth=remember{runCatching{FirebaseApp.initializeApp(context);FirebaseAuth.getInstance()}.getOrNull()}
 var email by remember{mutableStateOf("")}
 var password by remember{mutableStateOf("")}
 var visible by remember{mutableStateOf(false)}
 var confirmDelete by remember{mutableStateOf(false)}
 val d=deleteLabels[lang] ?: deleteLabels.getValue("en")
 var status by remember{mutableStateOf("")}
 var busy by remember{mutableStateOf(false)}
 var user by remember{mutableStateOf(auth?.currentUser)}
 DisposableEffect(auth){
 val listener=FirebaseAuth.AuthStateListener{user=it.currentUser}
 auth?.addAuthStateListener(listener)
 onDispose{auth?.removeAuthStateListener(listener)}
 }
 val clientId=remember{val id=context.resources.getIdentifier("default_web_client_id","string",context.packageName);if(id==0)"" else context.getString(id)}
 val google=remember(clientId){if(clientId.isBlank())null else GoogleSignIn.getClient(context,GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestIdToken(clientId).build())}
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
 val token=runCatching{GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(com.google.android.gms.common.api.ApiException::class.java).idToken}.getOrNull()
 if(token==null||auth==null){busy=false;status=t[10]}else auth.signInWithCredential(GoogleAuthProvider.getCredential(token,null)).addOnCompleteListener{busy=false;status=if(it.isSuccessful)"" else t[10]}
 }
 Card(Modifier.fillMaxWidth()){
 Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
 Text(t[0],style=MaterialTheme.typography.titleLarge)
 if(auth==null)Text(t[9])
 if(user!=null){
 Text(user?.email ?: "")
 if(user?.isEmailVerified==false)OutlinedButton(enabled=!busy,onClick={user?.sendEmailVerification()?.addOnCompleteListener{status=if(it.isSuccessful)t[8] else t[10]}}){Text(t[8])}
 Button(enabled=!busy,onClick={auth?.signOut();google?.signOut();password="";status=""}){Text(t[7])}
 TextButton(enabled=!busy,onClick={confirmDelete=true}){Text(d[2],color=MaterialTheme.colorScheme.error)}
 }else{
 OutlinedTextField(email,{email=it},label={Text(t[1])},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),modifier=Modifier.fillMaxWidth())
 OutlinedTextField(password,{password=it},label={Text(t[2])},singleLine=true,visualTransformation=if(visible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),trailingIcon={TextButton(onClick={visible=!visible}){Text(if(visible)d[1] else d[0])}},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),modifier=Modifier.fillMaxWidth())
 Button(enabled=auth!=null&&!busy&&email.isNotBlank()&&password.isNotEmpty(),onClick={busy=true;auth?.signInWithEmailAndPassword(email.trim(),password)?.addOnCompleteListener{busy=false;status=if(it.isSuccessful)"" else t[10];if(it.isSuccessful)password=""}},modifier=Modifier.fillMaxWidth()){Text(t[3])}
 OutlinedButton(enabled=auth!=null&&!busy&&email.isNotBlank()&&password.length>=6,onClick={busy=true;auth?.createUserWithEmailAndPassword(email.trim(),password)?.addOnCompleteListener{task->if(task.isSuccessful){password="";task.result.user?.sendEmailVerification()?.addOnCompleteListener{busy=false;status=if(it.isSuccessful)t[8] else t[10]} ?: run{busy=false}}else{busy=false;status=t[10]}}},modifier=Modifier.fillMaxWidth()){Text(t[4])}
 OutlinedButton(enabled=auth!=null&&google!=null&&!busy,onClick={google?.let{busy=true;launcher.launch(it.signInIntent)}},modifier=Modifier.fillMaxWidth()){Text(t[5])}
 TextButton(enabled=auth!=null&&!busy&&email.isNotBlank(),onClick={busy=true;auth?.sendPasswordResetEmail(email.trim())?.addOnCompleteListener{busy=false;status=if(it.isSuccessful)t[8] else t[10]}}){Text(t[6])}
 }
 if(confirmDelete)AlertDialog(onDismissRequest={confirmDelete=false},title={Text(d[2])},text={Text(d[3])},confirmButton={TextButton(onClick={
 confirmDelete=false;busy=true
 val current=auth?.currentUser
 if(current==null){busy=false}else current.delete().addOnCompleteListener{task->
 busy=false
 if(task.isSuccessful){auth?.signOut();google?.signOut();status=d[5]}else status=if(task.exception is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException)d[4] else t[10]
 }
 }){Text(d[2],color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton(onClick={confirmDelete=false}){Text(d[6])}})
 if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
 if(status.isNotBlank())Text(status)
 }
 }
}
private val accountLabels=mapOf(
"en" to listOf("Account (optional)","Email","Password","Sign in","Register","Google sign-in","Reset password","Sign out","Check your email","Account service not configured","Authentication failed"),
"fi" to listOf("Tili (valinnainen)","Sähköposti","Salasana","Kirjaudu","Rekisteröidy","Google-kirjautuminen","Palauta salasana","Kirjaudu ulos","Tarkista sähköposti","Tilipalvelua ei ole määritetty","Tunnistautuminen epäonnistui"),
"et" to listOf("Konto (valikuline)","E-post","Parool","Logi sisse","Registreeru","Google'i sisselogimine","Taasta parool","Logi välja","Kontrolli e-posti","Kontoteenus seadistamata","Autentimine ebaõnnestus"),
"ru" to listOf("Аккаунт (необязательно)","Email","Пароль","Войти","Регистрация","Войти через Google","Сбросить пароль","Выйти","Проверьте почту","Сервис аккаунтов не настроен","Ошибка авторизации"),
"sv" to listOf("Konto (valfritt)","E-post","Lösenord","Logga in","Registrera","Google-inloggning","Återställ lösenord","Logga ut","Kontrollera e-post","Kontotjänsten är inte konfigurerad","Inloggningen misslyckades"),
"de" to listOf("Konto (optional)","E-Mail","Passwort","Anmelden","Registrieren","Google-Anmeldung","Passwort zurücksetzen","Abmelden","E-Mail prüfen","Kontodienst nicht konfiguriert","Anmeldung fehlgeschlagen"),
"fr" to listOf("Compte (facultatif)","E-mail","Mot de passe","Connexion","Inscription","Connexion Google","Réinitialiser le mot de passe","Déconnexion","Vérifiez vos e-mails","Service de compte non configuré","Échec de connexion"),
"pl" to listOf("Konto (opcjonalne)","E-mail","Hasło","Zaloguj","Zarejestruj","Logowanie Google","Resetuj hasło","Wyloguj","Sprawdź pocztę","Usługa konta nieskonfigurowana","Błąd logowania"),
"es" to listOf("Cuenta (opcional)","Correo","Contraseña","Iniciar sesión","Registrarse","Acceso con Google","Restablecer contraseña","Cerrar sesión","Revisa tu correo","Servicio de cuenta sin configurar","Error de autenticación"),
"it" to listOf("Account (facoltativo)","Email","Password","Accedi","Registrati","Accesso Google","Reimposta password","Esci","Controlla la posta","Servizio account non configurato","Accesso non riuscito"),
"lv" to listOf("Konts (neobligāts)","E-pasts","Parole","Pieteikties","Reģistrēties","Google pieteikšanās","Atiestatīt paroli","Izrakstīties","Pārbaudi e-pastu","Konta pakalpojums nav konfigurēts","Autentifikācija neizdevās"),
"lt" to listOf("Paskyra (nebūtina)","El. paštas","Slaptažodis","Prisijungti","Registruotis","Google prisijungimas","Atkurti slaptažodį","Atsijungti","Patikrinkite el. paštą","Paskyros paslauga nesukonfigūruota","Prisijungti nepavyko"),
"nb" to listOf("Konto (valgfritt)","E-post","Passord","Logg inn","Registrer","Google-innlogging","Tilbakestill passord","Logg ut","Sjekk e-post","Kontotjenesten er ikke konfigurert","Innlogging mislyktes"),
"da" to listOf("Konto (valgfri)","E-mail","Adgangskode","Log ind","Registrer","Google-login","Nulstil adgangskode","Log ud","Tjek e-mail","Kontotjenesten er ikke konfigureret","Login mislykkedes"))

private val deleteLabels=mapOf(
"en" to listOf("Show","Hide","Delete account","Delete your account? Local projects will remain.","Sign out and sign in again before deleting.","Account deleted","Cancel"),
"fi" to listOf("Näytä","Piilota","Poista tili","Poistetaanko tili? Paikalliset projektit säilyvät.","Kirjaudu ulos ja uudelleen ennen poistamista.","Tili poistettu","Peruuta"),
"et" to listOf("Näita","Peida","Kustuta konto","Kustutada konto? Kohalikud projektid säilivad.","Logi enne kustutamist uuesti sisse.","Konto kustutatud","Loobu"),
"ru" to listOf("Показать","Скрыть","Удалить аккаунт","Удалить аккаунт? Локальные проекты сохранятся.","Выйдите и войдите снова перед удалением.","Аккаунт удалён","Отмена"),
"sv" to listOf("Visa","Dölj","Ta bort konto","Ta bort kontot? Lokala projekt behålls.","Logga ut och in igen före borttagning.","Kontot borttaget","Avbryt"),
"de" to listOf("Anzeigen","Ausblenden","Konto löschen","Konto löschen? Lokale Projekte bleiben erhalten.","Vor dem Löschen erneut anmelden.","Konto gelöscht","Abbrechen"),
"fr" to listOf("Afficher","Masquer","Supprimer le compte","Supprimer le compte ? Les projets locaux restent.","Reconnectez-vous avant la suppression.","Compte supprimé","Annuler"),
"pl" to listOf("Pokaż","Ukryj","Usuń konto","Usunąć konto? Projekty lokalne pozostaną.","Zaloguj się ponownie przed usunięciem.","Konto usunięte","Anuluj"),
"es" to listOf("Mostrar","Ocultar","Eliminar cuenta","¿Eliminar cuenta? Los proyectos locales se conservan.","Vuelve a iniciar sesión antes de eliminar.","Cuenta eliminada","Cancelar"),
"it" to listOf("Mostra","Nascondi","Elimina account","Eliminare l’account? I progetti locali restano.","Accedi nuovamente prima di eliminare.","Account eliminato","Annulla"),
"lv" to listOf("Rādīt","Slēpt","Dzēst kontu","Dzēst kontu? Vietējie projekti paliks.","Pirms dzēšanas piesakieties vēlreiz.","Konts dzēsts","Atcelt"),
"lt" to listOf("Rodyti","Slėpti","Ištrinti paskyrą","Ištrinti paskyrą? Vietiniai projektai liks.","Prieš ištrinant prisijunkite iš naujo.","Paskyra ištrinta","Atšaukti"),
"nb" to listOf("Vis","Skjul","Slett konto","Slette kontoen? Lokale prosjekter beholdes.","Logg inn på nytt før sletting.","Konto slettet","Avbryt"),
"da" to listOf("Vis","Skjul","Slet konto","Slet kontoen? Lokale projekter bevares.","Log ind igen før sletning.","Konto slettet","Annuller"))
