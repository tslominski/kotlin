package foo

var c0 = 0
var c1 = 0
var c2 = 0

class A() {
    var p = 0
    fun divAssign(a : Int) {
        c1++;
    }
    fun times(a : Int) {
        c2++;
    }
}

val a : A = A()
get() {
  c0++
  return $a
}

fun box() : String {

    a /= 3
    if (c0 != 1) {
        return "1"
    }
    if (c1 != 1) {
        return "2"
    }
    a *= 3
    if (c0 != 1) {
        return "3"
    }
  if (c2 != 1) {
    return "4"
  }
  return "OK"

}