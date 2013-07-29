// "Import" "false"
// ERROR: Unresolved reference: SomeTest
// ERROR: Cannot import from 'test'

package testing

import testing.test.SomeTest

fun main(args : Array<String>) {
    <caret>SomeTest()
}
