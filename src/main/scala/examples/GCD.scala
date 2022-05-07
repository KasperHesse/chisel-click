package examples

import chisel3._
import click._
import chisel3.experimental.BundleLiterals._

class GCD(dataWidth: Int)(implicit conf: ClickConfig) extends Module {
  def dtype() = new Bundle2(UInt(dataWidth.W), UInt(dataWidth.W))
  val io = IO(new Bundle {
    val in = new ReqAck(dtype())
    val out = Flipped(new ReqAck(dtype()))
  })

  //Components
  //R0 holds the bit used to select between new data and the data currently being processed in the loop
  val R0 = Module(new HandshakeRegister(false.B, true))
  //RF0 is used to fork the values of A,B to R0 and the demultiplexer
  val RF0 = Module(RegFork(dtype().Lit(_.a -> 0.U, _.b -> 0.U), false))
  //RF1 is used to the values of A,B to CL1 and DX1
  val RF1 = Module(RegFork(dtype().Lit(_.a -> 0.U, _.b -> 0.U), false))

  //F0 is used to split the result of A != B between two data channels
  val F0 = Module(Fork(Bool()))

  //CL1 implements a != b, used to track if execution has finished
  val CL0 = Module(new LogicBlock(dtype(), Bool(), (x: Bundle2[UInt, UInt]) => x.a =/= x.b, delay=3))
  //CL1 implements a>b, used to select whether to update the value of A or B through the loop
  val CL1 = Module(new LogicBlock(dtype(), Bool(), (x: Bundle2[UInt, UInt]) => x.a > x.b, delay=3))
  //CL2 sets a:= a-b, preserving the value of b
  val CL2 = Module(new LogicBlock(dtype(), dtype(), (x: Bundle2[UInt, UInt]) => {
    val y = Wire(dtype())
    y.a := x.a - x.b
    y.b := x.b
    y
  }, delay=conf.ADD_DELAY))
  val CL3 = Module(new LogicBlock(dtype(), dtype(), (x: Bundle2[UInt, UInt]) => {
    val y = Wire(dtype())
    y.a := x.a
    y.b := x.b - x.a
    y
  }, delay=conf.ADD_DELAY))

  //MX0 is a multiplexer used to select input data
  val MX0 = Module(new Multiplexer(dtype()))
  //DX0 is a demultiplexer used to propagate data to the output or keep it in the loop
  val DX0 = Module(new Demultiplexer(dtype()))
  //DX1 is a demultiplexer used to forward data to either of CL2 and CL3
  val DX1 = Module(new Demultiplexer(dtype()))
  //ME0 is used to select the updates value of a,b
  val ME0 = Module(new Merge(dtype()))

  //Connections
  MX0.io.sel <> R0.io.out
  MX0.io.in1 <> io.in
  MX0.io.out <> RF0.io.in
  RF0.io.out1 <> CL0.io.in
  RF0.io.out2 <> DX0.io.in
  CL0.io.out <> F0.io.in
  F0.io.out1 <> R0.io.in
  F0.io.out2 <> DX0.io.sel
  DX0.io.out2 <> RF1.io.in
  RF1.io.out1 <> CL1.io.in
  RF1.io.out2 <> DX1.io.in

  DX1.io.sel <> CL1.io.out
  CL2.io.in <> DX1.io.out2
  CL2.io.out <> ME0.io.in1
  CL3.io.in <> DX1.io.out1
  CL3.io.out <> ME0.io.in2
  MX0.io.in2 <> ME0.io.out

  io.out <> DX0.io.out1

  R0.io.reset := this.reset.asAsyncReset
}
