package com.mycompany.traveljournal.datasource;


import android.content.Context;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.mycompany.traveljournal.helpers.Util;
import com.mycompany.traveljournal.models.Comment;
import com.mycompany.traveljournal.models.Like;
import com.mycompany.traveljournal.models.Post;
import com.mycompany.traveljournal.models.User;
import com.mycompany.traveljournal.service.JournalCallBack;
import com.mycompany.traveljournal.service.JournalService;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;


public class ParseClient implements JournalService {

    private static final String TAG = "ParseClient";
    private static ParseClient instance = null;

    private ParseUser parseUser;

    protected ParseClient() {

    }

    /**
     *  Get a the singleton ParseClient instance
     */
    public static ParseClient getInstance(Context context) {
        if (instance == null) {
            instance = new ParseClient();

            // Automatically initialize
            instance.init(context);
        }
        return instance;
    }

    public void init(Context context) {

        // Enable Local Datastore
        Parse.enableLocalDatastore(context);

        // Register Parse sublasses
        //ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Like.class);
        ParseObject.registerSubclass(Comment.class);

        Parse.initialize(context, "ZFoSsZ6iQBe1CvJaNqio6V0nmlN4V7U4VzboX4J4", "0GDxAZahVe7ibC6pqiMNK6n91fYoh7HRfxXLo5TK");
        ParseUser.enableRevocableSessionInBackground();
        FacebookSdk.sdkInitialize(context);
        ParseFacebookUtils.initialize(context);
    }

    //TODO
    public void createPost(byte[] imageBytes, String caption, String description, double latitude, double longitude, final JournalCallBack<List<Post>> journalCallBack) {
        //PostCreator postCreator = new PostCreator();
        //postCreator.createPost();

    }

