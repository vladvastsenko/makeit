package fi.vladi.makeit
import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.ump.*

internal class MakeitAds(private val activity:Activity){
 private val consent=UserMessagingPlatform.getConsentInformation(activity)
 var ready by mutableStateOf(false)
 var privacyRequired by mutableStateOf(false)
 private var initialized=false
 fun start(){
  consent.requestConsentInfoUpdate(activity,ConsentRequestParameters.Builder().build(),{
   UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity){refresh()}
  },{refresh()})
 }
 private fun refresh(){
  privacyRequired=consent.privacyOptionsRequirementStatus==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
  ready=false
  if(consent.canRequestAds()){
   if(initialized){ready=true;return}
   initialized=true
   MobileAds.initialize(activity){activity.runOnUiThread{ready=consent.canRequestAds()}}
  }
 }
 fun privacy(){
  ready=false
  UserMessagingPlatform.showPrivacyOptionsForm(activity){refresh()}
 }
}
private const val LIVE_BANNER="ca-app-pub-5274592019802045/8437252898"
private const val TEST_BANNER="ca-app-pub-3940256099942544/9214589741"
@Composable internal fun MakeitBanner(){
 val context=LocalContext.current
 BoxWithConstraints(Modifier.fillMaxWidth()){
  val width=maxWidth.value.toInt().coerceAtLeast(1)
  val view=remember(width){AdView(context).apply{
   adUnitId=TEST_BANNER
   setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context,width))
   loadAd(AdRequest.Builder().build())
  }}
  DisposableEffect(view){onDispose{view.destroy()}}
  AndroidView(factory={view},modifier=Modifier.fillMaxWidth())
 }
}
internal val privacyLabels=mapOf("en" to "Privacy options","fi" to "Tietosuoja-asetukset","et" to "Privaatsusseaded","ru" to "Настройки конфиденциальности","sv" to "Sekretessinställningar","de" to "Datenschutzeinstellungen","fr" to "Options de confidentialité","pl" to "Ustawienia prywatności","es" to "Opciones de privacidad","it" to "Opzioni sulla privacy","lv" to "Privātuma iestatījumi","lt" to "Privatumo nustatymai","nb" to "Personverninnstillinger","da" to "Privatlivsindstillinger")
