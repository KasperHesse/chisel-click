package click

import chisel3._

/**
 * The RegFork block utilizes peephole optimizations to implement a register and fork statement in the same module
 * The RegFork-module supports any type of forking behavior, but if a simpler behavior is wanted (such as a the data
 * being mirrored on both output channels), the companion objects contains methods to instantiate these
 * @param typ1 The datatype of the input
 * @param init2 The default value on the first output channel
 * @param init3 The default value on the second output channel
 * @param ri2 Whether the first output channel is initialized with out.req high (true) or low (false)
 * @param ri3 Whether the second output channel is initialized with out.req high (true) or low (false)
 * @param fork The function used to implement the forking behavior
 * @param name A custom name for this RegFork module, if desired. Defaults to "RegFork"
 */
class RegFork[T1 <: Data, T2 <: Data, T3 <: Data]
  (typ1: T1, init2: T2, init3: T3, ri2: Boolean, ri3: Boolean, fork: T1 => (T2, T3), name: String = "RegFork")
  (implicit conf: ClickConfig) extends Module {

  val io = IO(new Bundle {
    val in = new ReqAck(typ1)
    val out1 = Flipped(new ReqAck(chiselTypeOf(init2)))
    val out2 = Flipped(new ReqAck(chiselTypeOf(init3)))
  })

  //Modules
  val Pa = Module(new PhaseRegister(false))
  val Pb = Module(new PhaseRegister(ri2))
  val Pc = Module(new PhaseRegister(ri3))

  val reg1 = Module(new CustomClockRegister(init2))
  val reg2 = Module(new CustomClockRegister(init3))

  //Clocking logic
  val click = simDelay((io.in.req ^ io.in.ack) && !(io.out1.req ^ io.out1.ack) && !(io.out2.req ^ io.out2.ack), conf.REG_DELAY).asClock
  Pa.io.clock := click
  Pa.io.reset := this.reset.asAsyncReset
  Pa.io.in := !Pa.io.out

  Pb.io.clock := click
  Pb.io.reset := this.reset.asAsyncReset
  Pb.io.in := !Pb.io.out

  Pc.io.clock := click
  Pc.io.reset := this.reset.asAsyncReset
  Pc.io.in := !Pc.io.out

  val (din1, din2) = fork(io.in.data)
  reg1.io.clock := click
  reg1.io.reset := this.reset.asAsyncReset
  reg1.io.in := din1
  reg2.io.clock := click
  reg2.io.reset := this.reset.asAsyncReset
  reg2.io.in := din2

  //Outputs
  io.out1.req := Pb.io.out
  io.out2.req := Pc.io.out
  io.out1.data := reg1.io.out
  io.out2.data := reg2.io.out
  io.in.ack := Pa.io.out
}

object RegFork {
  /**
   * Creates a RegFork-block which duplicates its input data on both output channels.
   * The output channels are initialized to hold the same data, and both either have out.req high or low when starting
   * @param init The initial value of the output channel. This also defines the datatype used
   * @param ri Whether out.req should start high (true) or low (false). Defaults to low / false
   * @tparam T
   * @return
   */
  def apply[T <: Data](init: T, ri: Boolean = false)(implicit conf: ClickConfig): RegFork[T, T, T] = {
    require(init.isWidthKnown, "The width of the init signal must be known")
    new RegFork(chiselTypeOf(init), init, init, ri, ri, (a: T) => (a, a))
  }
}
