package com.github.st1hy.countthemcalories.core.tokensearch;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.core.Utils;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public class TokenSearchView extends FrameLayout implements Searchable {

    TokenSearchTextView searchView;
    View expand;
    View collapse;
    private final Utils utils = new Utils();

    boolean isExpanded = false;

    public TokenSearchView(Context context) {
        super(context);
        init();
    }

    public TokenSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TokenSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public TokenSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (isInEditMode()) return;
        addView(inflateView(LayoutInflater.from(getContext())));
        expand = findViewById(R.id.token_search_expand);
        collapse = findViewById(R.id.token_search_collapse);
        searchView = findViewById(R.id.token_search_text_view);
        expand.setOnClickListener(view -> onExpandClicked());
        collapse.setOnClickListener(view -> onCollapseClicked());
        setupState();
    }

    protected View inflateView(@NonNull LayoutInflater inflater) {
        return inflater.inflate(R.layout.token_search_view, this, false);
    }

    public void expand(boolean requestFocus) {
        isExpanded = true;
        expand.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.VISIBLE);
        if (ViewCompat.isAttachedToWindow(searchView) && utils.hasLollipop()) {
            animateReveal(RevealIntent.NORMAL);
        } else {
            collapse.setVisibility(View.VISIBLE);
        }
        if (requestFocus) searchView.requestFocus();
        InputMethodManager imm = (InputMethodManager) searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(searchView, 0);
    }

    public void collapse() {
        isExpanded = false;
        expand.setVisibility(View.VISIBLE);
        collapse.setVisibility(View.INVISIBLE);
        if (ViewCompat.isAttachedToWindow(searchView) && utils.hasLollipop()) {
            animateReveal(RevealIntent.REVERSE);
        } else {
            searchView.setVisibility(INVISIBLE);
        }
        InputMethodManager imm = (InputMethodManager) searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    @TargetApi(21)
    private void animateReveal(RevealIntent intent) {
        int cx = searchView.getWidth() - expand.getWidth() / 2;
        int cy = searchView.getHeight() / 2;
        int startRadius = 0;
        int finalRadius = Math.max(searchView.getWidth(), searchView.getHeight());
        if (intent == RevealIntent.REVERSE) {
            int temp = startRadius;
            startRadius = finalRadius;
            finalRadius = temp;
        }
        Animator anim = ViewAnimationUtils.createCircularReveal(searchView, cx, cy, startRadius, finalRadius);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (intent == RevealIntent.NORMAL) {
                    collapse.setVisibility(View.VISIBLE);
                } else {
                    searchView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.start();
    }

    private enum RevealIntent {
        NORMAL, REVERSE
    }

    public void onFilterComplete(int count) {
        searchView.onFilterComplete(count);
    }

    protected void onExpandClicked() {
        expand(true);
    }

    protected void onCollapseClicked() {
        if (hasText()) {
            clearText();
        } else {
            collapse();
        }
    }

    public void setQuery(String query) {
        setQuery(query, Collections.emptyList());
    }

    public void clearText() {
        searchView.clear();
        searchView.getText().clear();
    }

    @Override
    public void setOnSearchChanged(@Nullable OnSearchChanged onSearchChanged) {
        searchView.setOnSearchChanged(onSearchChanged);
    }

    @NonNull
    @Override
    public String getQuery() {
        return searchView.getQuery();
    }

    @NonNull
    @Override
    public List<String> getTokens() {
        return searchView.getTokens();
    }

    @NonNull
    @CheckResult
    public Observable<Boolean> dropDownChange() {
        return RxTokenSearchTextView.dropDownDialogChange(searchView);
    }

    public void dismissDropDown() {
        searchView.dismissDropDown();
    }

    public boolean hasText() {
        return !searchView.getText().toString().isEmpty();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        isExpanded = savedState.isExpanded;
        setupState();
    }

    protected void setupState() {
        if (isExpanded) expand(false);
        else collapse();
    }

    public void setQuery(String query, List<String> tokens) {
        clearText();
        searchView.addAll(tokens);
        searchView.append(query);
    }

    @NonNull
    public TokenSearchTextView getSearchTextView() {
        return searchView;
    }

    private static class SavedState extends BaseSavedState {
        private boolean isExpanded;

        SavedState(Parcel source) {
            super(source);
            isExpanded = source.readInt() > 0;
        }

        SavedState(Parcelable superState, TokenSearchView view) {
            super(superState);
            isExpanded = view.isExpanded;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isExpanded ? 1 : 0);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
