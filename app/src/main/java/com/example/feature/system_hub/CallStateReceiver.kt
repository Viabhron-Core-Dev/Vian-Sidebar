package com.example.feature.system_hub

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val prefs = context.getSharedPreferences("vian_settings", Context.MODE_PRIVATE)
            val autoEnabled = prefs.getBoolean("call_recorder_enabled", false)
            val manualEnabled = prefs.getBoolean("call_recorder_manual_enabled", false)
            
            if (autoEnabled || manualEnabled) {
                // To keep it simple without rewriting CallRecorderManager, we can just start a quick IntentService 
                // or just call CallRecorderManager if it can handle being instantiated per-call.
                // Actually, if we just instantiate CallRecorderManager, it registers its own listeners, which might leak if not stopped.
                // Let's just have an orchestrator service or use HandleService.
            }
        }
    }
}
