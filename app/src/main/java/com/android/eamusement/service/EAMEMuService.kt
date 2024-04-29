package com.android.eamusement.service

import android.nfc.cardemulation.HostNfcFService
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class eAMEMuService : HostNfcFService() {
    override fun processNfcFPacket(commandPacket: ByteArray, extras: Bundle): ByteArray? {
        if (commandPacket.size < 1 + 1 + 8) {
            Toast.makeText(
                this.applicationContext,
                "ProcessPacket: " + "too short packet!",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }
        val nfcid2 = ByteArray(8)
        System.arraycopy(commandPacket, 2, nfcid2, 0, 8)
        var res = ""
        for (i in 0..7) {
            res += Integer.toHexString(nfcid2[i].toInt() and 0xFF)
        }

        return if (commandPacket[1] == 0x04.toByte()) {
            val resp = ByteArray(1 + 1 + 8 + 1)
            resp[0] = 11.toByte()
            resp[1] = 0x05.toByte()
            System.arraycopy(nfcid2, 0, resp, 2, 8)
            resp[10] = 0.toByte()
            Log.d("debug", resp.toString())
            resp
        } else {
            return null
        }
    }

    override fun onDeactivated(reason: Int) {
    }
}