

package com.flickrfinal.flickrfinal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.auth.oauth2.Credential;
import com.flickrfinal.apache.FragTabPagerCompat;
import com.flickrfinal.flickrfinal.events.UpdateRequest;
import com.flickrfinal.flickrfinal.oauth.OAuthHandler;
import com.flickrfinal.flickrfinal.retrofit.FlickrClient;
import com.flickrfinal.flickrfinal.retrofit.ServiceGenerator;
import com.flickrfinal.flickrfinal.retrofit.response.username.UsernameResponse;
import com.flickrfinal.flickrfinal.retrofit.response.validate.TokenTest;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.wuman.android.auth.oauth.OAuthHmacCredential;

import retrofit.RetrofitError;
import retrofit.client.Response;

/*
 * Main activity
 */
public class FlickrViewer extends FragTabPagerCompat {
    protected final static String TAG = "FlickrViewer";

    protected OAuthHandler mOauthHandler;
    protected FlickrClient mFlickrClient;
    protected Bus mEventBus;
    protected Dialog mDialog;
    protected String mUserId;


    protected int getLayoutID() {
        return R.layout.frag_tab;
    }

    protected int getTabHostID() {
        return android.R.id.tabhost;
    }

    protected int getPagerID() {
        return R.id.pager;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme here if needed
        super.onCreate(savedInstanceState);

        // layout set through parent via getLayoutID

        // start shared event bus
        mEventBus = FlickrDemo.getBusInstance();

        // Reload saved userId, in case it isn't the default
        mUserId = (String)getLastCustomNonConfigurationInstance();

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setHomeButtonEnabled(true);

        // Add all tabs here
        mTabsAdapter.addTab(mTabHost.newTabSpec(UserFragment.FRAGMENT_TAG).setIndicator(UserFragment.getTitle(this)),
                UserFragment.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec(FavoritesFragment.FRAGMENT_TAG).setIndicator(FavoritesFragment.getTitle(this)),
                FavoritesFragment.class, null);

        // Start data and authorization setup
        mOauthHandler = new OAuthHandler();

//         * Check if  authorized (saved OAuth token)
//         * If not, prompt user to log in
//
        if (checkAndInitialize()) {
            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.i(TAG, "checkAndInitialize done, now retrieving userId");
            }


            if(null == FlickrDemo.getOwnUserId() || null == mUserId) {
                // Find out ID of logged in user
                mFlickrClient.testToken(validateTokenCallback);
            }
        } else {

            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.i(TAG, "checkAndInitialize not initi, NOT loading data for  userId");
            }

