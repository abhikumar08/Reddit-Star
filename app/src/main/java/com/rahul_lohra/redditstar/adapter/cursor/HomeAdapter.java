package com.rahul_lohra.redditstar.adapter.cursor;

import android.app.Activity;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rahul_lohra.redditstar.R;
import com.rahul_lohra.redditstar.contract.IFrontPageAdapter;
import com.rahul_lohra.redditstar.contract.ILogin;
import com.rahul_lohra.redditstar.helper.ItemTouchHelperAdapter;
import com.rahul_lohra.redditstar.modal.custom.DetailPostModal;
import com.rahul_lohra.redditstar.storage.column.MyPostsColumn;
import com.rahul_lohra.redditstar.Utility.Constants;
import com.rahul_lohra.redditstar.Utility.Share;
import com.rahul_lohra.redditstar.Utility.UserState;
import com.rahul_lohra.redditstar.viewHolder.CursorRecyclerViewAdapter;
import com.rahul_lohra.redditstar.viewHolder.PostView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import static com.rahul_lohra.redditstar.Utility.Constants.updateLikes;
import static com.rahul_lohra.redditstar.viewHolder.PostView.DIRECTION_NULL;

/**
 * Created by rkrde on 29-01-2017.
 */

public class HomeAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> implements
        ItemTouchHelperAdapter {

    private Activity activity;
    private final String TAG = HomeAdapter.class.getSimpleName();
    private IFrontPageAdapter iFrontPageAdapter;
    private ILogin iLogin;
    public HomeAdapter(Activity activity, Cursor cursor, IFrontPageAdapter iFrontPageAdapter,ILogin iLogin) {
        super(activity, cursor);
        this.activity = activity;
        this.iFrontPageAdapter = iFrontPageAdapter;
        this.iLogin = iLogin;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        final PostView postView = (PostView) holder;

//        final FrontPageChildData frontPageChildData = list.get(position).getData();
        final String sqlId = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_SQL_ID));
        final String id = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_ID));
        final String subreddit = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_SUBREDDIT));
        final String subredditId = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_SUBREDDIT_ID));

        final String name = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_NAME));
        final String author = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_AUTHOR));
        final long createdUtc = cursor.getLong(cursor.getColumnIndex(MyPostsColumn.KEY_CREATED_UTC));
        final String time = Constants.getTimeDiff(createdUtc);
        final String ups = String.valueOf(cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_UPS)));
        final String title = String.valueOf(cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_TITLE)));
        final String commentsCount = String.valueOf(cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_COMMENTS_COUNT)));
//        final Preview preview = frontPageChildData.getPreview();
        final String thumbnail = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_THUMBNAIL));
        final String url = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_URL));
        final Integer likes = cursor.getInt(cursor.getColumnIndex(MyPostsColumn.KEY_LIKES));
        final String bigImageUrl = cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_BIG_IMAGE_URL));
        final String postHint =  cursor.getString(cursor.getColumnIndex(MyPostsColumn.KEY_POST_HINT));

        postView.setLikes(likes);
        if(cursor.isLast()){
            EventBus.getDefault().post("getNextData");
        }



        postView.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailPostModal modal = new DetailPostModal(id,
                        subreddit,ups,title,commentsCount,thumbnail,time,author,bigImageUrl,likes,name,postHint);
                iFrontPageAdapter.sendData(modal,postView.imageView,id);
            }
        });

        postView.imageUpVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String thingId = name;
                boolean loggedIn = UserState.isUserLoggedIn(activity);

                if (!loggedIn) {
                    iLogin.pleaseLogin();
                    return;
                }

                Integer mLikes = postView.getLikes();
                    if(mLikes == -1)
                    {
                        postView.performVoteAndUpdateLikes(DIRECTION_NULL,thingId);
                        updateLikes(activity.getApplicationContext(),DIRECTION_NULL,id,ups,mLikes);

                }else if(mLikes == 0) {
                    //upvote
                    postView.performVoteAndUpdateLikes(PostView.DIRECTION_UP,thingId);
                    updateLikes(activity.getApplicationContext(),PostView.DIRECTION_UP,id,ups,mLikes);
                }

            }
        });

//        postView.imageView.setOnClickListener((View v)->{});

        postView.imageDownVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                boolean loggedIn = UserState.isUserLoggedIn(activity);
                String thingId = name;
                if (!loggedIn) {
                    iLogin.pleaseLogin();
//                    Toast.makeText(activity, activity.getString(R.string.please_login), Toast.LENGTH_SHORT).show();
                    return;
                }

                Integer mLikes = postView.getLikes();
                    if(mLikes==1)
                    {
                        postView.performVoteAndUpdateLikes(DIRECTION_NULL,thingId);
                        updateLikes(activity.getApplicationContext(), DIRECTION_NULL,id,ups,mLikes);
                }else if(mLikes==0){
                    postView.performVoteAndUpdateLikes(PostView.DIRECTION_DOWN,thingId);
                    updateLikes(activity.getApplicationContext(),PostView.DIRECTION_DOWN,id,ups,mLikes);
                }
            }
        });

        postView.tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Share share = new Share();
                share.shareUrl(activity,url);
            }
        });

        //set Textual Data

        if (thumbnail != null) {
            if(thumbnail.equals("default")){
                Glide.with(activity)
                        .load("")
                        .centerCrop()
                        .crossFade()
                        .placeholder(R.drawable.ic_reddit)
                        .into(postView.imageView);
            }else {
                Glide.with(activity)
                        .load(thumbnail)
                        .centerCrop()
                        .crossFade()
                        .into(postView.imageView);
            }

        }else {
            postView.imageView.setVisibility(View.GONE);
        }

        postView.tvVote.setText(ups);
        postView.tvTitle.setText(subreddit);
        postView.tvDetail.setText(title);
        postView.tvComments.setText(commentsCount);


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PostView(activity,LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_posts, parent, false));
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        Log.d(TAG,"from:"+fromPosition+",to:"+toPosition);
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
//                context.getContentResolver().
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
//                Collections.swap(mItems, i, i - 1);
            }
        }
    }

    @Override
    public void onItemDismiss(int position) {
        //NO USE unless swipe is already disabled
    }

}
