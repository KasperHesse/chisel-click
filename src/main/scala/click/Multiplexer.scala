package click

import chisel3._

/**
 * A 2:1 multiplexer, using a single wire as its select-signal.
 * When sel=0, in1 is forwarded to the output. When sel=1, in2 is forwarded
 * The behavior is undefined if either input attempts to perform a handshake when the other input has yet to
 * finish its handshake
 * Note that this behavior is different from the behavior in the book by JSPA, as the behavior of the
 * select-signal has been flipped
 * @param gen The datatype to be switched
 * @param conf Configuration object
 * @tparam T
 */
class Multiplexer[T <: Data](gen: T)(implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val in1 = new ReqAck(gen)
    val in2 = new ReqAck(gen)
    val sel = new ReqAck(Bool())
    val out = Flipped(new ReqAck(gen))
  })

  //Phase registers
  val Pa = Module(new PhaseRegister(false))
  val Pb = Module(new PhaseRegister(false))
  val Pc = Module(new PhaseRegister(false))
  val Ps = Module(new PhaseRegister(false))

  //Inputs for phase registers
  Pa.io.in := Pa.io.out ^ !io.sel.data
  Pb.io.in := Pb.io.out ^ io.sel.data
  Pc.io.in := !Pc.io.out
  Ps.io.in := io.sel.req

  Pa.io.reset := this.reset.asAsyncReset
  Pb.io.reset := this.reset.asAsyncReset
  Pc.io.reset := this.reset.asAsyncReset
  Ps.io.reset := this.reset.asAsyncReset

  //Clock for phase registers
  val selX = io.sel.req ^ io.sel.ack
  val in1X = io.in1.req ^ io.in1.ack
  val in2X = io.in2.req ^ io.in2.ack
  val clickIn = (!(io.out.ack ^ io.out.req)).asClock
  val clickOut = (selX && io.sel.data && in2X) || (selX && !io.sel.data && in1X)

  Pa.io.clock := clickIn
  Pb.io.clock := clickIn
  Ps.io.clock := clickIn
  Pc.io.clock := clickOut.asClock

  //Drive outputs
  io.in1.ack := Pa.io.out
  io.in2.ack := Pb.io.out
  io.sel.ack := Ps.io.out
  io.out.req := simDelay(Pc.io.out, conf.MUX_DELAY)
  io.out.data := Mux(io.sel.data, io.in2.data, io.in1.data)
}
