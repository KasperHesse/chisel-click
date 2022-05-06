package click

import chisel3._

/**
 * A Fork-component, taking some data as its input and propagating that data to two outputs
 * The output data may either be duplicated on both outputs, or the outputs may get a subset
 * of the input data, as defined by the `fork` function given as parameter
 * Use the companion object to more easily instantiate the simplified version
 * @param typIn The datatype on the input
 * @param typOut1 The datatype on the first output
 * @param typOut2 The datatype on the second output
 * @param fork A function used to perform the forking behaviour. The first output is assigned to the out1-channel,
 *             the second output is assigned to the out2-channel
 * @tparam T1
 * @tparam T2
 * @tparam T3
 */
class Fork[T1 <: Data, T2 <: Data, T3 <: Data](typIn: T1, typOut1: T2, typOut2: T3, fork: T1 => (T2, T3)) extends Module {
  val io = IO(new Bundle {
    val in = new ReqAck(typIn)
    val out1 = Flipped(new ReqAck(typOut1))
    val out2 = Flipped(new ReqAck(typOut2))
  })

  val click = (!io.in.ack && io.out2.ack && io.out1.ack) || (io.in.ack && !io.out2.ack && !io.out1.ack)

  val phase = Module(new PhaseRegister(false))
  phase.io.clock := click.asClock
  phase.io.reset := this.reset.asAsyncReset
  phase.io.in := !phase.io.out

  io.in.ack := phase.io.out
  io.out1.req := io.in.req
  io.out2.req := io.in.req

  val d = fork(io.in.data)
  io.out1.data := d._1
  io.out2.data := d._2
}

object Fork {

  /**
   * Generates a Fork module which duplicates its input on both outputs
   * @param typ The type of data being forked
   * @tparam T
   * @return
   */
  def apply[T <: Data](typ: T): Fork[T, T, T] = {
    new Fork(typ, typ, typ, (a: T) => (a, a))
  }
}