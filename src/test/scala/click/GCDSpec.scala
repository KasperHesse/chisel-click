package click

import chisel3._
import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chiseltest._
import click.HandshakeTesting.HandshakeDriver
import examples.GCD
import org.scalatest.flatspec.AnyFlatSpec

import scala.math.pow
import scala.util.Random

class GCDSpec extends AnyFlatSpec with ChiselScalatestTester {

  "GCD" should "determine greatest common divisor" in {
    val width = 5
    test(new GCD(width)(ClickConfig(SIMULATION = true)))
      .withAnnotations(Seq(IcarusBackendAnnotation, WriteVcdAnnotation)) { dut =>

      // initialize handshake port drivers
      dut.io.in.initSource(dut.clock)
      dut.io.out.initSink(dut.clock)

      // generate test operand pairs
      val tests = Seq.fill(100) {
        (Random.between(1, pow(2, width).toInt),
          Random.between(1, pow(2, width).toInt))
      }

      // function to compute gcd
      def gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

      // function to generate input bundle with operands
      def pair(a: Int, b: Int) =
        new Bundle2(UInt(width.W), UInt(width.W)).Lit(_.a -> a.U, _.b -> b.U)

      fork { // operand sending thread
        for ((a,b) <- tests) {
          dut.io.in.send(pair(a,b))
        }
      }.fork { // result receiving thread
        for ((a, b) <- tests) {
          val res = gcd(a, b)
          dut.io.out.receiveExpect(pair(res, res))
        }
      }.join()
    }
  }
}
