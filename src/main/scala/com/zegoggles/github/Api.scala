package com.zegoggles.github

import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods._
import collection.mutable.ListBuffer
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.entity.StringEntity
import java.net.URI
import android.app.Activity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.NameValuePair
import com.zegoggles.github.Implicits._
import android.accounts.{AccountManager, Account}
import org.json.JSONObject
import android.content.Context
import java.lang.Boolean
import org.apache.commons.logging.Log

case class Exchange(code: String)

case class Token(access: String)

object Request {
  def apply(s: String, p: (String, String)*) = {
    val request = new Request(s)
    for ((k, v) <- p) request.add(k, v)
    request
  }
  implicit def string2Request(s: String): Request = new Request(s)
}

class Request(val url: String) extends  Logger {
  import collection.JavaConversions._

  lazy val params: ListBuffer[NameValuePair] = ListBuffer()
  var body: Option[String] = None

  def add(key: String, value: Any): Request = {
    params += new BasicNameValuePair(key, value.toString())
    this
  }

  def add(params: Map[String, Any]): Request = {
    for ((k, v) <- params) add(k, v)
    this
  }

  def body(b: String): Request = {
    body = Some(b); this
  }

  def queryString = URLEncodedUtils.format(params, "UTF-8")

  def toURI = URI.create(url + (if (params.isEmpty) "" else "?" + queryString))

  def toReq[T <: HttpRequestBase](r: Class[T]): T = {
    val req = r.newInstance
    req match {
      case e: HttpEntityEnclosingRequestBase =>
        if (body.isDefined) {
          e.setEntity(new StringEntity(body.get))
          req.setURI(toURI)
        } else if (!params.isEmpty) {
          e.setHeader("Content-Type", "application/x-www-form-urlencoded")
          e.setEntity(new StringEntity(queryString))
          req.setURI(URI.create(url))
        }
      case _ => req.setURI(toURI)
    }
    req
  }
}

object Api {
  def map2Json(m: Map[String, Any]): JSONObject = {
    val obj: JSONObject = new JSONObject();
    for ((k, v) <- m) {
      obj.put(k, v match {
        case m: Map[String, Any] => map2Json(m)
        case b: Boolean => b
        case i: Int => i
        case l: Long => l
        case d: Double => d
        case o: Any  => o
      })
    }
    obj
  }

  def parseTokenResponse(s: String): Option[Token] = {
    //"access_token=807e750b891b3fc47b0c951b4c11c0b610195b73&token_type=bearer"
    val token = for (
      fields <- s.split('&').map(s => s.split('='))
      if (fields(0) == "access_token")
    ) yield (fields(1))
    token.headOption.map(t => Token(t))
  }
}

class Api(val client_id: String, val client_secret: String, val redirect_uri: String, var token: Option[Token]) extends StdoutLogger {
  lazy val client = makeClient

  val authorizeUrl = "https://github.com/login/oauth/authorize?client_id=" +
    client_id + "&scope=user,gist&redirect_uri=" + redirect_uri

  def get(req: Request) = execute(req, classOf[HttpGet])
  def put(req: Request) = execute(req, classOf[HttpPut])
  def post(req: Request) = execute(req, classOf[HttpPost])

  def execute[T <: HttpRequestBase](req: Request, reqClass: Class[T]) = {
    token.map(t => req.add("access_token", t.access))
    client.execute(req.toReq(reqClass))
  }

  def exchangeToken(code: String): Option[Token] = {
    val resp = post(Request("https://github.com/login/oauth/access_token",
      "client_id" -> client_id,
      "client_secret" -> client_secret,
      "code" -> code,
      "redirect_uri" -> redirect_uri))

    resp.getStatusLine.getStatusCode match {
      case 200 =>
        token = Api.parseTokenResponse(resp.getEntity)
        token
      case _ => log("Invalid status code " + resp.getStatusLine); None
    }
  }

  def makeClient = new DefaultHttpClient()
}

trait ApiActivity extends Activity with TokenHolder {
  def api = getApplication.asInstanceOf[App].api
}

trait TokenHolder extends Context {
  lazy val accountType = getString(R.string.account_type)

  def account: Option[Account] =
    AccountManager.get(this).getAccountsByType(accountType).headOption

  def token: Option[Token] = {
    account.map(a => Token(AccountManager.get(this).getPassword(a)))
  }
}

trait ApiHolder extends TokenHolder {
  lazy val api = new Api(
    "4d483ec8f7deecf9c6f3",
    "5aa049fafde02f0fdebe52809722b3b894ea7ed2",
    "http://zegoggl.es/oauth/send-to-gist",
    token
  )
}