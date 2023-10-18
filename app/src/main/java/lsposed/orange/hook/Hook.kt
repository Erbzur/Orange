package lsposed.orange.hook

import android.app.Activity
import android.app.AndroidAppHelper
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import lsposed.orange.BuildConfig
import lsposed.orange.SharedConfig
import lsposed.orange.model.mapActivityOrientation
import lsposed.orange.ui.main.MainFragment

class Hook : IXposedHookZygoteInit, IXposedHookLoadPackage {

    private companion object {
        const val TAG = "Orange"

        fun logx(msg: String, toXposed: Boolean = false) {
            if (toXposed) {
                XposedBridge.log("$TAG: $msg")
            } else {
                Log.d(TAG, msg)
            }
        }
    }

    private val moduleConfig by lazy { SharedConfig.Fetcher() }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        XposedBridge.hookAllMethods(Activity::class.java, "attach", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val currentApp = AndroidAppHelper.currentApplication()
                if (currentApp.packageName == BuildConfig.APPLICATION_ID) {
                    return
                }
                logx("currentApp=${currentApp.packageName}")
                val configApp = moduleConfig.findConfigApp(currentApp.packageName) ?: return
                val orientation = mapActivityOrientation(configApp.orientation)
                val activity = param.thisObject as Activity
                logx("hook ${activity.javaClass.name}", true)
                activity.requestedOrientation = orientation
            }
        })
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(
                MainFragment::class.qualifiedName,
                lpparam.classLoader,
                "isModuleActive",
                XC_MethodReplacement.returnConstant(true)
            )
        }
    }
}