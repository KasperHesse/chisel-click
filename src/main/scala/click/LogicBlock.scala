package click

import chisel3._
import chisel3.util._

/**
 * A generic logic block that can be used to create a logic function.
 * This logic block takes a function as its argument, using that function to drive the output.
 * It should only be used for simple functions - for more complex logic blocks,
 * consider using [[LogicBlockMod]] which instantiates a Module to drive its outputs
 * @param gen1 The datatype on the input
 * @param gen2 The datatype on the output
 * @param f The function to compute
 * @param delay The delay to use for the logic block's request signal
 * @tparam T1 The type of input arguments. They are assumed to be the same
 * @tparam T2 The type of output argument
 */
class LogicBlock[T1 <: Data, T2 <: Data](gen1: T1, gen2: T2, f: T1 => T2, delay: Int)(implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val in = new ReqAck(gen1)
    val out = Flipped(new ReqAck(gen2))
  })

  val de = Module(DelayElement(delay))
  de.io.reqIn := io.in.req
  io.out.req := de.io.reqOut
  io.in.ack := io.out.ack

  io.out.data := f(io.in.data)
}

/**
 * A logic block implementing an adder.
 * The adder consists of a Join-block, followed by a Logic block implementing addition
 * @param gen1 Type of the first data input
 * @param gen2 Type of the second data input
 * @param gen3 Type of the data output
 * @param delay The delay of the adder. Defaults to -1, meaning that it should follow
 *              the value of [[ClickConfig.ADD_DELAY]] set in the implicit configuration object
 * @tparam T1
 * @tparam T2
 * @tparam T3
 */
class Adder[T1 <: Bits, T2 <: Bits, T3 <: Bits](gen1: T1, gen2: T2, gen3: T3, delay: Int = -1)(implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val in1 = new ReqAck(gen1)
    val in2 = new ReqAck(gen2)
    val out = Flipped(new ReqAck(gen3))
  })

  val joinDelays = Seq.fill(2)(Module(DelayElement(2)))
  val join = Module(new Join(gen1, gen2, new Bundle2(gen1, gen2), (a: T1, b: T2) => {
    val r = Wire(new Bundle2(gen1, gen2))
    r.a := a
    r.b := b
    r
  }))

  val logic = Module(new LogicBlock(new Bundle2(gen1, gen2), gen3, (in: Bundle2[T1, T2]) => in.a.asUInt + in.b.asUInt, if(delay == -1) conf.ADD_DELAY else delay))

  joinDelays(0).io.reqIn := io.in1.req
  joinDelays(1).io.reqIn := io.in2.req
  join.io.in1 <> io.in1
  join.io.in2 <> io.in2
  join.io.in1.req := joinDelays(0).io.reqOut
  join.io.in2.req := joinDelays(1).io.reqOut

  logic.io.in <> join.io.out

  io.out <> logic.io.out
}

object Adder {
  /**
   * Creates an N-bit adder taking UInts as its inputs, returning a UInt of the same width
   * @param width The width of the adder
   */
  def apply(width: Int)(implicit conf: ClickConfig): Adder[UInt, UInt, UInt] = {
    new Adder(UInt(width.W), UInt(width.W), UInt(width.W))
  }
}
