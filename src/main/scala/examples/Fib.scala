package examples

import chisel3._
import click._

/**
 * A Fibonacci circuit
 * @param dataWidth Width of the data being passed around the circuit
 */
class Fib(dataWidth: Int)(implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val go = Input(Bool())
    val out = Output(UInt(dataWidth.W))
    val valid = Output(Bool())
  })

  //Create modules
  val R0 = Module(new HandshakeRegister(0.U(dataWidth.W)))
//  val R1 = Module(new HandshakeRegister(1.U(dataWidth.W), true)) //R1 and R2 start with tokens
  val RF1 = Module(RegFork(1.U(dataWidth.W), true))


  val R2 = Module(new HandshakeRegister(0.U(dataWidth.W), true))
//  val F0 = Module(Fork(UInt(dataWidth.W)))
  val add = Module(Adder(dataWidth))

  add.io.out <> R0.io.in
  R0.io.out <> RF1.io.in
  RF1.io.out1 <> R2.io.in
  RF1.io.out2 <> add.io.in1
  add.io.in2 <> R2.io.out

  io.out := R2.io.out.data
  io.valid := R2.io.out.req

  //Handle resets and barrier signal
  R0.io.reset := this.reset.asAsyncReset
  R2.io.reset := this.reset.asAsyncReset
  add.io.in2.req := R2.io.out.req && io.go


}
