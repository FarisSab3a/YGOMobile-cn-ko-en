package cn.garymb.ygomobile.ui.preference.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.ourygo.assistant.service.DuelAssistantService;
import com.tencent.bugly.beta.Beta;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.garymb.ygomobile.App;
import cn.garymb.ygomobile.AppsSettings;
import cn.garymb.ygomobile.Constants;
import cn.garymb.ygomobile.lite.R;
import cn.garymb.ygomobile.ui.home.MainActivity;
import cn.garymb.ygomobile.ui.plus.DialogPlus;
import cn.garymb.ygomobile.ui.plus.VUiKit;
import cn.garymb.ygomobile.ui.preference.PreferenceFragmentPlus;
import cn.garymb.ygomobile.utils.FileUtils;
import cn.garymb.ygomobile.utils.IOUtils;
import cn.garymb.ygomobile.utils.SystemUtils;
import ocgcore.DataManager;

import static cn.garymb.ygomobile.Constants.ACTION_RELOAD;
import static cn.garymb.ygomobile.Constants.CORE_SKIN_AVATAR_SIZE;
import static cn.garymb.ygomobile.Constants.CORE_SKIN_BG_SIZE;
import static cn.garymb.ygomobile.Constants.CORE_SKIN_CARD_COVER_SIZE;
import static cn.garymb.ygomobile.Constants.PERF_TEST_REPLACE_KERNEL;
import static cn.garymb.ygomobile.Constants.PREF_CHANGE_LOG;
import static cn.garymb.ygomobile.Constants.PREF_CHECK_UPDATE;
import static cn.garymb.ygomobile.Constants.PREF_DECK_DELETE_DILAOG;
import static cn.garymb.ygomobile.Constants.PREF_DECK_MANAGER_V2;
import static cn.garymb.ygomobile.Constants.PREF_DEL_EX;
import static cn.garymb.ygomobile.Constants.PREF_FONT_ANTIALIAS;
import static cn.garymb.ygomobile.Constants.PREF_FONT_SIZE;
import static cn.garymb.ygomobile.Constants.PREF_GAME_FONT;
import static cn.garymb.ygomobile.Constants.PREF_GAME_PATH;
import static cn.garymb.ygomobile.Constants.PREF_IMAGE_QUALITY;
import static cn.garymb.ygomobile.Constants.PREF_IMMERSIVE_MODE;
import static cn.garymb.ygomobile.Constants.PREF_KEEP_SCALE;
import static cn.garymb.ygomobile.Constants.PREF_LOCK_SCREEN;
import static cn.garymb.ygomobile.Constants.PREF_ONLY_GAME;
import static cn.garymb.ygomobile.Constants.PREF_OPENGL_VERSION;
import static cn.garymb.ygomobile.Constants.PREF_PENDULUM_SCALE;
import static cn.garymb.ygomobile.Constants.PREF_READ_EX;
import static cn.garymb.ygomobile.Constants.PREF_SENSOR_REFRESH;
import static cn.garymb.ygomobile.Constants.PREF_START_SERVICEDUELASSISTANT;
import static cn.garymb.ygomobile.Constants.PREF_USE_EXTRA_CARD_CARDS;
import static cn.garymb.ygomobile.Constants.SETTINGS_AVATAR;
import static cn.garymb.ygomobile.Constants.SETTINGS_CARD_BG;
import static cn.garymb.ygomobile.Constants.SETTINGS_COVER;
import static cn.garymb.ygomobile.ui.home.ResCheckTask.getDatapath;

public class SettingFragment extends PreferenceFragmentPlus {

