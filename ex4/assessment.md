- 7 standard tests fail
- new basic block for each Java block -> not necessary
- allocation of array does not match array lookup (-2 points)
  - allocation allocates space for size+arrayData
  - array struct is different: size+dataPointer
  - lookup does computation based on array struct -> illegal memory access
- nested if not working (-1 point)
  - add additional jump to end of block checking the condition, which should not be there
  - generates empty block without any terminating instruction

Points: 12/15
