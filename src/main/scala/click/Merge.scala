package click

import chisel3._

/**
 * A Merge-component, used to perform a data merge operation.
 * This component expects its input to be mutually exclusive. If they are not, the behaviour is undefined.
 * Whichever input is currently active has its input forwarded to the output
 * @param typ The type of data to use on this channel
 * @tparam T
 */
class Merge[T <: Data](typ: T)
                      (implicit conf: ClickConfig) extends Module with RequireAsyncReset {
  val io = IO(new Bundle {
    val in1 = new ReqAck(typ)
    val in2 = new ReqAck(typ)
    val out = Flipped(new ReqAck(typ))
  })

  val Pa = Module(new PhaseRegister(false))
  val Pb = Module(new PhaseRegister(false))
  val Pc = Module(new PhaseRegister(false))

  val selA = io.in1.req ^ io.in1.ack
  val selB = io.in2.req ^ io.in2.ack

  val clickIn = !(io.out.req ^ io.out.ack)
  val clickOut = selA || selB

  Pa.io.in := io.in1.req
  Pa.clock := clickIn.asClock

  Pb.io.in := io.in2.req
  Pb.clock := clickIn.asClock

  Pc.io.in := !Pc.io.out
  Pc.clock := clickOut.asClock

  io.in1.ack := Pa.io.out
  io.in2.ack := Pb.io.out
  io.out.data := Mux(selA, io.in1.data, io.in2.data)
  io.out.req := simDelay(Pc.io.out, conf.MERGE_DELAY)

}