    private static final int COPY_SO_OK = 0;
    private static final int COPY_SO_EXCEPTION = 1;
    private static final int COPY_SO_NO_ROOT = 2;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case COPY_SO_OK:
                    Toast.makeText(getActivity(), "替换成功", Toast.LENGTH_SHORT).show();
                    break;
                case COPY_SO_EXCEPTION:
                    Toast.makeText(getActivity(), "替换失败，原因为" + msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case COPY_SO_NO_ROOT:
                    Toast.makeText(getActivity(), "没有root权限", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private AppsSettings mSettings;
    private boolean isInit = true;

    public SettingFragment() {

    }

    @Override
    protected SharedPreferences getSharedPreferences() {
        return AppsSettings.get().getSharedPreferences();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mSettings = AppsSettings.get();

        addPreferencesFromResource(R.xml.preference_game);
        bind(PREF_GAME_PATH, mSettings.getResourcePath());
//        bind(PREF_GAME_VERSION, mSettings.getVersionString(mSettings.getGameVersion()));
        bind(PREF_CHANGE_LOG, SystemUtils.getVersionName(getActivity())
                + "(" + SystemUtils.getVersion(getActivity()) + ")");
        bind(PREF_CHECK_UPDATE, getString(R.string.settings_about_author_pref) + " : " + getString(R.string.settings_author));
        bind(PREF_START_SERVICEDUELASSISTANT, mSettings.isServiceDuelAssistant());
        bind(PREF_LOCK_SCREEN, mSettings.isLockSreenOrientation());
        bind(PREF_FONT_ANTIALIAS, mSettings.isFontAntiAlias());
        bind(PREF_IMMERSIVE_MODE, mSettings.isImmerSiveMode());
        bind(PREF_PENDULUM_SCALE, mSettings.isPendulumScale());
        bind(PREF_SENSOR_REFRESH, mSettings.isSensorRefresh());
        bind(PREF_OPENGL_VERSION, mSettings.getOpenglVersion());
        bind(PREF_IMAGE_QUALITY, mSettings.getCardQuality());
        bind(PREF_GAME_FONT, mSettings.getFontPath());
        bind(PREF_READ_EX, mSettings.isReadExpansions());
        bind(PREF_DEL_EX, getString(R.string.about_delete_ex));
        bind(PREF_DECK_MANAGER_V2, mSettings.isUseDeckManagerV2());
        bind(PERF_TEST_REPLACE_KERNEL, "需root权限，请在开发者的指导下食用");
        Preference preference = findPreference(PREF_READ_EX);
        if (preference != null) {
            preference.setSummary(mSettings.getExpansionsPath().getAbsolutePath());
        }
        bind(PREF_DECK_DELETE_DILAOG, mSettings.isDialogDelete());
        //bind(PREF_USE_EXTRA_CARD_CARDS, mSettings.isUseExtraCards());
        bind(SETTINGS_AVATAR, new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_AVATAR_ME).getAbsolutePath());
        bind(SETTINGS_COVER, new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_COVER).getAbsolutePath());
        bind(SETTINGS_CARD_BG, new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_BG).getAbsolutePath());
        bind(PREF_FONT_SIZE, mSettings.getFontSize());
        bind(PREF_ONLY_GAME, mSettings.isOnlyGame());
        bind(PREF_KEEP_SCALE, mSettings.isKeepScale());
        isInit = false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        super.onPreferenceChange(preference, value);
        if (!isInit) {
            /*if (PREF_GAME_VERSION.equals(preference.getKey())) {
                int v = AppsSettings.get().getVersionValue(value.toString());
                if (v > 0 && v <= AppsSettings.get().getVersionValue("0xF99F")) {
                    mSettings.setGameVersion(v);
                    super.onPreferenceChange(preference, AppsSettings.get().getVersionString(v));
                    return true;
                } else {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(getContext(), getString(R.string.error_game_ver) + " " + value.toString(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), R.string.error_game_ver, Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            }*/
            if (PREF_FONT_SIZE.equals(preference.getKey())) {
                int size = Constants.DEF_PREF_FONT_SIZE;
                try {
                    size = Integer.parseInt(String.valueOf(value));
                } catch (Exception e) {

                }
            }
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                mSharedPreferences.edit().putBoolean(preference.getKey(), checkBoxPreference.isChecked()).apply();
                //如果是设置额外卡库的选项
                if (preference.getKey().equals(PREF_READ_EX)) {
                    //设置使用额外卡库后重新加载卡片数据
                    DataManager.get().load(true);
                }
                //开关决斗助手
                if (preference.getKey().equals(PREF_START_SERVICEDUELASSISTANT)) {
                    if (checkBoxPreference.isChecked()) {
                        getActivity().startService(new Intent(getActivity(), DuelAssistantService.class));
                    } else {
                        getActivity().stopService(new Intent(getActivity(), DuelAssistantService.class));
                    }
                }
                return true;
            }
            boolean rs = super.onPreferenceChange(preference, value);
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                mSharedPreferences.edit().putString(preference.getKey(), listPreference.getValue()).apply();
            } else {
                mSharedPreferences.edit().putString(preference.getKey(), "" + value).apply();
            }
            return rs;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (PREF_CHANGE_LOG.equals(key)) {
            new DialogPlus(getActivity())
                    .setTitleText(getString(R.string.settings_about_change_log))
                    .loadUrl("file:///android_asset/changelog.html", Color.TRANSPARENT)
                    .show();
        }
        if (PREF_CHECK_UPDATE.equals(key)) {
            Beta.checkUpgrade();
        }
        if (PREF_DEL_EX.equals(key)) {
            final DialogPlus dialog = new DialogPlus(getContext());
            dialog.setTitle(R.string.question);
            dialog.setMessage(R.string.ask_delete_ex);
            dialog.setLeftButtonListener((dlg, s) -> {
                FileUtils.delFile(mSettings.getExpansionsPath().getAbsolutePath());
                Toast.makeText(getContext(), R.string.done, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            });
            dialog.setRightButtonListener((dlg, s) -> {
                dialog.dismiss();
            });
            dialog.show();
        }
        if (PREF_PENDULUM_SCALE.equals(key)) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
            setPendlumScale(checkBoxPreference.isChecked());
        } else if (PREF_GAME_FONT.equals(key)) {
            //选择ttf字体文件，保存
            showFileChooser(preference, "*.ttf", mSettings.getFontDirPath(), getString(R.string.dialog_select_font));
        } else if (SETTINGS_AVATAR.equals(key)) {
            final DialogPlus dialog = new DialogPlus(getContext());
            dialog.setContentView(R.layout.dialog_avatar_select);
            dialog.setTitle(R.string.settings_game_avatar);
            dialog.show();
            //显示头像图片对话框
            View viewDialog = dialog.getContentView();
            ImageView avatar1 = viewDialog.findViewById(R.id.me);
            ImageView avatar2 = viewDialog.findViewById(R.id.opponent);
            setImage(mSettings.getCoreSkinPath() + "/" + Constants.CORE_SKIN_AVATAR_ME, CORE_SKIN_AVATAR_SIZE[0], CORE_SKIN_AVATAR_SIZE[1], avatar1);
            setImage(mSettings.getCoreSkinPath() + "/" + Constants.CORE_SKIN_AVATAR_OPPONENT, CORE_SKIN_AVATAR_SIZE[0], CORE_SKIN_AVATAR_SIZE[1], avatar2);
            avatar1.setOnClickListener((v) -> {
                //打开系统文件相册
                String outFile = new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_AVATAR_ME).getAbsolutePath();
                showImageDialog(preference, getString(R.string.settings_game_avatar),
                        outFile,
                        true, CORE_SKIN_AVATAR_SIZE[0], CORE_SKIN_AVATAR_SIZE[1]);
                dialog.dismiss();
            });
            avatar2.setOnClickListener((v) -> {
                //打开系统文件相册
                String outFile = new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_AVATAR_OPPONENT).getAbsolutePath();
                showImageDialog(preference, getString(R.string.settings_game_avatar),
                        outFile,
                        true, CORE_SKIN_AVATAR_SIZE[0], CORE_SKIN_AVATAR_SIZE[1]);
                dialog.dismiss();
            });
        } else if (SETTINGS_COVER.equals(key)) {
            //显示卡背图片对话框
            final DialogPlus dialog = new DialogPlus(getContext());
            dialog.setContentView(R.layout.dialog_cover_select);
            dialog.setTitle(R.string.card_cover);
            dialog.show();
            View viewDialog = dialog.getContentView();
            ImageView cover1 = viewDialog.findViewById(R.id.cover1);
            ImageView cover2 = viewDialog.findViewById(R.id.cover2);
            setImage(mSettings.getCoreSkinPath() + "/" + Constants.CORE_SKIN_COVER, CORE_SKIN_CARD_COVER_SIZE[0], CORE_SKIN_CARD_COVER_SIZE[1], cover1);
            setImage(mSettings.getCoreSkinPath() + "/" + Constants.CORE_SKIN_COVER2, CORE_SKIN_CARD_COVER_SIZE[0], CORE_SKIN_CARD_COVER_SIZE[1], cover2);
            cover1.setOnClickListener((v) -> {
                //打开系统文件相册
                String outFile = new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_COVER).getAbsolutePath();
                showImageDialog(preference, getString(R.string.card_cover),
                        outFile,
                        true, CORE_SKIN_CARD_COVER_SIZE[0], CORE_SKIN_CARD_COVER_SIZE[1]);
                dialog.dismiss();
            });
            cover2.setOnClickListener((v) -> {
                //打开系统文件相册
                String outFile = new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_COVER2).getAbsolutePath();
                showImageDialog(preference, getString(R.string.card_cover),
                        outFile,
                        true, CORE_SKIN_CARD_COVER_SIZE[0], CORE_SKIN_CARD_COVER_SIZE[1]);
                dialog.dismiss();
            });
        } else if (SETTINGS_CARD_BG.equals(key)) {
            //显示背景图片对话框
            final DialogPlus dialog = new DialogPlus(getContext());
            dialog.setContentView(R.layout.dialog_bg_select);
            dialog.setTitle(R.string.game_bg);
            dialog.show();
            View viewDialog = dialog.getContentView();
            ImageView bg = viewDialog.findViewById(R.id.bg);
            ImageView bg_menu = viewDialog.findViewById(R.id.bg_menu);
            ImageView bg_deck = viewDialog.findViewById(R.id.bg_deck);
            setImage(mSettings.getCoreSkinPath() + "/" + Constants.CORE_SKIN_BG, CORE_SKIN_BG_SIZE[0], CORE_SKIN_BG_SIZE[1], bg);
            setImage(mSettings.getCoreSkinPath() + "/" + Constants.CORE_SKIN_BG_MENU, CORE_SKIN_BG_SIZE[0], CORE_SKIN_BG_SIZE[1], bg_menu);
            setImage(mSettings.getCoreSkinPath() + "/" + Constants.CORE_SKIN_BG_DECK, CORE_SKIN_BG_SIZE[0], CORE_SKIN_BG_SIZE[1], bg_deck);
            bg.setOnClickListener((v) -> {
                //打开系统文件相册
                String outFile = new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_BG).getAbsolutePath();
                showImageDialog(preference, getString(R.string.bg),
                        outFile,
                        true, CORE_SKIN_BG_SIZE[0], CORE_SKIN_BG_SIZE[1]);
                dialog.dismiss();
            });
            bg_menu.setOnClickListener((v) -> {
                //打开系统文件相册
                String outFile = new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_BG_MENU).getAbsolutePath();
                showImageDialog(preference, getString(R.string.bg_menu),
                        outFile,
                        true, CORE_SKIN_BG_SIZE[0], CORE_SKIN_BG_SIZE[1]);
                dialog.dismiss();
            });
            bg_deck.setOnClickListener((v) -> {
                //打开系统文件相册
                String outFile = new File(mSettings.getCoreSkinPath(), Constants.CORE_SKIN_BG_DECK).getAbsolutePath();
                showImageDialog(preference, getString(R.string.bg_deck),
                        outFile,
                        true, CORE_SKIN_BG_SIZE[0], CORE_SKIN_BG_SIZE[1]);
                dialog.dismiss();
            });
        } else if (PREF_USE_EXTRA_CARD_CARDS.equals(key)) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
            if (checkBoxPreference.isChecked()) {
                checkBoxPreference.setChecked(false);
                mSettings.setUseExtraCards(false);
                showFileChooser(checkBoxPreference, "*.cdb", mSettings.getResourcePath(), getString(R.string.dialog_select_database));
            } else {
                mSettings.setUseExtraCards(false);
            }
        } else if (PREF_GAME_PATH.equals(key)) {
            showFolderChooser(preference, mSettings.getResourcePath(), getString(R.string.choose_game_path));
        } else if (PERF_TEST_REPLACE_KERNEL.equals(key)) {
            showFileChooser(preference, ".so", mSettings.getResourcePath(), "内核文件选择");
        }
        return false;
    }

    @Override
    protected void onChooseFileFail(Preference preference) {
        super.onChooseFileFail(preference);
        //空指针异常
        if (preference == null) return;
        String key = preference.getKey();
        if (PREF_USE_EXTRA_CARD_CARDS.equals(key)) {
            mSettings.setUseExtraCards(false);
            ((CheckBoxPreference) preference).setChecked(false);
        }
    }

    @Override
    protected void onChooseFileOk(Preference preference, String file) {
        if (preference == null) return;
        String key = preference.getKey();
        if (Constants.DEBUG)
            Log.i("kk", "onChooseFileOk:" + key + ",file=" + file);
        if (SETTINGS_AVATAR.equals(key) || SETTINGS_COVER.equals(key) || SETTINGS_CARD_BG.equals(key)) {
            super.onChooseFileOk(preference, file);
            onPreferenceClick(preference);
        } else if (PREF_GAME_PATH.equalsIgnoreCase(preference.getKey())) {
            if (!TextUtils.equals(mSettings.getResourcePath(), file)) {
//                Toast.makeText(getActivity(), R.string.restart_app, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MainActivity.class).setAction(ACTION_RELOAD));
                getActivity().finish();
            }
            mSettings.setResourcePath(file);
            super.onChooseFileOk(preference, file);
        } else if (PREF_USE_EXTRA_CARD_CARDS.equals(key)) {
            ((CheckBoxPreference) preference).setChecked(true);
            mSettings.setUseExtraCards(true);
            copyDataBase(preference, file);
        } else if (PERF_TEST_REPLACE_KERNEL.equals(key)) {
            File path = App.get().getFilesDir().getParentFile();
            String name = "libYGOMobile.so";
            File soFile = new File(path.getAbsolutePath() + "/lib", name);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message me = new Message();
                    Process process = null;
                    DataOutputStream os = null;
                    try {
                        String cmd = "chmod -R 777 " + soFile.getAbsolutePath();
                        process = Runtime.getRuntime().exec("su"); //切换到root帐号
                        if (process == null) {
                            me.what = COPY_SO_NO_ROOT;
                            handler.sendMessage(me);
                            return;
                        }
                        os = new DataOutputStream(process.getOutputStream());
                        os.writeBytes(cmd + "\n");
                        os.writeBytes("exit\n");
                        os.flush();
                        process.waitFor();

                        IOUtils.delete(soFile);
                        FileUtils.copyFile(file, soFile.getAbsolutePath(), true);
                        me.what = COPY_SO_OK;
                    } catch (Exception e) {
                        e.printStackTrace();
                        me.what = COPY_SO_EXCEPTION;
                        me.obj = e;
                    } finally {

                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (process != null)
                            process.destroy();

                    }
                    handler.sendMessage(me);
                }
            }).start();

        } else {
            super.onChooseFileOk(preference, file);
        }
    }

    private void showImageDialog(Preference preference, String title, String outFile, boolean isJpeg, int outWidth, int outHeight) {
        int width = getResources().getDisplayMetrics().widthPixels;
        //DialogPlus builder = new DialogPlus(getActivity());
        final ImageView imageView = new ImageView(getActivity());
        FrameLayout frameLayout = new FrameLayout(getActivity());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //builder.setTitle(title);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        frameLayout.addView(imageView, layoutParams);
        // builder.setContentView(frameLayout);
        //builder.setLeftButtonText(R.string.settings);
        //builder.setLeftButtonListener((dlg, s) -> {
        showImageCropChooser(preference, getString(R.string.dialog_select_image), outFile,
                isJpeg, outWidth, outHeight);
        //dlg.dismiss();
        // });
//        builder.setOnCancelListener((dlg) -> {
//            BitmapUtil.destroy(imageView.getDrawable());
//        });
        //builder.show();
        File img = new File(outFile);
        if (img.exists()) {
            Glide.with(this).load(img).signature(new StringSignature(img.getName() + img.lastModified()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(outWidth, outHeight)
                    .into(imageView);
        }
    }

    public void setImage(String outFile, int outWidth, int outHeight, ImageView imageView) {
        File img = new File(outFile);
        if (img.exists()) {
            Glide.with(this).load(img).signature(new StringSignature(img.getName() + img.lastModified()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(outWidth, outHeight)
                    .into(imageView);
        }
    }

    private void copyDataBase(Preference preference, String file) {
        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
        Dialog dlg = DialogPlus.show(getActivity(), null, getString(R.string.copy_databse));
        VUiKit.defer().when(() -> {
            File db = new File(mSettings.getResourcePath(), Constants.DATABASE_NAME);
            InputStream in = null;
            try {
                if (!TextUtils.equals(file, db.getAbsolutePath())) {
                    if (db.exists()) {
                        db.delete();
                    }
                    in = new FileInputStream(file);
                    //复制
                    IOUtils.copyToFile(in, db.getAbsolutePath());
                }
                //处理数据
//                ResCheckTask.doSomeTrickOnDatabase(db.getAbsolutePath());
                return true;
            } catch (Exception e) {

            } finally {
                IOUtils.close(in);
            }
            return false;
        }).fail((e) -> {
            dlg.dismiss();
            mSettings.setUseExtraCards(false);
            checkBoxPreference.setChecked(false);
            Toast.makeText(getActivity(), R.string.restart_app, Toast.LENGTH_SHORT).show();
        }).done((ok) -> {
            dlg.dismiss();
            checkBoxPreference.setChecked(ok);
            mSettings.setUseExtraCards(ok);
            Toast.makeText(getActivity(), R.string.restart_app, Toast.LENGTH_SHORT).show();
        });
    }

    private void setPendlumScale(boolean ok) {
        if (Constants.DEBUG)
            Log.i("kk", "setPendlumScale " + ok);
        File file = new File(mSettings.getResourcePath(), Constants.CORE_SKIN_PENDULUM_PATH);
        if (ok) {
            //rename
            Dialog dlg = DialogPlus.show(getActivity(), null, getString(R.string.coping_pendulum_image));
            VUiKit.defer().when(() -> {
                try {
                    IOUtils.createFolder(file);
                    IOUtils.copyFilesFromAssets(getActivity(), getDatapath(Constants.CORE_SKIN_PENDULUM_PATH),
                            file.getAbsolutePath(), false);
                } catch (IOException e) {
                }
            }).done((re) -> {
                dlg.dismiss();
            });
        } else {
            IOUtils.delete(file);
        }
    }

}

