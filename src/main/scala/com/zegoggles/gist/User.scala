package com.zegoggles.gist

import org.json.{JSONObject, JSONException}

object User {
  def fromJSON(s: String):Option[User] = {
    try {
      val o = new JSONObject(s)
      val root = if (o.has("user")) o.getJSONObject("user") else o
      Some(new User(root.getInt("id"),
        root.getString("name"),
        root.getString("login"),
        root.getString("email")))
    } catch {
      case e:JSONException => None
    }
  }
}

case class User(id: Int, name:String, login:String, email:String)