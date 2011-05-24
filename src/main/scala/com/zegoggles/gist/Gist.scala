package com.zegoggles.gist

import java.io.IOException
import scala.Some
import java.net.URL
import org.json.JSONObject
import java.text.SimpleDateFormat

object Gist extends JsonModel[Gist] {
  lazy val iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
  def getDate(s: String ) = iso8601.parse(s)

  def apply(j: JSONObject) = {
    val files = j.getJSONObject("files").names()
    val file = j.getJSONObject("files").getJSONObject(files.getString(0))
    val history = j.optJSONArray("history")
    val last_modified = if (history != null && history.length() > 0)
      getDate(history.getJSONObject(0).getString("committed_at"))
    else
      getDate(j.getString("created_at"))

    Some(Gist(j.getString("id"), j.getString("description"),
      file.getString("filename"),
      file.getLong("size"),
      j.getBoolean("public"),
      j.getString("url"),
      file.getString("raw_url"),
      file.optString("content"),
      last_modified.getTime / 1000L))
  }
}

case class Gist(id: String, description: String,
                filename: String, size: Long,
                public: Boolean, url: String, raw_url: String,
                content: String, last_modified: Long) {


  override def toString = "Gist %d".format(id)
  def asHtml = <span><b>{filename}</b><small> ({size_in_words}, {last_modified_ago})</small></span>
  def public_url =  "https://gist.github.com/" + url.substring(url.lastIndexOf("/")+1)
  def raw_content:Option[String] =
    try {
      Some(io.Source.fromURL(new URL(raw_url)).mkString)
    } catch { case e:IOException => None }

  def size_in_words = Utils.humanReadableSize(size)
  def last_modified_ago = Utils.humanTime(System.currentTimeMillis() / 1000L - last_modified) + " ago"
  def color = if (public) R.color.public_gist else R.color.private_gist
}

