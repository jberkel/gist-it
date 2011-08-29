package com.zegoggles.gist

import org.json.JSONObject

object User extends JsonModel[User] {
  def apply(j: JSONObject):Option[User] = {
    val root = if (j.has("user")) j.getJSONObject("user") else j
    Some(User(root.getInt("id"),
      root.getString("name"),
      root.getString("login"),
      root.getString("email")))
  }
}
case class User(id: Int, name:String, login:String, email:String)
