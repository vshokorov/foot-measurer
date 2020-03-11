from __future__ import absolute_import, division, print_function

from java import dynamic_proxy, jboolean, jvoid, Override
from java import constructor, method, static_proxy, jint

#from android.app import AlertDialog
#from android.content import Context, DialogInterface
#from android.graphics.drawable import ColorDrawable
#from android.os import Bundle
#from android.support.v4.app import DialogFragment
#from android.support.v7.app import AppCompatActivity
#from android.support.v7.preference import Preference, PreferenceFragmentCompat
#from android.view import Menu, MenuItem, View
#from java.lang import String

#from com.chaquo.python.demo import App, R
#import sys

from java.lang import String
from types import MappingProxyType

@method(jint, [jint])
def test(a):
    print("Python : Hello!")
    print("Python :", a)
    print("Python :", a.__repr__())
    return a + 10

class BasicAdder(static_proxy()):
    @constructor([jint])
    def __init__(self, n):
        super(BasicAdder, self).__init__()
        self.n = n

    @method(jint, [jint])
    def add(self, x):
        return self.n + x