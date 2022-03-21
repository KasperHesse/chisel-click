import chisel3._

package object click {
  class ReqAck[T <: Data](gen: T) extends Bundle {
    val req = Input(Bool())
    val ack = Output(Bool())
    val data = Input(gen)
  }

  class HandshakeIO[T <: Data](gen: T) extends Bundle {
    /** Handshake on input */
    val in = new ReqAck(gen)
    /** Handshake on output */
    val out = Flipped(new ReqAck(gen))
  }

  /** Helper object to create new data items wrapped in a handshake */
  object HandshakeIO {
    def apply[T <: Data](gen: T): HandshakeIO[T] = new HandshakeIO(gen)
  }
}
