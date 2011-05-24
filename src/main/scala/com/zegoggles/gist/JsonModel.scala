package com.zegoggles.gist

import scala.Some
import org.json.{JSONArray, JSONObject, JSONException}

trait JsonModel[T] {
  def apply(j: JSONObject): Option[T]
  def apply(s: String):Option[T] = try {
      apply(new JSONObject(s))
  } catch {
    case e:JSONException => None
  }

  def list(s: String)    = JSONArrayWrapper(s, apply(_:JSONObject))
  def list(l: JSONArray) = JSONArrayWrapper(l, apply(_:JSONObject))
}

object JSONArrayWrapper {
  def apply[T](l: JSONArray, transform: JSONObject => Option[T]):Option[JSONArrayWrapper[T]] =
    Some(new JSONArrayWrapper[T](l, transform))

  def apply[T](s: String, transform: JSONObject => Option[T]):Option[JSONArrayWrapper[T]] = try {
        apply(new JSONArray(s), transform)
    } catch {
      case e:JSONException => None
    }
}

class JSONArrayWrapper[+T](val list: JSONArray, val transform: JSONObject => Option[T]) extends IndexedSeq[T] {
  def apply(idx: Int) = transform(list.getJSONObject(idx)).get
  def length = list.length()
}
