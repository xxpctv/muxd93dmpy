package com.github.tvbox.osc.base;

import android.app.Activity;

import androidx.multidex.MultiDexApplication;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.callback.EmptyCallback;
import com.github.tvbox.osc.callback.LoadingCallback;
import com.github.tvbox.osc.data.AppDataManager;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.util.AppManager;
import com.github.tvbox.osc.util.EpgUtil;
import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.LocaleHelper;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.OkGoHelper;
import com.github.tvbox.osc.util.PlayerHelper;
import com.github.tvbox.osc.util.js.JSEngine;
import com.kingja.loadsir.core.LoadSir;
import com.orhanobut.hawk.Hawk;
import com.p2p.P2PClass;

import java.util.ArrayList;
import java.util.Arrays;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

/**
 * @author pj567
 * @date :2020/12/17
 * @description:
 */
public class App extends MultiDexApplication {
    private static App instance;
    private VodInfo vodInfo;

    public static App getInstance() {
        return instance;
    }

    private static P2PClass p;
    public static String burl;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initParams();
        // takagen99 : Initialize Locale
        initLocale();
        // OKGo
        OkGoHelper.init();
        // Get EPG Info获取台标
        EpgUtil.init();
        // 初始化Web服务器
        ControlManager.init(this);
        //初始化数据库
        AppDataManager.init();
        LoadSir.beginBuilder()
                .addCallback(new EmptyCallback())
                .addCallback(new LoadingCallback())
                .commit();
        AutoSizeConfig.getInstance().setCustomFragment(true).getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
        PlayerHelper.init();
        JSEngine.getInstance().create();
        FileUtils.cleanPlayerCache();
    }

    private void initParams() {
        // Hawk
        Hawk.init(this).build();
        Hawk.put(HawkConfig.DEBUG_OPEN, false);

        putDefault(HawkConfig.HOME_REC, 1);       // Home Rec 0=豆瓣, 1=推荐, 2=历史
        putDefault(HawkConfig.HOME_SHOW_SOURCE, true);         //首页显示数据源  true=显示，false=不显示
        putDefault(HawkConfig.HOME_REC_STYLE, true);    // 首页多行 true=多行，false=单行
        putDefault(HawkConfig.PLAY_TYPE, 2);      // Player   0=系统, 1=IJK, 2=Exo
        putDefault(HawkConfig.IJK_CODEC, "硬解码");// IJK Render 软解码, 硬解码
        putDefault(HawkConfig.HOME_SEARCH_POSITION, false);      //主页搜索按钮位置 false=下方 ,true=上方
		putDefault(HawkConfig.HOME_MENU_POSITION, true);     	 //主页菜单按钮位置 false=下方 ,true=上方
        putDefault(HawkConfig.SEARCH_VIEW, 1);                   // 0=文字列表搜索结果 ,1= 图片搜索结果
        putDefault(HawkConfig.HOME_NUM, 2);       // 历史记录数量
//        putDefault(HawkConfig.DOH_URL, 2);        // DNS
//        putDefault(HawkConfig.LIVE_CROSS_GROUP, 1);    //直播：跨选分类 0=否 ,1=是
//        putDefault(HawkConfig.LIVE_CHANNEL_REVERSE, 0);    //直播：换台反转 0= 否 ,1=是

//        String[] history = getResources().getStringArray(R.array.default_api_history);
        ArrayList<String> api_history = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.default_api_history)));
        putDefault(HawkConfig.API_HISTORY, api_history);  // 接口历史记录
        putDefault(HawkConfig.API_URL, "https://jihulab.com/clear1/yingmi/-/raw/main/xh.txt");  // 接口历史记录
        putDefault(HawkConfig.EPG_URL, "https://epg.112114.xyz/");  // 接口历史记录

    }

    private void initLocale() {
        if (Hawk.get(HawkConfig.HOME_LOCALE, 0) == 0) {
            LocaleHelper.setLocale(App.this, "zh");
        } else {
            LocaleHelper.setLocale(App.this, "");
        }
    }

    private void putDefault(String key, Object value) {
        if (!Hawk.contains(key)) {
            Hawk.put(key, value);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        JSEngine.getInstance().destroy();
    }

    public VodInfo getVodInfo() {
        return this.vodInfo;
    }

    public void setVodInfo(VodInfo vodinfo) {
        this.vodInfo = vodinfo;
    }

    public static P2PClass getp2p() {
        try {
            if (p == null) {
                p = new P2PClass(instance.getExternalCacheDir().getAbsolutePath());
            }
            return p;
        } catch (Exception e) {
            LOG.e(e.toString());
            return null;
        }
    }

    public Activity getCurrentActivity() {
        return AppManager.getInstance().currentActivity();
    }
}