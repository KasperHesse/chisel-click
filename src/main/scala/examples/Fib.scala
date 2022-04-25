package examples

import chisel3._
import click._

class Fib(dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val out = Output(UInt(dataWidth.W))
    val valid = Output(Bool())
  })

  //Create our 6 handshake latches
  val R0 = Module(new HandshakeRegister(0.U(dataWidth.W)))
  val R1 = Module(new HandshakeRegister(1.U(dataWidth.W)))
  val R2 = Module(new HandshakeRegister(0.U(dataWidth.W)))


  def add(a: Bundle): UInt = {
    val tpe = new Bundle {
      val a = UInt(8.W)
      val b = UInt(8.W)
    }
    val A = a.asTypeOf(tpe)
    A.a + A.b
  }

  val x = {a: UInt => a(15,8) + a(7,0)}

  val CL1 = Module(new LogicBlock[UInt, UInt](UInt(16.W), UInt(8.W), x))

  val CL0 = Module(new LogicBlock[UInt, UInt](UInt(16.W), UInt(8.W), x))
}
