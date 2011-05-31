package com.zegoggles.gist

import android.widget.TextView
import android.text.method.LinkMovementMethod
import android.view.View
import android.text.style.ClickableSpan
import android.text.{SpannableString, Spanned, Spannable}

object Utils {
  /**
   * Adapted from the {@link android.text.util.Linkify} class. Changes the
   * first instance of {@code link} into a clickable link attached to the given function.
   */
  def clickify(v: TextView, clickableText: String, listener: => Unit):Boolean = {
    val text = v.getText
    val string = text.toString
    val span = new ClickableSpan {
      def onClick(widget: View) {
        listener
      }
    }
    val start = string.indexOf(clickableText)
    if (start != -1) {
      val end = start + clickableText.length()
      text match {
        case spannable:Spannable => spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        case other =>
            val s = SpannableString.valueOf(other)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            v.setText(s)
      }
      val m = v.getMovementMethod
      if (m == null || !(m.isInstanceOf[LinkMovementMethod])) {
        v.setMovementMethod(LinkMovementMethod.getInstance())
      }
      true
    } else false
  }

  def readableSize(b: Long) = b match {
    case c if c > 0 && c < 1024 => b+" bytes"
    case c if c > 1024 && c < 1024*1024 => (b/1024).round+" kb"
    case c if c > 1024*1024 && c < 1024*1024*1024 => (b/1024/1024).round+" mb"
    case c  => (b/1024/1024/1024).round+" gb"
  }

  def readableTime(t: Long) = {
    def format(s: Long, divisor:Int, name:String) = {
        val value = (s/divisor).round
        value+" "+((if (value == 1) name else name+"s")+ " ago")
    }
    t match {
      case s if s < 10           => "just now"
      case s if s < 60           =>  format(s, 1, "second")
      case s if s < 3600         =>  format(s, 60, "minute")
      case s if s < 3600*24      =>  format(s, 3600, "hour")
      case s if s < 86400*30     =>  format(s, 86400, "day")
      case s if s < 86400*30*12  =>  format(s, 86400*30, "month")
      case s                     =>  format(s, 86400*30*12, "year")
    }
  }
}
