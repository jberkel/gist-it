package com.zegoggles.gist

import java.io.IOException
import scala.Some
import java.net.URL
import org.json.{JSONException, JSONObject}

object Gist {
  def apply(j: JSONObject): Option[Gist] = {
    val files = j.getJSONObject("files").names()
    val file = j.getJSONObject("files").getJSONObject(files.getString(0))
    Some(Gist(j.getString("id"), j.getString("description"),
      file.getString("filename"),
      file.getLong("size"),
      j.getBoolean("public"),
      j.getString("url"),
      file.getString("raw_url"),
      file.optString("content")))
  }

  def apply(s: String): Option[Gist] = {
    try {
      apply(new JSONObject(s))
    } catch {
      case e: JSONException => None
    }
  }
}

case class Gist(id: String, description: String,
                filename: String, size: Long,
                public: Boolean, url: String, raw_url: String, content: String) {

  override def toString = "Gist %d".format(id)
  def describe = "%s (%s)".format(filename, Utils.humanReadableSize(size), id)
  def raw_content:Option[String] =
    try {
      Some(io.Source.fromURL(new URL(raw_url)).mkString)
    } catch { case e:IOException => None }
}
