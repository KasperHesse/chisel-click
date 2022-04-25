package click

import chisel3._

/**
 * A Join-component that can be used to merge two handshake channels into one channel.
 * It takes data on two inputs, combines that data into one aggregate type and forwards that data on the output
 *
 * @param typIn1 The datatype on the first input channel
 * @param typIn2 The datatype on the second input channel
 * @param typOut The datatype on the output channel
 * @param join A function used to perform the joining behavior. The first argument is taken from the in1-channel,
 *             the second argument from the in2-channel
 * @tparam T1
 * @tparam T2
 * @tparam T3
 */
class Join[T1 <: Data, T2 <: Data, T3 <: Data](typIn1: T1, typIn2: T2, typOut: T3, join: (T1, T2) => T3) extends Module {
  val io = IO(new Bundle {
    val in1 = new ReqAck(typIn1)
    val in2 = new ReqAck(typIn2)
    val out = Flipped(new ReqAck(typOut))
  })

  val click = (!io.out.req && io.in1.req && io.in2.req) || (io.out.req && !io.in1.req && !io.in2.req)
  val phase = Module(new PhaseRegister(false))

  phase.io.clock := click.asClock
  phase.io.reset := this.reset.asAsyncReset
  phase.io.in := !phase.io.out

  io.out.req := phase.io.out
  io.in1.ack := io.out.ack
  io.in2.ack := io.out.ack

  io.out.data := join(io.in1.data, io.in2.data)
}

object Join {

  /**
   * Creates a simple Join-component which takes two inputs of width `width`, concatenating them
   * into an output of width `2*width`. The input on channel in1 is placed in the LSB, and the input
   * on channel in2 in the MSB of the output
   * @param width The bitwidth of the two inputs
   * @return
   */
  def apply(width: Int): Join[UInt, UInt, UInt] = {

    def join(in1: UInt, in2: UInt): UInt = {
      util.Cat(in2, in1)
    }
    new Join[UInt, UInt, UInt](UInt(width.W), UInt(width.W), UInt((2*width).W), join)
  }
}