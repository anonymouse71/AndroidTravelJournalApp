package com.mycompany.traveljournal.commentscreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mycompany.traveljournal.R;
import com.mycompany.traveljournal.datasource.ParseClient;

import java.util.ArrayList;

public class CommentActivity extends ActionBarActivity implements CommentFragment.NewCommentListenerInterface {

    private static final String TAG = "CommentActivity";
    String postId;
    ParseClient parseClient;
    private boolean newCommentCreated;
    private int numNewComments;
    private ArrayList<String> newComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Get Post ID
        postId = getIntent().getStringExtra("post_id");

        if (postId == null) {
            Log.wtf(TAG, "Using default post id");
            postId = "8nxq1SkIUo";
        }


        // Init Parse
        parseClient = ParseClient.getInstance(this);

        if(savedInstanceState == null) {
            setUpFragment();
        }

        newCommentCreated = false;
        numNewComments = 0;
        newComments = new ArrayList<>();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                doneWithActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setUpFragment() {
        CommentFragment commentFragment =  CommentFragment.newInstance(postId);
        commentFragment.setListener(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flContainer, commentFragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        doneWithActivity();
        super.onBackPressed();
    }

    private void doneWithActivity() {
        Intent data = new Intent();
        data.putExtra("new_comment_created", newCommentCreated);
        data.putExtra("num_new_comments", numNewComments);
        setResult(RESULT_OK, data);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void commentCreated(String body) {
        newCommentCreated = true;
        numNewComments += 1;
        newComments.add(body);
    }
}
