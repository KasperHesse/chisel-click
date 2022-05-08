package examples

import chisel3._
import chisel3.stage.ChiselStage
import click._

/**
 * A Fibonacci circuit
 * @param dataWidth Width of the data being passed around the circuit
 */
class Fib(dataWidth: Int)(implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val go = Input(Bool())
    val out = Flipped(new ReqAck(UInt(dataWidth.W)))
  })

  //Create modules
  val R0 = Module(new HandshakeRegister(0.U(dataWidth.W)))
  val RF0 = Module(RegFork(1.U(dataWidth.W), true))
  val RF1 = Module(RegFork(1.U(dataWidth.W), true))
  val add = Module(Adder(dataWidth))

  add.io.out <> R0.io.in
  R0.io.out <> RF0.io.in
  RF0.io.out1 <> RF1.io.in
  RF0.io.out2 <> add.io.in1

  RF0.io.out1 <> RF1.io.in
  RF0.io.out2 <> add.io.in1

  RF1.io.out1 <> io.out
  RF1.io.out2 <> add.io.in2

  //Handle resets and barrier signal
  R0.io.reset := this.reset.asAsyncReset
  add.io.in2.req := RF1.io.out2.req && io.go
}

class FibTop extends Module {
  val io = IO(new Bundle {
    val go = Input(Bool())
    val out = Flipped(new ReqAck(UInt(12.W)))
    val ackOut = Output(Bool())
  })

  val sync = Module(new InputSync)
  val conf = ClickConfig(SIMULATION = false)
  val fib = Module(new Fib(12)(conf))

  sync.io.in.ack := io.out.ack
  sync.io.in.req := false.B //Don't need this one
  fib.io.out.ack := sync.io.out.ack
  fib.io.go := io.go

  io.out.req := fib.io.out.req
  io.out.data := fib.io.out.data
  io.ackOut := sync.io.out.ack
}

object Fib extends App {
  (new ChiselStage).emitVerilog(new FibTop)
}