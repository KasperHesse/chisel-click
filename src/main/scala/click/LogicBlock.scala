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
 * @tparam T1 The type of input arguments. They are assumed to be the same
 * @tparam T2 The type of output argument
 */
class LogicBlock[T1 <: Data, T2 <: Data](gen1: T1, gen2: T2, f: T1 => T2) extends Module {
  val io = IO(new Bundle {
    val in = new ReqAck(gen1)
    val out = Flipped(new ReqAck(gen2))
  })

  io.out.req := io.in.req
  io.in.ack := io.out.ack

  io.out.data := f(io.in.data)
}

/**
 * A logic block implementing an adder
 * @param gen1 Type of the first data input
 * @param gen2 Type of the second data input
 * @param gen3 Type of the data output
 * @tparam T1
 * @tparam T2
 * @tparam T3
 */
class Adder[T1 <: Data, T2 <: Data, T3 <: Data](gen1: T1, gen2: T2, gen3: T3) extends Module {
  class inp extends Bundle {
    val a = Output(gen1) //Using Output() to avoid clone type issues, fields are actually inputs
    val b = Output(gen2)
  }
  val io = IO(new Bundle {
    val in = new ReqAck(new inp)
    val out = Flipped(new ReqAck(gen3))
  })

  def add(in: inp): UInt = in.a.asUInt + in.b.asUInt
  val logic = Module(new LogicBlock(new inp, gen3, add))

  logic.io.in.req := io.in.req
  logic.io.in.data := io.in.data
  logic.io.out.ack := io.out.ack

  io.in.ack := logic.io.in.ack
  io.out.req := logic.io.out.req
  io.out.data := logic.io.out.data
}

object LogicBlock {

}

object Adder {
  /**
   * Creates an N-bit adder taking UInt as its input, returning a UInt of the same width
   * @param width
   */
  def apply(width: Int): Adder[UInt, UInt, UInt] = {
    new Adder(UInt(width.W), UInt(width.W), UInt(width.W))
  }
}
