package com.zegoggles.gist

import android.accounts._
import android.app.Service
import android.os.Bundle
import java.lang.String
import android.content.{Context, Intent}

class AuthenticatorService extends Service {
    lazy val authenticator = new GithubAuthenticator(this)

    def onBind(intent: Intent) = {
        if (intent.getAction == AccountManager.ACTION_AUTHENTICATOR_INTENT) {
            authenticator.getIBinder
        } else {
            null
        }
    }

    class GithubAuthenticator(val context: Context) extends AbstractAccountAuthenticator(context) {
        def addAccount(resp: AccountAuthenticatorResponse, acctype: String, tokenType: String, features: Array[String], options: Bundle) = {
            val reply = new Bundle();
            val intent = (new Intent(context, classOf[Login]))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, resp)
            reply.putParcelable(AccountManager.KEY_INTENT, intent)
            reply
        }

        def hasFeatures(p1: AccountAuthenticatorResponse, p2: Account, p3: Array[String]) = null
        def updateCredentials(p1: AccountAuthenticatorResponse, p2: Account, p3: String, p4: Bundle) = null
        def getAuthTokenLabel(p1: String) = ""
        def getAuthToken(p1: AccountAuthenticatorResponse, p2: Account, p3: String, p4: Bundle) = null
        def confirmCredentials(p1: AccountAuthenticatorResponse, p2: Account, p3: Bundle) = null
        def editProperties(p1: AccountAuthenticatorResponse, p2: String) = null
    }
}

