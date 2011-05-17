package com.zegoggles.github

import org.json.JSONObject

object User {
  def fromJSON(o: JSONObject) = {
    new User(o.getInt("id"),
      o.getString("name"),
      o.getString("login"),
      o.getString("email"))

  }
}

case class User(id: Int, name:String, login:String, email:String)