import chisel3._

package object click {

  /**
   * Generates a delay element (if required), connecting the input signal and returning a handle
   * to the output signal
   * @param reqIn The input signal that should be delayed
   * @param delay The default delay value from the configuration used.
   * @param conf The configuration object in use
   * @return
   */
  def simDelay(reqIn: Bool, delay: Int)(implicit conf: ClickConfig): Bool = {
    if(delay > 0) {
      //Custom delay is set
      val d = Module(DelayElement(delay))
      d.io.reqIn := reqIn
      d.io.reqOut
    } else {
      //No delay
      reqIn
    }
  }
  class ReqAck[T <: Data](gen: T) extends Bundle {
    val req = Input(Bool())
    val ack = Output(Bool())
    val data = Input(gen)
  }

  /**
   * A bundle holding two data items, making it easier to implement fork/join and logic blocks taking multiple inputs
   * @param gen1
   * @param gen2
   * @tparam T1
   * @tparam T2
   */
  class Bundle2[T1 <: Data, T2 <: Data](gen1: T1, gen2: T2) extends Bundle {
    val a = Output(gen1) //Using Output() to avoid clone type issues, fields are actually inputs
    val b = Output(gen2)
  }

  /**
   * An I/O bundle for a handshake port on a module
   * @param typ The datatype on the input and output of this block
   * @tparam T
   */
  class HandshakeIO[T <: Data](typ: T) extends Bundle {
    /** Handshake on input */
    val in = new ReqAck(typ)
    /** Input reset signal */
    val reset = Input(AsyncReset())
    /** Handshake on output */
    val out = Flipped(new ReqAck(typ))
  }

  /**
   * An I/O bundle for a handshake port on a module where the input and output datatypes are not the same
   * @param typ1 The input datatype
   * @param typ2 The output datatype
   * @tparam T1
   * @tparam T2
   */
  class OddHandshakeIO[T1<: Data, T2 <: Data](typ1: T1, typ2: T2) extends Bundle {
    /** Input handshake */
    val in = new ReqAck(typ1)
    /** Output handshake */
    val out = Flipped(new ReqAck(typ2))
  }

  /** Helper object to create new data items wrapped in a handshake */
  object HandshakeIO {
    def apply[T <: Data](gen: T): HandshakeIO[T] = new HandshakeIO(gen)
  }
}
