package com.zegoggles.github.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class UnitTests extends AndroidTestCase {
  def testPackageIsCorrect {
    assertEquals("com.zegoggles.github", getContext.getPackageName)
  }
}