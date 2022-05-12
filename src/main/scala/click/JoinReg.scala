package click

import chisel3._
import chisel3.util._

/**
 * The JoinReg block utilizes peephole optimizations to implement a join block and register in the same module.
 * The JoinReg-module supports any type of forking behavior, but if a simpler behavior is wanted (such as two
 * input UInts being concatenated onto the output channel), the companion object contains methods to instantiate this.
 * @param typ1 The type of the first input
 * @param typ2 The type of the second input
 * @param init3 The initial/reset value of the output register
 * @param ro Initial value of the control circuit's out.req signal
 * @param join The function used to join the two input stream's data
 * @tparam T1
 * @tparam T2
 * @tparam T3
 */
class JoinReg[T1 <: Data, T2 <: Data, T3 <: Data]
(typ1: T1, typ2: T2, init3: T3, ro: Boolean, join: (T1, T2) => T3)
(implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val in1 = new ReqAck(typ1)
    val in2 = new ReqAck(typ2)
    val out = Flipped(new ReqAck(chiselTypeOf(init3)))
  })

  val click = simDelay((io.in1.req ^ io.in1.ack) && (io.in2.req ^ io.in2.ack) && !(io.out.req ^ io.out.ack),
    conf.REG_DELAY + conf.FORK_DELAY/2).asClock
  val Pa = Module(new PhaseRegister(false))
  val Pb = Module(new PhaseRegister(false))
  val Pc = Module(new PhaseRegister(ro))
  val reg = Module(new CustomClockRegister(init3))

  Pa.io.clock := click
  Pa.io.reset := this.reset.asAsyncReset
  Pa.io.in := !Pa.io.out

  Pb.io.clock := click
  Pb.io.reset := this.reset.asAsyncReset
  Pb.io.in := !Pb.io.out

  Pc.io.clock := click
  Pc.io.reset := this.reset.asAsyncReset
  Pc.io.in := !Pc.io.out

  reg.io.clock := click
  reg.io.reset := this.reset.asAsyncReset
  reg.io.in := join(io.in1.data, io.in2.data)

  io.out.data := reg.io.out
  io.in1.ack := Pa.io.out
  io.in2.ack := Pb.io.out
  io.out.req := Pc.io.out
}

object JoinReg {

  /**
   * Creates a JoinReg component where all datastreams are UInt's, and the data is joined by concatenating the
   * two inputs. Input 1 is placed in the MSB of the output, input 2 in the LSB
   * @param widthIn Width of the input channels
   * @param defaultOut Default/reset value of the output
   * @param ro Initial value of the control circuit's out.req signal
   * @return
   */
  def apply(widthIn: Int, defaultOut: Int, ro: Boolean)(implicit conf: ClickConfig): JoinReg[UInt, UInt, UInt] = {
    new JoinReg(UInt(widthIn.W), UInt(widthIn.W), defaultOut.U((2*widthIn).W), ro, (a: UInt, b: UInt) => Cat(a,b))
  }
}
