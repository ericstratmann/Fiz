// Sample Javascript test file, for testing the test framework.

test("test1", function() {
});
test("test2", function() {
    eval("x(;");
});
test("test3", function() {
    assertEqual(14, "14", "string versus integer");
    assertEqual("first error", "First error", "strings not equal");
    assertEqual(98, 99);
    assertEqual(100, 100);
    assertEqual(100, 101);
});
test("test4", function() {
    assertEqual("first error", "First error", "strings not equal");
    throw "unexpected exception";
});