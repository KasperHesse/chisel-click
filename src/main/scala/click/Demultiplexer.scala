package click

import chisel3._

/**
 * A demultiplexer takes the input data and forwards it to one of the output channels,
 * depending on which channel is triggered by the select signal.
 * If sel.data=0, the input is forwarded to output channel out1
 * If sel.data=1, the input is forwarded to output channel out2
 *
 * Note that the behavior is different from that of the demultiplexer in the book by JSPA,
 * as the behavior of the select-signal has been flipped
 * @param gen The datatype switched by this demultiplexer
 */
class Demultiplexer[T <: Data](gen: T)
                              (implicit conf: ClickConfig) extends Module with RequireAsyncReset {
  val io = IO(new Bundle {
    val in = new ReqAck(gen)
    val sel = new ReqAck(Bool())
    val out1 = Flipped(new ReqAck(gen))
    val out2 = Flipped(new ReqAck(gen))
  })

  //Phase registers
  val Pa = Module(new PhaseRegister(false))
  val Pb = Module(new PhaseRegister(false))
  val Pc = Module(new PhaseRegister(false))

  //Clocking for phase registers
  val clickIn = (!(io.out1.req ^ io.out1.ack) && !(io.out2.req ^ io.out2.ack)).asClock
  val clickOut = simDelay((io.in.req ^ io.in.ack) && (io.sel.req ^ io.sel.ack), conf.DEMUX_DELAY).asClock

  //Inputs to phase registers
  Pa.clock := clickIn
  Pa.io.in := !Pa.io.out

  Pb.clock := clickOut
  Pb.io.in := !io.sel.data ^ Pb.io.out

  Pc.clock := clickOut
  Pc.io.in := io.sel.data ^ Pc.io.out

  //Outputs
  io.out1.req := Pb.io.out
  io.out2.req := Pc.io.out
  io.out1.data := io.in.data
  io.out2.data := io.in.data
  io.in.ack := Pa.io.out
  io.sel.ack := Pa.io.out
}
