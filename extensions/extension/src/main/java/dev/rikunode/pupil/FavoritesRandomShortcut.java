package dev.rikunode.pupil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class FavoritesRandomShortcut {
    private static final String FAVORITES_VIEW_MODEL =
            "xyz.quaver.pupil.ui.viewmodel.FavoritesViewModel";
    private static Object viewModel;
    private static Function1 showDetail;
    private static Function1 detailNavigation;
    private static final String PREFS_NAME = "pupil_morphe_shortcut";
    private static final String SIDE_KEY = "random_button_side";
    private static final String DETAIL_KEY = "random_button_on_detail";
    private static WeakReference<ImageButton> shortcutButton = new WeakReference<>(null);
    private static WeakReference<Activity> currentActivity = new WeakReference<>(null);
    private static boolean openingRandom;
    private static boolean onFavorites;
    private static boolean detailVisible;

    private FavoritesRandomShortcut() {}

    public static void bind(Object currentViewModel, Object onShowDetail) {
        boolean previouslyFavorites = onFavorites;
        onFavorites = currentViewModel != null
                && FAVORITES_VIEW_MODEL.equals(currentViewModel.getClass().getName())
                && onShowDetail instanceof Function1;
        viewModel = onFavorites ? currentViewModel : null;
        showDetail = onFavorites ? (Function1) onShowDetail : null;
        if ((!onFavorites || !previouslyFavorites) && detailNavigation == null) detailVisible = false;
        refreshVisibility();
    }

    public static void hideForDetail() {
        detailVisible = true;
        refreshVisibility();
    }

    public static void bindDetail(Function1 onOpenGallery) {
        detailNavigation = onOpenGallery;
        detailVisible = true;
        refreshVisibility();
    }

    public static Function0 wrapOnDismiss(Function0 onDismiss) {
        if (onDismiss == null) return null;
        return () -> {
            Object result = onDismiss.invoke();
            detailNavigation = null;
            detailVisible = false;
            refreshVisibility();
            return result;
        };
    }

    public static void install(Activity activity) {
        currentActivity = new WeakReference<>(activity);
        FrameLayout root = activity.findViewById(android.R.id.content);
        if (root == null) return;

        boolean left = isLeft(activity);
        ImageButton button = shortcutButton.get();
        if (button == null || button.getParent() != root) {
            button = createButton(activity, root, left);
            shortcutButton = new WeakReference<>(button);
        } else {
            button.setLayoutParams(buttonParams(activity, left));
        }
        refreshVisibility();
    }

    // Called inside the original settings ColumnScope composition, immediately after
    // the existing Favorites-management Preference. Reuse Pupil's own Preference UI.
    public static void renderPositionPreference(Context context, Object composer) {
        String current = isLeft(context) ? "左" : "右";
        Function0 onClick = () -> {
            Context owner = context;
            while (owner instanceof ContextWrapper && !(owner instanceof Activity)) {
                owner = ((ContextWrapper) owner).getBaseContext();
            }
            if (owner instanceof Activity) {
                Activity activity = (Activity) owner;
                new AlertDialog.Builder(activity)
                        .setTitle("ランダムボタンの位置")
                        .setSingleChoiceItems(new String[]{"右", "左"}, isLeft(context) ? 1 : 0,
                                (dialog, which) -> {
                                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                            .edit().putString(SIDE_KEY, which == 1 ? "left" : "right")
                                            .apply();
                                    dialog.dismiss();
                                    activity.recreate();
                                })
                        .setNegativeButton("キャンセル", null)
                        .show();
            }
            return Unit.INSTANCE;
        };
        try {
            Class<?> preferences = Class.forName(
                    "xyz.quaver.pupil.ui.compose.settings.PreferenceKt");
            Method preference = null;
            for (Method candidate : preferences.getDeclaredMethods()) {
                if ("Preference".equals(candidate.getName())
                        && candidate.getParameterTypes().length == 12) {
                    preference = candidate;
                    break;
                }
            }
            if (preference == null) throw new NoSuchMethodException("Preference/12");
            preference.invoke(null, "ランダムボタンの位置", null, current,
                    false, false, null, onClick, null, null, composer, 0, 446);
        } catch (ReflectiveOperationException | RuntimeException error) {
            return;
        }
        renderDetailPreference(context, composer);
    }

    private static void renderDetailPreference(Context context, Object composer) {
        boolean enabled = showOnDetail(context);
        Function0 onClick = () -> {
            Context owner = context;
            while (owner instanceof ContextWrapper && !(owner instanceof Activity)) {
                owner = ((ContextWrapper) owner).getBaseContext();
            }
            if (owner instanceof Activity) {
                Activity activity = (Activity) owner;
                new AlertDialog.Builder(activity)
                        .setTitle("作品ページにもランダムボタンを表示")
                        .setSingleChoiceItems(new String[]{"ON", "OFF"}, enabled ? 0 : 1,
                                (dialog, which) -> {
                                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                            .edit().putBoolean(DETAIL_KEY, which == 0).apply();
                                    dialog.dismiss();
                                    activity.recreate();
                                })
                        .setNegativeButton("キャンセル", null)
                        .show();
            }
            return Unit.INSTANCE;
        };
        try {
            Class<?> preferences = Class.forName(
                    "xyz.quaver.pupil.ui.compose.settings.PreferenceKt");
            Method preference = null;
            for (Method candidate : preferences.getDeclaredMethods()) {
                if ("Preference".equals(candidate.getName())
                        && candidate.getParameterTypes().length == 12) {
                    preference = candidate;
                    break;
                }
            }
            if (preference == null) return;
            preference.invoke(null, "作品ページにもランダムボタンを表示", null,
                    enabled ? "ON" : "OFF", false, false, null, onClick,
                    null, null, composer, 0, 446);
        } catch (ReflectiveOperationException | RuntimeException error) {
            return;
        }
    }

    // Registers a separate LazyColumn item between the thumbnails header and pager.
    // The original three-column thumbnail grid is left untouched.
    public static void addThumbnailShortcutItem(Object lazyListScope) {
        Activity activity = currentActivity.get();
        if (activity == null || !showOnDetail(activity)) return;
        try {
            Function3 content = (scope, composer, flags) -> {
                renderThumbnailShortcut((Context) activity, composer);
                return Unit.INSTANCE;
            };
            Class<?> impl = Class.forName("androidx.compose.runtime.internal.ComposableLambdaImpl");
            Object composable = impl.getConstructor(int.class, Object.class, boolean.class)
                    .newInstance(0x5A4F01, content, true);
            Class<?> scopeClass = Class.forName("androidx.compose.foundation.lazy.LazyListScope");
            Class<?> itemClass = Class.forName("androidx.compose.foundation.lazy.LazyItemScope$-CC");
            itemClass.getMethod("item$default", scopeClass, Function3.class)
                    .invoke(null, lazyListScope, composable);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Fail soft: preserve the original thumbnail pager if internals differ.
        }
    }

    private static void renderThumbnailShortcut(Context context, Object composer) {
        Function1 factory = value -> createThumbnailShortcutRow((Context) value);
        Function1 update = value -> Unit.INSTANCE;
        try {
            Class<?> modifierClass = Class.forName("androidx.compose.ui.Modifier");
            Object companion = Class.forName("androidx.compose.ui.Modifier$Companion")
                    .getField("$$INSTANCE").get(null);
            Object modifier = Class.forName("androidx.compose.foundation.layout.SizeKt")
                    .getMethod("fillMaxWidth", modifierClass, float.class)
                    .invoke(null, companion, 1.0f);
            Class<?> composerClass = Class.forName("androidx.compose.runtime.Composer");
            Class.forName("androidx.compose.ui.viewinterop.AndroidView_androidKt")
                    .getMethod("AndroidView", Function1.class, modifierClass, Function1.class,
                            composerClass, int.class)
                    .invoke(null, factory, modifier, update, composer, 0);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Do not crash the gallery when an internal Compose signature differs.
        }
    }

    private static View createThumbnailShortcutRow(Context context) {
        FrameLayout row = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = View.MeasureSpec.getSize(widthMeasureSpec);
                // ThumbnailPager: 3 columns, 8dp between columns, aspect ratio 0.7.
                int itemWidth = Math.max(1, Math.round((width - 2f * dp(context, 8)) / 3f));
                int itemHeight = Math.max(1, Math.round(itemWidth / 0.7f));
                View button = getChildAt(0);
                if (button != null) {
                    button.measure(View.MeasureSpec.makeMeasureSpec(itemWidth, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(itemHeight, View.MeasureSpec.EXACTLY));
                }
                setMeasuredDimension(width, View.resolveSize(itemHeight, heightMeasureSpec));
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                View button = getChildAt(0);
                if (button != null) button.layout(0, 0, button.getMeasuredWidth(), button.getMeasuredHeight());
            }
        };
        ImageButton button = new ImageButton(context);
        int iconId = context.getResources().getIdentifier(
                "shuffle_variant", "drawable", context.getPackageName());
        if (iconId == 0) iconId = android.R.drawable.ic_menu_rotate;
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(68, 68, 91));
        background.setCornerRadius(dp(context, 6));
        button.setBackground(background);
        button.setImageResource(iconId);
        button.setColorFilter(Color.WHITE);
        button.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        button.setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16));
        button.setContentDescription("お気に入りからランダムに作品を開く");
        button.setOnClickListener(ignored -> openRandom(button));
        row.addView(button);
        return row;
    }

    private static boolean showOnDetail(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(DETAIL_KEY, true);
    }

    private static boolean isLeft(Context context) {
        return "left".equals(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(SIDE_KEY, "right"));
    }

    private static FrameLayout.LayoutParams buttonParams(Activity activity, boolean left) {
        int size = dp(activity, 54);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.gravity = (left ? Gravity.START : Gravity.END) | Gravity.BOTTOM;
        if (left) params.leftMargin = dp(activity, 16);
        else params.rightMargin = dp(activity, 16);
        params.bottomMargin = dp(activity, 146);
        return params;
    }

    private static ImageButton createButton(Activity activity, FrameLayout root, boolean left) {
        int iconId = activity.getResources().getIdentifier(
                "shuffle_variant", "drawable", activity.getPackageName());
        if (iconId == 0) iconId = android.R.drawable.ic_menu_rotate;

        ImageButton button = new ImageButton(activity);
        int size = dp(activity, 54);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(68, 68, 91));
        background.setCornerRadius(size / 2f);
        button.setBackground(background);
        button.setImageResource(iconId);
        button.setColorFilter(Color.WHITE);
        button.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        button.setPadding(dp(activity, 14), dp(activity, 14), dp(activity, 14), dp(activity, 14));
        button.setElevation(dp(activity, 7));
        button.setContentDescription("お気に入りからランダムに作品を開く");
        button.setOnClickListener(ignored -> openRandom(button));
        button.setVisibility(View.GONE);

        root.addView(button, buttonParams(activity, left));
        button.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {}

            @Override
            public void onViewDetachedFromWindow(View view) {
                if (shortcutButton.get() == view) {
                    shortcutButton.clear();
                    viewModel = null;
                    showDetail = null;
                    onFavorites = false;
                    detailVisible = false;
                    detailNavigation = null;
                    currentActivity.clear();
                    openingRandom = false;
                }
            }
        });
        return button;
    }

    private static void openRandom(ImageButton button) {
        boolean inDetail = detailVisible && detailNavigation != null;
        Object model = viewModel;
        Function1 destination = inDetail ? detailNavigation : showDetail;
        if (openingRandom || destination == null || (!inDetail && (!onFavorites || model == null))) return;

        openingRandom = true;
        button.setEnabled(false);
        try {
            Object galleryId;
            if (inDetail) {
                // Gallery details can be opened from tabs other than Favorites.
                // Read Pupil's saved Favorites rather than relying on a tab ViewModel.
                Object store = Class.forName("xyz.quaver.pupil.PupilKt")
                        .getMethod("getFavorites").invoke(null);
                Object items = store.getClass().getMethod("getItems").invoke(store);
                List<?> favorites = (List<?>) items;
                galleryId = favorites.isEmpty() ? null
                        : favorites.get(ThreadLocalRandom.current().nextInt(favorites.size()));
            } else {
                galleryId = model.getClass().getMethod("randomGalleryID").invoke(model);
            }
            if (galleryId instanceof Integer) {
                if (!inDetail) hideForDetail();
                destination.invoke(galleryId);
            }
        } catch (ReflectiveOperationException | RuntimeException error) {
            if (!inDetail) detailVisible = false;
            refreshVisibility();
            Toast.makeText(button.getContext(), "ランダム表示を開けませんでした", Toast.LENGTH_SHORT).show();
        } finally {
            openingRandom = false;
            button.setEnabled(true);
        }
    }

    private static void refreshVisibility() {
        ImageButton button = shortcutButton.get();
        if (button == null) return;
        boolean visible = !detailVisible && onFavorites
                && viewModel != null && showDetail != null;
        button.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private static int dp(Context activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
