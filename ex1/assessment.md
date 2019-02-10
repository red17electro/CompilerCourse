Please only use plain text, markdown or PDF files as readme! There is no guarantee I can handle other file formats!

# Lexer

Correct extension of the scanner.

Points: 1/1

# Parser

## Positive

- Nice and clean extension of the CUP file
- Good explanation in the readme
- Correct extension of the AST classes

## Neutral

- `Operator` for `ExprBinary`? Not needed since the subclasses already represent the different cases!!

Points: 3/3

# Visitor

Correct implementation of the `accept` methods in the AST classes and the visitor class(es).
You can also define visitor with results, which in you case would only be `int`.
But in general you should make this result type generic since the visitor pattern is intended to be a generic pattern.

Correct implementation of the pretty printer using the visitor pattern. Nice work!

Points: 3/3

# Evaluator

The evaluator seems to work, but:

- The evaluator does not really use the visitor pattern as intended. The visitor should represent the algorithm computed over the tree, not method in subclasses!!!
- `getValue` method in `Expr` subclasses. This logic should be implemented in the visitor subclass!!!
- `ExprBinary` has additional `Operator`, which could be inconsistent with the subclasses: Same information represented twice! Implementation of `getValue` fragile (equivalent with if and instanceof)!

Points: 0/3
