# gist-it

![icon][]

Open source Android gist API client written in Scala.

The Android app uses the new [github api][] to provide a "send to gist"
feature for most applications which have a "Send" or "Share" menu.

Check the following screenshot to get an idea of the flow (this example uses the
[ColorNote Notepad][] app)

![flow][]

By default gists are created anonymously - you can add your github account
using Android's "Account&Sync" preferences or follow the instructions in the
gist app itself.

With an associated account you also have the ability to edit existing gists -
Use "Load gist" from the menu, make changes and upload it again.

## Usage from other apps

If your are developing an Android app and want to make use of the gist api you
can do so with intents. At the moment there are two actions exposed:

### picking/loading a gist

    Intent intent = new Intent("com.zegoggles.gist.PICK");
    intent.putExtra("load_gist", false); // load gist content, defaults to true
    startActivityForResult(intent, 0)

### uploading a gist

    startActivityForResult(new Intent("com.zegoggles.gist.UPLOAD")
        .putExtra(Intent.EXTRA_TEXT, "text123")
        .putExtra("public", false)
        .putExtra("description", "testing gist upload via intent"), 0);

## Building from source

You need [sbt][] (simple-build-tool) in order to build the project, and
a snapshot version of the [sbt-android-plugin][].

    $ sbt update
    $ sbt 'project gist' package-debug

To run tests:

    $ sbt 'project gist' test

Pull requests welcome, especially the design needs some love (hint, hint).

## Credits / License

See LICENSE. Post it graphic by [christianalm][].

[gist]: https://github.com/blog/118-here-s-the-gist-of-it
[github api]: http://developer.github.com/v3/gists/
[ColorNote Notepad]: https://market.android.com/details?id=com.socialnmobile.dictapps.notepad.color.note
[sbt]: http://code.google.com/p/simple-build-tool/
[sbt-android-plugin]: https://github.com/jberkel/android-plugin
[flow]: https://github.com/downloads/jberkel/gist-it/send_flow.png
[icon]: https://github.com/downloads/jberkel/gist-it/gist-it-logo_128.png
[christianalm]: http://graphicriver.net/user/cristianalm
