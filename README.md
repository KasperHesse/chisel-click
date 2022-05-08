Chisel-Click
=======================

This repository contains a Chisel implementation of click elements [1], a module used for implementing 2-phase bundled-data
handshakes in asynchronous circuits. It specifically implements _phase-decoupled_ click elements [2], which allow for
rings with an uneven number of tokens.

The work is based on [2] and has been implemented based on the diagrams in [3].

# Using the library
The library has been built to leverage the highly generic circuit design that is possible
when using Chisel. In practice, this means that all modules in the `click` library can take any
Chisel datatype as their input, and generate any Chisel datatype on their output. This is the main
contribution of this project vs. the work presented in [2], where it is assumed that all 
signals are 32-bit vectors.

For example, the asynchronous "Fork" component can be used to drive two outputs from one input,
only allowing the next handshake once both consumers have acknowledged data reception.
In most cases, the data being driven onto both consumers is the same, but this may not always be the case.

The `Fork` module implements this behavior, taking *any* function which maps its input to both outputs as a parameter.
For example, it may be necessary to copy the 4 MSB of a signal to one consumer, and the remaining bits
to another consumer. Assuming 16-bit inputs, the `Fork` module is instantiated as follows
```scala
val f = Module(new Fork(
  typIn=UInt(16.W), 
  typOut1=UInt(4.W), 
  typOut2=UInt(12.W), 
  fork=(x: UInt) => (x(15,12), x(11,0))
))
```
Here, the parameters indicate that the input is a 16-bit UInt, the first output is a 4-bit UInt and the
second output a 12-bit UInt. The parameter `fork` is a function which implements the forking behavior.
If both outputs should follow the input, a companion object implements a simpler way of defining this behavior.
```scala
val f = Module(Fork(UInt(16.W)))
```

In all cases, objects expect an implicit `ClickConfig` object to be passed. This object contains delay values
used for simulation, as well as a flag indicating whether the circuit is being elaborated for simulation or synthesis.
Depending on the value of this flag, delay elements are implemented differently (see [DelayElement](src/main/scala/click/DelayElement.scala))


## Examples
In `examples`, two common circuit examples are available: A circuit for calculating the Fibonacci sequence,
and a circuit for computing the greatest common divisor of two integers. XDC files for implementing
these circuits on an FPGA are also available in the [xdc](xdc) directory.

Note that the examples will only work on Xilinx FPGA's, as the synthesized delay elements depend
on the Xilinx synthesis attribute `rloc` to be implemented correctly.

# Testing
The asynchronous circuit components have been tested using [cocotb](https://github.com/cocotb/cocotb/) and
[Icarus Verilog](http://iverilog.icarus.com/).

To run a test in a directory under `src/test/python`, execute
```
make single TESTNAME=<testname>
```
To execute all tests, simply run `make test`. Before running the tests, be sure to execute `make gen`
to generate the Verilog files that are tested.

Sources
===
- [1] A. Peeters, F. Te Beest, M. De Wit, and W. Mallon, “Click elements: An implementation style for data-driven compilation,” Proc. - Int. Symp. Asynchronous Circuits Syst., pp. 3–14, 2010, doi: 10.1109/ASYNC.2010.11.
- [2] A. Mardari, Z. Jelcicova, and J. Sparsø, “Design and FPGA-implementation of asynchronous circuits using two-phase handshaking,” Proc. - Int. Symp. Asynchronous Circuits Syst., vol. 2019-May, pp. 9–18, 2019, doi: 10.1109/ASYNC.2019.00010.
- [3] J. Sparsø, "Introduction to Asynchronous Circuit design" (Kindle Direct Publishing, 2022).