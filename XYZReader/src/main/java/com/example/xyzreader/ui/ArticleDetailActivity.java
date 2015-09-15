package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = ArticleDetailActivity.class.getCanonicalName();
    private Cursor mCursor;
    private ImageView mPhotoView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton mFab;
    private long mItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mItemId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }

        setContentView(R.layout.fragment_article_detail);

        getLoaderManager().initLoader(0, null, this);

        mPhotoView = (ImageView) findViewById(R.id.photo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.setTransitionName(getString(R.string.transition_article_image));
            postponeEnterTransition();
        }

        findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(
                                ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                                        .setType("text/plain")
                                        .setText("Some sample text")
                                        .getIntent(), getString(R.string.action_share))
                );
            }
        });


        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mFab = (FloatingActionButton) findViewById(R.id.share_fab);

        bindViews();
    }

    private void bindViews() {
        TextView titleView = (TextView) findViewById(R.id.article_title);
        TextView bodyView = (TextView) findViewById(R.id.article_body);

        if (mCursor != null) {
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            collapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            Picasso.with(getApplicationContext())
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .fit()
                    .centerCrop()
                    .into(mPhotoView, new Callback() {
                        @Override
                        public void onSuccess() {
                            scheduleStartPostponedTransition(mPhotoView);
                        }

                        @Override
                        public void onError() {

                        }
                    });
        } else {
            titleView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    private void scheduleStartPostponedTransition(final View sharedElement) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sharedElement.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                            startPostponedEnterTransition();
                            return true;
                        }
                    });
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(this, mItemId);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

}
