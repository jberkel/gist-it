package com.zegoggles.gist

import android.os.Bundle
import android.view.{LayoutInflater, ViewGroup, View}
import org.apache.http.HttpStatus
import org.json.JSONArray
import com.zegoggles.gist.Implicits._
import collection.mutable.ListBuffer
import android.widget.{ListView, TextView, BaseAdapter}
import android.app.{Activity, ProgressDialog, ListActivity}
import android.content.{Intent, Context}

class GistList extends ListActivity with ApiActivity with Logger {
  val gistAdapter = new GistAdapter()

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setListAdapter(gistAdapter)
    loadGists()
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val gist = gistAdapter.getItem(position)
    setResult(Activity.RESULT_OK, new Intent().putExtra("id", gist.id))
    finish()
  }

  def loadGists() {
    val pd = ProgressDialog.show(this, null, "Loading", true)
    executeAsync(api.get(_),
      Request("https://api.github.com/gists"),
      HttpStatus.SC_OK) { success =>
        pd.dismiss()
        val response:String = success.getEntity
        // XXX make functional
        val jsonList = new JSONArray(response)
        val b = new ListBuffer[Gist]()
        for (i <-  0 until jsonList.length()) {
          b += Gist.fromJSON(jsonList.getJSONObject(i))
        }
        gistAdapter.setGists(Vector[Gist](b: _*))
    } {
      error =>
        pd.dismiss()
        error match {
        case Left(e)  => warn("error getting gists", e)
        case Right(r) => warn("unexpected status code: "+r.getStatusLine)
      }
    }
  }
}

class GistAdapter extends BaseAdapter {
  var gists: Vector[Gist] = Vector.empty

  def getView(position: Int, convertView: View, parent: ViewGroup) = {
    val view = if (convertView == null) {
      parent.getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            .asInstanceOf[LayoutInflater].inflate(R.layout.gist_row, parent, false)
    } else {
      convertView
    }
    val tv: TextView  = view.findViewById(R.id.gist_id).asInstanceOf[TextView]
    tv.setText(getItem(position).toString)
    view
  }

  def getItemId(position: Int) = getItem(position).id
  def getItem(position: Int):Gist = gists(position)
  def getCount = gists.size

  override def hasStableIds = true

  def setGists(l: Vector[Gist]) {
    gists = l
    notifyDataSetChanged()
  }
}