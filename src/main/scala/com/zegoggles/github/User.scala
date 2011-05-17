package com.zegoggles.github

import org.json.JSONObject

object User {
  def fromJSON(o: JSONObject) = {
    val root = if (o.has("user")) o.getJSONObject("user") else o
    new User(root.getInt("id"),
      root.getString("name"),
      root.getString("login"),
      root.getString("email"))
  }
}

case class User(id: Int, name:String, login:String, email:String)