            mDialog = onPromptLogin();
        }
    }

    @Override
    public void onDestroy() {
        if(null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        super.onDestroy();
    }

    // Save current userId
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mUserId;
    }

    @Override
    public void onResume() {
        super.onResume();

        mEventBus.register(this);
    }

    @Override
    public void onPause() {
        mEventBus.unregister(this);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_flicker_viewer, menu);


        final boolean isShowingAnother = mUserId != null && mUserId.compareTo(FlickrDemo.getOwnUserId()) != 0;
        MenuItem item = menu.findItem(R.id.action_user);
        if(null != item) {
            item.setVisible(isShowingAnother);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retVal;

        int id = item.getItemId();

        if (id == R.id.action_user) {
            onActionUser();
            retVal = true;
        } else if (id == R.id.action_flickr) {
            onActionFlickr();
            retVal = true;
        } else if (id == R.id.action_search) {
            onActionSearch();
            retVal = true;
        } else if (id == R.id.action_logout) {
            onActionLoginLogout();
            retVal = true;
        } else {
            retVal = super.onOptionsItemSelected(item);
        }

        return retVal;
    }

    // Load our  data
    protected void onActionUser() {
        if(null != FlickrDemo.getOwnUserId()) {
            selectUserId(FlickrDemo.getOwnUserId());
        }
    }

    // Load interestingness
    protected void onActionFlickr() {
        final Intent intent = new Intent(this, PhotosetViewer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(PhotosetViewer.INTENT_EXTRA_PHOTOSET_ID, PhotosetViewer.INTERESTINGNESS_PHOTOSET_ID);
        intent.putExtra(PhotosetViewer.INTENT_EXTRA_USER_ID, PhotosetViewer.INTERESTINGNESS_USER_ID);
        startActivity(intent);
    }

    protected void onActionSearch() {
        onPromptSearch();
    }


    /*
     * Log in or log out
     */
    protected void onActionLoginLogout() {
        if(mFlickrClient == null) {
            OauthLoginTask task = new OauthLoginTask();
            task.execute();
        } else {
            oAuthLogout();
        }
    }

    /*
     * Check if OAuth token
     */
    protected boolean checkAndInitialize() {
        boolean result = mOauthHandler.checkPreviousAuthorization(this, FlickrDemoConstants.SINGLE_USER_ID);

        if(FlickrDemoConstants.DEBUG_ENABLE) {
            Log.i(TAG, "check for OAuth token: " + result);
        }

        if (result) {
            if(!initializeFlickrClient()) {
                result = false;
                if(FlickrDemoConstants.DEBUG_ENABLE) {
                    Log.w(TAG, "Failed to init FlickrClient");
                }
            }
        }

        return result;
    }


    /*
     * Ensue FlickrClient is available, using available credentials
     * Return true if it already exists or is successfully created
     */
    protected boolean initializeFlickrClient() {
        if(null == mFlickrClient) {
            Credential credential = mOauthHandler.getCredential();

            if (credential instanceof OAuthHmacCredential) {
                OAuthHmacCredential hmacCredential = (OAuthHmacCredential) credential;

                mFlickrClient = ServiceGenerator.createServiceSigned(FlickrClient.class,
                        ServiceGenerator.FLICKR_URL_BASE,
                        hmacCredential.getAccessToken(),
                        hmacCredential.getTokenSharedSecret());

                FlickrDemo.setFlickrClient(mFlickrClient);
            } else {
                if (FlickrDemoConstants.DEBUG_ENABLE) {
                    Log.w(TAG, "Incorrect credential ");
                }
            }
        }

        return (null != mFlickrClient);
    }


    private class OauthLoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            boolean success = false;
            try {
                success = mOauthHandler.getOAuthToken(FlickrViewer.this, FlickrDemoConstants.SINGLE_USER_ID);
            } catch (java.util.concurrent.CancellationException e) {
                if(FlickrDemoConstants.DEBUG_ENABLE) {
                    Log.w(TAG, "Failed to get OAuth token due to user cancellation, exiting");
                }
                FlickrViewer.this.finish();
            }
            if(success) {
                if(initializeFlickrClient()) {
                    // Find out ID of logged in user
                    mFlickrClient.testToken(validateTokenCallback);
                }
            } else {
                if(FlickrDemoConstants.DEBUG_ENABLE) {
                    Log.w(TAG, "Failed to get OAuth token");
                }
            }

            return Boolean.toString(success);
        }
    }


    public FlickrClient getFlickrClient() {
        return mFlickrClient;
    }

    protected void oAuthLogout() {
        boolean success = mOauthHandler.removeCredential(FlickrViewer.this, FlickrDemoConstants.SINGLE_USER_ID);
        mFlickrClient = null;

        if(FlickrDemoConstants.DEBUG_ENABLE) {
            Log.w(TAG, "oAuthLogout: " + success);
        }


        mDialog = onPromptLogin();
    }

    protected void findUserById(final String userId) {
        FlickrDemo.getFlickrClient().getUserIdFromName(userId, findUserIdCallback);
    }

    protected void selectUserId(final String userId) {
        if(FlickrDemoConstants.DEBUG_ENABLE) {
            Log.w(TAG, "selectUserId starting");
        }

        mUserId = userId;


        invalidateOptionsMenu();

        mEventBus.post(new UserIdUpdate(userId));
    }

    public class UserIdUpdate {
        protected final String mUserId;

        public UserIdUpdate(final String userId) {
            mUserId = userId;
        }

        public String getUserId() {
            return mUserId;
        }
    }

    @Subscribe
    public void UpdateRequestEvent(UpdateRequest event) {
        if (FlickrDemoConstants.DEBUG_ENABLE) {
            Log.i("UpdateRequestEvent", "Now providing userId: " + mUserId);
        }

        mEventBus.post(new UserIdUpdate(mUserId));
    }


    public Dialog onPromptLogin() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(this.getString(R.string.login_title));
        alert.setMessage(this.getString(R.string.login_message));

        alert.setPositiveButton(this.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        OauthLoginTask task = new OauthLoginTask();
                        task.execute();
                    }
                });

        alert.setNegativeButton(this.getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        FlickrViewer.this.finish();
                    }
                });

        return alert.show();
    }


    public void onPromptSearch() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(this.getString(R.string.dialog_search_title));
        alert.setMessage(this.getString(R.string.dialog_search_message));

        // Text box to enter search term
        final EditText input = new EditText(this);
        input.setSingleLine();
        input.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        alert.setView(input);

        alert.setIcon(R.drawable.ic_action_search_black);

        alert.setPositiveButton(this.getString(android.R.string.search_go),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final String term = input.getText().toString().trim();
                        if (!term.isEmpty()) {
                            findUserById(term);
                        }
                    }
                });

        alert.setNegativeButton(this.getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Hide the keyboard before closing, so text resizing is correct
                        InputMethodManager imm = (InputMethodManager) FlickrViewer.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                        // dialog will close
                    }
                });

        final AlertDialog dialog = alert.show();

        // set this listener afterwards so that it can dismiss the dialog
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                boolean retVal = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    final String term = input.getText().toString().trim(); // trim trailing spaces
                    if (!term.isEmpty()) {
                        findUserById(term);
                    }

                    // Hide the keyboard
                    InputMethodManager imm = (InputMethodManager) FlickrViewer.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                    dialog.dismiss();

                    retVal = true;
                }

                return retVal;
            }
        });
    }

    protected retrofit.Callback findUserIdCallback = new retrofit.Callback() {
        @Override
        public void success(Object o, Response response) {
            UsernameResponse usernameResponse = (UsernameResponse)o;
            if(null == usernameResponse || null == usernameResponse.getUser()) {
                if (FlickrDemoConstants.DEBUG_ENABLE) {
                    Log.i("findUserIdCallback", "No userId found for selected username");
                }
                StaticUtil.displayShortToast(FlickrViewer.this, getString(R.string.search_failed));
            } else {
                selectUserId(usernameResponse.getUser().getNsid());
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.e("findUserIdCallback", "RetrofitError" + retrofitError.getMessage());
            }
        }
    };

    protected retrofit.Callback validateTokenCallback = new retrofit.Callback() {
        @Override
        public void success(Object o, Response response) {
            final TokenTest tokenTest = (TokenTest)o;
            final String userId = tokenTest.getUser().getId();

            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.i("validateTokenCallback", "ID = " + userId);
            }

            FlickrDemo.setOwnUserId(userId);
            selectUserId(userId);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.e("validateTokenCallback", "RetrofitError" + retrofitError.getMessage());
            }
        }
    };
}
