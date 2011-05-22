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
      if (text.isInstanceOf[Spannable]) {
        text.asInstanceOf[Spannable].setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      } else {
        val s = SpannableString.valueOf(text)
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
}