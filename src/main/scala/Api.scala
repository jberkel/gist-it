package com.zegoggles.github

import java.net.URI
import actors.Actor
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import scala.collection.mutable.ListBuffer
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods._
import org.apache.http.HttpRequest

case class Exchange(code:String)
case class Token(access:String)

class Request(val url:String) {
  lazy val params:ListBuffer[NameValuePair] = ListBuffer()

  def add(key:String, value:Any):Request = {
    params += new BasicNameValuePair(key, value.toString())
    this
  }

  def params(params:Map[String,Any]):Request = {
    for ((k,v) <- params) add(k,v)
    this
  }

  def toReq[T <: HttpRequestBase](r: Class[T]):T = {
    val req = r.newInstance
    req.setURI(URI.create(url))
    req
  }
}

class Api(client_id:String, client_secret:String, redirect_uri:String) extends Actor with StdoutLogger {
    lazy val client = new DefaultHttpClient()

    def exchangeToken(code:String):Token = {
        val req = new Request("https://https://github.com/login/oauth/access_token")
                    .params(Map("client_id" -> client_id,
                                "client_secret" -> client_secret,
                                "code" -> code,
                                "redirect_uri" -> redirect_uri))


        client.execute(req.toReq(classOf[HttpPost]))
        Token("invalid")
    }

    def act() {
        while (true) {
            receive {
                case e:Exchange  =>
                  log("exchanging " + e.code)
                  val token = exchangeToken(e.code)
                case other       => log("unknown message: " +other)
            }
        }
    }
}

