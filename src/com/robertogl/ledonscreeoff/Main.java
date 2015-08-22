package com.robertogl.ledonscreeoff;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage {
	private static final String NOTIFICATION_CLASS = "com.android.server.notification.NotificationManagerService";
	private static boolean screenon;
	private static Context ledContext;
    
	private static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_USER_PRESENT)) {
                screenon = true;
            }
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                screenon = false;
            } 
        }
    };
	@Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

    if (lpparam.packageName.equals("android") && lpparam.processName.equals("android")) {
    	XposedHelpers.findAndHookMethod(NOTIFICATION_CLASS, lpparam.classLoader, "updateLightsLocked", new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
    			ledContext = (Context) XposedHelpers.callMethod(param.thisObject, "getContext");
    			IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_USER_PRESENT);
                intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                ledContext.registerReceiver(mBroadcastReceiver, intentFilter);
                
    			if (screenon) {
    			XposedHelpers.setBooleanField(param.thisObject, "mScreenOn", false);
    			}
    		}
    		});
    	}
    }
}
