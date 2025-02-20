<p align="center">
    <br />
    <a href="https://thirdweb.com">
        <img src="https://thirdweb.com/brand/thirdweb-icon.svg" width="200" alt=""/></a>
    <br />
</p>

<h1 align="center"><a href='https://thirdweb.com/'>thirdweb</a> Android SDK</h1>

<p align="center"><strong>Simple smart wallet connection for Android apps</strong></p>

## Features

- Let user connect to their smart wallet with a single line of code
- Automatic session keys to grant your app scoped access to user's wallet
- Best used in conjunction with [thirdweb engine](https://portal.thirdweb.com/engine) as the transaction executor
- Standard OAuth2 flow with access and refresh tokens which can be used for your own server's authentication
- Supports any EVM chain

## Installation

Add the following to your `build.gradle` file:

```gradle
dependencies {
    implementation 'com.thirdweb:connect:<latest_version_here>'
}
```

You can view the latest published version on [maven central](https://central.sonatype.com/artifact/com.thirdweb/connect).

## Usage

### Initialize the SDK

If you haven't already, create a project and obtain a free client id from the [thirdweb dashboard](https://thirdweb.com/team).

Create a new instance of the SDK with your clientId and redirect url. You should only keep one instance of the SDK in your app:

```kotlin
import com.thirdweb.Thirdweb

const val clientId = "your_client_id" // get this from the thirdweb dashboard
const val redirectUrl = "your_app://callback" // this should be a deeplink scheme that your app can handle

val thirdweb = Thirdweb(clientId, redirectUrl)
```

The redirectUrl is a deeplink scheme that should be registered in your `AndroidManifest.xml` file:

```xml  
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="your_app" android:host="callback" />
    </intent-filter>
</activity>
```

### Login flow

To connect to the user's wallet, call the `login` method from your UI:

```kotlin
thirdweb.login(context)
```

This will open the authentication flow in a custom chrome tab.

Upon completion, your redirectUrl will be called with the auth result.

You can handle the result in your `onCreate` and/or `onNewIntent` method depending on your launchMode behavior:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // your code ...
    handleLoginCallback(intent)
}

override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleLoginCallback(intent)
}

private fun handleLoginCallback(intent: Intent?) {
    // will only trigger if the activity was started with the correct intent
    // you can also check if the intent has the correct action and data by calling thirdweb.isLoginCallbackIntent(intent)
    thirdweb.handleLoginCallback(baseContext, intent,
        // onSuccess callback, returns the user object, and auth token data usable as your own server auth
        onSuccess = { user, authToken ->
            Toast.makeText(
                this@MainActivity,
                "Logged in as: ${user.userAddress}",
                Toast.LENGTH_LONG
            ).show()
            showUI(user) // your app logic
        },
        // onError callback
        onError = { exception ->
            Toast.makeText(
                this@MainActivity,
                "Error: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    )
}
```

Once this process is done, you can now execute transactions on behalf of the connected users as long as they're within the scoped permissions.

We recommend using [thirdweb engine](https://portal.thirdweb.com/engine), using a [backend wallet as the session key address](https://portal.thirdweb.com/engine/features/account-abstraction#transact-with-an-account). 
This lets you execute any onchain transaction with a simple and performant REST API.

## Get the user's wallet address

You can get the user's wallet address at any time by calling:

```kotlin
val user = thirdweb.getUser()
println(user.userAddress)
```

## Get the user's auth token

You can get the stored auth token by calling:

```kotlin
val authToken = thirdweb.getAuthToken()
println(authToken.accessToken)
```

This is useful to authenticate your own server's API requests. The accessToken follows the JWT standard, 
and can be verified on your backend with a standard JWKS endpoint located at `https://login.thirdweb.com/api/jwks`.

This is a well known standard and has many third party libraries that can help you verify the token, like [jose](https://github.com/panva/jose/blob/main/docs/jwks/remote/functions/createRemoteJWKSet.md#examples) in NodeJS, or [PyJWT](https://pyjwt.readthedocs.io/en/stable/usage.html#retrieve-rsa-signing-keys-from-a-jwks-endpoint) in Python.

With this you have your whole authentication flow covered, including the user's wallet connection and your own server's API requests.

## Logout

To disconnect the user's wallet, call:

```kotlin
thirdweb.logout()
```