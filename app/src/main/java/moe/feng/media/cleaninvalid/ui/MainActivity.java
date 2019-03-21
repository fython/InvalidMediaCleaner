package moe.feng.media.cleaninvalid.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import java9.util.stream.StreamSupport;
import moe.feng.media.cleaninvalid.BuildConfig;
import moe.feng.media.cleaninvalid.R;
import moe.feng.media.cleaninvalid.loader.InvalidImagesLoader;
import moe.feng.media.cleaninvalid.model.MediaItem;
import moe.feng.media.cleaninvalid.task.CleanFilesTask;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends Activity {

    private static void DEBUG_LOG(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EXTRA_STATE = TAG + ".extra.STATE";
    private static final String EXTRA_ITEMS = TAG + ".extra.ITEMS";

    private static final int STATE_NORMAL = 0;
    private static final int STATE_SCANNING = 1;
    private static final int STATE_CHOOSING = 2;
    private static final int STATE_CLEANING = 3;

    private static final int FIRST_STEP_VISIBILITY[] = {VISIBLE, GONE, GONE, GONE};
    private static final int PROGRESS_VISIBILITY[] = {GONE, VISIBLE, GONE, VISIBLE};
    private static final int LIST_VISIBILITY[] = {GONE, GONE, VISIBLE, GONE};
    private static final int RESET_BUTTON_VISIBILITY[] = {GONE, GONE, VISIBLE, GONE};

    private View mToolbar;
    private RecyclerView mRecyclerView;
    private View mFirstStepView, mProgressView, mEmptyView, mListContainer;
    private TextView mActionButton;
    private View mResetButton, mHelpButton;
    private TextView mProgressText;

    private MediaItemsAdapter mAdapter;

    private int mState = STATE_NORMAL;
    @NonNull
    private ArrayList<MediaItem> mItems = new ArrayList<>();

    private InvalidImagesLoader mInvalidLoader;
    @Nullable
    private CleanFilesTask mCleanFilesTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupStatusBarUi();
        setContentView(R.layout.activity_main);

        // Find views
        mToolbar = findViewById(R.id.toolbar);
        mRecyclerView = findViewById(android.R.id.list);
        mFirstStepView = findViewById(R.id.first_step_view);
        mProgressView = findViewById(R.id.progress_view);
        mActionButton = findViewById(R.id.action_button);
        mEmptyView = findViewById(R.id.empty_view);
        mListContainer = findViewById(R.id.list_container);
        mResetButton = findViewById(R.id.reset_button);
        mProgressText = findViewById(R.id.progress_text);
        mHelpButton = findViewById(R.id.help_button);

        final TextView firstStepText = findViewById(R.id.first_step_text);
        firstStepText.setText(HtmlCompat.fromHtml(getString(R.string.first_step_tips), 0));

        setupRecyclerView();
        setupViewCallbacks();

        mInvalidLoader = new InvalidImagesLoader(this);
        mInvalidLoader.registerListener(0, (loader, data) -> {
            if (data == null) {
                // TODO Show error in UI
                mState = STATE_NORMAL;
                updateViewsByState();
                return;
            }
            if (mState != STATE_SCANNING) {
                mState = STATE_NORMAL;
                updateViewsByState();
                return;
            }

            mItems = new ArrayList<>(data);
            mState = STATE_CHOOSING;
            updateViewsByState();
        });

        if (savedInstanceState != null) {
            mState = savedInstanceState.getInt(EXTRA_STATE);
            ArrayList<MediaItem> items = savedInstanceState.getParcelableArrayList(EXTRA_ITEMS);
            if (items == null) {
                items = new ArrayList<>();
            }
            mItems = items;
        } else {
            mState = STATE_NORMAL;
            mItems.clear();
        }

        updateViewsByState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_STATE, mState);
        outState.putParcelableArrayList(EXTRA_ITEMS, mItems);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInvalidLoader != null && mInvalidLoader.isStarted()) {
            mInvalidLoader.cancelLoad();
        }
        if (mCleanFilesTask != null && !mCleanFilesTask.isCancelled()) {
            mCleanFilesTask.cancel(true);
        }
    }

    private void setupStatusBarUi() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            uiFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    private void setupRecyclerView() {
        if (mAdapter == null) {
            mAdapter = new MediaItemsAdapter();
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    mEmptyView.setVisibility(mAdapter.getItemCount() == 0 ? VISIBLE : GONE);
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    onChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount,
                                               @Nullable Object payload) {
                    onChanged();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    onChanged();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    onChanged();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    onChanged();
                }
            });
        }
        mRecyclerView.setAdapter(mAdapter);
        final List<MediaItem> items = new ArrayList<>();
        mAdapter.submitList(items);
    }

    private void setupViewCallbacks() {
        final View rootView = findViewById(R.id.root_view);
        rootView.setOnApplyWindowInsetsListener((v, insets) -> {
            Log.i(TAG, insets.toString());
            final int windowInsetTop = insets.getSystemWindowInsetTop();
            final int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_height);
            mRecyclerView.setPadding(0, toolbarHeight + windowInsetTop, 0, 0);
            mToolbar.setPadding(0, windowInsetTop, 0, 0);

            final ViewGroup.MarginLayoutParams fsvLayoutParams =
                    (ViewGroup.MarginLayoutParams) mFirstStepView.getLayoutParams();
            fsvLayoutParams.topMargin = toolbarHeight + windowInsetTop;

            return insets.consumeSystemWindowInsets();
        });
        mActionButton.setOnClickListener(v -> onActionButtonClick());
        mRecyclerView.addOnScrollListener(new RaisedViewScrollListener(mToolbar));
        mResetButton.setOnClickListener(v -> {
            mState = STATE_NORMAL;
            updateViewsByState();
            mItems.clear();
        });
        mHelpButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/fython/InvalidMediaCleaner"));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    private void setActionButton(@DrawableRes int iconRes, @StringRes int textRes) {
        mActionButton.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        mActionButton.setText(textRes);
    }

    private void onActionButtonClick() {
        switch (mState) {
            case STATE_NORMAL: {
                if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                    requestPermissions(new String[] {
                            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
                    }, 0);
                    return;
                }
                mState = STATE_SCANNING;
                mInvalidLoader.startLoading();
                break;
            }
            case STATE_SCANNING: {
                mInvalidLoader.cancelLoad();
                mState = STATE_NORMAL;
                updateViewsByState();
                break;
            }
            case STATE_CHOOSING: {
                int count = (int) StreamSupport.stream(mItems)
                        .filter(item -> item.isChecked)
                        .count();
                if (count != 0) {
                    ConfirmCleanDialog
                            .newInstance(count)
                            .show(getFragmentManager(), "confirm_clean");
                }
                break;
            }
            case STATE_CLEANING: {
                if (mCleanFilesTask != null && !mCleanFilesTask.isCancelled()) {
                    mCleanFilesTask.cancel(true);
                }
                mState = STATE_NORMAL;
                updateViewsByState();
                break;
            }
            default: {
                throw new IllegalStateException("Unsupported state = " + mState);
            }
        }
        updateViewsByState();
    }

    private void updateViewsByState() {
        switch (mState) {
            case STATE_NORMAL: {
                mToolbar.setElevation(0);
                setActionButton(R.drawable.ic_image_search_twotone_24dp, R.string.action_scan);
                break;
            }
            case STATE_SCANNING: {
                mToolbar.setElevation(0);
                mProgressText.setText(R.string.progress_text_scanning);
                setActionButton(R.drawable.ic_stop_twotone_24dp, R.string.action_stop);
                break;
            }
            case STATE_CHOOSING: {
                mAdapter.submitList(new ArrayList<>(mItems));
                setActionButton(R.drawable.ic_delete_forever_twotone_24dp, R.string.action_clean);
                break;
            }
            case STATE_CLEANING: {
                mToolbar.setElevation(0);
                mProgressText.setText(R.string.progress_text_cleaning);
                setActionButton(R.drawable.ic_stop_twotone_24dp, R.string.action_stop);
                break;
            }
            default: {
                throw new IllegalStateException("Unsupported state = " + mState);
            }
        }
        mListContainer.setVisibility(LIST_VISIBILITY[mState]);
        mFirstStepView.setVisibility(FIRST_STEP_VISIBILITY[mState]);
        mProgressView.setVisibility(PROGRESS_VISIBILITY[mState]);
        mResetButton.setVisibility(RESET_BUTTON_VISIBILITY[mState]);
    }

    private void startCleaning() {
        if (mState != STATE_CHOOSING) {
            return;
        }
        mCleanFilesTask = new CleanFilesTask(this, this::onFinishedClean);
        //noinspection unchecked
        mCleanFilesTask.execute(StreamSupport.stream(mItems).filter(item -> item.isChecked));
        mState = STATE_CLEANING;
        updateViewsByState();
    }

    private void onFinishedClean(int deletedCount) {
        FinishedCleanDialog.newInstance(deletedCount)
                .show(getFragmentManager(), "finished_clean");
        mState = STATE_NORMAL;
        updateViewsByState();
    }

    public static class ConfirmCleanDialog extends DialogFragment {

        private static final String EXTRA_COUNT = "count";

        public static ConfirmCleanDialog newInstance(int itemCount) {
            final ConfirmCleanDialog dialog = new ConfirmCleanDialog();
            final Bundle arguments = new Bundle();
            arguments.putInt(EXTRA_COUNT, itemCount);
            dialog.setArguments(arguments);
            return dialog;
        }

        private int mCount = 0;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null) {
                mCount = getArguments().getInt(EXTRA_COUNT);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_confirm_clean_title)
                    .setMessage(getString(R.string.dialog_confirm_clean_message, mCount))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        final MainActivity activity = (MainActivity) getActivity();
                        activity.startCleaning();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        }

    }

    public static class FinishedCleanDialog extends DialogFragment {

        private static final String EXTRA_COUNT = "count";

        public static FinishedCleanDialog newInstance(int itemCount) {
            final FinishedCleanDialog dialog = new FinishedCleanDialog();
            final Bundle arguments = new Bundle();
            arguments.putInt(EXTRA_COUNT, itemCount);
            dialog.setArguments(arguments);
            return dialog;
        }

        private int mCount = 0;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null) {
                mCount = getArguments().getInt(EXTRA_COUNT);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_finished_clean_title)
                    .setMessage(getString(R.string.dialog_finished_clean_message, mCount))
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

    }

}
