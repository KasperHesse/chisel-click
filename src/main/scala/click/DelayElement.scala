package click

import chisel3._
import chisel3.util._

class DelayElement(delay: Int = 1) extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val reqIn = Input(Bool())
    val reqOut = Output(Bool())
  })

  override val desiredName = s"DelayElement_$delay"
  require(delay > 0)
  setInline(s"DelayElement_$delay.v",
    s"""module DelayElement_$delay (
      | input reqIn,
      | output reg reqOut
      |);
      |always@(reqIn) begin
      | #$delay reqOut <= reqIn;
      |end
      |endmodule""".stripMargin)
}
