package com.zegoggles.gist

import org.json.{JSONException, JSONObject, JSONArray}

object JsonList {
  def apply[T](s: String, f:JSONObject => Option[T]):Option[JsonList[T]] = {
    try { Some(new JsonList[T](s, f)) } catch { case e: JSONException => None }
  }
}

class JsonList[+T](val s: String, val transform: JSONObject => Option[T])
  extends IndexedSeq[T] {
  val list = new JSONArray(s)
  def apply(idx: Int) = transform(list.getJSONObject(idx)).get
  def length = list.length()
}