package com.zegoggles.gist

import android.os.Bundle
import android.view.{LayoutInflater, ViewGroup, View}
import org.apache.http.HttpStatus
import com.zegoggles.gist.Implicits._
import android.app.{Activity, ProgressDialog, ListActivity}
import android.content.{Intent, Context}
import android.widget.{Toast, ListView, TextView, BaseAdapter}

class GistList extends ListActivity with ApiActivity with Logger {
  val gistAdapter = new GistAdapter()

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setListAdapter(gistAdapter)
    loadGists()
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val gist = gistAdapter.getItem(position)
    setResult(Activity.RESULT_OK,
      new Intent().putExtra("id", gist.id)
                  .putExtra("filename", gist.filename)
                  .putExtra("raw_url", gist.raw_url)
                  .putExtra("content", gist.content))
    finish()
  }

  def loadGists() {
    val pd = ProgressDialog.show(this, null, getString(R.string.loading_gists), true)
    executeAsync(api.get(_),
      Request("https://api.github.com/gists"),
      HttpStatus.SC_OK, pd)(resp => JsonList(resp.getEntity, Gist(_)).map(l => gistAdapter.setGists(l))) {
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

  def getView(position: Int, convertView: View, parent: ViewGroup) = {
    val view = if (convertView == null) {
      parent.getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            .asInstanceOf[LayoutInflater].inflate(R.layout.gist_row, parent, false)
    } else {
      convertView
    }
    val gist = getItem(position)
    if (gist.public) view.findViewById(R.id.private_gist).setVisibility(View.GONE)
    val tv: TextView  = view.findViewById(R.id.gist_id).asInstanceOf[TextView]
    tv.setText(gist.describe)
    view
  }

  def getItemId(position: Int) = getItem(position).hashCode()
  def getItem(position: Int):Gist = gists(position)
  def getCount = gists.size
  override def hasStableIds = true

  def setGists(l: Traversable[Gist]) {
    gists = Vector[Gist](l.toSeq:_*)
    notifyDataSetChanged()
  }
}