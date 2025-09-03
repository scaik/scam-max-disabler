package ru.scaik.scammaxdisabler;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class crusher implements IXposedHookLoadPackage {
    final String CrushText = "FUCK MAX! FREE DISCORD AND TELEGRAM, ASSHOLES!";


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("ru.oneme.app")) return; // Для старых Xposed API

        XposedHelpers.findAndHookMethod(
            Activity.class,
            "onCreate",
            Bundle.class,
            new XC_MethodHook() {

                // Здесь идёт генерация текста ошибки для обхода "стакинга" ошибок в FireBase
                // В теории если часто "крашить" приложение можно будет засрать им всю консоль FireBase ошибками "FREE DISCORD"
                final String UniqueCrashText = java.util.UUID.randomUUID().toString() + " | " + CrushText;



                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    throw new RuntimeException(UniqueCrashText); // Работает идеально для Android 16, Xposed v100 (по крайней мере lkz vtyz)
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity act = (Activity) param.thisObject;

                    act.runOnUiThread(() -> {
                        new Thread(() -> {
                            android.widget.Toast t = new android.widget.Toast(act);
                            t.setView(null);
                            t.show();
                            // на всякий случай
                        }).start();
                    });

                    View root = act.findViewById(android.R.id.content);
                    root.post(() -> {
                        throw new RuntimeException(UniqueCrashText);
                        // на всякий случай
                    });

                    android.os.Looper.getMainLooper().quit(); // Вот это - босс, обойти, как я знаю - нереально
                }
            }
        );


    }
}
