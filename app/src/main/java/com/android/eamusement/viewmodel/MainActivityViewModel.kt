package com.android.eamusement.viewmodel

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.cardemulation.NfcFCardEmulation
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    lateinit var nfcFCardEmulation: NfcFCardEmulation
    lateinit var nfcAdapter: NfcAdapter
    lateinit var componentName: ComponentName
    lateinit var sf: SharedPreferences
    val context = getApplication<Application>().applicationContext

    //NFC 초기화
    fun firstnfc() : String {
        val pm = getApplication<Application>().packageManager
        val isSupport = pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF)

        if (isSupport == false) {
            Toast.makeText(context, "Not Support", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Start Setting", Toast.LENGTH_SHORT).show()

            nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            nfcFCardEmulation = NfcFCardEmulation.getInstance(nfcAdapter)
            componentName = ComponentName("com.android.eamusement", "com.android.eamusement.service.eAMEMuService")
            nfcFCardEmulation.registerSystemCodeForService(componentName, "4000")

            sf = getApplication<Application>().getSharedPreferences("sf", ComponentActivity.MODE_PRIVATE)
            var cardId : String? = sf.getString("cardID", "")

            if (cardId.equals("")) {
                cardId = randomCardID()
                nfcFCardEmulation.setNfcid2ForService(componentName, cardId)

                val editor = sf.edit()
                editor.putString("cardID", cardId)
                editor.commit()

                return cardId
            } else {
                nfcFCardEmulation.setNfcid2ForService(componentName, cardId)
                return cardId.toString()
            }
        }

        return ""
    }

    //ViewModel에 Context를 직접 주입하는건 바람직하지 않지만, 별 방법이 없다...
    fun turnonnfc(cardid: String, current: Context) {
        lateinit var inputcardid: String
        inputcardid = cardid

        if(inputcardid == null || inputcardid.length < 16){
            Toast.makeText(
                context,
                "sid는 반드시 16자리 16진수여야 합니다.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        inputcardid = inputcardid.toUpperCase();

        if (!inputcardid.matches(Regex("[0-9a-fA-F]+"))) {
            Toast.makeText(
                context,
                "sid는 반드시 16자리 16진수여야 합니다.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if(inputcardid.substring(0,4).contentEquals("02FE") == false){
            Toast.makeText(
                context,
                "sid는 반드시 02FE로 시작해야합니다.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (nfcFCardEmulation != null && componentName != null) {
            nfcFCardEmulation.disableService(current as Activity?)

            val res = nfcFCardEmulation.setNfcid2ForService(componentName, inputcardid)
            if (res == true) {
                Toast.makeText(
                    context,
                    "성공",
                    Toast.LENGTH_SHORT
                ).show()

                val editor: SharedPreferences.Editor = sf.edit()
                editor.putString("cardID", inputcardid)
                editor.commit()
            } else {
                Toast.makeText(
                    context,
                    "실패",
                    Toast.LENGTH_SHORT
                ).show()
            }

            nfcFCardEmulation.enableService(current, componentName)
        }
    }

    fun randomCardID(): String {
        val random = Random()
        return ("02FE" + java.lang.String.format("%04x", random.nextInt(65536))
            .uppercase(Locale.getDefault())
                + java.lang.String.format("%04x", random.nextInt(65536))
            .uppercase(Locale.getDefault())
                + java.lang.String.format("%04x", random.nextInt(65536))
            .uppercase(Locale.getDefault()))
    }
}