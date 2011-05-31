package com.zegoggles.gist

import android.os.Bundle
import org.apache.http.HttpStatus
import com.zegoggles.gist.Implicits._
import android.app.{Activity, ProgressDialog, ListActivity}
import android.content.{Intent, Context}
import android.text.Html
import actors.Futures
import java.io.IOException
import scala.Left
import android.widget._
import android.view._

class GistList extends ListActivity with ApiActivity with Logger {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    if (getLastNonConfigurationInstance != null) {
      setListAdapter(getLastNonConfigurationInstance.asInstanceOf[ListAdapter])
    } else {
      setListAdapter(new GistAdapter())

      app.preloadedList match {
        case Some(gists) => getListAdapter.setGists(gists)
        case None        => loadGists(pathFromIntent(getIntent))
      }
    }
  }

  def loadGist(gist: Gist)(whenDone: Either[IOException,String] => Unit) = Futures.future {
    val result = try {
      Right(gist.load)
    } catch {
      case e: IOException => Left(e)
    }
    runOnUiThread(whenDone(result))
  }

  def loadGists(path: String) {
    val pd = ProgressDialog.show(this, null, getString(R.string.loading_gists), true)
    executeAsync(api.get(_),
      Request(path),
      HttpStatus.SC_OK, Some(pd))(resp => Gist.list(resp.getEntity).map(onGistLoaded(_))) {
      error => error match {
          case Left(e)  => warn("error getting gists", e)
          case Right(r) => warn("unexpected status code: "+r.getStatusLine)
        }
        Toast.makeText(this, R.string.list_failed, Toast.LENGTH_LONG).show()
        finish()
    }
  }

  def onGistLoaded(g: Seq[Gist]) {
    app.preloadedList = Some(g)
    getListAdapter.setGists(g)
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val gist = getListAdapter.getItem(position)
    val extras = gist.asBundle

    def done() {
      setResult(Activity.RESULT_OK, new Intent().putExtras(extras).setData(gist.uri))
      finish()
    }

    if (shouldLoadBody(getIntent)) {
      val progress = ProgressDialog.show(this, null, getString(R.string.loading_gist), true)
      loadGist(gist) { r =>
        progress.dismiss()
        r match {
          case Right(body)     =>  extras.putString("body", body)
          case Left(exception) =>
            warn("error fetching content", exception)
            Toast.makeText(this,
              getString(R.string.loading_gist_failed, exception.getMessage),
              Toast.LENGTH_SHORT).show()
        }
        done()
      }
    } else done()
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    menu.add(Menu.NONE, 1, Menu.NONE, R.string.refresh).setIcon(android.R.drawable.ic_menu_rotate)
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
    case 1 => loadGists(pathFromIntent(getIntent)); true
    case _ => false
  }

  def pathFromIntent(intent: Intent): String =
    if (intent.getData != null) intent.getData.getPath else "/gists"

  def shouldLoadBody(intent: Intent) = intent.getBooleanExtra("load_gist", true)

  override def onRetainNonConfigurationInstance() = getListAdapter
  override def getListAdapter:GistAdapter = super.getListAdapter.asInstanceOf[GistAdapter]
}

class GistAdapter extends BaseAdapter {
  var gists: IndexedSeq[Gist] = Vector.empty

  def findView[T](v: View, tr: TypedResource[T]) = v.findViewById(tr.id).asInstanceOf[T]
  def getView(position: Int, convertView: View, parent: ViewGroup) = {
    val view = if (convertView == null)
      parent.getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            .asInstanceOf[LayoutInflater].inflate(R.layout.gist_row, parent, false)
    else convertView

    val gist = getItem(position)
    val description = findView(view, TR.gist_description)
    description.setText(Html.fromHtml(gist.asHtml.toString()))
    view.setBackgroundColor(view.getResources.getColor(gist.color))
    view
  }

  def getItemId(position: Int) = getItem(position).id.hashCode()
  def getItem(position: Int):Gist = gists(position)
  def getCount = gists.size
  override def hasStableIds = true

  def setGists(l: Traversable[Gist]) {
    gists = Vector[Gist](l.toSeq:_*)
    notifyDataSetChanged()
  }
}

package examples {
  // how to start from a console:
  // am start -a android.intent.action.MAIN -n com.zegoggles.gist/.examples.Pick
  class Pick extends Activity {
    override def onCreate(bundle: Bundle) {
      super.onCreate(bundle)

      // this is how you would call the upload activity from another app
      startActivityForResult(new Intent(Intents.PICK_GIST), 0)

      // here's how you would fetch only starred gists
      //startActivityForResult(new Intent(Intents.PICK_GIST)
      //  .setData(Uri.parse("gist://github.com/gists/starred")), 0)
    }

    override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
      if (resultCode == Activity.RESULT_OK && data.getData != null) {
        Toast.makeText(this, "Picked "+data.getData,Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(this, "Canceled",Toast.LENGTH_SHORT).show()
      }
    }
  }
}
