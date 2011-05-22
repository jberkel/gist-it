package com.zegoggles.gist

import org.json.JSONObject

object Gist {
  def fromJSON(s: String):Gist = fromJSON(new JSONObject(s))
  def fromJSON(j: JSONObject) = {
    new Gist(j.getString("id").toLong)
  }
}
class Gist(val id:Long) {

  override def toString = {
    "Gist: " + id
  }
}