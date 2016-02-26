AppAuth Extended demo
---------------------

*WARNING*: The demo app in this repository, while useful, does some
*very bad things* that should not be copied for use in *any production app*.
For further explanation, read the section on "Why is this app bad?" below.

This app demonstrate the use of the
[AppAuth for Android](https://github.com/openid/AppAuth-Android/)
library to interact with four identity providers (IDPs): Facebook, GitHub,
Google and Microsoft. While Google supports the current best-practices for
OAuth2 from mobile devices (specifically, public clients and PKCE), the other
providers do not:

- All require the use of client secrets in order
  to acquire a refresh token. The use of the implicit flow to acquire
  short lived access tokens is generally unacceptable for mobile apps, as
  frequent usage of custom tabs to acquire new access tokens will disrupt the
  user experience.

- All require that the redirect URI be an https URL. Prior to Android M, this
  would require the use of an "interstitial" web page to be used that could
  collect the response of the authorization request and forward it to the
  app via a custom scheme URI. With Android M and above this is less of an
  issue, as
  [deep linking](https://developer.android.com/training/app-indexing/deep-linking.html)
  can be used to allow the app to directly handle the https redirect URI.

- Github and Facebook allow re-authorization without any user interaction:
  the authorization endpoint will immediately redirect to the specified
  redirect URI if the user has already consented to the requested scopes.
  In order for an app to receive either a custom URI or deep link redirect,
  a user action must have triggered it, otherwise it is blocked as potentially
  malicious.

This app registers for deep linking on Android M and above, and uses an
interstitial page ([see here](http://appauth.demo-app.io/oauth2redirect)) to
capture the response on earlier versions of Android. The experience is far
from perfect, but it does work.

Why is this app bad?
====================

This app is a good example of what *not* to do when using AppAuth for Android:

- For deep linking to work, the app's certificate must match that of the
  declared affiliation
  ([see here](https://appauth.demo-app.io/.well-known/assetlinks.json)).
  In order to make it possible for you to compile an app with this certificate,
  it is included in this repository.

- The client IDs and _secrets_ are embedded into this repository to make it
  easier for you to build the demo without having to configure four separate
  IDPs. Even if I had not done this, they could be extracted from the APK.
  Client secrets often come with a number of security assumptions that are
  invalid once they are no longer secret. It is trivial for any other app to
  now pretend to be mine.

*DO NOT DO ANY OF THESE* in your own apps. As things stand at the time of
writing, one should not use AppAuth for Android to integrate _directly_ with
Facebook, GitHub or Microsoft. Once these providers support the recommendations
of [OAuth 2.0 for Native Apps](https://tools.ietf.org/html/draft-wdenniss-oauth-native-apps)
then that will change.

What should I be doing instead?
===============================

For IDPs which rely on client secrets, all authorization should be performed
with the support of your application's backend. In an ideal world, your
application's backend itself would act as an OAuth2 or OpenID Connect
authorization service: AppAuth would request authorization via your backend,
which in turn would fan out to the IDP of the user's choice. The backend
can then perform the exchange with the external IDP to secure a refresh token.
When this succeeds, it can then create an authorization code to send to your
app, which it exchanges for its own refresh token. All subsequent interaction
with the external IDP would be mediated by your own backend.

This "ideal world" scenario is complex, and obviously requires that your app
even _has_ a backend / web app. It is our hope that as mobile applications
become ever more important, identity providers will begin to support the
recommendations that allow AppAuth to work as intended, with the client
application fully in control of the authorization process.
