Chisel-Click
=======================

This repository contains a Chisel implementation of click elements [1], a module used for implementing 2-phase bundled-data
handshakes in asynchronous circuits. It specifically implements _phase-decoupled_ click elements [2], which allow for
rings with an uneven number of tokens.

The work is based on [2] and has been implemented based on [their VHDL code](https://github.com/zuzkajelcicova/Async-Click-Library).



Sources
===
- [1] A. Peeters, F. Te Beest, M. De Wit, and W. Mallon, “Click elements: An implementation style for data-driven compilation,” Proc. - Int. Symp. Asynchronous Circuits Syst., pp. 3–14, 2010, doi: 10.1109/ASYNC.2010.11.
- [2] A. Mardari, Z. Jelcicova, and J. Sparso, “Design and FPGA-implementation of asynchronous circuits using two-phase handshaking,” Proc. - Int. Symp. Asynchronous Circuits Syst., vol. 2019-May, pp. 9–18, 2019, doi: 10.1109/ASYNC.2019.00010.