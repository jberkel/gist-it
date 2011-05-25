package com.zegoggles.gist

import android.os.Bundle
import android.view.{LayoutInflater, ViewGroup, View}
import org.apache.http.HttpStatus
import com.zegoggles.gist.Implicits._
import android.app.{Activity, ProgressDialog, ListActivity}
import android.content.{Intent, Context}
import android.widget.{Toast, ListView, TextView, BaseAdapter}
import android.text.Html

class GistList extends ListActivity with ApiActivity with Logger {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setListAdapter(new GistAdapter())
    loadGists()
  }
  override def getListAdapter:GistAdapter = super.getListAdapter.asInstanceOf[GistAdapter]
  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val gist = getListAdapter.getItem(position)
    setResult(Activity.RESULT_OK, new Intent().putExtras(gist.asBundle).setData(gist.uri))
    finish()
  }

  def loadGists() {
    val pd = ProgressDialog.show(this, null, getString(R.string.loading_gists), true)
    executeAsync(api.get(_),
      Request("/gists"),
      HttpStatus.SC_OK, Some(pd))(resp => Gist.list(resp.getEntity).map(l => getListAdapter.setGists(l))) {
      error =>
        error match {
          case Left(e)  => warn("error getting gists", e)
          case Right(r) => warn("unexpected status code: "+r.getStatusLine)
        }
        Toast.makeText(this, R.string.list_failed, Toast.LENGTH_LONG).show()
        finish()
    }
  }
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
