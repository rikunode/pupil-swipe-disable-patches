package dev.rikunode.pupil;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class FavoritesRandomShortcut {
    private static final String FAVORITES_VIEW_MODEL =
            "xyz.quaver.pupil.ui.viewmodel.FavoritesViewModel";
    private static Object viewModel;
    private static Function1 showDetail;
    private static WeakReference<ImageButton> shortcutButton = new WeakReference<>(null);
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
        if (!onFavorites || !previouslyFavorites) detailVisible = false;
        refreshVisibility();
    }

    public static void hideForDetail() {
        detailVisible = true;
        refreshVisibility();
    }

    public static Function0 wrapOnDismiss(Function0 onDismiss) {
        if (onDismiss == null) return null;
        return () -> {
            Object result = onDismiss.invoke();
            detailVisible = false;
            refreshVisibility();
            return result;
        };
    }

    public static void install(Activity activity) {
        FrameLayout root = activity.findViewById(android.R.id.content);
        if (root == null) return;

        ImageButton existing = shortcutButton.get();
        if (existing != null && existing.getParent() == root) {
            refreshVisibility();
            return;
        }

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

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.rightMargin = dp(activity, 16);
        params.bottomMargin = dp(activity, 92);
        root.addView(button, params);
        shortcutButton = new WeakReference<>(button);
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
                }
            }
        });
        refreshVisibility();
    }

    private static void openRandom(ImageButton button) {
        Object model = viewModel;
        Function1 detail = showDetail;
        if (!onFavorites || detailVisible || model == null || detail == null) return;

        button.setEnabled(false);
        try {
            Object galleryId = model.getClass().getMethod("randomGalleryID").invoke(model);
            if (galleryId instanceof Integer) {
                hideForDetail();
                detail.invoke(galleryId);
            }
        } catch (ReflectiveOperationException | RuntimeException error) {
            detailVisible = false;
            refreshVisibility();
            Toast.makeText(button.getContext(), "ランダム表示を開けませんでした", Toast.LENGTH_SHORT).show();
        } finally {
            button.setEnabled(true);
        }
    }

    private static void refreshVisibility() {
        ImageButton button = shortcutButton.get();
        if (button != null) {
            button.setVisibility(onFavorites && !detailVisible && viewModel != null && showDetail != null
                    ? View.VISIBLE : View.GONE);
        }
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
