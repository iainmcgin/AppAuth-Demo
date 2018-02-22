/*
 * Copyright 2015 The AppAuth Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openid.appauthdemo.moreidps;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.BoolRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceConfiguration.RetrieveConfigurationCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An abstraction of identity providers, containing all necessary info for the demo app.
 */
class IdentityProvider {

    /**
     * Value used to indicate that a configured property is not specified or required.
     */
    public static final int NOT_SPECIFIED = -1;

    public static final IdentityProvider FACEBOOK = new IdentityProvider(
            "Facebook",
            R.bool.facebook_enabled,
            NOT_SPECIFIED, // facebook does not have an OpenID Connect discovery endpoint
            R.string.facebook_auth_endpoint,
            R.string.facebook_token_endpoint,
            R.string.facebook_client_id,
            R.string.facebook_client_secret,
            R.string.facebook_auth_redirect_uri,
            R.string.facebook_scope_string,
            R.drawable.btn_facebook,
            R.string.facebook_name);

    public static final IdentityProvider GITHUB = new IdentityProvider(
            "GitHub",
            R.bool.github_enabled,
            NOT_SPECIFIED, // github does not have an OpenID Connect discovery endpoint
            R.string.github_auth_endpoint,
            R.string.github_token_endpoint,
            R.string.github_client_id,
            R.string.github_client_secret,
            R.string.github_auth_redirect_uri,
            R.string.github_scope_string,
            R.drawable.btn_github,
            R.string.github_name);

    public static final IdentityProvider GOOGLE = new IdentityProvider(
            "Google",
            R.bool.google_enabled,
            R.string.google_discovery_uri,
            NOT_SPECIFIED, // auth endpoint is discovered
            NOT_SPECIFIED, // token endpoint is discovered
            R.string.google_client_id,
            NOT_SPECIFIED, // client secret is not required for Google
            R.string.google_auth_redirect_uri,
            R.string.google_scope_string,
            R.drawable.btn_google,
            R.string.google_name);

    public static final IdentityProvider MICROSOFT = new IdentityProvider(
            "Microsoft",
            R.bool.microsoft_enabled,
            R.string.microsoft_discovery_uri,
            NOT_SPECIFIED,
            NOT_SPECIFIED,
            R.string.microsoft_client_id,
            NOT_SPECIFIED, // client secret is not required for Microsoft
            R.string.microsoft_auth_redirect_uri,
            R.string.microsoft_scope_string,
            R.drawable.btn_microsoft,
            R.string.microsoft_name);

    public static final List<IdentityProvider> PROVIDERS = Arrays.asList(
            FACEBOOK,
            GITHUB,
            GOOGLE,
            MICROSOFT);

    public static List<IdentityProvider> getEnabledProviders(Context context) {
        ArrayList<IdentityProvider> providers = new ArrayList<>();
        for (IdentityProvider provider : PROVIDERS) {
            provider.readConfiguration(context);
            if (provider.isEnabled()) {
                providers.add(provider);
            }
        }
        return providers;
    }

    @NonNull
    public final String name;

    @DrawableRes
    public final int buttonImageRes;

    @StringRes
    public final int buttonContentDescriptionRes;

    @BoolRes
    private final int mEnabledRes;

    @StringRes
    private final int mDiscoveryEndpointRes;

    @StringRes
    private final int mAuthEndpointRes;

    @StringRes
    private final int mTokenEndpointRes;

    @StringRes
    private final int mClientIdRes;

    @StringRes
    private final int mClientSecretRes;

    @StringRes
    private final int mRedirectUriRes;

    @StringRes
    private final int mScopeRes;

    private boolean mConfigurationRead = false;
    private boolean mEnabled;
    private Uri mDiscoveryEndpoint;
    private Uri mAuthEndpoint;
    private Uri mTokenEndpoint;
    private String mClientId;
    private String mClientSecret;
    private Uri mRedirectUri;
    private String mScope;

