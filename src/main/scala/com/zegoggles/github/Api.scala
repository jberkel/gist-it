package com.zegoggles.github

import org.apache.http.message.BasicNameValuePair
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods._
import collection.mutable.ListBuffer
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.entity.StringEntity
import org.apache.http.NameValuePair
import java.net.URI
import android.app.Activity
import io.Source

case class Exchange(code:String)
case class Token(access:String)

object Request {
  def apply(s: String, p: (String, String)*) = {
     val request = new Request(s)
     for ((k,v) <- p) request.add(k,v)
     request
  }
}

class Request(val url:String) {
  import collection.JavaConversions._
  lazy val params:ListBuffer[NameValuePair] = ListBuffer()

  def add(key:String, value:Any):Request = {
    params += new BasicNameValuePair(key, value.toString())
    this
  }

  def add(params:Map[String,Any]):Request = {
    for ((k,v) <- params) add(k,v)
    this
  }

  def queryString = URLEncodedUtils.format(params, "UTF-8")
  def toURI = URI.create(url + (if (params.isEmpty) "" else "?"+queryString))

  def toReq[T <: HttpRequestBase](r: Class[T]):T = {
    val req = r.newInstance
    req match {
      case e:HttpEntityEnclosingRequestBase =>
        if (!params.isEmpty) {
          e.setHeader("Content-Type", "application/x-www-form-urlencoded")
          e.setEntity(new StringEntity(queryString))
        }
        req.setURI(URI.create(url))
      case _ => req.setURI(toURI)
    }
    req
  }
}

object Api {
  def parseTokenResponse(s: String):Option[Token] = {
    val token = for (
      fields <- s.split('&').map( s => s.split('='))
      if (fields(0) == "access_token")
    ) yield (fields(1))
    token.headOption.map( t => Token(t) )
  }
}

class Api(val client_id:String, val client_secret:String, val redirect_uri:String) extends StdoutLogger {
    lazy val client = makeClient

    val authorizeUrl = "https://github.com/login/oauth/authorize?client_id=" +
                client_id + "&scope=gist&redirect_uri=" + redirect_uri


    def post(req: Request) = client.execute(req.toReq(classOf[HttpPost]))

    def exchangeToken(code:String):Option[Token] = {
        val resp = post(Request("https://github.com/login/oauth/access_token",
             "client_id"     -> client_id,
             "client_secret" -> client_secret,
             "code"          -> code,
             "redirect_uri"  -> redirect_uri))

        resp.getStatusLine.getStatusCode match {
          case 200 =>
            //"access_token=807e750b891b3fc47b0c951b4c11c0b610195b73&token_type=bearer"
            Api.parseTokenResponse(Source.fromInputStream(resp.getEntity.getContent).getLines().mkString)
          case _   => log("Invalid status code "+resp.getStatusLine); None
        }
    }


    def makeClient = new DefaultHttpClient()
}

trait ApiActivity extends Activity {
  lazy val accountType = getString(R.string.account_type)

  def api = getApplication.asInstanceOf[App].api
}

trait ApiHolder {
  lazy val api = new Api("4d483ec8f7deecf9c6f3",
    "5aa049fafde02f0fdebe52809722b3b894ea7ed2",
    "http://zegoggl.es/oauth/send-to-gist")
}