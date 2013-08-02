package a

trait A {
    val b: B
    val nb: B?
}

trait B {
    fun foo(): Int
}

fun test(u: A?, x: A?, y: A?, z: A?, w: A, v: A?) {
    u?.b?.foo()!! // was UNNECESSARY_SAFE_CALL everywhere, because result type (of 'foo()') wasn't made nullable
    u!!.b<!UNNECESSARY_SAFE_CALL!>?.<!>foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    x?.b!!.foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    x!!.b<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>

    y?.nb?.foo()!!
    y!!.nb?.foo()!!
    z?.nb!!.foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    z!!.nb!!.foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>

    w.b<!UNNECESSARY_SAFE_CALL!>?.<!>foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    w.b<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    w.nb?.foo()!!
    w.nb!!.foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>

    v!!.b.foo()<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
}