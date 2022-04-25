import chisel3._

package object click {
  class ReqAck[T <: Data](gen: T) extends Bundle {
    val req = Input(Bool())
    val ack = Output(Bool())
    val data = Input(gen)
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
