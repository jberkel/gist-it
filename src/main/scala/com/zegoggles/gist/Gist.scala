package com.zegoggles.gist

import org.json.JSONObject

object Gist {
  def apply(j: JSONObject): Option[Gist] = {
    val files = j.getJSONObject("files").names()
    val file = j.getJSONObject("files").getJSONObject(files.getString(0))
    Some(Gist(j.getString("id"), j.getString("description"),
      file.getString("filename"),
      file.getLong("size"),
      j.getBoolean("public"),
      j.getString("url"),
      file.optString("content")))
  }

  def apply(s: String): Option[Gist] = {
    try {
      apply(new JSONObject(s))
    } catch {
      case e: Exception =>
        System.err.println(s)
        None
    }
  }
}

case class Gist(val id: String, val description: String,
           val filename: String, val size: Long,
           val public: Boolean, val url: String, val content: String) {

  override def toString = "Gist %d".format(id)
  def describe = "%s (%s)".format(filename, Utils.humanReadableSize(size), id)
}
