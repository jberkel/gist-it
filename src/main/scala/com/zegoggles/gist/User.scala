package com.zegoggles.gist

import org.json.{JSONObject, JSONException}

object User {
  def apply(j: JSONObject):Option[User] = {
    val root = if (j.has("user")) j.getJSONObject("user") else j
    Some(User(root.getInt("id"),
      root.getString("name"),
      root.getString("login"),
      root.getString("email")))
  }

  def apply(s: String):Option[User] = {
    try {
      apply(new JSONObject(s))
    } catch {
      case e:JSONException => None
    }
  }
}

case class User(id: Int, name:String, login:String, email:String)
