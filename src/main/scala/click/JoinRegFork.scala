package click

import chisel3._
import chisel3.util._

/**
 * The JoinRegFork-block leverages common optimization opportunities in a join, register and fork component
 * The input data is joined and subsequently forked according to the [[joinfork]] function
 * In the majority of cases, the input data is joined and then duplicated on both output channels.
 * In that case, using the companion object [[JoinRegFork]] to instantiate a symmetric JoinRegFork will be simpler
 * @param typ1 The datatype of the first input
 * @param typ2 The datatype of the second input
 * @param init3 The initial value of the first output
 * @param init4 The initial value of the second output
 * @param ro3   The initial value of the first output's out.req signal
 * @param ro4   The initial value of the second output's out.req signal
 * @param joinfork The function used to join input data and fork output data
 *
 */
class JoinRegFork[T1 <: Data, T2 <: Data, T3 <: Data, T4 <: Data](typ1: T1, typ2: T2, init3: T3, init4: T4, ro3: Boolean, ro4: Boolean)
                                                                 (joinfork: (T1, T2) => (T3, T4))
                                                                 (implicit conf: ClickConfig) extends Module with RequireAsyncReset {
  val io = IO(new Bundle {
    val in1 = new ReqAck(typ1)
    val in2 = new ReqAck(typ2)
    val out1 = Flipped(new ReqAck(chiselTypeOf(init3)))
    val out2 = Flipped(new ReqAck(chiselTypeOf(init4)))
  })

  //Phase registers and data register
  val Pa = Module(new PhaseRegister(false))
  val Pb = Module(new PhaseRegister(false))
  val Pc = Module(new PhaseRegister(ro3))
  val Pd = Module(new PhaseRegister(ro4))
  val reg1 = Module(new CustomClockRegister(init3))
  val reg2 = Module(new CustomClockRegister(init4))
  
  //Phase and handshake register signals
  val click = simDelay((io.in1.req ^ io.in1.ack) && (io.in2.req ^ io.in2.ack) && !(io.out1.req ^ io.out1.ack) && !(io.out2.req ^ io.out2.ack),
    conf.REG_DELAY + (conf.FORK_DELAY+conf.JOIN_DELAY)/2).asClock
  
  Pa.clock := click
  Pa.io.in := !Pa.io.out

  Pb.clock := click
  Pb.io.in := !Pb.io.out
  
  Pc.clock := click
  Pc.io.in := !Pc.io.out
  
  Pd.clock := click
  Pd.io.in := !Pd.io.out
  
  reg1.clock := click
  reg2.clock := click

  val (din1, din2) = joinfork(io.in1.data, io.in2.data)
  reg1.io.in := din1
  reg2.io.in := din2

  //Assign outputs
  io.out1.data := reg1.io.out
  io.out2.data := reg2.io.out
  io.out1.req := Pc.io.out
  io.out2.req := Pd.io.out
  io.in1.ack := Pa.io.out
  io.in2.ack := Pb.io.out
}

object JoinRegFork {
  /**
   * Creates a JoinRegFork block taking two UInt's on the input, concatenating them (in1 in the MSB, in2 in the LSB),
   * and then duplicates this on the output
   * @param widthIn Width of both input channels.
   * @param valueOut Default value of the output channels. Width is 2*widthIn
   * @param ro Initial value of the control circuit's out.req signal
   * @param conf Configuration object
   * @return
   */
  def apply(widthIn: Int, valueOut: Int, ro: Boolean)(implicit conf: ClickConfig): JoinRegFork[UInt, UInt, UInt, UInt] = {
    def joinfork(a: UInt, b: UInt): (UInt, UInt) = {
      val c = Cat(a, b)
      (c, c)
    }
    new JoinRegFork(UInt(widthIn.W), UInt(widthIn.W), valueOut.U((2*widthIn).W), valueOut.U((2*widthIn).W), ro, ro)(joinfork)
  }

  /**
   * Creates a JoinRegFork block taking two inputs of the same type, passing these onto the outputs without
   * modifying the data. It can be used as a structure for synchronizing multiple dataflows without affecting
   * the underlying data
   * @param init The initial value of both output channels
   * @param ro Initial value of the control circuit's out.req signals
   * @tparam T
   * @return
   */
  def apply[T <: Data](init: T, ro: Boolean)(implicit conf: ClickConfig): JoinRegFork[T, T, T, T] = {
    require(init.isWidthKnown, "The width of the init signal must be known")
    new JoinRegFork(chiselTypeOf(init), chiselTypeOf(init), init, init, ro, ro)((a,b) => (a, b))
  }
}