    public void getPostWithId(String postId, final JournalCallBack<List<Post>> journalCallBack) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.whereEqualTo("objectId", postId);
        query.setLimit(1);
        query.include("parse_user");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> resultPosts, ParseException e) {
                if (e == null) {
                    journalCallBack.onSuccess(resultPosts);
                } else {
                    journalCallBack.onFailure(e);
                }
            }
        });
    }

    public void getPostsNearLocation(double latitude, double longitude, int limit, final JournalCallBack<List<Post>> journalCallBack) {
        ParseGeoPoint userLocation = new ParseGeoPoint(latitude, longitude);
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.whereNear("location", userLocation);
        query.setLimit(limit);
        query.include("parse_user");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> resultPosts, ParseException e) {
                if (e == null) {
                    journalCallBack.onSuccess(resultPosts);
                } else {
                    journalCallBack.onFailure(e);
                }
            }
        });
    }

    public void createUser(String name, String profile_image_url, String cover_image_url) {
        final ParseUser user = ParseUser.getCurrentUser();
        user.put("name", name);
        user.put("profile_image_url", profile_image_url);
        user.put("cover_image_url", cover_image_url);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.wtf(TAG, "Succesfully saved user");
                } else {
                    Log.wtf(TAG, "Failed to save user" + e.toString());
                }
            }
        });
    }

    @Override
    public User getCurrentUser() {
        return Util.getUserFromParseUser(ParseUser.getCurrentUser());
    }

    @Override
    public void getPostsForUser(String userId, Date createdAt, int limit, final JournalCallBack<List<Post>> journalCallBack) {

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery("_User");
        userQuery.whereEqualTo("objectId", userId);
        query.whereMatchesQuery("parse_user", userQuery);
        if(createdAt!=null){
            //if we pass a timestamp, query the posts older than timestamp
            //otherwise query everything sorted by timestamp
            query.whereLessThan("createdAt", createdAt);
        }
        query.orderByDescending("createdAt");
        query.setLimit(limit);
        query.include("parse_user");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> resultPosts, ParseException e) {
                if (e == null) {
                    journalCallBack.onSuccess(resultPosts);
                } else {
                    journalCallBack.onFailure(e);
                }
            }
        });

    }

    public void getPostsWithinWindow(double latitudeMin, double longitudeMin, double latitudeMax, double longitudeMax, int limit, final JournalCallBack<List<Post>> journalCallBack) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.setLimit(limit);
        ParseGeoPoint swPoint = new ParseGeoPoint(latitudeMin, longitudeMin);
        ParseGeoPoint nePoint = new ParseGeoPoint(latitudeMax, longitudeMax);
        query.whereWithinGeoBox("location", swPoint, nePoint);
        query.include("parse_user");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> resultPosts, ParseException e) {
                if (e == null) {
                    journalCallBack.onSuccess(resultPosts);
                } else {
                    journalCallBack.onFailure(e);
                }
            }
        });
    }

    public void getRecentPosts(Date createdAt, int limit, final JournalCallBack<List<Post>> journalCallBack){

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        if(createdAt!=null){
            //if we pass a timestamp, query the posts older than timestamp
            //otherwise query everything sorted by timestamp
            query.whereLessThan("createdAt", createdAt);
        }
        query.orderByDescending("createdAt");
        query.setLimit(limit);
        query.include("parse_user");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> resultPosts, ParseException e) {
                if (e == null) {
                    journalCallBack.onSuccess(resultPosts);
                } else {
                    journalCallBack.onFailure(e);
                }
            }
        });
    }

    public void getPostsNearLocationOrderByDate(Date createdAt, double latitude, double longitude, int limit, final JournalCallBack<List<Post>> journalCallBack) {
        ParseGeoPoint userLocation = new ParseGeoPoint(latitude, longitude);
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.whereNear("location", userLocation);
        if(createdAt!=null){
            //if we pass a timestamp, query the posts older than timestamp
            //otherwise query everything sorted by timestamp
            query.whereLessThan("createdAt", createdAt);
        }
        query.orderByDescending("createdAt");
        query.setLimit(limit);
        query.include("parse_user");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> resultPosts, ParseException e) {
                if (e == null) {
                    journalCallBack.onSuccess(resultPosts);
                } else {
                    journalCallBack.onFailure(e);
                }
            }
        });
    }

    public void getPostsWithinMilesOrderByDate(Date createdAt, int maxDistance, double latitude, double longitude, int limit, final JournalCallBack<List<Post>> journalCallBack) {
        ParseGeoPoint userLocation = new ParseGeoPoint(latitude, longitude);
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.whereWithinMiles("location", userLocation, maxDistance);
        if(createdAt!=null){
            //if we pass a timestamp, query the posts older than timestamp
            //otherwise query everything sorted by timestamp
            query.whereLessThan("createdAt", createdAt);
        }
        query.orderByDescending("createdAt");
        query.setLimit(limit);
        query.include("parse_user");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> resultPosts, ParseException e) {
                if (e == null) {
                    journalCallBack.onSuccess(resultPosts);
                } else {
                    journalCallBack.onFailure(e);
                }
            }
        });
    }

    public void getUserWithId(String userId, final JournalCallBack<List<User>> journalCallBack) {
        /*ParseQuery<User> query = ParseQuery.getQuery(User.class);
        query.whereEqualTo("user_id", userId);
        query.setLimit(1);
        query.findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> resultUsers, ParseException e) {
                if (e == null) {
                    journalCallBack.onSuccess(resultUsers);
                } else {
                    journalCallBack.onFailure(e);
                }
            }
        });*/
    }

    public void createComment(Post post, String body) {
        Comment comment = new Comment();

        comment.put("post_id", post.getPostID());
        comment.put("parse_user", ParseUser.getCurrentUser());
        comment.put("body", body);

        comment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.wtf(TAG, "Successfully saved comment");
                } else {
                    Log.wtf(TAG, "Failed to save comment"+e.toString());
                }
            }
        });

        // Increment number of comments
        post.increment("num_comments");
        post.saveInBackground();
    }

    /**
     * This method is to change the author of a comment programmatically. It is
     * needed as it seems that the user pointer can't be modified from the Parse UI
     */
    public void updateCommentUser(final String commentId, String userId) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.getInBackground(userId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser curParseUser, ParseException e) {

                if (e != null) {
                    Log.wtf(TAG, "Couldnt get parse user: "+e.toString());
                }

                // Store the current Parse User
                parseUser = curParseUser;
                Log.wtf(TAG, "parse user name is "+parseUser.getString("name"));

                ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
                query.getInBackground(commentId, new GetCallback<Comment>() {
                    @Override
                    public void done(Comment comment, ParseException e) {
                        comment.put("parse_user", parseUser);
                        comment.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.wtf(TAG, "Success: Changed User for Comment "+commentId);
                                } else {
                                    Log.wtf(TAG, "Problem updating user for comment "+commentId);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

}