    IdentityProvider(
            @NonNull String name,
            @BoolRes int enabledRes,
            @StringRes int discoveryEndpointRes,
            @StringRes int authEndpointRes,
            @StringRes int tokenEndpointRes,
            @StringRes int clientIdRes,
            @StringRes int clientSecretRes,
            @StringRes int redirectUriRes,
            @StringRes int scopeRes,
            @DrawableRes int buttonImageRes,
            @StringRes int buttonContentDescriptionRes) {
        if (!isSpecified(discoveryEndpointRes)
                && !isSpecified(authEndpointRes)
                && !isSpecified(tokenEndpointRes)) {
            throw new IllegalArgumentException(
                    "the discovery endpoint or the auth and token endpoints must be specified");
        }

        this.name = name;
        this.mEnabledRes = checkSpecified(enabledRes, "enabledRes");
        this.mDiscoveryEndpointRes = discoveryEndpointRes;
        this.mAuthEndpointRes = authEndpointRes;
        this.mTokenEndpointRes = tokenEndpointRes;
        this.mClientIdRes = checkSpecified(clientIdRes, "clientIdRes");
        this.mClientSecretRes = clientSecretRes;
        this.mRedirectUriRes = checkSpecified(redirectUriRes, "redirectUriRes");
        this.mScopeRes = checkSpecified(scopeRes, "scopeRes");
        this.buttonImageRes = checkSpecified(buttonImageRes, "buttonImageRes");
        this.buttonContentDescriptionRes =
                checkSpecified(buttonContentDescriptionRes, "buttonContentDescriptionRes");
    }

    /**
     * This must be called before any of the getters will function.
     */
    public void readConfiguration(Context context) {
        if (mConfigurationRead) {
            return;
        }

        Resources res = context.getResources();
        mEnabled = res.getBoolean(mEnabledRes);

        mDiscoveryEndpoint = isSpecified(mDiscoveryEndpointRes)
                ? getUriResource(res, mDiscoveryEndpointRes, "discoveryEndpointRes")
                : null;
        mAuthEndpoint = isSpecified(mAuthEndpointRes)
                ? getUriResource(res, mAuthEndpointRes, "authEndpointRes")
                : null;
        mTokenEndpoint = isSpecified(mTokenEndpointRes)
                ? getUriResource(res, mTokenEndpointRes, "tokenEndpointRes")
                : null;
        mClientId = res.getString(mClientIdRes);
        mClientSecret = isSpecified(mClientSecretRes) ? res.getString(mClientSecretRes) : null;
        mRedirectUri = getUriResource(res, mRedirectUriRes, "mRedirectUriRes");
        mScope = res.getString(mScopeRes);

        mConfigurationRead = true;
    }

    private void checkConfigurationRead() {
        if (!mConfigurationRead) {
            throw new IllegalStateException("Configuration not read");
        }
    }

    public boolean isEnabled() {
        checkConfigurationRead();
        return mEnabled;
    }

    @Nullable
    public Uri getDiscoveryEndpoint() {
        checkConfigurationRead();
        return mDiscoveryEndpoint;
    }

    @Nullable
    public Uri getAuthEndpoint() {
        checkConfigurationRead();
        return mAuthEndpoint;
    }

    @Nullable
    public Uri getTokenEndpoint() {
        checkConfigurationRead();
        return mTokenEndpoint;
    }

    @NonNull
    public String getClientId() {
        checkConfigurationRead();
        return mClientId;
    }

    @Nullable
    public String getClientSecret() {
        checkConfigurationRead();
        return mClientSecret;
    }

    @NonNull
    public Uri getRedirectUri() {
        checkConfigurationRead();
        return mRedirectUri;
    }

    @NonNull
    public String getScope() {
        checkConfigurationRead();
        return mScope;
    }

    public void retrieveConfig(Context context,
                               RetrieveConfigurationCallback callback) {
        readConfiguration(context);
        if (getDiscoveryEndpoint() != null) {
            AuthorizationServiceConfiguration.fetchFromUrl(mDiscoveryEndpoint, callback);
        } else {
            AuthorizationServiceConfiguration config =
                    new AuthorizationServiceConfiguration(mAuthEndpoint, mTokenEndpoint, null);
            callback.onFetchConfigurationCompleted(config, null);
        }
    }

    private static boolean isSpecified(int value) {
        return value != NOT_SPECIFIED;
    }

    private static int checkSpecified(int value, String valueName) {
        if (value == NOT_SPECIFIED) {
            throw new IllegalArgumentException(valueName + " must be specified");
        }
        return value;
    }

    private static Uri getUriResource(Resources res, @StringRes int resId, String resName) {
        return Uri.parse(res.getString(resId));
    }
}
