package click

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ClickElementSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Click element"

  it should "forward a request" in {
    test(new ClickElement()) {dut =>
      dut.io.reqIn.poke(false.B)
      dut.io.ackOut.poke(false.B)
      dut.io.ackIn.expect(false.B)
      dut.io.reqOut.expect(false.B)
    }
  }
}
