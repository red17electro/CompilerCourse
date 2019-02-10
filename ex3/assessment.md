- 14 tests fail, but compiles
- correct name analysis (4 points)
- correct method override is not checked for inherited methods (0 points)
- handling of `null` not ok (1 point)
  - `null` is subtype of all class types (but not in your implementation)
- variable lookup not working for fields and local variables (5 points)

10 points

### Theory

for-rule:

- what you did exactly with the while-rule, I have not yet understood
- the first premise for the for-rule states that `i` is of type `int` in the modified environment, which is trivially true and can be left out
- when checking that `i` is not already defined, it is not very clever to name the type `int` because it can easily be confused with the type `int` (whereas it really is a type variable)

2 points

derivation:

- the way you wrote the derivation down is very confusing: I think you did multiple steps at once instead of applying the sequence rule, but than all of the statements should be on the same line
- the remaining derivation looks ok

2 points

overall: 14/18
