package com.zegoggles.github

class App extends android.app.Application {
    lazy val api = new Api("4d483ec8f7deecf9c6f3",
                           "secret",
                           "http://callback")
}
