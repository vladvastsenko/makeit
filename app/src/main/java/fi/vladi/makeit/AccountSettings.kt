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
 }else{
 OutlinedTextField(email,{email=it},label={Text(t[1])},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),modifier=Modifier.fillMaxWidth())
 OutlinedTextField(password,{password=it},label={Text(t[2])},singleLine=true,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),modifier=Modifier.fillMaxWidth())
 Button(enabled=auth!=null&&!busy&&email.isNotBlank()&&password.isNotEmpty(),onClick={busy=true;auth?.signInWithEmailAndPassword(email.trim(),password)?.addOnCompleteListener{busy=false;status=if(it.isSuccessful)"" else t[10];if(it.isSuccessful)password=""}},modifier=Modifier.fillMaxWidth()){Text(t[3])}
 OutlinedButton(enabled=auth!=null&&!busy&&email.isNotBlank()&&password.length>=6,onClick={busy=true;auth?.createUserWithEmailAndPassword(email.trim(),password)?.addOnCompleteListener{task->if(task.isSuccessful){password="";task.result.user?.sendEmailVerification()?.addOnCompleteListener{busy=false;status=if(it.isSuccessful)t[8] else t[10]} ?: run{busy=false}}else{busy=false;status=t[10]}}},modifier=Modifier.fillMaxWidth()){Text(t[4])}
 OutlinedButton(enabled=auth!=null&&google!=null&&!busy,onClick={google?.let{busy=true;launcher.launch(it.signInIntent)}},modifier=Modifier.fillMaxWidth()){Text(t[5])}
 TextButton(enabled=auth!=null&&!busy&&email.isNotBlank(),onClick={busy=true;auth?.sendPasswordResetEmail(email.trim())?.addOnCompleteListener{busy=false;status=if(it.isSuccessful)t[8] else t[10]}}){Text(t[6])}
 }
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
