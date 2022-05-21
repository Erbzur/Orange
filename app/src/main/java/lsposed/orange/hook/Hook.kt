package lsposed.orange.hook

import android.app.Activity
import android.app.AndroidAppHelper
import android.content.pm.ActivityInfo
import android.util.Log
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import lsposed.orange.BuildConfig
import lsposed.orange.model.Orientation
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

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        XposedBridge.hookAllMethods(
            Activity::class.java,
            "attach",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val currentApp = AndroidAppHelper.currentApplication()
                    if (currentApp.packageName == BuildConfig.APPLICATION_ID) {
                        return
                    }
                    logx("currentApp=${currentApp.packageName}")
                    val moduleConfig = ModuleConfig(currentApp)
                    val configApp = moduleConfig.findConfigApp(currentApp.packageName) ?: return
                    val orientation = when (Orientation.values()[configApp.orientation]) {
                        Orientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        Orientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        Orientation.REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        Orientation.SENSOR_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        Orientation.SENSOR -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                        else -> return
                    }
                    val activity = param.thisObject as Activity
                    logx("hook ${activity.javaClass.name}", true)
                    activity.requestedOrientation = orientation
                }
            }
        )
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