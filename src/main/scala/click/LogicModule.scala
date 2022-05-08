package click

import chisel3._
import chisel3.experimental.DataMirror

import scala.collection.immutable.SeqMap

/**
 * This trait must be added to modules containing logic function which should be embedded in a [[LogicModule]]
 * The functions `setInput` and `getOutput` should be used to drive the inputs
 * and retrieve outputs from the embedded module
 * @tparam T1
 * @tparam T2
 */
trait IsLogicModule[T1 <: Data, T2 <: Data] {
  /**
   * Drives the inputs of the module implementing a logic function
   * @param inp The input from the [[LogicModule]] (`io.in.data`)
   */
  def setInputs(inp: T1): Unit

  /**
   * Drives the outputs of the wrapping LogicModule
   * @param out The output port `io.out.data` from the wrapping LogicModule
   */
  def setOutputs(out: T2): Unit
}

/**
 * A variant on [[LogicBlock]] where the logic function is implemented in another Module.
 * This serves to easily migrate existing logic components into an asynchronous pipeline, as a LogicModule
 * wraps the logic with a req/ack handshake for asynchronous implementation
 * @param typ1 The datatype on the input of the logic module
 * @param typ2 The datatype on the output of the logic module
 * @param mdl The module implementing the combinational logic
 * @param delay The delay of the delay element associated with this logic
 * @tparam T1
 * @tparam T2
 */
class LogicModule[T1 <: Data, T2 <: Data](typ1: T1, typ2: T2, mdl: => RawModule with IsLogicModule[T1, T2], delay: Int)(implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val in = new ReqAck(typ1)
    val out = Flipped(new ReqAck(typ2))
  })

  val logic = mdl
  logic.setInputs(io.in.data)
  logic.setOutputs(io.out.data)
  io.in.ack := io.out.ack
  io.out.req := synthDelay(io.in.req, delay)
}
