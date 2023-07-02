package click

import chisel3._
import chisel3.experimental.DataMirror

import scala.collection.immutable.SeqMap

/**
 * This trait must be added to modules containing logic function which should be embedded in a [[LogicModule]]
 * The functions `getInput` and `getOutput` should be used to get a handle to the input and output signals
 * of the module.
 * @tparam T1
 * @tparam T2
 */
trait IsLogicModule[T1 <: Data, T2 <: Data] {
  /**
   * Gets a handle to the input port of the module implementing a logic function.
   * This allows it to be driven by the wrapping LogicModule
   * @return The input port
   */
  def getInput(): T1

  /**
   * Gets a handle to the output port of the module implementing a logic function.
   * This allows the output to drive the output of the wrapping LogicModule
   * @return The output port
   */
  def getOutput(): T2
}

/**
 * A variant on [[FunctionBlock]] where the logic function is implemented in another Module.
 * This serves to easily migrate existing logic components into an asynchronous pipeline, as a LogicModule
 * wraps the logic with a req/ack handshake for asynchronous implementation
 * @param typ1 The datatype on the input of the logic module
 * @param typ2 The datatype on the output of the logic module
 * @param mdl The module implementing the combinational logic
 * @param delay The delay of the delay element associated with this logic
 * @tparam T1
 * @tparam T2
 */
class LogicModule[T1 <: Data, T2 <: Data](typ1: T1, typ2: T2, mdl: => RawModule with IsLogicModule[T1, T2], delay: Int)
                                         (implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val in = new ReqAck(typ1)
    val out = Flipped(new ReqAck(typ2))
  })

  val logic = mdl

  //Functions are used to connect inputs and outputs of wrapping and wrapped module
  logic.getInput() := io.in.data
  io.out.data := logic.getOutput()
  io.in.ack := io.out.ack
  io.out.req := synthDelay(io.in.req, delay)
}
